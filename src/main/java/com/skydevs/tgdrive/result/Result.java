package com.skydevs.tgdrive.result;

import lombok.Data;

import java.io.Serializable;

/**
 * Description:
 * 后端统一返回结果
 * @author SkyDev
 * @date 2025-07-14 10:00:46
 */
@Data
public class Result<T> implements Serializable {

    private Integer code; //编码：1成功，0和其它数字为失败
    private String msg; //错误信息
    private T data; //数据

    /**
     * Description:
     * 只返回成功
     * @author SkyDev
     * @date 2025-07-14 09:59:37
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = 1;
        return result;
    }

    /**
     * Description:
     * 返回成功以及信息
     * @author SkyDev
     * @date 2025-07-14 09:59:45
     */
    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<>();
        result.data = object;
        result.code = 1;
        return result;
    }

    /**
     * Description:
     * 返回错误以及错误信息
     * @author SkyDev
     * @date 2025-07-14 10:00:18
     */
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.msg = msg;
        result.code = 0;
        return result;
    }

}
