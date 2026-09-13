package org.sitemesh.config;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

/**
 * High-performance, fully compatible SiteMesh 3 Filter for Jakarta EE (Servlet 6.0 / Tomcat 11).
 * Resolves the Tomcat 11 IllegalStateException / blank screen caused by forward()
 * by intercepting output and decorating using standard include().
 */
public class ConfigurableSiteMeshFilter implements Filter {

    private static final Pattern TITLE_PATTERN = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern HEAD_PATTERN = Pattern.compile("<head[^>]*>(.*?)</head>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern BODY_PATTERN = Pattern.compile("<body[^>]*>(.*?)</body>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern WRITE_TITLE_PATTERN = Pattern.compile("<sitemesh:write[^>]*property=['\"]title['\"][^>]*/>|<sitemesh:write[^>]*property=['\"]title['\"][^>]*>.*?</sitemesh:write>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern WRITE_HEAD_PATTERN = Pattern.compile("<sitemesh:write[^>]*property=['\"]head['\"][^>]*/>|<sitemesh:write[^>]*property=['\"]head['\"][^>]*>.*?</sitemesh:write>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern WRITE_BODY_PATTERN = Pattern.compile("<sitemesh:write[^>]*property=['\"]body['\"][^>]*/>|<sitemesh:write[^>]*property=['\"]body['\"][^>]*>.*?</sitemesh:write>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static class Mapping {
        final String pathPattern;
        final boolean isExclude;
        final String decorator;

        Mapping(String pathPattern, boolean isExclude, String decorator) {
            this.pathPattern = pathPattern;
            this.isExclude = isExclude;
            this.decorator = decorator;
        }
    }

    private final List<Mapping> mappings = new ArrayList<>();
    private FilterConfig filterConfig;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        loadConfiguration();
    }

    private void loadConfiguration() {
        mappings.clear();
        InputStream is = null;
        try {
            if (filterConfig != null && filterConfig.getServletContext() != null) {
                is = filterConfig.getServletContext().getResourceAsStream("/WEB-INF/sitemesh3.xml");
                if (is == null) {
                    is = filterConfig.getServletContext().getResourceAsStream("/WEB-INF/sitemesh.xml");
                }
            }

            if (is != null) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(is);
                NodeList mappingNodes = doc.getElementsByTagName("mapping");

                for (int i = 0; i < mappingNodes.getLength(); i++) {
                    Element el = (Element) mappingNodes.item(i);
                    String path = el.getAttribute("path");
                    boolean exclude = "true".equalsIgnoreCase(el.getAttribute("exclude"));
                    String decorator = el.getAttribute("decorator");

                    if (path != null && !path.isEmpty()) {
                        mappings.add(new Mapping(path, exclude, decorator));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[SiteMesh] Warning: Could not parse sitemesh configuration: " + e.getMessage());
        } finally {
            if (is != null) {
                try { is.close(); } catch (IOException ignored) {}
            }
        }

        // Fallback default rules if none configured
        if (mappings.isEmpty()) {
            mappings.add(new Mapping("/admin/*", false, "/decorators/admin.jsp"));
            mappings.add(new Mapping("/*", false, "/decorators/web.jsp"));
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = (contextPath != null && uri.startsWith(contextPath)) ? uri.substring(contextPath.length()) : uri;
        if (path.isEmpty()) path = "/";

        // Never decorate decorators themselves or internal files
        if (path.startsWith("/decorators/") || path.startsWith("/WEB-INF/")) {
            chain.doFilter(request, response);
            return;
        }

        // Check if excluded or find decorator
        String matchedDecorator = null;
        for (Mapping m : mappings) {
            if (matches(m.pathPattern, path)) {
                if (m.isExclude) {
                    chain.doFilter(request, response);
                    return;
                }
                if (matchedDecorator == null && m.decorator != null && !m.decorator.isEmpty()) {
                    matchedDecorator = m.decorator;
                }
            }
        }

        if (matchedDecorator == null) {
            chain.doFilter(request, response);
            return;
        }

        // Wrap request to convert forward() into include() so Tomcat 11 doesn't commit prematurely
        HttpServletRequest wrappedReq = new HttpServletRequestWrapper(req) {
            @Override
            public RequestDispatcher getRequestDispatcher(String path) {
                final RequestDispatcher rd = super.getRequestDispatcher(path);
                if (rd == null) return null;
                return new RequestDispatcher() {
                    @Override
                    public void forward(ServletRequest request, ServletResponse response)
                            throws ServletException, IOException {
                        rd.include(request, response);
                    }

                    @Override
                    public void include(ServletRequest request, ServletResponse response)
                            throws ServletException, IOException {
                        rd.include(request, response);
                    }
                };
            }
        };

        // Wrap response to capture output safely
        CharResponseWrapper responseWrapper = new CharResponseWrapper(resp);

        try {
            chain.doFilter(wrappedReq, responseWrapper);
        } catch (Throwable t) {
            if (t instanceof ServletException) throw (ServletException) t;
            if (t instanceof IOException) throw (IOException) t;
            throw new ServletException(t);
        }

        // If response was redirected or is an error, pass through
        if (responseWrapper.isRedirect() || responseWrapper.isError()) {
            return;
        }

        // Get captured content
        String originalHtml = responseWrapper.getCapturedContent();
        if (originalHtml == null || originalHtml.trim().isEmpty()) {
            return;
        }

        // If not HTML content, output directly
        String contentType = responseWrapper.getContentType();
        if (contentType != null && !contentType.toLowerCase().contains("text/html")) {
            byte[] rawBytes = responseWrapper.getCapturedBytes();
            resp.setContentLength(rawBytes.length);
            resp.getOutputStream().write(rawBytes);
            resp.getOutputStream().flush();
            return;
        }

        // Extract title, head, and body from original HTML
        String title = "";
        Matcher titleMatcher = TITLE_PATTERN.matcher(originalHtml);
        if (titleMatcher.find()) {
            title = titleMatcher.group(1).trim();
        }

        String head = "";
        Matcher headMatcher = HEAD_PATTERN.matcher(originalHtml);
        if (headMatcher.find()) {
            head = headMatcher.group(1).trim();
            // Remove the title from head if present to prevent duplication
            head = TITLE_PATTERN.matcher(head).replaceAll("").trim();
        }

        String body = "";
        Matcher bodyMatcher = BODY_PATTERN.matcher(originalHtml);
        if (bodyMatcher.find()) {
            body = bodyMatcher.group(1).trim();
        } else {
            body = originalHtml;
        }

        // Render decorator using include() to be 100% compliant with Tomcat 11
        RequestDispatcher dispatcher = req.getRequestDispatcher(matchedDecorator);
        if (dispatcher == null) {
            // Decorator not found, write original content
            byte[] raw = originalHtml.getBytes(StandardCharsets.UTF_8);
            resp.setContentType("text/html;charset=UTF-8");
            resp.setContentLength(raw.length);
            resp.getOutputStream().write(raw);
            resp.getOutputStream().flush();
            return;
        }

        CharResponseWrapper decoratorWrapper = new CharResponseWrapper(resp);
        try {
            dispatcher.include(req, decoratorWrapper);
        } catch (Exception e) {
            System.err.println("[SiteMesh] Error rendering decorator " + matchedDecorator + ": " + e.getMessage());
            byte[] raw = originalHtml.getBytes(StandardCharsets.UTF_8);
            resp.setContentType("text/html;charset=UTF-8");
            resp.setContentLength(raw.length);
            resp.getOutputStream().write(raw);
            resp.getOutputStream().flush();
            return;
        }

        String decoratorHtml = decoratorWrapper.getCapturedContent();
        if (decoratorHtml == null || decoratorHtml.isEmpty()) {
            byte[] raw = originalHtml.getBytes(StandardCharsets.UTF_8);
            resp.setContentType("text/html;charset=UTF-8");
            resp.setContentLength(raw.length);
            resp.getOutputStream().write(raw);
            resp.getOutputStream().flush();
            return;
        }

        // Substitute SiteMesh tags in decorator
        String finalHtml = WRITE_TITLE_PATTERN.matcher(decoratorHtml).replaceAll(Matcher.quoteReplacement(title));
        finalHtml = WRITE_HEAD_PATTERN.matcher(finalHtml).replaceAll(Matcher.quoteReplacement(head));
        finalHtml = WRITE_BODY_PATTERN.matcher(finalHtml).replaceAll(Matcher.quoteReplacement(body));

        // Write decorated HTML to client
        byte[] finalBytes = finalHtml.getBytes(StandardCharsets.UTF_8);
        resp.setContentType("text/html;charset=UTF-8");
        resp.setContentLength(finalBytes.length);
        try {
            resp.getOutputStream().write(finalBytes);
            resp.getOutputStream().flush();
        } catch (IllegalStateException e) {
            resp.getWriter().write(finalHtml);
            resp.getWriter().flush();
        }
    }

    private boolean matches(String pattern, String path) {
        if (pattern == null || path == null) return false;
        if (pattern.equals("/*") || pattern.equals("*")) return true;
        if (pattern.endsWith("/*")) {
            String prefix = pattern.substring(0, pattern.length() - 2);
            return path.equals(prefix) || path.startsWith(prefix + "/");
        }
        if (pattern.endsWith("*")) {
            String prefix = pattern.substring(0, pattern.length() - 1);
            return path.startsWith(prefix);
        }
        return path.equals(pattern);
    }

    @Override
    public void destroy() {}

    /**
     * Response wrapper that captures both character and byte outputs in memory
     * without committing the underlying response. Shields Tomcat 11 from auto-finishing
     * the response after RequestDispatcher.forward().
     */
    public static class CharResponseWrapper extends HttpServletResponseWrapper {
        private final CharArrayWriter charWriter = new CharArrayWriter();
        private final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        private PrintWriter printWriter;
        private ServletOutputStream servletStream;
        private boolean usedWriter = false;
        private boolean usedStream = false;
        private int status = 200;
        private String redirectLocation = null;

        public CharResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setStatus(int sc) {
            this.status = sc;
            super.setStatus(sc);
        }

        @Override
        public int getStatus() {
            return status;
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            this.redirectLocation = location;
            this.status = 302;
            super.sendRedirect(location);
        }

        @Override
        public void sendError(int sc) throws IOException {
            this.status = sc;
            super.sendError(sc);
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            this.status = sc;
            super.sendError(sc, msg);
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (usedStream) throw new IllegalStateException("getOutputStream() has already been called");
            if (printWriter == null) {
                printWriter = new PrintWriter(charWriter);
            }
            usedWriter = true;
            return printWriter;
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            if (usedWriter) throw new IllegalStateException("getWriter() has already been called");
            if (servletStream == null) {
                servletStream = new ServletOutputStream() {
                    @Override
                    public boolean isReady() { return true; }
                    @Override
                    public void setWriteListener(WriteListener writeListener) {}
                    @Override
                    public void write(int b) { byteStream.write(b); }
                    @Override
                    public void write(byte[] b, int off, int len) { byteStream.write(b, off, len); }
                };
            }
            usedStream = true;
            return servletStream;
        }

        @Override
        public void flushBuffer() {
            // Swallow flushBuffer so Tomcat does not prematurely commit or close the response
        }

        @Override
        public boolean isCommitted() {
            return false;
        }

        public String getCapturedContent() {
            if (printWriter != null) {
                printWriter.flush();
            }
            if (usedWriter) {
                return charWriter.toString();
            } else if (usedStream) {
                String encoding = getCharacterEncoding();
                if (encoding == null || encoding.isEmpty()) encoding = "UTF-8";
                try {
                    return byteStream.toString(encoding);
                } catch (Exception e) {
                    return byteStream.toString();
                }
            }
            return "";
        }

        public byte[] getCapturedBytes() {
            if (usedStream) {
                return byteStream.toByteArray();
            } else if (usedWriter) {
                return charWriter.toString().getBytes(StandardCharsets.UTF_8);
            }
            return new byte[0];
        }

        public boolean isRedirect() {
            return redirectLocation != null || (status >= 300 && status < 400);
        }

        public boolean isError() {
            return status >= 400;
        }
    }
}
