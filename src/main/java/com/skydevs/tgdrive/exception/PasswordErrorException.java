package com.skydevs.tgdrive.exception;

/**
 * Description:
 * 密码错误
 * @author SkyDev
 * @date 2025-07-14 09:36:04
 */
public class PasswordErrorException extends BaseException{
    /**
     * Description:
     * 密码错误
     * @author SkyDev
     * @date 2025-07-14 09:36:04
     */
    public PasswordErrorException(){
        super("密码错误");
    }

    public PasswordErrorException(String msg) {
        super(msg);
    }
}
