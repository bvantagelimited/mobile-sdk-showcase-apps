package com.ipification.demoapp.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ipification.demoapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val TAG: String = "MainActivity"

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ipButton.setOnClickListener {
            openIPActivity()
        }
        binding.imButton.setOnClickListener {
            openIMActivity()
        }

    }


    private fun openIPActivity() {
        val intent = Intent(applicationContext, IPificationAuthActivity::class.java)
        startActivity(intent)
    }

    private fun openIMActivity() {
        val intent = Intent(applicationContext, IMAuthActivity::class.java)
//        val intent = Intent(applicationContext, IMAuthAutoModeActivity::class.java)
//        val intent = Intent(applicationContext, IMAuthManualActivity::class.java)
        startActivity(intent)
    }

}

