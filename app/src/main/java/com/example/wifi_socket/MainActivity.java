package com.example.wifi_socket;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    public static boolean wifi_state=false, hotspot_state=false, ip_send_set=false;
    static EditText http, port, message;
    Button send, scan;
    static Context context;
    static GridView mess_list;
    static UserMessages userMessages;
    View bottomsheet;
    BottomSheetBehavior bottomSheetBehavior;
    static TextView your_ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = getApplicationContext();
        Settings themeColor = new Settings();
        init();
        send();
        res();
        scanpage();
        connection_state();
        startService(new Intent(getBaseContext(), NetworkRes.class));
    }

    private void connection_state() {

        final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int apState = 0;
        try {
            apState = (Integer) wifiManager.getClass().getMethod("getWifiApState").invoke(wifiManager);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        if (apState == 13) {
            // Ap Enabled
            hotspot_state=true;
            your_ip.setText(getLocalIpAddress());

        }else {

            hotspot_state=false;
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (mWifi.isConnected()) {
                // wifi conected
                wifi_state=true;
                your_ip.setText(getLocalIpAddress());

            }else {
                wifi_state=false;
                your_ip.setText("Not Connected");
                your_ip.setTextColor(Color.RED);

            }

        }

    }


    private void scanpage() {
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,ScanPage.class));
                res();
            }
        });
    }

    private void res() {
        Thread thread = new Thread(new MessRes());
        thread.start();
    }

    private void send() {
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = http.getText().toString();
                if (ip.equals("")){
                    Toast.makeText(MainActivity.this, "اول گیرنده را مشخص کنید", Toast.LENGTH_SHORT).show();
                }else if (ip.equals(your_ip.getText().toString())) {
                    Toast.makeText(MainActivity.this, "این که خودتی!", Toast.LENGTH_SHORT).show();
                }else {
                    MessSend m = new MessSend();
                    m.execute();
                }
            }
        });
    }

    private  void init() {
        send = (Button) findViewById(R.id.send);
        scan = (Button) findViewById(R.id.scan);
        http = (EditText) findViewById(R.id.http);
        port = (EditText) findViewById(R.id.port);
        message = (EditText) findViewById(R.id.message);
        mess_list = (GridView) findViewById(R.id.list_mess);
        your_ip=(TextView) findViewById(R.id.your_ip);
        userMessages=new UserMessages();
        View bottomsheet = findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomsheet);
        bottomsheet.setBackgroundColor(Settings.bottomsheetcolor);

        if (getLocalIpAddress() != null)
            your_ip.setText("your ip : "+getLocalIpAddress());
        else your_ip.setText("Not Connected!!");

        resize();
    }

    private void resize() {
        double h = (double) getDisplyHeight();
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setPeekHeight((int) Math.floor(0.1*h)+20);

        message.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence cs, int s, int c, int a) {}

            public void onTextChanged(CharSequence cs, int s, int b, int c) {
                if (message.getLineCount()>1)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
            }
        });

    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
                    .hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                if (intf.getName().contains("wlan")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
                            .hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()
                                && (inetAddress.getAddress().length == 4)) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException ex) {}
        return null;
    }

    public static int get_port() {
        return Integer.parseInt(port.getText().toString());
    }

    public static String get_ip() {
        return http.getText().toString();
    }

    public static String get_mess() {
        return message.getText().toString();

    }

    public int getDisplyHeight(){
        final WindowManager windowManager=getWindowManager();
        final Point size = new Point();
        int screenhight =0, actionbar=0,statusbar = 0;
        if (getActionBar() != null)
            actionbar=getActionBar().getHeight();
        int r = getResources().getIdentifier("status_bar_height","dimen","android");
        if (r>0){
            statusbar=getResources().getDimensionPixelSize(r);
        }
        int contop = (findViewById(android.R.id.content)).getTop();
        windowManager.getDefaultDisplay().getSize(size);
        screenhight=size.y;
        return screenhight-contop-actionbar-statusbar;
    }
}
