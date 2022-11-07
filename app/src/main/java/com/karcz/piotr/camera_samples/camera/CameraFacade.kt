@file:Suppress("DEPRECATION")

package com.karcz.piotr.camera_samples.camera

import android.hardware.Camera
import android.view.SurfaceHolder
import timber.log.Timber

private const val TIMBER_TAG = "CameraFacade"

class CameraFacade(private val targetResolutions: List<Pair<Int, Int>>) : SurfaceHolder.Callback {

    private var camera: Camera? = null

    override fun surfaceCreated(holder: SurfaceHolder) {
        camera?.apply {
            try {
                setPreviewDisplay(holder)
                setDisplayOrientation(90)
                startPreview()
            } catch (e: Exception) {
                Timber.d("Error setting camera preview: ${e.message}")
            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) = Unit

    override fun surfaceDestroyed(holder: SurfaceHolder) { stopCamera() }

    fun takePicture(action: (ByteArray) -> Unit)  {
        Timber.tag(TIMBER_TAG).d("outer lambda thread: ${Thread.currentThread().name}")
        camera?.takePicture(null, null) { bytes, _ ->
            Timber.tag(TIMBER_TAG).d("inner lambda thread: ${Thread.currentThread().name}")
            action(bytes)
        }
    }

    fun prepareCamera(): Camera.Size? {
        camera = try {
            Camera.open()
        } catch (e: RuntimeException) {
            Timber.d(e)
            return null
        }

        val parameters = camera?.parameters

        if (parameters != null && parameters.supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
        }

        val targetSizeWithRatio = camera?.parameters?.supportedPreviewSizes?.filter { supportedSize ->
            targetResolutions.contains(Pair(supportedSize.width, supportedSize.height))
        }?.maxByOrNull { it.width }

        val targetSizeByWidth = camera?.parameters?.supportedPreviewSizes?.filter { supportedSize ->
            targetResolutions.firstOrNull { targetSize ->
                supportedSize.width == targetSize.first
            } != null
        }?.maxByOrNull { it.width }

        val size = targetSizeWithRatio ?: targetSizeByWidth

        if (size != null) {
            parameters?.setPictureSize(size.width, size.height)
            parameters?.setPreviewSize(size.width, size.height)
        }

        camera?.parameters = parameters

        return size
    }

    fun stopCamera() = camera?.apply {
        Timber.tag(TIMBER_TAG).d("camera starts releasing")
        stopPreview()
        release()
        camera = null
        Timber.tag(TIMBER_TAG).d("camera released")
    }
}
