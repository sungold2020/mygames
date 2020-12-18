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

public class BlockBoardView extends View {
    public final static int MODE_MANUAL = 1;
    public final static int MODE_HELP = 2;   //进入help模式

    public final static int frameDivisor = 30; //边框取width/frameDivisor 。例如1/30

    int maxX, maxY;        //从baord获取到的棋盘大小
    //int exitUnit;          //出口，如果king为横条，取棋盘坐标y;，否则取棋盘坐标x;
    int frameWidth;        //保存系统计算得到的边框宽度
    int sizeOfUnit;        //最小单元格的尺寸
    int textHeight = 100;

    public List<BlockBoard> boardList = new ArrayList<>(); //一个链表存储当前检索过的步骤。
    public int currentStep = 0;
    //public Solution solution = null;
    public List<BlockBoard> solutionBlockBoardList = null; //用于播放解决方案的boardList
    public int currentStepOfSolution = 0;
    //public Solution solution = null;

    //public List<BlockBoard>
    public static BlockBoard startBlockBoard=null;

    public BlockPiece movingBlockPiece = null;
    public Location pressedLocation;    //按下的棋子时，对应的x,y坐标，属于movingBlockPiece的占据位置之一。
    public int mode = MODE_MANUAL;

    private FinishListener finishListener = null;
    public void init(){
        maxX = startBlockBoard.maxX;
        maxY = startBlockBoard.maxY;
        /*if (startBlockBoard.kingBlock.type == BlockPiece.PIECE_VERTICAL){
            exitUnit = startBlockBoard.kingBlock.x;
        }else{
            exitUnit = startBlockBoard.kingBlock.y;
        }*/

        boardList.add(startBlockBoard); //起始棋盘不能用push，只能add
    }
    public BlockBoardView(Context context){
        super(context);
        init();
    }
    public BlockBoardView(Context context, AttributeSet attrs){
        super(context,attrs);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //获取高度和宽度，减去边框，得到一个4*5的矩阵;
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        Log.v("board",String.format("x=%d;y=%d",width,height));
        frameWidth = (int)(width / frameDivisor);
        width = width - 2* frameWidth;
        height = height - 2*frameWidth-textHeight;
        if(height/maxY > width/maxX ){
            height = width *maxY/maxY;
        }else{
            width = height *maxX/maxY;
        }
        sizeOfUnit = width/maxX;
        Log.v("board",String.format("x=%d;y=%d;frame=%d",width,height,frameWidth));
        setMeasuredDimension(width+2*frameWidth,height+2*frameWidth+textHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        setBackgroundColor(Color.WHITE);
        //画边框
        drawFrame(canvas);
        if (startBlockBoard == null){
            return ;
        }
        if (startBlockBoard.kingBlock != null ) { drawExit(canvas); } //出口要依赖于kingblock

        canvas.translate(frameWidth,frameWidth);
        //画棋子
        drawBlockPieces(canvas);
        //画下面的文本框(步数)
        drawText(canvas);
    }
    public void drawFrame(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(Color.DKGRAY);
        //设置背景
        canvas.drawRect(0,0,canvas.getWidth(),canvas.getHeight(),paint);

        paint.setColor(Color.LTGRAY);
        //上框条
        canvas.drawRect(0,0,canvas.getWidth(),frameWidth,paint);
        //下框条
        canvas.drawRect(0,canvas.getHeight()-frameWidth-textHeight,canvas.getWidth(),canvas.getHeight()-textHeight,paint);
        //左框条
        canvas.drawRect(0,0,frameWidth, canvas.getHeight()-textHeight,paint);
        //右框条
        canvas.drawRect(canvas.getWidth()-frameWidth,0,canvas.getWidth(), canvas.getHeight()-textHeight,paint);

        Log.v("board",String.format("canvas:x=%d|y=%d",canvas.getWidth(),canvas.getHeight()));
    }
    public void drawExit(Canvas canvas){
        //画出口
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        float left,right,bottom,top;
        if (startBlockBoard.kingBlock.type == BlockPiece.PIECE_VERTICAL){
            left = frameWidth + startBlockBoard.kingBlock.x * sizeOfUnit;
            right = left + sizeOfUnit;
            if (startBlockBoard.destPointer > 0){
                //出口在顶部
                top = 0;
            }else{
                top = frameWidth + maxY * sizeOfUnit;
            }
            bottom = frameWidth;
        }else{
            bottom = frameWidth + (maxY - startBlockBoard.kingBlock.y) * sizeOfUnit;
            top = bottom - sizeOfUnit;
            if (startBlockBoard.destPointer > 0){
                //在右边
                left = frameWidth + maxX * sizeOfUnit;
            }else{
                left = 0;
            }
            right = left + frameWidth;
        }
        canvas.drawRect(left,top,right,bottom,paint);
    }

    public void drawBlockPieces(Canvas canvas){
        BlockBoard board = currentBlockBoard();
        //画king
        if (board.kingBlock != null) {
            drawBlockPiece(canvas, board.kingBlock);
        }
        if (board.verticalBlocks != null){
            for (int i = 0; i < board.verticalBlocks.length; i++) {
                drawBlockPiece(canvas, board.verticalBlocks[i]);
            }
        }
        if (board.horizonBlocks != null){
            for(int i=0; i<board.horizonBlocks.length; i++){
                drawBlockPiece(canvas,board.horizonBlocks[i]);
            }
        }
    }
    public void drawBlockPiece(Canvas canvas,BlockPiece block){
        float left=0,right=0,top=0,bottom=0;
        Bitmap bitmap = null;
        //Log.v("view","draw");
        //block.printBlockPiece();
        switch(block.type) {
            case BlockPiece.PIECE_HORIZON:
                left = block.x * sizeOfUnit;
                right = left + block.length * sizeOfUnit;
                bottom = (maxY-block.y) * sizeOfUnit;
                top = bottom - sizeOfUnit;
                // = ((BitmapDrawable) getResources().getDrawable(R.drawable.shuai1)).getBitmap();
                break;
            case BlockPiece.PIECE_VERTICAL:
                left = block.x * sizeOfUnit;
                right = left + sizeOfUnit;
                bottom = (maxY-block.y) * sizeOfUnit;
                top = bottom - block.length * sizeOfUnit;
                //bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.jiang1)).getBitmap();
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
        canvas.drawBitmap(getBitmap(block),null,destRect,paint);
    }
    private Bitmap getBitmap(BlockPiece block){
        if (block.name.equals("王")) {
            return ((BitmapDrawable) getResources().getDrawable(R.drawable.block_king)).getBitmap();
        }
        return ((BitmapDrawable) getResources().getDrawable(R.drawable.block_block)).getBitmap();
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
        if (startBlockBoard.bestSolution != null){
            bestText = String.format("最优步数:%d",startBlockBoard.bestSolution.getSteps());
        }
        if (mode == MODE_MANUAL) {
            text = String.format("当前步数：%d             %s", currentStep,bestText);
        }else{
            text = String.format("当前步数：%d             %s", currentStepOfSolution,bestText);
        }
        paint.setColor(Color.BLACK);
        paint.setTextSize(70);
        canvas.drawText(text,0,(2*frameWidth+maxY*sizeOfUnit+textHeight/2-10),paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mode == MODE_HELP) { return false; }
        float x = event.getX();
        float y = event.getY();
        Location destLocation;
        int direction = -1;
        BlockBoard newBlockBoard;
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                movingBlockPiece = getBlockPiece(x,y);
                if (movingBlockPiece == null){
                    return true;
                }
                pressedLocation = getLocation(x,y);
                Log.v("view",String.format("你按下了%s",movingBlockPiece.name));
                break;
            case MotionEvent.ACTION_MOVE:
                /*destBlockPiece = getBlockPiece(x,y);
                if (destBlockPiece ==null){
                    return true;
                }
                newBlockBoard = currentBlockBoard().newBlockBoardAfterMove(movingBlockPiece,destBlockPiece);*/
                /*destBlockPiece = getBlockPiece(x,y); //获取图像坐标(x,y)对应的棋子
                if (movingBlockPiece == null || destBlockPiece == null) { return true; }
                direction = getMovingDirection(movingBlockPiece.x,movingBlockPiece.y,destBlockPiece.x,destBlockPiece.y);
                if (direction == -1) { return  true; }
                newBlockBoard = currentBlockBoard().newBlockBoardAfterMove(movingBlockPiece,direction);
                if (newBlockBoard != null){
                    pushBlockBoard(newBlockBoard);
                    invalidate();
                }
                Log.v("view",String.format("move %s",destBlockPiece.name));*/
                //重新绘制
                break;
            case MotionEvent.ACTION_UP:
                /*destBlockPiece = getBlockPiece(x,y);
                if (destBlockPiece == null){
                    return true;
                }
                newBlockBoard = currentBlockBoard().newBlockBoardAfterMove(movingBlockPiece,destBlockPiece);*/
                destLocation = getLocation(x,y);
                if (movingBlockPiece == null || destLocation == null) { return true; }
                direction = getMovingDirection(pressedLocation,destLocation);
                if (direction == -1) { return true; }
                newBlockBoard = currentBlockBoard().newBoardAfterMove(movingBlockPiece,direction);
                if (newBlockBoard != null){
                    pushBlockBoard(newBlockBoard);
                    invalidate();
                }
                Log.v("view",String.format("目标移动到%d,%d",destLocation.x,destLocation.y));
                break;
        }
        return true;
    }

    public BlockPiece getBlockPiece(float x,float y){
        //根据指定坐标获取棋子
        int block_x;
        int block_y;
        block_x = (int) (x-frameWidth)/sizeOfUnit;
        block_y = (int) (y-frameWidth)/sizeOfUnit;
        block_y = maxY-1 - block_y; //转换坐标

        return currentBlockBoard().getBlockPiece(block_x,block_y);
    }
    public Location getLocation(float x,float y){
       /*( //转换android坐标x,y到棋盘位置的x,y
        int block_x;
        int block_y;
        block_x = (int) (x-frameWidth)/sizeOfUnit;
        block_y = (int) (y-frameWidth)/sizeOfUnit;
        block_y = MAX_Y-1 - block_y; //转换坐标
        if(block_x >= MAX_X || block_x < 0 || block_y >= MAX_Y || block_y < 0){
            return null;
        }
        return new Location(block_x,block_y);*/
        return new Location((int)x,(int)y);
    }
    public int getMovingDirection(Location location1,Location location2){
        //从(x1,y1)->(x2,y2)的方向
        int x1 = location1.x, y1 = location1.y;
        int x2 = location2.x, y2 = location2.y;
        /*if(x1 == x2){
            if (y2 > y1){
                return BlockPiece.DIRECTION_UP;
            }else if (y2 < y1){
                return BlockPiece.DIRECTION_DOWN;
            }else{
                return -1;
            }
        }else if(y1 == y2){
            if (x2 > x1){
                return BlockPiece.DIRECTION_RIGHT;
            }else if (x2 < x1){
                return BlockPiece.DIRECTION_LEFT;
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
                return BlockPiece.DIRECTION_RIGHT;
            }else{
                return BlockPiece.DIRECTION_LEFT;
            }
        }else{
            //垂直方向
            if(Math.abs(y1-y2) < sizeOfUnit/4) { return -1; }
            if (y2 > y1){ //android坐标Y是朝下的
                return BlockPiece.DIRECTION_DOWN;
            }else{
                return BlockPiece.DIRECTION_UP;
            }
        }
    }
    public void pushBlockBoard(BlockBoard board){
        //当回退到某一步时，如果这个时候手工移动了棋子，就删除currentStep以后的棋盘，兵从当前位置add
        while(currentStep < boardList.size()-1){
            popBlockBoard();
        }
        boardList.add(board);
        if (boardList.size() > 1) { currentStep++; } //加入第一个startboard时不可以currentstep++
        if (currentBlockBoard().isSuccess()){
            finishListener.finish(); //成功，回调给activity进行处理
        }
    }
    public void popBlockBoard(){
        if (boardList.size() == 1) {
            return;
        }
        boardList.remove(boardList.size() - 1);
    }
    public BlockBoard currentBlockBoard(){
        if (mode == MODE_MANUAL) {
            if (boardList.size() == 0){
                return null;
            }
            return boardList.get(currentStep);
        }else{
            return solutionBlockBoardList.get(currentStepOfSolution);
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
            if (currentStepOfSolution < solutionBlockBoardList.size() - 1) {
                currentStepOfSolution++;
            }
        }
        invalidate();
    }
    public void reset(){
        if (mode == MODE_MANUAL){
            boardList.clear();
            boardList.add(startBlockBoard); //第一个board不要用push
            currentStep = 0;
            invalidate();
        }else{
            currentStepOfSolution = 0;
            invalidate();
        }
    }
    public void refreshBlockBoard(){
        boardList.clear();
        if (startBlockBoard != null) {boardList.clear();}
        invalidate();
    }

    public void help(Solution solution){
        //找出解，然后进入help模式
        if (solution != null){
            solutionBlockBoardList = solution.buildBlockBoardList(startBlockBoard);
            startBlockBoard.bestSolution = solution;
        }else if(startBlockBoard.bestSolution != null){
            solutionBlockBoardList = startBlockBoard.bestSolution.buildBlockBoardList(startBlockBoard);
        }else{
            return;
        }
        mode = MODE_HELP;
        currentStepOfSolution = 0;
        invalidate();
        //(new SaveTask(startBlockBoard,solution)).execute();
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
         private  BlockBoard board;
         private  Solution solution;
         String sendString;
         SaveTask(BlockBoard board,Solution solution) {
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

