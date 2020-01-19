package com.image_loading_library.impl.di

import com.image_loading_library.ImageLoader
import com.image_loading_library.impl.domain.FileDownloader
import com.image_loading_library.impl.domain.ImageDownloadInteractor
import dagger.Component

@Component(modules = [ImageLoaderModule::class])
@ImageLoaderScope
internal interface ImageLoaderComponent {

    fun getImageDownloader(): ImageLoader
    fun getImageDownloadInteractor(): ImageDownloadInteractor

    interface Builder {
        fun build(): ImageLoaderComponent
    }
}