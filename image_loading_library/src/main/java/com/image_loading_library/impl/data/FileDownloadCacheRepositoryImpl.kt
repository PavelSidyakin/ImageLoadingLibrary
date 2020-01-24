package com.image_loading_library.impl.data

import android.util.Base64
import com.image_loading_library.impl.domain.FileDownloadCacheRepository
import com.image_loading_library.impl.utils.DispatcherProvider
import com.image_loading_library.impl.utils.logs.log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.GZIPOutputStream
import javax.inject.Inject
import kotlin.text.Charsets.UTF_8

internal class FileDownloadCacheRepositoryImpl

    @Inject
    constructor(
        private val dispatcherProvider: DispatcherProvider
    ): FileDownloadCacheRepository {

    private var cacheDirectory: String = ""
    private val cacheDirMutex = Mutex()

    override fun init(cacheDirectory: String) {
        this.cacheDirectory = cacheDirectory
        File(cacheDirectory).mkdirs()
    }

    override suspend fun putInCache(url: String, fileBytes: ByteArray) {
        log { i(TAG, "FileDownloadCacheRepositoryImpl.putInCache(). url = [${url}], imageBytesSize = [${fileBytes.size}]") }
        withContext(dispatcherProvider.io()) {
            cacheDirMutex.withLock {
                File(makePathForUrl(url)).writeBytes(fileBytes)
            }
        }
    }

    override suspend fun findInCache(url: String): ByteArray? {
        log { i(TAG, "FileDownloadCacheRepositoryImpl.findInCache(). url = [${url}]") }
        return doInCacheDir { cacheDir ->
            cacheDir.walk().find { file -> file.name.endsWith(encodeUrlForFileName(url)) }?.readBytes()
        }
    }

    override suspend fun getItemCount(): Int {
        return doInCacheDir { cacheDir ->
            cacheDir.listFiles()?.size ?: 0
        }.also {
            log { i(TAG, "FileDownloadCacheRepositoryImpl.getItemCount(). returned $it") }
        }
    }

    override suspend fun getCacheSize(): Long {
        return doInCacheDir { cacheDir ->
            var dirSize = 0L
            cacheDir.walk().forEach { file ->
                dirSize += file.length()
            }

            dirSize
        }.also {
            log { i(TAG, "FileDownloadCacheRepositoryImpl.getCacheSize(). returned $it") }
        }
    }

    override suspend fun invalidateCache() {
        log { i(TAG, "FileDownloadCacheRepositoryImpl.invalidateCache(). ") }

        doInCacheDir { cacheDir ->
            cacheDir.walk().forEach { file ->
                file.delete()
            }
        }
    }

    override suspend fun removeOldestItem() {
        log { i(TAG, "FileDownloadCacheRepositoryImpl.removeOldestItem(). ") }

        doInCacheDir { cacheDir ->
            cacheDir.walk().sortedBy { file -> file.name }.first().also {
                log { i(TAG, "FileDownloadCacheRepositoryImpl.removeOldestItem(). deleting ${it.name}") }
            }.delete()
        }
    }

    override suspend fun renewItem(url: String) {
        doInCacheDir { cacheDir ->
            cacheDir.walk().find { file -> file.name.endsWith(encodeUrlForFileName(url)) }
                ?.also {
                   log { i(TAG, "FileDownloadCacheRepositoryImpl.renewItem(). renaming ${it.name}") }
                }?.renameTo(File(makePathForUrl(url)))
        }.also {
            log { i(TAG, "FileDownloadCacheRepositoryImpl.renewItem(). returned $it") }
        }
    }

    private suspend fun <R> doInCacheDir(block: (cacheDir: File) -> R): R {
        if (cacheDirectory.isBlank()) {
            throw RuntimeException("doInCacheDir() Cache dir is not set. Call init() first.")
        }

        return withContext(dispatcherProvider.io()) {
            cacheDirMutex.withLock {
                block(File(cacheDirectory))
            }
        }
    }

    private fun makePathForUrl(url: String): String {
        if (cacheDirectory.isBlank()) {
            throw RuntimeException("makePathForUrl() Cache dir is not set. Call init() first.")
        }

        return File(cacheDirectory).resolve(makeFileNameForUrl(url)).toString()
    }

    override fun isInited(): Boolean {
        return cacheDirectory.isNotBlank()
    }

    private fun makeFileNameForUrl(url: String): String {
        return "${System.currentTimeMillis()}_${encodeUrlForFileName(url)}"
    }

    private fun encodeUrlForFileName(url: String): String {
        return Base64.encodeToString(zip(url), Base64.URL_SAFE or Base64.NO_WRAP)
    }

    private fun zip(str: String): ByteArray {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).bufferedWriter(UTF_8).use { it.write(str) }
        return bos.toByteArray()
    }

    companion object {
        private const val TAG = "FileCacheRepo"
    }

}