package top.wprop.app.push.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import top.wprop.app.push.model.dto.MessageDTO;
import top.wprop.app.push.service.PushService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class XiaomiPushServiceImpl implements PushService {
    @Value("${push.xiaomi.pushUrl}")
    private String PUSH_URL;
    @Value("${push.xiaomi.appSecret}")
    private String APP_SECRET;
    @Value("${push.xiaomi.appPackageName}")
    private String APP_package_name;
    @Value("${push.xiaomi.channelIdIm}")
    private String CHANNEL_ID_IM;
    @Value("${push.xiaomi.channelIdVoip}")
    private String CHANNEL_ID_VOIP;
    private final OkHttpClient httpClient = new OkHttpClient();

    public boolean pushMessage(MessageDTO messageDTO) throws IOException {
        log.debug("Xiaomi Push message: {}", messageDTO);

        RequestBody requestBody = getRequestBody(messageDTO);
        Request request = new Request.Builder()
                .url(PUSH_URL)
                .post(requestBody)
                .addHeader("Authorization", "key=" + APP_SECRET)
                .build();

        Response response = httpClient.newCall(request).execute();
        if (response.isSuccessful() && response.body() != null) {
            String responseBody = response.body().string();
            // 解析返回的JSON数据
            JSONObject jsonResponse = JSON.parseObject(responseBody);
            if (jsonResponse.getIntValue("code") == 0) {
                log.info("Push xiaomi result message:{}", responseBody);
                return true;
            } else {
                log.error("Push xiaomi result message:{}", responseBody);
                return false;
            }
        } else {
            log.error("Xiaomi Push failed:{}", response.body().string());
            return false;
        }

    }

    private RequestBody getRequestBody(MessageDTO messageDTO) throws UnsupportedEncodingException {
        String encodedPayload = URLEncoder.encode(messageDTO.getData().toString(), StandardCharsets.UTF_8.toString());
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("registration_id", messageDTO.getDeviceToken());
        formBuilder.add("description", messageDTO.getBody());
        formBuilder.add("payload", encodedPayload);
        formBuilder.add("restricted_package_name", APP_package_name);
        formBuilder.add("title", messageDTO.getTitle());
        formBuilder.add("extra.notify_effect", "1");

        if (messageDTO.getCategory().equalsIgnoreCase("im")) {
            formBuilder.add("extra.channel_id", CHANNEL_ID_IM);
        } else if (messageDTO.getCategory().equalsIgnoreCase("voip")) {
            formBuilder.add("extra.channel_id", CHANNEL_ID_VOIP);
        } else {
            log.error("Invalid category: {}", messageDTO.getCategory());
        }
        return formBuilder.build();
    }
}
