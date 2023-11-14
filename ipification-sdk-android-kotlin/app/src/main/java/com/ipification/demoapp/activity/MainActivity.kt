package com.ipification.demoapp.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ipification.demoapp.activity.im.IMAuthActivity
import com.ipification.demoapp.databinding.ActivityMainBinding
class MainActivity : AppCompatActivity() {
    private val TAG: String = "MainActivity"
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        binding.ipButton.setOnClickListener { openActivity(IPificationAuthActivity::class.java) }
        binding.imButton.setOnClickListener { openActivity(IMAuthActivity::class.java) }
        // Uncomment the line below to open a specific IM activity
        // binding.imButton.setOnClickListener { openActivity(IMAuthAutoModeActivity::class.java) }
        // binding.imButton.setOnClickListener { openActivity(IMAuthManualActivity::class.java) }
    }

    private fun openActivity(activityClass: Class<*>) {
        val intent = Intent(applicationContext, activityClass)
        startActivity(intent)
    }
}


