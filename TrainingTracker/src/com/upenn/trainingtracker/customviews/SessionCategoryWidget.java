package com.upenn.trainingtracker.customviews;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.upenn.trainingtracker.ImageManager;
import com.upenn.trainingtracker.PlanEntry;
import com.upenn.trainingtracker.R;
import com.upenn.trainingtracker.SessionActivity;
import com.upenn.trainingtracker.ViewUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

public class SessionCategoryWidget extends LinearLayout
{
	private boolean planVisible = false;
	private LinearLayout planLayout;
	private TableLayout table;
	private LinearLayout resultsBin;
	private SessionActivity activity;
	private int subCatID;
	private String planString;
	
	private List<View> resultViews = new ArrayList<View>();
	private List<Boolean> resultSequence = new ArrayList<Boolean>();
	
	public SessionCategoryWidget(Context context)
	{
		super(context);
	}
	public List<Boolean> getResultSequence()
	{
		return this.resultSequence;
	}
	public String getResultSequenceAsString()
	{
		String result = "";
		for (Boolean bool : this.resultSequence)
		{
			result += bool ? "1" : "0";
		}
		return result;
	}
	public SessionCategoryWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public SessionCategoryWidget(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}
	public void initializeView(String plan, String subCatName,int subCatID, SessionActivity activity)
	{
		this.setCollapseBehavior();
		this.setEditPlanBehavior();
		this.initializePlanTable(plan);
		this.setSuccessFailureButtons();
		this.setDeleteLastButton();
		this.planString = plan;
		
		TextView titleText = (TextView) this.findViewById(R.id.title);
		titleText.setText(subCatName);
		this.setCompleteDeleteBehavior();
		this.activity = activity;
		this.subCatID = subCatID;
		//TrainingInfoTether tether = TrainingInfoTether.getInstance();
		//this.planString = tether.planMapToPlanString(planMap);
	}
	public String getPlanString()
	{
		return this.planString;
	}
	private void setEditPlanBehavior()
	{
		ImageView changePlan = (ImageView) this.findViewById(R.id.editPlanButton);
		if (changePlan == null)
		{
			Log.i("TAG","it is null");
		}
		changePlan.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		    	builder.setMessage("Would you like to edit this plan? Current trial data will still be recorded.");
		    	builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
		    	{
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						SessionCategoryWidget.this.editPlan();
					}
		    	});
		    	builder.setNegativeButton("No", null);
		    	builder.create().show();		
			}
		});
	}
	private void editPlan()
	{
		this.activity.editPlan(this.subCatID);
	}
	private void setCompleteDeleteBehavior()
	{
		this.setOnLongClickListener(new OnLongClickListener()
		{
			@Override
			public boolean onLongClick(View arg0) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		    	builder.setMessage("Would you like to delete this category from the training session? All recorded trials will be lost.");
		    	builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
		    	{
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{

					}
		    	});
		    	builder.setNegativeButton("No", null);
		    	builder.create().show();		
				return false;
			}
		});
	}
	private void deleteLastTrial()
	{
    	AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
    	builder.setMessage("Would you like to delete the last trial?");
    	builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
    	{
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				if (resultViews.isEmpty()) return;
				int index = resultViews.size() - 1;
				SessionCategoryWidget.this.resultsBin.removeView(resultViews.get(index));
				resultViews.remove(resultViews.size() - 1);
				resultSequence.remove(resultSequence.size() - 1);
				SessionCategoryWidget.this.invalidate();
			}
    	});
    	builder.setNegativeButton("No", null);
    	builder.create().show();		
	}
	private void setDeleteLastButton()
	{
		Button deleteLastButton = (Button) this.findViewById(R.id.delete);
		deleteLastButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				SessionCategoryWidget.this.deleteLastTrial();
			}
		});
	}
	private void addSuccessFailureButton(boolean sf)
	{
		if (resultSequence.size() == 5)
		{
			Toast.makeText(this.getContext(), "Maximum of 5 trials are allowed", Toast.LENGTH_LONG).show();
			return;
		}
		Button button = null;
		final LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (sf) // success
		{
			button = (Button) inflater.inflate(R.layout.session_success_button, null);
		}
		else
		{
			button = (Button) inflater.inflate(R.layout.session_failure_button, null);
		}
		int dim = (int) ViewUtils.convertDpToPixel(20, getContext());
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dim, dim);
		params.setMargins(0, 0, (int) ViewUtils.convertDpToPixel(2, getContext()), 0);
		button.setLayoutParams(params);
		resultsBin.addView(button);
		resultSequence.add(sf);
		resultViews.add(button);
	}
	private void setSuccessFailureButtons()
	{
		this.resultsBin = (LinearLayout) this.findViewById(R.id.resultsBin);

		Button successButton = (Button) this.findViewById(R.id.success);
		Button failureButton = (Button) this.findViewById(R.id.failure);
		
		successButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0) 
			{
				SessionCategoryWidget.this.addSuccessFailureButton(true);
			}
		});
		failureButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0) 
			{
				SessionCategoryWidget.this.addSuccessFailureButton(false);
			}
		});
	}
	private void initializePlanTable(String plan)
	{		
		LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.table = (TableLayout) this.findViewById(R.id.tableID);
		this.planLayout = (LinearLayout) this.findViewById(R.id.planLayout);
		
		try
		{
			JSONArray questions = new JSONArray(plan);
			for (int index = 0; index < questions.length(); ++index)
			{
				JSONObject question = questions.getJSONObject(index);
				String questionText = question.getString("question");
				String answerText = question.getString("answer");
				String type = question.getString("type");
				
				TableRow row = (TableRow) inflater.inflate(com.upenn.trainingtracker.R.layout.session_table_row, null);
				TextView questionView = (TextView) row.findViewById(R.id.key);
				TextView answerView = (TextView) row.findViewById(R.id.value);
				questionView.setText(questionText);
				if (type.equals("c"))
				{
					answerView.setText(answerText.equals("1") ? "True" : "False");
				}
				else
				{
					answerView.setText(answerText);
				}
				this.table.addView(row);
			}
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		SessionCategoryWidget.this.removeView(this.planLayout);
		SessionCategoryWidget.this.invalidate();
	}
	private void setCollapseBehavior()
	{
		TextView text = (TextView) this.findViewById(R.id.collapseID);
		text.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view) 
			{
				if (SessionCategoryWidget.this.planVisible)
				{
					SessionCategoryWidget.this.collapseView();
				}
				else
				{
					SessionCategoryWidget.this.explandView();
				}
				Log.i("TAG", (SessionCategoryWidget.this.planVisible ? "Visible" : "Hidden"));
			}	
		});
		
	}
	public void collapseView()
	{
		if (!this.planVisible)
		{
			return;
		}
		this.removeView(planLayout);
		this.planVisible = false;
		this.invalidate();
	}
	public void explandView()
	{
		if (this.planVisible)
		{
			return;
		}
		SessionCategoryWidget.this.addView(planLayout);
		this.planVisible = true;
		this.invalidate();
	}
	public boolean isStarted()
	{
		return this.resultSequence.size() > 0;
	}
	public boolean isCompleted()
	{
		return this.resultSequence.size() == 5;
	}
	
}
