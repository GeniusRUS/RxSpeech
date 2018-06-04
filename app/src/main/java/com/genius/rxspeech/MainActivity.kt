package com.genius.rxspeech

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.genius.speech.RxSpeech
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun startDetect(v: View) {
        launch(UI) {
            if (RxSpeech.checkAnalyzerAvailable(this@MainActivity)) {
                val result = RxSpeech.with(this@MainActivity, "Custom title", locale = Locale("ru")).requestText().await()

                tv_result.text = result.toString()
            }
        }
    }
}
