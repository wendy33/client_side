package com.upenn.trainingtracker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.upenn.trainingtracker.customviews.AutoCategorySelector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class TrainingSelectorActivity extends Activity implements Notifiable
{
	private List<String> selectedCategories;
	private List<Integer> selectedCategoryIDs;
	private List<Button> buttons = new ArrayList<Button>();
	private int dogID;
	private static final int RESULT_PLAN_CATEGORIES = 100;
	private static final int RESULT_PULL_CATEGORIES = 101;

	private LinearLayout binLayout;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.training_selector_layout);
		this.binLayout = (LinearLayout) this.findViewById(R.id.selectedLayout);
		this.selectedCategories = new ArrayList<String>();
		this.selectedCategoryIDs = new ArrayList<Integer>();
		AutoCategorySelector categorySelector = (AutoCategorySelector) this.findViewById(R.id.categorySelectorID);
		categorySelector.setParentAndInitialize(this);
		
		Bundle extras = this.getIntent().getExtras();
		this.dogID = extras.getInt("dogID");
		
		
		this.addLookUpButtons();
		this.addPlannedCategories();
	}
	public void addPlannedCategories()
	{
		EntryTether tether = EntryTether.getInstance();
		List<Pair<Integer, String>> plannedSubCats = tether.getPlannedSubCategories(this, this.dogID);
		for (Pair<Integer, String> subCatPair : plannedSubCats)
		{
			int subCatID = subCatPair.getLeft();
			String subCatName = subCatPair.getRight();
			this.addNewCategory(subCatName, subCatID);
		}
	}
	 @Override
	 public boolean onCreateOptionsMenu(Menu menu)
	 {
	     MenuInflater menuInflater = getMenuInflater();
	     menuInflater.inflate(R.menu.selector_menu, menu);
	     return true;
	 }
	 /**
	  * Event Handling for Individual menu item selected
	  * Identify single menu item by it's id
	  * */
	 @Override
	 public boolean onOptionsItemSelected(MenuItem item)
	 {
		 switch (item.getItemId())
		 {
		 case R.id.removeAllID:
			 this.removeAllCategories();
			 break;
		 case R.id.syncID:
			 Log.i("TAG","Sync manager called");
			 SyncManager sm = SyncManager.getInstance(this);
			 sm.syncCategoryInfo(this, this, RESULT_PULL_CATEGORIES);
			 
			 break;
		 default:
			 return super.onOptionsItemSelected(item);
		 }
		 return true;
	 }
	 public void removeAllCategories()
	 {
		 for (Button button : this.buttons)
		 {
			 this.binLayout.removeView(button);
		 }
		 this.buttons.clear();
		 this.selectedCategories.clear();
		 this.selectedCategoryIDs.clear();
	 }
	 public void openAlertDialog(String message)
	 {
		 AlertDialog.Builder builder = new AlertDialog.Builder(this);
		 builder.setMessage(message);
		 builder.setPositiveButton("Ok", null);
		 builder.create().show();
	 }
	 public void checkOutCategories(final View view)
	 {
		 if (this.selectedCategories.isEmpty())
		 {
			 this.openAlertDialog("Please select at least one activity");
			 return;
		 }
		 Intent intent = new Intent(this, CheckOutActivity.class);
		 
		 int[] subCatIDs = new int[this.selectedCategoryIDs.size()];
		 String[] subCatNames = new String[this.selectedCategories.size()];
		 for (int index = 0; index < this.selectedCategoryIDs.size(); ++index)
		 {
			 subCatIDs[index] = this.selectedCategoryIDs.get(index);
			 subCatNames[index] = this.selectedCategories.get(index);
		 }

		 intent.putExtra("subCatIDs", subCatIDs);
		 intent.putExtra("subCatNames", subCatNames);
		 intent.putExtra("dogID", this.dogID);
		 this.startActivityForResult(intent, TrainingSelectorActivity.RESULT_PLAN_CATEGORIES);
	 }
	 private void startSession()
	 {
		 Intent intent = new Intent(this, SessionActivity.class);
		 
		 int[] subCatIDs = new int[this.selectedCategoryIDs.size()];
		 String[] subCatNames = new String[this.selectedCategories.size()];
		 for (int index = 0; index < this.selectedCategoryIDs.size(); ++index)
		 {
			 subCatIDs[index] = this.selectedCategoryIDs.get(index);
			 subCatNames[index] = this.selectedCategories.get(index);
		 }

		 intent.putExtra("subCatIDs", subCatIDs);
		 intent.putExtra("subCatNames", subCatNames);
		 intent.putExtra("dogID", this.dogID);
		 this.finish();
		 this.startActivity(intent); 
	 }
	 @Override
	 protected void onActivityResult(int requestCode, int resultCode, Intent data)
	 {
		 switch (requestCode)
		 {
		 case TrainingSelectorActivity.RESULT_PLAN_CATEGORIES:
			 if (resultCode == RESULT_OK)
			 {
				 this.startSession();
			 }
			 break;
		 }
	 }
	 public void addNewCategory(final String subCatName, final int subCatID)
	 {
		 if (this.selectedCategoryIDs.contains(subCatID)) return;
		 LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		 this.selectedCategoryIDs.add(subCatID);
		 this.selectedCategories.add(subCatName);
		 final Button button = (Button) inflater.inflate(R.layout.lookup_selected_widget, null);
		 this.buttons.add(button);
		 LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		 params.bottomMargin = 10;
		 params.rightMargin = 5;
		 params.leftMargin = 5;
		 params.topMargin = 10;
		 button.setLayoutParams(params);

		 button.setOnClickListener(new OnClickListener()
		 {

			 @Override
			 public void onClick(View arg0) 
			 {
				 String[] items = new String[] {"Remove Category", "View History","Cancel"};
				 ArrayAdapter<String> adapter = new ArrayAdapter<String> (TrainingSelectorActivity.this, android.R.layout.select_dialog_item, items);
				 AlertDialog.Builder builder = new AlertDialog.Builder(TrainingSelectorActivity.this);
				 builder.setTitle("Dog Selection");
				 builder.setAdapter(adapter, new DialogInterface.OnClickListener() 
				 {
					 @Override
					 public void onClick(DialogInterface dialog, int item) 
					 {
						 if (item == 0) // Remove Category
						 {
							 int indexOfID = TrainingSelectorActivity.this.selectedCategoryIDs.indexOf(subCatID);
							 TrainingSelectorActivity.this.selectedCategoryIDs.remove(indexOfID);
							 TrainingSelectorActivity.this.selectedCategories.remove(indexOfID);
							 
							 TrainingSelectorActivity.this.buttons.remove(button);
							 binLayout.removeView(button);
						 }
						 else if (item == 1) // View History in this subcategory
						 {
							 Intent intent = new Intent(TrainingSelectorActivity.this, HistoryActivity.class);
							 intent.putExtra("dogID", dogID);
							 intent.putExtra("subCatID", subCatID);
							 intent.putExtra("forResult", false);
							 TrainingSelectorActivity.this.startActivity(intent);
						 }
						 else if (item == 2) // Cancel
						 {

						 }

					 }
				 });
				 builder.create().show();
			 }
		 });
		 button.setText(subCatName);
		 this.binLayout.addView(button);
	 }
	 public void addLookUpButtons()
	 {
		 LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);


		 ParentCategoryTether tether = ParentCategoryTether.getInstance();
		 Map<String, Integer> parentNameToID = tether.getParentNameToIDMap(this);
		 
		 int rowCount = parentNameToID.size() / 2 + parentNameToID.size() % 2;

		 LinearLayout parent = (LinearLayout) this.findViewById(R.id.lookupLayout);
		 parent.removeAllViews();

		 Display display = getWindowManager().getDefaultDisplay();
		 Point size = new Point();
		 display.getSize(size);
		 int buttonWidth = (size.x - 80)/2;
		 
		 Iterator iter = parentNameToID.entrySet().iterator();
		 
		 while (iter.hasNext())
		 {
			 LinearLayout layout = new LinearLayout(this);
			 layout.setOrientation(LinearLayout.HORIZONTAL);

			 Button button = (Button) inflater.inflate(R.layout.lookup_widget, null);
			 LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			 params.bottomMargin = 10;
			 params.rightMargin = 10;
			 params.leftMargin = 10;
			 params.topMargin = 10;
			 button.setLayoutParams(params);
			 button.setWidth(buttonWidth);
			 
			 Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iter.next();
			 
			 button.setText(entry.getKey());
			 
			 this.configureListenerWithSubcategories(button, entry.getValue());
			 layout.addView(button);
			 if (!iter.hasNext())
			 {
				 parent.addView(layout);
				 return;
			 }
			 Button button2 = (Button) inflater.inflate(R.layout.lookup_widget, null);
			 button2.setLayoutParams(params);
			 button2.setWidth(buttonWidth);
			 
			 Map.Entry<String, Integer> entry2 = (Map.Entry<String, Integer>) iter.next();
			 button2.setText(entry2.getKey());
			 this.configureListenerWithSubcategories(button2, entry2.getValue());

			 layout.addView(button2);
			 Log.i("TAG","Adding view");
			 parent.addView(layout);
		 }
	 }
	 public void configureListenerWithSubcategories(Button button, int parentCategoryID)
	 {
		 final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		 //TrainingReader reader = TrainingReader.getInstance(this);
		 SubCategoryTether tether = SubCategoryTether.getInstance();
		 final List<Pair<String, Integer>> categories = tether.getSubCategoryNamesAndIDsForParentCategory(this, parentCategoryID);
		 Log.i("TAG","num: " + categories.size());
		 button.setOnClickListener(new OnClickListener() {
			 @Override
			 public void onClick(View v){
				 final Dialog dialog = new Dialog(TrainingSelectorActivity.this);
				 dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				 dialog.setContentView(R.layout.category_dialog_layout);

				 LinearLayout parent = (LinearLayout) dialog.findViewById(R.id.categoryLayout);
				 Display display = getWindowManager().getDefaultDisplay();
				 Point size = new Point();
				 display.getSize(size);
				 int buttonWidth = (size.x - 100)/2;
				 Log.i("TAG","width: " + buttonWidth);
				 for (int index = 0; index < categories.size(); ++index)
				 {
					 final Pair<String, Integer> category = categories.get(index);
					 LinearLayout layout = new LinearLayout(TrainingSelectorActivity.this);
					 layout.setOrientation(LinearLayout.HORIZONTAL);

					 Button button = (Button) inflater.inflate(R.layout.lookup_widget, null);
					 LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
					 params.bottomMargin = 10;
					 params.rightMargin = 10;
					 params.leftMargin = 10;
					 params.topMargin = 10;

					 button.setLayoutParams(params);

					 button.setWidth(buttonWidth);
					 button.setText(category.getLeft());
					 final int indexFinal = index;
					 button.setOnClickListener(new OnClickListener()
					 {
						 @Override
						 public void onClick(View v) {
							 TrainingSelectorActivity.this.addNewCategory(category.getLeft(), category.getRight());
							 dialog.cancel();
						 }
					 });
					 layout.addView(button);
					 ++index;
					 if (index == categories.size())
					 {
						 parent.addView(layout);
						 dialog.show();
						 return;
					 }
					 final Pair<String, Integer> category2 = categories.get(index);

					 Button button2 = (Button) inflater.inflate(R.layout.lookup_widget, null);
					 button2.setLayoutParams(params);
					 button2.setWidth(buttonWidth);
					 button2.setText(category2.getLeft());
					 final int indexFinal2 = index;
					 button2.setOnClickListener(new OnClickListener()
					 {
						 @Override
						 public void onClick(View v) {
							 TrainingSelectorActivity.this.addNewCategory(category2.getLeft(), category2.getRight());
							 dialog.cancel();
						 }
					 });
					 layout.addView(button2);
					 Log.i("TAG","Adding view");
					 parent.addView(layout);
				 }
				 Log.i("TAG","showing dialog");
				 dialog.show();
			 }

		 });
	 }
	 /**
	  * This method converts dp unit to equivalent pixels, depending on device density. 
	  * 
	  * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
	  * @param context Context to get resources and device specific display metrics
	  * @return A float value to represent px equivalent to dp depending on device density
	  */
	 public static float convertDpToPixel(float dp, Context context){
		 Resources resources = context.getResources();
		 DisplayMetrics metrics = resources.getDisplayMetrics();
		 float px = dp * (metrics.densityDpi / 160f);
		 return px;
	 }

	 /**
	  * This method converts device specific pixels to density independent pixels.
	  * 
	  * @param px A value in px (pixels) unit. Which we need to convert into db
	  * @param context Context to get resources and device specific display metrics
	  * @return A float value to represent dp equivalent to px value
	  */
	 public static float convertPixelsToDp(float px, Context context){
		 Resources resources = context.getResources();
		 DisplayMetrics metrics = resources.getDisplayMetrics();
		 float dp = px / (metrics.densityDpi / 160f);
		 return dp;
	 }
	@Override
	public void notifyOfEvent(int eventCode, String message) {
		Log.i("TAG","Selector notified: " + message);
		switch (eventCode){
		case RESULT_PULL_CATEGORIES:
			this.addLookUpButtons();
			break;
		}
	}
}
