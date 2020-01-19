package com.image_loading_library.impl.domain

internal interface FileDownloadCacheRepository {

    /**
     * Initializes cache repository.
     *
     * @param cacheDirectory cache directory absolute path
     */
    fun init(cacheDirectory: String)

    /**
     * Checks is the repository is initialized
     *
     * @return true if initialized, false otherwise
     */
    fun isInited(): Boolean

    /**
     * Puts file, downloaded from provided url in cache.
     *
     * @param url url from the file was downloaded
     * @param fileBytes content of the file
     */
    suspend fun putInCache(url: String, fileBytes: ByteArray)

    /**
     * Renews item in cache. Just renews stored date without content modification.
     *
     * @param url url from the file was downloaded
     */
    suspend fun renewItem(url: String)

    /**
     * Finds file in cache.
     *
     * @param url url from the file was downloaded
     *
     * @return file content bytes or null if file is not found in cache
     */
    suspend fun findInCache(url: String): ByteArray?

    /**
     * Returns item count in cache.
     *
     * @return Item count
     */
    suspend fun getItemCount(): Int

    /**
     * Returns actual cache size.
     *
     * @return current cache is bytes
     */
    suspend fun getCacheSize(): Long

    /**
     * Deletes all items in cache
     *
     */
    suspend fun invalidateCache()

    /**
     * Removes one oldest item in cache
     *
     */
    suspend fun removeOldestItem()

}