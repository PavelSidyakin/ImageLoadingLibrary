package com.image_loading_library

import android.graphics.Bitmap
import android.widget.ImageView

interface ImageLoader {

    /**
     * Sets [ImageView] to be used for downloaded image.
     *
     * @param imageView image view
     */
    fun into(imageView: ImageView)

    /**
     * Loads image from url.
     *
     * @param url URL string
     */
    fun load(url: String)

    /**
     * Cancels loading.
     */
    fun cancel()

    /**
     * Returns or sets callback for successful loading.
     */
    var doOnSuccess: (() -> Unit)?

    /**
     * Returns or sets callback for failed loading.
     */
    var doOnFail: ((throwable: Throwable) -> Unit)?

    /**
     * Returns or sets image placeholder during download process.
     *
     * If not set, transparent background is used.
     */
    var progressPlaceHolder: Bitmap?

    /**
     * Returns or sets image placeholder in case of error.
     *
     * If not set, transparent background is used.
     */
    var errorPlaceHolder: Bitmap?

    /**
     * Returns or sets progress color.
     *
     * If not set, black color is used.
     */
    var progressColor: Int

}