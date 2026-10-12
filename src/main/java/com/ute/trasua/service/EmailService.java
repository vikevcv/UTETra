package com.ute.trasua.service;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String mailFrom;

    @Value("${app.mail.from-name:UTETra Zen Gallery}")
    private String mailFromName;

    @Async
    public void sendOtpEmail(String to, String otp) {
        String subject = "Mã xác nhận đăng ký tài khoản UTETra";
        String htmlContent = buildOtpEmailTemplate(
                "Mã xác nhận đăng ký tài khoản",
                "Cảm ơn bạn đã lựa chọn trải nghiệm tại UTETra. Dưới đây là mã xác thực OTP để hoàn tất thủ tục tạo tài khoản của bạn:",
                otp
        );
        sendMimeEmail(to, subject, htmlContent);
    }

    @Async
    public void sendForgotPasswordOtpEmail(String to, String otp) {
        String subject = "[UTETra] Mã xác nhận đặt lại mật khẩu";
        String htmlContent = buildOtpEmailTemplate(
                "Mã xác nhận đặt lại mật khẩu",
                "UTETra nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. Dưới đây là mã xác thực OTP để thiết lập mật khẩu mới:",
                otp
        );
        sendMimeEmail(to, subject, htmlContent);
    }

    private void sendMimeEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(new InternetAddress(mailFrom, mailFromName, "UTF-8"));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Đã gửi email thành công đến [{}] với tên thương hiệu [{}]", to, mailFromName);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email đến [{}]: {}", to, e.getMessage(), e);
            throw new RuntimeException("Không thể gửi email xác thực. Vui lòng kiểm tra lại địa chỉ email hoặc thử lại sau.");
        }
    }

    private String buildOtpEmailTemplate(String title, String description, String otp) {
        return "<!DOCTYPE html>"
                + "<html lang=\"vi\">"
                + "<head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"></head>"
                + "<body style=\"margin: 0; padding: 0; background-color: #F7F5F0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;\">"
                + "  <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color: #F7F5F0; padding: 32px 16px;\">"
                + "    <tr>"
                + "      <td align=\"center\">"
                + "        <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"max-width: 540px; background-color: #FFFFFF; border-radius: 16px; overflow: hidden; border: 1px solid #E8E5DF; box-shadow: 0 4px 20px rgba(15, 16, 17, 0.06);\">"
                + "          <!-- Header / Brand Avatar -->"
                + "          <tr>"
                + "            <td align=\"center\" style=\"padding: 36px 24px 20px; background: linear-gradient(180deg, #FFFFFF 0%, #FAFAF8 100%); border-bottom: 1px solid #F0ECE4;\">"
                + "              <div style=\"display: inline-block; width: 68px; height: 68px; line-height: 68px; background-color: #5C7667; color: #FFFFFF; font-size: 30px; border-radius: 50%; box-shadow: 0 4px 14px rgba(92, 118, 103, 0.28); text-align: center;\">🍵</div>"
                + "              <h1 style=\"margin: 14px 0 4px; font-size: 24px; font-weight: 700; color: #0F1011; letter-spacing: 1px;\">UTETra</h1>"
                + "              <p style=\"margin: 0; font-size: 12px; letter-spacing: 2px; text-transform: uppercase; color: #8C6D53; font-weight: 600;\">Trà Đạo Đương Đại • Zen Gallery</p>"
                + "            </td>"
                + "          </tr>"
                + "          <!-- Body Content -->"
                + "          <tr>"
                + "            <td style=\"padding: 28px 32px;\">"
                + "              <h2 style=\"margin: 0 0 12px; font-size: 18px; font-weight: 700; color: #0F1011;\">" + title + "</h2>"
                + "              <p style=\"margin: 0 0 20px; font-size: 14px; line-height: 1.6; color: #4A4D52;\">"
                + "                Chào bạn,<br>" + description
                + "              </p>"
                + "              <!-- OTP Badge -->"
                + "              <div style=\"background-color: #F7F5F0; border: 2px dashed #8C6D53; border-radius: 12px; padding: 22px 16px; text-align: center; margin: 24px 0;\">"
                + "                <div style=\"font-size: 38px; font-weight: 800; letter-spacing: 10px; color: #0F1011; font-family: 'Courier New', Courier, monospace; padding-left: 10px;\">" + otp + "</div>"
                + "                <div style=\"margin-top: 10px; font-size: 13px; font-weight: 600; color: #5C7667;\">⏱ Mã này có hiệu lực trong vòng 5 phút</div>"
                + "              </div>"
                + "              <!-- Security Alert -->"
                + "              <div style=\"background-color: #FFF9F2; border-left: 4px solid #8C6D53; border-radius: 4px; padding: 12px 16px; margin: 20px 0;\">"
                + "                <p style=\"margin: 0; font-size: 12.5px; line-height: 1.5; color: #7A5B42;\">"
                + "                  <strong>Lưu ý an toàn:</strong> Tuyệt đối không chia sẻ mã này cho bất kỳ ai khác, kể cả nhân viên hỗ trợ của UTETra."
                + "                </p>"
                + "              </div>"
                + "              <p style=\"margin: 24px 0 0; font-size: 13.5px; line-height: 1.6; color: #4A4D52;\">"
                + "                Trân trọng,<br>"
                + "                <strong style=\"color: #0F1011;\">Đội ngũ UTETra Zen Gallery</strong>"
                + "              </p>"
                + "            </td>"
                + "          </tr>"
                + "          <!-- Footer -->"
                + "          <tr>"
                + "            <td align=\"center\" style=\"padding: 20px 24px; background-color: #FAFAF8; border-top: 1px solid #F0ECE4; font-size: 12px; color: #8C8F94; line-height: 1.6;\">"
                + "              <div>UTETra — Không gian Trà Đạo & Trà Sữa Đương Đại</div>"
                + "              <div style=\"margin-top: 4px; color: #A0A3A8;\">Nếu bạn không thực hiện yêu cầu này, xin vui lòng bỏ qua email.</div>"
                + "            </td>"
                + "          </tr>"
                + "        </table>"
                + "      </td>"
                + "    </tr>"
                + "  </table>"
                + "</body>"
                + "</html>";
    }
}

