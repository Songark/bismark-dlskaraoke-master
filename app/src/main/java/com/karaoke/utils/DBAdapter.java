package com.karaoke.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DBAdapter {
	
	private static final String DATABASE_NAME = "config.db";
	private static final String DATABASE_TABLE = "setting";
    private static final int DATABASE_VERSION = 1;
    
    private SQLiteDatabase db;
    private final DBOpenHelper dbHelper;
    
    public DBAdapter(Context context) {
        dbHelper = new DBOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = dbHelper.getWritableDatabase();
    }
    
    public void open() throws SQLiteException {
        try {
            db = dbHelper.getWritableDatabase();
        } catch (SQLiteException ex) {
            db = dbHelper.getReadableDatabase();
        }
    }
    
    public long insert(String key, String val) {
        ContentValues value = new ContentValues();
        value.put("key", key);
        value.put("val", val);
        return db.insert(DATABASE_TABLE, null, value);
    }
    
    public int update(String key, String val) {
        ContentValues value = new ContentValues();
        value.put("val", val);
        return db.update(DATABASE_TABLE, value, "key = '" + key + "'", null);
    }
    
    public boolean delete(String key) {
    	return db.delete(DATABASE_TABLE, "key = '" + key + "'", null) > 0;
    }
    
    public Cursor select(String key) {
    	return db.query(false, DATABASE_TABLE, new String[] { "key" , "val"}, "key = '" + key + "'", null, null, null, null, null);
    }
    
    public void close() {
        db.close();
    }
    
    private static class DBOpenHelper extends SQLiteOpenHelper {

    	public DBOpenHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
    		// TODO Auto-generated constructor stub
    	}

    	@Override
    	public void onCreate(SQLiteDatabase db) {
    		// TODO Auto-generated method stub
    		db.execSQL("Create table " + DATABASE_TABLE + " ('key' text, 'val' text);");
    	}

    	@Override
    	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    		// TODO Auto-generated method stub
    		db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
    	}
    }
}




