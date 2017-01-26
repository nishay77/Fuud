package com.nishay.fuud.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.nishay.fuud.Data;

import java.util.ArrayList;

/**
 * Created by Nishay on 8/30/2016.
 */
public class SQLHelper extends SQLiteAssetHelper {
    private static String DB_PATH;
    private static String DB_NAME = "fuud.db";
    private SQLiteDatabase db;
    private final Context context;
    private static final int DB_VER = 1;

    public SQLHelper(Context context) {
        super(context, DB_NAME, null, DB_VER);
        this.context = context;

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }


    public Data getRandomRow() {
        db = getReadableDatabase();
        Cursor c = db.rawQuery("Select * from recipes order by RANDOM() limit 1;", null);
        if(c != null && c.getCount() == 1) {
            c.moveToFirst();
            String url = c.getString(c.getColumnIndex("url"));
            String image = c.getString(c.getColumnIndex("image"));
            String name = c.getString(c.getColumnIndex("name"));
            db.close();
            return new Data(image, name, url);
        }
        else {
            db.close();
            return null;
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("upgrade", "yes");
    }

    public void saveRecipe(Data data) {
        db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("url", data.getLink());
        values.put("image", data.getImagePath());
        values.put("name", data.getName());
        db.insert("saved_recipes", null, values);
        db.close();
    }

    public ArrayList<Data> getSavedRecipes() {
        ArrayList<Data> data = new ArrayList<>();
        db = getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from saved_recipes order by _id", null);

        if(cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while(!cursor.isAfterLast()) {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String image = cursor.getString(cursor.getColumnIndex("image"));
                String url = cursor.getString(cursor.getColumnIndex("url"));
                data.add(new Data(image, name, url));
                cursor.moveToNext();
            }
        }
        cursor.close();

        return data;
    }

    public void deleteSaved(String name, String url) {
        db = getWritableDatabase();
        StringBuilder sb = new StringBuilder();
        sb.append("delete from saved_recipes");
        if(!name.equals("") && !url.equals("")) {
            sb.append(" where name = \"" + name + "\" and url = \"" + url + "\"");
        }
        db.execSQL(sb.toString());
    }

}
