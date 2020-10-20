package com.yeck.networktool;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public Handler mhandler;
    String ipq;

    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : interfaces) {
                List<InetAddress> addresses = Collections.list(networkInterface.getInetAddresses());
                for (InetAddress address : addresses) {
                    if (!address.isLoopbackAddress()) {
                        String sAddr = address.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':') < 0;
                        if (useIPv4) {
                            if (isIPv4) {
                                return sAddr;
                            }
                        } else {
                            if (!isIPv4) {
                                // 删除ip6区域后缀
                                int delim = sAddr.indexOf('%');
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            return "获取失败\n" + e;
        }
        return "";
    }

    public void copy(String content) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("simple text", content);
        assert clipboard != null;
        clipboard.setPrimaryClip(clip);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView textView = findViewById(R.id.textView);
        final EditText editText = findViewById(R.id.editText);
        final Button button = findViewById(R.id.button2);
        TextView textView2 = findViewById(R.id.textView2);
        final TextView textView3 = findViewById(R.id.textView3);
        button.setText("查询中");
        button.setClickable(false);
        editText.setClickable(false);
        String ip_private_v4 = "内网IP:\n\nIPV4:  " + getIPAddress(true);
        String ip_private_v6 = "IPV6:  " + getIPAddress(false);
        textView2.setText(ip_private_v6);
        textView3.setText(ip_private_v4);
        class Mhandler extends Handler {
            @Override
            public void handleMessage(Message msg) {
                String json = msg.obj.toString();
//                if(json.startsWith("\ufeff")){
//                    json = json.substring(1);
//                }
//                String json = json_str.substring(json_str.indexOf("{"), json_str.lastIndexOf("}") + 1);
                textView.setText(json_analyse(json));
                editText.setText(ipq);
                button.setClickable(true);
                editText.setClickable(true);
                button.setText("查询");
//                try {
//                    JSONObject jsonObject = new JSONObject(json);
//                    String country = jsonObject.optString("country");
//                    String ip = jsonObject.optString("query");
//                    String region = jsonObject.optString("regionName");
//                    String city = jsonObject.optString("city");
//                    String sum = "IP:  " + ip + "\n\n" + country + " " + region + " " + city + "\n";
//                    editText.setText(ip);
//                    textView.setText(sum);
//                } catch (JSONException e) {
//                    textView.setText(e.toString());
//                }
            }
        }

        textView3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {  //重写监听器中的onLongClick()方法
                String ip = getIPAddress(true);
                copy(ip);
                Toast.makeText(getApplicationContext(), ip + "\n\n已复制到剪贴板", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        textView2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {  //重写监听器中的onLongClick()方法
                String ip = getIPAddress(false);
                copy(ip);
                Toast.makeText(getApplicationContext(), ip + "\n\n已复制到剪贴板", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        mhandler = new Mhandler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String res;
                try {
                    res = Jsoup.connect("http://ip-api.com/json/?lang=zh-CN").ignoreContentType(true).execute().body();
                } catch (Exception e) {
                    res = e.toString();
                }


                Message msg = Message.obtain();
                msg.obj = res;
                msg.what = 1;
                mhandler.sendMessage(msg);
            }
        }).start();
    }

    public void clicked(View view) {
        final EditText editText = findViewById(R.id.editText);
        final String url = "http://ip-api.com/json/" + editText.getText() + "?lang=zh-CN";
        final TextView textView = findViewById(R.id.textView);
        final Button button = findViewById(R.id.button2);

        button.setText("查询中");
        button.setClickable(false);
        editText.setClickable(false);
        class Mhandler extends Handler {
            @Override
            public void handleMessage(Message msg) {
                String json = msg.obj.toString();
                textView.setText(json_analyse(json));
                button.setClickable(true);
                editText.setClickable(true);
                button.setText("查询");
//                textView.setText(json);
//                try {
//                    JSONObject jsonObject = new JSONObject(json);
//                    String country = jsonObject.optString("country");
//                    String ip = jsonObject.optString("query");
//                    String region = jsonObject.optString("regionName");
//                    String city = jsonObject.optString("city");
//                    String sum = "IP:  " + ip + "\n\n" + country + " " + region + " " + city + "\n";
//                    textView.setText(sum);
//                } catch (JSONException e) {
//                    textView.setText(e.toString());
//                }
            }
        }
        mhandler = new Mhandler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String res;
                try {
                    res = Jsoup.connect(url).ignoreContentType(true).execute().body();
                } catch (Exception e) {
                    res = e.toString();
                }
                Message msg = Message.obtain();
                msg.obj = res;
                msg.what = 1;
                mhandler.sendMessage(msg);
            }
        }).start();
    }

    private String json_analyse(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            String status = jsonObject.optString("status");
            String country = jsonObject.optString("country");
            String ip = jsonObject.optString("query");
            ipq = ip;
            String region = jsonObject.optString("regionName");
            String city = jsonObject.optString("city");
            String lat = jsonObject.optString("lat");
            String lon = jsonObject.optString("lon");
            String timezone = jsonObject.optString("timezone");
            if (status.equals("fail")) {
                String message = jsonObject.optString("message");
                if (message.equals("invalid query")) {
                    return "IP: " + ip + "\n\n地址格式错误";
                } else if (message.equals("private range")) {
                    return "IP: " + ip + "\n\n该地址为局域网地址";
                }
            }
            return "\n外网IP:  " + ip
                    + "\n\n" + country
                    + " " + region + " " + city
                    + "\n北纬:" + lat
                    + "\n东经:" + lon
                    + "\n时区:" + timezone;
        } catch (JSONException e) {
            return e.toString();
        }
    }
}
