package com.upenn.trainingtracker.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


import junit.framework.Assert;

import org.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Base64;

import com.upenn.trainingtracker.DatabaseHandler;
import com.upenn.trainingtracker.DogProfile;
import com.upenn.trainingtracker.DogTether;
import com.upenn.trainingtracker.EntryTether;
import com.upenn.trainingtracker.Keys;
import com.upenn.trainingtracker.ParentCategoryTether;
import com.upenn.trainingtracker.TetherUtils;
import com.upenn.trainingtracker.UserTether;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import org.junit.Before;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;


@RunWith(RobolectricTestRunner.class)
public class ParentCategoryTetherTest 
{
	private Context context = Robolectric.application;
	
    @Test
    public void addOneCategory() throws Exception 
    {
    	ParentCategoryTether tether = ParentCategoryTether.getInstance();
    	tether.addParentCategory(context, 2, "First");
    	assertTrue(tether.getParentCategories(context).contains("First"));
    }
    @Test
    public void addTwoCategory() throws Exception
    {
    	ParentCategoryTether tether = ParentCategoryTether.getInstance();
    	tether.addParentCategory(context, 2, "First");
    	tether.addParentCategory(context, 1, "Second");
    	List<String> parentCats1 = tether.getParentCategories(context);
    	assertTrue(parentCats1.contains("First"));
    	assertTrue(parentCats1.contains("Second"));
    	tether.deleteParentCategory(context, 2);
    	
    	List<String> parentCats2 = tether.getParentCategories(context);
    	assertTrue(parentCats2.contains("Second"));
    	assertEquals(parentCats2.size(), 1);
    }
    @Test
    public void testDeletion() throws Exception
    {
    	ParentCategoryTether tether = ParentCategoryTether.getInstance();
    	tether.addParentCategory(context, 2, "First");
    	tether.deleteParentCategory(context, 2);
    	assertEquals(tether.getParentCategories(context).size(), 0);
    }
    @Test
    public void addCategoryFromJSON() throws Exception
    {
    	String categories = "[{'id': 1, 'name': 'name1'},"
    			+ "{'id': 2, 'name': 'name2'}]";
    	ParentCategoryTether tether = ParentCategoryTether.getInstance();
    	tether.addParentCategoriesFromJSON(context, categories);
    	List<String> catNames = tether.getParentCategories(context);
    	assertTrue(catNames.contains("name1"));
    	assertTrue(catNames.contains("name2"));
    	assertEquals(2, catNames.size());
    }
    
	

}