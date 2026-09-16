package com.ute.trasua.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:thoain.n2006@gmail.com}")
    private String mailFrom;

    @Value("${app.mail.from-name:UTETra Zen Gallery}")
    private String mailFromName;

    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(to);
        message.setSubject("Mã xác nhận đăng ký tài khoản UTETra");
        message.setText("Chào bạn,\n\n" +
                "Mã xác nhận OTP của bạn là: " + otp + "\n\n" +
                "Mã này có hiệu lực trong 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.\n\n" +
                "Trân trọng,\nĐội ngũ UTETra Zen Gallery");
        
        mailSender.send(message);
    }

    public void sendForgotPasswordOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(to);
        message.setSubject("[UTETra] Mã xác nhận đặt lại mật khẩu");
        message.setText("Chào bạn,\n\n" +
                "UTETra nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.\n" +
                "Mã xác nhận OTP của bạn là: " + otp + "\n\n" +
                "Mã này có hiệu lực trong 5 phút. Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\n" +
                "Trân trọng,\nĐội ngũ UTETra Zen Gallery");

        mailSender.send(message);
    }
}

