package com.bignerdranch.android.criminalintent;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by danielmakover on 22/02/2018.
 */

public class CrimeFragment extends Fragment {
    private static final int READ_CONTACTS_PERMISSIONS_REQUEST = 1;

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_PHOTO = "DialogPhoto";

    private static final String ARG = "";

    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_CONTACT = 2;
    private static final int REQUEST_PHOTO = 3;

    private Crime mCrime;
    private EditText mTitleField;

    private Button mDateButton;
    private Button mTimeButton;

    private CheckBox mSolvedCheckBox;

    private Button mSuspectButton;
    private Button mReportButton;

    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private File mPhotoFile;

    private Button mCallSuspectButton;

    private int startPoint;
    private Button toStart;
    private Button toEnd;

    boolean callSuspect = false;

    private Callbacks mCallbacks;

    /*
    * Required interface for hosting activities
    * */
    public interface Callbacks {
        void onCrimeUpdated(Crime crime);
    }


    public static CrimeFragment newInstance(UUID crimeId){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.delete_crime:

                //delete the crime - from DB
                Log.d("CrimeFragment", "deleting a crime!");
                CrimeLab.get(getActivity()).deleteCrime(mCrime);
                mCallbacks.onCrimeUpdated(mCrime);//
                //going back to Crime List Activity

                /// -> ????FIX?????????

                if(CrimePagerActivity.ACTIVE){
                    Log.d("CrimeFragment", "Using a phone");
                    Intent intent = new Intent(getActivity(), CrimeListActivity.class);
                    startActivity(intent);
                }else{
                    Log.d("CrimeFragment", "Using a tablet");

                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.d("CrimeFragment", "Crime Fragment CREATED!");
        UUID crimeID = (UUID)getArguments().getSerializable(ARG_CRIME_ID);

        //sets the crime data
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeID);

        //sets the photo
        Log.d("CrimeFragment", "Should Crash NOW!");
        Log.d("CrimeFragment", "mCrime" + mCrime);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
    }

    @Override
    public void onPause(){
        super.onPause();

        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mCallbacks = (Callbacks)context;
    }

    @Override
    public void onDetach(){
        super.onDetach();
        mCallbacks = null;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        //gets the layout to inflate
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        int numCrime =  CrimeLab.get(getActivity()).getCrimeNumber(mCrime.getId());

        //wiring up the title feild
        mTitleField = (EditText)v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        //Wiring up the Date Button
        mDateButton = (Button)v.findViewById(R.id.crime_date);
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager =  getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        //Wiring up the Time Button
        mTimeButton = (Button)v.findViewById(R.id.crime_time);
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //initilize Dialog
                FragmentManager manager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mCrime.getTime());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                dialog.show(manager, DIALOG_TIME);
            }
        });

        //setting and updating the date and time buttons
        updateDateAndTime();

        //Wiring up the checkBox
        mSolvedCheckBox = (CheckBox)v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                mCrime.setSolved(isChecked);
                updateCrime();
            }

        });

        //////////////////Start - Suspect Button//////////////////////////////
        mSuspectButton = (Button)v.findViewById(R.id.crime_suspect);
        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);

        if(mCrime.getSuspect() != null){
            mSuspectButton.setText(mCrime.getSuspect());
        }
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(getActivity(), "getting suspect", Toast.LENGTH_SHORT).show();
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });

        PackageManager packageManager = getActivity().getPackageManager();
        if(packageManager.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) ==  null){
             mSuspectButton.setEnabled(false);
        }

        /////////////////End - Suspect Button////////////////////////////

        ///////////////Start - photos image and button///////////////////
        mPhotoButton = (ImageButton)v.findViewById(R.id.crime_camera);
        mPhotoView = (ImageView)v.findViewById(R.id.crime_photo);

        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                PhotoViewerFragment dialog = PhotoViewerFragment.newInstance(mPhotoFile);
                dialog.show(manager, DIALOG_PHOTO);
            }
        });
        updatePhotoView();
        ///////////////End - photos image and button///////////////////

        /////////////Start - Photo Button /////////////////////////////////
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 1- check if you can app has rights to use camera by querying the packageManager
        boolean canTakePhoto = mPhotoFile != null && captureImage.resolveActivity(packageManager)!= null;
        mPhotoButton.setEnabled(canTakePhoto);

        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ///2 - gets the uri (location) for the file
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "com.bignerdranch.android.criminalintent.fileprovider",
                        mPhotoFile);

                // 3 - adds tag telling the intent to store the image at the uri
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                ///4 - WTF?
                List<ResolveInfo> cameraActivities = getActivity()
                        .getPackageManager().queryIntentActivities(captureImage,
                        PackageManager.MATCH_DEFAULT_ONLY);

                //5 - grant permissions so camera can write the file to th uri (location)
                for(ResolveInfo activity: cameraActivities){
                    getActivity().grantUriPermission(activity.activityInfo.packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }

                //6 - run intent captureImage
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });

        //wiring up the send Report Button
        mReportButton = (Button)v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.crime_report_subject));

                i = Intent.createChooser(i, getString(R.string.send_report));
                startActivity(i);
            }
        });


        //initialize begining and end buttons
        toStart = (Button) v.findViewById(R.id.to_start);
        toEnd = (Button) v.findViewById(R.id.to_finish);

        //////////////////////determine when buttons are visible and when to work
        if(CrimePagerActivity.ACTIVE) {
            if (numCrime == 0) {
                toStart.setVisibility(View.GONE);
                toEnd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((CrimePagerActivity) getActivity()).goEnd();
                    }
                });
            } else if (numCrime == CrimeLab.get(getActivity()).getCrimes().size() - 1) {
                toEnd.setVisibility(View.GONE);
                toStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((CrimePagerActivity) getActivity()).goFront();
                    }
                });
            } else {
                toStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((CrimePagerActivity) getActivity()).goFront();
                    }
                });
                toEnd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((CrimePagerActivity) getActivity()).goEnd();
                    }
                });
            }
        }else{
            toStart.setVisibility(View.GONE);
            toEnd.setVisibility(View.GONE);
        }
        //////////////////////////////////End of button logic///////////////////////////////////

        return v;
    }

    ///this Code is the logic for when an intent brings you back to this fragment//////
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode != Activity.RESULT_OK){
            return;
        }

        if(requestCode == REQUEST_DATE){
            Date date = (Date)data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDateAndTime();
        }else if(requestCode == REQUEST_TIME){
            Date date = (Date)data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            mCrime.setTime(date);
            updateCrime();
            updateDateAndTime();

        }else if(requestCode == REQUEST_CONTACT && data != null){
            Uri contactUri = data.getData();
            Log.d("Query", "contactURI" + contactUri);
            //Specify which fields you want your query to return
            //values fo

            String[] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts._ID
            };

            Log.d("Query", "fields: " + queryFields.length);

            Cursor c = getActivity().getContentResolver().query(contactUri, queryFields, null, null, null);

            try{
                //double-check that you actually got results
                if(c.getCount() == 0){
                    return;
                }

                //pull out the first column of the first row of data -
                //the is your suspect's name

                c.moveToFirst();
                String suspect = c.getString(0);
                Log.d("QueryResults" ,"Field 1: " + c.getString(0));
                Log.d("QueryResults" ,"Feild 2: " + c.getString(1));

                mCrime.setSuspect(suspect);
                updateCrime();
                mSuspectButton.setText(suspect);
            }finally{
                c.close();
            }
        }else if(requestCode == REQUEST_PHOTO){
            Uri uri =  FileProvider.getUriForFile(getActivity(),
                        "com.bignerdranch.android.criminalintent.fileprovider",
                        mPhotoFile);

                getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                updateCrime();
                updatePhotoView();
        }
    }

    private void updateCrime(){
        //update crime in the DB -> via crimeLab
        CrimeLab.get(getActivity()).updateCrime(mCrime);

        //update ui -> via Hosting Activity
        mCallbacks.onCrimeUpdated(mCrime);
    }

    private void updateDateAndTime() {
//        String tTimeCrime = mCrime.getDate().toString();
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        String newDateFormat = format.format(Date.parse(mCrime.getDate().toString()));
        mDateButton.setText("Date reported: " + newDateFormat);

        Date tDate = mCrime.getTime();

        int hours = tDate.getHours();
        int minutes = tDate.getMinutes();

        String newTime = String.valueOf(hours) + ":" + String.valueOf(minutes);
        mTimeButton.setText("Time reported: " + newTime);
    }

    private String getCrimeReport(){
        String solvedString = null;
        if(mCrime.isSolved()){
            solvedString = getString(R.string.crime_report_solved);
        }else{
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EE, MMM dd";
        String dateSting = android.text.format.DateFormat.format(dateFormat,
                mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if(suspect == null){
            suspect = getString(R.string.crime_report_no_suspect);
        }else{
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        //doe I really need @SuppressLint("StringFormatMatches")????
       String report = getString(R.string.crime_report, mCrime.getTitle(), dateSting, solvedString, suspect);

        return report;

    }

    private void updatePhotoView(){
        if(mPhotoFile == null || !mPhotoFile.exists()){
            mPhotoView.setImageDrawable(null);
            mPhotoView.setContentDescription(getString(R.string.crime_photo_no_image_description));
        }else{
            Bitmap bitmap = PictureUtils.getScaledBitmap(
                    mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bitmap);
            mPhotoView.setContentDescription(getString(R.string.crime_photo_image_description));
        }
    }
}
