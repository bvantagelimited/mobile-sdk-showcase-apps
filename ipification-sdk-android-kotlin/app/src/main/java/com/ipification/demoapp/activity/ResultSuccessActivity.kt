package com.ipification.demoapp.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.ipification.demoapp.databinding.ActivitySuccessResultBinding
import org.json.JSONObject

class ResultSuccessActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySuccessResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuccessResultBinding.inflate(layoutInflater)
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
        val tokenInfo = intent.getStringExtra("responseStr")
        val jsonObject = JSONObject(tokenInfo ?: "")
        binding.tvMainDetail.text = jsonObject.toString(4) // 4 is the number of spaces for indentation
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