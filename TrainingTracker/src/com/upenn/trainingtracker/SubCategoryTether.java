package com.upenn.trainingtracker;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;


public class SubCategoryTether 
{
	private static SubCategoryTether instance;
	
	private SubCategoryTether() {}
	public static SubCategoryTether getInstance()
	{
		if (instance == null)
		{
			instance = new SubCategoryTether();
		}
		return instance;
	}
	public void addSubCategory(Context context, int id, String name, 
			String plan, int parentCategoryID)
	{
		ContentValues values = new ContentValues();
		values.put(Keys.SubCategory.ID, id);
		values.put(Keys.SubCategory.NAME, name);
		values.put(Keys.SubCategory.PLAN, plan);
		values.put(Keys.SubCategory.PARENT_CATEGORY_ID, parentCategoryID);
		DatabaseHandler db = new DatabaseHandler(context);
		db.insertIntoTable(DatabaseHandler.SUB_CATEGORY, null, values);
	}
	public List<String> getAllSubCategoryNames(Context context)
	{
		DatabaseHandler db = new DatabaseHandler(context);
		Cursor result = db.queryFromTable(DatabaseHandler.SUB_CATEGORY, 
				new String[]{Keys.SubCategory.NAME}, null, null);
		List<String> subCatNames = new ArrayList<String>();
		while (result.moveToNext())
		{
			subCatNames.add(result.getString(result.getColumnIndex(Keys.SubCategory.NAME)));
		}
		return subCatNames;
	}
	public Pair<List<String>,List<Integer>> getSubCatNamesAndIDsLists(Context context)
	{
		DatabaseHandler db = new DatabaseHandler(context);
		Cursor result = db.queryFromTable(DatabaseHandler.SUB_CATEGORY, 
				new String[]{Keys.SubCategory.NAME, Keys.SubCategory.ID}, null, null);
		List<String> subCatNames = new ArrayList<String>();
		List<Integer> subCatIDs = new ArrayList<Integer>();
		while (result.moveToNext())
		{
			subCatNames.add(result.getString(result.getColumnIndex(Keys.SubCategory.NAME)));
			subCatIDs.add(result.getInt(result.getColumnIndex(Keys.SubCategory.ID)));
		}
		return new Pair<List<String>,List<Integer>>(subCatNames, subCatIDs);
	}
	public String subCategoryIDToName(Context context, int ID)
	{
		DatabaseHandler db = new DatabaseHandler(context);
		Cursor cursor = db.queryFromTable(DatabaseHandler.SUB_CATEGORY, 
				new String[]{Keys.SubCategory.NAME}, Keys.SubCategory.ID + "=?", 
				new String[]{Integer.toString(ID)});
		cursor.moveToFirst();
		return cursor.getString(cursor.getColumnIndex(Keys.SubCategory.NAME));
	}
	public int getParentCategoryIDForSubCategoryID(Context context, int subCategoryID)
	{
		DatabaseHandler db = new DatabaseHandler(context);
		Cursor cursor = db.queryFromTable(DatabaseHandler.SUB_CATEGORY, 
				new String[]{Keys.SubCategory.PARENT_CATEGORY_ID}, Keys.SubCategory.ID + "=?", 
				new String[]{Integer.toString(subCategoryID)});
		cursor.moveToFirst();
		return cursor.getInt(cursor.getColumnIndex(Keys.SubCategory.PARENT_CATEGORY_ID));
	}
	public List<Question> getQuestionsForSubCategory(Context context, int ID)
	{
		DatabaseHandler db = new DatabaseHandler(context);
		Cursor cursor = db.queryFromTable(DatabaseHandler.SUB_CATEGORY, 
				new String[]{Keys.SubCategory.PLAN}, Keys.SubCategory.ID + "=?", 
				new String[]{Integer.toString(ID)});	
		cursor.moveToFirst();
		
		String questionsJSON = cursor.getString(cursor.getColumnIndex(Keys.SubCategory.PLAN));
		List<Question> result = null;
		try 
		{
			JSONArray questions = new JSONArray(questionsJSON);
			result = new ArrayList<Question>();
			for (int index = 0; index < questions.length(); ++index)
			{
				result.add(new Question(questions.getJSONObject(index)));
			}
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		return result;
	}
	public List<Pair<String, Integer>> getSubCategoryNamesAndIDsForParentCategory(Context context, int parentCategoryID)
	{
		DatabaseHandler db = new DatabaseHandler(context);
		Cursor result = db.queryFromTable(DatabaseHandler.SUB_CATEGORY, 
				new String[]{Keys.SubCategory.NAME, Keys.SubCategory.ID}, Keys.SubCategory.PARENT_CATEGORY_ID + "=?", 
				new String[]{Integer.toString(parentCategoryID)});
		List<Pair<String, Integer>> subCats = new ArrayList<Pair<String, Integer>>();
		while(result.moveToNext())
		{
			String name = result.getString(result.getColumnIndex(Keys.SubCategory.NAME));
			int id = result.getInt(result.getColumnIndex(Keys.SubCategory.ID));
			subCats.add(new Pair<String,Integer>(name,id));
		}
		return subCats;
	}
	public void addSubCategoriesFromJSON(Context context, String json)
	{
		try
		{
			JSONArray subCategories = new JSONArray(json);
			for (int index = 0; index < subCategories.length(); ++index)
			{
				JSONObject subCategory = subCategories.getJSONObject(index);
				int id = subCategory.getInt(Keys.SubCategory.ID);
				String name = subCategory.getString(Keys.SubCategory.NAME);
				String plan = subCategory.getString(Keys.SubCategory.PLAN);
				int parentCategoryID = subCategory.getInt(Keys.SubCategory.PARENT_CATEGORY_ID);
				this.addSubCategory(context, id, name, plan, parentCategoryID);
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}	
	}
	public void deleteSubCategory(Context context, int ID)
	{
		DatabaseHandler db = new DatabaseHandler(context);
		db.deleteEntries(DatabaseHandler.SUB_CATEGORY, Keys.SubCategory.ID + "=?", new String[]{Integer.toString(ID)});	
	}
	public String getTimeOfLastUpdate(Context context)
	{
		return TetherUtils.getTimeOfLastUpdate(context, DatabaseHandler.SUB_CATEGORY);
	}
	public void setTimeOfLastUpdate(Context context, String time)
	{
		TetherUtils.setTimeOfLastUpdate(context, DatabaseHandler.SUB_CATEGORY, time);
	}

	
}
