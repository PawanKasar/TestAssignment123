package com.example.airittestdemo.LocalDBAdapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import androidx.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

public class SqliteDBAdapter extends SQLiteOpenHelper {

    private static final String DatabaseName = "AIRIT.db";
    Context context;
    SQLiteDatabase sqLiteDatabase;
    private static final int version = 1;

    String DATABASE_CREATE_DEMO_CONTACT_LIST_TABLE= "create table DEMOCONTACTLISTTABLE (id integer primary key autoincrement, PersonName text UNIQUE, ContactImage text UNIQUE, ContactNumber text UNIQUE);";

    public SqliteDBAdapter(@Nullable Context context) {
        super(context, DatabaseName, null, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_DEMO_CONTACT_LIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS DEMOCONTACTLISTTABLE");
    }

    /**
    * Now Here goes your query functions I mean CRUD operations
    **/
    public long insertData( String PersonName, String ContactImage, String ContactNumber) {

        long returnV = 0;
        sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.beginTransaction();
        try {
            ContentValues con = new ContentValues();
            con.put("PersonName", PersonName);
            con.put("ContactImage", ContactImage);
            con.put("ContactNumber", ContactNumber);
            returnV = sqLiteDatabase.insert("DEMOCONTACTLISTTABLE", null, con);
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            returnV = 0;
        } finally {
            sqLiteDatabase.endTransaction();
        }
        return returnV;

    }

    /**Fetching Data From getRackMasterTable
     *
     * @return
     */
    // return count of getDataList
    public JSONArray getDataList() {
        SQLiteDatabase sqliteDatabase = this.getWritableDatabase();
        JSONArray resultSet = new JSONArray();
        JSONObject rowObject = null;
        sqliteDatabase.beginTransaction();
        try {
            Cursor cursor = sqliteDatabase.rawQuery("select * from  DEMOCONTACTLISTTABLE", null);

            cursor.moveToFirst();

            //
            while (cursor.isAfterLast() == false) {
                int totalColumn = cursor.getColumnCount();
                rowObject = new JSONObject();
                for (int i = 0; i < totalColumn; i++) {
                    if (cursor.getColumnName(i) != null) {
                        try {
                            rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                        } catch (Exception e) {
                            Log.d("TAG", e.getMessage());
                        }
                    }

                }
                resultSet.put(rowObject);
                cursor.moveToNext();
            }

            cursor.close();
            sqliteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            rowObject = null;
        } finally {
            sqliteDatabase.endTransaction();
        }
        return resultSet;
    }

    public boolean deleteItemPositionWise(int id)
    {
        return sqLiteDatabase.delete("DEMOCONTACTLISTTABLE", id + "=" + id,null) > 0;
    }
}
