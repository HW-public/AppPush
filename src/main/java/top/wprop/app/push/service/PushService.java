package top.wprop.app.push.service;

import top.wprop.app.push.model.dto.MessageDTO;

import java.io.IOException;

public interface PushService {
    boolean pushMessage(MessageDTO messageDTO) throws IOException;
}