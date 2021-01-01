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
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.List;

public class AddSukoduBoardActivity extends AppCompatActivity {

    SudokuBoardView boardView;
    TextView toolbarTitle;
    SudokuPiece lastClickPiece = null;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sudoku_board);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_add);
        setSupportActionBar(toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText("新增牌局");
        boardView = findViewById(R.id.board_view);
        //nameEditText = (EditText) findViewById(R.id.edit_name);
        initBoard();
        boardView.setMode(BoardView.MODE_ADDBOARD);
        boardView.setOnActionListener(new SudokuBoardView.ActionListener() {

            @Override
            public void onActionUp(SudokuPiece sudokuPiece) {
                Log.v("play_sudoku",String.format("click piece:"+sudokuPiece.toDBString()));
                /*switch (sudokuPiece.type){
                    case Piece.PIECE_SUDOKU_BOARD:
                        Log.v("play","click Board");
                        //打印所在宫格
                        boardView.brightSquare(sudokuPiece);
                        boardView.brightRow(sudokuPiece.y);
                        boardView.brightColumn(sudokuPiece.x);
                        break;
                    case Piece.PIECE_SUDOKU_NUMBER:
                        Log.v("play","click Big Number");
                        break;
                    case Piece.PIECE_SUDOKU_MINI_NUMBER:
                        Log.v("play","click small number");
                        break;
                }*/
                handleClick(sudokuPiece);
            }
        });
    }

    private void initBoard(){
        LayoutInflater factory = LayoutInflater.from(AddSukoduBoardActivity.this);
        final View textEntryView = factory.inflate(R.layout.dialog_add_sudoku, null);
        final EditText nameText = (EditText) textEntryView.findViewById(R.id.editText_name);
        final RadioGroup typeRadioGroup = (RadioGroup) textEntryView.findViewById(R.id.radio_group_type);

        AlertDialog.Builder builder = new AlertDialog.Builder(AddSukoduBoardActivity.this);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setView(textEntryView);
        builder.setTitle("新增棋盘");
        typeRadioGroup.check(R.id.radio_nine);
        nameText.setText("test");
        //TODO
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                name = nameText.getText().toString();
                SudokuType sudokuType = SudokuType.NINE;
                switch (typeRadioGroup.getCheckedRadioButtonId()){
                    case R.id.radio_four:
                        sudokuType = SudokuType.FOUR;
                        break;
                    case R.id.radio_six:
                        sudokuType = SudokuType.SIX;
                        break;
                    case R.id.radio_nine:
                        sudokuType = SudokuType.NINE;
                        break;
                    case R.id.radio_x:
                        sudokuType = SudokuType.X_STYLE;
                        break;
                    case R.id.radio_percent:
                        sudokuType = SudokuType.PERCENT;
                        break;
                    case R.id.radio_super:
                        sudokuType = SudokuType.SUPER;
                        break;
                    default:
                        Log.v("addSudoku","unknown type");
                        dialog.dismiss();
                }
                boardView.initBoard(new SudokuBoard(name,sudokuType));
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

    private void handleClick(SudokuPiece sudokuPiece){
        //进行检查处理
        //TODO
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
            Intent intent = new Intent(AddSukoduBoardActivity.this, PlaySudokuBoardActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Bundle bundle = new Bundle();
            //bundle.putString("name", boardView.board.name);
            bundle.putString("dbBoardString", boardView.board.toDBBoard().toJsonString());
            Log.v("addboard",boardView.board.toDBBoard().toJsonString());
            //bundle.putString("solution",.getSolution());
            intent.putExtras(bundle);
            startActivity(intent);
            //finish();
        }else{
            Log.v("addboard",reply);
            Toast.makeText(AddSukoduBoardActivity.this, reply, Toast.LENGTH_SHORT);
        }
    }
}