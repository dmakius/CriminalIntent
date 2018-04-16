package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by danielmakover on 04/03/2018.
 */

public class TimePickerFragment extends android.support.v4.app.DialogFragment {

    public static final String EXTRA_TIME = "come.bignerdranch.android.criminalintent.time";
    private static final String ARG_TIME = "time";

    private TimePicker mTimePicker;

    public static TimePickerFragment newInstance(Date date){
        Bundle args = new Bundle();
        args.putSerializable(ARG_TIME, date);

        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setArguments(args);

        return fragment;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //get the view and Inflate
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_time, null);

        Calendar calender = Calendar.getInstance();
        calender.setTime(calender.getTime());

        int hour = calender.get(Calendar.HOUR);
        int minute = calender.get(Calendar.MINUTE);

        mTimePicker = (TimePicker)v.findViewById(R.id.dialog_time_picker);
        mTimePicker.setHour(hour);
        mTimePicker.setMinute(minute);


        //create the dialog and return it
        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.time_of_day_title)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.M)
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {

                                //get the values picked by user
                                int hour = mTimePicker.getHour();
                                int minute = mTimePicker.getMinute();

                                //init a new date with DUMMY years, months, days - All We care about are the Hours and Minutes
                                Date date = new Date(0000, 00, 00, hour, minute);
                                sendResult(Activity.RESULT_OK, date);
                            }
                        })
                .create();
    }

    private void sendResult(int resultCode, Date time){
        if(getTargetFragment() == null){
            return;
        }

        //Create a NEW intent & add the time(Hours & Minutes)
        Intent intent = new Intent();
        intent.putExtra(EXTRA_TIME, time);

        //Send Data back to Hosting Fragment - Connection made with setTargetFragment(Fragment, REQUEST_CODE)
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
