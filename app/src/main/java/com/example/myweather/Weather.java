package com.example.myweather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Weather extends AppCompatActivity implements View.OnClickListener{
    //解析JSON获得的相关信息
    private String city;
    private App.forecast fc0, fc1, fc2;//今天、明天、后天的预报
    private String parent;//上级城市
    private String updateT;//更新时间
    private String t;
    private String date;//当前日期
    private String shidu;//当前湿度
    private String pm25;//当前pm2.5
    private String pm10;//当前pm10
    private String quality;//当前空气质量
    private String tem;//当前温度
    private List<App.forecast> forecasts;

    //今天、明天、后天的天气情况
    private String ymd,ymd0,ymd1,ymd2;//日期
    private String week,week0,week1,week2;//周几
    private String highTem,highTem0, highTem1, highTem2;//最高温度
    private String lowTem,lowTem0, lowTem1, lowTem2;//最低温度
    private String type,type0,type1,type2;//天气类型

    TextView textView;
    Button concern,refresh;
    String researchcitycode;//citycode
    String cityStr="";//显示的天气信息
    int databaseid;
    String databasedata;
    //用来标记数据库中是否有缓存，0表示没有缓存，需要访问在线API，1表示数据库中有缓存，2表示刷新（重新访问在线API获得数据）
    int sign = 1;
    //为活动设置菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        textView = findViewById(R.id.textView);
        concern = findViewById(R.id.concern);
        refresh = findViewById(R.id.refresh);
        concern.setOnClickListener(this);
        refresh.setOnClickListener(this);
        //获得intent传递过来的city_code
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        researchcitycode = extras.getString("trancitycode");
        //获得可写数据库
        MyDatabaseHelper dbHelper = new MyDatabaseHelper(this,"Weather.db",null,1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor  = db.query("Weather",new String[]{"id","data"},"id=?",new String[]{researchcitycode+""},null,null,null);
        //查找数据库，若有缓存则从数据库中读取
        if(cursor.moveToFirst()) {
            do {
                databaseid = cursor.getInt(cursor.getColumnIndex("id"));//citycode
                databasedata = cursor.getString(cursor.getColumnIndex("data"));//天气信息字符串
            } while (cursor.moveToNext());
            cursor.close();
        }
        int tranformat = 0;
        tranformat = Integer.parseInt(researchcitycode);
        if(databaseid ==  tranformat ){
            //若数据库中有缓存，直接解析并输出
            sign = 1;
            showResponse(databasedata);
        }
        else{
            //若数据库中没有缓存，访问在线天气API，请求数据
            sign = 0;
            sendRequestWithOkHttp();
        }

    }
    //访问在线天气API，请求数据
    private void sendRequestWithOkHttp(){
        //在子线程中请求网络，防止阻塞主线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //创建OkHttpClient对象
                    OkHttpClient client = new OkHttpClient();
                    //请求接口，创建Request对象
                    Request request = new Request.Builder().url("http://t.weather.itboy.net/api/weather/city/"+researchcitycode).build();
                    //得到Response对象
                    Response response = client.newCall(request).execute();
                    if(response.isSuccessful())
                    {
                        //body().string()只能调用一次
                        String responseData = response.body().string();

                        showResponse(responseData);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void parseJSONWithFastJSON(String jsonData){
        if(jsonData.length()<100){
            Log.d("M","城市ID不存在");
            Toast.makeText(this,"城市ID不存在，请重新输入",Toast.LENGTH_LONG).show();
            Weather.this.setResult(RESULT_OK,getIntent());
            Weather.this.finish();
        }
        else {
            //将JSON数据解析成为app实体类并进行操作
            App app = JSON.parseObject(jsonData, App.class);
            t = app.getTime();
            date = app.getDate();

            //城市相关信息
            App.CityInfo cityInfo = app.getCityInfo();
            city = cityInfo.getCity();
            parent = cityInfo.getParent();
            updateT = cityInfo.getUpdateTime();

            //只能获得当前的信息
            App.data data = app.getData();
            shidu = data.getShidu();//得到湿度
            pm10 = data.getPm10();//得到pm10
            pm25 = data.getPm25();//得到pm25
            quality = data.getQuality();//得到空气质量
            tem = data.getWendu();//得到温度
            forecasts = data.getForecast();//可以获得今天到未来14天的预报信息

            //今天
            fc0 = forecasts.get(0);
            highTem0 = fc0.getHigh();//得到今天的最高温度
            lowTem0 = fc0.getLow();//得到今天的最低温度
            week0 = fc0.getWeek();//得到今天的星期
            ymd0 = fc0.getYmd();//得到今天的日期
            type0 = fc0.getType();//得到今天天气类型

            //明天
            fc1 = forecasts.get(1);
            highTem1 = fc1.getHigh();//得到明天的最高温度
            lowTem1 = fc1.getLow();//得到明天的最低温度
            week1 = fc1.getWeek();//得到明天的星期
            ymd1 = fc1.getYmd();//得到明天的日期
            type1 = fc1.getType();//得到明天天气类型

            //后天
            fc2 = forecasts.get(2);
            highTem2 = fc2.getHigh();//得到后天的最高温度
            lowTem2 = fc2.getLow();//得到后天的最低温度
            week2 = fc2.getWeek();//得到后天的星期
            ymd2 = fc2.getYmd();//得到后天的日期
            type2 = fc2.getType();

            //昨天
            App.data.yesterday yesterday = data.getYesterday();
            ymd = yesterday.getYmd();//得到昨天的日期
            week = yesterday.getWeek();//得到昨天的星期
            highTem = yesterday.getHigh();//得到昨天的最高温度
            lowTem = yesterday.getLow();//得到昨天的最低温度
            type = yesterday.getType();

            if (sign == 0) {
                //此时数据库中没有缓存，需要将在线天气API查询到的数据存入数据库
                MyDatabaseHelper dbHelper = new MyDatabaseHelper(this, "Weather.db", null, 1);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("id", researchcitycode);
                //存入的是JSON字符串
                values.put("data", jsonData);
                db.insert("Weather", null, values);
                Log.d("TAG", "数据库写入成功");
            } else if (sign == 1) {
                //数据库中数据已存在
                Log.d("TAG", "数据已存在");
            } else {
                //更新数据库，需要更新对应id在线天气API查询到的数据
                MyDatabaseHelper dbHelper = new MyDatabaseHelper(this, "Weather.db", null, 1);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("id", researchcitycode);
                values.put("data", jsonData);
                db.update("Weather", values, "id=?", new String[]{researchcitycode + ""});
                Log.d("TAG", "数据库更新成功");
            }
        }
    }
    private void showResponse(final String response){
        // 通过runOnUiThread()方法回到主线程处理逻辑，更新UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //利用第三方FastJSON解析
                parseJSONWithFastJSON(response);
                String CityshowString;
                CityshowString = "所在地:" + parent + " "  + city + "\n";
                CityshowString = CityshowString + "当前日期:" + date + "\n" + "当前温度:" + tem + "\n";
                CityshowString = CityshowString + "当前空气湿度:" + shidu + "\n" + "当前空气质量:" + quality + "  " + "pm10:" + pm10 + "  " + "pm2.5:" + pm25 + "\n";
                CityshowString = CityshowString + "日期:" + ymd0 + " " + week0 + "\n" + "更新时间:" + updateT + "\n" + "数据更新时间:" + t + "\n";
                CityshowString = CityshowString + "最低温度:" + lowTem0 + "  " + "最高温度:" + highTem0 + "\n" + "天气:" + type0 + "\n";
                textView.setText(CityshowString);
            }
        });

    }
    @Override
    //菜单项的点击事件
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.yesterday:
                cityStr = "所在地:" + parent + " "  + city + "\n";
                cityStr = cityStr + "当前日期:" + date + "\n" + "当前温度:" + tem + "\n";
                cityStr = cityStr + "当前空气湿度:" + shidu + "\n" + "当前空气质量:" + quality + "  " + "pm10:" + pm10 + "  " + "pm2.5:" + pm25 + "\n";
                cityStr = cityStr + "日期:" + ymd + " " + week + "\n" + "更新时间:" + updateT + "\n" + "数据更新时间:" + t + "\n";
                cityStr = cityStr + "最低温度:" + lowTem + "  " + "最高温度:" + highTem + "\n" + "天气:" + type + "\n";
                textView.setText(cityStr);
                break;
            case  R.id.today:
                cityStr = "所在地:" + parent + " "  + city + "\n";
                cityStr = cityStr + "当前日期:" + date + "\n" + "当前温度:" + tem + "\n";
                cityStr = cityStr + "当前空气湿度:" + shidu + "\n" + "当前空气质量:" + quality + "  " + "pm10:" + pm10 + "  " + "pm2.5:" + pm25 + "\n";
                cityStr = cityStr + "日期:" + ymd0 + " " + week0 + "\n" + "更新时间:" + updateT + "\n" + "数据更新时间:" + t + "\n";
                cityStr = cityStr + "最低温度:" + lowTem0 + "  " + "最高温度:" + highTem0 + "\n" + "天气:" + type0 + "\n";
                textView.setText(cityStr);
                break;
            case R.id.tomorrow:
                cityStr = "所在地:" + parent + " "  + city + "\n";
                cityStr = cityStr + "当前日期:" + date + "\n" + "当前温度:" + tem + "\n";
                cityStr = cityStr + "当前空气湿度:" + shidu + "\n" + "当前空气质量:" + quality + "  " + "pm10:" + pm10 + "  " + "pm2.5:" + pm25 + "\n";
                cityStr = cityStr + "日期:" + ymd1 + " " + week1 + "\n" + "更新时间:" + updateT + "\n" + "数据更新时间:" + t + "\n";
                cityStr = cityStr + "最低温度:" + lowTem1 + "  " + "最高温度:" + highTem1 + "\n" + "天气:" + type1 + "\n";
                textView.setText(cityStr);
                break;
            case  R.id.forecast:
                cityStr = "所在地:" + parent + " "  + city + "\n";
                cityStr = cityStr + "当前日期:" + date + "\n" + "当前温度:" + tem + "\n";
                cityStr = cityStr + "当前空气湿度:" + shidu + "\n" + "当前空气质量:" + quality + "  " + "pm10:" + pm10 + "  " + "pm2.5:" + pm25 + "\n";
                cityStr = cityStr + "日期:" + ymd2 + " " + week2 + "\n" + "更新时间:" + updateT + "\n" + "数据更新时间:" + t + "\n";
                cityStr = cityStr + "最低温度:" + lowTem2 + "  " + "最高温度:" + highTem2 + "\n" + "天气:" + type2 + "\n";
                textView.setText(cityStr);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.concern:
                MyDatabaseHelper dbHelper = new MyDatabaseHelper(this, "Concern.db", null, 1);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                Cursor cursor  = db.query("Concern",new String[]{"city_code","city_name"},"city_code=?",new String[]{researchcitycode+""},null,null,null);
                //查找数据库，是否已经加入到关注列表
                if(cursor.getCount()!=0)
                {
                    Toast.makeText(this, "已加入关注列表！", Toast.LENGTH_LONG).show();
                }
                //未加入关注列表，在数据库对应表中插入数据
                else {
                    ContentValues values = new ContentValues();
                    values.put("city_code", researchcitycode);
                    values.put("city_name", city);
                    db.insert("Concern", null, values);         //加入数据库中的关注表
                    Toast.makeText(this, "关注成功！", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.refresh:
                //更新数据
                sign = 2;
                sendRequestWithOkHttp();//访问在线天气API，请求数据
                Log.d("TAG","数据库刷新成功");
                break;
        }
    }
}