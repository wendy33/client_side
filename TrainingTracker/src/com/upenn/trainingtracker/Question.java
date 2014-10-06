package com.upenn.trainingtracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Question 
{
	private String question;
	private String[] answers;
	private Type type;
	
	public static enum Type {
		CHECKBOX, OPTIONS
	}
	
	public Question(String question, String[] answers)
	{
		this.question = question;
		this.answers = answers;
	}
	public Question(JSONObject object)
	{
		String question = null;
		String type = null;
		try 
		{
			question = object.getString("name");
			type = object.getString("type");
			this.question = question;
			if (type.toLowerCase().equals("c"))
			{
				this.type = Type.CHECKBOX;
			}
			else
			{
				this.type = Type.OPTIONS;
				JSONArray answers = object.getJSONArray("answers");
				String[] arrAnswers = new String[answers.length()]; 
				for (int index = 0; index < answers.length(); ++index)
				{
					arrAnswers[index] = answers.getString(index);
				}
				this.answers = arrAnswers;
			}
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
	}
	public String getQuestion()
	{
		return this.question;
	}
	public String[] getAnswers()
	{
		return this.answers;
	}
	public Type getType()
	{
		return this.type;
	}
	

}
