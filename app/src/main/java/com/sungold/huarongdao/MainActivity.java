package com.sungold.huarongdao;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    public final static int SELECT_HUARONGDAO = 0;
    public final static int SELECT_BLOCK = 1;
    public final static int SELECT_DIGIT = 2;
    public final static int SELECT_BOX = 3;
    public final static int SELECT_SUKODU = 4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Toolbar toolbar =  (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText("选择游戏类型");

        Button buttonHuarongdao = (Button) findViewById(R.id.button_select_huarongdao);
        Button buttonBlock = (Button) findViewById(R.id.button_select_block);
        Button buttonDigit = (Button) findViewById(R.id.button_select_digit);
        Button buttonBox = (Button) findViewById(R.id.button_select_box);
        Button buttonSukodu = (Button) findViewById(R.id.button_select_sukodu);
        buttonHuarongdao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,BoardLevelActivity.class);
                //startActivityForResult(intent,1);
                Bundle bundle = new Bundle();
                bundle.putSerializable("gameType",GameType.HUARONGDAO);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        buttonBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                //Intent intent = new Intent(getActivity().getApplicationContext(),TorrentDetailActivity.class);
                Intent intent = new Intent(MainActivity.this,BoardLevelActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("gameType",GameType.BLOCK);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        buttonDigit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });
        buttonBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,BoardLevelActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("gameType",GameType.BOX);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        buttonSukodu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,BoardLevelActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("gameType",GameType.SUDOKU);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        getPref();
    }
    public void setClickListener(Button button,int select){
        final int buttonSelect = select;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SelectBoardActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("select",buttonSelect);
                intent.putExtras(bundle);
                //startActivityForResult(intent,1);
                startActivity(intent);
            }
        });
    }
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.toolbar_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()) {
            case R.id.toolbar_add:
                Log.v("MainActivity","click flesh button");
                //TODO
                Intent intent = new Intent(MainActivity.this, HuaAddBoardActivity.class);
                startActivity(intent);
                break;
            case R.id.toolbar_setting:
                setPref();
                break;
            default:
        }
        return true;
    }

    private void getPref(){
        SharedPreferences pref = getSharedPreferences("config",MODE_PRIVATE);
        String server = pref.getString("server","");
        int port = pref.getInt("port",0);
        Log.v("pref",String.format("read:server=%s|port=%d",server,port));
        if (!server.equals("")){
            Log.v("pref",String.format("set server=%s",server));
            MySocket.defaultServer = server;
        }
        if (port != 0){
            Log.v("pref",String.format("set port=%d",port));
            MySocket.defaultPort = port;
        }
    }
    private  void setPref(){
        LayoutInflater factory = LayoutInflater.from(MainActivity.this);
        final View textEntryView = factory.inflate(R.layout.dialog_setting, null);
        final EditText serverText = (EditText) textEntryView.findViewById(R.id.editText_ip);
        final EditText portText = (EditText)textEntryView.findViewById(R.id.editText_port);
        AlertDialog.Builder ad1 = new AlertDialog.Builder(MainActivity.this);
        ad1.setIcon(android.R.drawable.ic_dialog_info);
        ad1.setView(textEntryView);
        serverText.setText(MySocket.defaultServer);
        portText.setText(String.valueOf(MySocket.defaultPort));
        ad1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                String server = serverText.getText().toString();
                String portString = portText.getText().toString();
                int port = Integer.valueOf(portString);
                SharedPreferences.Editor editor = getSharedPreferences("config",MODE_PRIVATE).edit();
                editor.putString("server",server);
                editor.putInt("port",port);
                Log.v("pref",String.format("set sever = %s",server));
                Log.v("pref",String.format("set port = %d",port));
                MySocket.defaultServer = server;
                MySocket.defaultPort = port;
                if (editor.commit()) {
                    Log.v("pref","write to config");
                }
            }
        });
        ad1.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
            }
        });
        ad1.show();// 显示对话框
    }

}
enum GameType {
    HUARONGDAO(0),BLOCK(1),BOX(2), SUDOKU(3);
    private int value;

    GameType(int i) {
        // TODO Auto-generated constructor stub
        this.value = i;
    }
    public static GameType toEnum(int value) {
        switch (value) {
            case 0:
                return HUARONGDAO;
            case 1:
                return BLOCK;
            case 2:
                return BOX;
            case 3:
                return SUDOKU;
            default:
                return null;
        }
    }
    public  int  toInt(){
        return this.value;
    }

};
