package com.upenn.trainingtracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class TetherUtils 
{
	private static String IMAGE_DIR = "image_dir";
	public static String getCurrentDateTimeString()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(new Date());
	}
	public static JSONArray cursorToJSON(Cursor cursor, String[] strColumns, String[] intColumns)
	{
		JSONArray rows = new JSONArray();
		while (cursor.moveToNext())
		{
			JSONObject row = new JSONObject();
			if (strColumns != null)
			{
				for (String columnName : strColumns)
				{
					String value = cursor.getString(cursor.getColumnIndex(columnName));
					try {
						row.put(columnName, value);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
			if (intColumns != null)
			{
				for (String columnName : intColumns)
				{
					int value = cursor.getInt(cursor.getColumnIndex(columnName));
					try {
						row.put(columnName, value);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
			rows.put(row);
		}
		return rows;
	}
	public static String getTimeOfLastUpdate(Context context, String tableName)
	{
		DatabaseHandler db = new DatabaseHandler(context);
		Cursor cursor = db.queryFromTable(DatabaseHandler.LAST_UPDATE, 
				new String[]{Keys.LastUpdate.TIME}, 
				Keys.LastUpdate.TABLE_NAME + "=?", 
				new String[] {tableName});
		cursor.moveToFirst();
		return cursor.getString(cursor.getColumnIndex(Keys.LastUpdate.TIME));
	}
	public static void setTimeOfLastUpdate(Context context, String tableName, String time)
	{
		Log.i("DOG","SETTING FOR TABLE NAME: " + tableName + " to time of " + time);
		DatabaseHandler db = new DatabaseHandler(context);
		ContentValues values = new ContentValues();
		values.put(Keys.LastUpdate.TABLE_NAME, tableName);
		values.put(Keys.LastUpdate.TIME, time);
		db.updateTable(DatabaseHandler.LAST_UPDATE, values, 
				Keys.LastUpdate.TABLE_NAME + "=?", 
				new String[] {tableName});
	}
    /**
     * Saves the bitmap to InternalStorage.  This is storage that is only accessible to the application.
     * When the application is uninstalled, this information also dissapears.
     * @param context
     * @param bitmap
     * @param name
     */
    public static void saveImage(Context context, Bitmap bitmap, String name)
    {
    	ContextWrapper cw = new ContextWrapper(context);
    	File directory = cw.getDir(TetherUtils.IMAGE_DIR, Context.MODE_PRIVATE);
    	File myPath = new File(directory, name);
    	
    	FileOutputStream fos = null;
    	try {
    		fos = new FileOutputStream(myPath);
    		bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
    		fos.close();
    	} catch (Exception e){
    		e.printStackTrace();
    	}
    }
    /**
     * Loads Bitmaps from internal storage
     * @param context
     * @param name
     * @return
     */
    public static Bitmap loadImage(Context context, String name)
    {
    	ContextWrapper cw = new ContextWrapper(context);
    	Bitmap b = null;
        try {
        	File directory = cw.getDir(TetherUtils.IMAGE_DIR, Context.MODE_PRIVATE);
            File f=new File(directory, name);
            b = BitmapFactory.decodeStream(new FileInputStream(f));
        } 
        catch (FileNotFoundException e) 
        {
            e.printStackTrace();
        }
        return b;
    }

}
