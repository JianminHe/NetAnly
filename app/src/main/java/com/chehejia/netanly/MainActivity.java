package com.chehejia.netanly;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;



public class MainActivity extends AppCompatActivity {

    private static final int READ_PHONE_STATE_REQUEST = 37;
    EditText startDateEdit;
    EditText intervalEdit;
    Button getButton;
    private Gson gson = new Gson();
    static Context context =null;
    static Boolean runner = false;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestPermissions();
        startDateEdit = (EditText) findViewById(R.id.date);
        intervalEdit = (EditText) findViewById(R.id.interval);
        getButton = (Button) findViewById(R.id.get_button);
        //设置默认日期为当天
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        String date=sdf.format(new java.util.Date());
        startDateEdit.setText(date);
    }

    public long getStartDate() {
        String startDate = startDateEdit.getText().toString();
        long date = DateUtil.stringToDate(startDate);
        return date;
    }

    public long getTimeInterval() {
        String interval = intervalEdit.getText().toString();
        int time = 0;
        try {
            time = Integer.parseInt(interval);
        } catch (NumberFormatException e) {
            Log.e("NetAnly", e.getMessage().toString());
            time = 60;
        }

        //最小采样间隔60s
        if (time < 60)
            time = 60;

        return time;
    }

    public void getNetworkTraffic(View view){

//发现后台在运行，直接返回
        if (runner == true ) {
            Log.e("NetAnly", "Running now, exsit");
            return;
        }
        context= this;

        new Thread(new Runnable(){
            @Override
            public void run() {
                runner = true;
                context.getClass();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    long startDate = getStartDate();
                    long timeInterval = getTimeInterval();

                    List<Network> networks = new ArrayList<Network>();
                    long currentTime = System.currentTimeMillis();

                    NetworkStatsManager networkStatsManager = (NetworkStatsManager) getApplicationContext().getSystemService(Context.NETWORK_STATS_SERVICE);
                    NetworkStatsHelper networkStatsHelper = new NetworkStatsHelper(networkStatsManager);

                    //开始取样,最大取样时间1天
                    for(long i=startDate; i<= currentTime && i< (startDate + 24*60*60*1000); i = i + timeInterval*1000) {

                        long mobileRx = networkStatsHelper.getAllRxBytesMobile(context,startDate, i + timeInterval*1000);
                        long mobileTx = networkStatsHelper.getAllTxBytesMobile(context,startDate, i + timeInterval*1000 );
                        Network  n = new Network(DateUtil.longToDate(i + timeInterval*1000), mobileRx,mobileTx);
                        networks.add(n);
                    }
                    Type type=new TypeToken<List<Network>>(){}.getType();
                    String networkJsonList=gson.toJson(networks, type);
                    System.out.println(networkJsonList);
//                    Log.e("test", networkJsonList);
                    //准备csv文件内容
                    StringBuilder sb = new StringBuilder();
                     sb.append("DATE,RX,TX\n");
                    for(int i = 0; i<networks.size(); i++){
                        Network tmp = networks.get(i);
                        sb.append(tmp.getDate() + "," + tmp.getRx() + "," + tmp.getTx() + "\n");
                    }

                    //创建文件 命名方式NetAnly + 日期

                    Date date = new Date();
                    SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd");


                    try {
                        FileWriter mFile = new FileWriter("/mnt/sdcard/netanly" + dateFormat.format(date) + ".csv");
                        mFile.write(sb.toString());
                        mFile.close();
                    } catch (IOException e) {
                        Log.e("NetAnly error", e.getMessage().toString());
                    }




                    runner = false;

                }
            }}
        ).start();



        //由于获取一天网络带宽耗时较长，需要把按钮disable
        //恢复按钮可点击状态


    }



    //获取权限相关代码
    private void requestPermissions() {

        if (!hasPermissionToReadNetworkHistory()) {
            return;
        }
        if (!hasPermissionToReadPhoneStats()) {
            requestPhoneStateStats();
        }
    }
    //获取权限相关代码
    private boolean hasPermissions() {
        return hasPermissionToReadNetworkHistory() && hasPermissionToReadPhoneStats();
    }
    //获取权限相关代码
    private boolean hasPermissionToReadNetworkHistory() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        final AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        if (mode == AppOpsManager.MODE_ALLOWED) {
            return true;
        }
        appOps.startWatchingMode(AppOpsManager.OPSTR_GET_USAGE_STATS,
                getApplicationContext().getPackageName(),
                new AppOpsManager.OnOpChangedListener() {
                    @Override
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onOpChanged(String op, String packageName) {
                        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                                android.os.Process.myUid(), getPackageName());
                        if (mode != AppOpsManager.MODE_ALLOWED) {
                            return;
                        }
                        appOps.stopWatchingMode(this);
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        if (getIntent().getExtras() != null) {
                            intent.putExtras(getIntent().getExtras());
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplicationContext().startActivity(intent);
                    }
                });
        requestReadNetworkHistoryAccess();
        return false;
    }
    //获取权限相关代码
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void requestReadNetworkHistoryAccess() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }
    //获取权限相关代码
    private boolean hasPermissionToReadPhoneStats() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
            return false;
        } else {
            return true;
        }
    }
    //获取权限相关代码
    private void requestPhoneStateStats() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, READ_PHONE_STATE_REQUEST);
    }

    public static void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
