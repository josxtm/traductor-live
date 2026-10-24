package com.josxtm.traductorlive
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HistoryEntry(val time:String,val lang:String,val source:String,val target:String)
class HistoryDb(context:Context):SQLiteOpenHelper(context,"history.db",null,1) {
 override fun onCreate(db:SQLiteDatabase) { db.execSQL("CREATE TABLE entries(id INTEGER PRIMARY KEY AUTOINCREMENT, time INTEGER, lang TEXT, source TEXT, target TEXT)") }
 override fun onUpgrade(db:SQLiteDatabase,oldVersion:Int,newVersion:Int) {}
 fun insert(lang:String,source:String,target:String) {
  writableDatabase.execSQL("INSERT INTO entries(time,lang,source,target) VALUES(?,?,?,?)",arrayOf(System.currentTimeMillis(),lang,source,target))
 }
 fun entries():List<HistoryEntry> {
  val data=mutableListOf<HistoryEntry>()
  readableDatabase.rawQuery("SELECT time,lang,source,target FROM entries ORDER BY id DESC",null).use { c ->
   val format=SimpleDateFormat("dd/MM/yyyy HH:mm:ss",Locale("es","ES"))
   while(c.moveToNext()) data.add(HistoryEntry(format.format(Date(c.getLong(0))),c.getString(1),c.getString(2),c.getString(3)))
  }
  return data
 }
 fun clear(){writableDatabase.execSQL("DELETE FROM entries")}
}