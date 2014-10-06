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
public class EntryTetherTest 
{
	private Context context = Robolectric.application;
	
	@Before
	public void setup()
	{
    	ParentCategoryTether parentTether = ParentCategoryTether.getInstance();
    	parentTether.addParentCategory(context, 1, "FirstParent");
    	parentTether.addParentCategory(context, 2, "SecondParent");
    	parentTether.addParentCategory(context, 3, "ThirdParent");
    	
    	SubCategoryTether subTether = SubCategoryTether.getInstance();
    	subTether.addSubCategory(context, 1, "sub1", "plan1", 1);
    	subTether.addSubCategory(context, 2, "sub2", "plan2", 2);
    	subTether.addSubCategory(context, 3, "sub3", "plan3", 3);
    	
    	UserTether userTether = UserTether.getInstance();
    	userTether.addUser(context, 1, "fullName1", "userName1", "password1", "email1", "phone1");
    	userTether.addUser(context, 2, "fullName2", "userName2", "password2", "email2", "phone2");
    	
    	Bitmap bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ALPHA_8);
    	String imageEncoded = DogTetherTest.encodeTobase64(bitmap);
    	DogTether dogTether = DogTether.getInstance();
    	dogTether.addDog(context, 1, "name1", "2000-10-1 23:11:00:000", "breed1", "serviceType1", "image1.png", imageEncoded);
    	dogTether.addDog(context, 2, "name2", "2000-10-2 23:11:00:000", "breed2", "serviceType2", "image2.png", imageEncoded);
	}
    @Test
    public void addOneEntry() throws Exception 
    {
    	EntryTether tether = EntryTether.getInstance();
    	//tether.addEntry(context, dogID, subCatID, userID, plan, sessionDate, trialsResult);
    	tether.addEntry(context, 1, 1, 1, "plan1", "2000-10-12", "10011");
    }
    
	

}