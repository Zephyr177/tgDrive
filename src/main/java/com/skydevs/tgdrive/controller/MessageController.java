package com.skydevs.tgdrive.controller;

import com.skydevs.tgdrive.dto.Message;
import com.skydevs.tgdrive.result.Result;
import com.skydevs.tgdrive.service.BotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api")
@RequiredArgsConstructor
public class MessageController {

    private final BotService botService;

    /**
     * 发送消息
     *
     * @param message
     * @return
     */
    @PostMapping("/send-message")
    public Result<String> sendMessage(@RequestBody Message message) {
        log.info("处理消息发送");
        if (botService.sendMessage(message.getMessage())) {
            return Result.success("消息发送成功: " + message);
        } else {
            return Result.error("消息发送失败");
        }
    }

}
