package com.skydevs.tgdrive.service;

import com.skydevs.tgdrive.dto.ConfigForm;

import java.util.List;

public interface ConfigService {

    /**
     * Description: 根据文件名查询配置
     *
     * @param filename 文件名
     * @return ConfigForm
     * @author nanyang
     * @date 2025-07-11 16:21:26
     */
    ConfigForm get(String filename);

    /**
     * Description:
     * 插入配置
     * @param configForm 配置表单
     * @author nanyang
     * @date 2025-07-11 16:27:56
     */
    void save(ConfigForm configForm);

    /**
     * Description:
     * 获取配置
     * @author SkyDev
     * @date 2025-07-11 16:37:36
     */
    List<ConfigForm> getForms();
}
