package com.upenn.trainingtracker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.upenn.trainingtracker.customviews.HistoryEntryWidget;
import com.upenn.trainingtracker.customviews.SessionCategoryWidget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HistoryAdapter extends BaseAdapter
{
	private List<HistoryEntryWidget> selection = new ArrayList<HistoryEntryWidget>();
	private HistoryAdapter.SelectionType selectionType = SelectionType.UNITY;
	private List<String> lastFilterSet;
	//private HistoryActivity activity;
	private boolean planSelectable;
	
	public enum SelectionType
	{
		UNITY, INTERSECTION
	}
	private HistoryMapper mapper;

	public HistoryAdapter(Context context, boolean planSelectable, int subCatID,
			HistoryActivity activity, int dogID)
	{
		super();
		EntryTether entryTether = EntryTether.getInstance();
		this.planSelectable = planSelectable;				

		this.mapper = entryTether.getHistoryWidgetsForDogID(context, dogID, subCatID, this.planSelectable, activity);

		//this.activity = activity;
		this.selection.addAll(this.mapper.getAllWidgets());
		Log.i("TAG","SELECTION SIZE: " + this.selection.size());
		this.applyFilterStrings(new ArrayList<String>());
	}
	public void setSelectionType(HistoryAdapter.SelectionType type)
	{
		this.selectionType = type;
		if (lastFilterSet != null)
		{
			this.applyFilterStrings(this.lastFilterSet);
		}
	}


	public void applyFilterStrings(List<String> filters)
	{
		this.lastFilterSet = filters;
		
		this.selection.clear();
		if (filters.isEmpty())
		{
			this.selection.addAll(this.mapper.getAllWidgets());
			this.sortChronologically();
			this.notifyDataSetChanged();
			return;
		}
		// Get all the lists
		List<List<HistoryEntryWidget>> entryLists = new ArrayList<List<HistoryEntryWidget>>();
		//boolean emptyListPresent = false;
		for (String filter : filters)
		{
			List<HistoryEntryWidget> list = mapper.getListForFilter(filter);
			entryLists.add(list);
		}
		// First get the set of all entries
		Set<HistoryEntryWidget> entrySet = new HashSet<HistoryEntryWidget>();

		for (List<HistoryEntryWidget> list : entryLists)
		{
			entrySet.addAll(list);
		}
		this.selection.addAll(entrySet);
		switch (this.selectionType)
		{
			case UNITY:
				break;
			case INTERSECTION:
				for (List<HistoryEntryWidget> list : entryLists)
				{
					selection.retainAll(list);
				}
				break;
		}
		this.notifyDataSetChanged();
		this.sortChronologically();
	}
	private void sortChronologically()
	{
		Collections.sort(this.selection);
	}
//	public void clearFilters()
//	{
//		
//	}
	
	@Override
	public int getCount() 
	{
		// TODO Auto-generated method stub
		return this.selection.size();
	}

	@Override
	public Object getItem(int arg0) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) 
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View arg1, ViewGroup arg2) 
	{
		return selection.get(position);
	}

}
