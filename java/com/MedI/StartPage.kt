package com.MedI

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity

class StartPage: AppCompatActivity() {

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val action = event!!.actionMasked
        return when (action) {
            MotionEvent.ACTION_DOWN -> {
                true
            }

            MotionEvent.ACTION_UP -> {
                val intent = Intent(this, SpeechRecognition::class.java)
                startActivity(intent)

                true
            }

            else -> super.onTouchEvent(event)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_page)
    }

}
