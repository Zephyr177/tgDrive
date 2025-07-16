package com.skydevs.tgdrive.exception;

/**
 * Description:
 * 用户未找到
 * @author SkyDev
 * @date 2025-07-14 09:39:50
 */
public class UserNotFoundException extends BaseException {
    /**
     * Description:
     * 用户未找到
     * @author SkyDev
     * @date 2025-07-14 09:39:50
     */
    public UserNotFoundException() {
        super("用户不存在");
    }

    public UserNotFoundException(String msg) {
        super(msg);
    }
}
