package kon4.sam.guessauto.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import kon4.sam.guessauto.R
import org.xmlpull.v1.XmlPullParser

class DBHelper(private val fContext: Context) :
    SQLiteOpenHelper(fContext, DATABASE_NAME, null, 1) {
    private var database = this.writableDatabase
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE " + TABLE_NAME + " ("
                    + "id INTEGER PRIMARY KEY, " + "AutoName TEXT, " + "buttons TEXT, " + "link TEXT"
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
                    // Тег Record найден, теперь получим его атрибутыxxx и
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
        }catch (e: Exception) {
            Log.e(LOG_TAG, e.message, e)
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

    fun getAutoLinkBName(AutoName:String) : String {
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
        if (newVersion == 2) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            onCreate(db)
        }
    }

    companion object {
        const val LOG_TAG = "LOG_TAG"
        private const val DATABASE_NAME = "myDB"
        const val TABLE_NAME = "mytable"
    }
}