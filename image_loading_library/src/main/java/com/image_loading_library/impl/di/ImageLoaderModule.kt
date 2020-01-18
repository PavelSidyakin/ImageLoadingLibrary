package com.image_loading_library.impl.di

import com.image_loading_library.impl.presentation.ImageDownloaderImpl
import com.image_loading_library.impl.presentation.ImageDownloaderInternal
import com.image_loading_library.impl.utils.DispatcherProvider
import com.image_loading_library.impl.utils.DispatcherProviderImpl
import com.image_loading_library.impl.domain.FileDownloader
import com.image_loading_library.impl.data.FileDownloaderImpl
import com.image_loading_library.impl.domain.ImageDownloadInteractor
import com.image_loading_library.impl.domain.ImageDownloadInteractorImpl
import dagger.Binds
import dagger.Module

@Module
internal abstract class ImageLoaderModule {

    @Binds
    @ImageLoaderScope
    abstract fun provideDispatcherProvider(dispatcherProvider: DispatcherProviderImpl): DispatcherProvider

    @Binds
    @ImageLoaderScope
    abstract fun provideFileDownloader(fileDownloader: FileDownloaderImpl): FileDownloader

    @Binds
    @ImageLoaderScope
    abstract fun provideImageDownloadInteractor(downloader: ImageDownloadInteractorImpl): ImageDownloadInteractor

    // Without scope because it should be created new for each image
    @Binds
    abstract fun provideImageDownloader(imageDownloader: ImageDownloaderImpl): ImageDownloaderInternal

}