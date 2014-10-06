package com.upenn.trainingtracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;


public class ParentCategoryTether 
{
	private static ParentCategoryTether instance;
	
	private ParentCategoryTether() {}
	public static ParentCategoryTether getInstance()
	{
		if (instance == null)
		{
			instance = new ParentCategoryTether();
		}
		return instance;
	}
	public void addParentCategory(Context context, int id, String name)
	{
		ContentValues values = new ContentValues();
		values.put(Keys.ParentCategory.ID, id);
		values.put(Keys.ParentCategory.NAME, name);
		DatabaseHandler db = new DatabaseHandler(context);
		db.insertIntoTable(DatabaseHandler.PARENT_CATEGORY, null, values);
	}
	public String getParentCategoryNameFromID(Context context, int parentCategoryID)
	{
		DatabaseHandler db = new DatabaseHandler(context);
		Cursor result = db.queryFromTable(DatabaseHandler.PARENT_CATEGORY, 
				new String[]{Keys.ParentCategory.NAME}, Keys.ParentCategory.ID + "=?", 
				new String[]{Integer.toString(parentCategoryID)});
		result.moveToFirst();
		return result.getString(result.getColumnIndex(Keys.ParentCategory.NAME));
	}
	public void addParentCategoriesFromJSON(Context context, String json)
	{
		try
		{
			JSONArray subCategories = new JSONArray(json);
			for (int index = 0; index < subCategories.length(); ++index)
			{
				JSONObject subCategory = subCategories.getJSONObject(index);
				int id = subCategory.getInt(Keys.SubCategory.ID);
				String name = subCategory.getString(Keys.SubCategory.NAME);
				this.addParentCategory(context, id, name);
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}	
	}
	public List<String> getParentCategories(Context context)
	{
		DatabaseHandler db = new DatabaseHandler(context);
		Cursor result = db.queryFromTable(DatabaseHandler.PARENT_CATEGORY, 
				new String[]{Keys.ParentCategory.NAME}, null, null);
		List<String> parentCategories = new ArrayList<String>();
		while (result.moveToNext())
		{
			parentCategories.add(result.getString(result.getColumnIndex(Keys.ParentCategory.NAME)));
		}
		return parentCategories;
	}
	public Map<String, Integer> getParentNameToIDMap(Context context)
	{
		DatabaseHandler db = new DatabaseHandler(context);
		Cursor result = db.queryFromTable(DatabaseHandler.PARENT_CATEGORY, 
				new String[]{Keys.ParentCategory.NAME, Keys.ParentCategory.ID}, null, null);
		Map<String, Integer> parentNameToID = new HashMap<String, Integer>();
		while (result.moveToNext())
		{
			String parentName = result.getString(result.getColumnIndex(Keys.ParentCategory.NAME));
			int ID = result.getInt(result.getColumnIndex(Keys.ParentCategory.ID));
			parentNameToID.put(parentName, ID);
		}
		return parentNameToID;
	}
	public void deleteParentCategory(Context context, int ID)
	{
		DatabaseHandler db = new DatabaseHandler(context);
		db.deleteEntries(DatabaseHandler.PARENT_CATEGORY, Keys.SubCategory.ID + "=?", new String[]{Integer.toString(ID)});	
	}
	public String getTimeOfLastUpdate(Context context)
	{
		return TetherUtils.getTimeOfLastUpdate(context, DatabaseHandler.PARENT_CATEGORY);
	}
	public void setTimeOfLastUpdate(Context context, String time)
	{
		TetherUtils.setTimeOfLastUpdate(context, DatabaseHandler.PARENT_CATEGORY, time);
	}
}
