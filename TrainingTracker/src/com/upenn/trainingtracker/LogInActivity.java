package com.upenn.trainingtracker;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LogInActivity extends Activity implements Notifiable
{
	/*
	 *  A dialog is created in openCreateAccountPopup and logInCallBack() determines 
	 *  if this dialog will be closed.  For this reason, all dialogs use this instance variable
	 *  to allow for global access.
	 */
	private Dialog dialog;
	private static final int RESULT_GET_USERS = 1;
	private static final int RESULT_GET_USERS_AFTER_ADD = 2;
	private static final int RESULT_ADD_USER = 3;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.log_in_layout);
    }
    /**
     * Called by Anonymous Listener which was created inside openCreateAccountPopup().  Uses AsyncTask
     * to push values to server
     * @param name
     * @param username
     * @param password
     * @param email
     * @param phone
     */
    private void addNewUserToServer(String name, String username, String password,
    		String email, String phone)
    {
    	// Verify wifi is present
		ConnectionsManager cm = ConnectionsManager.getInstance(this);
    	boolean isAvailable = cm.checkForWifi(this, "A data connection is needed to sync users");
    	if (!isAvailable) return;
    	
    	List<NameValuePair> pairs = new ArrayList<NameValuePair>();
    	pairs.add(new BasicNameValuePair(Keys.User.FULL_NAME, name));
    	pairs.add(new BasicNameValuePair(Keys.User.USER_NAME, username));
    	pairs.add(new BasicNameValuePair(Keys.User.PASSWORD, password));
    	pairs.add(new BasicNameValuePair(Keys.User.EMAIL, email));
    	pairs.add(new BasicNameValuePair(Keys.User.PHONE, phone));
    	
    	SyncManager sm = SyncManager.getInstance(this);
    	sm.addUserToServer(this, this, LogInActivity.RESULT_ADD_USER, pairs);    
    }

    /**
     * Called when user tries to login.  Checks the database to see if the credentials are valid and
     * appropriately handles result.
     * @param view
     */
    public void logInCallBack(final View view)
    {
    	EditText usernameView = (EditText) this.findViewById(R.id.username);
    	EditText passwordView = (EditText) this.findViewById(R.id.password);
    	String userName = usernameView.getText().toString().trim();
    	String password = passwordView.getText().toString().trim();
    	
    	UserTether tether = UserTether.getInstance();
    	if (tether.isValiduser(this, userName, password))
    	{
    		int userID = tether.getUserID(this, userName);
    		SharedPreferences pref = this.getSharedPreferences(MainActivity.USER_PREFS, 0);
    		Log.i("TAG","Saving preferences");
    		pref.edit().putString(MainActivity.USER_NAME_KEY, userName).commit();
    		pref.edit().putString(MainActivity.USER_PASSWORD_KEY, password).commit();
    		pref.edit().putInt(MainActivity.USER_ID, userID).commit();
    		
    		Log.i("TAG","STARTING DOG ACTIVITY");
    		Intent intent = new Intent(this, DogSelectorActivity.class);
    		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    		LogInActivity.this.startActivity(intent);
    	}
    	else
    	{
    		Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_LONG).show();
    	}
    }
    /**
     * Opens dialog which allows user to have email sent to them with their account details.  Button which calls
     * this method is contained in log_in_layout.  
     * @param view
     */
    public void openRecoverAccountPopup(final View view)
    {
    	dialog = new Dialog(this);
    	dialog.setContentView(R.layout.recover_account_layout);
    	dialog.setTitle("Account Recovery");
    	Button createAccountButton = (Button) dialog.findViewById(R.id.createAccountButtonID);
    	createAccountButton.setOnClickListener(new OnClickListener()
    	{
			@Override
			public void onClick(View buttonView) 
			{
		    	EditText emailET = (EditText) dialog.findViewById(R.id.emailID);
		    	String email = emailET.getText().toString();
		    	Log.i("TAG",email);
				ConnectionsManager cm = ConnectionsManager.getInstance(LogInActivity.this);
				cm.promptRecoveryEmail(LogInActivity.this, email);
				dialog.dismiss();
			}
    	});
    	dialog.show();
    }
    /**
     * Event Handling for Individual menu item selected
     * Identify single menu item by it's id
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	Log.i("TAG","Syncing");
    	 switch (item.getItemId())
         {
         case R.id.itemSyncID:
        	 ConnectionsManager cm = ConnectionsManager.getInstance(this);
        	 boolean isAvailable = cm.checkForWifi(this, "A data connection is needed to sync users");
        	 if (!isAvailable) return false;
        	 //cm.postToServer("getUsers.php", null, this, this.RESULT_GET_USERS);
        	 SyncManager sm = SyncManager.getInstance(this);
        	 sm.syncUsersWithServer(this, this, RESULT_GET_USERS);
         default:
             return super.onOptionsItemSelected(item);
         }
    }
	@Override
	public void notifyOfEvent(int eventCode, String message) 
	{
		Log.i("TAG","notified");
		if (eventCode == RESULT_GET_USERS || eventCode == RESULT_GET_USERS_AFTER_ADD)
		{
/*			Log.i("TAG","Received");
			ConnectionsManager cm = ConnectionsManager.getInstance(this);
			boolean isValid = cm.isValidJSON(message);
			
			if (!isValid)
			{
				ViewUtils utils = ViewUtils.getInstance();
				utils.showAlertMessage(this, "Unable to connect to server.  Please try again later.");
				return;
			}
			DatabaseHandler handler = new DatabaseHandler(this.getApplicationContext());
			//handler.updateUsersWithJSON(message);	
			Log.i("TAG",message);*/
		}
		else if (eventCode == RESULT_ADD_USER)
		{
			if (message.equals("invalid_username"))
			{
				ViewUtils utils = ViewUtils.getInstance();
				utils.showAlertMessage(this, "Username is already taken");
				return;
			}
			if (!message.equals("success"))
			{
				ViewUtils utils = ViewUtils.getInstance();
				utils.showAlertMessage(this, "Unable to connect to server.  Please try again later.");
				return;
			}
			SyncManager sm = SyncManager.getInstance(this);
			sm.syncUsersWithServer(this, this, LogInActivity.RESULT_GET_USERS);
			this.dialog.dismiss();
		}
	}
     // Initiating Menu XML file (menu.xml)
	 @Override
	 public boolean onCreateOptionsMenu(Menu menu)
	 {
	     MenuInflater menuInflater = getMenuInflater();
	     menuInflater.inflate(R.menu.log_in_menu, menu);
	     return true;
	 }
	 /**
	  * Opens a pop-up dialog from which the user can create an account.  Opened when user selects create account button
	  * contained in log_in_layout.  Anonymous listener created for the submitting of new account which ultimately calls
	  * the addNewUserToServer method of this class
	  * @param view
	  */
    public void openCreateAccountPopup(final View view)
    {
    	// Check if wifi is available
    	final ConnectionsManager cm = ConnectionsManager.getInstance(this);
    	boolean isEnabled = cm.checkForWifi(this, "Wifi is needed to create new account");
    	if (!isEnabled) return;
    	
    	dialog = new Dialog(this);
    	dialog.setContentView(R.layout.create_account_layout);
    	dialog.setTitle("Account Creation");
    	Button createAccountButton = (Button) dialog.findViewById(R.id.createAccountButtonID);
    	createAccountButton.setOnClickListener(new OnClickListener()
    	{
			@Override
			public void onClick(View buttonView) 
			{
		    	ConnectionsManager cm = ConnectionsManager.getInstance(LogInActivity.this);
		    	boolean isEnabled = cm.checkForWifi(LogInActivity.this, "Wifi is needed to add a new account");
		    	if (!isEnabled) return;
		    	
			   	EditText fullNameET = (EditText) dialog.findViewById(R.id.nameID);
		    	EditText userNameET = (EditText) dialog.findViewById(R.id.userNmeID);
		    	EditText passwordET = (EditText) dialog.findViewById(R.id.passwordID);
		    	EditText emailET = (EditText) dialog.findViewById(R.id.emailID);
		    	EditText phoneET = (EditText) dialog.findViewById(R.id.phoneID);
		    	
		    	String fullName = fullNameET.getText().toString().trim();
		    	String userName = userNameET.getText().toString().trim();
		    	String password = passwordET.getText().toString().trim();
		    	String email = emailET.getText().toString().trim();
		    	String phone = phoneET.getText().toString().trim();
		    	
		    	if (fullName.equals("") || userName.equals("") || password.equals("") || email.equals("") ||
		    			phone.equals(""))
		    	{
		    		Toast.makeText(LogInActivity.this, "Empty Field(s)", Toast.LENGTH_LONG).show();
		    		return;
		    	}
		    	Log.i("TAG", "New Account: " + fullName + " " + userName + " " + password + " " + email + " " + phone);
		    	LogInActivity.this.addNewUserToServer(fullName, userName, password, email, phone);
			}
    	});
    	dialog.show();
    }

}
