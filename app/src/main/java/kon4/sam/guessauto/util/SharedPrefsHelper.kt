package kon4.sam.guessauto.util

import android.content.Context
import android.content.SharedPreferences

class SharedPrefsHelper(context: Context) {
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun getUserNameFromPrefs() :String? {
        return sharedPrefs.getString(USERNAME, "")
    }

    fun getScoreFromPrefs(): Int {
        return sharedPrefs.getInt(SCORE, 0)
    }

    fun getUrlFromPrefs(): String? {
        return sharedPrefs.getString(URL, "")
    }

    fun saveUserNameToPrefs(userName: String) {
        val editor = sharedPrefs.edit()
        editor.putString(USERNAME, userName)
        editor.apply()
    }

    fun saveUserScoreToPrefs(score: Int) {
        val editor = sharedPrefs.edit()
        editor.putInt(SCORE, score)
        editor.apply()
    }

    fun saveUserUrlToPrefs(url: String?) {
        val editor = sharedPrefs.edit()
        editor.putString(URL, url)
        editor.apply()
    }

    fun getStoredXmlDbVersion(): Int {
        return sharedPrefs.getInt(XML_DB_VERSION, 0)
    }

    fun saveXmlDbVersion(version: Int) {
        val editor = sharedPrefs.edit()
        editor.putInt(XML_DB_VERSION, version)
        editor.apply()
    }

    companion object {
        const val PREFERENCES = "PREFERENCES"
        const val USERNAME = "username"
        const val SCORE = "score"
        const val URL = "url"
        const val XML_DB_VERSION = "xml_db_version"
    }
}