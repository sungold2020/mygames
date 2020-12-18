package com.sungold.huarongdao;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AddBoardActivity extends AppCompatActivity {

    BoardView boardView;
    AddPieceView addPieceView;
    TextView toolbarTitle;

    GameType gameType;
    int maxX, maxY;
    String name;
    int kingType, kingLength; //blockboard需要

    Piece movingPiece = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setContentView(R.layout.activity_add_board);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_add);
        setSupportActionBar(toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText("新增牌局");
        addPieceView = (AddPieceView) findViewById(R.id.add_piece_view);
        boardView = (BoardView) findViewById(R.id.board_view);
        addPieceView.setGameType(gameType);
        //nameEditText = (EditText) findViewById(R.id.edit_name);
        addPieceView.setOnActionListener(new AddPieceView.ActionListener() {
            @Override
            public void onActionDown(Piece piece) {
                movingPiece = piece;
                Log.v("addboard","press piece:"+piece.name);
            }

            @Override
            public void onActionMove(float x, float y) {
                Log.v("addpiece",String.format("x=%f,y=%f",x,y));
                if(movingPiece.x == -1){
                    //移动的是布局的棋子
                }else{
                    //移动的是棋盘的棋子
                }
            }

            @Override
            public void onActionUp(int direction, float x, float y) {
                if(y >= 0){ return; }  //未进入boardview
                y = convertY(y); //转换未board中的android坐标
                if(movingPiece == null){
                    Log.v("addboard","movingPiece is null");
                    return;
                }
                boardView.movingPieceTo(movingPiece,(int)x,(int)y);
                boardView.invalidate();
                Log.v("addpiece",String.format("x=%f,y=%f",x,y));
            }
        });
        boardView.setMode(BoardView.MODE_ADDBOARD);
        boardView.setOnActionListener(new BoardView.ActionListener() {
            @Override
            public void onActionDown(Piece piece) {
                movingPiece = piece;
                //Log.v("boarview","press piece:"+piece.name);
            }

            @Override
            public void onActionMove(float x, float y) {
                Log.v("boardview",String.format("x=%f,y=%f",x,y));
            }

            @Override
            public void onActionUp(int direction, float x, float y) {
                Log.v("boardview",String.format("x=%f,y=%f",x,y));

            }
            @Override
            public void onLongClick(Piece piece){
                //长按事件尽在木块游戏中，用来设置king
                if(gameType == GameType.BLOCK){
                    //debug
                    boardView.board.printBoard();
                    BrickBoard brickBoard = (BrickBoard) boardView.board;
                    if((brickBoard.isKing(piece))){
                        brickBoard.unsetKing(piece);
                    }else{
                        brickBoard.setKing(piece);
                    }
                    boardView.board.printBoard();
                    boardView.invalidate();
                }
            }
        });
    }

    private void init() {
        Bundle bundle = this.getIntent().getExtras();
        gameType = (GameType) bundle.getSerializable("gameType");
        switch (gameType) {
            case HUARONGDAO:
                //TODO
                break;
            case BLOCK:
                initBrickBoard();
                break;
            case BOX:
                initBoxBoard();
                break;
        }
    }
    private void initBrickBoard(){
        LayoutInflater factory = LayoutInflater.from(AddBoardActivity.this);
        final View textEntryView = factory.inflate(R.layout.dialog_add_block, null);
        final EditText nameText = (EditText) textEntryView.findViewById(R.id.editText_name);
        final EditText maxXText = (EditText)textEntryView.findViewById(R.id.editText_maxX);
        final EditText maxYText = (EditText)textEntryView.findViewById(R.id.editText_maxY);
        final EditText kingTypeText = (EditText)textEntryView.findViewById(R.id.editText_kingType);
        final EditText kingLengthText  = (EditText)textEntryView.findViewById(R.id.editText_kingLength);

        AlertDialog.Builder builder = new AlertDialog.Builder(AddBoardActivity.this);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setView(textEntryView);
        builder.setTitle("新增棋盘");
        nameText.setText("test");
        maxXText.setText("6");
        maxYText.setText("6");
        kingTypeText.setText("0");
        kingLengthText.setText("2");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                name = nameText.getText().toString();
                maxX = Integer.valueOf(maxXText.getText().toString());
                maxY = Integer.valueOf(maxYText.getText().toString());
                int kingType = Integer.valueOf(kingTypeText.getText().toString());
                int kingLength = Integer.valueOf(kingLengthText.getText().toString());
                boardView.setBoard(new BrickBoard(name, maxX, maxY,maxX)); //TODO 可选出口
                Log.v("addboad",String.format("%s:%d,%d",name,maxX,maxY));
                toolbarTitle.setText(name);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void initBoxBoard() {
        LayoutInflater factory = LayoutInflater.from(AddBoardActivity.this);
        final View textEntryView = factory.inflate(R.layout.dialog_add_box, null);
        final EditText nameText = (EditText) textEntryView.findViewById(R.id.editText_name);
        final EditText maxXText = (EditText) textEntryView.findViewById(R.id.editText_maxX);
        final EditText maxYText = (EditText) textEntryView.findViewById(R.id.editText_maxY);

        AlertDialog.Builder builder = new AlertDialog.Builder(AddBoardActivity.this);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setView(textEntryView);
        builder.setTitle("新增棋盘");
        nameText.setText("test");
        maxXText.setText("6");
        maxYText.setText("6");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                name = nameText.getText().toString();
                maxX = Integer.valueOf(maxXText.getText().toString());
                maxY = Integer.valueOf(maxYText.getText().toString());
                boardView.setBoard(new BoxBoard(name, maxX, maxY));
                Log.v("addboad",String.format("%s:%d,%d",name,maxX,maxY));
                toolbarTitle.setText(name);
                dialog.dismiss();

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private float convertY(float y){
        //将addpieceview中的y转换为boardview中的y，要求:addpieceview布局在boarview下方。

        int height = boardView.getHeight();
        return height + y;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.toolbar_save:
                saveBoard();
                break;
            default:
        }
        return true;
    }

    public void saveBoard() {
        Log.v("MainActivity", "click saveboard button");
        //TODO
        String reply = boardView.board.checkBoard();
        if (reply.equals("OK")) {
            Log.v("addboardview", "check OK");
            //Intent intent = new Intent();
            //setResult(RESULT_SUCCESS,intent);
            //finish();
            Intent intent = new Intent(AddBoardActivity.this, PlayBoardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Bundle bundle = new Bundle();
            //bundle.putString("name", boardView.board.name);
            bundle.putString("dbBoardString", boardView.board.toDBBoard().toJsonString());
            //bundle.putString("solution",.getSolution());
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        } else {
            Log.v("addboard",reply);
            Toast.makeText(this, reply, Toast.LENGTH_SHORT);
        }
    }
}