package com.image_loading_library.impl.domain

import com.image_loading_library.impl.model.DownloadProgress
import com.image_loading_library.impl.utils.logs.XLog
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyArray
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

internal class ImageDownloadInteractorImplTest {

    @Mock
    private lateinit var fileDownloader: FileDownloader
    @Mock
    private lateinit var fileDownloadCacheRepository: FileDownloadCacheRepository

    @InjectMocks
    private lateinit var imageDownloadInteractor: ImageDownloadInteractorImpl

    @BeforeEach
    fun beforeEachTest() {
        MockitoAnnotations.initMocks(this)
        XLog.enableLogging(false)
    }

    @Nested
    inner class `No cache tests` {
        @BeforeEach
        fun beforeEachTest() {
            imageDownloadInteractor.cacheDirectory = ""
            whenever(fileDownloadCacheRepository.isInited()).thenReturn(false)
        }

        @Test
        fun `When download file, the same result from downloader should be emitted`() {
            runBlocking {
                val progressPercent0 = 10
                val progressPercent1 = 20
                val successBytes = byteArrayOf(1,2,3)
                val errorThrowable = RuntimeException("Aaaaa")

                // when
                whenever(fileDownloader.downloadFile(anyString())).thenReturn(flow {
                    emit(DownloadProgress.Start)
                    emit(DownloadProgress.Progress(progressPercent0))
                    emit(DownloadProgress.Progress(progressPercent1))
                    emit(DownloadProgress.Success(successBytes))
                    emit(DownloadProgress.Error(errorThrowable))
                })

                val results = arrayListOf<DownloadProgress>()

                // action
                imageDownloadInteractor.requestImage("uuuu").collect {
                    results.add(it)
                }

                // verify
                assert(results[0] == DownloadProgress.Start)
                assert(results[1] == DownloadProgress.Progress(progressPercent0))
                assert(results[2] == DownloadProgress.Progress(progressPercent1))
                assert(results[3] == DownloadProgress.Success(successBytes))
                assert(results[4] == DownloadProgress.Error(errorThrowable))
            }
        }

        @AfterEach
        fun afterEachTest() {
            verify(fileDownloadCacheRepository, never()).init(anyString())

            runBlocking {
                verify(fileDownloadCacheRepository, never()).findInCache(anyString())
                verify(fileDownloadCacheRepository, never()).invalidateCache()
                verify(fileDownloadCacheRepository, never()).putInCache(anyString(), any())
                verify(fileDownloadCacheRepository, never()).removeOldestItem()
                verify(fileDownloadCacheRepository, never()).getCacheSize()
                verify(fileDownloadCacheRepository, never()).getItemCount()
            }

        }
    }

    @Nested
    inner class `With cache tests` {
        private val maxCacheSizeBytes: Int = 55
        private val maxNumberOfCachedItems: Int = 17

        @BeforeEach
        fun beforeEachTest() {
            imageDownloadInteractor.cacheDirectory = "ddddd"
            imageDownloadInteractor.maxNumberOfCachedItems = maxNumberOfCachedItems
            imageDownloadInteractor.maxCacheSizeBytes = maxCacheSizeBytes
            whenever(fileDownloadCacheRepository.isInited()).thenReturn(true)

        }

        @Test
        fun `When image found in cache, it should be returned in Success immediately and should be updated in renewed in cache`() {
            runBlocking {
                val url = "uuuuurl"
                val cachedBytes = byteArrayOf(1,2,3)

                // when
                whenever(fileDownloader.downloadFile(anyString())).thenReturn(flow { })
                whenever(fileDownloadCacheRepository.findInCache(anyString())).thenReturn(cachedBytes)

                //action
                imageDownloadInteractor.requestImage(url).collect { progress ->

                    // verify
                    when(progress) {
                        is DownloadProgress.Start -> assert(false)
                        is DownloadProgress.Success -> assert(progress.bytes.contentEquals(cachedBytes))
                        is DownloadProgress.Error -> assert(false)
                        is DownloadProgress.Progress -> assert(false)
                    }
                }

                // verify
                verify(fileDownloader, never()).downloadFile(anyString())
                verify(fileDownloadCacheRepository, never()).putInCache(anyString(), any())
                verify(fileDownloadCacheRepository).renewItem(url)
                verify(fileDownloadCacheRepository, never()).invalidateCache()
                verify(fileDownloadCacheRepository, never()).removeOldestItem()
            }
        }

        @Nested
        inner class `When cache params are incorrect, shouldn't try to put in cache` {
            val imageBytes = byteArrayOf(1, 2, 3)

            @BeforeEach
            fun beforeEachTest() {
                // when
                runBlocking {
                    whenever(fileDownloader.downloadFile(anyString())).thenReturn(flow {
                        emit(DownloadProgress.Success(imageBytes))
                    })
                }
            }

            @Test
            fun `When cache max item count is 0`() {
                runBlocking {
                    imageDownloadInteractor.maxNumberOfCachedItems = 0

                    // action
                    imageDownloadInteractor.requestImage("csdce").collect {
                    }
                }
            }

            @Test
            fun `When cache max size is 0`() {
                runBlocking {
                    imageDownloadInteractor.maxCacheSizeBytes = 0

                    // action
                    imageDownloadInteractor.requestImage("wede").collect {
                    }
                }
            }

            @Test
            fun `When image size is greater then max cache size`() {
                runBlocking {
                    whenever(fileDownloader.downloadFile(anyString())).thenReturn(flow {
                        emit(DownloadProgress.Success(imageBytes))
                    })

                    imageDownloadInteractor.maxCacheSizeBytes = imageBytes.size - 1

                    // action
                    imageDownloadInteractor.requestImage("wede").collect {
                    }
                }
            }

            @AfterEach
            fun afterEachTest() {
                runBlocking {
                    verify(fileDownloadCacheRepository, never()).putInCache(anyString(), any())
                    verify(fileDownloadCacheRepository, never()).invalidateCache()
                    verify(fileDownloadCacheRepository, never()).removeOldestItem()
                }
            }
        }

        @Nested
        inner class `When image is not found in cache` {
            private val url = "uuuuuurl"
            private var imageBytes = byteArrayOf()

            @BeforeEach
            fun beforeEachTest() {
                runBlocking {
                    whenever(fileDownloadCacheRepository.findInCache(anyString())).thenReturn(null)
                }
            }

            @Test
            fun `When cache free space enough, image should be put in cache immediately` () {
                runBlocking {
                    imageBytes = ByteArray(maxCacheSizeBytes - 1)

                    // when
                    whenever(fileDownloader.downloadFile(anyString())).thenReturn(flow {
                        emit(DownloadProgress.Success(imageBytes))
                    })
                    whenever(fileDownloadCacheRepository.getItemCount()).thenReturn(maxNumberOfCachedItems - 1)
                    whenever(fileDownloadCacheRepository.getCacheSize()).thenReturn(1)

                    // action
                    imageDownloadInteractor.requestImage(url).collect {
                    }

                    // verify
                    val inOrder = inOrder(fileDownloader, fileDownloadCacheRepository)
                    inOrder.verify(fileDownloader).downloadFile(url)
                    inOrder.verify(fileDownloadCacheRepository).putInCache(url, imageBytes)
                    verify(fileDownloadCacheRepository, never()).invalidateCache()
                    verify(fileDownloadCacheRepository, never()).removeOldestItem()
                }
            }

            @Nested
            inner class `When cache free space in not enough` {
                @Test
                fun `disk space is not enough` () {
                    runBlocking {
                        imageBytes = ByteArray(2)

                        // when
                        whenever(fileDownloader.downloadFile(anyString())).thenReturn(flow {
                            emit(DownloadProgress.Success(imageBytes))
                        })
                        whenever(fileDownloadCacheRepository.getItemCount()).thenReturn(1)
                        whenever(fileDownloadCacheRepository.getCacheSize()).thenReturn(maxCacheSizeBytes - 1L)
                        whenever(fileDownloadCacheRepository.removeOldestItem()).then {
                            runBlocking {
                                whenever(fileDownloadCacheRepository.getItemCount()).thenReturn(0)
                                whenever(fileDownloadCacheRepository.getCacheSize()).thenReturn(0)
                            }
                        }


                        // action
                        imageDownloadInteractor.requestImage(url).collect {
                        }
                    }
                }

                @Test
                fun `number of items exceeded` () {
                    runBlocking {
                        imageBytes = ByteArray(2)

                        // when
                        whenever(fileDownloader.downloadFile(anyString())).thenReturn(flow {
                            emit(DownloadProgress.Success(imageBytes))
                        })
                        whenever(fileDownloadCacheRepository.getItemCount()).thenReturn(maxNumberOfCachedItems)
                        whenever(fileDownloadCacheRepository.getCacheSize()).thenReturn(0)
                        whenever(fileDownloadCacheRepository.removeOldestItem()).then {
                            runBlocking {
                                whenever(fileDownloadCacheRepository.getItemCount()).thenReturn(maxNumberOfCachedItems - 1)
                                whenever(fileDownloadCacheRepository.getCacheSize()).thenReturn(0)
                            }
                        }

                        // action
                        imageDownloadInteractor.requestImage(url).collect {
                        }
                    }
                }

                @AfterEach
                fun afterEachTest() {
                    // verify
                    runBlocking {
                        verify(fileDownloadCacheRepository).removeOldestItem()
                    }
                }
            }


            @AfterEach
            fun afterEachTest() {
                runBlocking {
                    // verify
                    val inOrder = inOrder(fileDownloader, fileDownloadCacheRepository)
                    inOrder.verify(fileDownloader).downloadFile(url)
                    inOrder.verify(fileDownloadCacheRepository).putInCache(url, imageBytes)
                }

            }
        }

    }

}