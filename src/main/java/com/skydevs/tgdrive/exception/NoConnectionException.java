package com.skydevs.tgdrive.exception;

/**
 * Description:
 * 网络连接异常
 * @author SkyDev
 * @date 2025-07-14 09:35:43
 */
public class NoConnectionException extends BaseException{
    /**
     * Description:
     * 网络连接异常
     * @author SkyDev
     * @date 2025-07-14 09:35:43
     */
    public NoConnectionException() {
        super("请检查网络连接");
    }
    public NoConnectionException(String msg) {
        super(msg);
    }
}
