package com.sungold.huarongdao;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import static com.sungold.huarongdao.HuaBoardView.MODE_HELP;
import static com.sungold.huarongdao.HuaBoardView.MODE_MANUAL;

public class PlaySudokuBoardActivity extends AppCompatActivity {
    public SudokuBoardView boardView;
    public Menu menu;
    public TextView toolbarTitle;
    public TextView textSteps;
    public TextView textHelp = null;   //求助计算解时对话框的文本框
    public Chronometer timer = null;
    private long recordTime = 0;        //用来记录暂停的时间点，用于恢复计时
    List<Board> boardList = new ArrayList<>();
    public List<Board> solutionBoardList = null; //用于播放解决方案的boardList
    public int currentStep = 0;
    public int currentStepOfSolution = 0;

    private SudokuBoard startBoard = null;
    public int mode = MODE_MANUAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //必须要先初始化board，boardview需要用到
        initBoard();

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_play_sudoku_board);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText(startBoard.name);
        textSteps = (TextView) findViewById(R.id.text_steps);
        timer = findViewById(R.id.layout_timer);
        setTextSteps();
        timerStart();
        boardView = (SudokuBoardView) findViewById(R.id.board_view);
        boardView.setBoard(startBoard);
        boardView.setOnActionListener(new SudokuBoardView.ActionListener() {
            @Override
            public void onActionUp(SudokuPiece sudokuPiece) {
                Log.v("play_sudoku", String.format("click piece:" + sudokuPiece.toDBString()));
                handleClick(sudokuPiece);
            }
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

    /*@Override
    protected void onStart() {
        super.onStart();
        timerStart();
    }7*/
    @Override
    protected void onResume(){
        super.onResume();
        timerResume();
    }
    @Override
    protected void onPause(){
        super.onPause();
        timerPause();
    }


    public void initBoard(){
        //获取board
        Bundle bundle = this.getIntent().getExtras();
        String dbboardString = (String) bundle.getString("dbBoardString");
        Log.v("play","dbboard:"+dbboardString);
        DBBoard dbBoard = DBBoard.fromJsonString(dbboardString);
        Log.v("play",dbBoard.getBoardString());
        Board board = dbBoard.toBoard();
        if (board == null) {
            Log.v("boardview","获取board失败");
        }
        startBoard = (SudokuBoard)board;
        if(board.bestSolution != null) { board.bestSolution.printSolution(); }
        startBoard.printBoard();
        boardList.add(startBoard);
        //startBoard = new SudokuBoard(SudokuType.NINE);
    }
    private void handleClick(SudokuPiece sudokuPiece){
        //有改变才回调给playActivity处理
        //1，把该board加入list
        pushBoard(boardView.board.copyBoard());
        //2，检查，提醒
        //TODO
    }

    public void finishBoard(){
        //完成棋局时调用
        AlertDialog.Builder builder = new AlertDialog.Builder(PlaySudokuBoardActivity.this);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle("你完成了该局");
        String message = String.format("你用时:%d秒,步数:%d",getTimer(),currentStep);
        builder.setMessage(message);
        builder.setPositiveButton("再来一局", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
                Intent intent = new Intent(PlaySudokuBoardActivity.this,MainActivity.class);
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
        getMenuInflater().inflate(R.menu.toolbar_sudoku_boardview,menu);
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
               fillMiniNumbers();
                /* 在对话框的确定按钮选择后设置
                toolbarTitle.setText("进入播放模式");
                menu.findItem(R.id.toolbar_exit).setVisible(true);
                menu.findItem(R.id.toolbar_help).setVisible(false);*/
                //boardView.setMode(BoardView.MODE_HELP);
                break;
            case R.id.toolbar_save:
                save();
                break;
            case R.id.toolbar_check:
                check();
            default:
        }
        return true;
    }
    public void setTextSteps(){
        Log.v("sudoku","set steps");
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
        boardView.setBoard((SudokuBoard)currentBoard());
        Log.v("sudoku",String.format("currentstep:%d",currentStep));
        setTextSteps();
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
        boardView.setBoard((SudokuBoard)currentBoard());
        //currentBoard().printBoard();
        Log.v("sudoku",String.format("currentstep:%d",currentStep));
        setTextSteps();
    }
    public void reset(){
        if (mode == MODE_MANUAL){
            boardList.clear();
            boardList.add(startBoard); //第一个board不要用push
            currentStep = 0;
        }else{
            currentStepOfSolution = 0;
        }
        boardView.setBoard((SudokuBoard)currentBoard());
        Log.v("sudoku",String.format("currentstep:%d",currentStep));
        setTextSteps();
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
        setTextSteps();
        Log.v("sudoku",String.format("currentstep:%d",currentStep));
    }
    public void popBoard(){
        if (boardList.size() == 1) {
            return;
        }
        boardList.remove(boardList.size() - 1);
        setTextSteps();
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
        //Log.v("sudoku",String.format("currentstep:%d",currentStep));
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

        LayoutInflater factory = LayoutInflater.from(PlaySudokuBoardActivity.this);
        final View textEntryView = factory.inflate(R.layout.dialog_help, null);
        textHelp = (TextView) textEntryView.findViewById(R.id.text_help);
        AlertDialog.Builder ad1 = new AlertDialog.Builder(PlaySudokuBoardActivity.this);
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
    private void check(){
        //检查board是否有解且唯一解
        String reply = ((SudokuBoard)currentBoard()).checkSolution();
        if(reply.equals("ok")){
            Toast.makeText(PlaySudokuBoardActivity.this,"OK",Toast.LENGTH_LONG).show();
        }else{
            //TODO
            Toast.makeText(PlaySudokuBoardActivity.this,reply,Toast.LENGTH_LONG).show();
        }
    }
    private void fillMiniNumbers(){
        //提示：填入所有备选数字
        boardView.board.fillMiniNumbers();
        boardView.invalidate();
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
    private void save(){
        (new SaveTask()).execute();
    }

    private void timerStart(){
        //启动计时
        Log.v("sudoku","启动计时");

        //timer.setBase(SystemClock.elapsedRealtime());
        recordTime = 0;
        timer.start();
    }
    private void timerPause(){
        Log.v("sudoku","暂停计时");
        recordTime = SystemClock.elapsedRealtime();
        timer.stop();
    }
    private void timerResume(){
        Log.v("sudoku","恢复计时");
        if(recordTime > 0){
            timer.setBase((timer.getBase()+(SystemClock.elapsedRealtime()-recordTime)));
        }
        timer.start();
    }
    private int getTimer(){
        //读取计时器，转换为秒
        String string = timer.getText().toString();
        if(string.length()==7){
            String[] split = string.split(":");
            int hour = Integer.parseInt(split[0]);
            int min = Integer.parseInt(split[1]);
            int second = Integer.parseInt(split[2]);
            return hour*3600+min*60+second;
        }else if(string.length() == 5){
            String[] split = string.split(":");
            int min = Integer.parseInt(split[0]);
            int second = Integer.parseInt(split[1]);
            return min*60+second;
        }
        return -1;
    }

    class SaveTask extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] objects) {
            startBoard.toDBBoard().save();
            return null;
        }
    }
}