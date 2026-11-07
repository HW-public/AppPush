package top.wprop.app.push.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import top.wprop.app.push.exception.CustomException;
import top.wprop.app.push.model.dto.MessageDTO;
import top.wprop.app.push.service.PushService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class VivoPushServiceImpl implements PushService {


    @Value("${push.vivo.authUrl}")
    private String AUTH_URL = "";
    @Value("${push.vivo.pushUrl}")
    private String UNICAST_URL = "";
    @Value("${push.vivo.appId}")
    private String APP_ID = "";
    @Value("${push.vivo.appKey}")
    private String APP_KEY = "";
    @Value("${push.vivo.appSecret}")
    private String APP_SECRET = "";

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean pushMessage(MessageDTO messageDTO) throws IOException {
        log.info("Pushing message to VIVO device with token: {}", messageDTO.getDeviceToken());

        // 获取AuthToken
        String authToken = getAuthToken();
        if (authToken == null) throw new CustomException("VIVO failed get token");
        String requestBody = createRequestBody(messageDTO.getDeviceToken(), messageDTO.getTitle()
                , messageDTO.getBody());
        // 发送单点推送消息
        Request request = new Request.Builder()
                .url(UNICAST_URL)
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                        requestBody))
                .addHeader("authToken", authToken)
                .build();

        Response response = httpClient.newCall(request).execute();
        if (response.isSuccessful() && response.body() != null) {
            String responseBody = response.body().string();
            // 解析返回的JSON数据
            JSONObject jsonResponse = JSON.parseObject(responseBody);
            return jsonResponse.getIntValue("code") == 0;
        }
        return false;
    }

    /**
     * 获取AuthToken
     *
     * @return 获取到的AuthToken，如果失败返回null
     */
    private String getAuthToken() {
        long timestamp = System.currentTimeMillis();
        String sign = generateSign(timestamp);

        Map<String, Object> authRequest = new HashMap<>();
        authRequest.put("appId", APP_ID);
        authRequest.put("appKey", APP_KEY);
        authRequest.put("timestamp", timestamp);
        authRequest.put("sign", sign);

        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(authRequest);
        } catch (IOException e) {
            log.error("Error creating auth request body", e);
            return null;
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                requestBody);
        Request request = new Request.Builder()
                .url(AUTH_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                Map<String, Object> responseMap = objectMapper.readValue(response.body().string()
                        , Map.class);
                if ((int) responseMap.get("result") == 0) {
                    return (String) responseMap.get("authToken");
                } else {
                    log.error("Failed to get auth token. Error: {}", responseMap.get("desc"));
                }
            } else {
                log.error("Failed to get auth token. Response code: {}", response.code());
            }
        } catch (IOException e) {
            log.error("Error sending auth request", e);
        }
        return null;
    }

    /**
     * 生成签名
     *
     * @param timestamp 时间戳
     * @return 签名
     */
    private String generateSign(long timestamp) {
        String rawString = APP_ID + APP_KEY + timestamp + APP_SECRET;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(rawString.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }


    private String createRequestBody(String deviceToken, String title, String content) throws JsonProcessingException {
        Map<String, Object> message = new HashMap<>();
        message.put("appId", APP_ID);
        message.put("regId", deviceToken);
        message.put("notifyType", 4); //  1:无，2:响铃，3:振动，4:响铃和振动
        message.put("title", title);
        message.put("content", content);
        message.put("skipType", 4); // 点击跳转类型 1：打开APP首页 2：打开链接 3：自定义 4:打开app内指定页面
        message.put("requestId", UUID.randomUUID().toString()); // 请求唯一标识
        return objectMapper.writeValueAsString(message);
    }
}