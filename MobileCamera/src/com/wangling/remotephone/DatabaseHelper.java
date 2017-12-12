package com.wangling.remotephone;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "remotephone_db";
    private static final int version = 1;
    
    public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, version);
		// TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table location_save(_id integer primary key autoincrement, loc_time integer not null, longitude decimal(11,6) not null, latitude decimal(11,6) not null);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
    }
}
