package kon4.sam.guessauto

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kon4.sam.guessauto.App.Companion.APP_PREFERENCES
import kon4.sam.guessauto.App.Companion.APP_PREFERENCES_USERNAME
import kon4.sam.guessauto.network.ApiClient
import kotlinx.android.synthetic.main.fragment_user.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserFragment : Fragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (App.username.isNotEmpty()) {
            editTextTextPersonName.setText(App.username)
        }

        button.setOnClickListener {
            if (editTextTextPersonName.text.isNotEmpty()) {
                val user_name = editTextTextPersonName.text.toString()

                if (App.username.isEmpty()) {
                    ApiClient().getApiService(activity!!.applicationContext).set_new_user(user_name).enqueue(object :
                        Callback<String> {
                        override fun onFailure(call: Call<String>, t: Throwable) {
                            Log.i("DEV", call.toString())
                            Log.i("DEV", t.message.toString())

                        }

                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            val res = response.body()!!
                            Log.i("DEV", res)

                            if (res == "OK") {
                                App.username = user_name
                                val editor = activity!!.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE).edit()
                                editor.putString(APP_PREFERENCES_USERNAME, user_name)
                                editor.apply()

                                activity!!.supportFragmentManager.beginTransaction()
                                    .remove(this@UserFragment).commit()
                            } else if (res == "User exist") {
                                textView4.setText("User exist")
                            } else {

                            }
                        }
                    })
                } else {
                    ApiClient().getApiService(activity!!.applicationContext).update_user_name(App.username, user_name).enqueue(object :
                        Callback<String> {
                        override fun onFailure(call: Call<String>, t: Throwable) {
                            Log.i("DEV", call.toString())
                            Log.i("DEV", t.message.toString())

                        }

                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            val res = response.body()!!
                            Log.i("DEV", res)

                            if (res == "OK") {
                                App.username = user_name
                                val editor = activity!!.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE).edit()
                                editor.putString(APP_PREFERENCES_USERNAME, user_name)
                                editor.apply()

                                activity!!.supportFragmentManager.beginTransaction()
                                    .remove(this@UserFragment).commit()
                            } else if (res == "User exist") {
                                textView4.setText("User exist")
                            } else {

                            }
                        }
                    })
                }

            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_user, container, false)
    }

    companion object {
        fun newInstance() = UserFragment()
    }
}