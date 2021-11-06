package kon4.sam.guessauto.ui

import android.app.Dialog
import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.endgame_dialog.*
import android.os.Bundle
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.LifecycleOwner
import kon4.sam.guessauto.App
import kon4.sam.guessauto.R
import kon4.sam.guessauto.view_model.GameViewModel

import java.util.*

class EndGameDialog(context: Context,
                    private val viewModel: GameViewModel,
                    private val lifecycleOwner: LifecycleOwner): Dialog(context) {

    private lateinit var userName: String
    private val resources = context.resources

    private val sendSaveUserName = View.OnClickListener {
        userName = userNameEditText.text.toString()
        if (userName.isEmpty()) {
            userNameEditTextLayout.error = "Enter your nickname"
            return@OnClickListener
        }
        if (userNameEditTextLayout.error.isNullOrEmpty()) {
            if (userName != App.user.user_name) {
                it.isEnabled = false
                viewModel.initiateUser(userName)
            }
        }
    }

    private val showAdAndClose = View.OnClickListener {
        viewModel.showAd()
        this.dismiss()
    }

    private fun setUserNameFields() {
        changeUserNameText.visibility = View.VISIBLE
        userNameEditTextLayout.visibility = View.VISIBLE
        userNameEditText.doOnTextChanged { text, _, _, _ ->
            if (text!!.length < 4) {
                userNameEditTextLayout.error = resources.getString(R.string.username_length_short_error)
            } else if (text.length >= 4) {
                userNameEditTextLayout.error = null
            }
        }
    }

    fun setupFields() {
        if (App.user.user_name.isEmpty()) {
            setUserNameFields()
            gameOverOkButton.setOnClickListener(sendSaveUserName)
        } else {
            if (viewModel.score > App.user.score) {
                viewModel.sendUserScore()
            }
            gameOverOkButton.setOnClickListener(showAdAndClose)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.userInitiated.observe(lifecycleOwner, {
            it.getContentIfNotHandled()?.let { message ->
                gameOverOkButton.isEnabled = true
                when(message) {
                    "OK" -> {
                        viewModel.sendUserScore()
                        viewModel.showAd()
                    }
                    "User exist" -> {
                        userNameEditTextLayout.error = resources.getString(R.string.user_already_exists)
                    }
                    else -> {
                        userNameEditTextLayout.error = resources.getString(R.string.unknown_error, message)
                    }
                }
            }
        })
    }

    fun setCongratsText(text: String) {
        gameOverTextFull.text = text
    }
}