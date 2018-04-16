package com.bignerdranch.android.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;
import java.util.UUID;

/**
 * Created by danielmakover on 01/03/2018.
 */

public class CrimePagerActivity extends AppCompatActivity  implements CrimeFragment.Callbacks{
    private static final String EXTRA_CRIME_ID= "com.bignerdranch.android.criminalintent.crime_id";
    static boolean ACTIVE = false;

    @Override
    public void onStart() {
        super.onStart();
        ACTIVE = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        ACTIVE = false;
    }
    public static Intent newIntent(Context packageContext, UUID crimeId){
        Intent intent = new Intent(packageContext, CrimePagerActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, crimeId);
        return intent;
    }

    private ViewPager mViewPager;
    private List<Crime> mCrimes;

    public void goFront(){
        mViewPager.setCurrentItem(0);
    }

    public void goEnd(){
//        Log.d("CrimePagerActivity", "End point is: " + mCrimes.size());
       int endPoint = mCrimes.size();
        mViewPager.setCurrentItem(endPoint);
    }

    public int getCrimeNumber(UUID crimeId){
            for(int i = 0; i < mCrimes.size(); i++){
                if(mCrimes.get(i).getId().equals(crimeId)){
                    return i;
                }
            }
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstnceState) {
        super.onCreate(savedInstnceState);
        Log.d("CrimePagerActivity", "Crime Pager Activity CREATED!");
        setContentView(R.layout.activity_crime_pager);

        UUID crimeId = (UUID)getIntent().getSerializableExtra(EXTRA_CRIME_ID);

        mViewPager = (ViewPager)findViewById(R.id.crime_view_pager);
        mCrimes = CrimeLab.get(this).getCrimes();

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager){
            @Override
            public Fragment getItem(int position){
                Crime crime = mCrimes.get(position);
                return CrimeFragment.newInstance(crime.getId());
            }

            @Override
            public int getCount(){
                return mCrimes.size();
            }
        });

        //get the crime you clicked on from the list sent of by the Intent
            for(int i =0; i < mCrimes.size(); i++){
                if(mCrimes.get(i).getId().equals(crimeId)){
                    mViewPager.setCurrentItem(i);
                    break;
                }
            }
        }

        /*
        any activity that hosts the fragment: CrimeFragment must implement the interface Callback.
        Therefore it MUST be implemented in the CrimePagerAcitivity. Since it is not used the
        function onCrimeUpdated is left Blank.

        The purpose of this is to prevent a compilation error!

        The Take Home: All activities that host the fragment must implement the interface whether it
        is used or not!
        */
        @Override
        public void onCrimeUpdated(Crime crime){
            Log.d("CrimePagerActivity", "NOT USED! Only for TABLETS!");
        }
}
