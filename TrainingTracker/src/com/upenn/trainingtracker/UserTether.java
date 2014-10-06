package com.upenn.trainingtracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class UserTether 
{
	private static UserTether instance;
	
	private UserTether() {}
	
	public static UserTether getInstance()
	{
		if (instance == null)
		{
			instance = new UserTether();
		}
		return instance;
	}
	public void addUser(Context context, int id, String fullName, String userName, 
			String password, String email, String phone)
	{
		ContentValues values = new ContentValues();
		values.put(Keys.User.ID, id);
		values.put(Keys.User.FULL_NAME, fullName);
		values.put(Keys.User.USER_NAME, userName);
		values.put(Keys.User.PASSWORD, password);
		values.put(Keys.User.EMAIL, email);
		values.put(Keys.User.PHONE, phone);
		
		DatabaseHandler db = new DatabaseHandler(context);
		db.insertIntoTable(DatabaseHandler.USER, null, values);
	}
	public int getUserID(Context context, String userName)
	{
		DatabaseHandler db = new DatabaseHandler(context);
		Cursor result = db.queryFromTable(DatabaseHandler.USER, new String[]{Keys.User.ID}, 
				Keys.User.USER_NAME + "=?", 
				new String[]{userName});
		result.moveToFirst();
		return result.getInt(result.getColumnIndex(Keys.User.ID));
	}
	public String getUserFullNameFromUserID(Context context, int userID)
	{
		DatabaseHandler db = new DatabaseHandler(context);
		Cursor result = db.queryFromTable(DatabaseHandler.USER, 
				new String[]{Keys.User.FULL_NAME}, Keys.User.ID + "=?", 
				new String[]{Integer.toString(userID)});
		result.moveToFirst();
		return result.getString(result.getColumnIndex(Keys.User.FULL_NAME));
	}
	public String getUserNameFromUserID(Context context, int userID)
	{
		DatabaseHandler db = new DatabaseHandler(context);
		Cursor result = db.queryFromTable(DatabaseHandler.USER, 
				new String[]{Keys.User.USER_NAME}, Keys.User.ID + "=?", 
				new String[]{Integer.toString(userID)});
		result.moveToFirst();
		return result.getString(result.getColumnIndex(Keys.User.USER_NAME));
	}	
	public void addUsersFromJSON(Context context, String json)
	{
		try
		{
			JSONObject input = new JSONObject(json);
			String updateTime = input.getString("time");
			this.setTimeOfLastUpdate(context, updateTime);
			JSONObject data = input.getJSONObject("data");
			JSONArray deletedUserIDs = data.getJSONArray("deleted_user_ids");
			for (int index = 0; index < deletedUserIDs.length(); ++index)
			{
				this.deleteUserByID(context, deletedUserIDs.getInt(index));
			}
			JSONArray users = data.getJSONArray("users");
			for (int index = 0; index < users.length(); ++index)
			{
				JSONObject user = users.getJSONObject(index);
				int id = user.getInt(Keys.User.ID);
				String fullName = user.getString(Keys.User.FULL_NAME);
				String userName = user.getString(Keys.User.USER_NAME);
				String password = user.getString(Keys.User.PASSWORD);
				String email = user.getString(Keys.User.EMAIL);
				String phone = user.getString(Keys.User.PHONE);
				Log.i("TAG","ADDED USER: " + userName);
				this.addUser(context, id, fullName, userName, password, email, phone);
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
	public void deleteUserByID(Context context, int userID)
	{
		DatabaseHandler db = new DatabaseHandler(context);
		db.deleteEntries(DatabaseHandler.USER, Keys.User.ID + "=?", new String[]{Integer.toString(userID)});
	}
	public List<String> getUserNames(Context activity)
	{
		DatabaseHandler db = new DatabaseHandler(activity);
		Cursor userCursor = db.queryFromTable(DatabaseHandler.USER, new String[]{Keys.User.USER_NAME}, null, null);
		List<String> userNames = new ArrayList<String>();
		while (userCursor.moveToNext())
		{
			String userName = userCursor.getString(userCursor.getColumnIndex(Keys.User.USER_NAME));
			userNames.add(userName);
		}
		return userNames;
	}
	public boolean isValiduser(Context context, String userName, String password)
	{
		DatabaseHandler db = new DatabaseHandler(context);
		Cursor result = db.queryFromTable(DatabaseHandler.USER, new String[]{Keys.User.USER_NAME}, 
				Keys.User.USER_NAME + "=? AND " + Keys.User.PASSWORD + "=?", 
				new String[]{userName, password});
		return result.getCount() > 0;
	}
	public List<String> getUserFullNames(Context activity)
	{
		DatabaseHandler db = new DatabaseHandler(activity);
		Cursor userCursor = db.queryFromTable(DatabaseHandler.USER, new String[]{Keys.User.FULL_NAME}, null, null);
		List<String> userNames = new ArrayList<String>();
		while (userCursor.moveToNext())
		{
			String userName = userCursor.getString(userCursor.getColumnIndex(Keys.User.FULL_NAME));
			userNames.add(userName);
		}
		return userNames;
	}
	public Map<String, String> getUserNameToUserFullName(Context activity)
	{
		Map<String, String> userNameToFullName = new HashMap<String, String>();
		List<String> userNames = this.getUserNames(activity);
		List<String> fullNames = this.getUserFullNames(activity);
		for (int index = 0; index < userNames.size(); ++index)
		{
			userNameToFullName.put(userNames.get(index), fullNames.get(index));
		}
		return userNameToFullName;
	}
	public String getTimeOfLastUpdate(Context context)
	{
		return TetherUtils.getTimeOfLastUpdate(context, DatabaseHandler.USER);
	}
	public void setTimeOfLastUpdate(Context context, String time)
	{
		TetherUtils.setTimeOfLastUpdate(context, DatabaseHandler.USER, time);
	}

}
