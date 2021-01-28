package kon4.sam.guessauto

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_splash.*


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val animation2 = AlphaAnimation(1.0f, 0.0f)
        animation2.setDuration(1000)
        animation2.startOffset = 1000
        animation2.fillAfter = true
        animation2.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(arg0: Animation) {
                Log.i("DEV", "onAnimationEnd2")
                val intent = Intent(this@SplashActivity, MenuActivity::class.java)
                startActivity(intent)
                finish()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })


        val animation1 = AlphaAnimation(0.0f, 1.0f)
        animation1.setDuration(1000)
        animation1.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(arg0: Animation) {
                Log.i("DEV", "onAnimationEnd1")
                launchTextView.startAnimation(animation2)
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        launchTextView.startAnimation(animation1)
    }

    companion object {
        const val SCREEN_LOADING_TIME:Long = 3400
    }
}