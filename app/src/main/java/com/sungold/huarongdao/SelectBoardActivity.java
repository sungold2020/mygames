package com.sungold.huarongdao;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.List;

public class SelectBoardActivity extends AppCompatActivity {
    private GameType gameType;
    private int select = -1;
    private GridView gridView = null;
    private List<DBBoard> dbBoardList = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_board);

        Bundle bundle = this.getIntent().getExtras();
        select = (int) bundle.getInt("select");
        gameType = (GameType) bundle.getSerializable("gameType");
        Log.v("board",String.format("get gametype=%d",gameType.toInt()));
        gridView = findViewById(R.id.gridview);
        (new QueryBoardTask(select,gameType.toInt())).execute();
    }
    private void initView(){
        if (dbBoardList != null){
            DBBoardAdapter adapter = new DBBoardAdapter(this,dbBoardList);
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.v("slect","click item");
                    DBBoard dbBoard = dbBoardList.get(position);
                    startBoard(dbBoard);
                }
            });
        }
    }
    private void startBoard(DBBoard dbBoard){
        //根据dbBoard启动棋盘开始玩
        Intent intent;
        Bundle bundle = new Bundle();
        bundle.putString("name",dbBoard.getName());
        bundle.putString("board",dbBoard.getBoardString());
        bundle.putString("solution",dbBoard.getSolutionString());
        switch (dbBoard.getGameType()){
            case HUARONGDAO:
                intent = new Intent(SelectBoardActivity.this,BoardViewActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtras(bundle);
                startActivity(intent);
                return;
            case BLOCK:
                intent = new Intent(SelectBoardActivity.this,BlockBoardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtras(bundle);
                startActivity(intent);
                return;
            case BOX:
                bundle.putString("dbBoardString", dbBoard.toJsonString());
                intent = new Intent(SelectBoardActivity.this,PlayBoardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtras(bundle);
                startActivity(intent);
                return;
            case SUDOKU:
                bundle.putString("dbBoardString", dbBoard.toJsonString());
                intent = new Intent(SelectBoardActivity.this,PlaySudokuBoardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtras(bundle);
                startActivity(intent);
                return;
        }
    }

    public void onActivityResult(int requestCode,int resultCode, Intent data ) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case HuaAddBoardActivity.RESULT_SUCCESS:
                Log.v("main","refreshboard");
                //boardView.refreshBoard();
                Intent intent = new Intent(SelectBoardActivity.this, HuaAddBoardActivity.class);
                Bundle bundle = new Bundle();
                //bundle.putString("name",name);
                intent.putExtras(bundle);
                break;
            case HuaAddBoardActivity.RESULT_FAILED:
                Log.v("Main","save failed");
                break;
            default:
                Log.v("TorrentsFragment", String.format("unknown resultcode:%d", resultCode));
        }
    }

    private class QueryBoardTask extends AsyncTask<String,Integer,String> {
        private int select;
        private int type;
        QueryBoardTask(int select,int type) {
            this.select = select;
            this.type = type;
        }
        @Override
        protected String doInBackground(String... paramas) {
            if (dbBoardList != null ) {
                dbBoardList.clear();
            }
            dbBoardList = DBBoard.query_boards(select,type);
            if (dbBoardList == null) {
                Log.v("select","failed to get dbboardlist");
                return "failed";
            } else {
                Log.v("select",String.format("get %d boards",dbBoardList.size()));
                return "success";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            initView();
        }
    }
}