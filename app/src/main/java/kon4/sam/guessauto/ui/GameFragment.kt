package kon4.sam.guessauto.ui

import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.multidex.BuildConfig
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.AndroidEntryPoint
import kon4.sam.guessauto.App
import kon4.sam.guessauto.R
import kon4.sam.guessauto.databinding.FragmentGameBinding
import kon4.sam.guessauto.view_model.GameViewModel
import kotlinx.android.synthetic.main.fragment_game.*
import timber.log.Timber


@AndroidEntryPoint
class GameFragment : Fragment() {

    private var successSound = 0
    private var wrongSound = 0
    private var timerMillisLeft: Long = 0
    private var isTimerPaused = false
    private var isTimerCancelled = false
    private var mInterstitialAd: InterstitialAd? = null
    private var endGameDialog: EndGameDialog? = null
    private val viewModel: GameViewModel by viewModels()
    private lateinit var soundPool: SoundPool
    private lateinit var binding: FragmentGameBinding

    private fun timer(millisInFuture:Long): CountDownTimer {
        return object: CountDownTimer(millisInFuture, 50) {
            override fun onTick(millisUntilFinished: Long){
                when {
                    isTimerPaused -> {
                        timerMillisLeft = millisUntilFinished
                        cancel()
                    }
                    isTimerCancelled -> {
                        cancel()
                    }
                    else -> {
                        progressBar.progress = millisUntilFinished.toInt() / 100
                    }
                }
            }

            override fun onFinish() {
                loseAttempt()
            }
        }
    }

    private val disappearing = AlphaAnimation(1.0f, 0.0f).apply {
        duration = 2000
        setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(arg0: Animation) {
                setNextImage()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }

    private val pressButtonListener = View.OnClickListener {
        pressButton(it)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_game, container, false)
        binding.viewModel = viewModel
        binding.button1.setOnClickListener(pressButtonListener)
        binding.button2.setOnClickListener(pressButtonListener)
        binding.button3.setOnClickListener(pressButtonListener)
        binding.button4.setOnClickListener(pressButtonListener)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        initAd()
        initCaptions()
        initSoundPool()
        setButtonsEnabled(false)
        setNextImage()
        setupObservers()
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showLeaveConfirmation()
                }
            })
    }


    private fun setupObservers() {
        viewModel.showAdEvent.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { needToShowAd ->
                if (needToShowAd) {
                    mInterstitialAd?.show(this.requireActivity()) ?: onGameEnd()
                }
            }
        })
        /*viewModel.userScoreUpdated.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { updated ->
                if (updated) {

                }
                //onGameEnd()
                //findNavController().popBackStack()
            }
        })*/
    }

    private fun preloadNextImage() {
        try {
            Glide.with(this)
                .load(viewModel.updateAndGetNextImageLink())
                .preload()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun initAd() {
        val adRequest = AdRequest.Builder().build()
        val adId = if (!BuildConfig.DEBUG) {
            resources.getString(R.string.adMobInterstitialId)
        } else {
            resources.getString(R.string.adMobInterstitialId)
        }
        InterstitialAd.load(requireActivity(), adId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Timber.e(adError.message)
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
                mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        //onGameEnd()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                        onGameEnd()
                    }

                    override fun onAdShowedFullScreenContent() {
                        mInterstitialAd = null
                        onGameEnd()
                    }
                }
            }
        })
    }

    private fun showLeaveConfirmation() {
        pauseTimer()
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.apply {
            setTitle(resources.getString(R.string.are_you_sure))
            setMessage(resources.getString(R.string.you_will_lost_progress))
            setPositiveButton("Ok") { _, _ ->
                findNavController().popBackStack()
            }
            setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->
                resumeTimer()
            }
        }.create().show()
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            showLeaveConfirmation()
        }
    }

    private fun initCaptions() {
        val scoreCaption = resources.getString(R.string.current_score) + " " + 0
        val autoCountCaption = resources.getString(R.string.auto) +": ${0}\\${viewModel.carImages.size}"
        scoreText.text = scoreCaption
        autoCountText.text = autoCountCaption
    }

    private fun initSoundPool() {
        soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            SoundPool.Builder()
                .setMaxStreams(6)
                .setAudioAttributes(audioAttributes)
                .build()
        } else {
            SoundPool(6, AudioManager.STREAM_MUSIC, 0)
        }
        successSound = soundPool.load(requireActivity(), R.raw.success, 1)
        wrongSound = soundPool.load(requireActivity(), R.raw.fail, 1)
    }

    private fun setNextImage() {
        viewModel.updateCurrentPhotoAutoName()
        Glide
            .with(this)
            .load(viewModel.nextImageLink)
            .listener(object: RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Toast.makeText(
                        this@GameFragment.context,
                        resources.getString(R.string.no_internet_connection),
                        Toast.LENGTH_LONG
                    ).show()
                    //setNewCard()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    setNewCard()
                    return false
                }
            })
            .into(carImage)
    }

    private fun setNewCard() {
        carImage.fitToScreen()
        setButtonsText()
        setButtonDefaultBackground()
        setButtonsEnabled(true)
        startTimer()
        viewModel.currentPhotoIndex++
        preloadNextImage()
    }

    private fun cancelTimer() {
        isTimerCancelled = true
        isTimerPaused = false
    }

    private fun startTimer() {
        timer(GAME_TIMER_MILLISECONDS).start()
        isTimerCancelled = false
        isTimerPaused = false
    }

    private fun resumeTimer() {
        timer(timerMillisLeft).start()
        isTimerPaused = false
        isTimerCancelled = false
    }

    private fun pauseTimer() {
        isTimerPaused = true
        isTimerCancelled = false
    }

    private fun setButtonsText() {
        val buttons = viewModel.getAllCaptionsForAuto()
        button1.text = buttons[0]
        button2.text = buttons[1]
        button3.text = buttons[2]
        button4.text = buttons[3]
    }

    private fun setButtonDefaultBackground() {
        requireActivity().findViewById<AnswerButton>(viewModel.clickedButtonViewId)?.setHasAnswer(false)
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        button1.isEnabled = enabled
        button2.isEnabled = enabled
        button3.isEnabled = enabled
        button4.isEnabled = enabled
    }

    private fun pressButton(view: View) {
        viewModel.clickedButtonViewId = view.id
        setButtonsEnabled(false)
        cancelTimer()

        val chosenAutoButtonText = (view as AnswerButton).text.toString()
        val isAnswerCorrect = chosenAutoButtonText == viewModel.currentPhotoAutoName
        if (isAnswerCorrect) {
            soundPool.play(successSound, 1f, 1f, 0, 0, 1f)
            viewModel.score += progressBar.progress
            increaseScoreCaption()
            view.setHasAnswer(true)
            view.setIsCorrectAnswer(true)
        } else {
            soundPool.play(wrongSound, 1f, 1f, 0, 0, 1f)
            view.setHasAnswer(true)
            view.setIsCorrectAnswer(false)
        }
        increaseAutoCaption()
        when {
            viewModel.currentPhotoIndex == viewModel.carImages.size -> {
                showFinalDialog(true)
                return
            }
            isAnswerCorrect -> {
                carImage.startAnimation(disappearing)
            }
            !isAnswerCorrect -> {
                loseAttempt()
            }
        }
    }

    private fun increaseScoreCaption() {
        val scoreCaption = resources.getString(R.string.current_score) + " " + viewModel.score
        scoreText.text = scoreCaption
    }

    private fun increaseAutoCaption() {
        val autoCountCaption = resources.getString(R.string.auto) +": ${viewModel.currentPhotoIndex}\\${viewModel.carImages.size}"
        autoCountText.text = autoCountCaption
    }

    private fun loseAttempt() {
        viewModel.attempt++
        setLoseHeart()
        if (viewModel.isLastAttempt()) {
            viewModel.attempt = 0
            showFinalDialog(false)
        } else {
            carImage.startAnimation(disappearing)
        }
    }

    private fun setLoseHeart() {
        bottomBarLayout.getChildAt(viewModel.attempt).setBackgroundResource(R.drawable.heart_empty)
    }

    private fun showFinalDialog(isGameCompleted: Boolean) {
        val dialog = EndGameDialog(requireContext(), viewModel, viewLifecycleOwner)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.endgame_dialog)
        dialog.setupFields()
        val finalText:String = if (isGameCompleted) {
            getCompletedGameDialogCaption()
        } else {
            getDefaultDialogCaption()
        }
        dialog.setCongratsText(finalText)
        dialog.show()
        endGameDialog = dialog
    }

    private fun getDefaultDialogCaption(): String {
        return when {
            viewModel.score > App.user.score -> {
                resources.getString(R.string.congrats) +"\n" +
                        resources.getString(R.string.new_high_score) + " " + viewModel.score
            }
            viewModel.score == 0 -> {
                resources.getString(R.string.your_score) + " " + viewModel.score
            }
            else -> {
                resources.getString(R.string.excellent_result) +"\n" +
                        resources.getString(R.string.your_score) + " " + viewModel.score
            }
        }
    }

    private fun getCompletedGameDialogCaption(): String {
        return resources.getString(R.string.congrats) + "\n" + resources.getString(R.string.no_auto_left)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelTimer()
        endGameDialog?.dismiss()
        soundPool.release()
    }

    private fun onGameEnd() {
        findNavController().popBackStack()
    }

    companion object {
        const val GAME_TIMER_MILLISECONDS:Long = 10000
    }
}