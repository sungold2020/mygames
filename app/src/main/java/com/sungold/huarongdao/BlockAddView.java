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

public class BlockAddView extends View {
    //以下数据在创建前在activity指定
    public static int maxX = 6, maxY = 6;
    public static int kingType = BlockPiece.PIECE_HORIZON, kingLength = 2;
    public static int exit = 6;
    public static String name="test";

    //棋盘区域
    int frameDivisor = 20; //边框取width/frameDivisor 。例如1/30
    int frameWidth;        //保存系统计算得到的边框宽度
    int sizeOfUnit;        //最小单元格的尺寸，棋盘的尺寸为(4*5)个size;

    //布局棋子区域
    final int kingAreaDivisor = 8;  // 1/8 棋盘高度
    int kingAreaHeight ;            //king区域的高度
    BlockRect kingBlockRect;        //king的区域坐标
    final int verticalDivisor = 4; // 1/4 高度
    BlockRect shortVerticalRect,mediumVerticalRect,longVerticalRect; //短，中，长的三个竖条的区域坐标
    int verticalHeight ;
    final int horizonDivisor = 8;
    int horizonHeight;
    BlockRect shortHorizonRect,mediumHorizonRect,longHorizonRect;

    public BlockBoard board = null;
    private BlockPiece movingPiece = null;
    private int movingDestX = -1,movingDestY = -1; //移动的棋子当前移动位置的坐标

    public BlockAddView(Context context){
        super(context);
        board = new BlockBoard(maxX,maxY);
        board.name = name;
        board.destPointer = exit;
    }
    public BlockAddView(Context context, AttributeSet attrs){
        super(context,attrs);
        board = new BlockBoard(maxX,maxY);
        board.name = name;
        board.destPointer = exit;
    }
    private float getDivisor(){
        float fenzi = kingAreaDivisor * verticalDivisor * horizonDivisor;
        float fenmu = fenzi - kingAreaDivisor * verticalDivisor - kingAreaDivisor * horizonDivisor - verticalDivisor * horizonDivisor;
        return fenzi / fenmu;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        Log.v("addblock",String.format("x=%d,y=%d",width,height));
        //以宽度来计算sizeOfUnit的大小
        int sizeOfUnitByWidth = (width - 2 * width/frameDivisor) / maxX;
        int sizeOfUnitByHeight = (height - height/kingAreaDivisor - height/verticalDivisor - height/horizonDivisor -2 * width/frameDivisor) / maxY;
        if (sizeOfUnitByWidth < sizeOfUnitByHeight){
            //取小的
            sizeOfUnit = sizeOfUnitByWidth;
            frameWidth = width / frameDivisor;

        }else{
            sizeOfUnit = sizeOfUnitByHeight;
            width =  (sizeOfUnit * maxX) * frameDivisor / (frameDivisor - 2);
            frameWidth = width/frameDivisor;
            width = sizeOfUnit * maxX + 2 * frameWidth;
        }
        height = (int)((sizeOfUnit * maxY + 2 * frameWidth)  * getDivisor());
        kingAreaHeight = height / kingAreaDivisor;
        verticalHeight = height / verticalDivisor;
        horizonHeight = height / horizonDivisor;
        height = sizeOfUnit * maxY + 2 * frameWidth + kingAreaHeight + verticalHeight + horizonHeight; //再算一次

        //计算布局棋子的区域坐标
        int left,right,bottom,top,sizeofMiniUnit;
        int translateHeight = 2 * frameWidth + maxY * sizeOfUnit; //高度位移量，从棋盘高度开始
        //king
        if (kingType == BlockPiece.PIECE_HORIZON){
            //取2/3高度，上下留1/6高度,宽度左边预留framewidth
            sizeofMiniUnit = kingAreaHeight * 2 / 3;
            left = frameWidth;
            top = kingAreaHeight/6 + translateHeight;
            right = (kingLength * sizeofMiniUnit) + left;
            bottom = top + sizeofMiniUnit;
        }else{
            //高度取kingheight，上下不留边
            sizeofMiniUnit = kingAreaHeight / kingLength;
            left = frameWidth;
            top = translateHeight;
            right = left + sizeofMiniUnit;
            bottom = translateHeight + kingAreaHeight;
        }
        kingBlockRect = new BlockRect(left,top,right,bottom);

        translateHeight += kingAreaHeight;
        //横条，按照宽度计算：左右各留frameWidth，三个横块中间固定留frameWidth，按照高度计算取height的2/3
        int sizeofMiniUnitByWidth = (width - 4 * frameWidth)/6;
        int sizeOfMiniUnitByHeight = horizonHeight  * 2 / 3;
        sizeofMiniUnit = Math.min(sizeOfMiniUnitByHeight,sizeofMiniUnitByWidth);
        shortHorizonRect = new BlockRect(frameWidth,translateHeight + horizonHeight/6,frameWidth+sizeofMiniUnit,translateHeight + horizonHeight/6 + sizeofMiniUnit);
        mediumHorizonRect = new BlockRect(2*frameWidth+sizeofMiniUnit,translateHeight + horizonHeight/6,
                2*frameWidth+3*sizeofMiniUnit,translateHeight + horizonHeight/6 + sizeofMiniUnit);
        longHorizonRect = new BlockRect(3*frameWidth+3*sizeofMiniUnit,translateHeight + horizonHeight/6 ,
                3*frameWidth+6*sizeofMiniUnit,translateHeight + horizonHeight/6 + sizeofMiniUnit);
        //竖条，高度取全部
        translateHeight += horizonHeight;
        sizeofMiniUnit = verticalHeight / 3;
        shortVerticalRect = new BlockRect(frameWidth,sizeofMiniUnit+translateHeight,frameWidth+sizeofMiniUnit,2*sizeofMiniUnit+translateHeight);
        mediumVerticalRect = new BlockRect(2*frameWidth+sizeofMiniUnit,sizeofMiniUnit/2+translateHeight,
                2*frameWidth+2*sizeofMiniUnit,sizeofMiniUnit*5/2+translateHeight);
        longVerticalRect = new BlockRect(3*frameWidth+2*sizeofMiniUnit,translateHeight,
                3*frameWidth+3*sizeofMiniUnit,translateHeight+verticalHeight);

        Log.v("view",String.format("frameWidth=%d,sizeOfUnit=%d",frameWidth,sizeOfUnit));
        Log.v("board",String.format("x=%d;y=%d",width,height));
        setMeasuredDimension(width,height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.v("board",String.format("canvas:x=%d|y=%d",canvas.getWidth(),canvas.getHeight()));
        //画边框
        drawFrame(canvas);
        //画出口
        if (board.kingBlock != null) {        drawExit(canvas); }
        //画棋子
        canvas.save();
        canvas.translate(frameWidth,frameWidth);//位移过边框
        drawPieces(canvas);
        canvas.restore();
        //画布局可选的mini棋子
        //canvas.save();
        //canvas.translate(0,2*frameWidth+maxY*sizeOfUnit);
        drawMiniPieces(canvas);
       // canvas.restore();
        //画正在移动的棋子的轨迹
        if (movingPiece != null){
            drawMovingPiece(canvas);
        }
    }
    public void drawFrame(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(Color.DKGRAY);
        //设置背景
        canvas.drawRect(0,0,canvas.getWidth(),2*frameWidth+maxY*sizeOfUnit,paint);

        paint.setColor(Color.LTGRAY);
        //上框条
        canvas.drawRect(0,0,canvas.getWidth(),frameWidth,paint);
        //下框条
        canvas.drawRect(0,frameWidth+maxY*sizeOfUnit,canvas.getWidth(),2*frameWidth+maxY*sizeOfUnit,paint);
        //左框条
        canvas.drawRect(0,0,frameWidth, 2*frameWidth+maxY*sizeOfUnit,paint);
        //右框条
        canvas.drawRect(frameWidth+maxX*sizeOfUnit,0,2*frameWidth+maxX*sizeOfUnit, 2*frameWidth+maxY*sizeOfUnit,paint);
    }
    public void drawExit(Canvas canvas){
        //画出口
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        float left,right,bottom,top;
        if (board.kingBlock.type == BlockPiece.PIECE_VERTICAL){
            left = frameWidth + board.kingBlock.x * sizeOfUnit;
            right = left + sizeOfUnit;
            if (board.destPointer > 0){
                //出口在顶部
                top = 0;
            }else{
                top = frameWidth + maxY * sizeOfUnit;
            }
            bottom = frameWidth;
        }else{
            bottom = frameWidth + (maxY - board.kingBlock.y) * sizeOfUnit;
            top = bottom - sizeOfUnit;
            if (board.destPointer > 0){
                //在右边
                left = frameWidth + maxX * sizeOfUnit;
            }else{
                left = 0;
            }
            right = left + frameWidth;
        }
        canvas.drawRect(left,top,right,bottom,paint);
    }
    public void drawPieces(Canvas canvas){
        //画曹操
        if (board.kingBlock != null) {
            drawPiece(canvas, board.kingBlock);
        }
        if (board.verticalBlocks != null) {
            for (int i = 0; i < board.verticalBlocks.length; i++) {
                drawPiece(canvas, board.verticalBlocks[i]);
            }
        }
        if (board.horizonBlocks != null){
            for(int i=0; i<board.horizonBlocks.length; i++){
                drawPiece(canvas,board.horizonBlocks[i]);
            }
        }
    }
    public void drawPiece(Canvas canvas,BlockPiece block){
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
    public void drawMovingPiece(Canvas canvas){
        if (movingPiece == null){
            return;
        }
        float left=0,top=0,right=0,bottom=0;
        left = movingDestX;
        bottom = movingDestY;
        switch (movingPiece.type){
            case BlockPiece.PIECE_VERTICAL:
                right = left + sizeOfUnit;
                top = bottom - movingPiece.length*sizeOfUnit;
                break;
            case BlockPiece.PIECE_HORIZON:
                right = left + movingPiece.length*sizeOfUnit;
                top = bottom - sizeOfUnit;
                break;
            default:
                Log.v("addboardview","unknown tyoe");
        }
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        //canvas.drawRect(left,top,right,bottom,paint);
        Log.v("view",String.format("%f,%f,%f,%f",left,top,right,bottom));
        canvas.drawRect(left,top,right,bottom,paint);
        //填充图片
        Rect destRect = new Rect((int)left+5,(int)top+5,(int)right-5,(int)bottom-5);
        Bitmap bitmap = getBitmap(movingPiece);
        if(bitmap == null){
            Log.v("addboardview","bitmap is null");
        }
        canvas.drawBitmap(bitmap,null,destRect,paint);
    }
    public void drawMiniPieces(Canvas canvas) {
        Bitmap bitmap;
        //king
        bitmap = getBitmap(new BlockPiece("王",kingType,kingLength,-1,-1));
        drawMiniPiece(canvas,bitmap,kingBlockRect);
        //横
        bitmap = getBitmap(new BlockPiece("横",BlockPiece.PIECE_HORIZON,0,-1,-1));
        drawMiniPiece(canvas,bitmap,shortHorizonRect);
        drawMiniPiece(canvas,bitmap,mediumHorizonRect);
        drawMiniPiece(canvas,bitmap,longHorizonRect);
        //竖
        bitmap = getBitmap(new BlockPiece("竖",BlockPiece.PIECE_VERTICAL,0,-1,-1));
        drawMiniPiece(canvas,bitmap,shortVerticalRect);
        drawMiniPiece(canvas,bitmap,mediumVerticalRect);
        drawMiniPiece(canvas,bitmap,longVerticalRect);
    }
    public void drawMiniPiece(Canvas canvas,Bitmap bitmap,BlockRect blockRect) {

        float left,top,right,bottom;
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        Rect destRect;

        //画背景,充当边框
        canvas.drawRect(blockRect.left,blockRect.top,blockRect.right,blockRect.bottom,paint);
        //填充图片，留一点边不填图片就成为边框
        destRect = new Rect((int)blockRect.left+3,(int)blockRect.top+3,(int)blockRect.right-3,(int)blockRect.bottom-3);
        canvas.drawBitmap(bitmap,null,destRect,paint);
    }
    public void movingPieceTo(int x,int y){
        //首先判断x,y是否超出board边界
        if( x <= frameWidth || x >= maxX*sizeOfUnit+frameWidth || y <= frameWidth || y >= maxY*sizeOfUnit+frameWidth ){
            if (movingPiece.x != -1){
                //非新增棋子，从棋盘移除边界，也即删除该棋子
                board.delBlockPiece(movingPiece);
            }
            return;
        }

        //x,y转换到piece_x,piece_y
        int piece_x = (x-frameWidth)/sizeOfUnit;
        int piece_y = (maxY-1) - (y-frameWidth)/sizeOfUnit;
        Log.v("addboardview",String.format("move %s to %d,%d",movingPiece.name,piece_x,piece_y));
        switch(movingPiece.type){
            case BlockPiece.PIECE_VERTICAL:
                for(int i=0; i<movingPiece.length; i++){
                    if (board.isOutOfBoard(piece_x,piece_y+i) || board.isOccupiedByOther(movingPiece,piece_x,piece_y+i)){
                        Log.v("addblock","被占据或者越界");
                        return ;
                    }
                }
                break;
            case BlockPiece.PIECE_HORIZON:
                for(int i=0; i<movingPiece.length; i++){
                    if (board.isOutOfBoard(piece_x+i,piece_y) || board.isOccupiedByOther(movingPiece,piece_x+i,piece_y)){
                        Log.v("addblock","被占据或者越界");
                        return ;
                    }
                }
                break;
            default:
                Log.v("addboardview","moveto,unknown type");
                return;
        }
        if (movingPiece.x == -1 && movingPiece.y == -1){
            //新棋子
            movingPiece.x = piece_x;
            movingPiece.y = piece_y;
            Log.v("addblock","新增棋子:"+movingPiece.name);
            board.addBlockPiece(movingPiece);
        }else{
            //移动棋子
            if(movingPiece.x == piece_x && movingPiece.y == piece_y){
                //位置没变
                return;
            }
            Log.v("addblock","移动棋子:"+movingPiece.name);
            board.delBlockPiece(movingPiece);
            movingPiece.x = piece_x;
            movingPiece.y = piece_y;
            board.addBlockPiece(movingPiece);
        }
    }
    private Bitmap getBitmap(BlockPiece block){
        if (block.name.equals("王")) {
            return ((BitmapDrawable) getResources().getDrawable(R.drawable.block_king)).getBitmap();
        }
        return ((BitmapDrawable) getResources().getDrawable(R.drawable.block_block)).getBitmap();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                movingPiece = getBlockPiece(x,y);
                if (movingPiece == null){
                    return false;
                }
                Log.v("view",String.format("你按下了%s",movingPiece.name));

                break;
            case MotionEvent.ACTION_MOVE:
                movingDestX = (int)x;
                movingDestY = (int)y;
                invalidate();
                //Log.v("view",String.format("move %s",destPiece.name));
                //重新绘制
                break;
            case MotionEvent.ACTION_UP:
                movingPieceTo((int)x,(int)y);
                movingPiece = null;
                invalidate();
                //Log.v("view",String.format("目标移动到%s",destBlockPiece.name));
                break;
        }
        return true;
    }

    public BlockPiece getBlockPiece(float x,float y){
        String name;
        //根据指定坐标获取棋子,两种情况，一种在棋盘内，一种棋盘外
        if(x <= maxX*sizeOfUnit+2*frameWidth && y <= maxY*sizeOfUnit+2*frameWidth) { //棋盘内
            int piece_x;
            int piece_y;
            piece_x = (int) (x - frameWidth) / sizeOfUnit;
            piece_y = (int) (y - frameWidth) / sizeOfUnit;
            piece_y = maxY  - 1 - piece_y; //转换坐标
            Log.v("board",String.format("转换后的坐标(%d,%d)",piece_x,piece_y));
            if (piece_x >= maxX || piece_x < 0 || piece_y >= maxY || piece_y < 0) {
                return null;
            }
            return board.blocks[piece_x][piece_y];
        }else{
            //king
            if(kingBlockRect.isInRect((int)x,(int)y)){
                if (board.kingBlock == null) {
                    return new BlockPiece("王", kingType, kingLength, -1,-1);
                }else{
                    return null;  //king;
                }
            }else if(shortVerticalRect.isInRect((int)x,(int)y)){
                name = getName(BlockPiece.PIECE_VERTICAL);
                return new BlockPiece(name,BlockPiece.PIECE_VERTICAL,1,-1,-1);
            }else  if(mediumVerticalRect.isInRect((int)x,(int)y)){
                name = getName(BlockPiece.PIECE_VERTICAL);
                return new BlockPiece(name,BlockPiece.PIECE_VERTICAL,2,-1,-1);
            }else  if(longVerticalRect.isInRect((int)x,(int)y)){
                name = getName(BlockPiece.PIECE_VERTICAL);
                return new BlockPiece(name,BlockPiece.PIECE_VERTICAL,3,-1,-1);
            }else  if(shortHorizonRect.isInRect((int)x,(int)y)) {
                name = getName(BlockPiece.PIECE_HORIZON);
                return new BlockPiece(name, BlockPiece.PIECE_HORIZON, 1, -1, -1);
            }else  if(mediumHorizonRect.isInRect((int)x,(int)y)) {
                name = getName(BlockPiece.PIECE_HORIZON);
                return new BlockPiece(name, BlockPiece.PIECE_HORIZON, 2, -1, -1);
            }else  if(longHorizonRect.isInRect((int)x,(int)y)) {
                name = getName(BlockPiece.PIECE_HORIZON);
                return new BlockPiece(name, BlockPiece.PIECE_HORIZON, 3, -1, -1);
            }
            return null;
        }
    }
    public Boolean saveBoard(){
        //检查棋盘布局是否已经完整，如果OK ,就存入BoardView.startBoard;
        int units = 0; //统计棋子占据的格子数
        int x=-1,y=-1;
        Log.v("addboardview","beigin");
        //检查棋子
        if (board.kingBlock == null) {
            Log.v("addboardview","caochao is null");
            return false;
        }
        //BlockBoardView.startBlockBoard = board;
        return true;
    }

    public String getName(int type){
        switch (type){
            case BlockPiece.PIECE_VERTICAL:
                if (board.verticalBlocks == null){
                    return "竖"+ HuaPiece.numberToChinese(1);
                }else{
                    return "竖"+ HuaPiece.numberToChinese(board.verticalBlocks.length+1);
                }
            case BlockPiece.PIECE_HORIZON:
                if (board.horizonBlocks == null){
                    return "横"+ HuaPiece.numberToChinese(1);
                }else{
                    return "横"+ HuaPiece.numberToChinese(board.horizonBlocks.length+1);
                }
        }
        return "未知";
    }
    //布局棋子的区域，对应androdi坐标
    class BlockRect{
        int left,right,top,bottom;
        BlockRect(int left,int top,int right,int bottom){
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            Log.v("addblock",String.format("rect:left=%d,right=%d,top=%d,bottom=%d",left,right,top,bottom));
        }
        public Boolean isInRect(int x,int y){
            if (x> left && x < right && y > top && y < bottom){
                return true;
            }
            return false;
        }
    }
}
