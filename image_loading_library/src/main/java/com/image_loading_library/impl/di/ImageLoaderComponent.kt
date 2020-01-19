package com.image_loading_library.impl.di

import com.image_loading_library.ImageLoader
import com.image_loading_library.impl.domain.FileDownloader
import dagger.Component

@Component(modules = [ImageLoaderModule::class])
@ImageLoaderScope
internal interface ImageLoaderComponent {

    fun getImageDownloader(): ImageLoader

    interface Builder {
        fun build(): ImageLoaderComponent
    }
}