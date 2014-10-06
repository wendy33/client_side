package com.upenn.trainingtracker.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import android.content.Context;

import com.upenn.trainingtracker.CompactParser;
import com.upenn.trainingtracker.ParentCategoryTether;
import com.upenn.trainingtracker.SubCategoryTether;

@RunWith(RobolectricTestRunner.class)
public class CategoryParseTest 
{
	private Context context = Robolectric.application;
	
    @Test
    public void addOneCategory() throws Exception 
    {
    	ParentCategoryTether parentTether = ParentCategoryTether.getInstance();
    	parentTether.addParentCategory(context, 1, "Parent Category 1");
    	parentTether.addParentCategory(context, 2, "Parent Category 2");
    	SubCategoryTether subTether = SubCategoryTether.getInstance();
    	subTether.addSubCategory(context, 1, "sub1", "plan1", 1);
    	subTether.addSubCategory(context, 2, "sub2", "plan2", 1);
    	
    	JSONObject message = new JSONObject();
    	JSONObject data = new JSONObject();
    	
    	// Deleted Parent IDs
	    JSONArray deletedParentCategories = new JSONArray();
	    // Deleted Sub IDs
	    JSONArray deletedSubCategories = new JSONArray();
    	deletedSubCategories.put(2);
    	// Parent Categories
    	JSONArray parents = new JSONArray();
    	parents.put(this.createDummyParent("parent1", 3));
    	parents.put(this.createDummyParent("parent2", 4));
    	// Sub Categories
    	JSONArray subs = new JSONArray();
    	subs.put(this.createDummySub("sub3", 3, 1, "myPlan"));
    	
    	
    	data.put("deleted_parent_category_ids", deletedParentCategories);
	    data.put("deleted_sub_category_ids", deletedSubCategories);
	    data.put("parent_categories", parents);
	    data.put("sub_categories", subs);
	    
	    message.put("data", data);
	    message.put("time", "2014-10-2 10:10:10");
	    String jsonMessage = message.toString();
	    
	    CompactParser parser = CompactParser.getInstance();
	    parser.parseCompactJSON(context, jsonMessage);
	    
	    List<String> parentNames = parentTether.getParentCategories(context);
	    assertEquals(4, parentNames.size());
	    List<String> subNames = subTether.getSubCategoryNamesForParentCategory(context, 1);
	    assertEquals(2, subNames.size());
	    assertTrue(!subNames.contains("sub2"));
	    assertTrue(subNames.contains("sub1"));
	    assertTrue(subNames.contains("sub3"));
	    
    }
    public JSONObject createDummyParent(String name, int id)
    {
    	JSONObject object = new JSONObject();
    	try 
    	{
			object.put("name", name);
	    	object.put("id", id);
		} 
    	catch (JSONException e) 
    	{
			e.printStackTrace();
		}
    	return object;
    }
    public JSONObject createDummySub(String name, int id, int parent_category_id, String plan)
    {
    	JSONObject object = new JSONObject();
    	try 
    	{
			object.put("name", name);
	    	object.put("id", id);
	    	object.put("parent_category_id", parent_category_id);
	    	object.put("plan", plan);
		} 
    	catch (JSONException e) 
		{
			e.printStackTrace();
		}
    	return object;
    }


}
