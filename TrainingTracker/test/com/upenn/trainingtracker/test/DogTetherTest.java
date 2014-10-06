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
import com.upenn.trainingtracker.Keys;
import com.upenn.trainingtracker.TetherUtils;
import com.upenn.trainingtracker.UserTether;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;


@RunWith(RobolectricTestRunner.class)
public class DogTetherTest {
	private Context context = Robolectric.application;
	
    @Test
    public void addOneDog() throws Exception 
    {
    	Bitmap bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ALPHA_8);
    	DogTether tether = DogTether.getInstance();
    	tether.addDog(context, 1, "name1", "2012-10-1", "breed1", 
    			"serviceType1", "imageName1.png", DogTetherTest.encodeTobase64(bitmap));
    	List<DogProfile> profiles = tether.getDogProfiles(context);
    	
    	assertEquals(1, profiles.size());
    	assertEquals("name1", profiles.get(0).getName());
    	assertEquals("breed1", profiles.get(0).getBreed());
    }
    
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String BIRTH_DATE = "birth_date";
	public static final String BREED = "breed";
	public static final String SERVICE_TYPE = "service_type";
	public static final String IMAGE_NAME = "image_name";
	
	
    @Test
    public void addDogsFromJSON() throws Exception 
    {
    	Bitmap bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ALPHA_8);
    	String dogs = "[{'id': 1, 'name': 'name1',"
    			+ "'birth_date': '2000-1-1', 'breed': 'breed1',"
    			+ "'service_type': 'service_type1', 'image_name': 'image_name1',"
    			+ "'image':'" + DogTetherTest.encodeTobase64(bitmap) + "'},"
    			
    			+ "{'id': 2, 'name': 'name2',"
    			+ "'birth_date': '2000-1-2', 'breed': 'breed2',"
    			+ "'service_type': 'service_type2', 'image_name': 'image_name2',"
    			+ "'image':'" + DogTetherTest.encodeTobase64(bitmap) + "'}]";
    	dogs = dogs.replace("\n","");
    	DogTether tether = DogTether.getInstance();
    	tether.addDogsFromJSON(context, dogs);
    	List<DogProfile> profiles = tether.getDogProfiles(context);
    	assertEquals(2, profiles.size());
    	assertEquals("name1", profiles.get(0).getName());
    }
    @Test
    public void timeOfLastUpdate() throws Exception
    {
    	DogTether tether = DogTether.getInstance();
    	assertEquals(tether.getTimeOfLastUpdate(Robolectric.application), "NEVER");
    	tether.setTimeOfLastUpdate(Robolectric.application, "time");
    	assertEquals(tether.getTimeOfLastUpdate(Robolectric.application), "time");
    }
    public static String encodeTobase64(Bitmap image)
    {
        Bitmap immagex=image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b,Base64.DEFAULT);

        Log.e("LOOK", imageEncoded);
        return imageEncoded;
    }
    

}