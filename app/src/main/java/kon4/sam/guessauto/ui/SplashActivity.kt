package kon4.sam.guessauto.ui

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import kon4.sam.guessauto.R
import kon4.sam.guessauto.view_model.SplashViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_splash.*

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        viewModel.getMyInfo()
        animateLaunchText()
    }

    private fun animateLaunchText() {
        val disappearing = AlphaAnimation(1.0f, 0.0f)
        disappearing.duration = 1000
        disappearing.startOffset = 1000
        disappearing.fillAfter = true
        disappearing.setAnimationListener(object : AnimationListener {
            override fun onAnimationEnd(arg0: Animation) {
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
        })

        val appearing = AlphaAnimation(0.0f, 1.0f)
        appearing.duration = 1000
        appearing.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(arg0: Animation) {
                launchTextView.startAnimation(disappearing)
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        launchTextView.startAnimation(appearing)
    }
}