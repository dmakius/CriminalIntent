package com.bignerdranch.android.criminalintent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bignerdranch.android.criminalintent.database.CrimeBaseHelper;
import com.bignerdranch.android.criminalintent.database.CrimeCursorWrapper;
import com.bignerdranch.android.criminalintent.database.CrimeDbSchema.CrimeTable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by danielmakover on 24/02/2018.
 */

public class CrimeLab {
    private static CrimeLab sCrimeLab;

    private Context mContext;
    private SQLiteDatabase mDatabase;
    private List<Crime> mCrimeList;


    // check to avoid creating the object again
    public static CrimeLab get(Context context){
        //if NULL create a new object
        if(sCrimeLab == null){
            sCrimeLab = new CrimeLab(context);

        }
        //if not null return what there is already
        return sCrimeLab;
    }

    //constructor - Add 100 Crimes to List
    private CrimeLab(Context context){
        mContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(mContext).getWritableDatabase();

    }

    //return the entire list
    public List<Crime> getCrimes(){
       List<Crime> crimes = new ArrayList<>();
//       Log.d("CrimeLab", "getting ALL Crimes");

       CrimeCursorWrapper cursor = queryCrimes(null, null);

       try{
           cursor.moveToFirst();
           while(!cursor.isAfterLast()){
               crimes.add(cursor.getCrime());
               cursor.moveToNext();
           }
       }finally{
           cursor.close();
       }
       return crimes;
    }

    public static ContentValues getContentValues(Crime crime){
        //saving crime to DB
        ContentValues values = new ContentValues();
        values.put(CrimeTable.Cols.UUID, crime.getId().toString());
        values.put(CrimeTable.Cols.TITLE, crime.getTitle());
        values.put(CrimeTable.Cols.DATE, crime.getDate().getTime());
        values.put(CrimeTable.Cols.TIME, crime.getTime().getTime());
        values.put(CrimeTable.Cols.SOLVED, crime.isSolved()? 1: 0);
        values.put(CrimeTable.Cols.SUSPECT, crime.getSuspect());

        return values;
    }

    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs){
        Cursor cursor = mDatabase.query(
                CrimeTable.NAME,
                null, //columns - selects all columns
                whereClause,
                whereArgs,
                null, //groupBy
                null,  //having
                null  //orderBy
        );

        return new CrimeCursorWrapper(cursor);
    }

    public void addCrime(Crime c){
//        Log.d("CrimeLab", "adding Crime to DB with date " + c.getDate());
        ContentValues values =  getContentValues(c);
        mDatabase.insert(CrimeTable.NAME, null, values);
    }


    //return a specific item from the list
    public Crime getCrime(UUID id){
//        Log.d("CrimeLab", "Fetching Crime using UUID " + id);

        CrimeCursorWrapper cursor = queryCrimes(
                CrimeTable.Cols.UUID + " = ?",
                new String[]{id.toString()}
        );

        try{
            if(cursor.getCount() == 0){
                return null;
            }

            cursor.moveToNext();
            return cursor.getCrime();
        }finally{
            cursor.close();
        }
    }

    //This function ONLY returns a file. It does NOT create one
    public File getPhotoFile(Crime crime){
        File filesDir = mContext.getFilesDir();
        return new File(filesDir, crime.getPhotoFileName());
    }

    public void updateCrime(Crime crime){
        String uuidString = crime.getId().toString();
        ContentValues values = getContentValues(crime);

        mDatabase.update(CrimeTable.NAME, values,
                CrimeTable.Cols.UUID + " = ? ",
                new String[] {uuidString});
    }

    public  void deleteCrime(Crime crime){
        String uuidString = crime.getId().toString();
        mDatabase.delete(CrimeTable.NAME,
                CrimeTable.Cols.UUID + " = ? ",
                new String[] {uuidString});
    }

    public int getCrimeNumber(UUID crimeId) {
        //query ALL of the crime from the DB
        CrimeCursorWrapper cursor = queryCrimes(null, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
          if(crimeId.equals(cursor.getCrime().getId())){
              return i;
          }
           cursor.moveToNext();
        }
        return 0;
    }
}
