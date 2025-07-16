package com.skydevs.tgdrive.exception;

/**
 * Description:
 * 配置文件不存在
 * @author SkyDev
 * @date 2025-07-14 09:33:54
 */
public class ConfigFileNotFoundException extends BaseException{
    /**
     * Description:
     * 配置文件不存在
     * @author SkyDev
     * @date 2025-07-14 09:33:54
     */
    public ConfigFileNotFoundException() {
        super("配置文件不存在");
    }

    public ConfigFileNotFoundException(String msg) {
        super(msg);
    }
}
