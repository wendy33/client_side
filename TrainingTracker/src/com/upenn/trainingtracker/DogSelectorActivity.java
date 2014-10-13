package com.upenn.trainingtracker;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.upenn.trainingtracker.customviews.AutoBreedSelector;
import com.upenn.trainingtracker.customviews.DateSelectorTextView;
import com.upenn.trainingtracker.customviews.ImageSelectorImageView;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class DogSelectorActivity extends FragmentActivity implements Notifiable
{
	/**
	 * These are the result-codes that identify which activity the user is returning from.  All of these
	 * activities are started from the ImageSelectorImageView of the add_dog_layout.
	 */
	public final static int CAMERA_INTENT_RESULT_CODE = 1; // Returning from camera app
	public final static int CROP_INTENT_RESULT_CODE = 2;   // Returning from cropping after camera app
	public final static int GALLERY_INTENT_RESULT_CODE = 3; // Retruning from gallery
	public final static int CROP_INTENT_RESULT_CODE_FROM_GALLERY = 4; // Returning from cropping after gallery
	
	public final static int RESULT_ADD_DOG = 6;
	public final static int RESULT_UPDATE_DOG = 7;
	public final static int RESULT_SYNC_DOGS = 8;
	public final static int RESULT_SYNC_EVERYTHING=11;

	
	public final int DOG_HAS_SYNCED = 5;
	private List<DogProfile> profiles;
	private Dialog addDogDialog;
	private DogProfile targetProfile;  //the one being updated

	private ImageSelectorImageView imageSelector;
	private Dialog updateDialog;
	private int dogIDBeingUpdated;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.i("TAG","Creagting dogselector");
		this.setContentView(R.layout.dog_selector_layout);

		DogTether tether = DogTether.getInstance();
		this.profiles = tether.getDogProfiles(this);
   	 	
		final DogAdapter adapter = new DogAdapter(this, profiles);
		ListView list = (ListView) this.findViewById(R.id.list);
		list.setAdapter(adapter);
		this.setListSelectionBehavior(list);

		adapter.getFilter().filter("");
		
        if (this.getIntent().hasExtra(MainActivity.USER_NAME_KEY))
        {
            Bundle extras = this.getIntent().getExtras();
        	this.verifyUser(extras.getString(MainActivity.USER_NAME_KEY));
        }
        
        EditText filterEditText = (EditText) findViewById(R.id.dogFilterTextID);
        this.setFilterBehvaior(filterEditText, adapter);

	}
	public void setFilterBehvaior(final EditText filterEditText, final DogAdapter adapter)
	{
        // Add Text Change Listener to EditText
        filterEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Call back the Adapter with current character to Filter
            	Log.i("TAG","Filtering");
                adapter.getFilter().filter(s.toString().trim());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
	
	}
	public void verifyUser(String userName)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Welcome " + userName + "! \nSelect OK to continue or Switch Account to login under different credentials");
		builder.setPositiveButton("Ok", null);
		builder.setNegativeButton("Switch Account", new DialogInterface.OnClickListener() 
		{
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				DogSelectorActivity.this.logOutOfAccount();
			}
		});
		builder.create().show();
	}
	public void setListSelectionBehavior(ListView list)
	{
		list.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int arg2,
					long arg3) {
				final int dogID = view.getId();
				String[] items = new String[] {"Train Dog", "View History", "Update Profile"};
				ArrayAdapter<String> adapter = new ArrayAdapter<String> (DogSelectorActivity.this, android.R.layout.select_dialog_item, items);
				AlertDialog.Builder builder = new AlertDialog.Builder(DogSelectorActivity.this);
				builder.setTitle("Dog Selection");
				builder.setAdapter(adapter, new DialogInterface.OnClickListener() 
				{
					@Override
					public void onClick(DialogInterface dialog, int item) 
					{
						if (item == 0 || item == 1) // Train Dog or ViewHistory
						{
							Intent intent;
					   		if(item == 0)
					   			intent = new Intent(DogSelectorActivity.this, TrainingSelectorActivity.class);
					   		else
					   			intent = new Intent(DogSelectorActivity.this, HistoryActivity.class);
					   		intent.putExtra("dogID", dogID);
					   		DogSelectorActivity.this.startActivity(intent);
						}
			
						else if (item == 2) // update profile
						{
							DogSelectorActivity.this.openUpdateDogPopUp(dogID);
						}
					}
				});
				builder.create().show();
			}
		});
	}

	public void refreshListDisplay()
	{
		DogTether tether = DogTether.getInstance();
		this.profiles = tether.getDogProfiles(this);
   	 	
		final DogAdapter adapter = new DogAdapter(this, profiles);
		ListView list = (ListView) this.findViewById(R.id.list);
		list.setAdapter(adapter);
		
		EditText filterEditText = (EditText) findViewById(R.id.dogFilterTextID);
	    this.setFilterBehvaior(filterEditText, adapter);
		adapter.getFilter().filter("");
	}
	/**
	 * The imageSelector (of type ImageSelectorImageView) launches the CropImage activity (in janmuller package)
	 * It launches it for a result (see "startActivityForResult").  The result is returned to the current activity
	 * which is this class.  The requestCode which was set when the activity was launched is now used to determine 
	 * the appropriate action to take.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Log.i("TAG","A RESULT HAS: " + requestCode);
		if (requestCode == DogSelectorActivity.CAMERA_INTENT_RESULT_CODE)
		{
			Log.i("TAG", "d");
			imageSelector.cropCameraResult();
		}
		else if (requestCode == DogSelectorActivity.CROP_INTENT_RESULT_CODE)
		{
			imageSelector.setCropResult();
		}
		else if (requestCode == DogSelectorActivity.GALLERY_INTENT_RESULT_CODE)
		{
			imageSelector.cropGalleryResult(data);
		}
		else if (requestCode == DogSelectorActivity.CROP_INTENT_RESULT_CODE_FROM_GALLERY)
		{
			imageSelector.setCropGalleryResult();
		}
	}
	/**
	 * This method is overridden so that the menu can be set.  The menu is opened when the physical
	 * "menu" button is pressed on the android
	 */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.dog_selector_menu, menu);
        return true;
    }
	public void openUpdateDogPopUp(int dogID)
	{
		this.dogIDBeingUpdated = dogID;
		// TODO: Should I use a hashmap
		targetProfile = null;
		for (DogProfile profile : this.profiles)
		{
			if (profile.getID() == dogID) 
			{
				targetProfile = profile;
				break;
			}
		}
		updateDialog = new Dialog(this);
		//updateDialog.setTitle("Update Profile");
		updateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		updateDialog.setContentView(R.layout.add_dog_layout);
		
		TextView nameText = (TextView) updateDialog.findViewById(R.id.nameID);
		nameText.setText(targetProfile.getName());
		
		DateSelectorTextView dateSelector = (DateSelectorTextView) updateDialog.findViewById(R.id.dateSelectorTextViewID); 
    	dateSelector.setParentFragment(this);
    	Log.i("TAG",targetProfile.getBirthDateString());
    	dateSelector.setDate(targetProfile.getBirthDateCalendar());
    	
    	AutoBreedSelector breedSelector = (AutoBreedSelector) updateDialog.findViewById(R.id.breedID);
    	breedSelector.initializeAutoBreeder(this);
    	boolean breedSuccess = breedSelector.setValue(targetProfile.getBreed());
    	
    	Spinner serviceSpinner = (Spinner) updateDialog.findViewById(R.id.serviceTypeID);
    	ArrayAdapter<String> adapter = (ArrayAdapter<String>) serviceSpinner.getAdapter();
    	int position = adapter.getPosition(targetProfile.getServiceType());
    	boolean serviceSuccess = position != -1;
    	if (serviceSuccess) serviceSpinner.setSelection(position);
    	
    	imageSelector = (ImageSelectorImageView) updateDialog.findViewById(R.id.dogImageID); 
    	imageSelector.setParentActivity(this);
    	imageSelector.setImageSelectorImage(targetProfile.getImage());
    	
    	Button updateButton = (Button) updateDialog.findViewById(R.id.addNewDogButtonID);
    	updateButton.setText("Update");
    	
    	// Set behavior of add-dog button
    	Button addDogButton = (Button) updateDialog.findViewById(R.id.addNewDogButtonID);
    	addDogButton.setOnClickListener(new OnClickListener()
    	{
			@Override
			public void onClick(View view) 
			{
				DogSelectorActivity.this.updateDogEntry(view);
			}    		
    	});
    	updateDialog.show();
    	if (!serviceSuccess || !breedSuccess)
    	{
    		String warning = null;
    		if (!serviceSuccess && !breedSuccess)
    		{
    			warning = "Breed and service type had invalid values.  Select valid values and click update.";
    		}
    		else if (!serviceSuccess)
    		{
    			warning = "Serivce type had an invalid value.  Select valid value and click update.";
    		}
    		else
    		{
    			warning = "Breed had invalid value.  Select valid value and lcick update.";
    		}
    		this.openAlertDialog(warning);
    	}
	}
    /**
     * This method is called by the add dog button (as defined in add_dog_layout.xml
     * Both the date-selector and image-selector need a reference to the parent activity. So, these elements
     * are retrieved via their id and passed a reference to this actiivty
     * @param view
     */
    public void openAddDogPopUp(final View view)
    {
    	Log.i("TAG","opening popup");
    	ConnectionsManager cm = ConnectionsManager.getInstance(this);
    	boolean isEnabled = cm.checkForWifi(this, "Wifi is needed to add a new dog");
    	if (!isEnabled) return;
    	this.addDogDialog = new Dialog(this);
    	this.addDogDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

    	addDogDialog.setContentView(R.layout.add_dog_layout);
    	DateSelectorTextView dateSelector = (DateSelectorTextView) addDogDialog.findViewById(R.id.dateSelectorTextViewID); 
    	dateSelector.setParentFragment(this);
    	imageSelector = (ImageSelectorImageView) addDogDialog.findViewById(R.id.dogImageID); 
    	imageSelector.setParentActivity(this);
    	
    	AutoBreedSelector breedText = (AutoBreedSelector) this.addDogDialog.findViewById(R.id.breedID);
    	breedText.initializeAutoBreeder(this);
    	
    	// Set behavior of add-dog button
    	Button addDogButton = (Button) this.addDogDialog.findViewById(R.id.addNewDogButtonID);
    	addDogButton.setOnClickListener(new OnClickListener()
    	{
			@Override
			public void onClick(View view) 
			{
				DogSelectorActivity.this.addNewDogEntry(view);
			}    		
    	});
    	addDogDialog.show();

    }
    public void openAlertDialog(String message)
    {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(message);
    	builder.setPositiveButton("Ok", null);
    	builder.create().show();
    }
    /**
     * This method is called when the user submits the new dog entry.  Values are retrieved from the fields
     * and then the values are pushed to the server with an AsyncTask
     * @param view
     */
    public void addNewDogEntry(final View view)
    {
    	Log.i("TAG","Add new dog called");
    	ConnectionsManager cm = ConnectionsManager.getInstance(this);
    	boolean isEnabled = cm.checkForWifi(this, "Wifi is needed to add a new dog");
    	if (!isEnabled) return;
    	
    	DogProfile profile = this.getProfileFromDialog(this.addDogDialog);
    	if (profile == null)
    	{
    		Log.i("TAG","Profile is null");
    		return;
    	}
    	
    	ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    	profile.getImage().compress(Bitmap.CompressFormat.JPEG, 90, outStream);
    	byte[] byteArray = outStream.toByteArray();
		String byteString = Base64.encodeToString(byteArray, 0);
		Log.i("TAG","BEFRE  " + byteString + "   AFTER");
    	
		// Add pairs that will be sent via HTTP 
    	final List<NameValuePair> pairs = new ArrayList<NameValuePair>();

    	pairs.add(new BasicNameValuePair("name", profile.getName()));
    	pairs.add(new BasicNameValuePair("breed", profile.getBreed()));
    	pairs.add(new BasicNameValuePair("service_type", profile.getServiceType()));
    	pairs.add(new BasicNameValuePair("birth_date", profile.getBirthDateString()));
		pairs.add(new BasicNameValuePair("image", byteString));
    	
		Log.i("TAG","Caaling addDog.php");
		
		SyncManager sm = SyncManager.getInstance(this);
		sm.addDogToServer(this, this, RESULT_ADD_DOG, pairs);
		//cm.postToServer("addDog.php", pairs, this, RESULT_ADD_DOG);
    	this.addDogDialog.cancel();
    }
        
    
    /**
     * Returns null if invalid data
     * @param dialog
     * @return
     */
    public DogProfile getProfileFromDialog(Dialog dialog)
    {
    	String name = ((TextView)dialog.findViewById(R.id.nameID)).getText().toString().trim();
    	
    	AutoBreedSelector breedTextView = (AutoBreedSelector) dialog.findViewById(R.id.breedID);
    	String breed = breedTextView.getText().toString().trim();
    	
    	//String breed = ((TextView)this.addDogDialog.findViewById(R.id.breedID)).getText().toString().trim();
    	
    	Spinner serviceTypeSpinner = ((Spinner)dialog.findViewById(R.id.serviceTypeID));
    	String serviceType = serviceTypeSpinner.getSelectedItem().toString().trim();
    	
    	DateSelectorTextView dateSelector = (DateSelectorTextView) dialog.findViewById(R.id.dateSelectorTextViewID);
    	Calendar dob = dateSelector.getDateOfBirth();
    	
    	boolean shouldContinue = this.validateNewDogData(dob, name, breed, serviceType);
    	if (!shouldContinue) return null;
    	
    	String dobString = dob.get(Calendar.YEAR) + "-" + dob.get(Calendar.MONTH) + "-" + dob.get(Calendar.DAY_OF_MONTH);
    	
    	// Get image and encode as string
    	ImageSelectorImageView imageSelector = (ImageSelectorImageView) dialog.findViewById(R.id.dogImageID);
    	Bitmap image = imageSelector.getBitmap();
    	DogProfile profile = new DogProfile(-1, name, dobString, breed, serviceType, image);
    	return profile;
    }
    public void updateDogEntry(final View view)
    {
    	ConnectionsManager cm = ConnectionsManager.getInstance(this);
    	boolean isEnabled = cm.checkForWifi(this, "Wifi is needed to update a dog");
    	if (!isEnabled) return;
    	
    	DogProfile profile = this.getProfileFromDialog(updateDialog);
    	if (profile == null) return;
    	Log.i("TAG-profID", Integer.toString(profile.getID()));
    	ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    	profile.getImage().compress(Bitmap.CompressFormat.JPEG, 90, outStream);
    	byte[] byteArray = outStream.toByteArray();
		String byteString = Base64.encodeToString(byteArray, 0);
    	
		// Add pairs that will be sent via HTTP 
    	final List<NameValuePair> pairs = new ArrayList<NameValuePair>();
    	
    	pairs.add(new BasicNameValuePair("id", Integer.toString(this.dogIDBeingUpdated)));
    	pairs.add(new BasicNameValuePair("name", profile.getName()));
    	pairs.add(new BasicNameValuePair("breed", profile.getBreed()));
    	pairs.add(new BasicNameValuePair("service_type", profile.getServiceType()));
    	pairs.add(new BasicNameValuePair("birth_date", profile.getBirthDateString()));
    	
    	if (profile.getImage() != targetProfile.getImage())
    	{
        	pairs.add(new BasicNameValuePair("image", byteString));
    	}
    	else
    	{
    		Log.i("TAG","Not adding image");
    	}
    	//cm.postToServer("updateDog", pairs, this, RESULT_UPDATE_DOG);
		SyncManager sm = SyncManager.getInstance(this);
		sm.updateDog(this, this, DogSelectorActivity.RESULT_UPDATE_DOG, pairs);
		//sm.syncDogInfoWithServer(this, this, RESULT_UPDATE_DOG);

    	this.updateDialog.cancel();
    }
    /**
     * Checks to ensure all new dog data is valid
     * @param dob
     * @param name
     * @param breed
     * @param serviceType
     * @return true or false depending on if program should continue adding the dog info to server
     */
    public boolean validateNewDogData(Calendar dob, String name, String breed, String serviceType)
    {
    	boolean allFieldsFull = !name.equals("") && !breed.equals("") && ! serviceType.equals("") && dob != null;
    	if (!allFieldsFull) Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show();
    	return allFieldsFull;
    }
    /**
     * Event Handling for Individual menu item selected
     * Identify single menu item by it's id
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	SyncManager sm = null;
    	 switch (item.getItemId())
         {
         case R.id.itemSyncID:
        	 ConnectionsManager cm = ConnectionsManager.getInstance(this);
        	 boolean isAvailable = cm.checkForWifi(this, "A data connection is needed to sync dogs");
        	 if (!isAvailable) return false;
        	 sm = SyncManager.getInstance(this);
        	 sm.syncDogsWithServer(this, this, DogSelectorActivity.RESULT_SYNC_DOGS);
        	 //sm.syncDogInfoWithServer(this, this, RESULT_UPDATE_DOG);
        	 //sm.syncCategoryDataWithServer(this);
        	 break;
         case R.id.itemSwitchAccountID:
        	 this.logOutOfAccount();
        	 break;
         case R.id.syncEntriesID:
        	 sm = SyncManager.getInstance(this);
        	 sm.syncEntriesWithServer(this, this, -1);
        	 break;
   /*      case R.id.itemSyncCategories:
        	 sm = SyncManager.getInstance(this);
        	 sm.syncCategoryDataWithServer(this);
        	 break; */
         case R.id.syncEverythingID:
        	 Syncer s = new Syncer();
        	 s.syncEverything(this, this, RESULT_SYNC_EVERYTHING);
         default:
             return super.onOptionsItemSelected(item);
         }
    	 return true;
    }
    public void logOutOfAccount()
    {
        SharedPreferences preferences = this.getSharedPreferences(MainActivity.USER_PREFS, 0);
        preferences.edit().remove(MainActivity.USER_NAME_KEY).commit();
        preferences.edit().remove(MainActivity.USER_PASSWORD_KEY).commit();
        
		Intent intent = new Intent(this, LogInActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		this.startActivity(intent);
    }
    
	@Override
	public void notifyOfEvent(int eventCode, String message) 
	{

		if (eventCode == RESULT_ADD_DOG)
		{
			if (!message.equals("success"))
			{
				ViewUtils utils = ViewUtils.getInstance();
				utils.showAlertMessage(this, "Unable to connect to server.  Please try again later.");
				return;
			}
			this.refreshListDisplay();
			//SyncManager sm = SyncManager.getInstance(this);
			//Log.i("TAG","Syncing dogs with server");
			//sm.syncDogsWithServer(this, this, RESULT_SYNC_DOGS);
			//sm.syncDogInfoWithServer(this, this, RESULT_UPDATE_DOG);
		}
		else if (eventCode == RESULT_UPDATE_DOG || eventCode == RESULT_SYNC_EVERYTHING)
		{
			this.refreshListDisplay();
		}
		else if (eventCode == RESULT_SYNC_DOGS)
		{
			Log.i("TAG","Done syncing, refreshing list display");
			this.refreshListDisplay();
		}
	}
}
