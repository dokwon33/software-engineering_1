package UOSense.UOSense_Backend.service;

import UOSense.UOSense_Backend.common.Utils.EmailUtil;
import UOSense.UOSense_Backend.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService{
    private final UserRepository userRepository;
    private final EmailUtil emailUtil;

    @Override
    public boolean checkMailAddress(String mailAddress) {
        if(!mailAddress.endsWith("@uos.ac.kr"))
            throw new IllegalArgumentException("올바른 웹메일 형식이 아닙니다.");
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkDuplicatedMail(String mailAddress) {
        if (userRepository.existsByEmail(mailAddress)) {
            throw new IllegalArgumentException("이미 가입된 메일 주소입니다.");
        }
        return true;
    }

    @Override
    public void sendAuthMail(String email, String purpose, String authCode) {
        try {
            if (!checkMailAddress(email)) return;
            String body = "<html><body style=\"text-align: center;\"><h2> 인증번호 [ "+ authCode +" ]</h2>" +
                    "<b>시대생 맛집 지도 "+ purpose +" 화면에서 인증번호를 입력해 주세요.</b></body></html>";
            MimeMessage newMail = emailUtil.createMail(email,purpose +" 인증 번호", body);
            emailUtil.sendMail(newMail);
        } catch (MessagingException e) {
            throw new IllegalArgumentException("메세지 전송 과정에 문제가 생겼습니다.");
        }
    }
}
