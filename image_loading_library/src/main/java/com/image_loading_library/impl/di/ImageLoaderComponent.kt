package com.image_loading_library.impl.di

import com.image_loading_library.ImageDownloader
import com.image_loading_library.impl.domain.ImageDownloadStream
import com.image_loading_library.impl.utils.network.FileDownloader
import dagger.Component

@Component(modules = [ImageLoaderModule::class])
@ImageLoaderScope
internal interface ImageLoaderComponent {

    fun getImageDownloader(): ImageDownloader
    fun getDownloader(): FileDownloader

    fun inject(imageDownloadStream: ImageDownloadStream)

    interface Builder {
        fun build(): ImageLoaderComponent
    }
}