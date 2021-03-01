package com.ipification.demoapp

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        val phone_number_verified = intent.getBooleanExtra("phone_number_verified", false)

        val text1 = findViewById<View>(R.id.record1) as TextView
        text1.text = "phone_number_verified : ${phone_number_verified}"

        val sub = intent.getStringExtra("sub")
        val text2 = findViewById<View>(R.id.record2) as TextView
        text2.text = "sub : ${sub}"

        val mobileID = intent.getStringExtra("mobileID")
        val text3 = findViewById<View>(R.id.record3) as TextView
        text3.text = "mobileID : ${mobileID}"

        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "Result"
        actionbar.setDisplayHomeAsUpEnabled(true)

    }

    fun back(view: View) {
        val myIntent = Intent(this, MainActivity::class.java)
        startActivity(myIntent)
        finish()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {
            android.R.id.home -> {
                // API 5+ solution
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}