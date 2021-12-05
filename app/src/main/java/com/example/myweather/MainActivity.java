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
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private List<Integer> idList = new ArrayList<>();
    private List<Integer> pidList = new ArrayList<>();//级联关联上级的id
    private List<String> city_nameList = new ArrayList<>();//城市名
    private List<String> city_codeList = new ArrayList<>();//城市编号
    ArrayAdapter adapter;//适配器，数组适配
    Button select,myconcern;
    EditText input;
    ListView first_list;//一级城市

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
                //在主页面显示省级信息和直辖市信息，关联上级id为0
                if(pid == 0 ) {
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
            //每次读取文件的缓存，加到字符串后
            while ((line = bufferedReader.readLine()) != null) {
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
        setContentView(R.layout.activity_main);
        select = findViewById(R.id.select);
        select.setOnClickListener(this);
        myconcern = findViewById(R.id.myconcern);
        myconcern.setOnClickListener(this);
        input = findViewById(R.id.input);
        //获取城市信息，存到city.json中
        String responseData = getJson("city.json",this);
        //解析city.json文件中的json对象
        parseJSONWithJSONObject(responseData);
        //通过适配器，在listview中显示一级城市名
        adapter = new ArrayAdapter(MainActivity.this,android.R.layout.simple_list_item_1,city_nameList);
        first_list = findViewById(R.id.first_list);
        first_list.setAdapter(adapter);//配置适配器

        first_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //获得点击一级城市的id
                int tran = idList.get(position);
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                //将点击一级城市的id通过intent传递给second_activity，用来进行二级城市pid匹配
                intent.putExtra("tran",tran);
                //通过显式intent启动
                startActivity(intent);
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select:
                //获得输入城市id
                String research_citycode = String.valueOf(input.getText());
                if (research_citycode.length() < 9) {
                    //城市id不能大于9
                    Toast.makeText(this, "数字长度不能小于九位！", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, Weather.class);
                    intent.putExtra("trancitycode", research_citycode);//通过intent传递数据
                    startActivity(intent);//通过显式intent启动
                }
                break;
            case R.id.myconcern://打开关注列表
                Intent intent = new Intent(MainActivity.this, MyConcernList.class);
                startActivity(intent);//通过显式intent启动
                break;
        }
    }
}