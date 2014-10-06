package com.upenn.trainingtracker;

import java.util.Calendar;

public class Keys 
{
	/**
	 * Server-connection information
	 */
	public static final String CONNECTION_PASSWORD = "32lk4j2lk3j42lk3";
	//public static final String SITE = "http://pennvetwdc.t15.org/";
	//public static final String SITE = "http://pennvetwdc.t15.org/";
	public static final String SITE = "http://165.123.214.125:7777/api/";
	/**
	 * These are the keys used to identify the different columns of the database tables.  Each static subclass
	 * represents a different table and each field a column of that table
	 */
	public static class ParentCategory
	{
		public static final String ID = "id";
		public static final String NAME = "name";
		public static final String SUB_CATEGORIES = "sub_categories";
	}
	public static class SubCategory
	{
		public static final String ID = "id";
		public static final String PARENT_CATEGORY_ID = "parent_category_id";
		public static final String NAME = "name";
		public static final String PLAN = "plan";
	}
	public static class User
    {
    	public static final String ID = "id";
    	public static final String FULL_NAME = "full_name";
    	public static final String USER_NAME = "user_name";
    	public static final String PASSWORD = "password";
    	public static final String EMAIL = "email";
    	public static final String PHONE = "phone";
    }
	
    public static class Dog
    {
    	public static final String ID = "id";
    	public static final String NAME = "name";
    	public static final String BIRTH_DATE = "birth_date";
    	public static final String BREED = "breed";
    	public static final String SERVICE_TYPE = "service_type";
    }
    public static class Entry
    {
    	public static final String ID = "id";
    	public static final String SESSION_DATE = "session_date";
    	public static final String PLAN = "plan";
    	public static final String TRIALS_RESULT = "trials_result";
    	public static final String DOG_ID = "dog_id";
    	public static final String USER_ID = "user_id";
    	public static final String SUB_CATEGORY_ID = "sub_category_id";
    	public static final String SYNCED = "synced";
    }
    public static class LastUpdate
    {
    	public static final String TABLE_NAME = "table_name";
    	public static final String TIME = "time";
    }

   /* public static String getTableNameForCategory(String category, int dogID)
    {
    	// Can send null reference since has already been created by this point
    	TrainingReader reader = TrainingReader.getInstance(null);
    	String catKey = reader.categoryToCatKey(category);
    	
    	return catKey + "_" + dogID;
    }
    public static String getTableNameForCatKey(String catKey, int dogID)
    {
    	return catKey + "_" + dogID;
    }
    public static String getSkillsTableName(int dogID)
    {
    	return "skills_table_" + dogID;
    }*/
    public static String getCurrentDateString()
    {
		Calendar c = Calendar.getInstance(); 
		int month = c.get(Calendar.MONTH) + 1;
		int day = c.get(Calendar.DAY_OF_MONTH);
		int year = c.get(Calendar.YEAR);
		
		int hours = c.get(Calendar.HOUR_OF_DAY);
		int minutes = c.get(Calendar.MINUTE);
		int second = c.get(Calendar.SECOND);
		String dateString = month + "-" + day + "-" + year + "-" + hours + ":" + minutes + ":" + second;
		
		return dateString;
    }
}
