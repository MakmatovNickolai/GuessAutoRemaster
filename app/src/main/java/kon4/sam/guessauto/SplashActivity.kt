package kon4.sam.guessauto

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import java.util.concurrent.TimeUnit

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        try {
            TimeUnit.MILLISECONDS.sleep(600)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }
}