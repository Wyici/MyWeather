package com.example.myweather;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;


public class MyConcernList extends AppCompatActivity {

    ArrayAdapter adapter;
    ListView MyConcernList;
    private List<String> city_nameList = new ArrayList<>();
    private List<String> city_codeList = new ArrayList<>();

    //初始化关注列表
    private void InitConcern() {
        MyDatabaseHelper dbHelper = new MyDatabaseHelper(this,"Concern.db",null,1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor  = db.rawQuery("select * from Concern",null);
        while(cursor.moveToNext()){
            String city_code = cursor.getString(cursor.getColumnIndex("city_code"));
            String city_name = cursor.getString(cursor.getColumnIndex("city_name"));
            city_codeList.add(city_code);
            city_nameList.add(city_name);
        }
    }
    //刷新ListView
    public void RefreshList(){
        //清空原来的两个列表
        city_nameList.removeAll(city_nameList);
        city_codeList.removeAll(city_codeList);
        //适配器更新
        adapter.notifyDataSetChanged();
        MyDatabaseHelper dbHelper = new MyDatabaseHelper(this,"Concern.db",null,1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor  = db.rawQuery("select * from Concern",null);
        while(cursor.moveToNext()){
            String city_code = cursor.getString(cursor.getColumnIndex("city_code"));
            String city_name = cursor.getString(cursor.getColumnIndex("city_name"));
            city_codeList.add(city_code);
            city_nameList.add(city_name);
        }
    }
    @Override
    protected void onStart(){
        super.onStart();
        //刷新
        RefreshList();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_concern_list);
        MyConcernList = findViewById(R.id.MyConcernList);
        //初始化
        InitConcern();
        MyDatabaseHelper dbHelper = new MyDatabaseHelper(this,"Concern.db",null,1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //适配器操作
        adapter = new ArrayAdapter(MyConcernList.this,android.R.layout.simple_list_item_1,city_nameList);
        MyConcernList.setAdapter(adapter);
        //item点击事件
        MyConcernList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void  onItemClick(AdapterView<?> parent, View view , int position , long id){
                //获得citycode，打开对应天气页面
                String tran = city_codeList.get(position);
                Intent intent = new Intent(MyConcernList.this,Weather.class);
                intent.putExtra("trancitycode",tran);
                startActivity(intent);
            }
        });
        //item长按事件，取消关注
        MyConcernList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //对话框提示
                AlertDialog.Builder builder = new AlertDialog.Builder(MyConcernList.this);
                builder.setTitle("提示");
                builder.setMessage("取消关注？");
                builder.setPositiveButton("取消关注", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //获得citycode
                        String id = city_codeList.get(position);
                        //从表中删除
                        db.delete("Concern","city_code = ?",new String[]{id});
                        //刷新
                        RefreshList();
                    }
                });
                builder.setNegativeButton("返回",null);
                builder.show();
                return true;
            }
        });
    }
}