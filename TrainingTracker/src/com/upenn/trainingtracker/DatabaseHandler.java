package com.upenn.trainingtracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

public class DatabaseHandler extends SQLiteOpenHelper
{ 
    // Database Version
    private static final int DATABASE_VERSION = 77;
 
    // Database Name
    private static final String DATABASE_NAME = "service_manager.db";
 
    // Contacts table name
    public static final String USER = "user";
    public static final String DOG = "dog";
    public static final String ENTRY = "entry";
    public static final String SUB_CATEGORY = "sub_category";
    public static final String PARENT_CATEGORY = "parent_category";
    public static final String LAST_UPDATE = "last_update";
    
    public static final String TABLE_SYNC = "sync_table";
 
    public DatabaseHandler(Context context) 
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    /**
     * This method is only called when first creating the database, NOT everytime the application is run.
     * If the structure of the database is changed, for example if additional columns or tables are added, then
     * these should be specified in this method and then the DATABASE_VERSION number above should be increased
     * by 1.  This tells Android to run the onUpgrade method which drops all the tables and then calls the onCreate
     * method.
     * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
     */
    @Override
    public void onCreate(SQLiteDatabase db) 
    {
        String CREATE_USERS_TABLE = "CREATE TABLE " + USER + "("
                + Keys.User.ID + " INTEGER PRIMARY KEY, " + 
        		Keys.User.FULL_NAME + " TEXT, " +
                Keys.User.USER_NAME + " TEXT, " +
        		Keys.User.PASSWORD + " TEXT, " + 
                Keys.User.EMAIL + "  TEXT, " +
        		Keys.User.PHONE + " TEXT " + 
        		")";
        
        String CREATE_PARENT_CATEGORY_TABLE = "CREATE TABLE " + PARENT_CATEGORY + "("
        		+ Keys.ParentCategory.ID + " INTEGER PRIMARY KEY, " +
        		Keys.ParentCategory.NAME + " TEXT " +
        		")";
        String CREATE_SUB_CATEGORY_TABLE = "CREATE TABLE " + SUB_CATEGORY + "(" +
        		Keys.SubCategory.ID + " INTEGER PRIMARY KEY, " +
        		Keys.SubCategory.NAME + " TEXT, " +
        		Keys.SubCategory.PLAN + " TEXT, " +
        		Keys.SubCategory.PARENT_CATEGORY_ID + " INTEGER, " +
        		"FOREIGN KEY(" + Keys.SubCategory.PARENT_CATEGORY_ID + ") REFERENCES " + 
        			PARENT_CATEGORY + "(" + Keys.ParentCategory.ID + ") " +
        		")";
        String CREATE_DOGS_TABLE = "CREATE TABLE " + DOG + "(" +
                Keys.User.ID + " INTEGER PRIMARY KEY," + 
        		Keys.Dog.NAME + " TEXT, " +
        		Keys.Dog.BIRTH_DATE + " TEXT, " +
                Keys.Dog.BREED + " TEXT, " +
        		Keys.Dog.SERVICE_TYPE + " TEXT " + 
                ")";
        
        String CREATE_ENTRIES_TABLE = "CREATE TABLE " + ENTRY + "("+ 
        		Keys.Entry.ID + " INTEGER PRIMARY KEY, " +
        		Keys.Entry.SESSION_DATE + " TEXT, " +
        		Keys.Entry.PLAN + " TEXT, " +
        		Keys.Entry.TRIALS_RESULT + " TEXT, " +
        		Keys.Entry.DOG_ID + " INTEGER, " +
        		Keys.Entry.USER_ID + " INTEGER, " +
        		Keys.Entry.SUB_CATEGORY_ID + " INTEGER, " +
        		Keys.Entry.SYNCED + " INTEGER " + 
        		")";
        String CREATE_LAST_UPDATE_TABLE = "CREATE TABLE " + LAST_UPDATE + "("+
        		Keys.LastUpdate.TABLE_NAME + " TEXT, " +
        		Keys.LastUpdate.TIME + " TEXT " +
        		")";
        
        db.execSQL(CREATE_PARENT_CATEGORY_TABLE);
        db.execSQL(CREATE_SUB_CATEGORY_TABLE);
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_DOGS_TABLE);
        db.execSQL(CREATE_ENTRIES_TABLE);
        db.execSQL(CREATE_LAST_UPDATE_TABLE);
        
        this.setDefaultLastUpdate(db);
    }
    public boolean tableContainsPrimaryKey(String tableName, int primaryKey)
    {
    	Cursor result = this.queryFromTable(tableName, null, "id=?", new String[]{Integer.toString(primaryKey)});
    	return result.getCount() > 0;
    }
	private void setDefaultLastUpdate(SQLiteDatabase db)
	{
		for (String tableName : new String[] {DatabaseHandler.USER, 
				DatabaseHandler.DOG, DatabaseHandler.ENTRY, DatabaseHandler.PARENT_CATEGORY})
		{
			ContentValues values = new ContentValues();
			values.put(Keys.LastUpdate.TABLE_NAME, tableName);
			values.put(Keys.LastUpdate.TIME, "NEVER");
			db.insert(DatabaseHandler.LAST_UPDATE, null, values);
		}
	}
    @Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) 
	{
    	db.execSQL("DROP TABLE '" + USER + "'");
    	db.execSQL("DROP TABLE '" + DOG + "'");
    	db.execSQL("DROP TABLE '" + ENTRY + "'");
    	db.execSQL("DROP TABLE '" + PARENT_CATEGORY + "'");
    	db.execSQL("DROP TABLE '" + SUB_CATEGORY + "'");
    	db.execSQL("DROP TABLE'" + LAST_UPDATE + "'");

    	onCreate(db);
	}
    public void deleteEntries(String tableName, String whereClause, String[] whereArgs)
    {
    	SQLiteDatabase db = this.getWritableDatabase();
    	db.delete(tableName, whereClause, whereArgs);
    }
    public void clearTable(String tableName)
    {
    	SQLiteDatabase db = this.getWritableDatabase();
    	db.delete(tableName, null, null);
    }
    public Cursor queryFromTable(String tableName, String[] columnNames, String whereClause, String[] whereArgs)
    {
    	SQLiteDatabase db = this.getReadableDatabase();
    	Cursor result = db.query(tableName, columnNames, whereClause, whereArgs, null, null, null, null);
    	return result;
    }
    public void insertIntoTable(String tableName, String nullColumnHack, ContentValues values)
    {
    	SQLiteDatabase db = this.getWritableDatabase();
    	db.insert(tableName, nullColumnHack, values);
    	System.out.println("Inserted into " + tableName);
    	db.close();
    }
    public void updateTable(String tableName, ContentValues values, String whereClause, String[] whereArgs)
    {
    	SQLiteDatabase db = this.getWritableDatabase();
    	db.update(tableName, values, whereClause, whereArgs);
    	db.close();
    }
    public int getNumRecordsForTable(String tableName)
    {
    	SQLiteDatabase db = this.getReadableDatabase();
    	Cursor cursor = db.rawQuery("SELECT * FROM " + tableName, null);
    	return cursor.getCount();
    }
    public void incrementEntryValue(String tableName, String columnName, String whereClause)
    {
    	SQLiteDatabase db = this.getWritableDatabase();
    	String sql = "UPDATE " + tableName + " SET " + columnName + " = " + columnName + " + 1 WHERE " + whereClause;
    	db.execSQL(sql);
    }
}
