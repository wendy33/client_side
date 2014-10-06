package com.upenn.trainingtracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.upenn.trainingtracker.customviews.HistoryEntryWidget;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;

public class EntryTether 
{
	private static EntryTether instance;
	private Context context;
	
	private EntryTether() {}
	public static EntryTether getInstance()
	{
		if (instance == null)
		{
			instance = new EntryTether();
		}
		return instance;
	}	
	public void markAllAsSynced()
	{
		DatabaseHandler db = new DatabaseHandler(context);
		ContentValues values = new ContentValues();
		values.put(Keys.Entry.SYNCED, 1);
		db.updateTable(DatabaseHandler.ENTRY, values, Keys.Entry.SYNCED + "=?", new String[]{"0"});
	}
	public void addEntry(Context context, int dogID, int subCatID, int userID,
			String plan, String sessionDate, String trialsResult, boolean synced)
	{
		if (trialsResult == null)
		{
			this.flushPreviousUnExecutedPlans(context, dogID, subCatID);
			trialsResult = "NONE";
		}
		ContentValues values = new ContentValues();
		values.put(Keys.Entry.DOG_ID, dogID);
		values.put(Keys.Entry.SUB_CATEGORY_ID, subCatID);
		values.put(Keys.Entry.USER_ID, userID);
		values.put(Keys.Entry.PLAN, plan);
		values.put(Keys.Entry.SESSION_DATE, sessionDate);
		values.put(Keys.Entry.TRIALS_RESULT, trialsResult);
		values.put(Keys.Entry.SYNCED, synced);
	
		DatabaseHandler db = new DatabaseHandler(context);
		try
		{
			db.insertIntoTable(DatabaseHandler.ENTRY, null, values);
		}
		catch(Exception e)
		{
			Log.e("E",e.getMessage());
		}
	}
	public void changeEntryIDs(String json)
	{
		DatabaseHandler db = new DatabaseHandler(this.context);
		try 
		{
			JSONObject idData = new JSONObject(json);
			JSONArray oldIDs = idData.getJSONArray("old_ids");
			JSONArray newIDs = idData.getJSONArray("new_ids");
			for (int index = 0; index < oldIDs.length(); ++index)
			{
				this.changeEntryID(db, oldIDs.getInt(index), newIDs.getInt(index));
			}
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		
	}
	public void addEntry(Context context, int id, int dogID, int subCatID, int userID,
			String plan, String sessionDate, String trialsResult, boolean synced)
	{
		if (trialsResult == null)
		{
			this.flushPreviousUnExecutedPlans(context, dogID, subCatID);
			trialsResult = "NONE";
		}
		ContentValues values = new ContentValues();
		values.put(Keys.Entry.ID, id);
		values.put(Keys.Entry.DOG_ID, dogID);
		values.put(Keys.Entry.SUB_CATEGORY_ID, subCatID);
		values.put(Keys.Entry.USER_ID, userID);
		values.put(Keys.Entry.PLAN, plan);
		values.put(Keys.Entry.SESSION_DATE, sessionDate);
		values.put(Keys.Entry.TRIALS_RESULT, trialsResult);
		values.put(Keys.Entry.SYNCED, synced);
	
		DatabaseHandler db = new DatabaseHandler(context);
		db.insertIntoTable(DatabaseHandler.ENTRY, null, values);
	}
	private void flushPreviousUnExecutedPlans(Context context, int dogID, int subCatID)
	{
		DatabaseHandler db = new DatabaseHandler(context);
		db.deleteEntries(DatabaseHandler.ENTRY, 
				Keys.Entry.DOG_ID + "=?" + " AND " + 
						Keys.Entry.SUB_CATEGORY_ID + "=?" + " AND " + 
						Keys.Entry.TRIALS_RESULT + "=?", 
				new String[]{Integer.toString(dogID), Integer.toString(subCatID), "NONE"});
	}
	public void flushPreviousUnExecutedPlansForSubCategoryID(Context context, int subCatID)
	{
		DatabaseHandler db = new DatabaseHandler(context);
		db.deleteEntries(DatabaseHandler.ENTRY, 
				Keys.Entry.SUB_CATEGORY_ID + "=?" + " AND " + 
						Keys.Entry.TRIALS_RESULT + "=?", 
				new String[]{Integer.toString(subCatID), "NONE"});
	}
	public List<Pair<Integer, String>> getPlannedSubCategories(Context context, int dogID)
	{
		this.context = context;
		DatabaseHandler db = new DatabaseHandler(context);
		Cursor entryResult = db.queryFromTable(DatabaseHandler.ENTRY, 
				new String[]{Keys.Entry.SUB_CATEGORY_ID}, 
					Keys.Entry.DOG_ID + "=?" + " AND " + 
					Keys.Entry.TRIALS_RESULT + "+?", 
				new String[]{Integer.toString(dogID), "NONE"});
		SubCategoryTether subTether = SubCategoryTether.getInstance();
		List<Pair<Integer, String>> plannedCategories = new ArrayList<Pair<Integer, String>>();
		while (entryResult.moveToNext())
		{
			int subCatID = entryResult.getInt(entryResult.getColumnIndex(Keys.Entry.SUB_CATEGORY_ID));
			String subCatName = subTether.subCategoryIDToName(context, subCatID);
			plannedCategories.add(new Pair(subCatID, subCatName));
		}
		return plannedCategories;
	}
	public String getPlanBySubCategoryID(Context context, int dogID, int subCatID)
	{
		DatabaseHandler db = new DatabaseHandler(context);
		Cursor result = db.queryFromTable(DatabaseHandler.ENTRY, 
				new String[]{Keys.Entry.PLAN}, 
					Keys.Entry.DOG_ID + "=?" + " AND " + 
							Keys.Entry.SUB_CATEGORY_ID + "=?" + " AND " + 
							Keys.Entry.TRIALS_RESULT + "=?", 
				new String []{Integer.toString(dogID), Integer.toString(subCatID), "NONE"});
		if (result.getCount() == 0) return null;
		result.moveToFirst();
		return result.getString(result.getColumnIndex(Keys.Entry.PLAN));
	}
	public void addEntriesFromJSON(Context context, String JSON)
	{
		try
		{
			JSONObject input = new JSONObject(JSON);
			String updateTime = input.getString("time");
			this.setTimeOfLastUpdate(context, updateTime);
			
			JSONObject data = input.getJSONObject("data");
			
			JSONArray deletedEntryIDs = data.getJSONArray("deleted_entry_ids");
			for (int index = 0; index < deletedEntryIDs.length(); ++index)
			{
				this.deleteEntryByID(context, deletedEntryIDs.getInt(index));
			}
			
			JSONArray entries = data.getJSONArray("entries");
			for (int index = 0; index < entries.length(); ++index)
			{
				JSONObject dog = entries.getJSONObject(index);
				int id = dog.getInt(Keys.Entry.ID);
				int dogID = dog.getInt(Keys.Entry.DOG_ID);
				int subCatID = dog.getInt(Keys.Entry.SUB_CATEGORY_ID);
				int userID = dog.getInt(Keys.Entry.USER_ID);
				String plan = dog.getString(Keys.Entry.PLAN);
				String sessionDate = dog.getString(Keys.Entry.SESSION_DATE);
				String trialsResult = dog.getString(Keys.Entry.TRIALS_RESULT);
				boolean synced = true;
				try
				{
	            	this.addEntry(context, id, dogID, subCatID, userID, 
	            			plan, sessionDate, trialsResult, synced);
				}
				catch(Exception e)
				{
					Log.e("TAG", e.getMessage());
				}
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}	
	}
	public void deleteEntryByID(Context context, int entryID)
	{
		DatabaseHandler db = new DatabaseHandler(context);
		db.deleteEntries(DatabaseHandler.ENTRY, Keys.Entry.ID + "=?", new String[]{Integer.toString(entryID)});
	}
	public void changeEntryID(DatabaseHandler db, int originalID, int updatedID)
	{
		ContentValues values = new ContentValues();
		values.put(Keys.Entry.ID, updatedID);
		db.updateTable(DatabaseHandler.ENTRY, values, Keys.Entry.ID + "=?", 
				new String[]{Integer.toString(originalID)});
	}
	public void changeMultipleEntryIDS(Context context, Map<Integer, Integer> idMap)
	{
		DatabaseHandler db = new DatabaseHandler(context);
		for (Map.Entry<Integer, Integer> entry : idMap.entrySet())
		{
			this.changeEntryID(db, entry.getKey(), entry.getValue());
		}
	}
	public JSONArray getEntriesNotPushed(Context context)
	{
		this.context = context;
		DatabaseHandler db = new DatabaseHandler(context);
		Cursor cursor = db.queryFromTable(DatabaseHandler.ENTRY, 
				new String[]{Keys.Entry.ID, Keys.Entry.DOG_ID,
					Keys.Entry.PLAN, Keys.Entry.SESSION_DATE,
					Keys.Entry.SUB_CATEGORY_ID, Keys.Entry.TRIALS_RESULT,
					Keys.Entry.USER_ID}, 
					Keys.Entry.SYNCED + "=?", 
					new String[]{"0"});
		JSONArray rows = TetherUtils.cursorToJSON(cursor, 
				new String[]{Keys.Entry.PLAN,
					Keys.Entry.SESSION_DATE, Keys.Entry.TRIALS_RESULT}, 
				new String[]{Keys.Entry.ID, Keys.Entry.DOG_ID, Keys.Entry.SUB_CATEGORY_ID,
					Keys.Entry.USER_ID});
		return rows;
	}

	public String getTimeOfLastUpdate(Context context)
	{
		return TetherUtils.getTimeOfLastUpdate(context, DatabaseHandler.ENTRY);
	}
	public void setTimeOfLastUpdate(Context context, String time)
	{
		TetherUtils.setTimeOfLastUpdate(context, DatabaseHandler.ENTRY, time);
	}
	//=============================== WIDGET GENERATION ===============================
	public HistoryMapper getHistoryWidgetsForDogID(Context context, int dogID, int subCatID, boolean planSelectable, HistoryActivity activity)
	{
		HistoryMapper mapper = new HistoryMapper();
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		SubCategoryTether subTether = SubCategoryTether.getInstance();
		ParentCategoryTether parentTether = ParentCategoryTether.getInstance();
		
		UserTether userTether = UserTether.getInstance();
		
		DatabaseHandler db = new DatabaseHandler(context);
		Cursor result;
		if (subCatID == -1)
		{
			result = db.queryFromTable(DatabaseHandler.ENTRY, 
					new String[]{Keys.Entry.PLAN,  Keys.Entry.SESSION_DATE, 
						Keys.Entry.SUB_CATEGORY_ID, Keys.Entry.TRIALS_RESULT, 
						Keys.Entry.USER_ID}, 
					Keys.Entry.DOG_ID + "=?",new String[]{Integer.toString(dogID)});
		}
		else
		{
			result = db.queryFromTable(DatabaseHandler.ENTRY, 
					new String[]{Keys.Entry.PLAN,  Keys.Entry.SESSION_DATE, 
						Keys.Entry.SUB_CATEGORY_ID, Keys.Entry.TRIALS_RESULT, 
						Keys.Entry.USER_ID}, 
					Keys.Entry.DOG_ID + "=?" + " AND " + Keys.Entry.SUB_CATEGORY_ID + "=?",
					new String[]{Integer.toString(dogID), Integer.toString(subCatID)});
		}
		while (result.moveToNext())
		{
			String plan = result.getString(result.getColumnIndex(Keys.Entry.PLAN));
			String sessionDate = result.getString(result.getColumnIndex(Keys.Entry.SESSION_DATE));
			String trialsResult = result.getString(result.getColumnIndex(Keys.Entry.TRIALS_RESULT));
			int subCategoryID = result.getInt(result.getColumnIndex(Keys.Entry.SUB_CATEGORY_ID));
			int parentCategoryID = subTether.getParentCategoryIDForSubCategoryID(context, subCategoryID);
			String parentCategoryName = parentTether.getParentCategoryNameFromID(context, parentCategoryID);
			
			int userID = result.getInt(result.getColumnIndex(Keys.Entry.USER_ID));
			String subCatName = subTether.subCategoryIDToName(context, subCategoryID);
			String userFullName = userTether.getUserFullNameFromUserID(context, userID);
			String userName = userTether.getUserNameFromUserID(context, userID);
			
			HistoryEntryWidget entryWidget = (HistoryEntryWidget) inflater.inflate(R.layout.history_widget, null);
			entryWidget.initializeView(plan, sessionDate, trialsResult, subCatName, userFullName);
			HistoryEntryWidget.Type type = entryWidget.getType();
			mapper.addWidgetToMaps(entryWidget, subCatName, parentCategoryName, userName, userFullName, type);
			
			if (planSelectable)
			{
				this.wirePlanSelectionToParentActivity(entryWidget, activity);
			}
		}
		return mapper;
	}
	public void wirePlanSelectionToParentActivity(final HistoryEntryWidget widget, final HistoryActivity activity)
	{
		widget.setOnLongClickListener(new OnLongClickListener()
		{
			@Override
			public boolean onLongClick(View arg0) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setMessage("Would you like to copy this plan?");
				builder.setPositiveButton("No", null);
				builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() 
				{
					@Override
					public void onClick(DialogInterface arg0, int arg1) 
					{
						String planString = widget.getPlanString();
						activity.returnPlanResult(planString);
					}
				});
				builder.create().show();
				return false;
			}
		});
	}
}
