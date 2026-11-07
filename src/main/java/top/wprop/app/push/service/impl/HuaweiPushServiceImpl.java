package top.wprop.app.push.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import top.wprop.app.push.model.dto.MessageDTO;
import top.wprop.app.push.service.PushService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class HuaweiPushServiceImpl implements PushService {
    private static final String SUCCESS = "80000000";
    @Value("${push.huawei.authUrl}")
    private String TOKEN_URL;
    @Value("${push.huawei.pushUrl}")
    private String PUSH_URL_TEMPLATE;
    @Value("${push.huawei.clientId}")
    private String CLIENT_ID;
    @Value("${push.huawei.clientSecret}")
    private String CLIENT_SECRET;
    private final OkHttpClient client = new OkHttpClient();
    private final ConcurrentHashMap<String, String> tokenCache = new ConcurrentHashMap<>();
    @Override
    public boolean pushMessage(MessageDTO messageDTO) throws IOException {
        log.debug("Huawei Push message: {}", messageDTO);

        String accessToken = getValidJwtToken();
        if (accessToken == null) {
            log.error("Failed to get Access Token from Huawei");
            return false;
        }

        String pushUrl = String.format(PUSH_URL_TEMPLATE, CLIENT_ID);
        String json = buildMessageJson(messageDTO);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), json);
        Request request = new Request.Builder()
                .url(pushUrl)
                .post(body)
                .header("Authorization", accessToken)
                .build();

        Response response = client.newCall(request).execute();
        if (response.isSuccessful() && response.body() != null) {
            String responseBody = response.body().string();
            log.info("Huawei Push response: {}", responseBody);
            String code = JSON.parseObject(responseBody).getString("code");
            return SUCCESS.equals(code);
        } else {
            log.error("Huawei Push failed: code = {}, body = {}",  response.code(), response.body().string());
            return false;
        }
    }

    private String getAccessToken() throws IOException {
        RequestBody body = new FormBody.Builder()
                .add("grant_type",   "client_credentials")
                .add("client_id",     CLIENT_ID)
                .add("client_secret", CLIENT_SECRET)
                .build();

        Request request = new Request.Builder()
                .url(TOKEN_URL)
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        if (response.isSuccessful() && response.body() != null) {
            String responseBody = response.body().string();
            JSONObject jsonResponse = JSON.parseObject(responseBody);
            long nowMillis = System.currentTimeMillis();
            Date expiresIn = new Date(nowMillis + 40 * 60 * 1000); // 令牌有效期为 40 分钟
            Object tokenType = jsonResponse.get("token_type");
            String token = jsonResponse.getString("access_token");
            // 将 token 和过期时间存储到缓存中
            tokenCache.put("access_token", tokenType + " " + token);
            tokenCache.put("expires_in", expiresIn.getTime() + "");
            return token;
        } else {
            log.error("Huawei : Failed to get Access Token");
        }

        return null;
    }

    private String buildMessageJson(MessageDTO messageDTO) {
        JSONObject clickAction = new JSONObject();
        clickAction.put("type", 3); // 消息点击行为类型，取值如下：1：打开应用自定义页面; 2：点击后打开特定URL; 3：点击后打开应用

        JSONArray token = new JSONArray();
        token.add(messageDTO.getDeviceToken());

        JSONObject notification = new JSONObject();
        notification.put("title",        messageDTO.getTitle());      // 通知栏消息的标题。
        notification.put("body",         messageDTO.getBody());       // 通知栏消息的内容。
        notification.put("click_action", clickAction);

        JSONObject android = new JSONObject();
        android.put("notification",  notification);
        if (messageDTO.getCategory().equals("im")) {
            android.put("category", "IM");
        } else if (messageDTO.getCategory().equals("voip")) {
            android.put("category", "VOIP");
        }else {
            log.error("Invalid category: {}", messageDTO.getCategory());
        }

        JSONObject message = new JSONObject();
        Map<String, Object> data = messageDTO.getData();
        if (!data.isEmpty()) {
            message.put("data", messageDTO.getData().toString());
        }else {
            log.error("Huawei Push message data is empty.");
        }
        message.put("android", android);
        message.put("token",   token);

        JSONObject messageJson = new JSONObject();
        messageJson.put("validate_only", false);  // true：测试消息; false：正式消息（默认值）测试消息只做格式合法性校验，不会推送给用户设备
        messageJson.put("message",       message);

        return messageJson.toJSONString();
    }

    private String getValidJwtToken() throws IOException {
        String accessToken = tokenCache.get("access_token");
        String expiresIn   = tokenCache.get("expires_in");
        if ((accessToken != null) && (expiresIn != null)) {
            long expirationTime = Long.parseLong(expiresIn);
            if (expirationTime > System.currentTimeMillis()) {
                return accessToken;
            }else {
                log.info("Huawei Push token is expired, generate new one.");
            }
        }else {
            log.info("Huawei Push token is null, generate new one.");
        }
        return getAccessToken(); // 生成新的 token 令牌
    }
}
