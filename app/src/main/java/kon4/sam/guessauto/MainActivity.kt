package kon4.sam.guessauto

import android.R.attr.shape
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.AlertDialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import kon4.sam.guessauto.App.Companion.APP_PREFERENCES
import kon4.sam.guessauto.App.Companion.APP_PREFERENCES_USERNAME
import kon4.sam.guessauto.data.DBHelper
import kon4.sam.guessauto.network.ApiClient
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.fragment_user.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {
    private var hearts: Array<ImageView> = arrayOf()
    private lateinit var dbHelper: DBHelper
    private lateinit var timer: CountDownTimer
    private lateinit var allCarImages: MutableList<String>
    private lateinit var currentPhotoAutoName:String
    private var currentPhotoIndex = 0
    private var score = 0
    private var attempt = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setupToolbar()
        setupButtonsCorners()

        dbHelper = DBHelper(this)
        allCarImages = dbHelper.getAllAuto()
        allCarImages.shuffle()


        hearts = arrayOf(heart1, heart2, heart3)
        timer = object : CountDownTimer(10000, 50) {
            override fun onTick(millisUntilFinished: Long) {
                progressBar.progress = millisUntilFinished.toInt() / 100
            }

            override fun onFinish() {
                loseAttempt()
            }
        }

        setRandomImage()
    }

    private fun setupButtonsCorners() {
        (button1.background.mutate() as GradientDrawable).cornerRadii = floatArrayOf(70f, 70f, 0f, 0f, 0f, 0f, 0f, 0f)
        (button2.background.mutate() as GradientDrawable).cornerRadii = floatArrayOf(0f, 0f, 70f, 70f, 0f, 0f, 0f, 0f)
        (button4.background.mutate() as GradientDrawable).cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, 70f, 70f, 0f, 0f)
        (button3.background.mutate() as GradientDrawable).cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 70f, 70f)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true);
            supportActionBar!!.setDisplayShowHomeEnabled(true);
            supportActionBar!!.setDisplayShowTitleEnabled(false)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    fun pressButton(view: View) {
        view as Button
        setButtonsEnabled(false)
        timer.cancel()

        val chosenAutoButtonText = view.text.toString()
        var isAnswerCorrect = false
        if (chosenAutoButtonText == currentPhotoAutoName) {
            isAnswerCorrect = true
            score += progressBar.progress
            onSuccessAnswer(view)
        } else {
            onWrongAnswer(view)
        }
        setCaption()
        when {
            currentPhotoIndex == allCarImages.size -> {
                saveResultToPrefs()
                showFinalDialog(R.string.title, R.string.completed, R.drawable.starwin)
                return
            }
            isAnswerCorrect -> {
                setNewCard()
            }
            !isAnswerCorrect -> {
                loseAttempt()
            }
        }
    }

    private fun setCaption() {
        val scoreCaption =  resources.getString(R.string.auto) +": ${currentPhotoIndex}\\${allCarImages.size} " + resources.getString(R.string.score) + " " + score + " "
        scoreText.text = scoreCaption
    }

    private fun onSuccessAnswer(view: View) {
        playSound(R.raw.success)
        (view.background as GradientDrawable).setColor(ContextCompat.getColor(this, R.color.colorGreen))
    }

    private fun onWrongAnswer(view: View) {
        playSound(R.raw.fail)
        (view.background as GradientDrawable).setColor(ContextCompat.getColor(this, R.color.colorRed))
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
                }
            })
    }

    private fun setRandomImage() {
        currentPhotoAutoName =  allCarImages[currentPhotoIndex]
        val nextImageLink = dbHelper.getAutoLinkBName(allCarImages[currentPhotoIndex])

        progressBar2.visibility = View.VISIBLE
        Glide
            .with(this)
            .load(nextImageLink)
            .listener(object : RequestListener<Drawable> {

                override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        isFirstResource: Boolean
                ): Boolean {
                    progressBar2.visibility = View.GONE
                    Toast.makeText(this@MainActivity, "Не удалось загрузить фото, похоже проблемы с интернетом", Toast.LENGTH_LONG).show()
                    return false
                }

                override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                ): Boolean {
                    progressBar2.visibility = View.GONE
                    setButtonsText()
                    setButtonsDefaultBackground()
                    setButtonsEnabled(true)
                    timer.start()
                    currentPhotoIndex++
                    return false
                }
            })
            .into(carImage)
    }

    private fun setButtonsText() {
        val buttons = dbHelper.getAllCaptionsForAuto(currentPhotoAutoName)
        button1.text = buttons[0]
        button2.text = buttons[1]
        button3.text = buttons[2]
        button4.text = buttons[3]
    }

    private fun setButtonsDefaultBackground() {
        (button1.background as GradientDrawable).setColor(ContextCompat.getColor(this, R.color.colorPrimary))
        (button2.background as GradientDrawable).setColor(ContextCompat.getColor(this, R.color.colorPrimary))
        (button3.background as GradientDrawable).setColor(ContextCompat.getColor(this, R.color.colorPrimary))
        (button4.background as GradientDrawable).setColor(ContextCompat.getColor(this, R.color.colorPrimary))
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        button1.isEnabled = enabled
        button2.isEnabled = enabled
        button3.isEnabled = enabled
        button4.isEnabled = enabled
    }


    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        dbHelper.close()
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

        val user_name = applicationContext.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE).getString(
            APP_PREFERENCES_USERNAME, "")!!

        ApiClient().getApiService(this).set_score(user_name, score.toString()).enqueue(object :
            Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.i("DEV", call.toString())
                Log.i("DEV", t.message.toString())

            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                val res = response.body()


                if (res == "OK") {
                    Log.i("DEV", "SAVED SCORE")
                } else {

                }
            }
        })

        App.score = score
    }

    private fun getCurrentResultFromPrefs(): Int {
        return App.score
    }


    companion object {
        const val PHOTO_APPEARING_TIME:Long = 2400
    }
}