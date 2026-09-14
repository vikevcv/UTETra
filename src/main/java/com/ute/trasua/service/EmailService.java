package com.ute.trasua.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Mã xác nhận đăng ký tài khoản UTETra");
        message.setText("Chào bạn,\n\n" +
                "Mã xác nhận OTP của bạn là: " + otp + "\n\n" +
                "Mã này có hiệu lực trong 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.\n\n" +
                "Trân trọng,\nĐội ngũ UTETra");
        
        mailSender.send(message);
    }
}
