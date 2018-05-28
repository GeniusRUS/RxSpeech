package com.genius.speech

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.annotation.IntRange
import android.support.annotation.StringRes
import android.support.v4.app.BundleCompat
import kotlinx.coroutines.experimental.CompletableDeferred
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList

class RxSpeech private constructor(context: Context, prompt: String?, @StringRes promptRes: Int?, locale: Locale?, @IntRange(from = 0) range: Int?) {

    private var contextReference: WeakReference<Context> = WeakReference(context)
    private lateinit var emitter: CompletableDeferred<ArrayList<String>>
    private var prompt: String? = null
    private var maxResults = 3
    private var locale: Locale? = null

    init {
        prompt?.let {
            this.prompt = it
        }

        promptRes?.let {
            this.prompt = contextReference.get()?.getString(it)
        }

        this.locale = locale?.let { it } ?: Locale.getDefault()

        range?.let {
            this.maxResults = it
        }
    }

    suspend fun requestText(): ArrayList<String> {
        emitter = CompletableDeferred()

        contextReference.get()?.let {
            it.startActivity(OverlapView.newInstance(it, this))
        } ?: emitter.completeExceptionally(ContextNullException("Received context == null. Sorry"))
        return emitter.await()
    }

    private fun handleResult(text: ArrayList<String>) {
        emitter.complete(text)
    }

    companion object {

        @JvmStatic
        fun with(context: Context, prompt: String? = null, locale: Locale? = null, @IntRange(from = 0) range: Int? = null): RxSpeech {
            return RxSpeech(context, prompt, null, locale, range)
        }

        @JvmStatic
        fun with(context: Context, @StringRes promptRes: Int? = null, locale: Locale? = null, @IntRange(from = 0) range: Int? = null): RxSpeech {
            return RxSpeech(context, null, promptRes, locale, range)
        }

        @JvmStatic
        fun checkAnalyzerAvailable(context: Context): Boolean {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            return intent.resolveActivity(context.packageManager) != null
        }
    }

    private class ContextNullException(reason: String): Exception(reason)

    private class AnalyzerNotFound(reason: String): Exception(reason)

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
                        speech.handleResult(result)
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
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, speech.locale.toString())
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, speech.maxResults)

            //TODO сделать возможность класть [RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT & RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT_BUNDLE]

            speech.prompt?.let {
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, it)
            }
            if (intent.resolveActivity(this.packageManager) != null) {
                startActivityForResult(intent, REQ_CODE_SPEECH_INPUT)
            } else {
                speech.emitter.completeExceptionally(AnalyzerNotFound("Voice recognition app not found"))
            }
        }

        private class RxSpeechBinder constructor(internal val rxSpeech: RxSpeech) : Binder()
    }
}