package com.upenn.trainingtracker;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

public class DogTether 
{
	private static DogTether instance;
	
	private DogTether() {}
	public static DogTether getInstance()
	{
		if (instance == null)
		{
			instance = new DogTether();
		}
		return instance;
	}
	public void addDog(Context context, int id, String name, String birthDate, String breed,
			String serviceType, String imageEncoded)
	{
		ContentValues values = new ContentValues();
		values.put(Keys.Dog.ID, id);
		values.put(Keys.Dog.NAME, name);
		values.put(Keys.Dog.BIRTH_DATE, birthDate);
		values.put(Keys.Dog.BREED, breed);
		values.put(Keys.Dog.SERVICE_TYPE, serviceType);
		DatabaseHandler db = new DatabaseHandler(context);
		
		if (db.tableContainsPrimaryKey(DatabaseHandler.DOG, id))
		{
			db.updateTable(DatabaseHandler.DOG, values, Keys.Dog.ID + "=?", new String[]{Integer.toString(id)});
		}
		else
		{
			db.insertIntoTable(DatabaseHandler.DOG, null, values);
		}
		
		byte[] byteArray = Base64.decode(imageEncoded, Base64.DEFAULT);
		String imageName = "dog_image_" + id + ".png";
		Bitmap img = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
		if (img == null){
			Log.i("TAG","Image equals null - addDog");
		}
		TetherUtils.saveImage(context, img, imageName);
	}
	public void deleteDog(Context context, int ID)
	{
		DatabaseHandler db = new DatabaseHandler(context);
		db.deleteEntries(DatabaseHandler.DOG, Keys.Dog.ID + "=?", new String[]{Integer.toString(ID)});
	}
	public void addDogsFromJSON(Context context, String json)
	{
		Log.i("DOG","Adding dogs from JSON");
		try
		{
			JSONObject input = new JSONObject(json);
			String updateTime = input.getString("time");
			this.setTimeOfLastUpdate(context, updateTime);
			JSONObject data = input.getJSONObject("data");
			JSONArray dogs = data.getJSONArray("dogs");
			JSONArray deletedDogIDs = data.getJSONArray("deleted_dog_ids");
			
			for (int index = 0; index < deletedDogIDs.length(); ++index)
			{
				this.deleteDog(context, deletedDogIDs.getInt(index));
			}
			
			for (int index = 0; index < dogs.length(); ++index)
			{
				JSONObject dog = dogs.getJSONObject(index);
				int id = dog.getInt("id");
				String name = dog.getString("name");
				String birthDate = dog.getString("birth_date");
				String breed = dog.getString("breed");
				String serviceType = dog.getString("service_type");
				
				String imageEncoded = dog.getString("image");  			
				
            	this.addDog(context, id, name, birthDate, breed, serviceType, 
            			imageEncoded);
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}	
	}
	public String getTimeOfLastUpdate(Context context)
	{
		return TetherUtils.getTimeOfLastUpdate(context, DatabaseHandler.DOG);
	}
	public void setTimeOfLastUpdate(Context context, String time)
	{
		TetherUtils.setTimeOfLastUpdate(context, DatabaseHandler.DOG, time);
	}
    /**
     * Will return the information needed about each dog for the DogSelector activity
     * @return
     */
    public List<DogProfile> getDogProfiles(Context context)
    {
    	DatabaseHandler db = new DatabaseHandler(context);
    	String[] columnNames = new String[] {Keys.Dog.ID, Keys.Dog.NAME, Keys.Dog.BIRTH_DATE, 
    			Keys.Dog.BREED, Keys.Dog.SERVICE_TYPE};
    	
    	Cursor cursor = db.queryFromTable(DatabaseHandler.DOG, columnNames, null, null);    	
    	
    	List<DogProfile> profiles = new ArrayList<DogProfile>();
    	
    	while(cursor.moveToNext())
    	{
    		int ID = cursor.getInt(cursor.getColumnIndex(Keys.Dog.ID));
    		Log.i("TAG-ID",Integer.toString(ID));
    		String name = cursor.getString(cursor.getColumnIndex(Keys.Dog.NAME));
    		String birthDate = cursor.getString(cursor.getColumnIndex(Keys.Dog.BIRTH_DATE));
    		String breed = cursor.getString(cursor.getColumnIndex(Keys.Dog.BREED));
    		String serviceType = cursor.getString(cursor.getColumnIndex(Keys.Dog.SERVICE_TYPE));
    		String imageName = "dog_image_" + ID + ".png";
    		Bitmap image = TetherUtils.loadImage(context, imageName);
    		DogProfile prof = new DogProfile(ID, name, birthDate, breed, serviceType, image);
    		profiles.add(prof);
    	}
    	db.close();
    	return profiles;
    }

	
}
