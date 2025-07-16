package com.skydevs.tgdrive.exception;

/**
 * Description:
 * 获取文件大小失败
 * @author SkyDev
 * @date 2025-07-14 09:34:37
 */
public class FailedToGetSizeException extends BaseException {
    /**
     * Description:
     * 获取文件大小失败
     * @author SkyDev
     * @date 2025-07-14 09:34:37
     */
    public FailedToGetSizeException() {
        super("无法获取文件大小");
    }

    public FailedToGetSizeException(String msg) {
        super(msg);
    }
}
