package com.skydevs.tgdrive.entity;

import lombok.*;

import java.util.List;

/**
 * Description:
 * 大文件信息
 * @author SkyDev
 * @date 2025-07-11 17:31:47
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BigFileInfo {
    private String fileName;
    /**
     * 文件大小
     */
    private long fileSize;

    /**
     * 分片文件各个id
     */
    private List<String> fileIds;

    /**
     * 是否时记录文件，如果是记录文件，则根据记录文件的fileIds属性获取各个分片文件的id
     */
    private boolean isRecordFile;
}
