package com.ipification.demoapp.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.ipification.demoapp.databinding.ActivityFailResultBinding

class ResultFailActivity : AppCompatActivity() {
    lateinit var binding: ActivityFailResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFailResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setup()
    }

    private fun setup() {
        val errorMessage = intent.getStringExtra("error")
        binding.tvMainDetail.text = errorMessage


        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "Result"
        actionbar.setDisplayHomeAsUpEnabled(true)
    }

    private fun back() {
        onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // API 5+ solution
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}