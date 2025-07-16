package com.skydevs.tgdrive.exception;

/**
 * Description:
 * 获取bot token失败
 * @author SkyDev
 * @date 2025-07-14 09:35:02
 */
public class GetBotTokenFailedException extends BaseException{
    /**
     * Description:
     * 获取bot token失败
     * @author SkyDev
     * @date 2025-07-14 09:35:02
     */
    public GetBotTokenFailedException() {
        super("获取bot token失败");
    }

    public GetBotTokenFailedException(String msg) {
        super(msg);
    }
}
