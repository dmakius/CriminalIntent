package com.bignerdranch.android.criminalintent;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.UUID;

/**
 * Created by danielmakover on 24/02/2018.
 */

public class CrimeListActivity extends SingleFragmentActivity
        implements CrimeListFragment.Callbacks, CrimeFragment.Callbacks, CrimeListFragment.OnDeleteCrimeListener {
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

        @Override
        protected Fragment createFragment() {
            Log.d("CrimeActivity", "Crime Activity CREATED!");
            return new CrimeListFragment();
        }

        @Override
        protected int getLayoutResId() {
            return R.layout.activity_masterdetail;
        }

        @Override
        public void onCrimeSelected(Crime crime){
            //if the device is a phone -> detail_fragment_container will be null
            if(findViewById(R.id.detail_fragment_container) == null){
                Intent intent = CrimePagerActivity.newIntent(this, crime.getId());
                startActivity(intent);
            //if the device is NOT a phone -> detail_fragment_container will be present
            }else{
                Fragment newDetail = CrimeFragment.newInstance(crime.getId());

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_fragment_container, newDetail).commit();
            }
        }

        //
        public void onCrimeUpdated(Crime crime){
            CrimeListFragment listFragment = (CrimeListFragment)
                    getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);
            listFragment.updateUI();
        }

    @Override
    public void onCrimeIdSelected(UUID crimeId) {
        CrimeFragment crimeFragment = (CrimeFragment) getSupportFragmentManager().findFragmentById(R.id.detail_fragment_container);
        CrimeListFragment listFragment = (CrimeListFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        listFragment.deleteCrime(crimeId);
        listFragment.updateUI();
        //FIXME:For two- panel detail errors. when I want to delete item in list using the swipe. I took an error that recyclerview.setNextAnim(int) on a null object reference. And I added this code. Now swipe to remove  perfect in the two-pane layout.
        if(crimeFragment==null){

            // here is empty. Because to delete an item from the list without selecting it in the two-pane layout.

        }else {
            // when you remove list item with using swipe, the detail screen is disappear.
            listFragment.getActivity().getSupportFragmentManager().beginTransaction().remove(crimeFragment).commit();
        }
    }
}

