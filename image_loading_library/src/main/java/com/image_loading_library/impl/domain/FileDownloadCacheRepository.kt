package com.image_loading_library.impl.domain

internal interface FileDownloadCacheRepository {

    fun init(cacheDirectory: String)

    fun isInited(): Boolean

    suspend fun putInCache(url: String, imageBytes: ByteArray)

    suspend fun renewItem(url: String)

    suspend fun findInCache(url: String): ByteArray?

    suspend fun getItemCount(): Int

    suspend fun getCacheSize(): Long

    suspend fun invalidateCache()

    suspend fun removeOldestItem()

}