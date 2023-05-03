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
import kon4.sam.guessauto.databinding.ActivitySplashBinding
import kon4.sam.guessauto.view_model.SplashViewModel

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel.getMyInfo()
        viewModel.reloadAllCars()
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
                binding.launchTextView.startAnimation(disappearing)
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        binding.launchTextView.startAnimation(appearing)
    }
}