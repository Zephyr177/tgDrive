package com.skydevs.tgdrive.mapper;

import com.github.pagehelper.Page;
import com.skydevs.tgdrive.entity.FileInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Description:
 * 文件mapper
 * @author SkyDev
 * @date 2025-07-11 17:41:20
 */
@Mapper
public interface FileMapper {

    /**
     * Description:
     * 插入已上传文件
     * @author SkyDev
     * @date 2025-07-11 17:33:41
     */
    @Insert("INSERT INTO files (file_name, download_url, upload_time, file_id, size, full_size, webdav_path, dir) VALUES (#{fileName}, #{downloadUrl}, #{uploadTime}, #{fileId}, #{size}, #{fullSize}, #{webdavPath}, #{dir})")
    void insertFile(FileInfo fileInfo);

    /**
     * Description:
     * 获取全部文件
     * @author SkyDev
     * @date 2025-07-11 17:34:09
     */
    @Select("SELECT * FROM files order by upload_time desc ")
    Page<FileInfo> getAllFiles();

    /**
     * Description:
     * 根据文件id获取文件名
     * @author SkyDev
     * @date 2025-07-11 17:34:21
     */
    @Select("SELECT file_name FROM files where file_id = #{fileId} AND (webdav_path IS NULL OR webdav_path != 'deleted') LIMIT 1")
    String getFileNameByFileId(String fileId);

    /**
     * Description:
     * 根据文件id获取文件完整大小
     * @author SkyDev
     * @date 2025-07-11 17:34:41
     */
    @Select("SELECT full_size FROM files where file_id = #{fileId} LIMIT 1")
    Long getFullSizeByFileId(String fileId);

    /**
     * Description:
     * 更新前缀URL
     * @author SkyDev
     * @date 2025-07-11 17:35:09
     */
    void updateUrl(String prefix);

    /**
     * Description:
     * 根据webdav path获取文件信息
     * @author SkyDev
     * @date 2025-07-11 17:35:33
     */
    @Select("SELECT * FROM files WHERE webdav_path = #{path}")
    FileInfo getFileByWebdavPath(String path);

    /**
     * Description:
     * 根据webdav path获取一个文件夹下的所有文件
     * @author SkyDev
     * @date 2025-07-11 17:36:14
     */
    @Select("SELECT * FROM files WHERE webdav_path LIKE #{path} || '%' ORDER BY id DESC")
    List<FileInfo> getFilesByPathPrefix(String path);

    /**
     * Description:
     * 删除文件
     * @author SkyDev
     * @date 2025-07-11 17:37:23
     */
    @Select("DELETE FROM files WHERE file_id = #{fileId}")
    void deleteFile(String fileId);

    /**
     * Description:
     * 根据webdav的路径删除文件
     * @author SkyDev
     * @date 2025-07-11 17:37:34
     */
    @Delete("DELETE FROM files WHERE webdav_path LIKE CONCAT(#{path}, '%')")
    void deleteFileByWebDav(String path);

    /**
     * Description:
     * 根据webdav的路径更新文件信息
     * @author SkyDev
     * @date 2025-07-11 17:38:05
     */
    @Update("UPDATE files SET download_url = #{file.downloadUrl}, upload_time = #{file.uploadTime}, size = #{file.size}, full_size = #{file.fullSize}, file_id = #{file.fileId} WHERE webdav_path = #{target}")
    void updateFileAttributeByWebDav(@Param("file") FileInfo file, @Param("target") String target);

    /**
     * Description:
     * 插入一个文件，文件的信息是原文件的信息，只修改了webdav的路径为目标路径
     * @author SkyDev
     * @date 2025-07-11 17:40:23
     */
    @Insert("INSERT INTO files (file_name, download_url, upload_time, file_id, size, full_size, webdav_path, dir) VALUES (#{file.fileName}, #{file.downloadUrl}, #{file.uploadTime}, #{file.fileId}, #{file.size}, #{file.fullSize}, #{target}, #{file.dir})")
    void moveFile(@Param("file") FileInfo sourceFile, @Param("target") String target);
}
