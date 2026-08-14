package com.example.service

import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.data.AppDatabase
import com.example.data.KeyboardPreferences
import com.example.ui.keyboard.KeyboardActionListener
import com.example.ui.keyboard.KeyboardView
import com.example.ui.keyboard.PredictiveEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FeministInputMethodService : InputMethodService(),
    LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val job = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + job)

    private lateinit var preferences: KeyboardPreferences
    private lateinit var database: AppDatabase
    private lateinit var predictiveEngine: PredictiveEngine

    private val currentTypedWord = mutableStateOf("")
    private val predictionsState = mutableStateOf<List<String>>(emptyList())

    // Compose Service Lifecycle Boilerplate
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        try {
            savedStateController.performRestore(null)
        } catch (_: Exception) {}
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        preferences = KeyboardPreferences.getInstance(this)
        database = AppDatabase.getInstance(this)
        predictiveEngine = PredictiveEngine(database.keyboardDao())
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
        currentTypedWord.value = ""
        predictionsState.value = emptyList()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        currentTypedWord.value = ""
        predictionsState.value = emptyList()
        if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        }
        super.onFinishInputView(finishingInput)
    }

    override fun onCreateInputView(): View {
        if (!lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }

        return ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@FeministInputMethodService))
            setViewTreeLifecycleOwner(this@FeministInputMethodService)
            setViewTreeViewModelStoreOwner(this@FeministInputMethodService)
            setViewTreeSavedStateRegistryOwner(this@FeministInputMethodService)

            setContent {
                val settings by preferences.settings.collectAsState()
                val typedWord by currentTypedWord
                val predictions by predictionsState

                KeyboardView(
                    settings = settings,
                    currentTypedText = typedWord,
                    predictiveSuggestions = predictions,
                    onSuggestionSelected = { suggestion ->
                        replaceCurrentWord(suggestion)
                    },
                    listener = object : KeyboardActionListener {
                        override fun onKeyText(text: String) {
                            try {
                                currentInputConnection?.commitText(text, 1)
                            } catch (e: Exception) {
                                Log.e("FeministIME", "Error committing text", e)
                            }
                            updateTypedWord(text)
                        }

                        override fun onDelete() {
                            try {
                                currentInputConnection?.deleteSurroundingText(1, 0)
                            } catch (e: Exception) {
                                Log.e("FeministIME", "Error deleting text", e)
                            }
                            if (currentTypedWord.value.isNotEmpty()) {
                                currentTypedWord.value = currentTypedWord.value.dropLast(1)
                                fetchPredictions(currentTypedWord.value)
                            }
                        }

                        override fun onSpace() {
                            try {
                                val current = currentTypedWord.value
                                val autocorrect = predictiveEngine.getAutocorrectWord(current)
                                if (autocorrect != null) {
                                    replaceCurrentWord(autocorrect)
                                } else {
                                    currentInputConnection?.commitText(" ", 1)
                                }
                            } catch (e: Exception) {
                                Log.e("FeministIME", "Error on space", e)
                            }
                            currentTypedWord.value = ""
                            predictionsState.value = emptyList()
                        }

                        override fun onEnter() {
                            try {
                                currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                                currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                            } catch (e: Exception) {
                                Log.e("FeministIME", "Error on enter", e)
                            }
                            currentTypedWord.value = ""
                            predictionsState.value = emptyList()
                        }

                        override fun onCursorMove(direction: Int) {
                            val ic = currentInputConnection ?: return
                            try {
                                when (direction) {
                                    -1 -> ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT))
                                    1 -> ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT))
                                    -10 -> ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP))
                                    10 -> ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN))
                                }
                            } catch (e: Exception) {
                                Log.e("FeministIME", "Error moving cursor", e)
                            }
                        }

                        override fun onShortcutTrigger(expansion: String) {
                            try {
                                val len = currentTypedWord.value.length
                                if (len > 0) {
                                    currentInputConnection?.deleteSurroundingText(len, 0)
                                }
                                currentInputConnection?.commitText("$expansion ", 1)
                            } catch (e: Exception) {
                                Log.e("FeministIME", "Error triggering shortcut", e)
                            }
                            currentTypedWord.value = ""
                            predictionsState.value = emptyList()
                        }
                    }
                )
            }
        }
    }

    private fun updateTypedWord(newChar: String) {
        if (newChar.length == 1 && (newChar[0].isLetterOrDigit() || newChar[0] == '·')) {
            currentTypedWord.value += newChar
            fetchPredictions(currentTypedWord.value)
        } else {
            currentTypedWord.value = ""
            predictionsState.value = emptyList()
        }
    }

    private fun fetchPredictions(prefix: String) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                val suggestions = predictiveEngine.getSuggestions(prefix)
                withContext(Dispatchers.Main) {
                    predictionsState.value = suggestions
                }
            } catch (e: Exception) {
                Log.e("FeministIME", "Error fetching predictions", e)
            }
        }
    }

    private fun replaceCurrentWord(replacement: String) {
        try {
            val ic = currentInputConnection ?: return
            val wordLen = currentTypedWord.value.length
            if (wordLen > 0) {
                ic.deleteSurroundingText(wordLen, 0)
            }
            ic.commitText("$replacement ", 1)
        } catch (e: Exception) {
            Log.e("FeministIME", "Error replacing word", e)
        }
        currentTypedWord.value = ""
        predictionsState.value = emptyList()
    }

    override fun onDestroy() {
        try {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            job.cancel()
        } catch (_: Exception) {}
        super.onDestroy()
    }
}
