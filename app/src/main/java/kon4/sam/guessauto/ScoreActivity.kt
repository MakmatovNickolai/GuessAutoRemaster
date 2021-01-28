package kon4.sam.guessauto

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kon4.sam.guessauto.App.Companion.apiClient
import kon4.sam.guessauto.network.ApiClient
import kon4.sam.guessauto.network.User
import kon4.sam.guessauto.network.UserItem
import kotlinx.android.synthetic.main.activity_score.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ScoreActivity : AppCompatActivity() {
    private val adapter = GroupAdapter<GroupieViewHolder>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        scoreRecycler.adapter = adapter
        setScoreText()
    }

    override fun onResume() {
        super.onResume()
        //setScoreText()
    }

    private fun setScoreText() {

        progressBar3.visibility - View.VISIBLE
        apiClient.getApiService(this).get_all_score().enqueue(object :
            Callback<List<User>> {
            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                progressBar3.visibility - View.GONE
                Log.i("DEV", call.toString())
                Log.i("DEV", t.message.toString())

            }

            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                val listRes = response.body()
                val text =  App.username+ ", " + resources.getString(R.string.record) + " " + App.score
                textView3.text = text
                progressBar3.visibility - View.GONE
                listRes?.forEach {
                    if (it.user_name == App.username) {
                        adapter.add(UserItem(it, true))
                    } else {
                        adapter.add(UserItem(it,false))
                    }
                }
            }
        })

    }
}