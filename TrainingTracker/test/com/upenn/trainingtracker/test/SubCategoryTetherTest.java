package com.upenn.trainingtracker.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


import static org.junit.Assert.assertTrue;
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
import com.upenn.trainingtracker.Question;
import com.upenn.trainingtracker.SubCategoryTether;
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
public class SubCategoryTetherTest 
{
	private Context context = Robolectric.application;
	@Before
	public void setup()
	{
    	ParentCategoryTether tether = ParentCategoryTether.getInstance();
    	tether.addParentCategory(context, 1, "FirstParent");
    	tether.addParentCategory(context, 2, "SecondParent");
    	tether.addParentCategory(context, 3, "ThirdParent");
	}
    @Test
    public void addOneCategory() throws Exception 
    {
    	SubCategoryTether tether = SubCategoryTether.getInstance();
    	tether.addSubCategory(context, 1, "Sub1", "plan1", 2);
    	assertEquals(1, tether.getSubCategoryNamesForParentCategory(context, 2).size());
    	assertEquals(0, tether.getSubCategoryNamesForParentCategory(context, 1).size());
    }
    @Test
    public void addTwoCategory() throws Exception
    {
    	SubCategoryTether tether = SubCategoryTether.getInstance();
    	tether.addSubCategory(context, 1, "Sub1", "plan1", 2);
    	tether.addSubCategory(context, 2, "name2", "plan2", 2);
    	assertEquals(2, tether.getSubCategoryNamesForParentCategory(context, 2).size());
    	assertEquals(0, tether.getSubCategoryNamesForParentCategory(context, 1).size());
    }
    @Test
    public void testDeletion() throws Exception
    {
    	SubCategoryTether tether = SubCategoryTether.getInstance();
    	tether.addSubCategory(context, 1, "Sub1", "plan1", 2);
    	tether.addSubCategory(context, 2, "name2", "plan2", 2);
    	tether.deleteSubCategory(context, 1);
    	assertEquals(1, tether.getSubCategoryNamesForParentCategory(context, 2).size());
    }
    @Test
    public void addCategoryFromJSON() throws Exception
    {
    	String categories = "[{'id': 1, 'name': 'name1',"
    			+ "'parent_category_id': 1, 'plan': 'plan1'},"
    			+ "{'id': 2, 'name': 'name2',"
    			+ "'parent_category_id': 1, 'plan': 'plan2'}]";
    	SubCategoryTether tether = SubCategoryTether.getInstance();
    	tether.addSubCategoriesFromJSON(context, categories);
    	List<String> names = tether.getSubCategoryNamesForParentCategory(context, 1);
    	assertEquals(2, names.size());
    	assertTrue(names.contains("name1"));
    	assertTrue(names.contains("name2"));
    }
    @Test
    public void testPlan() throws Exception
    {
    	SubCategoryTether tether = SubCategoryTether.getInstance();
    	String questionsJSON = "[{'name':'question1','type':'c'},"
    			+ "{'name':'question2','type':'o','answers':['one','two','three']}]";
    	tether.addSubCategory(context, 1, "Sub1", questionsJSON, 2);
    	List<Question> questions = tether.getQuestionsForSubCategory(context, 1);
    	assertEquals(2, questions.size());
    	assertEquals("question1", questions.get(0).getQuestion());
    	assertEquals("question2", questions.get(1).getQuestion());
    	assertTrue(questions.get(1).getAnswers()[0].equals("one"));
    }
	

}