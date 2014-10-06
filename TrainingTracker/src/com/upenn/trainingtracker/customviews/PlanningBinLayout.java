package com.upenn.trainingtracker.customviews;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;

public class PlanningBinLayout extends ScrollView
{
	private Map<String, CheckBox> checkBoxMap;
	private Map<String, Spinner> spinnerMap;
	
	public PlanningBinLayout(Context context)
	{
		super(context);
		this.init();
	}
	public PlanningBinLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.init();
	}
	private void init()
	{
		this.checkBoxMap = new HashMap<String, CheckBox>();
		this.spinnerMap = new HashMap<String, Spinner>();
	}
	
	public void registerCheckBox(String question, CheckBox check)
	{
		this.checkBoxMap.put(question, check);
	}
	public void registerSpinner(String question, Spinner spinner)
	{
		this.spinnerMap.put(question, spinner);
	}
	public void setSpinnerByQuestion(String question, String selection)
	{
		Spinner spinner = this.spinnerMap.get(question);
		if (spinner == null)
		{
			Log.i("TAG","Spinner not found for question: " + question);
			return;
		}
		int position = ((ArrayAdapter)spinner.getAdapter()).getPosition(selection);
		Log.i("TAG","Setting position: " + position);
		spinner.setSelection(position);
	}
	public void setCheckBoxByQuestion(String question, boolean value)
	{
		CheckBox checkBox = this.checkBoxMap.get(question);
		if (checkBox == null)
		{
			Log.i("TAG", "Checkbox not found for question: " + question);
			return;
		}
		Log.i("TAG","Setting selecte to : " + value);
		checkBox.setChecked(value);
		Log.i("TAG","Is it checked: " + checkBox.isChecked());
		checkBox.refreshDrawableState();
	}
	public void setSelectionsWithPlan(String plan)
	{
		try
		{
			JSONArray questions = new JSONArray(plan);
			for (int index = 0; index < questions.length(); ++index)
			{
				JSONObject question = questions.getJSONObject(index);
				String questionText = question.getString("question");
				String answerText = question.getString("answer");
				Log.i("TAG","SETTING SELECTIONS^^^^^^^^^^");
				Log.i("TAG","QUESTION: " + questionText);
				Log.i("TAG","ANSWER: " + answerText);
				String type = question.getString("type");
				if (type.equals("c"))
				{
					this.setCheckBoxByQuestion(questionText, answerText.equals("1") ? true : false);
				}
				else
				{
					this.setSpinnerByQuestion(questionText, answerText);
				}
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		this.invalidate();
	}
	public String getPlanAsJSON()
	{
		JSONArray plan = new JSONArray();
		try
		{
			Iterator<String> iter = this.checkBoxMap.keySet().iterator();
			while (iter.hasNext())
			{
				JSONObject object = new JSONObject();
				String question = iter.next();
				String answer = this.getCheckBoxValueByQuestion(question);
				object.put("question", question);
				object.put("type", "c");
				object.put("answer", answer);
				plan.put(object);
			}
			iter = this.spinnerMap.keySet().iterator();
			while (iter.hasNext())
			{
				JSONObject object = new JSONObject();
				String question = iter.next();
				String answer = this.getSpinnerValueByQuestion(question);
				object.put("question", question);
				object.put("type", "o");
				object.put("answer", answer);
				plan.put(object);
			}
		}
		catch  (JSONException e)
		{
			e.printStackTrace();
		}
		return plan.toString();
	}
	private String getSpinnerValueByQuestion(String question)
	{
		Spinner spinner = this.spinnerMap.get(question);
		return spinner.getSelectedItem().toString();
	}
	private String getCheckBoxValueByQuestion(String question)
	{
		CheckBox check = this.checkBoxMap.get(question);
		return (check.isChecked() ? "1" : "0");
	}

	

}