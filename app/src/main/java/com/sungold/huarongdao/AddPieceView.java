package com.sungold.huarongdao;

import android.content.Context;
import android.content.res.TypedArray;
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

public class AddPieceView extends View {
    GameType gameType = null;
    int miniSizeOfUnit;    //布局可选棋子的最大尺寸，实际要减去miniFrameWidth
    int miniFrameWidth,miniFrameHeight;

    private PieceRect[] pieces = null;
    private PieceRect movingPiece = null;
    private int movingDestX = -1,movingDestY = -1; //移动的棋子当前移动位置的坐标

    ActionListener actionListener;
    public AddPieceView(Context context){
        super(context);
    }
    public AddPieceView(Context context, AttributeSet attrs){
        super(context,attrs);
        //解析xml中的自定义attr
        /*TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.AddPieceView);
        for(int i=0; i<typedArray.getIndexCount(); i++){
            switch (typedArray.getIndex(i)){
                case R.styleable.AddPieceView_gameType:
                    gameType = GameType.toEnum(typedArray.getInt(R.styleable.AddPieceView_gameType,-1));
                    break;
                default:
                    Log.v("addpiece","Unexpected value: " + typedArray.getIndex(i));
            }
        }*/
    }
    public void setGameType(GameType gameType){
        this.gameType = gameType;
        invalidate();
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //假设sizeOfUnit = a; frameDivisor = b
        //width = 4*a + 2*frameWidth = 4 *a + 2 * width / b;
        //height = 5*a + 2*width/b + width * 2 / 6
        //height/width = 5/4(1-2/b)+2/n+2/6
        //注:width*2/6是给布局可选4种棋子的高度：4种棋子并排为6格，高度为2格。缩小版，大小以width为基线。
        //  每一个棋子的miniSizeOfUnit = width/6-miniSizeOfUnit/10（边框）
        if (gameType == null) { return; }
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        Log.v("addpiece",String.format("width=%d,height=%s",width,height));
        switch (gameType){
            case HUARONGDAO:
                onMeasureHua(width,height);
                break;
            case BLOCK:
                onMeasureBlock(width,height);
                break;
            case BOX:
                onMeasureBox(width,height);
                break;
        }
        setMeasuredDimension(width,height);
    }

    private void onMeasureHua(int width,int height){
        //并排caochao,vetial,horizon,solider四种棋子，占用宽度2+2+1+1=6个miniSizeOfUnit
        //上下预留宽度 miniFrameHeight和左右预留宽度miniFrameWidth  取1/4 miniSizeOfUnit计算
        //如果宽度有空余，则重新计算miniFrameHeight

        int unitByWidth =( width*4)/29; // width/(5/4+2+2+1+1);
        int unitByHeight = (height*2)/3; //height/(1+1/2);
        if (unitByWidth > unitByHeight){
            miniSizeOfUnit = unitByHeight;
            miniFrameHeight = miniSizeOfUnit/4;
            miniFrameWidth = (width-6*miniSizeOfUnit)/5;
        }else{
            miniSizeOfUnit = unitByWidth;
            miniFrameWidth = miniSizeOfUnit/4;
            miniFrameHeight = (height-2*miniSizeOfUnit)/2;
        }

        //生成四个棋子
        int left,right,top,bottom;
        Piece piece;
        Rect rect;
        pieces = new PieceRect[4];
        //曹操
        piece = new Piece("曹操",Piece.PIECE_CAOCHAO,2,-1,-1) ;
        left = miniFrameWidth;
        right = left + 2 * miniSizeOfUnit;
        top = miniFrameHeight;
        bottom = top + 2 * miniSizeOfUnit;
        pieces[0] = new PieceRect(piece,new Rect(left,top,right,bottom));
        //将
        piece = new Piece("将",Piece.PIECE_VERTICAL,2,-1,-1);
        left = pieces[0].rect.right + miniFrameWidth;
        right = left + miniSizeOfUnit;
        top = miniFrameHeight;
        bottom = top + 2 * miniSizeOfUnit;
        pieces[1] = new PieceRect(piece,new Rect(left,top,right,bottom));
        //帅
        piece = new Piece("帅",Piece.PIECE_HORIZON,2,-1,-1);
        left = pieces[1].rect.right + miniFrameWidth;
        right = left + 2 * miniSizeOfUnit;
        top = miniFrameHeight + miniSizeOfUnit/2 ;
        bottom = top + miniSizeOfUnit;
        pieces[2] = new PieceRect(piece,new Rect(left,top,right,bottom));
        //兵
        piece = new Piece("兵",Piece.PIECE_SOLDIER,1,-1,-1);
        left = pieces[2].rect.right + miniFrameWidth;
        right = left + miniSizeOfUnit;
        top = miniFrameHeight + miniSizeOfUnit/2 ;
        bottom = top + miniSizeOfUnit;
        pieces[3] = new PieceRect(piece,new Rect(left,top,right,bottom));
    }
    private void onMeasureBlock(int width,int height){
        //  最左边摆放3格竖条，然后上为1格方块，下为2格竖条，最右边上为2格横条，下为3格横条
        //  占用最大格子数为，宽为:1+1+3=5，高为3
        //  frameSize取1/4格,宽需要4个frameWidth也即1个unit,高需要3个frame也即3/4个unit

        int unitByWidth = width/(1+5);
        int unitByHeight = (height*4)/15; // height/(3/4+3)
        if(unitByWidth > unitByHeight){
            miniSizeOfUnit = unitByHeight;
            miniFrameHeight = miniSizeOfUnit/4;
            miniFrameWidth = (width-5*miniSizeOfUnit)/4;
        }else{
            miniSizeOfUnit = unitByWidth;
            miniFrameWidth = miniSizeOfUnit/4;
            miniFrameHeight = (height-3*miniSizeOfUnit)/3;
        }
        int left,right,top,bottom;
        Piece piece;
        pieces = new PieceRect[5];
        //3格长竖条
        piece = new Piece("竖",Piece.PIECE_VERTICAL,3,-1,-1) ;
        left = miniFrameWidth;
        right = left +  miniSizeOfUnit;
        top = miniFrameHeight;
        bottom = top + 3 * miniSizeOfUnit;
        pieces[0] = new PieceRect(piece,new Rect(left,top,right,bottom));
        //1格方块
        piece = new Piece("竖",Piece.PIECE_VERTICAL,1,-1,-1);
        left = pieces[0].rect.right + miniFrameWidth;
        right = left + miniSizeOfUnit;
        top = miniFrameHeight;
        bottom = top + miniSizeOfUnit;
        pieces[1] = new PieceRect(piece,new Rect(left,top,right,bottom));
        //2格竖条
        piece = new Piece("竖",Piece.PIECE_VERTICAL,2,-1,-1);
        //left = pieces[0].rect.right + miniFrameWidth;  //左右不变
        //right = left + 2 * miniSizeOfUnit;
        top = pieces[1].rect.bottom + miniFrameHeight ;
        bottom = top + 2 * miniSizeOfUnit;
        pieces[2] = new PieceRect(piece,new Rect(left,top,right,bottom));
        //2格横条
        piece = new Piece("横",Piece.PIECE_HORIZON,2,-1,-1);
        left = pieces[2].rect.right + miniFrameWidth;
        right = left + 2 * miniSizeOfUnit;
        top = miniFrameHeight;
        bottom = top + miniSizeOfUnit;
        pieces[3] = new PieceRect(piece,new Rect(left,top,right,bottom));
        //3格横条
        piece = new Piece("横",Piece.PIECE_HORIZON,3,-1,-1);
       // left = pieces[].rect.right + miniFrameWidth; 左不变
        right = left + 3 * miniSizeOfUnit;
        top = pieces[3].rect.bottom + miniFrameHeight;
        bottom = top + miniSizeOfUnit;
        pieces[4] = new PieceRect(piece,new Rect(left,top,right,bottom));
    }
    private void onMeasureBox(int width,int height){
        //并排boy,box,block,dest
        //frame取1/4个unit，宽需要4个unit，5个frame，高度需要1个unit，2个frame

        int unitByWidth =( width*4)/21; // width/(5/4+4);
        int unitByHeight = (height*2)/3; //height/(1+2/4);
        if (unitByWidth > unitByHeight){
            miniSizeOfUnit = unitByHeight;
            miniFrameHeight = miniSizeOfUnit/4;
            miniFrameWidth = (width-4*miniSizeOfUnit)/5;
        }else{
            miniSizeOfUnit = unitByWidth;
            miniFrameWidth = miniSizeOfUnit/4;
            miniFrameHeight = (height-miniSizeOfUnit)/2;
        }

        //生成四个棋子
        int left,right,top,bottom;
        Piece piece;
        pieces = new PieceRect[4];
        //上下位置都一样
        top = miniFrameHeight;
        bottom = top + miniSizeOfUnit;
        //boy
        piece = new Piece("boy",Piece.PIECE_BOY,1,-1,-1) ;
        left = miniFrameWidth;
        right = left + miniSizeOfUnit;
        pieces[0] = new PieceRect(piece,new Rect(left,top,right,bottom));
        //box
        piece = new Piece("箱",Piece.PIECE_BOX,1,-1,-1);
        left = pieces[0].rect.right + miniFrameWidth;
        right = left + miniSizeOfUnit;
        pieces[1] = new PieceRect(piece,new Rect(left,top,right,bottom));
        //block
        piece = new Piece("砖",Piece.PIECE_BLOCK,1,-1,-1);
        left = pieces[1].rect.right + miniFrameWidth;
        right = left + miniSizeOfUnit;
        pieces[2] = new PieceRect(piece,new Rect(left,top,right,bottom));
        //兵
        piece = new Piece("目标",Piece.PIECE_DEST,1,-1,-1);
        left = pieces[2].rect.right + miniFrameWidth;
        right = left + miniSizeOfUnit;
        pieces[3] = new PieceRect(piece,new Rect(left,top,right,bottom));
    }
    @Override
    protected void onDraw(Canvas canvas) {
        if (gameType == null) { return; }
        super.onDraw(canvas);
        Log.v("addpiece",String.format("canvas:x=%d|y=%d",canvas.getWidth(),canvas.getHeight()));
        if (pieces != null){
            for(int i=0; i<pieces.length;i++){
                drawPiece(canvas,pieces[i]);
            }
        }
        if (movingPiece != null){
            drawMovingPiece(canvas);
        }
    }
    public void drawPiece(Canvas canvas, PieceRect piece){
        Log.v("addpiece",String.format("draw %s:%d,%d,%d,%d",piece.piece.name,piece.rect.left,piece.rect.top,piece.rect.right,piece.rect.bottom));
        float left=0,right=0,top=0,bottom=0;
        Bitmap bitmap = null;
        Paint paint = new Paint();
        //画黑边，填充稍微大一些的区域都为黑色
        paint.setColor(Color.BLACK);
        left = piece.rect.left-5;
        right = piece.rect.right+5;
        top = piece.rect.top-5;
        bottom = piece.rect.bottom+5;
        canvas.drawRect(left,top,right,bottom,paint);
        //然后填充图片，留下边框
        bitmap = piece.piece.getBitmap(this);
        canvas.drawBitmap(bitmap,null,piece.rect,paint);
    }
    public void drawMovingPiece(Canvas canvas){
        if (movingPiece == null){
            return;
        }
        int left=0,top=0,right=0,bottom=0;
        left = movingDestX;
        bottom = movingDestY;
        right = left + movingPiece.getWidth();
        top = bottom - movingPiece.getHeight();
        movingPiece.rect = new Rect(left,top,right,bottom);
        drawPiece(canvas,movingPiece);

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
                Log.v("view",String.format("你按下了%s",movingPiece.piece.name));
                actionListener.onActionDown(movingPiece.piece);
                break;
            case MotionEvent.ACTION_MOVE:
                movingDestX = (int) x;
                movingDestY = (int) y;
                invalidate();
                actionListener.onActionMove(x,y);
                break;
            case MotionEvent.ACTION_UP:
                //Log.v("view",String.format("目标移动到%s",destPiece.name));
                //actionListener.onActionUp();
                actionListener.onActionUp(-1,x,y);
                movingPiece = null;
                invalidate();
                break;
        }
        return true;
    }

    public PieceRect getPiece(float x, float y){
        for(int i=0; i<pieces.length; i++){
            if(pieces[i].isInRect(x,y)){
                return pieces[i].copyPieceRect();
            }
        }
        return null;
    }
    class PieceRect{
        Piece piece;
        Rect rect;
        PieceRect(Piece piece,Rect rect){
            this.piece = piece;
            this.rect = rect;
        }
        public Boolean isInRect(int x,int y){
            if(x > rect.left && x < rect.right && y > rect.top && y < rect.bottom){
                return true;
            }
            return false;
        }
        public Boolean isInRect(float x,float y){
            return isInRect((int)x,(int)y);
        }
        public PieceRect copyPieceRect(){
            Rect newRect = new Rect(rect.left,rect.top,rect.right,rect.bottom);
            return new PieceRect(piece.copyPiece(),newRect);
        }
        public int getWidth(){
            return rect.right - rect.left;
        }
        public int getHeight(){
            return rect.bottom - rect.top;
        }
    }
    /*class Rect{
        int left,right,top,bottom;
        Rect(int left,int top,int right,int bottom){
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            Log.v("addPiece",String.format("rect:left=%d,right=%d,top=%d,bottom=%d",left,right,top,bottom));
        }
        Rect(float left,float top,float right,float bottom){
            this.left = (int) left;
            this.top = (int) top;
            this.right = (int) right;
            this.bottom = (int) bottom;
        }
        public Boolean isInRect(int x,int y){
            if (x> left && x < right && y > top && y < bottom){
                return true;
            }
            return false;
        }
    }*/
    public interface ActionListener{
        public void onActionDown(Piece piece);     //按下的棋子
        public void onActionMove(float x,float y); //move到位置坐标(android坐标)
        public void onActionUp(int direction,float x,float y); //方向及位置,xy对应坐标
    }
    public void setOnActionListener(ActionListener actionListener){
        this.actionListener = actionListener;
    }
}
