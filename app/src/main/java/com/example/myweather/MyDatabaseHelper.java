package com.example.myweather;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class MyDatabaseHelper extends SQLiteOpenHelper {//数据库创建
    //创建Weather表，字段有id和data
    public static final String CREATE_NOTE = "create table Weather("
            +"id integer primary key not null,"
            +"data String not null)";
    //创建Concern表，字段有城市代码和城市名
    public static final String CREATE_CONCERN = "create table Concern("
            +"city_code String primary key not null,"
            +"city_name String not null)";

    private Context mContext;
    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context,name,factory,version);
        mContext = context;
    }
    @Override
    public void  onCreate(SQLiteDatabase db){//第一次连接时，创建两个表
        db.execSQL(CREATE_NOTE);
        db.execSQL(CREATE_CONCERN);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }
}
