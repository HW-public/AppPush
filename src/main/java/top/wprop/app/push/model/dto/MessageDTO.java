package top.wprop.app.push.model.dto;

import top.wprop.app.push.annotation.ValidateValues;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
public class MessageDTO {
    @NotNull(message = "manufacturer 不能为空")
    @ValidateValues(allowedStringValues = {"xiaomi", "vivo", "oppo", "apple","huawei"},
                    message = "manufacturer 值只能为 xiaomi,vivo,oppo,apple,huawei")
    private String manufacturer;

    @NotNull(message = "deviceToken 不能为空")
    private String deviceToken;

    @ValidateValues(allowedStringValues = {"im", "voip"}, message = "category 值只能为 im(文本),voip(视频)")
    @NotNull(message = "category 不能为空")
    private String category;

    @NotNull(message = "title 不能为空")
    private String title;

    @NotNull(message = "body 不能为空")
    private String body;

    private Map<String, Object> data;
}