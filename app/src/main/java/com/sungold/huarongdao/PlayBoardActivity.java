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

import java.util.ArrayList;
import java.util.List;

import static com.sungold.huarongdao.HuaBoardView.MODE_HELP;
import static com.sungold.huarongdao.HuaBoardView.MODE_MANUAL;

public class PlayBoardActivity extends AppCompatActivity {
    public BoardView boardView;
    public Menu menu;
    public TextView toolbarTitle;
    public TextView textSteps;
    public TextView textHelp = null;   //求助计算解时对话框的文本框

    List<Board> boardList = new ArrayList<>();
    public int currentStep = 0;
    public List<Board> solutionBoardList = null; //用于播放解决方案的boardList
    public int currentStepOfSolution = 0;

    private Board startBoard = null;
    private Piece movingPiece = null;
    private Boolean boySelected = false;
    public int mode = MODE_MANUAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //必须要先初始化board，boardview需要用到
        initBoard();

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_play_board);
        Toolbar toolbar =  (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText(startBoard.name);
        textSteps = (TextView) findViewById(R.id.text_steps);
        boardView = (BoardView) findViewById(R.id.board_view);
        boardView.setBoard(startBoard);
        boardView.setOnActionListener(new BoardView.ActionListener() {
            @Override
            public void onActionDown(Piece piece) {
                movingPiece = piece;
            }

            @Override
            public void onActionMove(float x, float y) {

            }

            @Override
            public void onActionUp(int direction, float x, float y) {
                actionUp(direction, x, y);
                movingPiece = null;
            }
            @Override
            public void onLongClick(Piece piece){}
        });

        Button buttonBack = (Button) findViewById(R.id.button_back);
        Button buttonForward = (Button) findViewById(R.id.button_forward);
        Button buttonReset = (Button) findViewById(R.id.button_reset);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });
        buttonForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forward();
            }
        });
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });

    }
    public void initBoard(){
        //获取board
        Bundle bundle = this.getIntent().getExtras();
        String dbboardString = (String) bundle.getString("dbBoardString");
        Log.v("block","dbboard:"+dbboardString);
        DBBoard dbBoard = DBBoard.fromJsonString(dbboardString);
        Board board = dbBoard.toBoard();
        if (board == null) {
            Log.v("boardview","获取board失败");
        }
        startBoard = board;
        if(board.bestSolution != null) { board.bestSolution.printSolution(); }
        board.printBoard();
        boardList.add(startBoard);
    }
    private void actionDown(Piece piece){
        ; //不做处理
    }
    private void actionMove(float x,float y){
        ; //不做处理
    }
    private void actionUp( int directon,float x,float y){
        Log.v("playboard",String.format("direction=%d",directon));
        Board newBoard;

        if(mode == MODE_HELP) { return; } //帮助模式下不响应boardview事件
        if(directon < 0){
            //棋子没有移动，box游戏中可用来实现选中boy和目标
            if(startBoard.gameType != GameType.BOX) { return; } //不是box游戏，忽略它

            if(boySelected == true){
                //将boy移动到目标位置
                BoardView.Location location = boardView.getBoardLocation(x,y);
                if(location == null) { return; }
                BoxBoard board = (BoxBoard) currentBoard();
                Log.v("play",String.format("移动boy到%d,%d",location.x,location.y));
                newBoard = board.newBoardAfterMoveBoy(location.x,location.y);
                if(newBoard != null){
                    pushBoard(newBoard);
                    boardView.setBoard(newBoard);
                    Log.v("play","新增board");
                }
                boySelected = false;
                return;
            }
            if(movingPiece == null) { return; }

            if(movingPiece.type == Piece.PIECE_BOY){
                boySelected = true;
                Log.v("play","你选中了boy");
                return;
            }
            return;
        }
        if(movingPiece == null)  { return ; }
        if(startBoard.gameType == GameType.BOX && movingPiece.type != Piece.PIECE_BOY) { return ; } //box游戏仅允许boy移动
        newBoard = currentBoard().newBoardAfterMove(movingPiece, directon);
        if(newBoard != null){
            pushBoard(newBoard);
            boardView.setBoard(newBoard);
        }
    }
    public void finishBoard(){
        //完成棋局时调用
        AlertDialog.Builder builder = new AlertDialog.Builder(PlayBoardActivity.this);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle("你完成了该局");
        String message = String.format("你的步数为:%d",currentStep);
        builder.setMessage(message);
        builder.setPositiveButton("再来一局", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
                Intent intent = new Intent(PlayBoardActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        builder.setNegativeButton("重玩", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                reset();
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
                toolbarTitle.setText(startBoard.name);
                boardView.setMode(MODE_MANUAL);
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
    public void setTextSteps(){
        String bestText = "",text = "";
        if (startBoard.bestSolution != null){
            bestText = String.format("最优步数:%d",startBoard.bestSolution.getSteps());
        }
        if (mode == MODE_MANUAL) {
            text = String.format("当前步数：%d              %s", currentStep,bestText);
        }else{
            text = String.format("当前步数：%d              %s", currentStepOfSolution,bestText);
        }
        textSteps.setText(text);
    }
    public void back(){
        if (mode == MODE_MANUAL) {
            if (currentStep >= 1) {
                currentStep -= 1;
            }
        }else{
            if (currentStepOfSolution >= 1) {
                currentStepOfSolution -= 1;
            }
        }
        boardView.setBoard(currentBoard());
    }
    public void forward(){
        if (mode == MODE_MANUAL) {
            if (currentStep < boardList.size()-1){
                currentStep++;
            }
        }else{
            if (currentStepOfSolution < solutionBoardList.size() - 1) {
                currentStepOfSolution++;
            }
        }
        boardView.setBoard(currentBoard());
        currentBoard().printBoard();
    }
    public void reset(){
        if (mode == MODE_MANUAL){
            boardList.clear();
            boardList.add(startBoard); //第一个board不要用push
            currentStep = 0;
        }else{
            currentStepOfSolution = 0;
        }
        boardView.setBoard(currentBoard());
    }
    public void pushBoard(Board board){
        board.printBoard();
        //当回退到某一步时，如果这个时候手工移动了棋子，就删除currentStep以后的棋盘，兵从当前位置add
        while(currentStep < boardList.size()-1){
            popBoard();
        }
        boardList.add(board);
        if (boardList.size() > 1) { currentStep++; } //加入第一个startboard时不可以currentstep++
        if (currentBoard().isSuccess()){
            finishBoard();
        }
    }
    public void popBoard(){
        if (boardList.size() == 1) {
            return;
        }
        boardList.remove(boardList.size() - 1);
    }
    public Board currentBoard(){
        if (mode == MODE_MANUAL) {
            if (boardList.size() == 0){
                return null;
            }
            return boardList.get(currentStep);
        }else{
            return solutionBoardList.get(currentStepOfSolution);
        }
    }
    private  void help(){
        if (startBoard.bestSolution != null){
            enterHelpMode(startBoard.bestSolution);
            return;
        }

        final FindSolution findSolution = new FindSolution(startBoard);
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

        LayoutInflater factory = LayoutInflater.from(PlayBoardActivity.this);
        final View textEntryView = factory.inflate(R.layout.dialog_help, null);
        textHelp = (TextView) textEntryView.findViewById(R.id.text_help);
        AlertDialog.Builder ad1 = new AlertDialog.Builder(PlayBoardActivity.this);
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
        if(solution == null) {
            Log.v("play", "enter help mode,solution is null");
            return;
        }
        toolbarTitle.setText("进入播放模式");
        solution.printSolution();
        solutionBoardList = solution.buildBoardList(startBoard);
        mode = MODE_HELP;
        boardView.setMode(mode);
        menu.findItem(R.id.toolbar_exit).setVisible(true);
        menu.findItem(R.id.toolbar_help).setVisible(false);
        //boardView.help(solution);
    }
}