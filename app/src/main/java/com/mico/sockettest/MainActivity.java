package com.mico.sockettest;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private EditText ip;
    private EditText port;
    private EditText send_info;
    private Button conn;
    private Button send;
    private TextView info;

    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    private String context;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.i("====Handler Info===", context);
            info.setText(context);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        conn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new myThread().start();

            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = send_info.getText().toString();
                Log.i("====Info===", data);
                if (data != null) {
                    if (socket.isConnected()) {
                        if (!socket.isOutputShutdown()) {
                            try {
                                out.write(data);
                                out.flush();
                                //out.close();
                                Log.i("====Info socket===", data);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.i("====Warning===", "Output Shutdown");
                        }
                    } else {
                        Log.i("====Warning===", "socket disconnected");
                    }
                }
            }
        });


    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initClientSocket(String ip, int port) {

        try {

            socket = new Socket();
            socket.connect(new InetSocketAddress(ip,port),5000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 初始化控件
     */
    private void initViews() {

        ip = (EditText) findViewById(R.id.edit_ip);
        port = (EditText) findViewById(R.id.edit_port);
        send_info = (EditText) findViewById(R.id.edit_send);
        conn = (Button) findViewById(R.id.btn_conn);
        send = (Button) findViewById(R.id.btn_send);
        info = (TextView) findViewById(R.id.txt_info);

    }

    class myThread extends Thread{

        @Override
        public void run() {
            String rm_ip = ip.getText().toString();
            int rm_port = Integer.parseInt(port.getText().toString());

            initClientSocket(rm_ip, rm_port);

            try {
                    if (!socket.isClosed()) {
                        if (socket.isConnected()) {
                            if (!socket.isInputShutdown()) {
                                while((context = in.readLine()) != null) {
                                    conn.setClickable(false);
                                    context += "\n";
                                    Log.i("====getInfo===", context);
                                    handler.sendMessage(handler.obtainMessage());
                                }
                            } else {
                                Log.i("====Warning===", "Input ShutDown");
                            }
                        } else {
                            Log.i("====Warning===", "socket disconnected");
                            conn.setClickable(true);
                        }
                    } else {
                        Log.i("====Warning===", "socket closed");
                    }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
