package com.upenn.trainingtracker;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class SyncManager implements Notifiable
{
	private static SyncManager instance;
	private Context context;
	
	public static final int RESULT_ADD_USER = 1;
	public static final int RESULT_PULL_USERS = 2;
	
	public static final int RESULT_ADD_DOG = 3;
	public static final int RESULT_PULL_DOGS = 4;
	public static final int RESULT_UPDATE_DOG = 5;
	
	public static final int RESULT_PUSH_ENTRIES = 6;
	public static final int RESULT_PULL_ENTRIES = 7;
	
	public static final int RESULT_PULL_CATEGORIES = 8;

	
	private ArrayList<Notifiable> observers = new ArrayList<Notifiable>();
	private int eventCode;
	
	private SyncManager(Context context)
	{
		this.context = context;
	}
	public static SyncManager getInstance(Context activity)
	{
		if (instance == null && activity == null)
		{
			throw new IllegalArgumentException("Cannot give null activity when instance is not instantiated");
		}
		if (instance == null)
		{
			instance = new SyncManager(activity);
		}
		return instance;
	}
	//============================ USERS ============================
    public void syncUsersWithServer(Context context, Notifiable observer, int eventCode)
    {
    	this.observers.add(observer);
    	this.eventCode = eventCode;
    	
    	this.pullUsersFromServer(context);
    }
    public void addUserToServer(Context context, Notifiable observer, int eventCode, List<NameValuePair> pairs)
    {
    	this.observers.add(observer);
    	this.eventCode = eventCode;
    	this.context = context;
    	
		ConnectionsManager cm = ConnectionsManager.getInstance(context);
    	cm.postToServer("addUser", pairs, this, RESULT_ADD_USER);
    }
    private void pullUsersFromServer(Context context)
    {
    	ConnectionsManager cm = ConnectionsManager.getInstance(context);
    	//boolean isAvailable = cm.isGConnectionAvailable() || cm.isWifiAvailable();
    	//if (!isAvailable) return;
    	UserTether tether = UserTether.getInstance();
    	
    	
    	String timeOfLastUpdate = tether.getTimeOfLastUpdate(context);
    	Log.i("TAG",timeOfLastUpdate);
    	List<NameValuePair> pairs = new ArrayList<NameValuePair>();
    	pairs.add(new BasicNameValuePair("last_updated", timeOfLastUpdate));
    	
    	cm.postToServer("getUsers", pairs, this, RESULT_PULL_USERS);
    }
  //============================ DOGS ============================
    public void syncDogsWithServer(Context context, Notifiable observer, int eventCode)
    {
    	this.observers.add(observer);
    	this.eventCode = eventCode;
    	this.pullDogsFromServer(context);
    }
    public void addDogToServer(Context context, Notifiable observer, int eventCode, List<NameValuePair> pairs)
    {
    	this.observers.add(observer);
    	this.eventCode = eventCode;
    	this.context = context;
    	
    	Log.i("DOG","Posting new dog to server");
    	ConnectionsManager cm = ConnectionsManager.getInstance(context);
    	cm.postToServer("addDog", pairs, this, RESULT_ADD_DOG);
    	
    }
    public void pullDogsFromServer(Context context)
    {
    	ConnectionsManager cm = ConnectionsManager.getInstance(context);
    	DogTether tether = DogTether.getInstance();
    	String timeOfLastUpdate = tether.getTimeOfLastUpdate(context);
    	List<NameValuePair> pairs = new ArrayList<NameValuePair>();
    	Log.i("DOG","Last update: " + timeOfLastUpdate);
    	pairs.add(new BasicNameValuePair("last_updated", timeOfLastUpdate));
    	
    	cm.postToServer("getDogs", pairs, this, RESULT_PULL_DOGS);
    }
    public void updateDog(Context context, Notifiable observer, int eventCode, List<NameValuePair> pairs)
    {
    	this.observers.add(observer);
    	this.eventCode = eventCode;
    	this.context = context;
    	
    	ConnectionsManager cm = ConnectionsManager.getInstance(context);
    	cm.postToServer("updateDog", pairs, this, RESULT_UPDATE_DOG);
    }
  //============================ ENTRIES ============================
    public void syncEntriesWithServer(Context context, Notifiable observer, int eventCode)
    {
    	this.observers.add(observer);
    	this.eventCode = eventCode;
    	
    	this.pushEntriesToServer(context);
    }
    public void pushEntriesToServer(Context context)
    {
    	EntryTether tether = EntryTether.getInstance();
    	
    	List<NameValuePair> pairs = new ArrayList<NameValuePair>();
    	pairs.add(new BasicNameValuePair("entries", tether.getEntriesNotPushed(context).toString()));
    	
    	ConnectionsManager cm = ConnectionsManager.getInstance(context);
    	cm.postToServer("addEntries", pairs, this, RESULT_PUSH_ENTRIES);
    }
    public void pullEntriesFromServer(Context context)
    {
    	ConnectionsManager cm = ConnectionsManager.getInstance(context);
    	EntryTether tether = EntryTether.getInstance();
    	String timeOfLastUpdate = tether.getTimeOfLastUpdate(context);
    	List<NameValuePair> pairs = new ArrayList<NameValuePair>();
    	pairs.add(new BasicNameValuePair("last_updated", timeOfLastUpdate));
    	
    	cm.postToServer("getEntries", pairs, this, RESULT_PULL_ENTRIES);
    }
  //============================ CATEGORIES ============================
    public void syncCategoryInfo(Context context, Notifiable observer, int eventCode)
    {
    	this.observers.add(observer);
    	this.eventCode = eventCode;
    	Log.i("TAG","Pulling category info");
    	this.pullCategoryInfoFromServer(context);
    }
    private void pullCategoryInfoFromServer(Context context)
    {
    	ConnectionsManager cm = ConnectionsManager.getInstance(context);
    	
    	ParentCategoryTether tether = ParentCategoryTether.getInstance();
    	String timeOfLastUpdate = tether.getTimeOfLastUpdate(context);
    	List<NameValuePair> pairs = new ArrayList<NameValuePair>();
    	pairs.add(new BasicNameValuePair("last_updated", timeOfLastUpdate));
    	
    	
    	cm.postToServer("getCategoriesCompact", pairs, this, RESULT_PULL_CATEGORIES);
    }
    private void notifyObservers(String message)
    {
    	for (Notifiable observer : this.observers)
    	{
    		observer.notifyOfEvent(this.eventCode, message);
    	}
    }
	@Override
	public void notifyOfEvent(int eventCode, String message)
	{
		ConnectionsManager cm = null;
		Log.i("TAG","MESSAGE: " + message);
		switch (eventCode)
		{
		//--------- USERS ----------
		case SyncManager.RESULT_ADD_USER:
			if (message.equals("success"))
			{
				this.pullUsersFromServer(this.context);
				Log.i("TAG","Success in pushing");
			}
			else
			{
				this.notifyObservers(message);
				this.observers.clear();
			}
			break;
		case SyncManager.RESULT_PULL_USERS:
			if (ConnectionsManager.isValidJSON(message))
			{
				UserTether tether = UserTether.getInstance();
				tether.addUsersFromJSON(this.context, message);
			}
			else
			{
				this.notifyObservers(message);
			}
			this.notifyObservers("success");
			this.observers.clear();
			break;
		//--------- DOGS ----------
		case SyncManager.RESULT_ADD_DOG:
			if (message.equals("success"))
			{
				this.pullDogsFromServer(this.context);
			}
			else
			{
				Log.i("TAG", "notifying observers");
				this.notifyObservers(message);
				this.observers.clear();
			}
			break;
		case SyncManager.RESULT_PULL_DOGS:
			if (ConnectionsManager.isValidJSON(message))
			{
				DogTether tether = DogTether.getInstance();
				tether.addDogsFromJSON(this.context, message);
				this.notifyObservers("success");
			}
			else
			{
				this.notifyObservers(message);
			}
			
			
			this.observers.clear();
			break;
		case SyncManager.RESULT_UPDATE_DOG:
			if (message.equals("success"))
			{
				this.pullDogsFromServer(this.context);
			}
			else
			{
				this.notifyObservers(message);
				this.observers.clear();
			}
			//--------- ENTRIES ----------
		case SyncManager.RESULT_PULL_ENTRIES:
			if (ConnectionsManager.isValidJSON(message))
			{
				Log.i("TAG","Finished pulling entries");
				EntryTether tether = EntryTether.getInstance();
				tether.addEntriesFromJSON(this.context, message);
				Log.i("TAG","Entries have now been added locally");
				//this.pushEntriesToServer(this.context);
			}

			this.notifyObservers(message);
			this.observers.clear();
			
			break;
		case SyncManager.RESULT_PUSH_ENTRIES:
			if (ConnectionsManager.isValidJSON(message))
			{
				EntryTether tether = EntryTether.getInstance();
				tether.markAllAsSynced();
				tether.changeEntryIDs(message);
				this.pullEntriesFromServer(context);
			}
			else
			{
				this.notifyObservers(message);
				this.observers.clear();
			}
			break;
		case SyncManager.RESULT_PULL_CATEGORIES:
			if (ConnectionsManager.isValidJSON(message))
			{
				CompactParser parser = CompactParser.getInstance();
				Log.i("Category","Calling parser");
				parser.parseCompactJSON(this.context, message);
				this.notifyObservers("success");
			}
			else 
			{
				this.notifyObservers(message);
			}
			this.observers.clear();
		}
		
	}
	
	
}
