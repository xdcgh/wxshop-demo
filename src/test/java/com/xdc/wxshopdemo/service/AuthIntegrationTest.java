package com.xdc.wxshopdemo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kevinsawicki.http.HttpRequest;
import com.xdc.wxshopdemo.WxshopDemoApplication;
import com.xdc.wxshopdemo.entity.LoginResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static com.xdc.wxshopdemo.service.TelVerificationServiceTest.VALID_PARAMETER;
import static com.xdc.wxshopdemo.service.TelVerificationServiceTest.VALID_PARAMETER_CODE;
import static java.net.HttpURLConnection.HTTP_OK;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WxshopDemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
public class AuthIntegrationTest {
    @Autowired
    Environment environment;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static class HttpResponse {
        int code;
        String body;
        Map<String, List<String>> headers;

        HttpResponse(int code, String body, Map<String, List<String>> headers) {
            this.code = code;
            this.body = body;
            this.headers = headers;
        }
    }

    private HttpResponse doHttpRequest(String apiName, boolean isGet, Object requestBody, String cookie) throws JsonProcessingException {
        HttpRequest request = isGet ? HttpRequest.get(getUrl(apiName)) : HttpRequest.post(getUrl(apiName));
        request.contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE);
        if (cookie != null) {
            request.header("Cookie", cookie);
        }
        if (requestBody != null) {
            // send 方法要放到最后
            request.send(objectMapper.writeValueAsString(requestBody));
        }

        return new HttpResponse(request.code(), request.body(), request.headers());
    }

    @Test
    public void loginLogoutTest() throws JsonProcessingException {
        // 最开始默认情况下，访问/api/status 处于未登录状态
        String statusResponse = doHttpRequest("/api/status", true, null, null).body;
        LoginResponse response = objectMapper.readValue(statusResponse, LoginResponse.class);
        Assertions.assertFalse(response.isLogin());

        // 发送验证码
        int responseCode = doHttpRequest("/api/code", false, VALID_PARAMETER, null).code;
        Assertions.assertEquals(HTTP_OK, responseCode);

        // 带着验证码进行登录，得到Cookie
        Map<String, List<String>> responseHeaders = doHttpRequest("/api/login", false, VALID_PARAMETER_CODE, null).headers;
        List<String> setCookie = responseHeaders.get("Set-Cookie");
        String sessionId = getSessionIdFromSetCookie(setCookie.stream()
                .filter(cookie -> cookie.contains("JSESSIONID"))
                .findFirst()
                .get());

        // 带着Cookie 访问 /api/status ，应该处于登录状态
        statusResponse = doHttpRequest("/api/status", true, null, sessionId).body;
        response = objectMapper.readValue(statusResponse, LoginResponse.class);
        Assertions.assertTrue(response.isLogin());
        Assertions.assertEquals(VALID_PARAMETER.getTel(), response.getUser().getTel());

        // 调用 /api/logout
        // 注销登录,注意注销登录也要带Cookie ，不然服务器不知道谁来注销
        doHttpRequest("/api/logout", false, null, sessionId);

        // 再次带着Cookie 访问 /api/status，恢复成未登录状态
        statusResponse = doHttpRequest("/api/status", true, null, sessionId).body;
        response = objectMapper.readValue(statusResponse, LoginResponse.class);
        Assertions.assertFalse(response.isLogin());
    }

    private String getSessionIdFromSetCookie(String setCookie) {
        // JSESSIONID=10554593-edc9-4254-8c35-b27130c08a35; Path=/; HttpOnly; SameSite=lax
        // -> JSESSIONID=10554593-edc9-4254-8c35-b27130c08a35
        int semiColonIndex = setCookie.indexOf(";");

        return setCookie.substring(0, semiColonIndex);
    }

    @Test
    public void returnHttpOKWhenParameterIsCorrect() throws JsonProcessingException {
        int responseCode = HttpRequest.post(getUrl("/api/code"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .send(objectMapper.writeValueAsString(VALID_PARAMETER))
                .code();

        Assertions.assertEquals(HttpServletResponse.SC_OK, responseCode);
    }

    @Test
    public void returnHttpBadRequestWhenParameterIsWarm() throws JsonProcessingException {
        int responseCode = HttpRequest.post(getUrl("/api/code"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .send(objectMapper.writeValueAsString(TelVerificationServiceTest.EMPTY_TEL))
                .code();

        Assertions.assertEquals(HttpServletResponse.SC_BAD_REQUEST, responseCode);
    }

    private String getUrl(String apiName) {
        // 获取集成测试的端口号
        return "http://localhost:" + environment.getProperty("local.server.port") + apiName;
    }

}
