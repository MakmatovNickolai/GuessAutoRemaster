package kon4.sam.guessauto

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kon4.sam.guessauto.App.Companion.APP_PREFERENCES
import kon4.sam.guessauto.App.Companion.apiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.system.exitProcess

class MenuActivity : AppCompatActivity() {
    var user_name:String=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        getMyInfo()
    }

    private fun getMyInfo() {
        val user_name =  applicationContext.getSharedPreferences(
            APP_PREFERENCES, Context.MODE_PRIVATE
        ).getString(
            App.APP_PREFERENCES_USERNAME, ""
        )!!

        if (user_name.isNotEmpty()) {
            App.username = user_name
            apiClient.getApiService(this).get_my_score(user_name).enqueue(object :
                Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.i("DEV", call.toString())
                    Log.i("DEV", t.message.toString())

                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    val res = response.body()
                    if (res != null) {
                        App.score = res.toInt()
                    }
                }
            })
        } else {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.menuMain, UserFragment.newInstance(), "user")
                .commit()
        }

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
            R.id.change -> {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.menuMain, UserFragment.newInstance(), "user")
                    .commit()
            }
            R.id.exit -> {
                moveTaskToBack(true)
                exitProcess(-1)
            }
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            Log.i("DEV", "onBackPressed")
            super.onBackPressed()
        } else {
            Log.i("DEV", "popBackStack")
            supportFragmentManager.popBackStack()
        }
    }
}