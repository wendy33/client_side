package com.upenn.trainingtracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class CompactParser 
{
	private static CompactParser instance;
	
	private CompactParser(){}
	
	public static CompactParser getInstance()
	{
		if (CompactParser.instance == null)
		{
			CompactParser.instance = new CompactParser();
		}
		return CompactParser.instance;
	}
	public void parseCompactJSON(Context context, String message)
	{
		try
		{
			JSONObject info = new JSONObject(message);
			String last_updated = info.getString("time");
			
			JSONObject data = info.getJSONObject("data");
			JSONArray deletedParentCategoryIDs = data.getJSONArray("deleted_parent_category_ids");
			JSONArray deletedSubCategoryIDs = data.getJSONArray("deleted_sub_category_ids");
			JSONArray parentCategories = data.getJSONArray("parent_categories");
			JSONArray subCategories = data.getJSONArray("sub_categories");
			
			this.deleteParentIDs(context, deletedParentCategoryIDs);
			this.deleteSubIDs(context, deletedSubCategoryIDs);
			this.addParentCategories(context, parentCategories);
			this.addSubCategories(context, subCategories);
			
			ParentCategoryTether parentTether = ParentCategoryTether.getInstance();
			SubCategoryTether subTether = SubCategoryTether.getInstance();
			parentTether.setTimeOfLastUpdate(context, last_updated);
			subTether.setTimeOfLastUpdate(context, last_updated);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
	private void addParentCategories(Context context, JSONArray parentCategories) throws JSONException
	{
		ParentCategoryTether tether = ParentCategoryTether.getInstance();
		
		for (int index = 0; index < parentCategories.length(); ++index)
		{
			JSONObject parentCat = parentCategories.getJSONObject(index);
			String name = parentCat.getString(Keys.ParentCategory.NAME);
			int id = parentCat.getInt(Keys.ParentCategory.ID);
			tether.addParentCategory(context, id, name);
			Log.i("Category","Added parent category: " + name);
		}
	}
	private void addSubCategories(Context context, JSONArray subCategories) throws JSONException
	{
		SubCategoryTether tether = SubCategoryTether.getInstance();
		for (int inner = 0; inner < subCategories.length(); ++inner)
		{
			JSONObject subCat = subCategories.getJSONObject(inner);
			String name = subCat.getString(Keys.SubCategory.NAME);
			String plan = subCat.getString(Keys.SubCategory.PLAN);
			int parentCategoryID = subCat.getInt(Keys.SubCategory.PARENT_CATEGORY_ID);
			int id = subCat.getInt(Keys.SubCategory.ID);
			tether.addSubCategory(context, id, name, plan, parentCategoryID);
			Log.i("Category","Added sub category: " + name);
		}
	}
	private void deleteParentIDs(Context context, JSONArray parentIDs)
	{
		ParentCategoryTether tether = ParentCategoryTether.getInstance();
		for (int index = 0; index < parentIDs.length(); ++index)
		{
			try {
				tether.deleteParentCategory(context, parentIDs.getInt(index));
				Log.i("Category","Deleted parent category id: " + Integer.toString(parentIDs.getInt(index)));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	private void deleteSubIDs(Context context, JSONArray subIDs)
	{
		SubCategoryTether subTether = SubCategoryTether.getInstance();
		EntryTether entryTether = EntryTether.getInstance();
		for (int index = 0; index < subIDs.length(); ++index)
		{
			try 
			{
				int subCatID = subIDs.getInt(index);
				subTether.deleteSubCategory(context, subCatID);
				entryTether.flushPreviousUnExecutedPlansForSubCategoryID(context, subCatID);
				Log.i("Category","Deleted sub category id: " + Integer.toString(subIDs.getInt(index)));
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
			}
		}
	}

}
