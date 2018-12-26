package com.goodocom.wms.bluetooth

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Database(context: Context, name: String): SQLiteOpenHelper(context, name, null, 1) {
    private var begin: Boolean = false
        set(v) {
            if(v && !field) {
                writableDatabase.execSQL("PRAGMA synchronous = OFF")
                writableDatabase.execSQL(SQL_CLEAR_CONTACT)
                writableDatabase.beginTransaction()
            } else if(!v && field) {
                writableDatabase.setTransactionSuccessful()
                writableDatabase.endTransaction()
            }
            field = v
        }
    override fun onCreate(db: SQLiteDatabase?) {
        db?.let { it.execSQL(SQL_CREATE_CONTACT) }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}
    fun insert(name: String, number: String): Boolean {
        val cValue = ContentValues()
        cValue.put(COL_NAME, name)
        cValue.put(COL_NUMBER, number)
        begin = true
        return writableDatabase.insert(CONTACT, null, cValue) != -1L
    }

    fun commit() { begin = false }
    fun clear() { begin = true }

    fun query(): Cursor? {
        return readableDatabase.query(CONTACT,
            null, null, null, null, null, null)
    }

    fun query(number: String): String? {
        var name: String
        var num: String

        val cur = query()
        cur!!.moveToFirst()
        while (!cur.isAfterLast) {
            name = cur.getString(cur.getColumnIndex(COL_NAME))
            num = cur.getString(cur.getColumnIndex(COL_NUMBER))
            if (num == number) {
                cur.close()
                return name
            }
            cur.moveToNext()
        }

        return null
    }

    companion object {
        private const val CONTACT = "contact"
        private const val SQL_CLEAR_CONTACT = "DELETE FROM $CONTACT"
        private const val SQL_CREATE_CONTACT = "create table if not exists $CONTACT(_id integer primary key autoincrement,name text,number text)"

        const val COL_NAME = "name"
        const val COL_NUMBER = "number"
    }
}