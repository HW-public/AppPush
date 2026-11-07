package top.wprop.app.push.factory;


import top.wprop.app.push.constant.HttpCode;
import top.wprop.app.push.exception.CustomException;
import top.wprop.app.push.service.PushService;
import top.wprop.app.push.service.impl.ApplePushServiceImpl;
import top.wprop.app.push.service.impl.HuaweiPushServiceImpl;
import top.wprop.app.push.service.impl.OppoPushServiceImpl;
import top.wprop.app.push.service.impl.XiaomiPushServiceImpl;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PushServiceFactory {
    @Resource
    private XiaomiPushServiceImpl xiaomiPushService;
    @Resource
    private ApplePushServiceImpl applePushService;
    @Resource
    private HuaweiPushServiceImpl huaweiPushService;
    @Resource
    private OppoPushServiceImpl oppoPushService;
    private final Map<String, PushService> serviceMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        serviceMap.put("xiaomi", xiaomiPushService);
        serviceMap.put("apple", applePushService);
        serviceMap.put("huawei", huaweiPushService);
        serviceMap.put("oppo", oppoPushService);
    }

    public PushService getPushService(String manufacturer) {
        PushService pushService = serviceMap.get(manufacturer.toLowerCase());
        if (pushService == null) {
            throw new CustomException("该厂商暂未开发", HttpCode.InvalidParameter);
        }
        return pushService;
    }
}
