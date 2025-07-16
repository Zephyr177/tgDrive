package com.skydevs.tgdrive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Description:
 * 视图控制
 * @author SkyDev
 * @date 2025-07-11 17:54:19
 */
@Controller
public class ViewController {
    /**
     * Description:
     * 重定向
     * @return String
     * @author SkyDev
     * @date 2025-07-11 17:54:35
     */
    @RequestMapping("/{path:[^\\.]*}")
    public String forward() {
        return "forward:/index.html";
    }
}
