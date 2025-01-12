package com.MedI

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.TextView
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import android.speech.tts.TextToSpeech
import java.util.Locale
import android.view.Gravity


class SpeechRecognition: AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var speechRecognizer: SpeechRecognizer
    private var isFirstTouch = true
    private var errorOccurred = false
// 음성인식
    private val recognitionListener: RecognitionListener = object : RecognitionListener {
        // 말하기 시작할 준비가되면 호출
        override fun onReadyForSpeech(params: Bundle) {
            Toast.makeText(applicationContext, "Start Recording", Toast.LENGTH_SHORT).show()
        }
        // 말하기 시작했을 때 호출
        override fun onBeginningOfSpeech() {
            val guide = findViewById<TextView>(R.id.guide)
            guide.gravity = Gravity.CENTER
            guide.text = "Listening"
        }
        // 입력받는 소리의 크기를 알려줌
        override fun onRmsChanged(rmsdB: Float) {}
        // 말을 시작하고 인식이 된 단어를 buffer에 담음
        override fun onBufferReceived(buffer: ByteArray) {}
        // 말하기를 중지하면 호출
        override fun onEndOfSpeech() {
            val guide = findViewById<TextView>(R.id.guide)
            guide.text = "Converting"
            speakOut("Give us a Moment")
        }
        // 오류 발생했을 때 호출
        override fun onError(error: Int) {
            errorOccurred = true
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio Error"
                SpeechRecognizer.ERROR_CLIENT -> "Client Error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "No permision"
                SpeechRecognizer.ERROR_NETWORK -> "Network Error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network Timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No Match"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RECOGNIZER Busy"
                SpeechRecognizer.ERROR_SERVER -> "Server Error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech Timeout"
                else -> "Unrecognized Error"
            }
            val guide = findViewById<TextView>(R.id.guide)
            guide.text = "Error: \n $message"
            speakOut("Error occurred ${message} \n touch for retry")
        }
        // 인식 결과가 준비되면 호출
        override fun onResults(results: Bundle) {
            // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어줌
            val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            for (i in matches!!.indices) {
                val guide = findViewById<TextView>(R.id.guide)
                guide.gravity = Gravity.CENTER
                guide.text = matches[i]
            }
            val output = findViewById<TextView>(R.id.guide).text.toString()
            val guide2 = findViewById<TextView>(R.id.guide2)
            guide2.text = "Touch to Proceed"
            speakOut("Touch to Proceed")
        }
        // 부분 인식 결과를 사용할 수 있을 때 호출
        override fun onPartialResults(partialResults: Bundle) {}
        // 향후 이벤트를 추가하기 위해 예약
        override fun onEvent(eventType: Int, params: Bundle) {}
    }

// TTS
    private lateinit var tts: TextToSpeech

    override fun onInit(status: Int)
    {
        if (status == TextToSpeech.SUCCESS) {
            // 언어 설정 (한국어로 설정)
            val result = tts.setLanguage(Locale.ENGLISH)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "TTS: ENGLISH not available", Toast.LENGTH_SHORT).show()
            } else {
                // TTS 준비 완료
                speakOut("Tell us your medication. \n Touch to Record")
            }
        } else {
            Toast.makeText(this, "TTS Initialization failed", Toast.LENGTH_SHORT).show()
        }
    }

    // TTS로 문자열을 읽어주는 메서드
    private fun speakOut(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onDestroy() {
        // TextToSpeech 리소스 해제
        if (this::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }


// Touch Event
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val action = event!!.actionMasked
        return when (action) {
            MotionEvent.ACTION_DOWN -> {
                true
            }
            MotionEvent.ACTION_UP -> {
                if (isFirstTouch){
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this@SpeechRecognition)
                    speechRecognizer.setRecognitionListener(recognitionListener)    // 리스너 설정
                    speechRecognizer.startListening(intent)
                    isFirstTouch=false
                } else{
                    if (errorOccurred){
                        speechRecognizer.startListening(intent)
                        errorOccurred=false
                    } else {
                        Handler(Looper.getMainLooper()).postDelayed({
                            val intent2 = Intent(this, MainActivity::class.java)
                            startActivity(intent2)
                        }, 500) // 500ms = 0.5초 후 화면 전환
                        isFirstTouch = true // 다시 상태를 초기화
                    }
                }
                true
            }

            else -> super.onTouchEvent(event)
        }

    }

    private fun requestPermission() {
        // 버전 체크, 권한 허용했는지 체크
        if (Build.VERSION.SDK_INT >= 23 &&
            ContextCompat.checkSelfPermission(this@SpeechRecognition, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this@SpeechRecognition,
                arrayOf(Manifest.permission.RECORD_AUDIO), 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speech)

        requestPermission()

        tts = TextToSpeech(this,this)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)    // 여분의 키
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000)
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1500)
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500)

    }

}
