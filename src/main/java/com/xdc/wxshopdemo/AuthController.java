package com.xdc.wxshopdemo;

import com.xdc.wxshopdemo.entity.LoginResponse;
import com.xdc.wxshopdemo.service.AuthService;
import com.xdc.wxshopdemo.service.TelVerificationService;
import com.xdc.wxshopdemo.service.UserContext;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService authService;
    private final TelVerificationService telVerificationService;

    public AuthController(AuthService authService,
                          TelVerificationService telVerificationService) {
        this.authService = authService;
        this.telVerificationService = telVerificationService;
    }

    @PostMapping("/code")
    public void code(@RequestBody TelAndCode telAndCode,
                     HttpServletResponse response) {
        if (telVerificationService.verifyTelParameter(telAndCode)) {
            authService.sendVerificationCode(telAndCode.getTel());
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public void login(@RequestBody TelAndCode telAndCode) {
        UsernamePasswordToken token = new UsernamePasswordToken(
                telAndCode.getTel(),
                telAndCode.getCode());

        token.setRememberMe(true);

        SecurityUtils.getSubject().login(token);
    }

    @PostMapping("/logout")
    public void logout() {
        SecurityUtils.getSubject().logout();
    }

    @GetMapping("/status")
    public Object loginStatus() {
        if (UserContext.getCurrentUser() == null) {
            return LoginResponse.notLogin();
        } else {
            return LoginResponse.login(UserContext.getCurrentUser());
        }
    }

    public static class TelAndCode {
        private String tel;
        private String code;

        public TelAndCode(String tel, String code) {
            this.tel = tel;
            this.code = code;
        }

        public String getTel() {
            return tel;
        }

        public void setTel(String tel) {
            this.tel = tel;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }
}
