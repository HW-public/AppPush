package top.wprop.app.push.service.impl;

import com.alibaba.fastjson.JSONObject;
import top.wprop.app.push.model.dto.MessageDTO;
import top.wprop.app.push.service.PushService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ApplePushServiceImpl implements PushService {
    @Value("${push.apple.keyId}")
    private String APNS_KEY_ID = "";
    @Value("${push.apple.teamId}")
    private String TEAM_ID = "";
    @Value("${push.apple.keyFilePath}")
    private String KEY_FILE_PATH = "";
    @Value("${push.apple.appPackageName}")
    private String APNS_TOPIC = "";
    @Value("${push.apple.pushUrl}")
    private String PUSH_URL = "";
    private final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json;charset=utf-8");
    private final ConcurrentHashMap<String, String> jwtCache = new ConcurrentHashMap<>();

    @Override
    public boolean pushMessage(MessageDTO messageDTO) throws IOException {
        log.debug("Apple Push message: {}", messageDTO);

        OkHttpClient client = new OkHttpClient();
        JSONObject root  = new JSONObject();        // 创建最外层的 JSON 对象
        JSONObject aps   = new JSONObject();        // 创建 aps 对象
        JSONObject alert = new JSONObject();        // 创建 alert 对象

        root.put("aps", aps);
        aps.put("alert", alert);

        // 设置 alert 的内容
        alert.put("title", messageDTO.getTitle());
        alert.put("body", messageDTO.getBody());

        // 将 Map 转换为 JSONObject
        JSONObject data = new JSONObject(messageDTO.getData());
        alert.put("data", data);

        // 打印生成的 JSON 字符串
        String jsonString = root.toJSONString();
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, jsonString);

        // 获取 JWT 令牌
        String jwt = getValidJwtToken();
        Request request = new Request.Builder().url(PUSH_URL + messageDTO.getDeviceToken())
                .header("apns-topic",            APNS_TOPIC) // 通知的主题。通常，主题是您应用的 bundle ID/app ID
                .header("apns-push-type",  "alert")    // 设置推送类型为通知
                .header("apns-priority",   "10")       // 设置优先级为高
                .header("apns-expiration", "0")        // 如果值为 0，则 APNs 仅尝试发送一次通知，并且不会存储通知。
                .header("authorization", jwt)                // 此标头的值为 bearer <provider_token>，其中
                // <provider_token> 是加密令牌，
                .post(body).build();

        Response response = client.newCall(request).execute();
        if (response.isSuccessful() && response.body() != null) {
            log.info("Apple Push response: {}", response.headers());
            return true;
        } else {
            log.error("Apple Push failed: code = {}, body = {}",  response.code(), response.body().string());
            return false;
        }
    }

    private String getValidJwtToken() throws IOException {
        String jwt = jwtCache.get("apnsToken");
        String expirationStr = jwtCache.get("apnsTokenExpiration");
        if (jwt != null && expirationStr != null) {
            long expirationTime = Long.parseLong(expirationStr);
            if (expirationTime > System.currentTimeMillis()) {
                return jwt;
            }else {
                log.info("Apple Push token is expired, generate new one.");
            }
        }else {
            log.info("Apple Push token is null, generate new one.");
        }
        // 生成新的 JWT 令牌
        return generateAPNsToken();
    }

    private String generateAPNsToken() throws IOException {
        // 读取私钥文件内容
        String keyContent = new String(Files.readAllBytes(Paths.get(KEY_FILE_PATH)), StandardCharsets.UTF_8);
        // 获取私钥
        PrivateKey privateKey = getPrivateKey(keyContent);
        // 设置 JWT 的有效载荷
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date exp = new Date(nowMillis + 40 * 60 * 1000); // 令牌有效期为 40 分钟
        // 构建 JWT
        String jwt = Jwts.builder()
                .setHeaderParam("alg", "ES256") // 算法
                .setHeaderParam("kid", APNS_KEY_ID) // Key ID
                .setIssuer(TEAM_ID) // Team ID
                .setIssuedAt(now) // 签发时间
                //.setExpiration(exp) // 过期时间
                .signWith(SignatureAlgorithm.ES256, privateKey) // 使用私钥签名
                .compact();
        jwt = "bearer " + jwt;
        // 将 JWT 和过期时间存储到缓存中
        jwtCache.put("apnsToken", jwt);
        jwtCache.put("apnsTokenExpiration", exp.getTime() + "");
        return jwt;
    }

    private PrivateKey getPrivateKey(String keyContent) throws IOException {
        // 清理 PEM 格式的私钥内容
        String s1 = keyContent.replace("-----BEGIN PRIVATE KEY-----", "");
        String s2 = s1.replace("-----END PRIVATE KEY-----", "");
        String privateKeyPEM = s2.replaceAll("\\s", "");

        // Base64 解码
        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
        // 创建 PKCS8EncodedKeySpec
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        try {
            // 获取 KeyFactory 实例
            KeyFactory kf = KeyFactory.getInstance("EC");
            // 生成私钥
            return kf.generatePrivate(keySpec);
        } catch (Exception e) {
            log.error("Failed to parse private key", e);
            throw new IOException("Failed to parse private key", e);
        }
    }
}

