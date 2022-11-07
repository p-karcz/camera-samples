package com.karcz.piotr.camera_samples

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.karcz.piotr.camera_samples.dashboard.DashboardFragment
import com.karcz.piotr.camera_samples.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container_view, DashboardFragment.newInstance())
            .commit()
    }
}
