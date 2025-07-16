package com.skydevs.tgdrive.utils;

public class UserFriendly {
    /**
     * Description:
     * 将size转换为人类易读的单位
     * @author SkyDev
     * @param size 文件大小
     * @date 2025-07-16 16:51:58
     * @return 人类易读的单位
     */
    public static String humanReadableFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int unitIndex = (int) (Math.log10(size) / Math.log10(1024));
        double readableSize = size / Math.pow(1024, unitIndex);
        return String.format("%.1f %s", readableSize, units[unitIndex]);
    }
}
