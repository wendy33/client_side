package com.upenn.trainingtracker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.upenn.trainingtracker.customviews.SessionCategoryWidget;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

public class SessionActivity extends Activity
{
	private Map<Integer, SessionCategoryWidget> subCatIDToWidget;
	private int dogID;
	private LinearLayout binLayout;
	private static final int EDIT_PLAN_RESULT = 100;
	
	private int subCatIDBeingEdited;
	private String subCatNameBeingEdited;
	private int indexOfSubCatIDBeingEdited;
	private SessionCategoryWidget widgetBeingEdited;
	
	private List<Integer> subCatIDs;
	private List<String> subCatNames;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.session_layout);
		this.subCatIDToWidget = new HashMap<Integer, SessionCategoryWidget>();
		
		Bundle extras = this.getIntent().getExtras();
		this.subCatIDs = this.intArrayToIntegerList(extras.getIntArray("subCatIDs"));
		this.subCatNames = this.stringArrayToStringList(extras.getStringArray("subCatNames"));
		this.dogID = extras.getInt("dogID");

		
		binLayout = (LinearLayout) this.findViewById(R.id.bin);
		
		for (int index = 0; index < this.subCatIDs.size(); ++index)
		{
			SessionCategoryWidget widget = this.createWidget(
					this.subCatIDs.get(index), this.subCatNames.get(index));
			binLayout.addView(widget);
			this.subCatIDToWidget.put(this.subCatIDs.get(index), widget);
		}
	}
	private List<Integer> intArrayToIntegerList(int[] arr)
	{
		List<Integer> myList = new ArrayList<Integer>();
		for (int num : arr)
		{
			myList.add(num);
		}
		return myList;
	}
	private List<String> stringArrayToStringList(String[] arr)
	{
		List<String> myList = new ArrayList<String>();
		for (String str : arr)
		{
			myList.add(str);
		}
		return myList;
	}
	private SessionCategoryWidget createWidget(int subCatID, String subCatName)
	{
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		EntryTether tether = EntryTether.getInstance();

		SessionCategoryWidget widget = (SessionCategoryWidget) inflater.inflate(R.layout.session_category_widget, null);
		String plan = tether.getPlanBySubCategoryID(this, this.dogID, subCatID);
		
		widget.initializeView(plan, subCatName, subCatID, this);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 10, 0, 10);
		widget.setLayoutParams(params);
		return widget;
	}
	public void collapseAllWidgets()
	{
		for (int subCatID : this.subCatIDs)
		{
			SessionCategoryWidget widget = this.subCatIDToWidget.get(subCatID);
			widget.collapseView();
		}
	}
	public void recordAllCategories(final View view)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(Html.fromHtml(this.getRecordMessage()));
    	builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
    	{
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				String[] plans = new String[subCatIDs.size()];
				for (int index = 0; index < subCatIDs.size(); ++index)
				{
					int subCatID = subCatIDs.get(index);
					SessionCategoryWidget widget = subCatIDToWidget.get(subCatID);
					plans[index] = widget.getPlanString();
					if (widget.isStarted())
					{
						SessionActivity.this.recordWidget(subCatID);
					}
				}
				SessionActivity.this.finish();
				SessionActivity.this.launchCheckOutActivity(plans);
			}
    	});
    	builder.setNegativeButton("No", null);
    	builder.create().show();	
	}
	private void launchCheckOutActivity(String[] plans)
	{

		Intent intent = new Intent(this, CheckOutActivity.class);

		int[] subCatIDs = new int[this.subCatIDs.size()];
		String[] subCatNames = new String[this.subCatNames.size()];
		for (int index = 0; index < this.subCatIDs.size(); ++index)
		{
			subCatIDs[index] = this.subCatIDs.get(index);
			subCatNames[index] = this.subCatNames.get(index);
		}

		intent.putExtra("subCatIDs", subCatIDs);
		intent.putExtra("subCatNames", subCatNames);
		intent.putExtra("dogID", this.dogID);
		intent.putExtra("plans", plans);
		
		this.startActivity(intent);
	}
	public String getRecordMessage()
	{
		List<Integer> incomplete = new ArrayList<Integer>();
		List<Integer> notStarted = new ArrayList<Integer>();
		for(int subCatID : this.subCatIDs)
		{
			SessionCategoryWidget widget = this.subCatIDToWidget.get(subCatID);
			if (!widget.isStarted())
			{
				notStarted.add(subCatID);
			}
			else if (!widget.isCompleted())
			{
				incomplete.add(subCatID);
			}
		}
		String message = "";
		if (!incomplete.isEmpty())
		{
			message = "<b>If you submit now the following categories will be tagged as aborted:</b> <br>";
			for (int subCatID : incomplete)
			{
				String subCatName = this.subCatNames.get(this.subCatIDs.indexOf(subCatID));
				message += subCatName + " <br>";
			}
			message += "<br>";
		}
		if (!notStarted.isEmpty())
		{
			message += "<b>If you submit now the following categories will be deleted</b>: <br>";
			for (int subCatID : notStarted)
			{
				String subCatName = this.subCatNames.get(this.subCatIDs.indexOf(subCatID));
				message += subCatName + " <br>";
			}
			message += "<br>";
		}
		if (!notStarted.isEmpty() || !incomplete.isEmpty())
		{
			message += "Are you sure you want to continue?";
		}
		else
		{
			message = "Would you like to submit this session?";
		}
		return message;
	}
	private int findIndexOfIntInArray(int num, int[] array)
	{
		for (int index = 0; index < array.length; ++index)
		{
			if (array[index] == num)
			{
				return index;
			}
		}
		return -1;
	} 
	public void recordWidget(int subCatID)
	{
		SessionCategoryWidget widget = this.subCatIDToWidget.get(subCatID);
		EntryTether tether = EntryTether.getInstance();
		
		SharedPreferences preferences = this.getSharedPreferences(MainActivity.USER_PREFS, 0);
		int userID = preferences.getInt(MainActivity.USER_ID, -1);
		
		String plan = widget.getPlanString();
		String sessionDate = TetherUtils.getCurrentDateTimeString();
		String trialsResult = widget.getResultSequenceAsString();
		
		tether.addEntry(this, this.dogID, subCatID, userID, plan, sessionDate, trialsResult, false);
	}
	/*
	 * Called from the widget when the user selects edit icon
	 */
	public void editPlan(int subCatID)
	{
		SessionCategoryWidget widget = this.subCatIDToWidget.get(subCatID);
		this.subCatIDBeingEdited = subCatID;
		String subCatName = this.subCatNames.get(this.subCatIDs.indexOf(subCatID));
		this.subCatNameBeingEdited = subCatName;
		this.widgetBeingEdited = widget;
		this.indexOfSubCatIDBeingEdited = this.subCatIDs.indexOf(subCatID);
		
		this.removeWidget(subCatID);
		String planString = this.widgetBeingEdited.getPlanString();		
		if (this.widgetBeingEdited.isStarted())
		{
			this.recordWidget(subCatID);
		}
		Intent intent = new Intent(this, CheckOutActivity.class);
		
		int[] subCatIDs = {subCatID};
		String[] subCatNames = {subCatName};
		String[] plans = {planString};
		
		intent.putExtra("subCatIDs", subCatIDs);
		intent.putExtra("subCatNames", subCatNames);
		intent.putExtra("dogID", this.dogID);
		intent.putExtra("plans", plans);

		this.startActivityForResult(intent, SessionActivity.EDIT_PLAN_RESULT);
	}
	private String getCurrentDateString()
	{
		Calendar c = Calendar.getInstance(); 
		int month = c.get(Calendar.MONTH) + 1;
		int day = c.get(Calendar.DAY_OF_MONTH);
		int year = c.get(Calendar.YEAR);
		
		int hours = c.get(Calendar.HOUR_OF_DAY);
		int minutes = c.get(Calendar.MINUTE);
		int second = c.get(Calendar.SECOND);
		return month + "-" + day + "-" + year + "-" + hours + ":" + minutes + ":" + second;
	}
	public void removeWidget(int subCatID)
	{
		SessionCategoryWidget widget = this.subCatIDToWidget.get(subCatID);
		this.binLayout.removeView(widget);
		this.subCatIDToWidget.remove(subCatID);
		Log.i("TAG","INDEX TO BE REMOVED: " + this.subCatIDs.indexOf(subCatID));
		Log.i("TAG","SIZE OF SUBCATnAMES: " + this.subCatNames.size());
		this.subCatNames.remove(this.subCatIDs.indexOf(subCatID));
		this.subCatIDs.remove(this.subCatIDs.indexOf(subCatID));
	}
	public void addNewWidgetAfterEdit()
	{
		SessionCategoryWidget widget = this.createWidget(this.subCatIDBeingEdited, this.subCatNameBeingEdited);
		this.subCatIDToWidget.put(this.subCatIDBeingEdited, widget);
		this.subCatIDs.add(this.indexOfSubCatIDBeingEdited, this.subCatIDBeingEdited);
		this.subCatNames.add(this.indexOfSubCatIDBeingEdited, this.subCatNameBeingEdited);
		this.refreshBinLayout();
	}
	public void refreshBinLayout()
	{
		this.binLayout.removeAllViews();
		for (int subCatID : this.subCatIDs)
		{
			SessionCategoryWidget widget = this.subCatIDToWidget.get(subCatID);
			this.binLayout.addView(widget);
		}
		this.binLayout.invalidate();
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Log.i("TAG","Result deliverde");
		switch (requestCode)
		{
		case SessionActivity.EDIT_PLAN_RESULT:
			// Add the new widget if a new plan was added
			if (resultCode == RESULT_OK)
			{
				this.addNewWidgetAfterEdit();
			}
			else
			{
				// Default plan - same as original, was added before
				// so always add plan
				this.addNewWidgetAfterEdit();
			}
			this.subCatIDBeingEdited = -1;
			this.widgetBeingEdited = null;
			break;
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) 
	    {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Would you like to exit this training session? All training data will be lost");
			builder.setPositiveButton("No", null);
			builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() 
			{
				@Override
				public void onClick(DialogInterface arg0, int arg1) 
				{
					SessionActivity.this.finish();
				}
			});
			builder.create().show();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
    /**
     * Event Handling for Individual menu item selected
     * Identify single menu item by it's id
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	 switch (item.getItemId())
         {
         case R.id.collapseAllID:
        	 this.collapseAllWidgets();
         default:
             return super.onOptionsItemSelected(item);
         }
    }
     // Initiating Menu XML file (menu.xml)
	 @Override
	 public boolean onCreateOptionsMenu(Menu menu)
	 {
	     MenuInflater menuInflater = getMenuInflater();
	     menuInflater.inflate(R.menu.session_menu, menu);
	     return true;
	 }
	
}
