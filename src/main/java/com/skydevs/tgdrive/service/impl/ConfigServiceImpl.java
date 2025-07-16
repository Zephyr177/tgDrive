package com.skydevs.tgdrive.service.impl;

import com.skydevs.tgdrive.dto.ConfigForm;
import com.skydevs.tgdrive.exception.NoConfigException;
import com.skydevs.tgdrive.mapper.ConfigMapper;
import com.skydevs.tgdrive.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description:
 * 配置服务类
 * @author nanyang
 * @date 2025-07-11 16:27:17
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final ConfigMapper configMapper;

    /**
     * Description: 根据文件名查询配置
     *
     * @param filename 文件名
     * @return ConfigForm
     * @author nanyang
     * @date 2025-07-11 16:21:26
     */
    @Override
    public ConfigForm get(String filename) {
        return configMapper.getByName(filename);
    }

    /**
     * Description:
     * 插入配置
     * @param configForm 配置表单
     * @author nanyang
     * @date 2025-07-11 16:27:56
     */
    @Override
    public void save(ConfigForm configForm) {
        ConfigForm temp = configMapper.getByName(configForm.getName());
        if (temp == null) {
            configMapper.insert(configForm);
        } else {
            configMapper.deleteByName(configForm.getName());
            configMapper.insert(configForm);
        }
    }

    /**
     * Description:
     * 获取配置
     * @author SkyDev
     * @date 2025-07-11 16:37:36
     */
    @Override
    public List<ConfigForm> getForms() {
        List<ConfigForm> configForms =  configMapper.getAll();
        if (configForms == null) {
            throw new NoConfigException();
        }
        return configForms;
    }
}
