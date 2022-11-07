package com.karcz.piotr.camera_samples.camerarx

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.karcz.piotr.camera_samples.R
import com.karcz.piotr.camera_samples.StorageFacade
import com.karcz.piotr.camera_samples.databinding.FragmentCameraRxBinding
import com.karcz.piotr.camera_samples.permission.PermissionFragment
import timber.log.Timber

class CameraRxFragment : Fragment() {

    private var _binding: FragmentCameraRxBinding? = null
    private val binding get() = _binding!!

    private val cameraFacade by lazy {
        CameraRxFacade(
            listOf(
                Pair(1920, 1080),
                Pair(1280, 720),
                Pair(720, 480)
            )
        ).apply {
            shouldClearLastDisposable = arguments?.getBoolean(ARG_PARAM_CLEAR_LAST_DISPOSABLE) ?: false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentCameraRxBinding
        .inflate(inflater, container, false)
        .also { _binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpListeners()
        setUpCamera()
    }

    override fun onStop() {
        cameraFacade.stopCamera()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @Suppress("DEPRECATION")
    private fun setUpCamera() {
        if (isCameraPermissionGranted()) {
            binding.surfaceView.holder.addCallback(cameraFacade)
            cameraFacade.prepareCamera { cameraSize ->
                if (cameraSize != null) {
                    val cameraSizeRatio = cameraSize.width.div(cameraSize.height.toDouble())
                    val screenSizeRatio = Resources.getSystem().displayMetrics.run {
                        heightPixels.div(widthPixels.toDouble())
                    }

                    val layoutParams =
                        binding.surfaceView.layoutParams as ConstraintLayout.LayoutParams
                    layoutParams.dimensionRatio = String.format("%d:%d", cameraSize.height, cameraSize.width)

                    if (cameraSizeRatio < screenSizeRatio) {
                        layoutParams.height = ConstraintLayout.LayoutParams.MATCH_PARENT
                        layoutParams.width = 0
                    } else {
                        layoutParams.height = 0
                        layoutParams.width = ConstraintLayout.LayoutParams.MATCH_PARENT
                    }

                    binding.surfaceView.layoutParams = layoutParams
                }
            }
        } else {
            parentFragmentManager.also { it.popBackStack() }
                .beginTransaction()
                .replace(R.id.fragment_container_view, PermissionFragment.newInstance())
                .addToBackStack(PermissionFragment.TAG)
                .commit()
        }
    }

    private fun isCameraPermissionGranted() = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun setUpListeners() {
        binding.buttonTakePhoto.setOnClickListener {
            cameraFacade.takePicture { bytes ->
                context?.let { StorageFacade.createPictureFile(it.cacheDir, bytes) }
                try {
                    parentFragmentManager.popBackStack()
                } catch (e: IllegalStateException) {
                    Timber.e(e)
                }
            }
        }
    }

    companion object {
        fun newInstance(shouldClearLastDisposable: Boolean) =
            CameraRxFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_PARAM_CLEAR_LAST_DISPOSABLE, shouldClearLastDisposable)
                }
            }

        const val TAG_CLEAR = "CAMERA_RX_CLEAR"
        const val TAG_NO_CLEAR = "CAMERA_RX_NO_CLEAR"

        private const val ARG_PARAM_CLEAR_LAST_DISPOSABLE = "shouldClearLastDisposable"
    }
}
