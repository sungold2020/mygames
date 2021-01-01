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

public class BoardLevelActivity extends AppCompatActivity {
    public final static int SELECT_VERY_EASY = 0;
    public final static int SELECT_EASY = 1;
    public final static int SELECT_MEDIUM = 2;
    public final static int SELECT_HARD = 3;
    public final static int SELECT_VERY_HARD = 4;

    public GameType gameType; //activity共用多个游戏类型选择用

    //public BoardView boardView;
    //public TextView textHelp = null;
    //public String stringHep = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getIntent().getExtras();
        gameType = (GameType) bundle.getSerializable("gameType");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_board_level);
        Toolbar toolbar =  (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText("华容道");

        //boardView = (BoardView) findViewById(R.id.board_view);
        Button buttonVeryEasy = (Button) findViewById(R.id.button_select_veryeasy);
        Button buttonEasy = (Button) findViewById(R.id.button_select_easy);
        Button buttonMedium = (Button) findViewById(R.id.button_select_medium);
        Button buttonHard = (Button) findViewById(R.id.button_select_hard);
        Button buttonVeryHard = (Button) findViewById(R.id.button_select_veryhard);
        Button buttonAdd = (Button) findViewById(R.id.button_add);
        Button buttonSaved = (Button) findViewById(R.id.button_saved);

        setClickListener(buttonVeryEasy,SELECT_VERY_EASY);
        setClickListener(buttonEasy,SELECT_EASY);
        setClickListener(buttonMedium,SELECT_MEDIUM);
        setClickListener(buttonHard,SELECT_HARD);
        setClickListener(buttonVeryHard,SELECT_VERY_HARD);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                //Intent intent = new Intent(getActivity().getApplicationContext(),TorrentDetailActivity.class);
                Intent intent;
                Bundle bundle;
                switch(gameType){
                    case HUARONGDAO:
                        intent = new Intent(BoardLevelActivity.this, HuaAddBoardActivity.class);
                        break;
                    case BLOCK:
                    case BOX:
                        bundle = new Bundle();
                        bundle.putSerializable("gameType",gameType);
                        intent = new Intent(BoardLevelActivity.this,AddBoardActivity.class);
                        intent.putExtras(bundle);
                        break;
                    case SUDOKU:
                        bundle = new Bundle();
                        bundle.putSerializable("gameType",gameType);
                        intent = new Intent(BoardLevelActivity.this,AddSukoduBoardActivity.class);
                        intent.putExtras(bundle);
                        break;
                    default:
                        return;
                }
                //Log.v("boardlevel","start activity");
                startActivity(intent);
            }
        });
        buttonSaved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                Bundle bundle;
                switch(gameType){
                    case HUARONGDAO:
                        //TODO
                        return;
                    case BLOCK:
                    case BOX:
                        //TODO
                        return;
                    case SUDOKU:
                        bundle = new Bundle();
                        bundle.putSerializable("gameType",gameType);
                        intent = new Intent(BoardLevelActivity.this, SelectGoingBoardActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    default:
                        return;
                }
            }
        });
        getPref();
    }
    public void setClickListener(Button button,int select){
        final int buttonSelect = select;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BoardLevelActivity.this,SelectBoardActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("gameType",gameType);
                bundle.putInt("select",buttonSelect);
                intent.putExtras(bundle);
                //startActivityForResult(intent,1);
                Log.v("board","put in gameType");
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
                Intent intent = new Intent(BoardLevelActivity.this, HuaAddBoardActivity.class);
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
        LayoutInflater factory = LayoutInflater.from(BoardLevelActivity.this);
        final View textEntryView = factory.inflate(R.layout.dialog_setting, null);
        final EditText serverText = (EditText) textEntryView.findViewById(R.id.editText_ip);
        final EditText portText = (EditText)textEntryView.findViewById(R.id.editText_port);
        AlertDialog.Builder ad1 = new AlertDialog.Builder(BoardLevelActivity.this);
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