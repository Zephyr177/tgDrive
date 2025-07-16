package com.skydevs.tgdrive.mapper;

import com.skydevs.tgdrive.dto.ConfigForm;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Description:
 * 配置文件mapper
 * @author SkyDev
 * @date 2025-07-14 09:49:46
 */
@Mapper
public interface ConfigMapper {
    /**
     * Description:
     * 新增配置文件
     * @author SkyDev
     * @date 2025-07-14 09:49:32
     */
    void insert(ConfigForm configForm);


    /**
     * Description:
     * 根据配置文件名查询配置
     * @param name 配置文件名
     * @return ConfigForm
     * @author SkyDev
     * @date 2025-07-14 09:48:44
     */
    @Select("SELECT * From configs where name = #{name}")
    ConfigForm getByName(@Param("name")String name);

    /**
     * Description:
     * 根据配置文件名删除配置
     * @param name 配置文件名
     * @author SkyDev
     * @date 2025-07-14 09:47:58
     */
    @Delete("DELETE FROM configs where name = #{name}")
    void deleteByName(String name);

    /**
     * Description:
     * 获取全部配置文件及其信息
     * @return List of ConfigForm
     * @author SkyDev
     * @date 2025-07-14 09:47:04
     */
    @Select("SELECT * FROM configs")
    List<ConfigForm> getAll();
}
