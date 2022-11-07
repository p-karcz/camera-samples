@file:Suppress("DEPRECATION")

package com.karcz.piotr.camera_samples.camerarx

import android.hardware.Camera
import android.view.SurfaceHolder
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

private const val TIMBER_TAG = "CameraRxFacade"

class CameraRxFacade(private val targetResolutions: List<Pair<Int, Int>>) : SurfaceHolder.Callback {

    var shouldClearLastDisposable = false
    private val cameraCompositeDisposable = CompositeDisposable()
    private var camera: Camera? = null

    override fun surfaceCreated(holder: SurfaceHolder) = executeAction("surfaceCreated disposed") {
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

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        if (shouldClearLastDisposable) {
            executeAction("surfaceDestroyed disposed") {
                releaseCamera()
            }
        } else {
            stopPreviewAndFreeCamera()
        }
    }

    fun takePicture(action: (ByteArray) -> Unit) = executeAction("takePicture disposed") {
        Timber.tag(TIMBER_TAG).d("outer lambda thread: ${Thread.currentThread().name}")
        camera?.takePicture(null, null) { bytes, _ ->
            Timber.tag(TIMBER_TAG).d("inner lambda thread: ${Thread.currentThread().name}")
            action(bytes)
        }
    }

    fun prepareCamera(onCompletionAction: (Camera.Size?) -> Unit) =
        executeCallable("prepare camera disposed", onCompletionAction) {
            camera = try {
                Camera.open()
            } catch (e: RuntimeException) {
                Timber.d(e)
                return@executeCallable null
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

            return@executeCallable size
        }

    fun stopCamera() {
        if (shouldClearLastDisposable) {
            executeAction("stopCamera disposed") {
                releaseCamera()
            }
        } else {
            stopPreviewAndFreeCamera()
        }
        cameraCompositeDisposable.clear()
    }

    private fun releaseCamera() = camera?.apply {
        Timber.tag(TIMBER_TAG).d("camera starts releasing")
        stopPreview()
        release()
        camera = null
        Timber.tag(TIMBER_TAG).d("camera released")
    }

    private fun stopPreviewAndFreeCamera() {
        Completable
            .fromAction {
                releaseCamera()
            }
            .subscribeOn(Schedulers.single())
            .doOnDispose {
                Timber.tag(TIMBER_TAG).d("stopPreviewAndFreeCamera disposed")
            }
            .subscribe()
    }

    private fun executeAction(msg: String? = null, action: () -> Unit) {
        cameraCompositeDisposable.add(
            Completable
                .fromAction(action)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnDispose { Timber.tag(TIMBER_TAG).d(msg) }
                .subscribe({ }, { Timber.tag(TIMBER_TAG).e(it) })
        )
    }

    private fun <T> executeCallable(msg: String? = null, onCompletionAction: (T) -> Unit, callable: () -> T) {
        cameraCompositeDisposable.add(
            Single
                .fromCallable(callable)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnDispose { Timber.tag(TIMBER_TAG).d(msg) }
                .subscribe(onCompletionAction) { Timber.tag(TIMBER_TAG).e(it) }
        )
    }
}
