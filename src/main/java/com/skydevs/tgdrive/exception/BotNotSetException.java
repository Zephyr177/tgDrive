package com.skydevs.tgdrive.exception;

/**
 * Description:
 * bot token未设置
 * @author SkyDev
 * @date 2025-07-14 09:33:38
 */
public class BotNotSetException extends BaseException{
    /**
     * Description:
     * bot token 未设置
     * @author SkyDev
     * @date 2025-07-14 09:37:50
     */
    public BotNotSetException() {
        super("bot token未设置");
    }

    public BotNotSetException(String msg) {
        super(msg);
    }
}
