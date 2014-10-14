package com.upenn.trainingtracker;

import android.content.Context;

public class Syncer implements Notifiable
{
	private Notifiable observer;
	private int eventCode;
	private Context context;
	
	private static final int RESULT_SYNC_CATEGORIES = 1;
	private static final int RESULT_SYNC_DOGS = 2;
	private static final int RESULT_SYNC_USERS = 3;
	private static final int RESULT_SYNC_ENTRIES = 4;
	
	public void syncEverything(Context context, Notifiable observer, int eventCode)
	{
		this.observer = observer;
		this.eventCode = eventCode;
		this.context = context;
		SyncManager sm = SyncManager.getInstance(context);
		sm.syncCategoryInfo(context, this, RESULT_SYNC_CATEGORIES);
	}
	@Override
	public void notifyOfEvent(int eventCode, String message) 
	{
		SyncManager sm = null;
		switch(eventCode)
		{
		case RESULT_SYNC_CATEGORIES:
			sm = helpSync(sm, RESULT_SYNC_DOGS);
			break;
		case RESULT_SYNC_DOGS:
			sm = helpSync(sm, RESULT_SYNC_USERS);
			break;
		case RESULT_SYNC_USERS:
			sm = helpSync(sm, RESULT_SYNC_ENTRIES);
			break;
		case RESULT_SYNC_ENTRIES:
			this.observer.notifyOfEvent(this.eventCode, message);
			break;
		}
		
	}
	private SyncManager helpSync(SyncManager sm, int resultSync) {
		sm = SyncManager.getInstance(this.context);
		sm.syncEntriesWithServer(this.context, sm, resultSync);
		return sm;
	}
	

}
