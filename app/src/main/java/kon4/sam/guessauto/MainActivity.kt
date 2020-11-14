package kon4.sam.guessauto

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.AlertDialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kon4.sam.guessauto.data.DBHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var hearts: Array<ImageView> = arrayOf()
    private lateinit var dbHelper: DBHelper
    private lateinit var timer: CountDownTimer
    private lateinit var allCarImages: MutableList<String>
    private var currentPhotoDrawableIdentifier = 0
    private var currentPhotoIndex = 0
    private var score = 0
    private var attempt = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        dbHelper = DBHelper(this)
        allCarImages = dbHelper.getAllAuto()
        allCarImages.shuffle()

        setRandomImage()
        setButtonsText()

        hearts = arrayOf(heart1, heart2, heart3)
        timer = object : CountDownTimer(15000, 50) {
            override fun onTick(millisUntilFinished: Long) {
                progressBar.progress = millisUntilFinished.toInt() / 150
            }

            override fun onFinish() {
                loseAttempt()
            }
        }
        timer.start()
    }


    fun pressButton(view: View) {
        view as Button
        setButtonsEnabled(false)
        timer.cancel()
        if (currentPhotoIndex == 50) {
            saveResultToPrefs()
            showFinalDialog(R.string.title, R.string.alert, R.drawable.starwin)
        }

        val correctAutoName = dbHelper.getAutoNameById(
                resources.getResourceEntryName(
                        currentPhotoDrawableIdentifier
                )
        )
        val chosenAutoButtonText = view.text.toString()
        if (chosenAutoButtonText == correctAutoName) {
            onSuccessAnswer(view)
            setNewCard()
        } else {
            onWrongAnswer(view)
            loseAttempt()
        }
    }

    private fun onSuccessAnswer(view: View) {
        playSound(R.raw.success)
        score += progressBar.progress
        val scoreCaption = resources.getString(R.string.score) + " " + score
        scoreText.text = scoreCaption
        view.setBackgroundColor(ContextCompat.getColor(this, R.color.colorGreen))
    }

    private fun onWrongAnswer(view: View) {
        playSound(R.raw.fail)
        view.setBackgroundColor(ContextCompat.getColor(this, R.color.colorRed))
    }

    private fun isLastAttempt(): Boolean {
        return attempt == 3
    }

    private fun loseAttempt() {
        attempt++
        hearts[attempt - 1].setImageResource(R.drawable.heart)
        if (isLastAttempt()) {
            onLostFinalAttempt()
        } else {
            setNewCard()
        }
    }

    private fun onLostFinalAttempt() {
        if (score > getCurrentResultFromPrefs()) {
            saveResultToPrefs()
            showFinalDialog(R.string.title, R.string.alert, R.drawable.starwin)

        } else {
            showFinalDialog(R.string.title2, R.string.alert2, R.drawable.star)
        }
        attempt = 0
    }

    private fun showFinalDialog(titleId: Int, messageId: Int, iconId: Int) {
        val builder = AlertDialog.Builder(this)
        with(builder)
        {
            setTitle(titleId)
            setIcon(iconId)
            setMessage(resources.getString(messageId) + " " + score)
            setPositiveButton("ОК") { _, _ ->
                onBackPressed()
            }
            setCancelable(false)
        }
        builder.show()
    }

    private fun setNewCard() {
        carImage.animate().alpha(0f)
            .setDuration(PHOTO_APPEARING_TIME)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    carImage.alpha = 1f
                    setRandomImage()
                    setButtonsText()
                    setButtonsDefaultBackgorund()
                    setButtonsEnabled(true)
                    timer.start()
                }
            })
    }

    private fun setRandomImage() {
        currentPhotoDrawableIdentifier = resources.getIdentifier(
            allCarImages[currentPhotoIndex],
            "drawable",
            applicationContext.packageName
        )
        carImage.setImageResource(currentPhotoDrawableIdentifier)
        currentPhotoIndex++
    }

    private fun setButtonsText() {
        val buttons = dbHelper.getAllCaptionsForAuto(
            resources.getResourceEntryName(
                currentPhotoDrawableIdentifier
            )
        )
        button1.text = buttons[0]
        button2.text = buttons[1]
        button3.text = buttons[2]
        button4.text = buttons[3]
    }

    private fun setButtonsDefaultBackgorund() {
        val color = ContextCompat.getColor(this, R.color.colorPrimary)
        button1.setBackgroundColor(color)
        button2.setBackgroundColor(color)
        button3.setBackgroundColor(color)
        button4.setBackgroundColor(color)
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        button1.isEnabled = enabled
        button2.isEnabled = enabled
        button3.isEnabled = enabled
        button4.isEnabled = enabled
    }

    fun toMenu(view: View?) {
        onBackPressed()
    }

    override fun onStop() {
        super.onStop()
        dbHelper.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }

    override fun onBackPressed() {
        playSound(R.raw.trans)
        super.onBackPressed()
    }

    private fun playSound(soundId: Int) {
        val mp = MediaPlayer.create(this, soundId)
        mp.setOnCompletionListener {
                it.reset()
                it.release()
            }
        mp.start()
    }

    private fun saveResultToPrefs() {
        val editor = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE).edit()
        editor.putInt(APP_PREFERENCES_SCORE, score)
        editor.apply()
    }

    private fun getCurrentResultFromPrefs(): Int {
        return getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE).getInt(
            APP_PREFERENCES_SCORE,
            0
        )
    }


    companion object {
        const val PHOTO_APPEARING_TIME:Long = 2400
        const val LOG_TAG = "LOG_TAG"
        const val APP_PREFERENCES = "mysettings"
        private const val APP_PREFERENCES_SCORE = "score"
    }
}