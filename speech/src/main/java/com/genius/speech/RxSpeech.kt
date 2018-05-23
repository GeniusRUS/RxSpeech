package com.genius.speech

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.annotation.IntRange
import android.support.v4.app.BundleCompat
import io.reactivex.Observable
import java.lang.ref.WeakReference
import io.reactivex.subjects.PublishSubject
import java.util.*

class RxSpeech private constructor(context: Context) {

    private var contextReference: WeakReference<Context> = WeakReference(context)
    private lateinit var emitter: PublishSubject<ArrayList<String>>
    private var prompt: String? = null
    private var maxResults = 3
    private var locale: Locale? = null
        get() { return field ?: Locale.getDefault() }

    fun setPrompt(message: String): RxSpeech {
        this.prompt = message

        return this
    }

    fun setLocale(locale: Locale): RxSpeech {
        this.locale = locale

        return this
    }

    fun setMaxResults(@IntRange(from = 0) range: Int): RxSpeech {
        this.maxResults = range

        return this
    }

    fun requestText(): Observable<ArrayList<String>> {
        emitter = PublishSubject.create()

        contextReference.get()?.let {
            it.startActivity(OverlapView.newInstance(it, this))
        } ?: emitter.onError(ContextNullException("Received context == null. Sorry"))
        return emitter
    }

    private fun onActivityResult(text: ArrayList<String>) {
        emitter.onNext(text)
        emitter.onComplete()
    }

    companion object {

        @JvmStatic
        fun with(context: Context): RxSpeech {
            return RxSpeech(context)
        }
    }

    private class ContextNullException(reason: String): Exception(reason)

    class OverlapView: Activity() {

        private lateinit var speech: RxSpeech

        companion object {

            private const val REQ_CODE_SPEECH_INPUT = 101
            private const val BUNDLE = "bundle"
            const val CALLER_EXTRA = "caller_extra"

            internal fun newInstance(context: Context, speech: RxSpeech): Intent {
                val intent = Intent(context, OverlapView::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val bundle = Bundle()
                BundleCompat.putBinder(bundle, CALLER_EXTRA, RxSpeechBinder(speech))
                intent.putExtra(CALLER_EXTRA, bundle)
                return intent
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            if (savedInstanceState == null) {
                handleIntent(intent)
            }
        }

        override fun onNewIntent(intent: Intent) {
            handleIntent(intent)
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)

            when (requestCode) {
                REQ_CODE_SPEECH_INPUT -> {
                    if (resultCode == RESULT_OK && null != data) {

                        val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        speech.onActivityResult(result)
                    }
                }
            }

            finish()
        }

        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            outState.putParcelable(BUNDLE, intent.extras)
        }

        override fun onRestoreInstanceState(savedInstanceState: Bundle) {
            super.onRestoreInstanceState(savedInstanceState)

            val bundle = savedInstanceState.getParcelable(BUNDLE) as Bundle
            val caller = bundle.getBundle(CALLER_EXTRA)
            if (caller != null) {
                val iBinder = BundleCompat.getBinder(caller, CALLER_EXTRA)
                if (iBinder is RxSpeechBinder) {
                    speech = iBinder.rxSpeech
                }
            }
        }

        private fun handleIntent(intent: Intent) {
            val bundle = intent.extras.getBundle(CALLER_EXTRA)
            if (bundle != null) {
                val iBinder = BundleCompat.getBinder(bundle, CALLER_EXTRA)
                if (iBinder is RxSpeechBinder) {
                    speech = iBinder.rxSpeech
                }
            }

            startListening()
        }

        private fun startListening() {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, speech.locale)
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, speech.maxResults)

            //TODO сделать возможность класть [RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT & RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT_BUNDLE]

            speech.prompt?.let {
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, it)
            }
            if (intent.resolveActivity(this.packageManager) != null) {
                startActivityForResult(intent, REQ_CODE_SPEECH_INPUT)
            }
        }

        private class RxSpeechBinder constructor(internal val rxSpeech: RxSpeech) : Binder()
    }
}