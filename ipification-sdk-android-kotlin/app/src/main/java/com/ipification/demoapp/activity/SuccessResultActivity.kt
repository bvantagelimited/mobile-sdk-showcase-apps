package com.ipification.demoapp.activity

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.ipification.demoapp.data.TokenInfo
import com.ipification.demoapp.databinding.ActivitySuccessResultBinding

class SuccessResultActivity : AppCompatActivity() {
    lateinit var binding: ActivitySuccessResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuccessResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setup()
    }

    private fun setup(){
        val tokenInfo = intent.getParcelableExtra<TokenInfo>("tokenInfo")
        Log.d("tokenInfo", ""+ tokenInfo?.phoneNumber)
        val phoneNumber = tokenInfo?.phoneNumber
        val phoneNumberVerified = tokenInfo?.phoneNumberVerified
        val sub = if(tokenInfo?.sub != null ) " | sub: ${tokenInfo.sub}" else ""
        val mobileID = if(tokenInfo?.mobileID != null ) " | mobileID: ${tokenInfo.mobileID}" else ""
//
        val loginHint = if(tokenInfo?.loginHint != null ) " | Phone Number: ${tokenInfo.loginHint}" else ""
        var result = "Phone Number Verified: $phoneNumberVerified $loginHint"
        if(!phoneNumber.isNullOrEmpty() ){
            result = "Phone Number: $phoneNumber"
        }


        binding.tvMainDetail.text = result
        binding.detail.text = "$result $sub $mobileID"
//        binding.btnSendLog.setOnClickListener{
//            back()
//        }
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