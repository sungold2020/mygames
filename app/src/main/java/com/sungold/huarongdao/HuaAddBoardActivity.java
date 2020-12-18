package com.sungold.huarongdao;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

public class HuaAddBoardActivity extends AppCompatActivity {
    public final static int RESULT_SUCCESS = 2;
    public final static int RESULT_FAILED = 0;
    AddBoardView addBoardView;
    EditText nameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hua_add_board);
        Toolbar toolbar =  (Toolbar) findViewById(R.id.toolbar_add);
        setSupportActionBar(toolbar);
        TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText("新增牌局");
        addBoardView = (AddBoardView) findViewById(R.id.add_board);
        nameEditText = (EditText) findViewById(R.id.edit_name);
    }
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.toolbar_add,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()) {
            case R.id.toolbar_save:
                Log.v("MainActivity","click saveboard button");
                //TODO
                if (addBoardView.saveBoard(nameEditText.getText().toString())){
                    Log.v("addboardview","check OK");
                    //Intent intent = new Intent();
                    //setResult(RESULT_SUCCESS,intent);
                    //finish();
                    Intent intent = new Intent(HuaAddBoardActivity.this,BoardViewActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    Bundle bundle = new Bundle();
                    bundle.putString("name",addBoardView.board.name);
                    bundle.putString("board",addBoardView.board.piecesToString());
                    //bundle.putString("solution",.getSolution());
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                }
                //Log.v("addboard","failed to save");
                break;
            case R.id.toolbar_back:
                //setPref();
                break;
            default:
        }
        return true;
    }
}