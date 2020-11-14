package kon4.sam.guessauto

import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlin.system.exitProcess

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    fun onMenuItemClick(view: View) {
        MediaPlayer.create(this, R.raw.trans).start()
        when (view.id) {
            R.id.start -> {
                val main = Intent(this, MainActivity::class.java)
                startActivity(main)
            }
            R.id.records -> {
                val score = Intent(this, ScoreActivity::class.java)
                startActivity(score)
            }
            R.id.exit -> {
                moveTaskToBack(true)
                exitProcess(-1)
            }
        }
    }
}