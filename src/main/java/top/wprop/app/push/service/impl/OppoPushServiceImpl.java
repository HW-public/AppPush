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

@Slf4j
@Service
public class OppoPushServiceImpl implements PushService {
    @Value("oppo.pushUrl")
    private static final String PUSH_URL = "";
    @Value("oppo.authUrl")
    private static final String AUTH_URL = "";
    @Value("${oppo.appKey}")
    private static final String APP_KEY = "";
    @Value("oppo.masterSecret")
    private String MASTER_SECRET = "";

    private final OkHttpClient httpClient = new OkHttpClient();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean pushMessage(MessageDTO messageDTO) throws IOException {
        // 获取Auth Token
        String authToken = getAuthToken();
        if (authToken == null) throw new CustomException("OPPO failed get token");
        // 构造请求体
        String requestBody = createRequestBody(messageDTO.getDeviceToken(), messageDTO.getTitle()
                , messageDTO.getBody());

        // 创建POST请求
        Request request = new Request.Builder()
                .url(PUSH_URL)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=utf-8"),
                        requestBody))
                .addHeader("auth_token", authToken) // 添加鉴权令牌
                .build();

        // 发送请求
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
     * 获取Auth Token
     *
     * @return 获取到的Auth Token，如果失败返回null
     */
    private String getAuthToken() throws IOException {
        long timestamp = System.currentTimeMillis();
        String sign = generateSign(APP_KEY, timestamp, MASTER_SECRET);

        // 构造请求体
        String requestBody = "app_key=" + APP_KEY + "&sign=" + sign + "&timestamp=" + timestamp;

        // 创建POST请求
        Request request = new Request.Builder()
                .url(AUTH_URL)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded;charset=utf-8"), requestBody))
                .build();

        Response response = httpClient.newCall(request).execute();
        if (response.isSuccessful() && response.body() != null) {
            String responseBody = response.body().string();
            // 解析返回的JSON数据
            JSONObject jsonResponse = JSON.parseObject(responseBody);
            if (jsonResponse.getIntValue("code") == 0) {
                JSONObject data = jsonResponse.getJSONObject("data");
                return data.getString("auth_token");
            }
        }
        return null;
    }

    /**
     * 生成签名
     *
     * @param appKey       应用的AppKey
     * @param timestamp    时间戳
     * @param masterSecret MasterSecret
     * @return SHA256签名
     */
    private String generateSign(String appKey, long timestamp, String masterSecret) {
        String rawString = appKey + timestamp + masterSecret;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawString.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
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

    /**
     * 构造请求体
     *
     * @param deviceToken 设备的Registration ID
     * @param title       设置在通知栏展示的通知栏标题
     * @param content     设置在通知栏展示的通知的正文内容
     * @return JSON格式的请求体
     */
    private String createRequestBody(String deviceToken, String title, String content) throws JsonProcessingException {
        // 构造请求体
        Map<String, Object> message = new HashMap<>();
        message.put("target_type", 2); // 2 表示 registration_id
        message.put("target_value", deviceToken);

        Map<String, Object> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("content", content);
        notification.put("show_start_time", 0);
        notification.put("show_end_time", 0);
        /**
         *["IM","ACCOUNT","DEVICE_REMINDER","ORDER","TODO","SUBSCRIPTION"]
         **/
        notification.put("category", "IM");
        notification.put("notify_level", 16); // 1-通知栏,2-通知栏+锁屏,16-通知栏+锁屏+横幅+震动+铃声
        message.put("notification", notification);
        return objectMapper.writeValueAsString(message);
    }
}