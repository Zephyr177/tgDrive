package com.skydevs.tgdrive.utils

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Protocol
import java.util.concurrent.TimeUnit

object OkHttpClientFactory {
    // 提供一个方法，返回配置好的 OkHttpClient 实例
    @JvmStatic
    fun createClient(): OkHttpClient {
        // 自定义连接池设置 - 减少最大空闲连接数，但增加存活时间
        val connectionPool = ConnectionPool(3, 5, TimeUnit.MINUTES)

        // 创建并返回 OkHttpClient 实例
        return OkHttpClient.Builder()
            .connectionPool(connectionPool)
            .connectTimeout(30, TimeUnit.SECONDS)  // 减少连接超时
            .readTimeout(120, TimeUnit.SECONDS)    // 增加读取超时
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)        // 启用连接失败重试
            .build()
    }
    
    // 为下载大文件提供专用客户端
    @JvmStatic
    fun createDownloadClient(): OkHttpClient {
        // 仅使用HTTP/1.1，避免HTTP/2可能的内存问题
        val protocols = listOf(Protocol.HTTP_1_1)
        
        return OkHttpClient.Builder()
            .protocols(protocols)
            .connectionPool(ConnectionPool(5, 2, TimeUnit.MINUTES)) // 增加并发连接
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.MINUTES)      // 更长的读取超时用于大文件
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)                 // 自动跟随重定向
            .followSslRedirects(true)
            .build()
    }
}