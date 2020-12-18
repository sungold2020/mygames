package com.sungold.huarongdao;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import static com.sungold.huarongdao.HuaBoard.MAX_X;
import static com.sungold.huarongdao.HuaBoard.MAX_Y;

public class HuaBoardView extends View {
    public final static int MODE_MANUAL = 1;
    public final static int MODE_HELP = 2;   //进入help模式
    int frameDivisor = 30; //边框取width/frameDivisor 。例如1/30
    int frameWidth;        //保存系统计算得到的边框宽度
    int sizeOfUnit;        //最小单元格的尺寸，棋盘的尺寸为(4*5)个size;
    int textHeight = 100;

    public List<HuaBoard> boardList = new ArrayList<>(); //一个链表存储当前检索过的步骤。
    public int currentStep = 0;
    //public Solution solution = null;
    public List<HuaBoard> solutionBoardList = null; //用于播放解决方案的boardList
    public int currentStepOfSolution = 0;
    //public Solution solution = null;

    //public List<Board>
    public static HuaBoard startBoard=null;

    public HuaPiece movingPiece = null;
    public Location pressedLocation;    //按下的棋子时，对应的x,y坐标，属于movingPiece的占据位置之一。
    public int mode = MODE_MANUAL;

    private FinishListener finishListener = null;
    public HuaBoardView(Context context){
        super(context);
        if (startBoard != null) {
            boardList.add(startBoard); //起始棋盘不能用push，只能add
            Log.v("boadview","压入startboard");
        }
    }
    public HuaBoardView(Context context, AttributeSet attrs){
        super(context,attrs);
        if (startBoard != null) {
            boardList.add(startBoard); //起始棋盘不能用push，只能add
            Log.v("boadview","压入startboard");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //获取高度和宽度，减去边框，得到一个4*5的矩阵;
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        Log.v("board",String.format("x=%d;y=%d",width,height));
        frameWidth = (int)(width / frameDivisor);
        width = width - 2* frameWidth;
        height = height - 2*frameWidth;
        if(height > width * 5/4){
            height = width *5/4;
        }else{
            width = height *4/5;
        }
        sizeOfUnit = width/4;
        Log.v("board",String.format("x=%d;y=%d;frame=%d",width,height,frameWidth));
        setMeasuredDimension(width+2*frameWidth,height+2*frameWidth+textHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        setBackgroundColor(Color.WHITE);
        //画边框
        drawFrame(canvas);
        if (startBoard == null){
            return ;
        }
        canvas.translate(frameWidth,frameWidth);
        //画棋子
        drawPieces(canvas);
        //画下面的文本框(步数)
        drawText(canvas);
    }
    public void drawFrame(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(Color.LTGRAY);
        //上框条
        canvas.drawRect(0,0,canvas.getWidth(),frameWidth,paint);
        //下框条
        //canvas.drawRect(0,canvas.getHeight()-frameWidth-textHeight,canvas.getWidth(),canvas.getHeight()-textHeight,paint);
        canvas.drawRect(0,5*sizeOfUnit+frameWidth,sizeOfUnit+frameWidth,5*sizeOfUnit+2*frameWidth,paint);
        canvas.drawRect(3*sizeOfUnit+frameWidth,5*sizeOfUnit+frameWidth,canvas.getWidth(),5*sizeOfUnit+2*frameWidth,paint);
        //左框条
        canvas.drawRect(0,0,frameWidth, canvas.getHeight()-textHeight,paint);
        //右框条
        canvas.drawRect(canvas.getWidth()-frameWidth,0,canvas.getWidth(), canvas.getHeight()-textHeight,paint);
        Log.v("board",String.format("canvas:x=%d|y=%d",canvas.getWidth(),canvas.getHeight()));

    }
    public void drawPieces(Canvas canvas){
        HuaBoard board = currentBoard();
        //画曹操
        if (board.caochao != null) {
            drawPiece(canvas, board.caochao);
        }
        if (board.jiang != null){
            for (int i = 0; i < board.jiang.length; i++) {
                drawPiece(canvas, board.jiang[i]);
            }
        }
        if (board.shuai != null){
            for(int i=0; i<board.shuai.length; i++){
                drawPiece(canvas,board.shuai[i]);
            }
        }
        if (board.bing != null) {
            for (int i = 0; i < board.bing.length; i++) {
                drawPiece(canvas, board.bing[i]);
            }
        }
        if (board.space != null) {
            for (int i = 0; i < board.space.length; i++) {
                drawPiece(canvas, board.space[i]);
                //drawPiece(canvas, board.space[1]);
            }
        }
    }
    public void drawPiece(Canvas canvas, HuaPiece piece){
        float left=0,right=0,top=0,bottom=0;
        Bitmap bitmap = null;
        Log.v("view","draw");
        piece.printPiece();
        switch(piece.type) {
            case HuaPiece.PIECE_CAOCHAO:
                left = piece.x * sizeOfUnit;
                right = left + 2 * sizeOfUnit;
                bottom = (5-piece.y) * sizeOfUnit;
                top = bottom - 2 * sizeOfUnit;
                //bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.caochao)).getBitmap();
                break;
            case HuaPiece.PIECE_HORIZON:
                left = piece.x * sizeOfUnit;
                right = left + 2 * sizeOfUnit;
                bottom = (5-piece.y) * sizeOfUnit;
                top = bottom - 1 * sizeOfUnit;
                // = ((BitmapDrawable) getResources().getDrawable(R.drawable.shuai1)).getBitmap();
                break;
            case HuaPiece.PIECE_VERTICAL:
                left = piece.x * sizeOfUnit;
                right = left + 1 * sizeOfUnit;
                bottom = (5-piece.y) * sizeOfUnit;
                top = bottom - 2 * sizeOfUnit;
                //bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.jiang1)).getBitmap();
                break;
            case HuaPiece.PIECE_SOLDIER:
                left = piece.x * sizeOfUnit;
                right = left + 1 * sizeOfUnit;
                bottom = (5-piece.y) * sizeOfUnit;
                top = bottom - 1 * sizeOfUnit;
                //bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.bing)).getBitmap();
                break;
            case HuaPiece.PIECE_EMPTY:
                left = piece.x * sizeOfUnit;
                right = left + 1 * sizeOfUnit;
                bottom = (5-piece.y) * sizeOfUnit;
                top = bottom - 1 * sizeOfUnit;
                break;
                //bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.space)).getBitmap();
            default:
                Log.v("boardview","error type");
        }
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        //canvas.drawRect(left,top,right,bottom,paint);
        Log.v("view",String.format("%f,%f,%f,%f",left,top,right,bottom));
        canvas.drawRect(left,top,right,bottom,paint);
        //填充图片
        Rect destRect = new Rect((int)left+5,(int)top+5,(int)right-5,(int)bottom-5);
        canvas.drawBitmap(getBitmap(piece),null,destRect,paint);
    }
    private Bitmap getBitmap(HuaPiece piece){
        if (piece.name.equals("将一")){
            return ((BitmapDrawable) getResources().getDrawable(R.drawable.jiang1)).getBitmap();
        }else if(piece.name.equals("将二")){
            return ((BitmapDrawable) getResources().getDrawable(R.drawable.jiang2)).getBitmap();
        }else if(piece.name.equals("将三")){
            return ((BitmapDrawable) getResources().getDrawable(R.drawable.jiang3)).getBitmap();
        }else if (piece.name.equals("将四")){
            return ((BitmapDrawable) getResources().getDrawable(R.drawable.jiang4)).getBitmap();
        }else if (piece.name.equals("将五")){
            return ((BitmapDrawable) getResources().getDrawable(R.drawable.jiang5)).getBitmap();
        }else if (piece.name.equals("帅一")){
            return ((BitmapDrawable) getResources().getDrawable(R.drawable.shuai1)).getBitmap();
        }else if (piece.name.equals("帅二")){
            return ((BitmapDrawable) getResources().getDrawable(R.drawable.shuai2)).getBitmap();
        }else if (piece.name.equals("帅三")){
            return ((BitmapDrawable) getResources().getDrawable(R.drawable.shuai3)).getBitmap();
        }else if (piece.name.equals("帅四")){
            return ((BitmapDrawable) getResources().getDrawable(R.drawable.shuai4)).getBitmap();
        }else if (piece.name.equals("帅五")){
            return ((BitmapDrawable) getResources().getDrawable(R.drawable.shuai5)).getBitmap();
        }else if (piece.name.equals("曹操")){
            return ((BitmapDrawable) getResources().getDrawable(R.drawable.caochao)).getBitmap();
        }else if (piece.name.startsWith("兵")){
            return ((BitmapDrawable) getResources().getDrawable(R.drawable.bing)).getBitmap();
        }else if (piece.name.startsWith("空")){
            return ((BitmapDrawable) getResources().getDrawable(R.drawable.space)).getBitmap();
        }else{
            return null;
        }
    }
    public void drawText(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        /*
        canvas.drawLine(0,5*sizeOfUnit+2*frameWidth,canvas.getWidth(),5*sizeOfUnit+2*frameWidth,paint);
        canvas.drawLine(0,canvas.getHeight(),canvas.getWidth(),canvas.getHeight(),paint);
        canvas.drawLine(0,5*sizeOfUnit+2*frameWidth,0,canvas.getHeight(),paint);
        canvas.drawLine(canvas.getWidth(),5*sizeOfUnit+2*frameWidth,canvas.getWidth(),canvas.getHeight(),paint);*/

        String text;
        String bestText = "";
        if (startBoard.bestSolution != null){
            bestText = String.format("最优步数:%d",startBoard.bestSolution.getSteps());
        }
        if (mode == MODE_MANUAL) {
            text = String.format("当前步数：%d              %s", currentStep,bestText);
        }else{
            text = String.format("当前步数：%d              %s", currentStepOfSolution,bestText);
        }
        paint.setColor(Color.BLACK);
        paint.setTextSize(70);
        canvas.drawText(text,0,(2*frameWidth+5*sizeOfUnit+textHeight/2-10),paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mode == MODE_HELP) { return false; }
        float x = event.getX();
        float y = event.getY();
        Location destLocation;
        int direction = -1;
        HuaBoard newBoard;
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                movingPiece = getPiece(x,y);
                if (movingPiece.type == HuaPiece.PIECE_EMPTY) {
                    movingPiece = null; //空格不可移动
                }
                if (movingPiece == null){
                    return true;
                }
                pressedLocation = getLocation(x,y);
                Log.v("view",String.format("你按下了%s",movingPiece.name));
                break;
            case MotionEvent.ACTION_MOVE:
                /*destPiece = getPiece(x,y);
                if (destPiece ==null){
                    return true;
                }
                newBoard = currentBoard().newBoardAfterMove(movingPiece,destPiece);*/
                /*destPiece = getPiece(x,y); //获取图像坐标(x,y)对应的棋子
                if (movingPiece == null || destPiece == null) { return true; }
                direction = getMovingDirection(movingPiece.x,movingPiece.y,destPiece.x,destPiece.y);
                if (direction == -1) { return  true; }
                newBoard = currentBoard().newBoardAfterMove(movingPiece,direction);
                if (newBoard != null){
                    pushBoard(newBoard);
                    invalidate();
                }
                Log.v("view",String.format("move %s",destPiece.name));*/
                //重新绘制
                break;
            case MotionEvent.ACTION_UP:
                /*destPiece = getPiece(x,y);
                if (destPiece == null){
                    return true;
                }
                newBoard = currentBoard().newBoardAfterMove(movingPiece,destPiece);*/
                destLocation = getLocation(x,y);
                if (movingPiece == null || destLocation == null) { return true; }
                direction = getMovingDirection(pressedLocation,destLocation);
                if (direction == -1) { return true; }
                newBoard = currentBoard().newBoardAfterMove(movingPiece,direction);
                if (newBoard != null){
                    pushBoard(newBoard);
                    invalidate();
                }
                Log.v("view",String.format("目标移动到%d,%d",destLocation.x,destLocation.y));
                break;
        }
        return true;
    }

    public HuaPiece getPiece(float x, float y){
        //根据指定坐标获取棋子
        int piece_x;
        int piece_y;
        piece_x = (int) (x-frameWidth)/sizeOfUnit;
        piece_y = (int) (y-frameWidth)/sizeOfUnit;
        piece_y = MAX_Y-1 - piece_y; //转换坐标
        if(piece_x >= MAX_X || piece_x < 0 || piece_y >= MAX_Y || piece_y < 0){
            return null;
        }
        return currentBoard().pieces[piece_x][piece_y];
    }
    public Location getLocation(float x,float y){
       /*( //转换android坐标x,y到棋盘位置的x,y
        int piece_x;
        int piece_y;
        piece_x = (int) (x-frameWidth)/sizeOfUnit;
        piece_y = (int) (y-frameWidth)/sizeOfUnit;
        piece_y = MAX_Y-1 - piece_y; //转换坐标
        if(piece_x >= MAX_X || piece_x < 0 || piece_y >= MAX_Y || piece_y < 0){
            return null;
        }
        return new Location(piece_x,piece_y);*/
       return new Location((int)x,(int)y);
    }
    public int getMovingDirection(Location location1,Location location2){
        //从(x1,y1)->(x2,y2)的方向
        int x1 = location1.x, y1 = location1.y;
        int x2 = location2.x, y2 = location2.y;
        /*if(x1 == x2){
            if (y2 > y1){
                return Piece.DIRECTION_UP;
            }else if (y2 < y1){
                return Piece.DIRECTION_DOWN;
            }else{
                return -1;
            }
        }else if(y1 == y2){
            if (x2 > x1){
                return Piece.DIRECTION_RIGHT;
            }else if (x2 < x1){
                return Piece.DIRECTION_LEFT;
            }else{
                return -1;
            }
        }else{
            return -1;
        }*/
        if (Math.abs(x1-x2) > Math.abs(y1-y2)){
            //水平方向
            if (Math.abs(x1-x2) < sizeOfUnit/4) { return -1; } //移动小于1/4个单元格，忽略
            if (x2 > x1){
                return HuaPiece.DIRECTION_RIGHT;
            }else{
                return HuaPiece.DIRECTION_LEFT;
            }
        }else{
            //垂直方向
            if(Math.abs(y1-y2) < sizeOfUnit/4) { return -1; }
            if (y2 > y1){ //android坐标Y是朝下的
                return HuaPiece.DIRECTION_DOWN;
            }else{
                return HuaPiece.DIRECTION_UP;
            }
        }
    }
    public void pushBoard(HuaBoard board){
        //当回退到某一步时，如果这个时候手工移动了棋子，就删除currentStep以后的棋盘，兵从当前位置add
        while(currentStep < boardList.size()-1){
            popBoard();
        }
        boardList.add(board);
        if (boardList.size() > 1) { currentStep++; } //加入第一个startboard时不可以currentstep++
        if (currentBoard().isSuccess()){
            finishListener.finish(); //成功，回调给activity进行处理
        }
    }
    public void popBoard(){
        if (boardList.size() == 1) {
            return;
        }
        boardList.remove(boardList.size() - 1);
    }
    public HuaBoard currentBoard(){
        if (mode == MODE_MANUAL) {
            if (boardList.size() == 0){
                return null;
            }
            return boardList.get(currentStep);
        }else{
            return solutionBoardList.get(currentStepOfSolution);
        }
    }
    public void printStep(){
        System.out.println("步骤如下：");
        for(int i=0; i<boardList.size(); i++){
            String name = boardList.get(i).getNextStepName();
            String direction = boardList.get(i).getNextStepDirection();
            System.out.println(String.format("%s move %s",name,direction));
        }
        System.out.println(String.format("total step:%d",boardList.size()-1));
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
        invalidate();
    }
    public void forward(){
        // TODO
        if (mode == MODE_MANUAL) {
            if (currentStep < boardList.size()-1){
                currentStep++;
            }
        }else{
            if (currentStepOfSolution < solutionBoardList.size() - 1) {
                currentStepOfSolution++;
            }
        }
        invalidate();
    }
    public void reset(){
        if (mode == MODE_MANUAL){
            boardList.clear();
            boardList.add(startBoard); //第一个board不要用push
            currentStep = 0;
            invalidate();
        }else{
            currentStepOfSolution = 0;
            invalidate();
        }
    }
    public void refreshBoard(){
        boardList.clear();
        if (startBoard != null) {boardList.clear();}
        invalidate();
    }
    public void help(Solution solution){
        //找出解，然后进入help模式
        if (solution != null){
            solutionBoardList = solution.buildHuaBoardList(startBoard);
            startBoard.bestSolution = solution;
        }else if(startBoard.bestSolution != null){
            solutionBoardList = startBoard.bestSolution.buildHuaBoardList(startBoard);
        }else{
            return;
        }
        mode = MODE_HELP;
        currentStepOfSolution = 0;
        invalidate();
        //(new SaveTask(startBoard,solution)).execute();
    }
    public void setMode(int mode){
        this.mode = mode;
        switch (mode){
            case MODE_HELP:
                currentStepOfSolution = 0;
                break;
            case MODE_MANUAL:
                break;
            default:
                Log.v("boardview","unknown mode");
        }
        invalidate();
    }
   /* private class SaveTask extends  AsyncTask<String,Integer,String> {
        private  Board board;
        private  Solution solution;
        String sendString;
        SaveTask(Board board,Solution solution) {
            this.board = board;
            this.solution = solution;
            sendString = toJsonString();
        }
        private String toJsonString(){
            try{
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("command","save");
                jsonObject.put("board",board.toJsonString());
                jsonObject.put("solution",solution.toJson());
                return jsonObject.toString();
            }catch (Exception e){
                e.printStackTrace();
                return "";
            }
        }
        @Override
        protected String doInBackground(String... paramas) {
            MySocket mySocket = new MySocket();
            if (!mySocket.connect()) {
                return "连接服务器异常";
            }
            if (sendString.equals("") || !mySocket.send(sendString)) {
                return "发送消息失败";
            }
            String reply = mySocket.recieve().toString();
            if (reply.equals("")){
                return "接收消息失败";
            }
            Log.v("TorrentTask",reply);
            return "执行成功";
        }
        @Override
        protected void onPostExecute(String result){
            Toast.makeText(getContext(), result, Toast.LENGTH_LONG).show();
        }
    }
*/
    public interface FinishListener{
        public void finish();
    }
    public void setOnFinishListener(FinishListener finishListener){
        this.finishListener = finishListener;
    }
    class Location{
        int x;
        int y;
        Location(int x,int y){
            this.x = x;
            this.y = y;
        }
    }
}

