package com.example.myweather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SecondActivity extends AppCompatActivity implements View.OnClickListener{


    private List<Integer> idList = new ArrayList<>();
    private List<Integer> pidList = new ArrayList<>();//级联关联上级的id
    private List<String> city_nameList = new ArrayList<>();//城市名
    private List<String> city_codeList = new ArrayList<>();//城市编号
    ListView second_list;//二级城市
    Button myconcern;
    int tranid = 0;
    ArrayAdapter adapter;
    /*
        json的相关操作
     */
    //将getJson()得到的Json字符串转换为JSONObject的数组
    private void parseJSONWithJSONObject(String jsonData){
        try{
            JSONArray jsonArray = new JSONArray(jsonData);
            for(int i = 0;i<jsonArray.length();i++){
                //获得JSONobject对象
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                //解析JSONobject对象，将对应值添加到list
                Integer id = jsonObject.getInt("id");
                Integer pid = jsonObject.getInt("pid");
                String city_code = jsonObject.getString("city_code");
                String city_name = jsonObject.getString("city_name");
                //mainactivity传递过来的tranid是二级城市的关联上级id
                if(pid == tranid ) {
                    //将选择的一级城市对应的二级城市全部显示
                    idList.add(id);
                    pidList.add(pid);
                    city_codeList.add(city_code);
                    city_nameList.add(city_name);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //得到assets文件夹下的city.json的字符串形式的json
    public static String getJson(String fileName, Context context) {
        //转换字符串
        StringBuilder stringBuilder = new StringBuilder();
        try {
            //从给定位置获取文件，assets文件夹下的city.json文件
            InputStream is = context.getAssets().open(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
            String line;
            //每次读取文件的缓存
            while ((line = bufferedReader.readLine()) != null) {
                //加到字符串后
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        myconcern = findViewById(R.id.concern);
        myconcern.setOnClickListener(this);
        second_list = findViewById(R.id.second_list);

        //接受Mainactivity传递过来的tran，实际是一级城市的id用来与二级城市pid匹配
        Intent intent = getIntent();
        tranid = intent.getIntExtra("tran",-1);

        //获取城市信息，存到city.json中
        String responseData = getJson("city.json",this);
        //解析city.json文件中的json对象
        parseJSONWithJSONObject(responseData);
        /*
            listview 适配器相关
         */
        adapter = new ArrayAdapter(SecondActivity.this,android.R.layout.simple_list_item_1,city_nameList);
        second_list.setAdapter(adapter);
        second_list = findViewById(R.id.second_list);
        //点击城市传递city_code获取天气信息
        second_list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void  onItemClick(AdapterView<?> parent, View view , int position , long id){
                String trancode = city_codeList.get(position);
                //intent将city_code信息传递给Weather并开启活动
                Intent intent = new Intent(SecondActivity.this, Weather.class);
                intent.putExtra("trancitycode",trancode);
                startActivity(intent);
            }
        });
    }
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.concern)
        {
            Intent intent = new Intent(SecondActivity.this, MyConcernList.class);
            startActivity(intent);//通过显式intent启动
        }
    }
}