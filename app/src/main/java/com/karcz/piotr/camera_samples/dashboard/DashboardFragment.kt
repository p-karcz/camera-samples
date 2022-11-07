package com.karcz.piotr.camera_samples.dashboard

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.karcz.piotr.camera_samples.R
import com.karcz.piotr.camera_samples.StorageFacade
import com.karcz.piotr.camera_samples.camera.CameraFragment
import com.karcz.piotr.camera_samples.camerarx.CameraRxFragment
import com.karcz.piotr.camera_samples.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentDashboardBinding
        .inflate(inflater, container, false)
        .also { _binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpImage()
        setUpListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpImage() {
        val file = StorageFacade.getPicture(requireContext().cacheDir)
        if (file.exists()) binding.imageViewTakenPhoto.setImageBitmap(BitmapFactory.decodeFile(file.path))
    }

    private fun setUpListeners() {
        binding.buttonMainThread.setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container_view, CameraFragment.newInstance())
                .addToBackStack(CameraFragment.TAG)
                .commit()
        }
        binding.buttonCameraRxClearLast.setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container_view, CameraRxFragment.newInstance(shouldClearLastDisposable = true))
                .addToBackStack(CameraRxFragment.TAG_CLEAR)
                .commit()
        }
        binding.buttonCameraRxNoClearLast.setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container_view, CameraRxFragment.newInstance(shouldClearLastDisposable = false))
                .addToBackStack(CameraRxFragment.TAG_NO_CLEAR)
                .commit()
        }
    }

    companion object {
        fun newInstance() = DashboardFragment()
    }
}
