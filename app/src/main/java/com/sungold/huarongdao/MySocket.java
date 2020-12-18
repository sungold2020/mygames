package com.sungold.huarongdao;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class MySocket {
    public static Socket socket = null;
    public static ServerSocket serverSocket = null;
    public static String defaultServer="192.168.31.45";
    public static int defaultPort=12346;

    private String host;
    private int port;

    //不指定的情况下，取静态变量给定的服务器和端口号
    public MySocket() {
        host = defaultServer;
        port = defaultPort;
   }
    public MySocket(String host, int port){
        this.host = host;
        this.port = port;
    }
    public boolean accept() {
        try {
            if (serverSocket == null)  {
                serverSocket = new ServerSocket(port);
            }
            socket = serverSocket.accept();
            return true;
        }catch( Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public Boolean connect(){
       try{
			Log.v("MainActivity","begin connect to server"+host);
            socket = new Socket(host,port);
			Log.v("MainActivity","connected to server");
            return true;
        }catch (Exception err){
            System.out.println(err);
			Log.v("MainActivity","error: connect to server");
            return false;
        }
    }
    public Boolean send(String message){
        try	{
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(message.getBytes("UTF-8"));
            socket.shutdownOutput();
            Log.v("socket","send:"+message);
            return true;
        }catch (Exception err){
			Log.v("MainActivity","error: send to server");
            //System.out.println(err);
            return false;
        }
    }
    public StringBuffer recieve(){
        StringBuffer sb = new StringBuffer();
        try	{
            InputStream inputStream = socket.getInputStream();
            byte[] allBytes = new  byte[102400];
            byte[] readbytes = new byte[102400];
            int allLength=0,length=0;
            while(( length = inputStream.read(readbytes)) != -1) {
                //sb.append(new String(bytes,0,len,"UTF-8"));
                System.arraycopy(readbytes,0,allBytes,allLength,length);
                allLength += length;
            }
            sb.append(new String(allBytes,0,allLength,"UTF-8"));
        }catch (Exception err){
            //System.out.println(err);
			Log.v("MainActivity","error: recieve from server");
            return sb;
        }
        return sb;
    }
    public Boolean  close() {
        try {
            if (socket != null ) { socket.close(); }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
