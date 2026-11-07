package top.wprop.app.push.controller;

import top.wprop.app.push.common.AjaxResult;
import top.wprop.app.push.factory.PushServiceFactory;
import top.wprop.app.push.model.dto.MessageDTO;
import top.wprop.app.push.service.PushService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@Api(tags = "消息推送")
@Slf4j
@RestController
public class PushController {
    @Resource
    private PushServiceFactory pushServiceFactory;

    /**
     * 推送消息
     *
     * @param messageDTO 消息体
     * @return 推送结果
     */
    @PostMapping("/push")
    public AjaxResult pushMessage(@RequestBody @Valid MessageDTO messageDTO) {
        try {
            PushService pushService = pushServiceFactory.getPushService(messageDTO.getManufacturer());
            return pushService.pushMessage(messageDTO) ? AjaxResult.success() : AjaxResult.error();
        } catch (Exception e) {
            log.error("Failed to push message", e);
            return AjaxResult.error(e.getMessage());
        }
    }
}
