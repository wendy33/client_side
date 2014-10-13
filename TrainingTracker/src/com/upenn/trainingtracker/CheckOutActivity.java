package com.upenn.trainingtracker;



import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.upenn.trainingtracker.customviews.CheckOutProgressView;
import com.upenn.trainingtracker.customviews.PlanningBinLayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class CheckOutActivity extends Activity
{
	private Map<Integer, PlanningBinLayout> subCatIDToView;
	private Map<Integer, List<Question>> subCatIDToQuestions;
	
	private int currentSubCatID;
	private int[] subCatIDs;
	private String[] subCatNames;
	
	private GestureDetector gestureDetector;
	//private CheckOutActivity.MyGestureDetector gestureDetectorInner;
	private int dogID;
	private CheckOutProgressView progressView;
	public static final int RESULT_GET_PLAN = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.check_out_layout);
		View parentView = this.findViewById(R.id.checkOutParentView);
		//this.attachSwipeListener(parentView);
		Bundle extras = this.getIntent().getExtras();
		
		
		SubCategoryTether tether = SubCategoryTether.getInstance();
		this.subCatIDs = extras.getIntArray("subCatIDs");
		this.subCatNames = extras.getStringArray("subCatNames");
		this.subCatIDToQuestions = new HashMap<Integer, List<Question>>();
		if (this.subCatNames == null)
		{
			Log.i("TAG","Sub cat names is null");
		}
		
		this.initializeCheckOutProgressView(subCatIDs.length);
		this.dogID = extras.getInt("dogID");
		this.currentSubCatID = this.subCatIDs[0];
		
		this.initializeLayout();
		
		
		/*
		 * Optional argument for use when the plan is not already stored in the database
		 * Used in two situations: When user changes a plan during a session and when
		 * user plans categories after a session completes
		 */
		String[] plans = extras.getStringArray("plans");
		if (plans != null)
		{
			for (int index = 0; index < plans.length; ++index)
			{
				String plan = plans[index];
				int subCatID = this.subCatIDs[index];
				PlanningBinLayout bin = this.subCatIDToView.get(subCatID);
				bin.setSelectionsWithPlan(plan);
			}
		}
		else
		{
			EntryTether entryTether = EntryTether.getInstance();
			for (int subCatID : this.subCatIDs)
			{
				String plan = entryTether.getPlanBySubCategoryID(this, this.dogID, subCatID);
				if (plan == null) continue;
				PlanningBinLayout bin = this.subCatIDToView.get(subCatID);
				bin.setSelectionsWithPlan(plan);
			}
		}
	}
	private void initializeCheckOutProgressView(int numViews)
	{
		progressView = (CheckOutProgressView) this.findViewById(R.id.checkOutProgressView);
		progressView.initializeCheckOutProgressView(numViews);
	}
	private void initializeLayout()
	{
		Spinner spinner = (Spinner) this.findViewById(R.id.categorySelectorID);
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, this.subCatNames);
		spinner.setAdapter(spinnerArrayAdapter);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View arg1,
					int position, long arg3) {
				int subCatID = CheckOutActivity.this.subCatIDs[position];
				CheckOutActivity.this.switchToViewBySubCatID(subCatID);
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		this.subCatIDToView = new HashMap<Integer, PlanningBinLayout>();
		// Initialize category to entries structure and category to view structure
		for (int subCatID: this.subCatIDs)
		{
			this.subCatIDToView.put(subCatID, this.getViewForCategory(subCatID));
		}
		// Set view to current view
		Log.i("TAG","SWITCHING TO: " + this.subCatIDs[0]);
		this.switchToViewBySubCatID(this.subCatIDs[0]);
	}
	private void recordPlanInformation()
	{
		SharedPreferences preferences = this.getSharedPreferences(MainActivity.USER_PREFS, 0);
		int userID = preferences.getInt(MainActivity.USER_ID, -1);
		
		EntryTether tether = EntryTether.getInstance();
		for (int subCatID : this.subCatIDs)
		{
			PlanningBinLayout binLayout = this.subCatIDToView.get(subCatID);
			String plan = binLayout.getPlanAsJSON();
			String sessionDate = TetherUtils.getCurrentDateTimeString();
			Log.i("TAG",plan);
			tether.addEntry(this, this.dogID, subCatID, userID, plan, sessionDate, null, false);
		}
	}
	public void grabHistory(final View view)
	{
   		Intent intent = new Intent(CheckOutActivity.this, HistoryActivity.class);
   		intent.putExtra("dogID", dogID);
   		intent.putExtra("subCatID", this.currentSubCatID);
   		intent.putExtra("forResult", true);
   		this.startActivityForResult(intent, RESULT_GET_PLAN);
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Log.i("TAG","Result deliverde");
		switch (requestCode)
		{
		case CheckOutActivity.RESULT_GET_PLAN:
			if (resultCode == RESULT_OK)
			{
				// Add the new widget if a new plan was added
				String plan = data.getStringExtra("plan");
				Log.i("TAG","PLAN: " + plan);
				PlanningBinLayout bin = this.subCatIDToView.get(this.currentSubCatID);
				bin.setSelectionsWithPlan(plan);
				Log.i("TAG","Plan: " + plan);
			}
			else
			{
				Log.e("TAG","RESULT NOT OK");
			}
			break;
		}
	}
	public void submitPlan(final View view)
	{
		Log.i("TAG","Submiting info");
		this.recordPlanInformation();
		setResult(RESULT_OK,null);  
		this.finish();
	}
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) 
	    {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Would you like to exit this planning session? All data will be lost");
			builder.setPositiveButton("No", null);
			builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() 
			{
				@Override
				public void onClick(DialogInterface arg0, int arg1) 
				{
			    	setResult(RESULT_CANCELED, null);
					CheckOutActivity.this.finish();
				}
			});
			builder.create().show();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	/*
	 * Methods for rendering the views are below.  Each of the views are first created.  The parent view is a custom
	 * view of the PlanningBinLayout class.  For example, if you selected the categories of Patterns, Over, and Tunnel
	 * then three of these parent views would be created and stored in the categoryToView map (instance variable).
	 * When a sub-category is added, the CheckBox or Spinner is registered with the parent view with the sub-category key
	 * as the identifier.  This allows for efficient harvesting of the information.  The spinners display the options
	 * but we want to harvest the option-keys.  For this reason, both the options and option-keys are given to the 
	 * parent layout.  This way it can translate the selected value to the key value
	 */
	private void switchToViewBySubCatID(int subCatID)
	{
		LinearLayout parentLayout = (LinearLayout) this.findViewById(R.id.checkOutScrollBin);
		parentLayout.removeAllViews();
		View view = this.subCatIDToView.get(subCatID);
		this.currentSubCatID = subCatID;
		parentLayout.addView(view);
		int subCatIndex = this.findIndexOfIntInArray(subCatID, this.subCatIDs);
		this.progressView.setSelected(subCatIndex);
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
	private PlanningBinLayout getViewForCategory(int subCatID)
	{    			
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		PlanningBinLayout viewBinParent = (PlanningBinLayout) inflater.inflate(R.layout.check_out_view_bin, null);
		TableLayout viewBin = (TableLayout) viewBinParent.findViewById(R.id.tableLayoutBin);

		SubCategoryTether subTether = SubCategoryTether.getInstance();
		List<Question> questions = subTether.getQuestionsForSubCategory(this, subCatID);
		
		for (Question question: questions)
		{
			TableRow canvasLayout = null;
			switch (question.getType())
			{
				case CHECKBOX: 
					canvasLayout = (TableRow) this.getCheckBoxFromQuestion(question, viewBinParent);
				break;
				case OPTIONS: 
					canvasLayout = (TableRow) this.getSpinnerFromQuestion(question, viewBinParent);
				break;
			}
			viewBin.addView(canvasLayout);
		}
		
		return viewBinParent;
	}

	private LinearLayout getCheckBoxFromQuestion(Question question, PlanningBinLayout viewBinParent)
	{
		Log.i("TAG","Adding checkbox");
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout checkLayout = (LinearLayout) inflater.inflate(R.layout.options_check_widget, null);
		
		CheckBox check = (CheckBox) checkLayout.findViewById(R.id.optionsCheckWidget);
		viewBinParent.registerCheckBox(question.getQuestion(), check);
				
		TextView text = (TextView) checkLayout.findViewById(R.id.optionsCheckWidgetText);
		text.setText(question.getQuestion());		
		
		return checkLayout;
	}
	private LinearLayout getSpinnerFromQuestion(Question question, PlanningBinLayout viewBinParent)
	{
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.options_widget, null);
		
		TextView text = (TextView) layout.findViewById(R.id.optionsTextID);
		text.setText(question.getQuestion());
		
		Spinner spinner = (Spinner) layout.findViewById(R.id.optionsSpinnerID);
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, question.getAnswers());
		spinner.setAdapter(spinnerArrayAdapter);
		viewBinParent.registerSpinner(question.getQuestion(), spinner);
		
		
		return layout;
	}

//	private void setSpinner(int subCatID)
//	{
//		int index = Arrays.asList(this.subCatIDs).indexOf(subCatID);
//		Spinner spinner = (Spinner) this.findViewById(R.id.categorySelectorID);
//	    spinner.setSelection(index);
//	}
}
