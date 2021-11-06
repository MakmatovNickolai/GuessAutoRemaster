package kon4.sam.guessauto.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kon4.sam.guessauto.R
import kon4.sam.guessauto.util.SharedPrefsHelper
import org.xmlpull.v1.XmlPullParser
import timber.log.Timber

class DBHelper(private val fContext: Context, private val sharedPrefsHelper: SharedPrefsHelper) :
    SQLiteOpenHelper(fContext, DATABASE_NAME, null, 1) {

    private var database = this.writableDatabase

    override fun onOpen(db: SQLiteDatabase?) {
        super.onOpen(db)

        var xmlVersion = getXmlDbVersion()
        val storedXmlVersion = sharedPrefsHelper.getStoredXmlDbVersion()
        if (xmlVersion == null) {
            xmlVersion = 1
        }
        if (xmlVersion > storedXmlVersion)  {
            onUpgrade(db!!, storedXmlVersion, xmlVersion)
        }
    }

    private fun getXmlDbVersion(): Int? {
        val xml = fContext.resources.getXml(R.xml.autobase)
        var eventType = xml.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && xml.name == "version") {
                xml.getAttributeValue(null, "number").toIntOrNull().also { version ->
                    return version
                }
            } else {
                eventType = xml.next()
            }
        }
        return 0
    }
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE " + TABLE_NAME + " ("
                    + "id INTEGER PRIMARY KEY, " + "AutoName TEXT, " +
                    "buttons TEXT, " + "link TEXT"
                    + ");"
        )

        // Добавляем записи в таблицу
        val values = ContentValues()

        // Получим файл из ресурсов
        val res = fContext.resources

        // Открываем xml-файл
        val xml = res.getXml(R.xml.autobase)
        try {
            // Ищем конец документа
            var eventType = xml.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                // Ищем теги record
                if (eventType == XmlPullParser.START_TAG
                    && xml.name == "record"
                ) {
                    // Тег Record найден, теперь получим его атрибуты xxx и
                    // вставляем в таблицу
                    val link = xml.getAttributeValue(null, "link")
                    val autoName = xml.getAttributeValue(null, "AutoName")
                    val buttons = xml.getAttributeValue(null, "buttons")

                    values.put("AutoName", autoName)
                    values.put("buttons", buttons)
                    values.put("link", link)
                    db.insert(TABLE_NAME, null, values)
                }
                eventType = xml.next()
            }
        } catch (e: Exception) {
            Timber.e(e)
        } finally {
            // Close the xml file
            xml.close()
        }
    }

    fun getAllCaptionsForAuto(autoName: String) : MutableList<String> {
        val selectQuery = "SELECT  buttons FROM mytable WHERE AutoName = ?"
        var captions = "No captions fetched from db"

        database.rawQuery(selectQuery, arrayOf(autoName)).use {
            if (it.moveToFirst()) {
                captions = it.getString(it.getColumnIndex("buttons"))
            }
        }
        val res = captions.split(',').toMutableList()
        res.shuffle()
        return res
    }

    fun getAutoLink(AutoName:String) : String {
        val sqlQuery = "select link from mytable where AutoName = ?"
        var res = "No auto fetched from db"
        database.rawQuery(sqlQuery, arrayOf(AutoName)).use {
            if (it.moveToFirst()) {
                res = it.getString(it.getColumnIndex("link"))
                it.close()
            }
        }
        return res
    }

    fun getAllAuto(): MutableList<String> {
        val temp = mutableListOf<String>()
        database.query("mytable", arrayOf("AutoName"), null, null, null, null, null).use {
            if (it.moveToFirst()) {
                val imageColIndex = it.getColumnIndex("AutoName")
                temp.add(it.getString(imageColIndex))
                while (it.moveToNext()) {
                    temp.add(it.getString(imageColIndex))
                }
            }
            it.close()
        }
        return temp
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (newVersion > oldVersion) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            onCreate(db)
            sharedPrefsHelper.saveXmlDbVersion(newVersion)
        }
    }

    companion object {
        private const val DATABASE_NAME = "myDB"
        const val TABLE_NAME = "mytable"
    }
}