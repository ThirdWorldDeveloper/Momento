package com.phoon.phoon.Momento;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.phoon.phoon.Momento.R.id.textView2;

/**
 * Created by Hello on 22/3/2017.
 */

public class AddNoteActivity extends AppCompatActivity {

    SQLiteDatabase db;
    DbHelper mDbHelper;
    EditText mTitleText;
    EditText mDescriptionText;
    Spinner mSpinner;
    DatePicker pickerDate;
    TimePicker pickerTime;
    TextView time;
    TextView date;
    CheckBox checkBoxAlarm;
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final int RESULT_PICK_CONTACT = 8;
    private TextView mName;
    private TextView mAddress;
    private ImageView ivImage;
    private TextView mContact,mContactNum;
    private View mContactLayout;
    String Latitude;
    String Longitude;
    private String userChoosenTask;
    private static final int REQUEST_CAMERA = 0, SELECT_FILE = 2;
    Uri uriSavedImage = null;
    Menu myMenu;
    Calendar calender;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addnote);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mDbHelper = new DbHelper(this);
        db = mDbHelper.getWritableDatabase();

        mTitleText = (EditText) findViewById(R.id.txttitle);
        mDescriptionText = (EditText) findViewById(R.id.description);
        mSpinner = (Spinner) findViewById(R.id.spinnerNoteType);
        pickerDate = (DatePicker) findViewById(R.id.datePicker);
        pickerTime = (TimePicker) findViewById(R.id.timePicker);
        time = (TextView) findViewById(R.id.txtTime);
        date = (TextView) findViewById(R.id.txtDate);
        checkBoxAlarm = (CheckBox) findViewById(R.id.checkBox);
        mName = (TextView) findViewById(R.id.textView);
        mAddress = (TextView) findViewById(textView2);
        ivImage = (ImageView) findViewById(R.id.ivImage);
        ImageViewPopUpHelper.enablePopUpOnClick(this, ivImage);
        mContact = (TextView) findViewById(R.id.contactNameTextView);
        mContactNum = (TextView) findViewById(R.id.contactNumberTextView);
        mContactLayout = (View) findViewById(R.id.contactLayout);


        pickerDate.setVisibility(View.GONE);
        pickerTime.setVisibility(View.GONE);
        time.setVisibility(View.GONE);
        date.setVisibility(View.GONE);

        mDescriptionText.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                v.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction() & MotionEvent.ACTION_MASK){
                    case MotionEvent.ACTION_UP:
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    mDescriptionText.setText(sharedText);
                }
            } else if (type.startsWith("image/")) {
                Uri imageUri2 = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (imageUri2 != null) {
                    uriSavedImage = imageUri2;
                    ivImage.setImageURI(uriSavedImage);
                    ivImage.setVisibility(View.VISIBLE);
                }
            }
        }

        final BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        View viewTab = bottomBar.getCurrentTab();
        View parentView = (View) viewTab.getParent();
        ViewGroup mItemContainer = (ViewGroup) parentView.findViewById(com.roughike.bottombar.R.id.bb_bottom_bar_item_container);

        for (int i = 0; i < mItemContainer.getChildCount(); i++) {
            View viewItem = mItemContainer.getChildAt(i);
            //TITLE
            TextView titleTab = (TextView) viewItem.findViewById(com.roughike.bottombar.R.id.bb_bottom_bar_title);
            titleTab.setVisibility(View.GONE);
            //ICON
            AppCompatImageView icon = (AppCompatImageView) viewItem.findViewById(com.roughike.bottombar.R.id.bb_bottom_bar_icon);
            icon.setY(6);

        }

        bottomBar.setDefaultTabPosition(3);

        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                switch (tabId){
                    case R.id.tab_nearby:
                        try {
                            PlacePicker.IntentBuilder intentBuilder =
                                    new PlacePicker.IntentBuilder();
                            Intent intent = intentBuilder.build(AddNoteActivity.this);
                            startActivityForResult(intent, PLACE_PICKER_REQUEST);

                        } catch (GooglePlayServicesRepairableException
                                | GooglePlayServicesNotAvailableException e) {
                            e.printStackTrace();
                        }
                        break;
                    case R.id.tab_info:
                        Toast.makeText(AddNoteActivity.this, "Create your note" , Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.tab_contact:
                        pickContact();
                        break;
                    case R.id.tab_photo:
                        final CharSequence[] items = { "Take Photo", "Choose from Library",
                                "Cancel" };

                        AlertDialog.Builder builder = new AlertDialog.Builder(AddNoteActivity.this);
                        builder.setTitle("Add Photo!");
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                boolean result=Utility.checkPermission(AddNoteActivity.this);

                                if (items[item].equals("Take Photo")) {
                                    userChoosenTask ="Take Photo";
                                    if(result)
                                        cameraIntent();

                                } else if (items[item].equals("Choose from Library")) {
                                    userChoosenTask ="Choose from Library";
                                    if(result)
                                        galleryIntent();

                                } else if (items[item].equals("Cancel")) {
                                    dialog.dismiss();
                                }
                            }
                        });
                        builder.show();
                        break;
                    default:
                        break;
                }
            }
        });

        bottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                switch (tabId){
                    case R.id.tab_nearby:
                        try {
                            PlacePicker.IntentBuilder intentBuilder =
                                    new PlacePicker.IntentBuilder();
//                            intentBuilder.setLatLngBounds(BOUNDS_MOUNTAIN_VIEW);
                            Intent intent = intentBuilder.build(AddNoteActivity.this);
                            startActivityForResult(intent, PLACE_PICKER_REQUEST);

                        } catch (GooglePlayServicesRepairableException
                                | GooglePlayServicesNotAvailableException e) {
                            e.printStackTrace();
                        }
                        break;
                    case R.id.tab_info:
                        Toast.makeText(AddNoteActivity.this, "Create your note", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.tab_contact:
                        pickContact();
                        break;
                    case R.id.tab_photo:
                        final CharSequence[] items = { "Take Photo", "Choose from Library",
                                "Cancel" };

                        AlertDialog.Builder builder = new AlertDialog.Builder(AddNoteActivity.this);
                        builder.setTitle("Add Photo!");
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                boolean result=Utility.checkPermission(AddNoteActivity.this);

                                if (items[item].equals("Take Photo")) {
                                    userChoosenTask ="Take Photo";
                                    if(result)
                                        cameraIntent();

                                } else if (items[item].equals("Choose from Library")) {
                                    userChoosenTask ="Choose from Library";
                                    if(result)
                                        galleryIntent();

                                } else if (items[item].equals("Cancel")) {
                                    dialog.dismiss();
                                }
                            }
                        });
                        builder.show();
                        break;
                    default:
                        bottomBar.setSelected(false);
                        bottomBar.clearFocus();
                        break;
                }
            }
        });

        ArrayAdapter adapter = ArrayAdapter.createFromResource(
                this, R.array.note_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(
                    AdapterView parent, View view, int position, long id) {
                if(id == 2){
                        showToast(getString(R.string.added_alert));
                    checkBoxAlarm.setEnabled(true);
                }
                else {
                    checkBoxAlarm.setEnabled(false);
                    checkBoxAlarm.setChecked(false);
                }
            }

            public void onNothingSelected(AdapterView parent) {
            }
        });


//        pickerButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    PlacePicker.IntentBuilder intentBuilder =
//                            new PlacePicker.IntentBuilder();
//                    intentBuilder.setLatLngBounds(BOUNDS_MOUNTAIN_VIEW);
//                    Intent intent = intentBuilder.build(AddNoteActivity.this);
//                    startActivityForResult(intent, PLACE_PICKER_REQUEST);
//
//                } catch (GooglePlayServicesRepairableException
//                        | GooglePlayServicesNotAvailableException e) {
//                    e.printStackTrace();
//                }
//            }
//        });


        mTitleText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mTitleText.getText().toString().length()>0) {
                    myMenu.findItem(R.id.action_save)
                            .setVisible(true);
                    myMenu.findItem(R.id.action_save)
                            .setEnabled(true);
                    myMenu.findItem(R.id.action_back)
                            .setVisible(false);
                    myMenu.findItem(R.id.action_back)
                            .setEnabled(false);
                }
                else {
                    myMenu.findItem(R.id.action_save)
                            .setVisible(false);
                    myMenu.findItem(R.id.action_save)
                            .setEnabled(false);
                    myMenu.findItem(R.id.action_back)
                            .setVisible(true);
                    myMenu.findItem(R.id.action_back)
                            .setEnabled(true);
                }
            }
        });



        checkBoxAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true){
                    pickerDate.setVisibility(View.VISIBLE);
                    pickerTime.setVisibility(View.VISIBLE);
                    time.setVisibility(View.VISIBLE);
                    date.setVisibility(View.VISIBLE);
                }
                else{
                    pickerDate.setVisibility(View.GONE);
                    pickerTime.setVisibility(View.GONE);
                    time.setVisibility(View.GONE);
                    date.setVisibility(View.GONE);
                }
            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if(userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                } else {
                    //code for deny
                }
                break;
        }
    }

   private void pickContact()
    {

        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);


    }

    private void galleryIntent()
    {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    private void cameraIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

       //folder stuff
        File imagesFolder = new File(Environment.getExternalStorageDirectory(), "MyImages");
        imagesFolder.mkdirs();

        File image = new File(imagesFolder, "QR_" + timeStamp + ".png");
        uriSavedImage = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", image);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
        startActivityForResult(intent, REQUEST_CAMERA);
    }


    void showToast(CharSequence msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_note, menu);
        myMenu = menu;
        return true;
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.action_save:
                String title = mTitleText.getText().toString();
                String detail = mDescriptionText.getText().toString();
                String type =  mSpinner.getSelectedItem().toString();
                String location = mName.getText().toString();
                String address = mAddress.getText().toString();
                String contact = mContact.getText().toString();
                String contactNum = mContactNum.getText().toString();

                ContentValues cv = new ContentValues();
                cv.put(mDbHelper.TITLE, title);
                cv.put(mDbHelper.DETAIL, detail);
                cv.put(mDbHelper.TYPE, type);
                cv.put(mDbHelper.TIME, getString(R.string.Not_Set));
                cv.put(mDbHelper.LOCATION_NAME, location);
                cv.put(mDbHelper.ADDRESS, address);
                cv.put(mDbHelper.LATITUDE,Latitude);
                cv.put(mDbHelper.LONGITUDE,Longitude);
                cv.put(mDbHelper.CONTACT,contact);
                cv.put(mDbHelper.CONTACT_NO, contactNum);

                if (null!=uriSavedImage) {
                    String imageuri = uriSavedImage.toString();
                    cv.put(mDbHelper.IMAGE, imageuri);
                }

                if (checkBoxAlarm.isChecked()){
                    calender = Calendar.getInstance();
                    calender.clear();
                    calender.set(Calendar.MONTH, pickerDate.getMonth());
                    calender.set(Calendar.DAY_OF_MONTH, pickerDate.getDayOfMonth());
                    calender.set(Calendar.YEAR, pickerDate.getYear());
                    calender.set(Calendar.HOUR, pickerTime.getHour());
                    calender.set(Calendar.MINUTE, pickerTime.getMinute());
                    calender.set(Calendar.SECOND, 00);

                    SimpleDateFormat formatter = new SimpleDateFormat(getString(R.string.hour_minutes));
                    String timeString = formatter.format(new Date(calender.getTimeInMillis()));
                    SimpleDateFormat dateformatter = new SimpleDateFormat(getString(R.string.dateformate));
                    String dateString = dateformatter.format(new Date(calender.getTimeInMillis()));

                    cv.put(mDbHelper.TIME, timeString);
                    cv.put(mDbHelper.DATE, dateString);
                }

                long id = db.insert(mDbHelper.TABLE_NAME, null, cv);
                int reqCode = (int) id;

                if (calender != null){
                    AlarmManager alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent(this, AlarmReceiver.class);

                    String alertTitle = mTitleText.getText().toString();
                    String alertDescription = mDescriptionText.getText().toString();
                    intent.putExtra(getString(R.string.alert_title), alertTitle);
                    intent.putExtra("description", alertDescription);
                    intent.putExtra("rowid",id);

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this,reqCode, intent, 0);

                    alarmMgr.set(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), pendingIntent);
                }

                Intent openMainScreen = new Intent(this, MainActivity.class);
                openMainScreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(openMainScreen);
                return true;

            case R.id.action_back:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == PLACE_PICKER_REQUEST) {

                final Place place = PlacePicker.getPlace(data, this);
                final CharSequence name = place.getName();
                final CharSequence address = place.getAddress();
                Latitude = String.valueOf(place.getLatLng().latitude);
                Longitude = String.valueOf(place.getLatLng().longitude);

                mName.setText(name);
                mAddress.setText(address);
                mName.setVisibility(View.VISIBLE);
                mAddress.setVisibility(View.VISIBLE);

            }
            else if (requestCode == SELECT_FILE) {
                uriSavedImage = data.getData();
                ivImage.setImageURI(uriSavedImage);
                ivImage.setVisibility(View.VISIBLE);

            }
            else if (requestCode == REQUEST_CAMERA) {
                ivImage.setImageURI(uriSavedImage);
                ivImage.setVisibility(View.VISIBLE);
            }
            else if (requestCode == RESULT_PICK_CONTACT){
                Cursor cursor = null;
                try {

                    String phoneNo = null ;
                    String name = null;
                    Uri uri = data.getData();
                    cursor = getContentResolver().query(uri, null, null, null, null);
                    cursor.moveToFirst();

                    int  phoneIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    int  nameIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

                    phoneNo = cursor.getString(phoneIndex);
                    name = cursor.getString(nameIndex);

                    mContact.setText(name);
                    mContactNum.setText(phoneNo);
                    mContactLayout.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }

    }



}
