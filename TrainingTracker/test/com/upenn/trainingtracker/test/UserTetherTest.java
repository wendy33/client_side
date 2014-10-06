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

import com.upenn.trainingtracker.DatabaseHandler;
import com.upenn.trainingtracker.Keys;
import com.upenn.trainingtracker.UserTether;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;


@RunWith(RobolectricTestRunner.class)
public class UserTetherTest {
    @Test
    public void addOneUser() throws Exception 
    {
        //String hello = new FirstActivity().getResources().getString(R.string.hello_world);
        //assertThat(hello, equalTo("Hello World!"));
    	UserTether tether = UserTether.getInstance();
    	tether.addUser(Robolectric.application, 2, "Matthew MacLean", "mattmac22", 
    			"password", "matt@gmail.com", "967-234-1394");
    	
    	assertEquals(tether.getUserNames(Robolectric.application).size(), 1);
    	assertTrue(tether.getUserNames(Robolectric.application).contains("mattmac22"));
    }
    @Test
    public void addUsersFromJSON() throws Exception 
    {
    	String users = "[{'id': 1, 'full_name': 'full_name1',"
    			+ "'user_name': 'user_name1', 'password': 'password1',"
    			+ "'email': 'email1', 'phone': 'phone1'},"
    			
    			+ "{'id': 2, 'full_name': 'full_name2',"
    			+ "'user_name': 'user_name2', 'password': 'password2',"
    			+ "'email': 'email2', 'phone': 'phone2'}]";
    	
    	UserTether tether = UserTether.getInstance();
    	tether.addUsersFromJSON(Robolectric.application, users);
    	List<String> names = tether.getUserNames(Robolectric.application);
    	
    	assertEquals(names.size(), 2);
    	assertTrue(names.contains("user_name1"));
    	assertTrue(names.contains("user_name2"));
    }
    @Test
    public void timeOfLastUpdate() throws Exception
    {
    	UserTether tether = UserTether.getInstance();
    	assertEquals(tether.getTimeOfLastUpdate(Robolectric.application), "NEVER");
    	tether.setTimeOfLastUpdate(Robolectric.application, "time");
    	assertEquals(tether.getTimeOfLastUpdate(Robolectric.application), "time");
    }
    

}