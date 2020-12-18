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


public class BoardView extends View {
    public final static int MODE_HELP = 1;   //帮助模式
    public final static int MODE_MANUAL = 2; //手工模式
    public final static int MODE_ADDBOARD = 3; //布局模式

    int frameDivisor = 2; //边框取sizeOfUnit的1/2
    int frameWidth;        //保存系统计算得到的边框宽度
    int sizeOfUnit;        //最小单元格的尺寸，棋盘的尺寸为maxX,maxY
    public int maxX = 6,maxY = 6;

    public Board board = null;
    private int mode = MODE_MANUAL;  //缺省是手工模式
    public Piece movingPiece = null;
    private int movingDestX = -1,movingDestY = -1; //移动的棋子当前移动位置的坐标
    public Location pressedLocation;    //按下的棋子时，对应的x,y坐标，属于movingPiece的占据位置之一。
    private long downTime;

    private ActionListener actionListener = null;
    public BoardView(Context context){
        super(context);
    }
    public BoardView(Context context, AttributeSet attrs){
        super(context,attrs);
    }
    public void setBoard(Board board){
        this.board = board;
        maxX = board.maxX;
        maxY = board.maxY;
        //invalidate();
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //获取高度和宽度，减去边框，得到一个4*5的矩阵;
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        Log.v("board",String.format("x=%d;y=%d",width,height));
        if(maxX > 0) {
            sizeOfUnit = Math.min(width/(maxX+2/frameDivisor), width/(maxY+2/frameDivisor));
            width = (maxX+2/frameDivisor) * sizeOfUnit;
            height = (maxY+2/frameDivisor) * sizeOfUnit;
            frameWidth = (int) (sizeOfUnit / frameDivisor);

            setMeasuredDimension(width, height);
        }
        Log.v("board", String.format("x=%d;y=%d;frame=%d", width, height, frameWidth));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        setBackgroundColor(Color.WHITE);
        //画边框
        drawFrame(canvas);
        if (board == null){
            return ;
        }
        //画出口
        drawExit(canvas);

        canvas.translate(frameWidth,frameWidth);
        //画棋子
        drawPieces(canvas);

    }
    public void drawFrame(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(Color.LTGRAY);
        //上框条
        canvas.drawRect(0,0,canvas.getWidth(),frameWidth,paint);
        //下框条
        canvas.drawRect(0,canvas.getHeight()-frameWidth,canvas.getWidth(),canvas.getHeight(),paint);
        //左框条
        canvas.drawRect(0,0,frameWidth, canvas.getHeight(),paint);
        //右框条
        canvas.drawRect(canvas.getWidth()-frameWidth,0,canvas.getWidth(), canvas.getHeight(),paint);
        Log.v("board",String.format("canvas:x=%d|y=%d",canvas.getWidth(),canvas.getHeight()));

    }
    public void drawPieces(Canvas canvas){
        if (movingPiece != null){
            drawMovingPiece(canvas);
        }
        switch (board.gameType){
            case HUARONGDAO:
                drawPiecesHua(canvas);
                break;
            case BLOCK:
                drawPiecesBlock(canvas);
                break;
            case BOX:
                Log.v("boardview","beging draw box pieces");
                drawPiecesBox(canvas);
                break;
            default:
                return;
        }
    }
    private void drawPiecesHua(Canvas canvas){
        //TODO
    }
    private void drawPiecesBlock(Canvas canvas){
        BrickBoard brickBoard = (BrickBoard) board;
        if(brickBoard.kingBlock != null) {
            drawPiece(canvas, brickBoard.kingBlock);
        }
        //画竖条
        if(brickBoard.verticalBlocks != null) {
            for (int i = 0; i < brickBoard.verticalBlocks.length; i++) {
                drawPiece(canvas, brickBoard.verticalBlocks[i]);
            }
        }
        //画横条
        if(brickBoard.horizonBlocks != null) {
            for (int i = 0; i < brickBoard.horizonBlocks.length; i++) {
                drawPiece(canvas, brickBoard.horizonBlocks[i]);
            }
        }
    }
    private void drawPiecesBox(Canvas canvas){
        BoxBoard boxBoard = (BoxBoard) board;
        if(boxBoard.boy != null) {
            Log.v("boardview","draw boy");
            drawPiece(canvas, boxBoard.boy);
        }
        //画blocks
        if(boxBoard.blocks != null) {
            for (int i = 0; i < boxBoard.blocks.length; i++) {
                drawPiece(canvas, boxBoard.blocks[i]);
            }
        }
        //画boxs
        if(boxBoard.boxs != null) {
            for (int i = 0; i < boxBoard.boxs.length; i++) {
                drawPiece(canvas, boxBoard.boxs[i]);
            }
        }
        //画目标，分几种情况，1、和boy重叠，2、和box重叠，3、未重叠
        Log.v("boardview","开始画目标");
        if(boxBoard.destPointers != null){
            for(int i=0; i < boxBoard.destPointers.length; i++){
                int x = boxBoard.destPointers[i].x;
                int y = boxBoard.destPointers[i].y;
                Piece newPiece = boxBoard.getPiece(x,y).copyPiece();
                if(newPiece == null || newPiece.type == Piece.PIECE_DEST){
                    drawPiece(canvas,boxBoard.destPointers[i]);
                }else if(newPiece.type == Piece.PIECE_BOX){ //和BOX重叠
                    newPiece.type = Piece.PIECE_DEST_BOX;
                    drawPiece(canvas,newPiece);
                }else if(newPiece.type == Piece.PIECE_BOY){
                    newPiece.type = Piece.PIECE_DEST_BOY;
                    drawPiece(canvas,newPiece);
                }
                else{
                    Log.v("boardview",String.format("newPiece:type=%d,name=%s",newPiece.type,newPiece.name));
                }
            }
        }else{
            Log.v("boardview","目标是空的");
        }
    }
    public void drawPiece(Canvas canvas, Piece piece){
        float left=0,right=0,top=0,bottom=0;
        Bitmap bitmap = null;
        Log.v("boardview","draw");
        Log.v("boardview",String.format("newPiece:type=%d,name=%s",piece.type,piece.name));
        piece.printPiece();
        switch(piece.type) {
            case Piece.PIECE_CAOCHAO:
                left = piece.x * sizeOfUnit;
                right = left + 2 * sizeOfUnit;
                bottom = (maxY-piece.y) * sizeOfUnit;
                top = bottom - 2 * sizeOfUnit;
                break;
            case Piece.PIECE_HORIZON:
                left = piece.x * sizeOfUnit;
                right = left + piece.length * sizeOfUnit;
                bottom = (maxY-piece.y) * sizeOfUnit;
                top = bottom - 1 * sizeOfUnit;
                break;
            case Piece.PIECE_VERTICAL:
                left = piece.x * sizeOfUnit;
                right = left + 1 * sizeOfUnit;
                bottom = (maxY-piece.y) * sizeOfUnit;
                top = bottom - piece.length * sizeOfUnit;
                break;
            case Piece.PIECE_SOLDIER:
            case Piece.PIECE_EMPTY:
            case Piece.PIECE_BLOCK:
            case Piece.PIECE_BOX:
            case Piece.PIECE_BOY:
            case Piece.PIECE_DEST:
            case Piece.PIECE_DEST_BOX:
            case Piece.PIECE_DEST_BOY:
                left = piece.x * sizeOfUnit;
                right = left + 1 * sizeOfUnit;
                bottom = (maxY-piece.y) * sizeOfUnit;
                top = bottom - 1 * sizeOfUnit;
                break;
            default:
                Log.v("boardview","error type");
                return ;
        }
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        //canvas.drawRect(left,top,right,bottom,paint);
        Log.v("view",String.format("%f,%f,%f,%f",left,top,right,bottom));
        canvas.drawRect(left,top,right,bottom,paint);
        //填充图片
        Rect destRect = new Rect((int)left+5,(int)top+5,(int)right-5,(int)bottom-5);
        canvas.drawBitmap(piece.getBitmap(this),null,destRect,paint);
    }
    public void drawMovingPiece(Canvas canvas){
        if (movingPiece == null){
            return;
        }
        int left=0,top=0,right=0,bottom=0;
        left = movingDestX;
        bottom = movingDestY;

        left = movingDestX;
        bottom = movingDestY;
        right = left + movingPiece.getWidth()*sizeOfUnit;
        top = bottom - movingPiece.getHeight()*sizeOfUnit;
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        canvas.drawRect(left,top,right,bottom,paint);
        //填充图片
        Rect destRect = new Rect((int)left+5,(int)top+5,(int)right-5,(int)bottom-5);
        canvas.drawBitmap(movingPiece.getBitmap(this),null,destRect,paint);
    }
    public void drawExit(Canvas canvas){
        int left,right,top,bottom;
        switch (board.gameType){
            case BLOCK:
                BrickBoard brickBoard = (BrickBoard) board;
                if(brickBoard.kingBlock == null) {return;}
                if(brickBoard.kingBlock.type == Piece.PIECE_VERTICAL){
                    //垂直出口
                    left = brickBoard.kingBlock.x * sizeOfUnit + frameWidth;
                    right = left + sizeOfUnit;
                    if(brickBoard.destPointer == 0) { //下方
                        top = frameWidth + brickBoard.maxY  * sizeOfUnit;
                        bottom = top + frameWidth;
                    }else{//上方
                        top = 0;
                        bottom = frameWidth;
                    }
                }else {
                    //水平出口
                    bottom = (brickBoard.maxY - brickBoard.kingBlock.y) * sizeOfUnit + frameWidth;
                    top = bottom - sizeOfUnit;
                    if(brickBoard.destPointer == 0){ //左边
                        left = 0;
                        right = frameWidth;
                    }else {   //右边
                        left = maxX * sizeOfUnit + frameWidth;
                        right = left + frameWidth;
                    }
                }
                break;
            default:
                return;
        }
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        canvas.drawRect(left,top,right,bottom,paint);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mode == MODE_HELP) { return false; }
        float x = event.getX();
        float y = event.getY();
        Location destLocation;
        int direction = -1;
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                movingPiece = getPiece(x,y);
                if (movingPiece != null) {Log.v("view",String.format("你按下了%s",movingPiece.name)); }
                //if (movingPiece == null) { return true; }

                actionListener.onActionDown(movingPiece);  //回调给activity处理
                pressedLocation = new Location((int)x,(int)y);
                downTime = event.getDownTime();
                //Log.v("view",String.format("你按下了%s",movingPiece.name));
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
                //actionListener.onActionMove(x,y);  //回调给activity处理
                movingDestX = (int) x;
                movingDestY = (int) y;
                if( (event.getEventTime()-downTime) > 500){
                    Log.v("boardview","触发长按事件");
                    actionListener.onLongClick(movingPiece);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                destLocation = new Location((int)x,(int)y);
                //if (movingPiece == null || destLocation == null) { return true; }
                if( (event.getEventTime()-downTime) > 500){
                    Log.v("boardview","触发长按事件");
                    actionListener.onLongClick(movingPiece);
                    movingPiece = null;
                    return true;
                }
                if(mode == MODE_MANUAL) {
                    direction = getMovingDirection(pressedLocation, destLocation);
                    actionListener.onActionUp(direction, x, y); //回调给activity去处理
                }else if(mode == MODE_ADDBOARD){
                    if(movingPiece == null) { return true; }
                    movingPieceTo(movingPiece,(int)x,(int)y);
                }
                movingPiece = null;
                invalidate();
                Log.v("view",String.format("目标移动到%d,%d",destLocation.x,destLocation.y));
                break;
        }
        return true;
    }

    public Piece getPiece(float x, float y){
        //根据指定坐标获取棋子
        int piece_x;
        int piece_y;
        piece_x = (int) (x-frameWidth)/sizeOfUnit;
        piece_y = (int) (y-frameWidth)/sizeOfUnit;
        piece_y = maxY-1 - piece_y; //转换坐标
        if(piece_x >= maxX || piece_x < 0 || piece_y >= maxY || piece_y < 0){
            return null;
        }
        Log.v("boardview",String.format("piece:x=%d,y=%d",piece_x,piece_y));
        return board.pieces[piece_x][piece_y];
    }
    public Location getBoardLocation(float x,float y){
        //转换android坐标x,y到棋盘位置的x,y
        int piece_x;
        int piece_y;
        piece_x = (int) (x-frameWidth)/sizeOfUnit;
        piece_y = (int) (y-frameWidth)/sizeOfUnit;
        piece_y = board.maxY-1 - piece_y; //转换坐标
        if(piece_x >= board.maxX || piece_x < 0 || piece_y >= board.maxY || piece_y < 0){
            return null;
        }
        return new Location(piece_x,piece_y);
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
                return Piece.DIRECTION_RIGHT;
            }else{
                return Piece.DIRECTION_LEFT;
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

    public void setMode(int mode){
        this.mode = mode;
        switch (mode){
            case MODE_HELP:
                break;
            case MODE_MANUAL:
                break;
            case MODE_ADDBOARD:
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
   public void movingPieceTo(Piece piece,int x,int y){
       //给addboard布局group调用

       //首先判断x,y是否超出board边界
       if( x <= frameWidth || x >= maxX*sizeOfUnit+frameWidth || y <= frameWidth || y >= maxY*sizeOfUnit+frameWidth ){
           if (piece.x != -1){
               //非新增棋子，从棋盘移除边界，也即删除该棋子
               board.delPiece(piece);
           }
           return;
       }

       //x,y转换到piece_x,piece_y
       int piece_x = (x-frameWidth)/sizeOfUnit;
       int piece_y = (maxY-1) - (y-frameWidth)/sizeOfUnit;
       Log.v("addboardview",String.format("move %s to %d,%d",piece.name,piece_x,piece_y));
       switch(piece.type){
           case Piece.PIECE_CAOCHAO:
               if (board.isOutOfBoard(piece_x,piece_y) || board.isOutOfBoard(piece_x+1,piece_y+1)) {
                   return;
               }
               if (board.isOccupiedByOther(movingPiece,piece_x,piece_y) || board.isOccupiedByOther(movingPiece,piece_x+1,piece_y)
                       || board.isOccupiedByOther(movingPiece,piece_x,piece_y+1) || board.isOccupiedByOther(movingPiece,piece_x+1,piece_y+1)) {
                   return;
               }
               break;
           case Piece.PIECE_VERTICAL:
               for(int i=0; i<piece.length; i++){
                   if (board.isOutOfBoard(piece_x,piece_y+i) || board.isOccupiedByOther(piece,piece_x,piece_y+i)){
                       Log.v("addblock","被占据或者越界");
                       return ;
                   }
               }
               break;
           case Piece.PIECE_HORIZON:
               for(int i=0; i<piece.length; i++){
                   if (board.isOutOfBoard(piece_x+i,piece_y) || board.isOccupiedByOther(piece,piece_x+i,piece_y)){
                       Log.v("addblock","被占据或者越界");
                       return ;
                   }
               }
               break;
           case Piece.PIECE_SOLDIER:
           case Piece.PIECE_BLOCK:
           case Piece.PIECE_BOX:
           case Piece.PIECE_BOY:
           case Piece.PIECE_DEST:
               if (board.isOutOfBoard(piece_x,piece_y)) {
                   return;
               }
               if (board.isOccupiedByOther(piece,piece_x,piece_y)) {
                   return;
               }
               break;
           default:
               Log.v("addboardview","moveto,unknown type");
               return;
       }
       if (piece.x == -1 && piece.y == -1){
           //新棋子
           piece.x = piece_x;
           piece.y = piece_y;
           Log.v("boardview","新增棋子:"+piece.name);
           board.addPiece(piece);
       }else{
           //移动棋子
           if(piece.x == piece_x && piece.y == piece_y){
               //位置没变
               return;
           }
           Log.v("addblock","移动棋子:"+piece.name);
           board.delPiece(piece);
           piece.x = piece_x;
           piece.y = piece_y;
           board.addPiece(piece);
       }
   }
    public interface ActionListener{
        public void onActionDown(Piece piece);     //按下的棋子
        public void onActionMove(float x,float y); //move到位置坐标(android坐标)
        public void onActionUp(int direction,float x,float y); //方向及位置,xy对应坐标
        public void onLongClick(Piece piece);     //长按事件，按下的棋子
    }
    public void setOnActionListener(ActionListener actionListener){
        this.actionListener = actionListener;
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

