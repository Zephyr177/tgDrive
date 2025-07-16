package com.skydevs.tgdrive.exception;

/**
 * Description:
 * 无配置文件
 * @author SkyDev
 * @date 2025-07-14 09:35:25
 */
public class NoConfigException extends BaseException{
    /**
     * Description:
     * 无配置文件
     * @author SkyDev
     * @date 2025-07-14 09:35:25
     */
    public NoConfigException() {
        super("当前没有配置文件");
    }

    public NoConfigException(String msg) {
        super(msg);
    }
}
