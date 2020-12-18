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

import static com.sungold.huarongdao.HuaBoard.MAX_X;
import static com.sungold.huarongdao.HuaBoard.MAX_Y;

public class AddBoardView extends View {
    int frameDivisor = 30; //边框取width/frameDivisor 。例如1/30
    int frameWidth;        //保存系统计算得到的边框宽度
    int sizeOfUnit;        //最小单元格的尺寸，棋盘的尺寸为(4*5)个size;
    int miniSizeOfUnit;    //布局可选棋子的最大尺寸，实际要减去miniFrameWidth
    int miniFrameWidth;
    //int textHeight = 100;

    public HuaBoard board = null;
    private HuaPiece movingPiece = null;
    private int movingDestX = -1,movingDestY = -1; //移动的棋子当前移动位置的坐标

    public AddBoardView(Context context){
        super(context);
        board = new HuaBoard();
    }
    public AddBoardView(Context context, AttributeSet attrs){
        super(context,attrs);
        board = new HuaBoard();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //假设sizeOfUnit = a; frameDivisor = b
        //width = 4*a + 2*frameWidth = 4 *a + 2 * width / b;
        //height = 5*a + 2*width/b + width * 2 / 6
        //height/width = 5/4(1-2/b)+2/n+2/6
        //注:width*2/6是给布局可选4种棋子的高度：4种棋子并排为6格，高度为2格。缩小版，大小以width为基线。
        //  每一个棋子的miniSizeOfUnit = width/6-miniSizeOfUnit/10（边框）

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        float b = (float) frameDivisor;
        double radio = 5.0/4.0*(1.0-2.0/b)+2.0/b+2.0/6.0;

        if(height > width * radio){
            height = (int)(width * radio);
        }else{
            width = (int) (height / radio) ;
        }
        sizeOfUnit = (int)(width*(1-2.0/frameDivisor)) / 4;
        frameWidth = width/frameDivisor;
        miniSizeOfUnit = width/6;
        miniFrameWidth = miniSizeOfUnit/10;
        Log.v("view",String.format("radio=%f,frameWidth=%d,sizeOfUnit=%d",radio,frameWidth,sizeOfUnit));
        Log.v("board",String.format("x=%d;y=%d",width,height));
        Log.v("board",String.format("minisize=%d;miniframe=%d",miniSizeOfUnit,miniFrameWidth));
        setMeasuredDimension(width,height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.v("board",String.format("canvas:x=%d|y=%d",canvas.getWidth(),canvas.getHeight()));
        //画边框
        drawFrame(canvas);
        //画棋子
        canvas.save();
        canvas.translate(frameWidth,frameWidth);//位移过边框
        drawPieces(canvas);
        canvas.restore();
        //画布局可选的mini棋子
        canvas.save();
        canvas.translate(0,2*frameWidth+5*sizeOfUnit);
        drawMiniPieces(canvas);
        canvas.restore();
        //画正在移动的棋子的轨迹
        if (movingPiece != null){
            drawMovingPiece(canvas);
        }
    }
    public void drawFrame(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(Color.LTGRAY);
        //上框条
        canvas.drawRect(0,0,canvas.getWidth(),frameWidth,paint);
        //下框条
        canvas.drawRect(0,frameWidth+5*sizeOfUnit,canvas.getWidth(),2*frameWidth+5*sizeOfUnit,paint);
        //左框条
        canvas.drawRect(0,0,frameWidth, 2*frameWidth+5*sizeOfUnit,paint);
        //右框条
        canvas.drawRect(frameWidth+4*sizeOfUnit,0,2*frameWidth+4*sizeOfUnit, 2*frameWidth+5*sizeOfUnit,paint);
        //填充空
        Bitmap bitmap = getBitmap(new HuaPiece("空一", HuaPiece.PIECE_SOLDIER,0,0));
        Rect destRect = new Rect(frameWidth,frameWidth,4*sizeOfUnit+frameWidth,5*sizeOfUnit+frameWidth);
        canvas.drawBitmap(bitmap,null,destRect,paint);
    }
    public void drawPieces(Canvas canvas){
        //画曹操
        if (board.caochao != null) {
            drawPiece(canvas, board.caochao);
        }
        if (board.jiang != null) {
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
        //Log.v("view","draw");
        piece.printPiece();
        if (movingPiece != null && movingPiece.type == piece.type
                && movingPiece.x == piece.x && movingPiece.y == piece.y){
            //要画的棋子和正在移动的棋子相同，那么就不画
            return;
        }
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
                //bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.space)).getBitmap();
            default:
                Log.v("boardview","error type");
        }
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        //canvas.drawRect(left,top,right,bottom,paint);
        //Log.v("view",String.format("%f,%f,%f,%f",left,top,right,bottom));
        canvas.drawRect(left,top,right,bottom,paint);
        //填充图片
        Rect destRect = new Rect((int)left+5,(int)top+5,(int)right-5,(int)bottom-5);
        canvas.drawBitmap(getBitmap(piece),null,destRect,paint);
    }
    public void drawMovingPiece(Canvas canvas){
        if (movingPiece == null){
            return;
        }
        float left=0,top=0,right=0,bottom=0;
        left = movingDestX;
        bottom = movingDestY;
        switch (movingPiece.type){
            case HuaPiece.PIECE_CAOCHAO:
                right = left + 2*sizeOfUnit;
                top = bottom - 2*sizeOfUnit;
                break;
            case HuaPiece.PIECE_VERTICAL:
                right = left + sizeOfUnit;
                top = bottom - 2*sizeOfUnit;
                break;
            case HuaPiece.PIECE_HORIZON:
                right = left + 2*sizeOfUnit;
                top = bottom - sizeOfUnit;
                break;
            case HuaPiece.PIECE_SOLDIER:
                right = left + sizeOfUnit;
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
    public void drawMiniPieces(Canvas canvas){
        float left,top,right,bottom;
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        Rect destRect;
        Bitmap bitmap;

        //画曹操
        left = miniFrameWidth;
        top = miniFrameWidth;
        right = 2*miniSizeOfUnit-miniFrameWidth;
        bottom = 2*miniSizeOfUnit-miniFrameWidth;
        canvas.drawRect(left,top,right,bottom,paint);
        //填充图片
        destRect = new Rect((int)left+3,(int)top+3,(int)right-3,(int)bottom-3);
        bitmap = getBitmap(new HuaPiece("曹操", HuaPiece.PIECE_CAOCHAO,0,0));
        canvas.drawBitmap(bitmap,null,destRect,paint);

        //画将
        left = 2*miniSizeOfUnit+miniFrameWidth;
        top = miniFrameWidth;
        right = 3*miniSizeOfUnit-miniFrameWidth;
        bottom = 2*miniSizeOfUnit-miniFrameWidth;
        canvas.drawRect(left,top,right,bottom,paint);
        //填充图片
        destRect = new Rect((int)left+3,(int)top+3,(int)right-3,(int)bottom-3);
        bitmap = getBitmap(new HuaPiece("将一", HuaPiece.PIECE_VERTICAL,0,0));
        canvas.drawBitmap(bitmap,null,destRect,paint);

        //画帅
        left = 3*miniSizeOfUnit+miniFrameWidth;
        top = miniSizeOfUnit+miniFrameWidth;
        right = 5*miniSizeOfUnit-miniFrameWidth;
        bottom = 2*miniSizeOfUnit-miniFrameWidth;
        canvas.drawRect(left,top,right,bottom,paint);
        //填充图片
        destRect = new Rect((int)left+3,(int)top+3,(int)right-3,(int)bottom-3);
        bitmap = getBitmap(new HuaPiece("帅一", HuaPiece.PIECE_HORIZON,0,0));
        canvas.drawBitmap(bitmap,null,destRect,paint);

        //画兵
        left = 5*miniSizeOfUnit+miniFrameWidth;
        top = miniSizeOfUnit+miniFrameWidth;
        right = 6*miniSizeOfUnit-miniFrameWidth;
        bottom = 2*miniSizeOfUnit-miniFrameWidth;
        canvas.drawRect(left,top,right,bottom,paint);
        //填充图片
        destRect = new Rect((int)left+3,(int)top+3,(int)right-3,(int)bottom-3);
        bitmap = getBitmap(new HuaPiece("兵一", HuaPiece.PIECE_SOLDIER,0,0));
        canvas.drawBitmap(bitmap,null,destRect,paint);
    }
    public void movingPieceTo(int x,int y){
        //首先判断x,y是否超出board边界
        if( x <= frameWidth || x >= 4*sizeOfUnit+frameWidth || y <= frameWidth || y >= 5*sizeOfUnit+frameWidth ){
            if (movingPiece.x != -1){
                //非新增棋子，从棋盘移除边界，也即删除该棋子
                board.delPiece(movingPiece);
            }
            return;
        }
        //x,y转换到piece_x,piece_y
        int piece_x = x/sizeOfUnit;
        int piece_y = (MAX_Y-1) - y/sizeOfUnit;
        Log.v("addboardview",String.format("move %s to %d,%d",movingPiece.name,piece_x,piece_y));
        switch(movingPiece.type){
            case HuaPiece.PIECE_CAOCHAO:
                if (board.isOutOfBoard(piece_x,piece_y) || board.isOutOfBoard(piece_x+1,piece_y+1)) {
                    return;
                }
                if (board.isOccupiedByOther(movingPiece,piece_x,piece_y) || board.isOccupiedByOther(movingPiece,piece_x+1,piece_y)
                    || board.isOccupiedByOther(movingPiece,piece_x,piece_y+1) || board.isOccupiedByOther(movingPiece,piece_x+1,piece_y+1)) {
                    return;
                }
                break;
            case HuaPiece.PIECE_VERTICAL:
                if (board.isOutOfBoard(piece_x,piece_y) || board.isOutOfBoard(piece_x,piece_y+1)) {
                    return;
                }
                if (board.isOccupiedByOther(movingPiece,piece_x,piece_y) || board.isOccupiedByOther(movingPiece,piece_x,piece_y+1)) {
                    return;
                }
                break;
            case HuaPiece.PIECE_HORIZON:
                if (board.isOutOfBoard(piece_x,piece_y) || board.isOutOfBoard(piece_x+1,piece_y)) {
                    return;
                }
                if (board.isOccupiedByOther(movingPiece,piece_x,piece_y) || board.isOccupiedByOther(movingPiece,piece_x+1,piece_y)) {
                    return;
                }
                break;
            case HuaPiece.PIECE_SOLDIER:
                if (board.isOutOfBoard(piece_x,piece_y)) {
                    return;
                }
                if (board.isOccupiedByOther(movingPiece,piece_x,piece_y)) {
                    return;
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
            board.addPiece(movingPiece);
        }else{
            //移动棋子
            if(movingPiece.x == piece_x && movingPiece.y == piece_y){
                //位置没变
                return;
            }
            board.delPiece(movingPiece);
            movingPiece.x = piece_x;
            movingPiece.y = piece_y;
            board.addPiece(movingPiece);
        }
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                movingPiece = getPiece(x,y);
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
                //Log.v("view",String.format("目标移动到%s",destPiece.name));
                break;
        }
        return true;
    }

    public HuaPiece getPiece(float x, float y){
        String name;
        //根据指定坐标获取棋子,两种情况，一种在棋盘内，一种棋盘外
        if(x <= 4*sizeOfUnit+2*frameWidth && y <= 5*sizeOfUnit+2*frameWidth) { //棋盘内
            int piece_x;
            int piece_y;
            piece_x = (int) (x - frameWidth) / sizeOfUnit;
            piece_y = (int) (y - frameWidth) / sizeOfUnit;
            piece_y = MAX_Y - 1 - piece_y; //转换坐标
            if (piece_x >= MAX_X || piece_x < 0 || piece_y >= MAX_Y || piece_y < 0) {
                return null;
            }
            return board.pieces[piece_x][piece_y];
        }else{
            y -= 5*sizeOfUnit+2*frameWidth;
            //曹操
            if(x >= miniFrameWidth && x <= 2*miniSizeOfUnit-miniFrameWidth
                && y >= miniFrameWidth && y <= 2*miniSizeOfUnit-miniFrameWidth){
                if (board.caochao == null) {
                    return new HuaPiece("曹操", HuaPiece.PIECE_CAOCHAO, -1, -1);
                }else{
                    return null;  //不允许出现多个曹操;
                }
            }else if(x >= 2*miniSizeOfUnit+miniFrameWidth && x <= 3*miniSizeOfUnit-miniFrameWidth
                    && y >= miniFrameWidth && y <= 2*miniSizeOfUnit-miniFrameWidth ){
                name = getName(HuaPiece.PIECE_VERTICAL);
                return new HuaPiece(name, HuaPiece.PIECE_VERTICAL,-1,-1);
            }else if(x >= 3*miniSizeOfUnit+miniFrameWidth && x <= 5*miniSizeOfUnit-miniFrameWidth
                   && y >= miniSizeOfUnit+miniFrameWidth && y <= 2*miniSizeOfUnit-miniFrameWidth){
                name = getName(HuaPiece.PIECE_HORIZON);
                return new HuaPiece(name, HuaPiece.PIECE_HORIZON,-1,-1);
            }else if(x >= 5*miniSizeOfUnit+miniFrameWidth && x <= 6*miniSizeOfUnit-miniFrameWidth
                && y >= miniSizeOfUnit+miniFrameWidth && y <= 2*miniSizeOfUnit-miniFrameWidth){
                name = getName(HuaPiece.PIECE_SOLDIER);
                return new HuaPiece(name, HuaPiece.PIECE_SOLDIER,-1,-1);
            }
            return null;
        }
    }
    public Boolean saveBoard(String name){
        if (name.equals("")) { return false; }
        //检查棋盘布局是否已经完整，如果OK ,就存入BoardView.startBoard;
        int units = 0; //统计棋子占据的格子数
        int x=-1,y=-1;
        Log.v("addboardview","beigin");
        //检查棋子
        if (board.caochao == null) {
            Log.v("addboardview","caochao is null");
            return false;
        }
        x = board.caochao.x; y = board.caochao.y;
        if (board.pieces[x][y] != board.caochao || board.pieces[x][y+1] != board.caochao
            || board.pieces[x+1][y] != board.caochao || board.pieces[x+1][y+1] != board.caochao){
            Log.v("addboardview","check caochao failed");
            return false;
        }
        units += 4;
        if (board.jiang != null) {
            for (int i = 0; i < board.jiang.length; i++){
                if (board.jiang[i] == null) { return  false; }
                x = board.jiang[i].x ; y = board.jiang[i].y;
                if( board.pieces[x][y] != board.jiang[i] || board.pieces[x][y+1] != board.jiang[i]){
                    Log.v("addboardview","check jiang failed");
                    return false;
                }
                units += 2;
            }
        }
        if (board.shuai != null) {
            for (int i = 0; i < board.shuai.length; i++){
                if (board.shuai[i] == null) { return  false; }
                x = board.shuai[i].x ; y = board.shuai[i].y;
                if( board.pieces[x][y] != board.shuai[i] || board.pieces[x+1][y] != board.shuai[i]){
                    Log.v("addboardview","check shuai failed");
                    return false;
                }
                units += 2;
            }
        }
        if (board.bing != null) {
            for (int i = 0; i < board.bing.length; i++){
                if (board.bing[i] == null) { return  false; }
                x = board.bing[i].x ; y = board.bing[i].y;
                if( board.pieces[x][y] != board.bing[i] ){
                    Log.v("addboardview","check bing failed");
                    return false;
                }
                units += 1;
            }        }
        Log.v("addBoardview",String.format("units=%d",units));
        //if ( units+2 != MAX_X*MAX_Y) { return false; } //限制只能有两个空格
        int numberOfSpace = MAX_X*MAX_Y - units;

        //自动生成两个空格
        board.space = new HuaPiece[numberOfSpace];
        int indexOfSpace = 0;

        for (x=0; x<MAX_X; x++){
            for(y=0; y<MAX_Y; y++){
                if (board.pieces[x][y] == null){
                    Log.v("addBoardview",String.format("index=%d",indexOfSpace));
                    //if (indexOfSpace >= 2) { return false; }
                    board.space[indexOfSpace] = new HuaPiece("空一", HuaPiece.PIECE_EMPTY,x,y);
                    board.pieces[x][y] = board.space[indexOfSpace];
                    indexOfSpace += 1;
                }
            }
        }
        //因保存数据库中的格式为pieces二维数组的棋子名（如将将曹曹xxxx），丢失了序号，但是解题答案中是有序号的。
        //为了便于解题答案和棋盘一致。需要生成数据库中的字符串，然后自动重新生成新的棋盘(自动生成棋子的序号，例如将一，将二)
        String string = board.piecesToString();
        Log.v("addboard",string);
        board = HuaBoard.fromPiecesString(string);

        board.name = name;
        //BoardView.startBoard = board;
        //BoardView.startBoard.name = name;
        return true;
    }

    public String getName(int type){
        switch (type){
            case HuaPiece.PIECE_CAOCHAO:
                return "曹操";
            case HuaPiece.PIECE_VERTICAL:
                if (board.jiang == null){
                    return "将一";
                }else if(board.jiang.length == 1){
                    return "将二";
                }else if(board.jiang.length == 2){
                    return "将三";
                }else if(board.jiang.length == 3){
                    return "将四";
                }else{
                    return "将五";
                }
            case HuaPiece.PIECE_HORIZON:
                if (board.shuai == null){
                    return "帅一";
                }else if(board.shuai.length == 1){
                    return "帅二";
                }else if(board.shuai.length == 2){
                    return "帅三";
                }else if(board.shuai.length == 3){
                    return "帅四";
                }else{
                    return "帅五";
                }
            case HuaPiece.PIECE_SOLDIER:
                if (board.bing == null){
                    return "兵一";
                }else if(board.bing.length == 1){
                    return "兵二";
                }else if(board.bing.length == 2){
                    return "兵三";
                }else if(board.bing.length == 3){
                    return "兵四";
                }else if(board.bing.length == 4){
                    return "兵五";
                }else if(board.bing.length == 5){
                    return "兵六";
                }else if(board.bing.length == 6){
                    return "兵七";
                }else if(board.bing.length == 7){
                    return "兵八";
                }else{
                    return "兵";
                }
            case HuaPiece.PIECE_EMPTY:
                if (board.space == null){
                    return "空一";
                }else if(board.space.length == 1){
                    return "空二";
                }else if(board.space.length == 2){
                    return "空三";
                }else if(board.space.length == 3){
                    return "空四";
                }else{
                    return "空";
                }
        }
        return "";
    }
}
