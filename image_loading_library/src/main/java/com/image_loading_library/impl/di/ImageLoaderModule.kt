package com.image_loading_library.impl.di

import com.image_loading_library.ImageDownloader
import com.image_loading_library.impl.ImageDownloaderImpl
import com.image_loading_library.impl.data.ImageDownloadRepositoryImpl
import com.image_loading_library.impl.domain.ImageDownloadRepository
import com.image_loading_library.impl.utils.DispatcherProvider
import com.image_loading_library.impl.utils.DispatcherProviderImpl
import com.image_loading_library.impl.utils.network.FileDownloader
import com.image_loading_library.impl.utils.network.FileDownloaderImpl
import dagger.Binds
import dagger.Module

@Module
internal abstract class ImageLoaderModule {

    @Binds
    @ImageLoaderScope
    abstract fun provideDispatcherProvider(dispatcherProvider: DispatcherProviderImpl): DispatcherProvider

    @Binds
    @ImageLoaderScope
    abstract fun provideImageDownloadRepository(imageDownloadRepository: ImageDownloadRepositoryImpl): ImageDownloadRepository

    @Binds
    @ImageLoaderScope
    abstract fun provideDownloader(downloader: FileDownloaderImpl): FileDownloader



    // Without scope because it should be created new for each image
    @Binds
    abstract fun provideImageDownloader(imageDownloader: ImageDownloaderImpl): ImageDownloader

}