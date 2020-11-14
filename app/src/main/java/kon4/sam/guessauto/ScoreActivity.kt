package kon4.sam.guessauto

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_score.textView3


class ScoreActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setScoreText()
    }

    override fun onResume() {
        super.onResume()
        setScoreText()
    }

    private fun setScoreText() {
        val text =  resources.getString(R.string.record) + " " + applicationContext.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE).getInt(APP_PREFERENCES_SCORE, 0)
        textView3.text = text
    }

    companion object {
        const val APP_PREFERENCES = "mysettings"
        private const val APP_PREFERENCES_SCORE = "score"
    }
}