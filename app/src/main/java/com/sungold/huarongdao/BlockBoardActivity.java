package com.sungold.huarongdao;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class BlockBoardActivity extends AppCompatActivity {
    public BlockBoardView boardView;
    public Menu menu;
    public TextView toolbarTitle;

    public TextView textHelp = null;
    //public String stringHep = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //必须要先初始化board，boardview需要用到
        initBoard();

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_block_board);
        Toolbar toolbar =  (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText(boardView.startBlockBoard.name);

        boardView = (BlockBoardView) findViewById(R.id.board_view);
        Button buttonBack = (Button) findViewById(R.id.button_back);
        Button buttonForward = (Button) findViewById(R.id.button_forward);
        Button buttonReset = (Button) findViewById(R.id.button_reset);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boardView.back();
            }
        });
        buttonForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boardView.forward();
            }
        });
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boardView.reset();
            }
        });
        boardView.setOnFinishListener(new BlockBoardView.FinishListener() {
            @Override
            public void finish() {
                finishBoard();
            }
        });
    }
    public void initBoard(){
        //获取board
        Bundle bundle = this.getIntent().getExtras();
        String name = (String) bundle.getString("name");
        String boardString = (String) bundle.getString("board");
        String solutionString = (String) bundle.getString("solution");
        BlockBoard board = BlockBoard.fromDBString(boardString);
        if (board == null) {
            Log.v("boardview","获取board失败");
        }
        if (solutionString != null && !solutionString.equals("")){
            Solution solution = Solution.parseFromJsonString(solutionString);
            if (solution != null){
                board.bestSolution = solution;
            }
        }
        board.name = name;

        /*BlockPiece kingBlock = new BlockPiece("王",BlockPiece.PIECE_HORIZON,2,0,3);
        BlockPiece[] verticalBlocks = new BlockPiece[5];
        BlockPiece[] horizonBlocks = new BlockPiece[2];
        verticalBlocks[0] = new BlockPiece("竖一",BlockPiece.PIECE_VERTICAL,2,1,1);
        verticalBlocks[1] = new BlockPiece("竖二",BlockPiece.PIECE_VERTICAL,3,2,3);
        verticalBlocks[2] = new BlockPiece("竖三",BlockPiece.PIECE_VERTICAL,3,3,3);
        verticalBlocks[3] = new BlockPiece("竖四",BlockPiece.PIECE_VERTICAL,2,4,1);
        verticalBlocks[4] = new BlockPiece("竖五",BlockPiece.PIECE_VERTICAL,2,4,3);
        horizonBlocks[0] = new BlockPiece("横一",BlockPiece.PIECE_HORIZON,2,2,1);
        horizonBlocks[1] = new BlockPiece("横二",BlockPiece.PIECE_HORIZON,2,4,5);
        BlockBoardView.startBlockBoard = new BlockBoard("tt",6,6,6,kingBlock,verticalBlocks,horizonBlocks);*/
        BlockBoardView.startBlockBoard = board;
    }
    public void finishBoard(){
        //完成棋局时调用
        AlertDialog.Builder builder = new AlertDialog.Builder(BlockBoardActivity.this);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle("你完成了该局");
        String message = String.format("你的步数为:%d",boardView.currentStep);
        builder.setMessage(message);
        builder.setPositiveButton("再来一局", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
                Intent intent = new Intent(BlockBoardActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        builder.setNegativeButton("重玩", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                boardView.reset();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public boolean onCreateOptionsMenu(Menu menu){
        this.menu = menu;
        getMenuInflater().inflate(R.menu.toolbar_boardview,menu);
        menu.findItem(R.id.toolbar_exit).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()) {
            case R.id.toolbar_exit:
                Log.v("MainActivity","click flesh button");
                //boardview进入manual模式,隐藏退出按钮，显示帮助按钮
                menu.findItem(R.id.toolbar_exit).setVisible(false);
                menu.findItem(R.id.toolbar_help).setVisible(true);
                toolbarTitle.setText(boardView.startBlockBoard.name);
                boardView.setMode(HuaBoardView.MODE_MANUAL);
                break;
            case R.id.toolbar_help:
                //开始找最优解，弹出对话框
                help();
                /* 在对话框的确定按钮选择后设置
                toolbarTitle.setText("进入播放模式");
                menu.findItem(R.id.toolbar_exit).setVisible(true);
                menu.findItem(R.id.toolbar_help).setVisible(false);*/
                //boardView.setMode(BoardView.MODE_HELP);
                break;
            default:
        }
        return true;
    }
    public void back(){
        boardView.back();
    }
    private  void help(){
        if (boardView.startBlockBoard.bestSolution != null){
            enterHelpMode(null);
            return;
        }

        final BlockFindSolution findSolution = new BlockFindSolution(boardView.startBlockBoard);
        Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                Log.v("solutiion","收到消息");
                if(msg.what == 0x11){
                    //Toast.makeText(getContext(),String.format("找到解：%d 步",findSolution.solution.getSteps()),Toast.LENGTH_SHORT);
                    textHelp.setText(String.format("当前最优解：%d 步",findSolution.solution.getSteps()));
                }else if(msg.what == 0x12){
                    //Toast.makeText(MainActivity.this,"寻找最优解完成",Toast.LENGTH_SHORT);
                    Log.v("main","完成寻找最优解");
                    if (findSolution.solution == null){
                        textHelp.setText(String.format("未能找到最优解"));
                    }else {
                        textHelp.setText(String.format("找到最优解：%d 步", findSolution.solution.getSteps()));
                    }
                }else if(msg.what == 0x13){
                    textHelp.setText(String.format("从服务器找到最优解：%d 步",findSolution.solution.getSteps()));
                }
            }
        };
        findSolution.handler = handler;
        findSolution.start();

        LayoutInflater factory = LayoutInflater.from(BlockBoardActivity.this);
        final View textEntryView = factory.inflate(R.layout.dialog_help, null);
        textHelp = (TextView) textEntryView.findViewById(R.id.text_help);
        AlertDialog.Builder ad1 = new AlertDialog.Builder(BlockBoardActivity.this);
        ad1.setIcon(android.R.drawable.ic_dialog_info);
        ad1.setView(textEntryView);
        ad1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                enterHelpMode(findSolution.solution);
            }
        });
        ad1.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
            }
        });
        Dialog dialog = ad1.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
    public void enterHelpMode(Solution solution){
        toolbarTitle.setText("进入播放模式");
        menu.findItem(R.id.toolbar_exit).setVisible(true);
        menu.findItem(R.id.toolbar_help).setVisible(false);
        boardView.help(solution);
    }
}