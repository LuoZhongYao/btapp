package com.goodocom.wms.bluetooth

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Database(context: Context, name: String): SQLiteOpenHelper(context, name, null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.let {
            it.execSQL(SQL_CREATE_CALLLOG_TABLE)
            it.execSQL(SQL_CREATE_PHONEBOOK_TABLE)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}
    fun insert(name: String, number: String): Boolean {
        val cValue = ContentValues()
        cValue.put(COL_NAME, name)
        cValue.put(COL_NUMBER, number)
        return writableDatabase.insert(PHONEBOOK_TABLE, null, cValue) != -1L
    }

    fun clear() {
        writableDatabase.execSQL(SQL_CLEAR_PHONEBOOK)
    }

    fun query(): Cursor? {
        return readableDatabase.query(PHONEBOOK_TABLE,
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
        private val SQL_CREATE_PHONEBOOK_TABLE = "create table if not exists phonebook(_id integer primary key autoincrement,name text,number text)"
        private val SQL_CREATE_CALLLOG_TABLE = "create table if not exists calllog(_id integer primary key autoincrement,name text,number text,type integer)"
        private val SQL_CLEAR_PHONEBOOK = "DELETE FROM phonebook"
        private val SQL_CLEAR_CALLLOG = "DELETE FROM calllog"
        private val PHONEBOOK_TABLE = "phonebook"
        private val CALLLOG_TABLE = "calllog"

        val COL_NAME = "name"
        val COL_NUMBER = "number"
        val COL_TYPE = "type"
    }
}