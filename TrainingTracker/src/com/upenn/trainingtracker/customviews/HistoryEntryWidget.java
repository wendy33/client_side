package com.upenn.trainingtracker.customviews;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.upenn.trainingtracker.ImageManager;
import com.upenn.trainingtracker.PlanEntry;
import com.upenn.trainingtracker.R;
import com.upenn.trainingtracker.ViewUtils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class HistoryEntryWidget extends LinearLayout implements Comparable<HistoryEntryWidget>
{
	private LinearLayout planLayout;
	private TableLayout table;
	private boolean planVisible;
	private LinearLayout resultsBin;
	private HistoryEntryWidget.Type type;
	private Calendar date;
	
	private boolean planTableInitialized;
	private String plan;
	
	
	public enum Type 
	{
		Passed, Failed, Aborted, Planned
	}
	
	public HistoryEntryWidget(Context context)
	{
		super(context);
	}
	public HistoryEntryWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public HistoryEntryWidget(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}
	public String getPlanString()
	{
		//TrainingInfoTether tether = TrainingInfoTether.getInstance();
		//return tether.planMapToPlanString(this.planMap);
		return this.plan;
	}
	public void initializeView(String plan, String sessionDate, String trialsResult, 
			String subCatName, String userFullName)
	{
		this.resultsBin = (LinearLayout) this.findViewById(R.id.resultsBin);
		
		this.plan = plan;
		this.setTrainerName(userFullName);
		this.setDateText(sessionDate);
		this.setSubCatName(subCatName);
		this.setTrialsResultButtons(trialsResult);
		
		this.date = new GregorianCalendar();
		try 
		{
			this.date.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sessionDate));
		} catch (ParseException e) 
		{
			e.printStackTrace();
		}
		
		this.setCollapseBehavior();
	}
	public HistoryEntryWidget.Type getType()
	{
		return this.type;
	}
	public void setTrainerName(String trainerName)
	{
		TextView trainerNameView = (TextView) this.findViewById(R.id.trainerName);
		trainerNameView.setText(trainerName);
	}
	public void setDateText(String dateTime)
	{
		TextView dateView = (TextView) this.findViewById(R.id.date);
		dateView.setText(dateTime);
	}
	public void setSubCatName(String subCatName)
	{
		TextView textView = (TextView) this.findViewById(R.id.title);
		textView.setText(subCatName);
	}
	private void setTrialsResultButtons(String trialsResult)
	{
		List<Boolean> resultSequence = this.parseBoolSequence(trialsResult);
		Log.i("TAG","RESULT LENGTH: " + resultSequence.size());
		int numSuccess = 0;
		for (Boolean sf : resultSequence)
		{
			if (sf) ++numSuccess;
			this.addSuccessFailureButton(sf);
		}
		if (numSuccess >= 4) // success
		{
			type = Type.Passed;
			this.setBackground(this.getResources().getDrawable(R.drawable.history_widget_passed));
		}
		else if (numSuccess < 4 && resultSequence.size() == 5) // failed
		{
			type = Type.Failed;
			this.setBackground(this.getResources().getDrawable(R.drawable.history_widget_failed));
		}
		else if (numSuccess < 4 && resultSequence.size() > 0) // aborted
		{
			type = Type.Aborted;
			this.setBackground(this.getResources().getDrawable(R.drawable.history_widget_aborted));
		}
		else if (resultSequence.size() == 0) // planned
		{
			type = Type.Planned;
			this.setBackground(this.getResources().getDrawable(R.drawable.history_widget_planned));
		}
		int padding = (int)ViewUtils.convertDpToPixel(10, this.getContext());
		this.setPadding(padding, padding, padding, padding);
	}
	private List<Boolean> parseBoolSequence(String boolSequence)
	{
		List<Boolean> resultSequence = new ArrayList<Boolean>();
		for (int index = 0; index < boolSequence.length(); ++index)
		{
			resultSequence.add(boolSequence.charAt(index) == '1' ? true : false);
		}
		return resultSequence;
	}
	private void addSuccessFailureButton(boolean sf)
	{
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
	}
	private void initializePlanTable()
	{
		//TrainingReader reader = TrainingReader.getInstance(this.getContext());
		//Map<String, PlanEntry> keyToEntry = reader.getViewCompositionMapByCategoryKey(catKey);
		
		//Iterator<String> iter = planMap.keySet().iterator();
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

		this.removeView(this.planLayout);
		this.invalidate();
	}
	private void setCollapseBehavior()
	{
		TextView text = (TextView) this.findViewById(R.id.collapseID);
		text.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view) 
			{
				if (HistoryEntryWidget.this.planVisible)
				{
					HistoryEntryWidget.this.collapseView();
				}
				else
				{
					HistoryEntryWidget.this.expandView();
				}
				Log.i("TAG", (HistoryEntryWidget.this.planVisible ? "Visible" : "Hidden"));
			}	
		});
		
	}
	public void collapseView()
	{
		if (!this.planTableInitialized)
		{
			this.initializePlanTable();
			this.planTableInitialized = true;
		}
		if (!this.planVisible)
		{
			return;
		}
		this.removeView(planLayout);
		this.planVisible = false;
		this.invalidate();
	}
	public void expandView()
	{
		if (!this.planTableInitialized)
		{
			this.initializePlanTable();
			this.planTableInitialized = true;
		}
		if (this.planVisible)
		{
			return;
		}
		this.addView(planLayout);
		this.planVisible = true;
		this.invalidate();
	}
	@Override
	public int compareTo(HistoryEntryWidget otherWidget) 
	{
		return this.date.compareTo(otherWidget.date) * -1;
	}
}
