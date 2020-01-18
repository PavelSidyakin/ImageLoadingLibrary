package com.image_loading_library.impl.presentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.image_loading_library.R
import com.image_loading_library.impl.domain.ImageDownloadInteractor
import com.image_loading_library.impl.model.DownloadProgress
import com.image_loading_library.impl.utils.DispatcherProvider
import com.image_loading_library.impl.utils.logs.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

internal class ImageDownloaderImpl
    @Inject
    constructor(
        private val imageDownloadInteractor: ImageDownloadInteractor,
        private val dispatcherProvider: DispatcherProvider
    ) : ImageDownloaderInternal, CoroutineScope {

    override val coroutineContext: CoroutineContext = dispatcherProvider.io() + Job()

    override var progressPlaceHolder: Bitmap? = null
    override var errorPlaceHolder: Bitmap? = null

    private var imageView: ImageView? = null

    override fun into(imageView: ImageView) {
        this.imageView = imageView
        imageView.setImageDrawable(imageView.context?.resources?.getDrawable(R.drawable.image_with_progress_layer_list, null))
    }

    override fun load(url: String) {
        if (imageView == null) {
            throw RuntimeException("ImageView is not set. Call into() before call load()")
        }

        launch {
            imageDownloadInteractor.requestImage(url)
                .collect { progress ->

                    when(progress) {
                        is DownloadProgress.Start -> handleDownloadStart(progress)
                        is DownloadProgress.GotSize-> handleDownloadGotSize(progress)
                        is DownloadProgress.Progress -> handleDownloadProgress(progress)
                        is DownloadProgress.Success -> handleDownloadSuccess(progress)
                        is DownloadProgress.Error-> handleDownloadError(progress)
                    }
                }
        }
    }

    private suspend fun handleDownloadStart(start: DownloadProgress.Start) {
        log { i(TAG, "ImageDownloaderImpl.handleDownloadStart(). progress=$start, progressPlaceHolder=$progressPlaceHolder") }

        progressPlaceHolder?.let { setImageBitmap(it) }
    }

    private suspend fun handleDownloadGotSize(progress: DownloadProgress.GotSize) {
        log { i(TAG, "ImageDownloaderImpl.handleDownloadGotSize(). progress = [${progress}]") }

    }

    private suspend fun handleDownloadProgress(progress: DownloadProgress.Progress) {
        log { i(TAG, "ImageDownloaderImpl.handleDownloadProgress(). progress = [${progress}]") }

        progressPlaceHolder?.let { setImageBitmap(it) }
        setProgress(progress.progressPercent)
    }

    private suspend fun handleDownloadError(error: DownloadProgress.Error) {
        log { i(TAG, "ImageDownloaderImpl.handleDownloadError(). error=$error errorPlaceHolder=$errorPlaceHolder") }

        errorPlaceHolder?.let { setImageBitmap(it) }
        setProgress(0)
    }

    private suspend fun handleDownloadSuccess(success: DownloadProgress.Success) {
        log { i(TAG, "ImageDownloaderImpl.handleDownloadSuccess(). success = [${success.bytes.size} bytes]") }

        setImageBitmap(BitmapFactory.decodeByteArray(success.bytes, 0, success.bytes.size))
        setProgress(0)
    }

    private suspend fun setProgress(progress: Int) {
        log { i(TAG, "ImageDownloaderImpl.setProgress(). progress = [${progress}]") }

        withContext(dispatcherProvider.main()) {
            val layerList = imageView?.drawable as LayerDrawable
            val progressRingGradient = layerList.findDrawableByLayerId(R.id.layer_list_item_progress_ring) as GradientDrawable

            if (progress == 0) {
                // Hide progress
                progressRingGradient.setColor(imageView!!.resources.getColor(android.R.color.transparent))
            }

            progressRingGradient.setGradientCenter(0.5f, progress / 100f)
        }
    }

    private suspend fun setImageBitmap(bitmap: Bitmap) {
        withContext(dispatcherProvider.main()) {
            val layerList = imageView?.drawable as LayerDrawable

            val roundedDrawable = RoundedBitmapDrawableFactory.create(imageView!!.context.resources, bitmap)
            roundedDrawable.setAntiAlias(true)
            roundedDrawable.isCircular = true

            layerList.setDrawableByLayerId(R.id.layer_list_item_image, roundedDrawable)
        }
    }

    companion object {
        private const val TAG = "ImageDownloader"

    }

}