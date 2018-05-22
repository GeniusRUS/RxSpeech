package com.genius.rxspeech

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.genius.speech.RxSpeech
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun startDetect(v: View) {
        RxSpeech.with(this)
            .setPrompt("Custom title")
            .setLocale(Locale("ru"))
            .requestText()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe( { tv_result.text = it[0] },
        { it.printStackTrace() } )
    }
}
