package com.bignerdranch.android.criminalintent;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by danielmakover on 24/02/2018.
 */

public class CrimeListFragment extends Fragment {
    private RecyclerView mCrimeRecyclerView;
    private TextView mEmptyView;
    private CrimeAdapter mAdapter;
    private List<Crime> crimes;

    private OnDeleteCrimeListener mDeleteCallBack;

    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    private boolean mSubtitleVisible;

    private Callbacks mCallBacks;

    /*
    * Required interface for hosting Activities
    * */
    public interface Callbacks{
        void onCrimeSelected(Crime crime);
    }

    /*
    * Required interface for swipe-delete
    * */
    public interface OnDeleteCrimeListener {
        void onCrimeIdSelected(UUID crimeId);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mCallBacks = (Callbacks)context;
        mDeleteCallBack = (OnDeleteCrimeListener) context;
    }

    @Override
    public void onDetach(){
        super.onDetach();
        mCallBacks = null;
        mDeleteCallBack = null;
    }


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d("CrimeListFragment", "Crime List Fragment CREATED!");
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);

        if(mSubtitleVisible){
            subtitleItem.setTitle(R.string.hide_subtitle);
        }else{
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.new_crime:
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                mCallBacks.onCrimeSelected(crime);

                //conditional to only update List UI on tablet
                if(getActivity().findViewById(R.id.detail_fragment_container) != null){
                    updateUI();
                }

                return true;

            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle(){
        CrimeLab crimeLab = CrimeLab.get(getActivity());

        int crimeCount = crimeLab.getCrimes().size();
        String subtitle = getResources().getQuantityString(R.plurals.subtitle_plural, crimeCount, crimeCount);

        if(!mSubtitleVisible){
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity)getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        mCrimeRecyclerView = (RecyclerView) view.findViewById(R.id.crime_recycler_view);
        mEmptyView = (TextView) view.findViewById(R.id.empty_view);

        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if(savedInstanceState != null){
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }
        updateUI();

        setCrimeRecyclerViewItemTouchListener();
        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    public void setCrimeRecyclerViewItemTouchListener() {

        ItemTouchHelper.SimpleCallback itemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Crime crime= mAdapter.mCrimes.get(position);
                Log.d("CrimeListFragment", "onSwiped: " + crime.getId());
                mDeleteCallBack.onCrimeIdSelected(crime.getId());

            }
        };

        ItemTouchHelper iteItemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        iteItemTouchHelper.attachToRecyclerView(mCrimeRecyclerView);
    }

    public void deleteCrime(UUID crimeId) {
        Crime crime = CrimeLab.get(getActivity()).getCrime(crimeId);
        CrimeLab.get(getActivity()).deleteCrime(crime);
    }

    public void updateUI(){
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        crimes = crimeLab.getCrimes();

        //display blank recylcer viewer text
        if(crimes.isEmpty()){
            mEmptyView.setVisibility(View.VISIBLE);
            mCrimeRecyclerView.setVisibility(View.INVISIBLE);
        }else{
            mEmptyView.setVisibility(View.INVISIBLE);
            mCrimeRecyclerView.setVisibility(View.VISIBLE);
        }
        //END- display blank recycler viewer text

        if(mAdapter == null){
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        }else{
            mAdapter.setCrimes(crimes);
            mAdapter.notifyDataSetChanged();
        }

        updateSubtitle();
    }

    //the individual item in the recycler view
    private class CrimeHolder extends RecyclerView.ViewHolder {
            private TextView mTitleTextView;
            private TextView mDateTextView;
            private ImageView mSolvedView;

            private Crime mCrime;

            public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {
                super(inflater.inflate(R.layout.list_item_crime, parent, false));
                itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mCallBacks.onCrimeSelected(mCrime);
                        }
                    });
                mTitleTextView = (TextView) itemView.findViewById(R.id.crime_title);
                mDateTextView = (TextView) itemView.findViewById(R.id.crime_date);
                mSolvedView = (ImageView)itemView.findViewById(R.id.crime_solved);
            }


        public void bind(Crime crime){
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            //reformat the date
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
            String newDateFormat = format.format(Date.parse(mCrime.getDate().toString()));
            mDateTextView.setText(newDateFormat);

            mSolvedView.setVisibility(crime.isSolved() ? View.VISIBLE : View.GONE);

        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {
        private List<Crime> mCrimes;

        //constructor
        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @Override
        public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            return new CrimeHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(CrimeHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            holder.bind(crime);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        public void setCrimes(List<Crime> crimes){
            mCrimes = crimes;
        }
    }
}
