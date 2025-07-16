package com.skydevs.tgdrive.exception;

/**
 * Description:
 * 业务基础异常
 * @author SkyDev
 * @date 2025-07-14 09:34:17
 */
public class BaseException extends RuntimeException {

    public BaseException() {
    }

    public BaseException(String msg) {
        super(msg);
    }

}
