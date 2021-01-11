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
import android.widget.EditText;
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
    public TextView textHint;
    public TextView textHelp = null;   //求助计算解时对话框的文本框
    public Chronometer timer = null;
    private long recordTime = 0;        //用来记录暂停计时的相对时间（秒），恢复计时将从它开始继续计时。
    List<Board> boardList = new ArrayList<>();
    public List<Board> solutionBoardList = null; //用于播放解决方案的boardList
    public int currentStep = 0;
    public int currentStepOfSolution = 0;

    private SudokuBoard startBoard = null;
    private int dbType;
    public int mode = MODE_MANUAL;

    AlertDialog alertDialog = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //必须要先初始化board，boardview需要用到
        initBoard(savedInstanceState);

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_play_sudoku_board);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText(startBoard.name);
        textSteps = (TextView) findViewById(R.id.text_steps);
        timer = findViewById(R.id.layout_timer);
        textHint = findViewById(R.id.text_hint);
        setTextSteps();
        timerResume();
        //textHint.setText("这里是提示信息");
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
    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        SudokuBoard board = (SudokuBoard)currentBoard();
        outState.putString("dbBoardString",board.toGoingDBBoard().toJsonString());
        Log.v("sudoku",board.toGoingDBBoard().toJsonString());
    }

    public void initBoard(Bundle savedInstanceState){
        // 两个入口:从保存的状态中恢复，或者上一个activity传递过来的参数恢复。
        // 两种类型，来自于暂存的TYPE_GOING或者起始的TYPE_START，start类型要保存到数据库中去。
        DBBoard dbBoard;
        if(savedInstanceState != null){
            String dbBoardString = savedInstanceState.getString("dbBoardString");
            dbBoard = DBBoard.fromJsonString(dbBoardString);
            Log.v("sudoku","恢复dbboard:"+dbBoardString);
        }else {
            Bundle bundle = this.getIntent().getExtras();
            String dbboardString = (String) bundle.getString("dbBoardString");
            Log.v("play", "dbboard:" + dbboardString);
            dbBoard = DBBoard.fromJsonString(dbboardString);
        }
        Log.v("play", dbBoard.getBoardString());
        Board board = dbBoard.toBoard();
        if (board == null) {
            Log.v("boardview","获取board失败");
        }
        startBoard = (SudokuBoard)board;
        if(board.bestSolution != null) { board.bestSolution.printSolution(); }
        startBoard.printBoard();
        boardList.add(startBoard);
        dbType = dbBoard.dbType;
        if(dbBoard.dbType == DBBoard.DBBOARD_TYPE_START) {
            (new SudokuTask(SudokuTask.TASK_CHECK_AND_SAVE)).execute();  //检查解并保存数据库
        }
        //startBoard = new SudokuBoard(SudokuType.NINE);
        recordTime = ((SudokuBoard) board).seconds; //从此开始恢复计时
        Log.v("sudoku",String.format("recordTime=%d",recordTime));
    }
    private void handleClick(SudokuPiece sudokuPiece){
        //有改变才回调给playActivity处理
        textHint.setText("");
        //1，把该board加入list
        pushBoard(boardView.board.copyBoard());
        //2，检查，提醒
        //TODO
    }

    public void finishBoard(){
        //完成棋局时,提示用户已完成。
        //type_start则保存数据库，更新seconds
        //type_going则还需要同时删除going_boards中的记录

        //保存到数据库中，更新seconds
        (new SudokuTask(SudokuTask.TASK_SAVE_DB)).execute(); //自动保存到数据库
        //type_going，同步删除记录
        /*if(dbType == DBBoard.DBBOARD_TYPE_GOING){
            (new SudokuTask(SudokuTask.TASK_DELETE_GOING_DB)).execute();
        }*/
        (new SudokuTask(SudokuTask.TASK_DELETE_GOING_DB)).execute();
        //弹出对话框提示用户
        AlertDialog.Builder builder = new AlertDialog.Builder(PlaySudokuBoardActivity.this);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle("你完成了该局");
        startBoard.seconds = getTimer();

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
                break;
            case R.id.toolbar_hint:
                findHint();
                break;
            case R.id.toolbar_name:
                changeName();
                break;
            default:
        }
        return true;
    }
    private void showMyDialog(String message){
        if(alertDialog == null){
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PlaySudokuBoardActivity.this);
            dialogBuilder.setTitle("sudoku");
            dialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    alertDialog = null;
                }
            });
            alertDialog = dialogBuilder.create();
        }
        alertDialog.setMessage(message);
        alertDialog.show();
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
        timerStart(); //重新开始计时
    }
    public void pushBoard(Board board){
        //board.printBoard();
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
    private void help(){
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
        showMyDialog("checking");
        (new SudokuTask(SudokuTask.TASK_CHECK_SOLUTION)).execute();
    }
    private void findHint() {
        //找提示
        /*List<SudokuPiece> pieceList = ((SudokuBoard)currentBoard()).findHint();
        if (pieceList != null) {
            boardView.drawHintPieces(pieceList);
        }*/
        SudokuBoard board = (SudokuBoard) currentBoard();
        board.checkMiniNumbers();   // 找提示之前，先检查和bignumber冲突的miniNumnber
        List<SudokuPiece> pieceList = null;

        // 1.1、找备选数字中是唯一的。
        // 1.2 找同一系列备选数字唯一的.
        pieceList = board.findUniqueMiniNumber();
        if (pieceList != null) {
            Log.v("sudoku", "找到唯一数字提示");
            boardView.drawHintPieces(pieceList);
            boardView.setSelectBigNumber(board.hintMiniNumber);
            //Toast.makeText(PlaySudokuBoardActivity.this, "找到备选数字唯一的单元格", Toast.LENGTH_LONG).show();
            textHint.setText("找到备选数字唯一的单元格");
            return;
        }

        // 2、找同一系列（行，列，宫格等）中多选数字唯一的。
        pieceList = board.findUniqueMiniNumbers();
        if (pieceList != null) {
            Log.v("sudoku", "找到多选数字提示");
            boardView.drawHintPieces(pieceList);
            boardView.setSelectMiniNumber(board.hintMiniNumber);
            textHint.setText("locked candidates:找到多选组合的单元格");
            //Toast.makeText(PlaySudokuBoardActivity.this, "找到备选数字组合唯N的单元格", Toast.LENGTH_LONG).show();
            return;
        }

        //3、找一个宫格中仅有同一行/同一列/同一对角线才有备选数字的情况。（这样可以删除同一行/列/对角线的其他位置的备选数字）
        for (int n = 1; n <= board.getMaxNumber(board.sudokuType); n++) {
            //宫格中miniNumber仅在同一行出现
            pieceList = board.findMiniInLineOfSquare(n);
            if (pieceList != null && pieceList.size() > 0) {
                boardView.drawHintPieces(pieceList);
                //Toast.makeText(PlaySudokuBoardActivity.this, String.format("找到宫格中仅在同一行存在备选数字:%d", n), Toast.LENGTH_LONG).show();
                textHint.setText(String.format("宫格中仅在同一行存在备选数字:%d", n));
                boardView.setSelectMiniNumber(n);
                return;
            }
            //宫格中miniNumber仅在同一列出现
            pieceList = board.findMiniInColumnOfSquare(n);
            if (pieceList != null && pieceList.size() > 0) {
                boardView.drawHintPieces(pieceList);
                //Toast.makeText(PlaySudokuBoardActivity.this, String.format("找到宫格中仅在同一列存在备选数字:%d", n), Toast.LENGTH_LONG).show();
                textHint.setText(String.format("宫格中仅在同一列存在备选数字:%d", n));
                boardView.setSelectMiniNumber(n);
                return;
            }
            //宫格中miniNumber仅在对角线出现
            pieceList = board.findMiniInDiagnoalOfSquare(n);
            if (pieceList != null && pieceList.size() > 0) {
                boardView.drawHintPieces(pieceList);
                //Toast.makeText(PlaySudokuBoardActivity.this, String.format("找到宫格中仅在同一对角线存在备选数字:%d", n), Toast.LENGTH_LONG).show();
                textHint.setText(String.format("宫格中仅在同一对角线存在备选数字:%d", n));
                boardView.setSelectMiniNumber(n);
                return;
            }
        }
        //4、找同一行/同一列/同一对角线仅在一个宫格中才有备选数字的情况。（这样可以删除宫格的其他位置的备选数字）
        for (int n = 1; n <= board.getMaxNumber(board.sudokuType); n++) {
            //同一行
            pieceList = board.findMiniInSquareOfLine(n);
            if (pieceList != null && pieceList.size() > 0) {
                boardView.drawHintPieces(pieceList);
                //Toast.makeText(PlaySudokuBoardActivity.this, String.format("找到同一行仅在一宫格中存在备选数字:%d", n), Toast.LENGTH_LONG).show();
                textHint.setText(String.format("一行仅在同一宫格中存在备选数字:%d", n));
                boardView.setSelectMiniNumber(n);
                return;
            }
            //同一列
            pieceList = board.findMiniInSquareOfColumn(n);
            if (pieceList != null && pieceList.size() > 0) {
                boardView.drawHintPieces(pieceList);
                //Toast.makeText(PlaySudokuBoardActivity.this, String.format("找到同一列仅在一宫格中存在备选数字:%d", n), Toast.LENGTH_LONG).show();
                textHint.setText(String.format("一列仅在同一宫格中存在备选数字:%d", n));
                boardView.setSelectMiniNumber(n);
                return;
            }
            //同一对角线
            pieceList = board.findMiniInSquareOfDiagonal(n);
            if (pieceList != null && pieceList.size() > 0) {
                boardView.drawHintPieces(pieceList);
                //Toast.makeText(PlaySudokuBoardActivity.this, String.format("找到同一对角线仅在一宫格中存在备选数字:%d", n), Toast.LENGTH_LONG).show();
                textHint.setText(String.format("对角线仅在同一宫格中存在备选数字:%d", n));
                boardView.setSelectMiniNumber(n);
                return;
            }
        }

        //5 找fish
        for(int n=1; n<board.getMaxNumber(board.sudokuType); n++){
            pieceList = board.findFish(n);
            if (pieceList != null && pieceList.size() > 0) {
                boardView.drawHintPieces(pieceList);
                Log.v("debug","找到fish:"+String.valueOf(n));
                //Toast.makeText(PlaySudokuBoardActivity.this, String.format("找到fish:%d", n), Toast.LENGTH_LONG).show();
                textHint.setText(String.format("fish:备选数字%d", n));
                boardView.setSelectMiniNumber(n);
                return;
            }
        }

        //5、找finned-X-Wing
        for (int n = 1; n <= board.getMaxNumber(board.sudokuType); n++) {
            pieceList = board.findFinnedFish(n);
            if (pieceList != null && pieceList.size() > 0) {
                boardView.drawHintPieces(pieceList);
                boardView.setSelectMiniNumber(n);
                //Toast.makeText(PlaySudokuBoardActivity.this, String.format("找到finned-X-Wing:%d", n), Toast.LENGTH_LONG).show();
                textHint.setText(String.format("finned-fish:备选数字%d", n));
                return;
            }
        }

        //6、找w-wing
        pieceList = board.findW_Wing();
        if (pieceList != null && pieceList.size() != 0) {
            boardView.drawHintPieces(pieceList);
            boardView.setSelectMiniNumber(board.hintMiniNumber);
            //Toast.makeText(PlaySudokuBoardActivity.this, String.format("找到W-Wing:"), Toast.LENGTH_LONG).show();
            textHint.setText(String.format("W-Wing:红色框中piece组成W-Wing,那么可以删除piece交叉的%d备选数字",board.hintMiniNumber));
            return;
        }
        //找HiddenUR
        pieceList = board.findHiddenUniqueRectangle();
        if (pieceList != null && pieceList.size() != 0) {
            boardView.drawHintPieces(pieceList);
            boardView.setSelectMiniNumber(board.hintMiniNumber);
            //Toast.makeText(PlaySudokuBoardActivity.this, String.format("找到W-Wing:"), Toast.LENGTH_LONG).show();
            textHint.setText(String.format("W-Wing:红色框中piece组成W-Wing,那么可以删除piece交叉的%d备选数字",board.hintMiniNumber));
            return;
        }
        //7 找XYWing
        pieceList = board.findXYWing();
        if (pieceList != null && pieceList.size() != 0) {
            boardView.drawHintPieces(pieceList);
            boardView.setSelectMiniNumber(board.hintMiniNumber);
            //Toast.makeText(PlaySudokuBoardActivity.this, String.format("找到XYWing"), Toast.LENGTH_LONG).show();
            textHint.setText(String.format("XY-Wing:红色框中piece组成XY-Wing,那么可以删除piece交叉的%d备选数字",board.hintMiniNumber));
            return;
        }
        //8 找XYZWing
        pieceList = board.findXYZWing();
        if (pieceList != null && pieceList.size() != 0) {
            boardView.drawHintPieces(pieceList);
            boardView.setSelectMiniNumber(board.hintMiniNumber);
            //Toast.makeText(PlaySudokuBoardActivity.this, String.format("找到XYZWing"), Toast.LENGTH_LONG).show();
            textHint.setText(String.format("XYZ-Wing:红色框中piece组成XYZ-Wing,那么可以删除piece交叉的%d备选数字",board.hintMiniNumber));
            return;
        }

        //寻找wxyz-wing
        pieceList = board.findWXYZWing();
        if (pieceList != null && pieceList.size() != 0) {
            boardView.drawHintPieces(pieceList);
            boardView.setSelectMiniNumber(board.hintMiniNumber);
            //Toast.makeText(PlaySudokuBoardActivity.this, String.format("找到XYZWing"), Toast.LENGTH_LONG).show();
            textHint.setText(String.format("WXYZ-Wing:红色框中piece组成WXYZ-Wing,那么可以删除piece交叉的%d备选数字",board.hintMiniNumber));
            return;
        }
        //9,chain

        if(board.findChainPieces()){
            boardView.drawChainList(board.bestChainList,board.bestStartFlag,board.bestChainX);
            textHint.setText(String.format("Chain:备选数字%d的piece组成链表",board.bestChainX));
            return;
        }
        //10 找xy chain
        if (board.findXYChain()) {
            //boardView.drawChainList(board.chainList,SudokuBoard.CHAIN_WEAK_ALWAYS,board.hintMiniNumber);
            boardView.drawChainList(board.bestChainList,SudokuBoard.CHAIN_WEAK_ALWAYS,board.bestChainX);
            Log.v("debug",String.format("miniNumber=%d,chain:%s",board.bestChainX,board.pieceListToString(board.bestChainList)));
            //Toast.makeText(PlaySudokuBoardActivity.this, String.format("找到XY-chain:"), Toast.LENGTH_LONG).show();
            textHint.setText(String.format("XY-Chain:红色框中piece组成XY-Chain,那么可以起点和终点piece交叉的%d备选数字",board.bestChainX));
            return;
        }

        Log.v("sudoku","没找到提示");
        Toast.makeText(PlaySudokuBoardActivity.this,"没找到可用提示",Toast.LENGTH_LONG).show();
    }
    private void fillMiniNumbers(){
        //提示：填入所有备选数字
        boardView.board.fillMiniNumbers();
        pushBoard(boardView.board.copyBoard());
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
        //暂存
        Log.v("sudoku","暂存goingDB");
        (new SudokuTask(SudokuTask.TASK_SAVE_GOING_DB)).execute();
    }
    private void changeName(){
        LayoutInflater factory = LayoutInflater.from(PlaySudokuBoardActivity.this);
        final View textEntryView = factory.inflate(R.layout.dialog_sudoku_change_name, null);
        final EditText nameText = (EditText) textEntryView.findViewById(R.id.editText_name);

        AlertDialog.Builder builder = new AlertDialog.Builder(PlaySudokuBoardActivity.this);
        builder.setView(textEntryView);
        builder.setTitle("修改名字");
        nameText.setText(startBoard.name);
        //TODO
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                startBoard.name = nameText.getText().toString();
                boardView.board.name = startBoard.name;
                //boardlist
                for(int j=0; j<boardList.size(); j++){
                    boardList.get(j).name = startBoard.name;
                }
                toolbarTitle.setText(startBoard.name);
                (new SudokuTask(SudokuTask.TASK_SAVE_DB)).execute();
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

    private void timerStart(){
        //启动计时
        Log.v("sudoku","启动计时");

        //timer.setBase(SystemClock.elapsedRealtime());
        recordTime = 0;
        timer.start();
    }
    private void timerPause(){
        //recordTime = SystemClock.elapsedRealtime();
        recordTime = getTimer();
        timer.stop();
        Log.v("sudoku",String.format("暂停计时:%d",recordTime));
    }
    private void timerResume(){
        Log.v("sudoku",String.format("恢复计时:%d",recordTime));
        //从recordTime开始计时
        if(recordTime > 0){
            //timer.setBase((timer.getBase()+(SystemClock.elapsedRealtime()-recordTime)));
            timer.setBase(SystemClock.elapsedRealtime()-recordTime*1000);
        }
        timer.start();
    }
    private int getTimer(){
        //读取计时器，转换为秒
        /*String string = timer.getText().toString();
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
        return -1;*/
        return (int) ((SystemClock.elapsedRealtime()-timer.getBase())/1000);
    }

    private class SudokuTask extends AsyncTask<String,Integer,String> {
        public final static int TASK_SAVE_DB = 1;
        public final static int TASK_CHECK_SOLUTION = 2;
        public final static int TASK_CHECK_AND_SAVE = 3;
        public final static int TASK_SAVE_GOING_DB = 4;
        public final static int TASK_DELETE_GOING_DB = 5;
        public final static int TASK_CHANGE_NAME = 6;

        private int taskID = TASK_SAVE_DB;
        private String reply = null;
        private int resultCode = 0;

        SudokuTask(int taskID) {
            this.taskID = taskID;
        }

        @Override
        protected String doInBackground(String... paramas) {
            switch (taskID) {
                case TASK_SAVE_DB:
                    //保存DB，仅保存modifiable=piece的棋子
                    startBoard.seconds = getTimer();
                    reply = startBoard.toInitialBoard().toDBBoard().save();
                    return reply;
                case TASK_CHANGE_NAME:
                    //改名不要更新seconds
                    reply = startBoard.toInitialBoard().toDBBoard().save();
                    return reply;
                case TASK_CHECK_SOLUTION:
                    //检查board是否有解且唯一解
                    resultCode = ((SudokuBoard) currentBoard()).checkSolution();
                    return "";
                case TASK_CHECK_AND_SAVE:
                    //先检查
                    resultCode = ((SudokuBoard) currentBoard()).checkSolution();
                    if (resultCode != SudokuBoard.CHECK_SOLUTION_OK) {
                        return "";
                    }
                    //继续保存到数据库
                    reply = startBoard.toDBBoard().save();
                    return reply;
                case TASK_SAVE_GOING_DB:
                    Log.v("sudoku","save_going");
                    ((SudokuBoard)currentBoard()).seconds = getTimer();
                    reply = ((SudokuBoard)currentBoard()).toGoingDBBoard().save();
                    return reply;
                case TASK_DELETE_GOING_DB:
                    reply = ((SudokuBoard)currentBoard()).toGoingDBBoard().deleteGoingBoard();
                    return reply;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            String message = "";
            switch (taskID) {
                case TASK_SAVE_DB:
                case TASK_CHANGE_NAME:
                    //todo
                    Toast.makeText(PlaySudokuBoardActivity.this,reply,Toast.LENGTH_SHORT);
                    break;
                case TASK_CHECK_SOLUTION:
                    switch (resultCode) {
                        case SudokuBoard.CHECK_SOLUTION_OK:
                            message = "正确";
                            break;
                        case SudokuBoard.CHECK_SOLUTION_NO:
                            message = "无解";
                            break;
                        case SudokuBoard.CHECK_SOLUTION_NOT_ONLY:
                            message = "解不唯一";
                            break;
                    }
                    showMyDialog(message);
                    break;
                case TASK_CHECK_AND_SAVE:
                    switch (resultCode) {
                        case SudokuBoard.CHECK_SOLUTION_OK:
                            //显示保存到数据库的情况
                            if (reply.toLowerCase().equals("ok")) {
                                Toast.makeText(PlaySudokuBoardActivity.this, "数独检查正确并保存到数据库中", Toast.LENGTH_SHORT).show();
                            } else {
                                showMyDialog(reply);
                            }
                            return;
                        case SudokuBoard.CHECK_SOLUTION_NO:
                            showMyDialog("错误：该数独无解");
                            return;
                        case SudokuBoard.CHECK_SOLUTION_NOT_ONLY:
                            showMyDialog("错误：该数独解不唯一");
                            return;
                    }
                case TASK_SAVE_GOING_DB:
                    showMyDialog(reply);
                    return;
                case TASK_DELETE_GOING_DB:
                    Toast.makeText(PlaySudokuBoardActivity.this,reply,Toast.LENGTH_SHORT).show();
                    return;
            }
        }
    }
}