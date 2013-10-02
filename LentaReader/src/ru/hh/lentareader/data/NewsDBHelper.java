package ru.hh.lentareader.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NewsDBHelper extends SQLiteOpenHelper {

	public static final String KEY_GUID = "guid";
	public static final String KEY_TITLE = "title";
	public static final String KEY_LINK = "link";
	public static final String KEY_DESCRIPTION = "desc";
	public static final String KEY_DATE = "date";
	public static final String KEY_IMAGE = "image_link";
	public static final String KEY_CATEGORY = "category";
	public static final String KEY_APP_ID = "app_id";
	
	public static final String DB_NAME = "lenta_news";
	
	public NewsDBHelper(Context context) {
		super(context, DB_NAME, null, 1);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table " + DB_NAME + " (" + 
	            "_id integer primary key autoincrement," +
		        KEY_GUID + " text," +
		        KEY_TITLE + " text," +
		        KEY_LINK + " text," +
		        KEY_DESCRIPTION + " text," +
		        KEY_DATE + " text," +
		        KEY_IMAGE + " text," +
		        KEY_CATEGORY + " text," +
		        KEY_APP_ID + " text" +
				");");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	
	

}