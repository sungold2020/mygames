package com.sungold.huarongdao;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;


public class SudokuBoardView extends View {
    public final static int MODE_HELP = 1;   //帮助模式
    public final static int MODE_MANUAL = 2; //手工模式
    public final static int MODE_ADDBOARD = 3; //布局模式
    public final static int COLOR_SELECTED_BACKGROUND = Color.GRAY; //选中时的背景色。
    public final static int COLOR_BIGNUMBER_BACKGROUND = Color.YELLOW;  //点亮大数字时的背景色。
    public final static int COLOR_SMALLNUMBER_BACKGROUND = Color.GREEN;  //点亮小数字时的背景色。
    public final static int COLOR_BIGNUMBER_UNMODIFIABLE = Color.BLACK; //不可修改的数字颜色(题的数字)
    public final static int COLOR_BIGNUMBER_MODIFIABLE = Color.BLUE; //解题填入的数字颜色.
    public final static int COLOR_BIGNUMBER_CONFLICT = Color.RED; //数字冲突是显示颜色.
    public final static int COLOR_SMALL_BOARD_NUMBER = Color.BLACK; //数字盘的数字颜色
    public final static int COLOR_SMALL_BOARD_NUMBER_COMPLETED = Color.LTGRAY; //数字盘，已经完成的数字颜色的颜色
    public final static int COLOR_PERCENT_BACKGROUND = Color.LTGRAY;   //百分比宫格的背景色
    public final static int COLOR_SUPER_BACKGROUND = Color.LTGRAY;     //super宫格的背景色
    public final static int COLOR_DIAGONAL_BACKGROUND = Color.LTGRAY;   //百分比宫格的背景色
    public final static int COLOR_HINT_BACKGROUND = Color.RED; //提示单元格的背景色

    SudokuType sudokuType = SudokuType.NINE;
    final int padWidth = 30;           //左右的边距
    final int padHeight = 5;           //上下的边距
    int bigBoardHeight; //大board的高度
    int bigBoardPadWidth,bigBoardPadHeight; //大board的上下和左右边距
    int width,height;  //view的宽度和高度
    int sizeOfUnit;    //大board的单元格大小
    int smallBoardHeight; //小board的高度
    int smallBoardPadWidth=20,smallBoardPadHeight=10; //小board的上下和左右中边距
    int sizeOfMiniUnit;  //小board的单元格大小

    public SudokuBoard board = null;
    private int mode = MODE_MANUAL;  //缺省是手工模式

    public SudokuPiece clickPiece = null; //记录按下的piece

    private SudokuPiece brightSquarePiece = null; //board中点亮的宫格
    private int brightRow = -1;                 //board中点亮的行
    private int brightColumn = -1;              //board中点亮的列

    private int selectedNumber = -1; //点亮的数字，将会在board中显示关联该数字的背景色
                                    //大数字等于它的,备选小数字包含它的显示不同背景色
    private SudokuPiece selectedPiece = null; //当前选中的piece，可以是board中的piece，也可以是大小数字盘的piece

    private List<SudokuPiece> hintPieceList = null;
    private ActionListener actionListener = null;
    public SudokuBoardView(Context context){
        super(context);
    }
    public SudokuBoardView(Context context, AttributeSet attrs){
        super(context,attrs);
    }
    public void initBoard(SudokuBoard board){
        this.board = (SudokuBoard)board.copyBoard();
        sudokuType = board.sudokuType;
        requestLayout();
    }
    public void setBoard(SudokuBoard board){
        this.board = (SudokuBoard)board.copyBoard();
        sudokuType = board.sudokuType;
        invalidate();
        //requestLayout();
    }
    public void setMode(int mode){
        this.mode = mode;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //根据sudokutype，计算baord大小，布局如下：
        //宫格棋盘
        //左右两个数字宫格。高度取上面大棋盘的一半
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        Log.v("board",String.format("x=%d;y=%d",width,height));
        int maxX = SudokuBoard.getMaxX(sudokuType);
        int maxY = SudokuBoard.getMaxY(sudokuType);
        if(maxX > 0 && maxY > 0){
            if (height >= (width*3/2)){
                height = width*3/2;
            }else{
                width = height*2/3;
            }
            bigBoardHeight = (height)*2/3;
            smallBoardHeight = bigBoardHeight/2;
            setMeasuredDimension(width, height);

            //计算board
            int unitByWidth = (width - 2*padWidth) / maxX;
            int unitByHeight = (bigBoardHeight-2*padHeight)/maxY;
            sizeOfUnit = Math.min(unitByWidth,unitByHeight);
            bigBoardPadWidth = (width - sizeOfUnit*maxX)/2;
            bigBoardPadHeight = (width -sizeOfUnit*maxY)/2;

            //计算smallboard, 左右各一个宫格
            int numberOfX = SudokuBoard.getNumberOfSmallBoardX(sudokuType); //一行的数字个数
            int numberOfY = SudokuBoard.getNumberOfSmallBoardY(sudokuType); //一列的数字个数
            //以width计算的sizeOfMiniUnit
            int miniUnitByWidth = ((width - (3*smallBoardPadWidth)) / numberOfX) / 2;
            int minUnitByHeight = (smallBoardHeight - 2*smallBoardPadHeight) / numberOfY;
            sizeOfMiniUnit = Math.min(miniUnitByWidth,minUnitByHeight);
            smallBoardPadHeight = (smallBoardHeight - numberOfY*sizeOfMiniUnit) / 2;
            smallBoardPadWidth = (width - 2*sizeOfMiniUnit*numberOfX)/3;
            Log.v("sudokuBoardview",String.format("board=(%d,%d)",width,height));
            Log.v("sudokuBoardview",String.format("height=(%d,%d)",bigBoardHeight,smallBoardHeight));
            Log.v("sudokuBoardview",String.format("smallpad=(%d,%d)",smallBoardPadWidth,smallBoardPadHeight));
        }
        //Log.v("board", String.format("x=%d;y=%d;frame=%d", width, height));
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        setBackgroundColor(Color.WHITE);

        //画棋盘
        /*if(brightBoardPiece != null){
            brightPiece(canvas,brightBoardPiece);
        }*/
        drawBoard(canvas);
        if (brightSquarePiece != null){
            brightSquare(canvas, brightSquarePiece);
        }
        if(brightRow >= 0){
            brightRow(canvas,brightRow);
        }
        if(brightColumn >= 0){
            brightColumn(canvas,brightColumn);
        }
        //画提示单元格的框线
        drawHintPieces(canvas);
        /*//点亮大数字，必须先于画数字棋盘，否则数字会被冲掉，或者位置不对
        if(brightBigNumber > 0){
            brightBigNumber(canvas,brightBigNumber);
        }
        if(brightSmallNumber > 0){
            brightSmallNumber(canvas, brightSmallNumber);
        }*/
        //画左右两个数字棋盘
        //canvas.translate(0,bigBoardHeight); //canvas.restore()
        drawSmallBoard(canvas);
        //canvas.restore();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mode == MODE_HELP) { return false; }
        float x = event.getX();
        float y = event.getY();

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                clickPiece = getPiece(x,y);
                if (clickPiece != null) {
                    //Log.v("view",String.format("你按下了%s",clickPiece.name));
                    invalidate();
                }
                //actionListener.onActionDown(clickPiece);  //回调给activity处理
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                SudokuPiece sudokuPiece = getPiece(x,y);
                if(clickPiece != null
                        && sudokuPiece != null
                        && clickPiece.type == sudokuPiece.type
                        && clickPiece.x == sudokuPiece.x
                        && clickPiece.y == sudokuPiece.y){
                    // down/up的是同一个piece，才等于点击事件
                    Log.v("view","click piece:");
                    clickPiece.printPiece();
                    handleClickPiece(clickPiece);
                }
                Log.v("view",String.format("点击之后的selectedPiece:"));
                if(selectedPiece != null){
                    selectedPiece.printPiece();
                }
                Log.v("view",String.format("点击之后的selectedNumber:%d",selectedNumber));
                invalidate();
                break;
        }
        return true;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        Log.v("sudoku",String.format("press key(%d,%s)",keyCode,event.toString()));
        switch(keyCode){
            case KeyEvent.KEYCODE_1:
                handlePressNumber(1);
                break;
            case KeyEvent.KEYCODE_2:
                handlePressNumber(2);
                break;
            case KeyEvent.KEYCODE_3:
                handlePressNumber(3);
                break;
            case KeyEvent.KEYCODE_4:
                handlePressNumber(4);
                break;
            case KeyEvent.KEYCODE_5:
                handlePressNumber(5);
                break;
            case KeyEvent.KEYCODE_6:
                handlePressNumber(6);
                break;
            case KeyEvent.KEYCODE_7:
                handlePressNumber(7);
                break;
            case KeyEvent.KEYCODE_8:
                handlePressNumber(8);
                break;
            case KeyEvent.KEYCODE_9:
                handlePressNumber(9);
                break;
        }
        return true;
    }
    private void handlePressNumber(int number){
        // 按下数字键number，相当于按了大数字盘的对应数字
        if(number > SudokuBoard.getMaxNumber(sudokuType)){
            return;
        }
        handleClickPiece(new SudokuPiece(number,SudokuPiece.PIECE_SUDOKU_NUMBER,-1,-1));
    }
    private void handleClickPiece(SudokuPiece piece){
        //actionListener.onActionUp(clickPiece); 回调给activity去处理
        //根据之前选中的piece来作不同处理

        //首先去除hint
        hintPieceList = null;
        if(selectedPiece == null){
            selectedPiece = piece;
            if(selectedPiece.type == Piece.PIECE_SUDOKU_BOARD) {
                if(piece.getNumber() > 0) {
                    //点亮该数字
                    selectedNumber = selectedPiece.getNumber();
                }
            }else{
                //点亮数字
                selectedNumber = selectedPiece.getNumber();
            }
            return;
        }
        Log.v("view","selectedPiece:");
        selectedPiece.printPiece();
        Log.v("view","clickPiece");
        piece.printPiece();

        switch(selectedPiece.type){
            case Piece.PIECE_SUDOKU_BOARD:
                //上次选中的是board中的单元格
                switch(piece.type){
                    case Piece.PIECE_SUDOKU_BOARD:
                        //上次选中board单元格，这次选中的也是board单元格
                        if(isPieceSelected(piece)) {
                            //同一个单元格，取消选中
                            selectedPiece = null;
                            //如果数字被点亮，也同步取消
                            if(piece.getNumber() > 0){
                                selectedNumber = 0;
                            }
                        }else {
                            //1，不同单元格，切换选中
                            selectedPiece = piece;
                            // 2.如果单元格已经填写数字，点亮该数字
                            if(piece.getNumber() > 0){
                                selectedNumber = piece.getNumber();
                            }
                        }

                        return;
                    case Piece.PIECE_SUDOKU_NUMBER:
                        //上次选中board单元格，这次选中bignumber
                        //1,单元格填入数字
                        board.setNumber(selectedPiece.x,selectedPiece.y,piece.getNumber());
                        //2,点亮该数字
                        selectedNumber = piece.getNumber();
                        //3，board有改变，回调给activity去处理
                        actionListener.onActionUp(piece);
                        return;
                    case Piece.PIECE_SUDOKU_MINI_NUMBER:
                        //上次选中board单元格，这次选中mininumber
                        //1,单元格增加备选小数字
                        board.addMiniNumber(selectedPiece.x,selectedPiece.y,piece.getNumber());
                        //2,点亮该数字
                        selectedNumber = piece.getNumber();
                        //3，board有改变，回调给activity去处理
                        actionListener.onActionUp(piece);
                        return;
                    default:
                        return;
                }
            case Piece.PIECE_SUDOKU_NUMBER:
                //上次选中的是bignumber
                switch(piece.type){
                    case Piece.PIECE_SUDOKU_BOARD:
                        //这次选中的是board中的单元格
                        //1,单元格填入该数字
                        board.setNumber(piece.x,piece.y,selectedPiece.getNumber());
                        //2,回调给activity
                        actionListener.onActionUp(piece);
                        return;
                    case Piece.PIECE_SUDOKU_NUMBER:
                        //这次选中的也是bignumber
                        if(selectedNumber == piece.getNumber()){
                            //如果是同一个bignumber，就取消selectedPiece和selectedNumber
                            selectedPiece = null;
                            selectedNumber = -1;
                        }else{
                            //否则就切换selectedPiece和selectedNumber
                            selectedPiece = piece;
                            selectedNumber = piece.getNumber();
                        }
                        return;
                    case Piece.PIECE_SUDOKU_MINI_NUMBER:
                        //这次选中的是miniNumber，切换seletedPiece和selectedNumber
                        selectedPiece = piece;
                        selectedNumber = piece.getNumber();
                        return;
                    default:
                        return;
                }
            case Piece.PIECE_SUDOKU_MINI_NUMBER:
                //上次选中的是board中的miniNumber
                switch(piece.type){
                    case Piece.PIECE_SUDOKU_BOARD:
                        //这次选中的是board中的单元格
                        //1,单元格填入该数字
                        board.addMiniNumber(piece.x,piece.y,selectedPiece.getNumber());
                        //2,回调给activity
                        actionListener.onActionUp(piece);
                        return;
                    case Piece.PIECE_SUDOKU_NUMBER:
                        //这次选中的是bigNumber，切换seletedPiece和selectedNumber
                        selectedPiece = piece;
                        selectedNumber = piece.getNumber();
                        return;
                    case Piece.PIECE_SUDOKU_MINI_NUMBER:
                        //这次选中的也是mininumber
                        if(selectedNumber == piece.getNumber()){
                            //如果是同一个mininumber，就取消selectedPiece和selectedNumber
                            selectedPiece = null;
                            selectedNumber = -1;
                        }else{
                            //否则就切换selectedPiece和selectedNumber
                            selectedPiece = piece;
                            selectedNumber = piece.getNumber();
                        }
                        return;
                    default:
                        return;
                }
            default:
                return;
        }
    }
    public void drawBoard(Canvas canvas){
        //画横线
        int maxNumber = SudokuBoard.getMaxNumber(sudokuType);
        for(int i=0; i<=maxNumber; i++){
            Paint paint = new Paint();
            paint.setColor(Color.LTGRAY);
            paint.setStrokeWidth(2);
            int startX = bigBoardPadWidth;
            int stopX = width-bigBoardPadWidth;
            int startY = (maxNumber-i)*sizeOfUnit+bigBoardPadHeight;
            int stopY = startY;
            canvas.drawLine(startX,startY,stopX,stopY,paint);
        }
        //画竖线
        for(int i=0; i<=maxNumber; i++){
            Paint paint = new Paint();
            paint.setColor(Color.LTGRAY);
            paint.setStrokeWidth(2);
            int startX = bigBoardPadWidth + i*sizeOfUnit;
            int stopX =startX;
            int startY = bigBoardPadHeight;
            int stopY = bigBoardHeight-bigBoardPadHeight;
            canvas.drawLine(startX,startY,stopX,stopY,paint);
        }
        //画宫格粗线-横
        int numberOfSquareY = SudokuBoard.getNumberOfSquareY(sudokuType);
        int maxYOfSquare = SudokuBoard.getMaxYOfSquare(sudokuType);
        for(int i=0; i<=numberOfSquareY; i++){
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(5);
            int startX = bigBoardPadWidth;
            int stopX = width-bigBoardPadWidth;
            int startY = (numberOfSquareY-i)*maxYOfSquare*sizeOfUnit+bigBoardPadHeight;
            int stopY = (numberOfSquareY-i)*maxYOfSquare*sizeOfUnit+bigBoardPadHeight;
            canvas.drawLine(startX,startY,stopX,stopY,paint);
        }
        //画宫格粗线-竖
        int numberOfSquareX = SudokuBoard.getNumberOfSquareX(sudokuType);
        int maxXOfSquare = SudokuBoard.getMaxXOfSquare(sudokuType);
        for(int i=0; i<=numberOfSquareX; i++){
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(5);
            int startX = bigBoardPadWidth + i*maxXOfSquare*sizeOfUnit;
            int stopX = bigBoardPadWidth + i*maxXOfSquare*sizeOfUnit;
            int startY = bigBoardPadHeight;
            int stopY = bigBoardHeight-bigBoardPadHeight;
            canvas.drawLine(startX,startY,stopX,stopY,paint);
        }

        //填写数字
        if (board == null) { return; }
        int maxX = SudokuBoard.getMaxX(sudokuType);
        int maxY = SudokuBoard.getMaxY(sudokuType);
        for(int x=0; x<maxX; x++){
            for(int y=0; y<maxY; y++){
                drawPiece(canvas,(SudokuPiece)board.getPiece(x,y));
            }
        }
    }
    private void drawHintPieces(Canvas canvas){
        if(hintPieceList == null){
            return ;
        }
        for(int i=0; i<hintPieceList.size(); i++){
            brightPiece(canvas,hintPieceList.get(i));
        }
    }
    public void drawHintPieces(List<SudokuPiece> pieceList){
        this.hintPieceList = pieceList;
        invalidate();
    }
    private void drawSmallBoard(Canvas canvas){
        int numberOfX = SudokuBoard.getNumberOfSmallBoardX(sudokuType);
        int numberOfY = SudokuBoard.getNumberOfSmallBoardY(sudokuType);
        int maxNumber = SudokuBoard.getMaxNumber(sudokuType);

        //画左边的大数字宫格
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);
        //画横线
        for(int i=0; i<=numberOfY; i++){
            int startX = smallBoardPadWidth;
            int stopX = startX + sizeOfMiniUnit*numberOfX;
            int startY = bigBoardHeight+smallBoardPadHeight + i*sizeOfMiniUnit;
            int stopY = startY;
            canvas.drawLine(startX,startY,stopX,stopY,paint);
        }
        //画竖线
        for(int i=0; i<=numberOfX; i++){
            int startX = smallBoardPadWidth + i * sizeOfMiniUnit;
            int stopX = startX;
            int startY = smallBoardPadHeight+bigBoardHeight;
            int stopY = startY+sizeOfMiniUnit*numberOfY;
            canvas.drawLine(startX,startY,stopX,stopY,paint);
        }
        //画背景，如果大数字盘中的数字被选中
        if(selectedPiece != null && selectedPiece.type == Piece.PIECE_SUDOKU_NUMBER){
            Log.v("sudoku",String.format("点亮bignumber背景:%d",selectedPiece.getNumber()));
            drawSMallBoardBackground(canvas,selectedPiece.getNumber(),true);
        }
        //填数字
        for(int i=1; i<=maxNumber; i++){
            int left = smallBoardPadWidth + (((i-1)%numberOfX))*sizeOfMiniUnit;
            int right = left + sizeOfMiniUnit;
            int top = bigBoardHeight+smallBoardPadHeight + ((i-1)/numberOfX)*sizeOfMiniUnit;
            int bottom = top+sizeOfMiniUnit;
            int color;
            if(board != null && board.numberCompleted(i)){
                //如果该数字已经完成，就显示灰色
                color = COLOR_SMALL_BOARD_NUMBER_COMPLETED;
            }else{
                color = COLOR_SMALL_BOARD_NUMBER;
            }
            drawNumber(canvas,left,top,right,bottom,i,color);
        }

        //画右边的小数字宫格
        //canvas.translate(sizeOfMiniUnit*numberOfX+smallBoardPadWidth,0);
        //画横线
        for(int i=0; i<=numberOfY; i++){
            int startX = 2*smallBoardPadWidth+sizeOfMiniUnit*numberOfX;
            int stopX = startX + sizeOfMiniUnit*numberOfX;
            int startY = bigBoardHeight+smallBoardPadHeight + i*sizeOfMiniUnit;
            int stopY = startY;
            canvas.drawLine(startX,startY,stopX,stopY,paint);
        }
        //画竖线
        for(int i=0; i<=numberOfX; i++){
            int startX = 2*smallBoardPadWidth + sizeOfMiniUnit*numberOfX + i * sizeOfMiniUnit;
            int stopX = startX;
            int startY = bigBoardHeight+smallBoardPadHeight;
            int stopY = startY+sizeOfMiniUnit*numberOfY;
            canvas.drawLine(startX,startY,stopX,stopY,paint);
        }
        //画背景，如果小数字盘中的数字被选中
        if(selectedPiece != null && selectedPiece.type == Piece.PIECE_SUDOKU_MINI_NUMBER){
            Log.v("sudoku",String.format("点亮smallnumber背景:%d",selectedPiece.getNumber()));
            drawSMallBoardBackground(canvas,selectedPiece.getNumber(),false);
        }
        //填数字
        for(int i=1; i<=maxNumber; i++){
            int left = 2*smallBoardPadWidth + sizeOfMiniUnit*numberOfX+(((i-1)%numberOfX))*sizeOfMiniUnit;
            int right = left + sizeOfMiniUnit;
            int top = bigBoardHeight+smallBoardPadHeight + ((i-1)/numberOfX)*sizeOfMiniUnit;
            int bottom = top+sizeOfMiniUnit;
            int color;
            if(board != null && board.numberCompleted(i)){
                //如果该数字已经完成，就显示灰色
                color = COLOR_SMALL_BOARD_NUMBER_COMPLETED;
            }else{
                color = COLOR_SMALL_BOARD_NUMBER;
            }
            //填小数字，仅填右下角
            left = (right+left)/2;
            top = (top+bottom)/2;
            drawNumber(canvas,left,top,right,bottom,i,color);
        }
    }

    private void drawPiece(Canvas canvas, SudokuPiece piece){
        /*
        画单元格（含背景，大小数字等）


        */
        if(piece == null){
            return;
        }
        //  画背景
        // 如果属于百分比宫格，显示背景色COLOR_PERCENT_BACKGROUND
        // 如果属于super宫格，显示背景色COLOR_SUPER_BACKGROUND
        // 如果备选数字包含brrightNumber，显示背景色COLOR_SMALLNUMBER_BACKGROUND
        // 如果数字等于brightNumber，显示背景色COLOR_BIGNUMBER_BACKGROUND
        // 如果被选中，显示背景色（优先）
        // 如果多个条件都满足，层层覆盖，所以最后显示的是最后画的背景色
        if(sudokuType == SudokuType.PERCENT && board.isInPercentSquare(piece)){
            drawBoardBackground(canvas,piece.x,piece.y,COLOR_PERCENT_BACKGROUND);
        }
        if(sudokuType == SudokuType.SUPER && board.isInSuperSquare(piece)){
            drawBoardBackground(canvas,piece.x,piece.y,COLOR_SUPER_BACKGROUND);
        }
        if(piece.getNumber() > 0 && selectedNumber == piece.getNumber()){
            drawBoardBackground(canvas, piece.x, piece.y, COLOR_BIGNUMBER_BACKGROUND);
        }else if(piece.getNumber() == 0 && piece.haveMiniNumber(selectedNumber) && !board.numberExist(piece,selectedNumber)){
            drawBoardBackground(canvas,piece.x,piece.y,COLOR_SMALLNUMBER_BACKGROUND);
        }
        if(isPieceSelected(piece)){
            drawBoardBackground(canvas,piece.x,piece.y,COLOR_SELECTED_BACKGROUND);
        }
        //画对角线
        if(sudokuType == SudokuType.X_STYLE && board.isInDiagonal(piece)){
                drawDiagonal(canvas,piece.x,piece.y);
        }else if(sudokuType == SudokuType.PERCENT && board.isInPercentDiagonal(piece)) {
                drawDiagonal(canvas, piece.x, piece.y);
        }
        //写数字
        // 不可更改的：COLOR_BIGNUMBER_UNMODIFIABLE
        // 可更改的：COLOR_BIGNUMBER_MODIFIABLE
        int leftOfPiece = getAndroidX(piece.x);
        int bottomOfPiece = getAndroidY(piece.y);
        if (piece.getNumber() > 0){  //大数字
            int color;
            if (!piece.isModifiable()){  //不可修改
                color = COLOR_BIGNUMBER_UNMODIFIABLE;
            }else if(board.isNumberUnique(piece)){ //数字唯一
                color = COLOR_BIGNUMBER_MODIFIABLE;
            }else{  //数字不唯一(冲突)
                color = COLOR_BIGNUMBER_CONFLICT;
            }
            drawNumber(canvas,
                    leftOfPiece,
                    bottomOfPiece-sizeOfUnit,
                    leftOfPiece +sizeOfUnit,
                    bottomOfPiece,
                    piece.getNumber(),color);
        }else{ // 备选小数字
            int[] numbers = piece.getMiniNumbers();
            if (numbers != null) {
                for (int i = 0; i < numbers.length; i++) {
                    int left, right,top,bottom;
                    int numberOfX, numberOfY;  //备选数字中，每行/每列的数字个数
                    int optionUnitWidth, optionUnitHeight; //备选框中，单元格的宽度，高度

                    if(board.numberExist(piece,numbers[i])){
                        //该miniNumber和board中的数字冲突（同行，同列，同宫格中已经存在该数字），就不显示
                        continue;
                    }
                    numberOfX = SudokuBoard.getNumberOfSquareX(sudokuType);
                    numberOfY = SudokuBoard.getNumberOfSquareY(sudokuType);
                    optionUnitWidth = sizeOfUnit / numberOfX;
                    optionUnitHeight = sizeOfUnit / numberOfY;
                    left = ((numbers[i]-1) % numberOfX) * optionUnitWidth + leftOfPiece;
                    right = left + optionUnitWidth;
                    bottom =  bottomOfPiece - ((numbers[i]-1) / numberOfX) * optionUnitHeight;
                    top = bottom - optionUnitHeight;
                    drawNumber( canvas,left,top,right,bottom,numbers[i],Color.BLACK);
                }
            }
        }

        /*// 对角线/百分比宫格背景单独标识
        if(sudokuType == SudokuType.X_STYLE){
            if(board.isInDiagonal(piece)){
                drawDiagonal(canvas,piece.x,piece.y);
            }
        }else if(sudokuType == SudokuType.PERCENT){
            if(board.isInPercentDiagonal(piece) ){
                drawDiagonal(canvas,piece.x,piece.y);
            }if(board.isInPercentSquare(piece)) {
                drawBoardBackground(canvas,piece.x,piece.y,COLOR_DIAGONAL_BACKGROUND);
            }
        }
        //写数字
        if (piece.getNumber() > 0){  //大数字
            //画背景
            if(isPieceSelected(piece)){  //单元格是否被选中
                drawBoardBackground(canvas,piece.x,piece.y,COLOR_SELECTED_BACKGROUND);
            }else if(selectedNumber > 0){
                    if(piece.getNumber() == selectedNumber) {
                        //选中数字
                        drawBoardBackground(canvas, piece.x, piece.y, COLOR_BIGNUMBER_BACKGROUND);
                    }
            }
            //写数字，是否可以更改用不同颜色，冲突用不同颜色
            int color;
            if (!piece.isModifiable()){  //不可修改
                color = COLOR_BIGNUMBER_UNMODIFIABLE;
            }else if(board.isNumberUnique(piece)){ //数字唯一
                color = COLOR_BIGNUMBER_MODIFIABLE;
            }else{  //数字不唯一(冲突)
                color = COLOR_BIGNUMBER_CONFLICT;
            }
            drawNumber(canvas,
                    leftOfPiece,
                    bottomOfPiece-sizeOfUnit,
                    leftOfPiece +sizeOfUnit,
                    bottomOfPiece,
                    piece.getNumber(),color);
        }else{ // 备选小数字
            //画背景
            if(isPieceSelected(piece)){  //单元格是否被选中
                drawBoardBackground(canvas,piece.x,piece.y,COLOR_SELECTED_BACKGROUND);
            }else if(selectedNumber > 0){
                if(piece.haveMiniNumber(selectedNumber)&& !board.numberExist(piece,selectedNumber)){
                    drawBoardBackground(canvas,piece.x,piece.y,COLOR_SMALLNUMBER_BACKGROUND);
                }
            }
            int[] numbers = piece.getMiniNumbers();
            if (numbers != null) {
                for (int i = 0; i < numbers.length; i++) {
                    int left, right,top,bottom;
                    int numberOfX, numberOfY;  //备选数字中，每行/每列的数字个数
                    int optionUnitWidth, optionUnitHeight; //备选框中，单元格的宽度，高度

                    if(board.numberExist(piece,numbers[i])){
                        //该miniNumber和board中的数字冲突（同行，同列，同宫格中已经存在该数字），就不显示
                        continue;
                    }
                    numberOfX = SudokuBoard.getNumberOfSquareX(sudokuType);
                    numberOfY = SudokuBoard.getNumberOfSquareY(sudokuType);
                    optionUnitWidth = sizeOfUnit / numberOfX;
                    optionUnitHeight = sizeOfUnit / numberOfY;
                    left = ((numbers[i]-1) % numberOfX) * optionUnitWidth + leftOfPiece;
                    right = left + optionUnitWidth;
                    bottom =  bottomOfPiece - ((numbers[i]-1) / numberOfX) * optionUnitHeight;
                    top = bottom - optionUnitHeight;
                    drawNumber( canvas,left,top,right,bottom,numbers[i],Color.BLACK);
                }
            }
        }*/
    }
    private void drawNumber(Canvas canvas,int left, int top, int right, int bottom, int number,int color){
        //在矩阵中填写数字number
        int height = bottom - top;
        int textSize = height; //高度为size
        int baseline = bottom -height/6 ;
        int middle = (right+left)/2;
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setAntiAlias(true);
        canvas.drawText(String.valueOf(number), middle, baseline, paint);
    }
    private void brightPiece(Canvas canvas, SudokuPiece piece){
        //点亮piece的框线
        if(piece.x < 0 || piece.y < 0){
            return;
        }
        int left = getAndroidX(piece.x);
        int bottom = getAndroidY(piece.y);
        int right = left + sizeOfUnit;
        int top = bottom - sizeOfUnit;
        Paint paint = new Paint();
        paint.setColor(COLOR_HINT_BACKGROUND);
        paint.setStrokeWidth(5);
        //画上横线
        canvas.drawLine(left,top,right,top,paint);
        //画下横线
        canvas.drawLine(left,bottom,right,bottom,paint);
        //画左竖线
        canvas.drawLine(left,bottom,left,top,paint);
        //画右竖线
        canvas.drawLine(right,bottom,right,top,paint);
    }
    private void brightSquare(Canvas canvas,SudokuPiece sudokuPiece){
        //点亮sudokupiece所在的宫格
        Log.v("boardview", "开始点亮宫格");
        if(sudokuPiece.type != Piece.PIECE_SUDOKU_BOARD){
            return;
        }
        int maxY = SudokuBoard.getMaxY(sudokuType);
        int maxXOfSquare = SudokuBoard.getMaxXOfSquare(sudokuType);
        int maxYOfSquare = SudokuBoard.getMaxYOfSquare(sudokuType);
        int indexOfSquareX = (int)(sudokuPiece.x/maxXOfSquare);
        int startX = getAndroidX(indexOfSquareX*maxXOfSquare);
        int stopX = getAndroidX((indexOfSquareX+1)*maxXOfSquare);
        int indexOfSquareY = (int)(sudokuPiece.y/maxYOfSquare);
        int stopY = getAndroidY(indexOfSquareY*maxYOfSquare);
        int startY = getAndroidY((indexOfSquareY+1)*maxYOfSquare);

        //点亮区域
        brightRect(canvas,startX,startY,stopX,stopY);
    }
    public void brightSquare(SudokuPiece sudokuPiece){
        brightSquarePiece = sudokuPiece;
        invalidate();
    }
    private void brightRow(Canvas canvas,int y){
        //点亮y行
        if (y < 0){
            return;
        }
        int startX = getAndroidX(0);
        int stopX = getAndroidX(SudokuBoard.getMaxX(sudokuType));
        int stopY = getAndroidY(y);
        int startY = getAndroidY(y+1);
        brightRect(canvas,startX,startY,stopX,stopY);
    }
    public void brightRow(int y){
        brightRow = y;
        invalidate();
    }
    private void brightColumn(Canvas canvas,int x){
        //点亮x列
        if (x < 0){
            return;
        }
        int startX = getAndroidX(x);
        int stopX = getAndroidX(x+1);
        int stopY = getAndroidY(0);
        int startY = getAndroidY(SudokuBoard.getMaxY(sudokuType));
        brightRect(canvas,startX,startY,stopX,stopY);
    }
    public void brightColumn(int x){
        brightColumn = x;
        invalidate();
    }
    /*private void brightPiece(Canvas canvas,SudokuPiece piece){
        int left = getAndroidX(piece.x);
        int right = getAndroidX(piece.x+1);
        int bottom = getAndroidY(piece.y);
        int top = getAndroidY(piece.y+1);
        Paint paint = new Paint();
        paint.setColor(Color.LTGRAY);
        canvas.drawRect(left,top,right,bottom,paint);
        Log.v("boardview",String.format("点亮区域:%d,%d",piece.x,piece.y));
    }
    public void brightPiece(SudokuPiece piece){
        brightBoardPiece = piece;
        invalidate();
    }
    public void clearBrightPiece(){
        brightBoardPiece = null;
        invalidate();
    }*/
    private void drawBoardBackground(Canvas canvas,int x,int y,int color){
        //画背景色：x,y为board的坐标
        int left = getAndroidX(x)+3;      //+-3为防止冲掉分界线
        int right = getAndroidX(x+1)-3;
        int bottom = getAndroidY(y)-3;
        int top = getAndroidY(y+1)+3;
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(left,top,right,bottom,paint);
        //Log.v("boardview",String.format("点亮区域:%d,%d",piece.x,piece.y));
    }
    private void drawDiagonal(Canvas canvas,int x,int y){
        //画board中单元格对角线，x,y为board的坐标
        int left = getAndroidX(x);
        int bottom = getAndroidY(y);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(5);
        if(x == y) {  //对角线一
            canvas.drawLine(left, bottom, left + sizeOfUnit, bottom - sizeOfUnit, paint);
            if(sudokuType == SudokuType.X_STYLE && x == 4){//中心点还要画一个方向二的对角线
                canvas.drawLine(left, bottom-sizeOfUnit, left + sizeOfUnit, bottom, paint);
            }
        }else{ //对角线二
            canvas.drawLine(left, bottom-sizeOfUnit, left + sizeOfUnit, bottom, paint);
        }
    }
    private void drawSMallBoardBackground(Canvas canvas,int number,Boolean isBigNumber){
        //画数字盘的背景色
        int left,bottom;
        if(isBigNumber) {
            left = getBigNumberAndroidX(number);
            bottom = getBigNumberAndroidY(number);
        }else {
            left = getSmallNumberAndroidX(number);
            bottom = getSmallNumberAndroidY(number);
        }
        Paint paint = new Paint();
        paint.setColor(COLOR_SELECTED_BACKGROUND);
        Log.v("view",String.format("%d,%d",left,bottom));

        canvas.drawRect(left,bottom-sizeOfMiniUnit,left+sizeOfMiniUnit,bottom,paint);
    }
    private void brightRect(Canvas canvas,int startX,int startY,int stopX,int stopY){
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        canvas.drawLine(startX,startY,stopX,startY,paint);
        canvas.drawLine(startX,stopY,stopX,stopY,paint);
        canvas.drawLine(startX,startY,startX,stopY,paint);
        canvas.drawLine(stopX,startY,stopX,stopY,paint);
    }
    /*private void brightBigNumber(Canvas canvas, int number){
        Log.v("view","高亮bignumber:"+String.valueOf(number));
        int left = getBigNumberAndroidX(number);
        int bottom = getBigNumberAndroidY(number);
        Paint paint = new Paint();
        paint.setColor(Color.LTGRAY);
        canvas.drawRect(left,bottom-sizeOfMiniUnit,left+sizeOfMiniUnit,bottom,paint);
    }
    private void brightSmallNumber(Canvas canvas, int number){
        int left = getSmallNumberAndroidX(number);
        int bottom = getSmallNumberAndroidY(number);
        Paint paint = new Paint();
        paint.setColor(Color.LTGRAY);
        canvas.drawRect(left,bottom-sizeOfMiniUnit,left+sizeOfMiniUnit,bottom,paint);
    }
     */
    /*public void brightBigNumber(int number){
        brightBigNumber = number;
        brightBoardNumber = number;
        invalidate();
    }
    public void  brightSmallNumber(int number){
        brightSmallNumber = number;
        brightBoardMiniNumber = number;
        invalidate();
    }
    public void clearBrightBigNumber(){
        Log.v("view","clear brightbignumber:");
        brightBigNumber = -1;
        brightBoardNumber = -1;
        invalidate();
    }
    public void  clearBrightSmallNumber(){
        brightSmallNumber = -1;
        brightBoardMiniNumber = -1;
        invalidate();
    }
    public void clearBright(){
        SudokuPiece brightSqurePiece = null;
        int brightRow = -1;
        int brightColumn = -1;
        int brightNumber = -1;
        int brightMiniNumber = -1;
    }*/
    private int getAndroidX(int x){
        //piece的(x，y)单元格的左下角，返回对应安卓坐标x位置
        return bigBoardPadWidth + x * sizeOfUnit;
    }
    private int getAndroidY(int y){
        //piece的(x，y)单元格的左下角，返回对应安卓坐标Y位置
        int maxY = SudokuBoard.getMaxY(sudokuType);
        return bigBoardPadHeight + (maxY - y) * sizeOfUnit;
    }
    private int getBigNumberAndroidX(int number){
        int numberOfX = SudokuBoard.getNumberOfSmallBoardX(sudokuType);
        int x = (number-1) % numberOfX;
        return smallBoardPadWidth + x * sizeOfMiniUnit;
    }
    private int getBigNumberAndroidY(int number){
        int numberOfX = SudokuBoard.getNumberOfSmallBoardX(sudokuType);
        int y = (number-1) / numberOfX;
        return bigBoardHeight + smallBoardPadHeight + (y+1) * sizeOfMiniUnit;
    }
    private int getSmallNumberAndroidX(int number){
        int numberOfX = SudokuBoard.getNumberOfSmallBoardX(sudokuType);
        int x = (number-1) % numberOfX;
        return numberOfX*sizeOfMiniUnit+  2*smallBoardPadWidth + x * sizeOfMiniUnit;
    }
    private int getSmallNumberAndroidY(int number){
        int numberOfX = SudokuBoard.getNumberOfSmallBoardX(sudokuType);
        int y = (number-1) / numberOfX;
        return bigBoardHeight + smallBoardPadHeight + (y+1) * sizeOfMiniUnit;
    }


    public SudokuPiece getPiece(float x, float y){
        //根据指定坐标获取棋子
        int maxY = SudokuBoard.getMaxY(sudokuType);
        int numberOfX = SudokuBoard.getNumberOfSmallBoardX(sudokuType);
        int numberOfY = SudokuBoard.getNumberOfSmallBoardY(sudokuType);
        int piece_x,piece_y;
        int number;
        Log.v("view",String.format("y=%f,bigBoardHeight=%d",y,bigBoardHeight));
        if(y < bigBoardHeight){
            //棋盘
            piece_x = (int)((x-bigBoardPadWidth)/sizeOfUnit);
            piece_y = maxY - 1 - (int)(y-bigBoardPadHeight)/sizeOfUnit;
            Log.v("view",String.format("getPiece(%d,%d)",piece_x,piece_y));
            if(board.isOutOfBoard(piece_x,piece_y)){
                return null;
            }
            return (SudokuPiece)board.getPiece(piece_x,piece_y);
        }else if(y<bigBoardHeight+smallBoardHeight){
            //数字盘
            if (x <= smallBoardPadWidth){ //左边距
                return null;
            }
            else if(x < (width - smallBoardPadWidth - sizeOfMiniUnit*numberOfX)){
                //左边的大数字宫格
                y = y - bigBoardHeight;
                piece_x = (int)((x - smallBoardPadWidth)/sizeOfMiniUnit);
                piece_y = (int)((y - smallBoardPadHeight)/sizeOfMiniUnit);
                number = piece_x + 1 + piece_y*numberOfX;
                Log.v("view",String.format("x=%d,y=%d",piece_x,piece_y));
                if(number <= 0 || number > SudokuBoard.getMaxNumber(sudokuType)){
                    return null;
                }
                return new SudokuPiece(number,SudokuPiece.PIECE_SUDOKU_NUMBER,-1,-1);
            }else if(x >(width - 2*smallBoardPadWidth - sizeOfMiniUnit*numberOfX)){
                //右边的小数字宫格
                if (mode == MODE_ADDBOARD){ //add模式下，数字小盘不可用
                    return null;
                }
                y = y - bigBoardHeight;
                piece_x = (int)((x - 2*smallBoardPadWidth -sizeOfMiniUnit*numberOfX)/sizeOfMiniUnit);
                piece_y = (int)((y - smallBoardPadHeight)/sizeOfMiniUnit);
                number = piece_x + 1 + piece_y*numberOfX;
                if(number <= 0 || number > SudokuBoard.getMaxNumber(sudokuType)){
                    return null;
                }
                return new SudokuPiece(number,SudokuPiece.PIECE_SUDOKU_MINI_NUMBER,-1,-1);
            }
            return null;
        }else{
            return null;
        }
    }
    private Boolean isPieceSelected(SudokuPiece piece){
        //piece是否被选中？
        if(selectedPiece == null){
            return false;
        }
        if(selectedPiece.type == Piece.PIECE_SUDOKU_BOARD){
            if(piece.type == Piece.PIECE_SUDOKU_BOARD
                    && piece.x == selectedPiece.x
                    && piece.y == selectedPiece.y){
                return  true;
            }
            return false;
        }else{
            if(piece.type == selectedPiece.type && piece.getNumber() == selectedPiece.getNumber()){
                return true;
            }
            return false;
        }
    }
    public interface ActionListener{
        public void onActionUp(SudokuPiece piece); //方向及位置,xy对应坐标
        //public void onLongClick(Piece piece);     //长按事件，按下的棋子
    }
    public void setOnActionListener(ActionListener actionListener){
        this.actionListener = actionListener;
    }
}


