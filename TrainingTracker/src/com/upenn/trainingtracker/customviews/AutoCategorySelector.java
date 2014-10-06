package com.upenn.trainingtracker.customviews;

import java.util.List;

import com.upenn.trainingtracker.Pair;
import com.upenn.trainingtracker.SubCategoryTether;
import com.upenn.trainingtracker.TrainingSelectorActivity;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class AutoCategorySelector extends AutoCompleteTextView
{
	private TrainingSelectorActivity parent;
	private List<Integer> subCatIDs;
	private List<String> subCatNames;
	
	public AutoCategorySelector(Context context)
	{
		super(context);
	}
	public AutoCategorySelector(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public AutoCategorySelector(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}
	public void setParentAndInitialize(TrainingSelectorActivity activity)
	{
		this.parent = activity;
		this.init();
		this.attachListener();
	}
	public void init()
	{
		Log.i("TAG","Settin adapter");
		SubCategoryTether subTether = SubCategoryTether.getInstance();
		Pair<List<String>,List<Integer>> lists = subTether.getSubCatNamesAndIDsLists(this.parent);
		this.subCatIDs = lists.getRight();
		this.subCatNames = lists.getLeft();
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(parent,
                android.R.layout.simple_dropdown_item_1line, lists.getLeft());
    	this.setAdapter(adapter);
	}
	public void attachListener()
	{
		this.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) 
			{
				String subCatName = (String) arg0.getItemAtPosition(position);
				int subCatID = subCatIDs.get(subCatNames.indexOf(subCatName));
				Log.i("SUB","ID: " + subCatID);
				Log.i("SUB","NAME: " + subCatName);
				AutoCategorySelector.this.parent.addNewCategory(subCatName, subCatID);
				AutoCategorySelector.this.setText("");
			}
		});
	}
	
}
