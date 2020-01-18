package com.image_loading_library.impl.di

import com.image_loading_library.impl.presentation.ImageDownloaderInternal
import com.image_loading_library.impl.domain.FileDownloader
import dagger.Component

@Component(modules = [ImageLoaderModule::class])
@ImageLoaderScope
internal interface ImageLoaderComponent {

    fun getImageDownloaderInternal(): ImageDownloaderInternal
    fun getDownloader(): FileDownloader

    interface Builder {
        fun build(): ImageLoaderComponent
    }
}