package com.image_loading_library.impl.utils

import kotlinx.coroutines.CoroutineDispatcher

internal interface DispatcherProvider {

    fun io(): CoroutineDispatcher
    fun main(): CoroutineDispatcher

}