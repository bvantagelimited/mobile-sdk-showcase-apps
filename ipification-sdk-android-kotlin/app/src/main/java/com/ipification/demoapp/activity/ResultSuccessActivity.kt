package com.ipification.demoapp.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.ipification.demoapp.databinding.ActivitySuccessResultBinding
import org.json.JSONObject

class ResultSuccessActivity : AppCompatActivity() {
    lateinit var binding: ActivitySuccessResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuccessResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setup()
    }

    private fun setup(){
        val tokenInfo = intent.getStringExtra("responseStr")
        val jsonObject = JSONObject(tokenInfo ?: "")
        binding.tvMainDetail.text = (jsonObject.toString(4)) // 4 is number of spaces for indent
        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "Result"
        actionbar.setDisplayHomeAsUpEnabled(true)
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