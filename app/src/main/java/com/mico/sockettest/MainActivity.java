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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

public class MainActivity extends AppCompatActivity {

    final static String IP = "120.27.110.225";
    final static int PORT = 30000;

    private EditText send_info;
    private Button send;
    private TextView info;

    private Socket socket;

    private String context;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 0x11){
                Bundle bundle = msg.getData();
                info.append("Server: " + bundle.getString("msg")+"\n");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context = send_info.getText().toString();
                info.append("client: " + context + "\n");
                new myThread(context).start();
            }
        });
    }

    /**
     * 初始化控件
     */
    private void initViews() {

        send_info = (EditText) findViewById(R.id.edit_send);
        send = (Button) findViewById(R.id.btn_send);
        info = (TextView) findViewById(R.id.txt_info);

    }

    class myThread extends Thread {

        private String context;
        private String buffer;

        public myThread(String context){
            this.context = context;
        }

        @Override
        public void run() {

            Message msg = new Message();
            msg.what = 0x11;
            Bundle bundle = new Bundle();
            bundle.clear();
            try{
                socket = new Socket();
                socket.connect(new InetSocketAddress(IP, PORT),5000);

                OutputStream out = socket.getOutputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line = null;
                buffer = "";
                while((line = in.readLine()) != null){
                    buffer = line + buffer;
                }

                out.write(context.getBytes("utf-8"));
                out.flush();
                bundle.putString("msg", buffer);
                msg.setData(bundle);

                handler.sendMessage(msg);

                in.close();
                out.close();
                socket.close();

            }catch (SocketException e){
                bundle.putString("msg","服务器连接失败");
                msg.setData(bundle);
                handler.sendMessage(msg);
            }catch (IOException eio){
                eio.printStackTrace();
            }

        }
    }

}
