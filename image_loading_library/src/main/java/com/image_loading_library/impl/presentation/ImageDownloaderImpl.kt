package com.image_loading_library.impl.presentation

import android.graphics.*
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.PathShape
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.image_loading_library.R
import com.image_loading_library.impl.domain.ImageDownloadInteractor
import com.image_loading_library.impl.model.DownloadProgress
import com.image_loading_library.impl.utils.DispatcherProvider
import com.image_loading_library.impl.utils.logs.log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.math.max


internal class ImageDownloaderImpl
    @Inject
    constructor(
        private val imageDownloadInteractor: ImageDownloadInteractor,
        private val dispatcherProvider: DispatcherProvider
    ) : ImageDownloaderInternal, CoroutineScope {

    override val coroutineContext: CoroutineContext = dispatcherProvider.io() + Job()

    override var progressPlaceHolder: Bitmap? = null
    override var errorPlaceHolder: Bitmap? = null
    override var progressColor: Int = 0

    override var doOnFail: ((throwable: Throwable) -> Unit)? = null
    override var doOnSuccess: (() -> Unit)? = null

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

    override fun cancel() {
        coroutineContext.cancel()
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
        doOnFail?.invoke(error.throwable)
    }

    private suspend fun handleDownloadSuccess(success: DownloadProgress.Success) {
        log { i(TAG, "ImageDownloaderImpl.handleDownloadSuccess(). success = [${success.bytes.size} bytes]") }

        setProgress(0)
        setImageBitmap(BitmapFactory.decodeByteArray(success.bytes, 0, success.bytes.size))
        doOnSuccess?.invoke()
    }

    private suspend fun setProgress(progress: Int) {
        log { i(TAG, "ImageDownloaderImpl.setProgress(). progress = [${progress}]") }

        withContext(dispatcherProvider.main()) {
            val layerList = imageView?.drawable as LayerDrawable

            val strokeWidth = 20f
            val arcLeft = strokeWidth / 2
            val arcTop = strokeWidth / 2
            val arcRight = imageView!!.width.toFloat() - strokeWidth / 2
            val arcBottom = imageView!!.height.toFloat() - strokeWidth / 2

            val path = Path().apply {
                addArc(arcLeft, arcTop, arcRight, arcBottom, -90f, 360 * progress / 100f)
            }

            val pathShape = PathShape(path, imageView!!.width.toFloat(), imageView!!.height.toFloat())
            val shapeDrawable = ShapeDrawable(pathShape).apply {
                setBounds(0, 0, imageView!!.width, imageView!!.height)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = strokeWidth
                paint.color = progressColor
            }

            layerList.setDrawableByLayerId(R.id.layer_list_item_progress_ring, shapeDrawable)
        }
    }

    private suspend fun setImageBitmap(bitmap: Bitmap) {
        withContext(dispatcherProvider.main()) {
            val layerList = imageView?.drawable as LayerDrawable

            val croppedBitmap = cropToSquare(bitmap)
            val roundedDrawable = RoundedBitmapDrawableFactory.create(imageView!!.context.resources, croppedBitmap)
            roundedDrawable.setAntiAlias(true)
            roundedDrawable.cornerRadius = max(bitmap.width, bitmap.height) / 2.0f
            layerList.setDrawableByLayerId(R.id.layer_list_item_image, roundedDrawable)
            layerList.invalidateSelf()
        }
    }

    private fun cropToSquare(bitmap: Bitmap): Bitmap? {
        val width = bitmap.width
        val height = bitmap.height
        val newWidth = if (height > width) width else height
        val newHeight = if (height > width) height - (height - width) else height
        var cropW = (width - height) / 2
        cropW = if (cropW < 0) 0 else cropW
        var cropH = (height - width) / 2
        cropH = if (cropH < 0) 0 else cropH
        return Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight)
    }
    companion object {
        private const val TAG = "ImageDownloader"

    }

}