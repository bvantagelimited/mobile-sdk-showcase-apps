package com.ipification.demoapp.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.ipification.demoapp.databinding.ActivityFailResultBinding
import org.json.JSONObject

class ResultFailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFailResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFailResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBar()
        setupContent()
    }

    private fun setupActionBar() {
        supportActionBar?.apply {
            title = "Result"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupContent() {
        val errorMessage = intent.getStringExtra("error")
        try {
            val jsonObject = JSONObject(errorMessage ?: "")
            binding.tvMainDetail.text = jsonObject.toString(4) // 4 is the number of spaces for indentation
        }catch (e: Exception)
        {
            binding.tvMainDetail.text = errorMessage
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}