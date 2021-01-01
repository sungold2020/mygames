package com.sungold.huarongdao;

import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//棋盘，所有棋子的位置,构成了棋盘
public class SudokuBoard extends Board{
    public final static int CHECK_SOLUTION_OK = 1;
    public final static int CHECK_SOLUTION_NO = 2;
    public final static int CHECK_SOLUTION_NOT_ONLY = 3;

    public SudokuType sudokuType;

    public SudokuPiece[][] pieces = null;
    int seconds = 0 ;  //花费的时间

    //生成一个棋盘，但棋盘中的棋子未设置
    SudokuBoard(String name,SudokuType sudokuType){
        super(name,GameType.SUDOKU,getMaxX(sudokuType),getMaxY(sudokuType));
        this.sudokuType = sudokuType;
        //初始化pieces
        int maxX = getMaxX(sudokuType);
        int maxY = getMaxY(sudokuType);
        pieces = new SudokuPiece[maxX][maxY];
        for(int x=0; x<maxX; x++){
            for(int y=0; y<maxY; y++){
                pieces[x][y] = new SudokuPiece(0,SudokuPiece.PIECE_SUDOKU_BOARD,x,y);
            }
        }
    }
    //根据输入的棋子生成一个棋盘
    SudokuBoard(String name,SudokuType sudokuType, SudokuPiece[][] pieces) {
        super(name,GameType.SUDOKU,getMaxX(sudokuType),getMaxY(sudokuType));
        this.sudokuType = sudokuType;
        this.pieces = pieces;
    }

    @Override
    public void convertBoard() {
        ;
    }

    @Override
    public Piece getPieceByName(String name) {
        return null;
    }

    @Override
    public Piece getPiece(int x, int y) {
        if (isOutOfBoard(x, y)) {
            return null;
        }
        return pieces[x][y];
    }
    @Override
    public Board copyBoard() {
        SudokuPiece[][] newPieces;
        int mxX = getMaxX(sudokuType);
        int maxY = getMaxY(sudokuType);
        newPieces = new SudokuPiece[maxX][maxY];
        for(int x=0; x<mxX; x++){
            for(int y=0; y<maxY; y++){
                newPieces[x][y] = pieces[x][y].copyPiece();
            }
        }
        return new SudokuBoard(name,sudokuType,newPieces);
    }

    @Override
    public Boolean isSuccess() {
        if (pieces == null){
            Log.e("sudokuboard","pieces is null for isSuccess");
            return false;
        }
        if(!isPiecesUnique()){
            return false;
        }
        return true;
    }

    @Override
    public Boolean isOutOfBoard(int x, int y) {
        int MAX_X =  getMaxX(sudokuType);
        int MAX_Y = getMaxY(sudokuType);
        if (x < 0 || x >= MAX_X || y < 0 || y >= MAX_Y) {
            return true;
        }
        return false;
    }

    @Override
    public void addPiece(Piece piece) {

    }

    @Override
    public void delPiece(Piece piece) {

    }
    @Override
    public String checkBoard() {
        //检查行
        for(int y=0; y<getMaxY(sudokuType); y++) {
            if (!checkBigNumberRow(y)){
                return "failed";
            }
        }
        //检查列
        for(int x=0; x<getMaxX(sudokuType); x++){
            if (!checkBigNumberColumn(x)){
                return "failed";
            }
        }
        //检查宫格
        int numberOfSquareX = getNumberOfSquareX(sudokuType);
        int numberOfSquareY = getNumberOfSquareY(sudokuType);
        for(int x=0; x<numberOfSquareX; x++){
            for(int y=0; y<numberOfSquareY; y++){
                int piece_x = x*getMaxXOfSquare(sudokuType);
                int pice_y = y*getMaxYOfSquare(sudokuType);
                if(!checkBigNumberSquare(piece_x,pice_y)){
                    return "failed";
                }
            }
        }
        switch(sudokuType){
            case FOUR:
            case SIX:
            case NINE:
                return "OK";
            case X_STYLE:
                //还需要检查两个对角线
                if(checkBigNumberDiagonal(4,4)){
                    return "OK";
                }
                return "failed";
            case PERCENT:
                if(checkBigNumberPercentDiagonal(4,4)
                        && checkBigNumberPercent(2,6)
                        && checkBigNumberPercent(6,2)){
                    return "OK";
                }
                return "failed";
            case SUPER:
                if(checkBigNumberSuper(1,1) && checkBigNumberSuper(5,5)
                        && checkBigNumberSuper(7,1) && checkBigNumberSuper(1,7)){
                    return "OK";
                }
                return "failed";
            default:
                Log.e("sudoku","unknown sudoku type in checkBoard");
        }
        return "OK";
    }

    @Override
    public int getHash() {
        return 0;
    }

    @Override
    public JSONObject toJson() {
        return null;
    }
    public Boolean isSameBoard(SudokuBoard board){
        int maxX = getMaxX(sudokuType);
        int maxY = getMaxY(sudokuType);
        if(sudokuType != board.sudokuType){
            return false;
        }
        for(int x=0; x<maxX; x++){
            for(int y=0; y<maxY; y++){
                if(pieces[x][y].getNumber() != board.pieces[x][y].getNumber()){
                    return false;
                }
            }
        }
        return true;
    }
    public SudokuBoard toInitialBoard(){
        //复制一个新的board，仅保留数字大于0，modifiable=false的piece
        SudokuPiece[][] newPieces = new SudokuPiece[maxX][maxY];
        for(int x=0; x<maxX; x++){
            for(int y=0; y<maxY; y++){
                if(pieces[x][y].getNumber() > 0 && pieces[x][y].isModifiable() == false) {
                    newPieces[x][y] = new SudokuPiece(pieces[x][y].getNumber(), SudokuPiece.PIECE_SUDOKU_BOARD, x, y);
                }else{
                    newPieces[x][y] = new SudokuPiece(0, SudokuPiece.PIECE_SUDOKU_BOARD, x, y);
                }
            }
        }
        SudokuBoard newBoard = new SudokuBoard(name,sudokuType,newPieces);
        newBoard.seconds = seconds;
        return newBoard;
    }
    public String toDBString() {
        //第一个数字为type，后面数字为每一个单元格的数字
        StringBuffer stringBuffer = new StringBuffer(200);
        //名字
        stringBuffer.append(name+"|");
        //type
        stringBuffer.append(String.format("%d|",sudokuType.toInt()));
        //board的大数字
        for(int x=0; x<getMaxX(sudokuType); x++){
            for(int y=0; y<getMaxY(sudokuType); y++){
                stringBuffer.append(pieces[x][y].getNumber());
            }
        }
        return stringBuffer.toString();
    }
    public static SudokuBoard fromDBString(String string){
        String name = "";
        SudokuType sudokuType;
        SudokuPiece[][] pieces;
        String[] strings = string.split("\\|");
        if(strings.length == 3){
            name = strings[0];
            sudokuType = SudokuType.toEnum(Integer.valueOf(strings[1]));
            pieces =  SudokuBoard.piecesFromString(sudokuType,strings[2]);
        }else{
            int type = Integer.valueOf(String.valueOf(string.charAt(0)));
            sudokuType = SudokuType.toEnum(type);
            pieces = SudokuBoard.piecesFromString(sudokuType,string.substring(1));
        }
        if(sudokuType == null){
            Log.e("sudoku",String.format("unknown sudokutype"));
            return null;
        }

        return new SudokuBoard(name,sudokuType,pieces);
    }
    private String piecesToString(){
        //pieces的大数字转换为string
        StringBuffer stringBuffer = new StringBuffer(200);
        for(int x=0; x<getMaxX(sudokuType); x++){
            for(int y=0; y<getMaxY(sudokuType); y++){
                stringBuffer.append(pieces[x][y].getNumber());
            }
        }
        return stringBuffer.toString();
    }
    public static SudokuPiece[][] piecesFromString(SudokuType sudokuType,String string){
        int maxX = SudokuBoard.getMaxX(sudokuType);
        int maxY = SudokuBoard.getMaxY(sudokuType);
        if(string.length() != maxX*maxY){
            Log.e("sudoku",String.format("length = %d",string.length()));
            return null;
        }
        SudokuPiece[][] pieces = new SudokuPiece[maxX][maxY];
        for(int i=0; i<string.length(); i++){
            int x = i / maxY;
            int y = i % maxY;
            int number = Integer.valueOf(String.valueOf(string.charAt(i)));
            //Log.v("sudoku",String.format("%d,%d:%d",x,y,number));
            if(number == 0) {
                pieces[x][y] = new SudokuPiece(0,Piece.PIECE_SUDOKU_BOARD,x,y);
            }else{
                pieces[x][y] = new SudokuPiece(number,Piece.PIECE_SUDOKU_BOARD,x,y,false);
            }
        }
        return pieces;
    }
    public String toGoingDBString(){
        StringBuffer stringBuffer = new StringBuffer(1000);
        //名字
        stringBuffer.append(String.format("%s|",name));
        //type
        stringBuffer.append(String.format("%d|",sudokuType.toInt()));
        //pieces(含备选小数字)
        if (pieces != null) {
            for (int x=0; x<maxX; x++) {
                for(int y=0; y<maxY; y++) {
                    stringBuffer.append(pieces[x][y].toDBString()+"|");
                }
            }
        }
        return stringBuffer.toString();
    }
    public static SudokuBoard fromGoingDBString(String string) {
        if (string.equals("")) { return null; }
        String[] strings = string.split("\\|");
        if (strings.length <= 1) { return null; }
        //名字
        String name = strings[0];
        //type
        SudokuType sudokuType = SudokuType.toEnum(Integer.valueOf(strings[1]));
        if (sudokuType == null){
            return  null;
        }
        int maxX = getMaxX(sudokuType);
        int maxY = getMaxY(sudokuType);
        /*if(strings.length != maxX*maxY){

        }*/
        SudokuPiece[][] pieces = new SudokuPiece[maxX][maxY];
        for(int i=2; i<strings.length; i++){
            SudokuPiece piece = SudokuPiece.fromDBString(strings[i]);
            if(piece == null){
                Log.v("sudoku",String.format("piece is null from %d: %s ",i,strings[i]));
                break;
            }
            pieces[piece.x][piece.y] = piece;
            //piece.printPiece();
        }

        return new SudokuBoard(name,sudokuType, pieces);
    }
    public DBBoard toDBBoard(){
        return new DBBoard(name,GameType.SUDOKU,toDBString(),seconds,"");
    }
    public DBBoard toGoingDBBoard(){
        return new DBBoard(DBBoard.DBBOARD_TYPE_GOING,name,GameType.SUDOKU,toGoingDBString(),seconds,"");
    }
    public void setPiece(SudokuPiece piece, int x, int y){
        //(x,y)位置，设置新的piece中的数字
        //如果piece.type 是大数字，则直接设置(x,y).number
        //如果piece.type 是小数字，则增加(x,y).miniNumber
        Log.v("board",String.format("setPiece:(%d,%d)",x,y));
        if(piece.type == Piece.PIECE_SUDOKU_NUMBER){
            if(pieces[x][y].getNumber() == piece.getNumber()){
                pieces[x][y].setNumber(0);
            }else {
                pieces[x][y].setNumber(piece.getNumber());
            }
        }else if(piece.type == Piece.PIECE_SUDOKU_MINI_NUMBER){
            if(pieces[x][y].haveMiniNumber(piece.getNumber())){
                pieces[x][y].delMiniNumber(piece.getNumber());
            }else {
                pieces[x][y].addMiniNumber(piece.getNumber());
            }
        }else{
            return ;
        }
    }
    public void setNumber(int x,int y,int number){
        //(x,y)单元格设置为number
        //如果单元格=number，就删除该number，否则就置为该number
        if(!pieces[x][y].isModifiable()){
            return;
        }
        if(number == pieces[x][y].getNumber()){
            pieces[x][y].setNumber(0);
        }else{
            pieces[x][y].setNumber(number);
        }
    }

    //检查是否有解及唯一解
    public int checkSolution(){
        SudokuBoard startBoard = (SudokuBoard)copyBoard();
        SudokuFindSolution findSolution = new SudokuFindSolution(startBoard);
        findSolution.findSolution();
        if(findSolution.solutionCount() == 1){
            return CHECK_SOLUTION_OK;
        }else if(findSolution.solutionCount() == 0){
            Log.v("sudoku","错误：无解");
            return CHECK_SOLUTION_NO;
        }else{
            Log.v("sudoku","there is 1+ solution");
            findSolution.printSolution();
            return CHECK_SOLUTION_NOT_ONLY;
        }
    }
    //检查每一行，每一列，每一宫格数字是否重复
    public Boolean isPiecesUnique(){
        //首先检查所有数字都已经填上，并且数字都小于maxNumber
        int maxNumber = getMaxNumber(sudokuType);
        for(int x=0; x<maxNumber; x++){
            for(int y=0; y<maxNumber; y++){
                if (pieces[x][y].getNumber() <= 0 || pieces[x][y].getNumber() > maxNumber){
                    return false;
                }
            }
        }
        //每行数字是否重复
        for(int y=0; y<maxNumber; y++){
            int[] numbers = new int[maxNumber];
            for(int x=0; x<maxNumber; x++){
                numbers[x] = pieces[x][y].getNumber();
            }
            if (!isNumbersUnique(numbers)){
                return false;
            }
        }
        //每列数字是否重复
        for(int x=0; x<maxNumber; x++){
            int[] numbers = new int[maxNumber];
            for(int y=0; y<maxNumber; y++){
                numbers[y] = pieces[x][y].getNumber();
            }
            if (!isNumbersUnique(numbers)){
                return false;
            }
        }
        //检查每个小宫格的数字是否重复
        int squareOfX = getNumberOfSquareX(sudokuType); //宫格X的个数，宫格Y的个数
        int squareOfY = getNumberOfSquareY(sudokuType);
        int numberOfX = getMaxXOfSquare(sudokuType);
        int numberOfY = getMaxYOfSquare(sudokuType); //每一宫格x的个数,y的个数
        for(int X = 0; X< squareOfX; X++){
            for(int Y = 0; Y< squareOfY; Y++){
                int[] numbers = new int[maxNumber];
                int startX =  X * numberOfX;
                int startY =  Y * numberOfY;
                for(int i=0; i<maxNumber; i++){
                    int x = startX + i % numberOfX;
                    int y = startY + i / numberOfX;
                    numbers[i] = pieces[x][y].getNumber();
                }
                if (!isNumbersUnique(numbers)){
                    return false;
                }
            }
        }
        return true;
    }
    private Boolean isNumbersUnique(int[] numbers){
        //检查numbers的数字是否为从1-numbers.length，不重复
        for(int i=0; i<numbers.length; i++){
            if (numbers[i] <=0 || numbers[i] > numbers.length){
                return false;
            }
            for(int j=0; j<i; j++){
                if(numbers[i] == numbers[j]){
                    return  false;
                }
            }
        }
        return true;
    }
    private Boolean isNumbersUnique(List<Integer> numbers){
        //检查numbers的数字是否不重复
        if(numbers.size() <= 2){
            return true;
        }
        for(int i=0; i<numbers.size(); i++){
            for(int j=0; j<i; j++){
                if(numbers.get(i) == numbers.get(j)){
                    return  false;
                }
            }
        }
        return true;
    }
    private Boolean isPiecesUnique(List<SudokuPiece> pieceList){
        //检查pieceList的数字是否不重复
        if(pieceList.size() <= 2){
            return true;
        }
        for(int i=0; i<pieceList.size(); i++){
            if(pieceList.get(i).getNumber() == 0){
                continue;
            }
            for(int j=0; j<i; j++){
                if(pieceList.get(i).getNumber() == pieceList.get(j).getNumber()){
                    return  false;
                }
            }
        }
        return true;
    }
    //(x,y)单元格增加mininumber,如果单元格已经包含该miniNumber，就删除它，否则就增加它
    public void addMiniNumber(int x,int y, int number){
        if(pieces[x][y].haveMiniNumber(number)) {
            Log.v("sdoku","del mininumber"+String.valueOf(number));
            pieces[x][y].delMiniNumber(number);
        }else{
            pieces[x][y].addMiniNumber(number);
        }
    }

    //找提示
    public List<SudokuPiece> findHint(){
        //找提示，返回piece数组，在view中提示
        checkMiniNumbers();   // 找提示之前，先检查和bignumber冲突的miniNumnber
        List<SudokuPiece> pieceList = null;

        // 1、找备选数字中是唯一的。
        pieceList = findUniqueMiniNumber();
        if(pieceList != null){
            Log.v("sudoku","找到唯一数字提示");
            return pieceList;
        }
        // 2、找同一系列（行，列，宫格等）中多选数字唯一的。
        pieceList = findUniqueMiniNumbers();
        if(pieceList != null){
            Log.v("sudoku","找到多选数字提示");
            return pieceList;
        }

        for(int n=1; n<=getMaxNumber(sudokuType); n++){
            //宫格中miniNumber仅在同一行出现
            pieceList = findMiniInLineOfSquare(n);
            if(pieceList != null && pieceList.size() > 0){
                return pieceList;
            }
            //宫格中miniNumber仅在同一列出现
            pieceList = findMiniInColumnOfSquare(n);
            if(pieceList != null && pieceList.size() > 0){
                return pieceList;
            }
            //宫格中miniNumber仅在对角线出现
            pieceList = findMiniInDiagnoalOfSquare(n);
            if(pieceList != null && pieceList.size() > 0){
                return pieceList;
            }
        }
        Log.v("sudoku","没找到提示");
        return  null;
    }
    //找同一行/列/对角线/宫格等唯一的备选数值
    public List<SudokuPiece> findUniqueMiniNumber(){
        //找出备选数字唯一的所有单元，以及同一系列(行，列，等)某一备选数字仅在一个单元格存在的所有单元。
        List<SudokuPiece> pieceList = new ArrayList<>();
        int maxX = getMaxX(sudokuType);
        int maxY = getMaxY(sudokuType);

        for(int x=0; x<maxX; x++){
            for(int y=0; y<maxY; y++){
                if(pieces[x][y].getMiniNumbers() == null){
                    continue;
                }
                if(pieces[x][y].getNumber() == 0 && pieces[x][y].getMiniNumbers().length == 1){
                    pieceList.add(pieces[x][y]);
                }
            }
        }
        //找出同一系列(行，列，等)某一备选数字仅在一个单元格存在的所有单元。
        //TODO
        if(pieceList.size() == 0){
            return null;
        }else{
            return pieceList;
        }
    }
    //找同一行/列/对角线/宫格等，唯N组合数字仅在唯N单元存在的。
    public List<SudokuPiece> findUniqueMiniNumbers(){
        //找出备选数字组合"唯N”的单元格s，例如258,25,28,2358,
        int maxX = getMaxX(sudokuType);
        int maxY = getMaxY(sudokuType);
        List<SudokuPiece> pieceList;

        //同一行
        for(int y=9; y<maxY; y++){
            pieceList = new ArrayList<>();
            for(int x=0; x<maxX; x++){
                if(pieces[x][y].getNumber() > 0){
                    continue;
                }
                pieceList.add(pieces[x][y]);
            }
            /*if(pieceList.size() <= 2){
                continue;
            }*/
            if(findUniqueMiniNumbers(pieceList) != null){
                return findUniqueMiniNumbers(pieceList);
            }
        }
        //同一列
        for(int x=0; x<maxX; x++){
            pieceList = new ArrayList<>();
            for(int y=0; y<maxY; y++){
                if(pieces[x][y].getNumber() > 0){
                    continue;
                }
                pieceList.add(pieces[x][y]);
            }
            //Log.v("debug",String.format("x=%d",x));
            if(findUniqueMiniNumbers(pieceList) != null){
                return findUniqueMiniNumbers(pieceList);
            }
        }
        //同一宫格
        int numberOfSquareX = getNumberOfSquareX(sudokuType);
        int numberOfSquareY = getNumberOfSquareY(sudokuType);
        for(int x=0; x<numberOfSquareX; x++){
            for(int y=0; y<numberOfSquareY; y++){
                int piece_x = x*getMaxXOfSquare(sudokuType);
                int pice_y = y*getMaxYOfSquare(sudokuType);
                pieceList = removeBigNumberPiece(getSquare(piece_x,pice_y));
                if(findUniqueMiniNumbers(pieceList) != null){
                    return findUniqueMiniNumbers(pieceList);
                }
            }
        }
        switch(sudokuType){
            case FOUR:
            case SIX:
            case NINE:
                return null;
            case X_STYLE:
                //同一对角线一
                pieceList = removeBigNumberPiece(getDiagonal(0,0));
                if(findUniqueMiniNumbers(pieceList) != null){
                    return findUniqueMiniNumbers(pieceList);
                }
                //同一对角线二
                pieceList = removeBigNumberPiece(getDiagonal(0,8));
                if(findUniqueMiniNumbers(pieceList) != null){
                    return findUniqueMiniNumbers(pieceList);
                }
                return null;
            case PERCENT:
                //同一百分比对角线
                pieceList = removeBigNumberPiece(getPercentDiagonal(0,0));
                if(findUniqueMiniNumbers(pieceList) != null){
                    return findUniqueMiniNumbers(pieceList);
                }
                //同一百分比宫格一
                pieceList = removeBigNumberPiece(getPercentSquare(2,6));
                if(findUniqueMiniNumbers(pieceList) != null){
                    return findUniqueMiniNumbers(pieceList);
                }
                //同一百分比宫格二
                pieceList = removeBigNumberPiece(getPercentSquare(6,3));
                if(findUniqueMiniNumbers(pieceList) != null){
                    return findUniqueMiniNumbers(pieceList);
                }
                return null;
            case SUPER:
                //左下宫格
                pieceList = removeBigNumberPiece(getSuperSquare(1,1));
                if(findUniqueMiniNumbers(pieceList) != null){
                    return findUniqueMiniNumbers(pieceList);
                }
                //左上宫格
                pieceList = removeBigNumberPiece(getSuperSquare(1,7));
                if(findUniqueMiniNumbers(pieceList) != null){
                    return findUniqueMiniNumbers(pieceList);
                }
                //右下宫格
                pieceList = removeBigNumberPiece(getSuperSquare(7,1));
                if(findUniqueMiniNumbers(pieceList) != null){
                    return findUniqueMiniNumbers(pieceList);
                }
                //右上宫格
                pieceList = removeBigNumberPiece(getSuperSquare(7,7));
                if(findUniqueMiniNumbers(pieceList) != null){
                    return findUniqueMiniNumbers(pieceList);
                }
                return null;
            default:
                return null;
        }
    }
    //找某一备选数字，仅在一个宫格中同一行存在的
    public List<SudokuPiece> findMiniInLineOfSquare(int miniNumber){
        //备选数字miniNumber
        //同一个宫格中，只有同一行才有备选数字miniNumber，如果宫格以外的同行其他位置存在备选数字，（那么删除本行其他宫格的备选数字。）
        // 举例：1,1,1,1,1,1,1,1,1
        //                  x,x,x
        //                  x,x,x


        List<SudokuPiece> outPieceList; //输出的满足条件的pieceList
        int maxY = SudokuBoard.getMaxY(sudokuType);
        int maxX = SudokuBoard.getMaxX(sudokuType);
        Log.v("debug",String.format("mini=%d,maxY=%d",miniNumber,maxY));
        for(int y=0; y<maxY; y++){  //逐行分析
            int maxXOfSquare = getMaxXOfSquare(sudokuType);
            int numberOfSquare = maxX/maxXOfSquare;
            Log.v("debug",String.format("  y=%d",y));
            //逐个宫格找是否有满足条件的宫格
            for(int i=0; i<numberOfSquare; i++){
                //获取该宫格的所有piece
                List<SudokuPiece> pieceList = getSquare(i*maxXOfSquare,y);
                //检查宫格所有piece中,除了y行以外，是否还包含该备选数字。
                Log.v("debug",String.format("        square:%s",pieceListToString(pieceList)));
                if(!numberExistInList(pieceList,miniNumber) && !isOtherPlaceHaveMiniExcludeLine(pieceList,y,miniNumber)){
                    outPieceList = getPiecesByLine(pieceList,y,miniNumber);
                    //检查除了outPieceList中的piece以外，本行其他位置是否包含miniNumber
                    Log.v("debug",String.format("        outPieces:%s",pieceListToString(outPieceList)));
                    for(int x=0; x<maxX; x++){
                        if(pieces[x][y].getNumber() > 0 || isInPieceList(outPieceList,pieces[x][y])){
                            continue;
                        }
                        if(pieces[x][y].haveMiniNumber(miniNumber)){
                            Log.v("debug",String.format("        return:%s",pieceListToString(outPieceList)));
                            return outPieceList;
                        }
                    }
                }
            }

            //百分比宫格，也是如此
            if(sudokuType == SudokuType.PERCENT){
                //获取本行的百分比宫格
                List<SudokuPiece> pieceList = getPercentSquareOfLine(y);
                if(pieceList == null){
                    continue;
                }
                //该宫格除了本行以外其他位置不包含备选数字mininumber
                if(!numberExistInList(pieceList,miniNumber) && !isOtherPlaceHaveMiniExcludeLine(pieceList,y,miniNumber)){
                    outPieceList = getPiecesByLine(pieceList,y,miniNumber);
                    //除了该宫格以外，本行其他位置还包含了备选数字
                    for(int x=0; x<maxX; x++){
                        if(pieces[x][y].getNumber() > 0 || isInPieceList(outPieceList,pieces[x][y])){
                            continue;
                        }
                        if(pieces[x][y].haveMiniNumber(miniNumber)){
                            return outPieceList;
                        }
                    }
                }
            }
            if(sudokuType == SudokuType.SUPER){
                //获取本行的左super宫格
                List<SudokuPiece> pieceList = getSuperSquareOfLine(y,Direction.LEFT);
                if(pieceList == null){
                    continue;
                }
                //该宫格除了本行以外其他位置不包含备选数字mininumber
                if(!numberExistInList(pieceList,miniNumber) && !isOtherPlaceHaveMiniExcludeLine(pieceList,y,miniNumber)){
                    outPieceList = getPiecesByLine(pieceList,y,miniNumber);
                    //除了该宫格以外，本行其他位置还包含了备选数字
                    for(int x=0; x<maxX; x++){
                        if(pieces[x][y].getNumber() > 0 || isInPieceList(outPieceList,pieces[x][y])){
                            continue;
                        }
                        if(pieces[x][y].haveMiniNumber(miniNumber)){
                            return outPieceList;
                        }
                    }
                }
                //获取本行的右super宫格
                pieceList = getSuperSquareOfLine(y,Direction.RIGHT);
                if(pieceList == null){
                    continue;
                }
                //该宫格除了本行以外其他位置不包含备选数字mininumber
                if(!numberExistInList(pieceList,miniNumber) && !isOtherPlaceHaveMiniExcludeLine(pieceList,y,miniNumber)){
                    outPieceList = getPiecesByLine(pieceList,y,miniNumber);
                    //除了该宫格以外，本行其他位置还包含了备选数字
                    for(int x=0; x<maxX; x++){
                        if(pieces[x][y].getNumber() > 0 || isInPieceList(outPieceList,pieces[x][y])){
                            continue;
                        }
                        if(pieces[x][y].haveMiniNumber(miniNumber)){
                            return outPieceList;
                        }
                    }
                }
            }
        }
        return null;
    }
    //找某一备选数字，仅在一个宫格中同一列存在的
    public List<SudokuPiece> findMiniInColumnOfSquare(int miniNumber){
        //备选数字miniNumber
        //同一个宫格中，只有同一列才有备选数字miniNumber，如果宫格以外的同列其他位置存在备选数字，（那么删除本列其他宫格的备选数字。）
        // 举例： 1, x
        //       1， x
        //       1，
        //       1,

        List<SudokuPiece> outPieceList; //输出的满足条件的pieceList
        int maxY = SudokuBoard.getMaxY(sudokuType);
        int maxX = SudokuBoard.getMaxX(sudokuType);
        for(int x=0; x<maxX; x++){  //逐列分析
            int maxYOfSquare = getMaxYOfSquare(sudokuType);
            int numberOfSquare = maxY/maxYOfSquare;
            //逐个宫格找是否有满足条件的宫格
            for(int i=0; i<numberOfSquare; i++){
                //获取该宫格的所有piece
                List<SudokuPiece> pieceList = getSquare(x,i*maxYOfSquare);
                //检查宫格所有piece中,除了y行以外，是否还包含该备选数字。
                if(!numberExistInList(pieceList,miniNumber) && !isOtherPlaceHaveMiniExcludeColumn(pieceList,x,miniNumber)){
                    outPieceList = getPiecesByColumn(pieceList,x,miniNumber);
                    //检查除了outPieceList中的piece以外，本行其他位置是否包含miniNumber
                    for(int y=0; y<maxX; y++){
                        if(pieces[x][y].getNumber() > 0 || isInPieceList(outPieceList,pieces[x][y])){
                            continue;
                        }
                        if(pieces[x][y].haveMiniNumber(miniNumber)){
                            return outPieceList;
                        }
                    }
                }
            }

            //百分比宫格，也是如此
            if(sudokuType == SudokuType.PERCENT){
                //获取本列的百分比宫格
                List<SudokuPiece> pieceList = getPercentSquareOfColumn(x);
                if(pieceList == null){
                    continue;
                }
                //该宫格除了本列以外其他位置不包含备选数字mininumber
                if(!numberExistInList(pieceList,miniNumber) && !isOtherPlaceHaveMiniExcludeColumn(pieceList,x,miniNumber)){
                    outPieceList = getPiecesByColumn(pieceList,x,miniNumber);
                    //除了该宫格以外，本列其他位置还包含了备选数字
                    for(int y=0; y<maxY; y++){
                        if(pieces[x][y].getNumber() > 0 || isInPieceList(outPieceList,pieces[x][y])){
                            continue;
                        }
                        if(pieces[x][y].haveMiniNumber(miniNumber)){
                            return outPieceList;
                        }
                    }
                }
            }
            if(sudokuType == SudokuType.SUPER){
                //获取本列的上super宫格
                List<SudokuPiece> pieceList = getSuperSquareOfColumn(x,Direction.UP);
                if(pieceList == null){
                    continue;
                }
                //该宫格除了本列以外其他位置不包含备选数字mininumber
                if(!numberExistInList(pieceList,miniNumber) && !isOtherPlaceHaveMiniExcludeColumn(pieceList,x,miniNumber)){
                    outPieceList = getPiecesByColumn(pieceList,x,miniNumber);
                    //除了该宫格以外，本行其他位置还包含了备选数字
                    for(int y=0; y<maxY; y++){
                        if(pieces[x][y].getNumber() > 0 || isInPieceList(outPieceList,pieces[x][y])){
                            continue;
                        }
                        if(pieces[x][y].haveMiniNumber(miniNumber)){
                            return outPieceList;
                        }
                    }
                }
                //获取本列的下super宫格
                pieceList = getSuperSquareOfColumn(x,Direction.DOWN);
                if(pieceList == null){
                    continue;
                }
                //该宫格除了本列以外其他位置不包含备选数字mininumber
                if(!numberExistInList(pieceList,miniNumber) && !isOtherPlaceHaveMiniExcludeColumn(pieceList,x,miniNumber)){
                    outPieceList = getPiecesByColumn(pieceList,x,miniNumber);
                    //除了该宫格以外，本行其他位置还包含了备选数字
                    for(int y=0; y<maxY; y++){
                        if(pieces[x][y].getNumber() > 0 || isInPieceList(outPieceList,pieces[x][y])){
                            continue;
                        }
                        if(pieces[x][y].haveMiniNumber(miniNumber)){
                            return outPieceList;
                        }
                    }
                }
            }
        }
        return null;
    }
    //找某一备选数字，仅在一个宫格中的对角线存在的
    public List<SudokuPiece> findMiniInDiagnoalOfSquare(int miniNumber){
        List<SudokuPiece> outPieceList;
        if(sudokuType == SudokuType.PERCENT || sudokuType == SudokuType.X_STYLE) {
            //百分比对角线
            for (int i=0; i<3; i++) {
                List<SudokuPiece> pieceList = getSquare(i * 3, i * 3);
                if (!isOtherPlaceHaveMiniExcludePercentDiagnoal(pieceList, miniNumber)) {
                    outPieceList = getPiecesByPercentDiagnoal(pieceList,miniNumber);
                    //检查除了outPieceList中的piece以外，该对角线其他位置是否包含备选数字
                    for (int j = 0; j < 9; j++) {
                        if (pieces[j][j].getNumber() > 0 || isInPieceList(outPieceList, pieces[j][j])) {
                            continue;
                        }
                        if (pieces[j][j].haveMiniNumber(miniNumber)) {
                            return outPieceList;
                        }
                    }
                }
            }
        }
        if(sudokuType == SudokuType.X_STYLE){
            //百分比对角线
            for (int i=0; i<3; i++) {
                List<SudokuPiece> pieceList = getSquare(i * 3, 8 - i * 3);
                if (!numberExistInList(pieceList,miniNumber) && !isOtherPlaceHaveMiniExcludeXDiagnoal(pieceList, miniNumber)) {
                    outPieceList = getPiecesByXDiagnoal(pieceList,miniNumber);
                    //检查除了outPieceList中的piece以外，该对角线其他位置是否包含备选数字
                    for (int j = 0; j < 9; j++) {
                        if (pieces[j][8-j].getNumber() > 0 || isInPieceList(outPieceList, pieces[j][8-j])) {
                            continue;
                        }
                        if (pieces[j][8-j].haveMiniNumber(miniNumber)) {
                            return outPieceList;
                        }
                    }
                }
            }
        }
      return null;
    }
    //去除pieceList中已经填写bigNumber的piece
    private List<SudokuPiece> removeBigNumberPiece(List<SudokuPiece> pieceList){
        if(pieceList == null){
            return null;
        }
        List<SudokuPiece> newList = new ArrayList<>();
        for(int i=0; i<pieceList.size(); i++){
            if(pieceList.get(i).getNumber() == 0){
                newList.add(pieceList.get(i));
            }
        }
        if(newList.size() == 0){
            return null;
        }
        return newList;
    }
    private List<SudokuPiece> findUniqueMiniNumbers(List<SudokuPiece> inPieceList){
        // 内部函数,输入的piece列表中，如果存在N个备选数字仅在N个单元中存在(N>=2 && N<PieceList.size())。
        // 同时这些单元格中不存在其他备选数字，并且其他单元格存在这些备选数字，例如：28 28 128

        //单元格至少>=3才有效
        if(inPieceList == null || inPieceList.size() <= 2){
            return null;
        }
        // 1,首先找出所有数字,组成一个数字链表
        List<Integer> numberList = new ArrayList<>();
        for(int i=0; i<inPieceList.size(); i++){
            int[] miniNumbers = inPieceList.get(i).getMiniNumbers();
            if(miniNumbers == null){
                 continue;
            }
            for(int j=0; j<miniNumbers.length; j++){
                if(Numbers.isInNumberList(numberList,miniNumbers[j])){
                    continue;
                }
                numberList.add(miniNumbers[j]);
            }
        }
        if(numberList == null){
            return null;
        }
        //debug
        /*String str = "";
        if(numberList != null) {
            for (int i = 0; i < numberList.size(); i++) {
                str += String.valueOf(numberList.get(i));
            }
            Log.v("debug", "numberList:" + str);
        }*/
        // 2,找出所有可能的2到N-1的数字组合(N为数字总个数)
        List<Numbers> numbersList = new ArrayList<>();
        for(int n=2; n<numberList.size(); n++){ //从 2 到 N-1
            //找出n个数字的组合
            List<Numbers> newNumbersList = Numbers.findAllCombination(numberList,n);
            //把这个组合加入链表
            numbersList = Numbers.addNumbersList(numbersList,newNumbersList);
        }
        /*//debug
        if(numbersList != null) {
            str = "";
            for (int i = 0; i < numbersList.size(); i++) {
                str += numbersList.get(i).toString() + ",";
            }
        }
        Log.v("debug","numbersList="+str);*/

        // 3，找符合numbersList中组合的单元格。
        if(numbersList == null || numbersList.size() == 0){
            Log.e("sudoku","数字组合为空");
            return null;
        }
        //逐个组合进行分析pieceList
        for(int i=0; i<numbersList.size(); i++){
            Numbers numbers = numbersList.get(i);
            if(numbers.count() == 0){
                Log.e("sudoku","组合中的数字个数为0");
                continue;
            }
            int countOfInclude = 0; //pieceList中包含数字组合中的数字的piece个数
            List<SudokuPiece> pieceList = new ArrayList<>();
            for(int j=0; j<inPieceList.size(); j++){
                SudokuPiece piece = inPieceList.get(j);
                if(piece.getNumber() > 0){
                    continue;
                }
                if(piece.getMiniNumbers() == null || piece.getMiniNumbers().length == 0){
                    continue;
                }
                Boolean onlyInclude = true; //是否只包含组合数字
                Boolean include = false; //包含了组合数字。
                int[] miniNumbers = piece.getMiniNumbers();
                for(int k=0; k<miniNumbers.length; k++){
                    if(numbers.include(miniNumbers[k])){ //该miniNumber[k]包含在numbers组合中
                        include = true;
                    }else{ //不包含
                        onlyInclude = false;
                   }
                }
                if(include){
                    countOfInclude += 1;
                }
                if(onlyInclude){
                    pieceList.add(piece);
                }
            }
            if (pieceList.size() == numbers.count()){
                //仅包含组合numbers的piece个数恰好等于numbers的个数
                if(countOfInclude > numbers.count()){
                    //同时还有其他单元格包含numbers的数字，例如25,25,238,38
                    //找到了有效的唯一数字组合。25,25
                    return pieceList;
                }
            }
        }
        return null;
    }

    public void fillMiniNumbers(){
        //填入所有备选数字
        int maxX = getMaxX(sudokuType);
        int maxY = getMaxY(sudokuType);
        for(int x=0; x<maxX; x++){
            for(int y=0; y<maxY; y++){
                fillMiniNumbers(pieces[x][y]);
            }
        }
        checkMiniNumbers();
    }
    private void fillMiniNumbers(SudokuPiece piece){
        //piece填入所有可能的miniNumber
        /*if(piece.getNumber() > 0){

        }*/
        for(int i=1; i<=getMaxNumber(sudokuType); i++){
            piece.addMiniNumber(i);
        }
    }
    public void checkMiniNumbers(){
        //检查和bignumber冲突的备选数字，删除它
        for(int x=0; x<getMaxX(sudokuType); x++){
            for(int y=0; y<getMaxY(sudokuType); y++){
                checkMiniNumbers(pieces[x][y]);
            }
        }
    }
    private void checkMiniNumbers(SudokuPiece piece){
        //检查piece的mininumber，如果和系列（行，列，宫格等）的大数字冲突，就删除

        if(piece.getNumber() > 0){
            return;
        }
        if(piece.getMiniNumbers() == null){
            return;
        }
        int[] miniNumbers = piece.getMiniNumbers();
        for(int i=0; i<miniNumbers.length; i++){
            //逐个检查备选数字
            //行，列，宫格
            if(numberExistRow(piece,miniNumbers[i])
                    || numberExistColumn(piece,miniNumbers[i])
                    || numberExistSquare(piece,miniNumbers[i])){
                piece.delMiniNumber(miniNumbers[i]);
                continue;
            }
            switch(sudokuType){
                case FOUR:
                case SIX:
                case NINE:
                    continue;
                case X_STYLE:
                    //检查对角线
                    if(numberExistDiagonal(piece,miniNumbers[i])){
                        piece.delMiniNumber(miniNumbers[i]);
                    }
                    break;
                case PERCENT:
                    if(numberExistPercentDiagonal(piece,miniNumbers[i])
                            || numberExistPercentSquare(piece,miniNumbers[i])){
                        piece.delMiniNumber(miniNumbers[i]);
                    }
                    break;
                case SUPER:
                    if(numberExistSuperSquare(piece,miniNumbers[i])){
                        piece.delMiniNumber(miniNumbers[i]);
                    }
            }
        }
    }
    public Boolean isNumberUnique(SudokuPiece piece){
        if(!isNumberUniqueRow(piece) || !isNumberUniqueColumn(piece) || !isNumberUniqueSquare(piece)){
            return false;
        }
        if(sudokuType == SudokuType.X_STYLE && !isNumberUniqueDiagonal(piece)){
            return false;
        }
        if(sudokuType == SudokuType.PERCENT && !isNumberUniquePercent(piece)){
            return false;
        }
        if(sudokuType == SudokuType.SUPER && !isNumberUniqueSuperSquare(piece)){
            return false;
        }
        return true;
    }
    public Boolean isNumberUniqueRow(SudokuPiece piece){
        //piece的bigNumber在同行中是否唯一。
        if(piece.getNumber() == 0){
            return false;
        }
        int y = piece.y;
        for(int x=0; x<getMaxX(sudokuType); x++){
            if(x != piece.x && pieces[x][y].getNumber() == piece.getNumber()){
                return false;
            }
        }
        return true;
    }
    public Boolean isNumberUniqueColumn(SudokuPiece piece) {
        //piece的bigNumber在同列中是否唯一。
        if (piece.getNumber() == 0) {
            return false;
        }
        int x = piece.x;
        for (int y = 0; y < getMaxY(sudokuType); y++) {
            if (y != piece.y && pieces[x][y].getNumber() == piece.getNumber()) {
                return false;
            }
        }
        return true;
    }
    public Boolean isNumberUniqueSquare(SudokuPiece piece){
        //piece的bignumber在同宫格中是否唯一
        if(piece.getNumber() == 0){
            return false;
        }
        List<SudokuPiece> pieceList = getSquare(piece.x,piece.y);
        for(int i=0; i<pieceList.size(); i++){
            if(pieceList.get(i).getNumber() == piece.getNumber()
                    && (pieceList.get(i).x != piece.x || pieceList.get(i).y != piece.y)){
                return false;
            }
        }
        return true;
    }
    public Boolean isNumberUniqueDiagonal(SudokuPiece piece){
        //piece所在的对角线上，piece.number是否唯一
        if(piece.getNumber() == 0){
            return false;
        }
        List<SudokuPiece> pieceList = getDiagonal(piece.x,piece.y);
        if(pieceList != null){
            for(int i=0; i<pieceList.size(); i++){
                if(pieceList.get(i).getNumber() == piece.getNumber()
                        && (pieceList.get(i).x != piece.x || pieceList.get(i).y != piece.y)  ){
                    return false;
                }
            }
        }
        return true;
    }
    public Boolean isNumberUniquePercent(SudokuPiece piece){
        //piece所在百分比宫格及百分比对角线上，piece.number是否唯一
        if(piece.getNumber() == 0){
            return false;
        }
        //百分比对角线上是否唯一
        List<SudokuPiece> pieceList = getPercentDiagonal(piece.x, piece.y);
        if(pieceList != null){
            for(int i=0; i<pieceList.size(); i++){
                if(pieceList.get(i).getNumber() == piece.getNumber()
                    && (pieceList.get(i).x != piece.x || pieceList.get(i).y != piece.y)){
                    return false;
                }
            }
        }
        //所在百分比宫格上是否唯一
        pieceList = getPercentSquare(piece.x,piece.y);
        if(pieceList != null){
            for(int i=0; i<pieceList.size(); i++){
                if(pieceList.get(i).getNumber() == piece.getNumber()
                        && (pieceList.get(i).x != piece.x || pieceList.get(i).y != piece.y)){
                    return false;
                }
            }
        }
        return  true;
    }
    public Boolean isNumberUniqueSuperSquare(SudokuPiece piece){
        //piece所在超级宫格中，是否唯一
        if(piece.getNumber() == 0){
            return false;
        }
        //super宫格上是否唯一
        List<SudokuPiece> pieceList = getSuperSquare(piece.x,piece.y);
        if(pieceList != null){
            for(int i=0; i<pieceList.size(); i++){
                if(pieceList.get(i).getNumber() == piece.getNumber()
                        && (pieceList.get(i).x != piece.x || pieceList.get(i).y != piece.y)){
                    return false;
                }
            }
        }
        return  true;
    }
    public Boolean numberExist(SudokuPiece piece,int number){
        //piece所在的行，列，宫格中是否已经存在number?
        if(numberExistRow(piece,number) || numberExistColumn(piece,number) || numberExistSquare(piece,number)){
            return true;
        }
        if(sudokuType == SudokuType.X_STYLE){
            if(numberExistDiagonal(piece,number)){
                return true;
            }
        }
        if(sudokuType == SudokuType.PERCENT){
            if(numberExistPercentSquare(piece,number) || numberExistPercentDiagonal(piece,number)){
                return true;
            }
        }
        if(sudokuType == SudokuType.SUPER){
            if(numberExistSuperSquare(piece,number)){
                return true;
            }
        }
        return false;
    }
    public Boolean numberExistRow(SudokuPiece piece,int number){
        //piece所在行中是否已经存在number?
        int y = piece.y;
        int maxX = getMaxX(sudokuType);
        for(int x=0; x<maxX; x++){
            if(pieces[x][y].getNumber() == number){
                //Log.v("sudoku",String.format("(%d,%d)=%d",x,y,number));
                return true;
            }
        }
        return false;
    }
    public Boolean numberExistColumn(SudokuPiece piece,int number){
        //piece所在列中是否已经存在number?
        int x = piece.x;
        int maxY = getMaxY(sudokuType);
        for(int y=0; y<maxY; y++){
            if(pieces[x][y].getNumber() == number){
                //Log.v("sudoku",String.format("(%d,%d)=%d",x,y,number));
                return true;
            }
        }
        return false;
    }
    public Boolean numberExistSquare(SudokuPiece piece,int number){
        //number在同宫格中是否存在
        /*List<SudokuPiece> pieceList = getSquare(piece.x,piece.y);
        for(int i=0; i<pieceList.size(); i++){
            if(pieceList.get(i).getNumber() == number){
                //Log.v("sudoku",String.format("(%d,%d)=%d",pieceList.get(i).x,pieceList.get(i).y,number));
                return true;
            }
        }*/
        int maxXOfSquare = getMaxXOfSquare(sudokuType);
        int maxYOfSquare = getMaxYOfSquare(sudokuType);
        int indexOfSquareX = (int)(piece.x/maxXOfSquare);
        int startX = indexOfSquareX * maxXOfSquare;
        int stopX = (indexOfSquareX+1) * maxXOfSquare - 1;
        int indexOfSquareY = (int)(piece.y/maxYOfSquare);
        int startY = indexOfSquareY * maxYOfSquare;
        int stopY = (indexOfSquareY+1) * maxYOfSquare - 1;
        for(int i=startX; i<=stopX; i++){
            for(int j=startY; j<=stopY; j++){
                if(pieces[i][j].getNumber() == number){
                    return true;
                }
            }
        }
        return false;
    }
    public Boolean numberExistDiagonal(SudokuPiece piece,int number){
        //number在对角线中是否存在
        /*List<SudokuPiece> pieceList = getDiagonal(piece.x,piece.y);
        if (pieceList != null){
            for(int i=0; i<pieceList.size(); i++){
                if(pieceList.get(i).getNumber() == number){
                    //Log.v("sudoku",String.format("(%d,%d)=%d",pieceList.get(i).x,pieceList.get(i).y,number));
                    return true;
                }
            }
        }*/
        int maxNumber = getMaxNumber(sudokuType);
        if(piece.x == piece.y){//在对角线一
            for(int i=0; i<maxNumber; i++){
                if(pieces[i][i].getNumber() == number){
                    return true;
                }
            }
        }
        if(piece.x+piece.y == maxNumber-1){
            for(int i=0; i<maxNumber; i++){
                if(pieces[i][maxNumber-1-i].getNumber() == number){
                    return true;
                }
            }
        }
        return false;
    }
    public Boolean numberExistPercentDiagonal(SudokuPiece piece,int number){
        //number在百分比数独的对角线中是否存在
        /*List<SudokuPiece> pieceList = getPercentDiagonal(piece.x,piece.y);
        if (pieceList != null){
            for(int i=0; i<pieceList.size(); i++){
                if(pieceList.get(i).getNumber() == number){
                    //Log.v("sudoku",String.format("(%d,%d)=%d",pieceList.get(i).x,pieceList.get(i).y,number));
                    return true;
                }
            }
        }*/
        if(piece.x != piece.y){  //不在百分比对角线上
            return false;
        }
        int maxNumber = getMaxNumber(sudokuType);
        for(int i=0; i<maxNumber; i++){
            if(pieces[i][i].getNumber() == number){
                return true;
            }
        }
        return false;
    }
    public Boolean numberExistPercentSquare(SudokuPiece piece, int number){
        //大数字number在百分比宫格上是否存在
        /*List<SudokuPiece> pieceList = getPercent(piece.x,piece.y);
        if (pieceList != null){
            for(int i=0; i<pieceList.size(); i++){
                if(pieceList.get(i).getNumber() == number){
                    //Log.v("sudoku",String.format("(%d,%d)=%d",pieceList.get(i).x,pieceList.get(i).y,number));
                    return true;
                }
            }
        }*/
        //左上宫格
        if(piece.x>=1 && piece.x<=3 && piece.y<=7 && piece.y>=5){
            for(int x=1; x<=3; x++){
                for(int y=5; y<=7; y++){
                    if(pieces[x][y].getNumber() == number){
                        return true;
                    }
                }
            }
        }
        //右下宫格
        if(piece.x>=5 && piece.x<=7 && piece.y>=1 && piece.y<=3){
            for(int x=5; x<=7; x++){
                for(int y=1; y<=3; y++){
                    if(pieces[x][y].getNumber() == number){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public Boolean numberExistSuperSquare(SudokuPiece piece,int number){
        //大数字number在super宫格中是否存在

        //左上宫格
        if(piece.x>=1 && piece.x<=3 && piece.y<=7 && piece.y>=5){
            for(int x=1; x<=3; x++){
                for(int y=5; y<=7; y++){
                    if(pieces[x][y].getNumber() == number){
                        return true;
                    }
                }
            }
        }
        //左下宫格
        if(piece.x>=1 && piece.x<=3 && piece.y>=1 && piece.y<=3){
            for(int x=1; x<=3; x++){
                for(int y=1; y<=3; y++){
                    if(pieces[x][y].getNumber() == number){
                        return true;
                    }
                }
            }
        }
        //右上宫格
        if(piece.x>=5 && piece.x<=7 && piece.y>=5 && piece.y<=7){
            for(int x=5; x<=7; x++){
                for(int y=5; y<=7; y++){
                    if(pieces[x][y].getNumber() == number){
                        return true;
                    }
                }
            }
        }
        //右下宫格
        if(piece.x>=5 && piece.x<=7 && piece.y>=1 && piece.y<=3){
            for(int x=5; x<=7; x++){
                for(int y=1; y<=3; y++){
                    if(pieces[x][y].getNumber() == number){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private Boolean numberExistInList(List<SudokuPiece> pieceList,int number){
        //bigNumber是否在pieceList中已经存在
        for(int i=0; i<pieceList.size(); i++){
            if(pieceList.get(i).getNumber() == number){
                return true;
            }
        }
        return false;
    }
    public Boolean checkBigNumberRow(int y){
        //检查y列数字是否重复
        //不重复返回true，否则返回 false
        int maxX = getMaxX(sudokuType);
        List<Integer> numbers = new ArrayList<>();
        for(int i=0; i<maxX; i++){
            if(pieces[i][y].getNumber() > 0){
                numbers.add(pieces[i][y].getNumber());
            }
        }
        if(numbers.size() >= 2){
            return isNumbersUnique(numbers);
        }
        return true;
    }
    public Boolean checkBigNumberColumn(int x){
        //检查y列数字是否重复
        //不重复返回true，否则返回 false
        int maxY = getMaxY(sudokuType);
        List<Integer> numbers = new ArrayList<>();
        for(int i=0; i<maxY; i++){
            if(pieces[x][i].getNumber() > 0){
                numbers.add(pieces[x][i].getNumber());
            }
        }
        return isNumbersUnique(numbers);
    }
    public Boolean checkBigNumberSquare(int x,int y){
        //检查(x,y)所在宫格数字是否重复
        //不重复返回true，否则返回 false
        List<Integer> numbers = new ArrayList<>();
        List<SudokuPiece> squarePieces = getSquare(x,y);
        for(int i=0; i<squarePieces.size(); i++){
            if(squarePieces.get(i).getNumber() > 0){
                numbers.add(squarePieces.get(i).getNumber());
            }
        }
        return isNumbersUnique(numbers);
    }
    private Boolean checkBigNumberDiagonal(int x,int y){
        List<SudokuPiece> pieceList = getDiagonal(x,y);
        if(pieceList == null){
            return true;
        }
        return isPiecesUnique(pieceList);
    }
    private Boolean checkBigNumberPercent(int x,int y){
        //检查x,y所在百分比宫格数字是否重复
        List<SudokuPiece> pieceList = getPercentSquare(x,y);
        if(pieceList == null){
            return true;
        }
        return isPiecesUnique(pieceList);
    }
    private Boolean checkBigNumberSuper(int x,int y){
        //检查x,y所在宫格数字是否重复
        List<SudokuPiece> pieceList = getSuperSquare(x,y);
        if(pieceList == null){
            return true;
        }
        return isPiecesUnique(pieceList);
    }
    private Boolean checkBigNumberPercentDiagonal(int x,int y){
        //检查x,y所在百分比对角线数字是否重复
        List<SudokuPiece> pieceList = getPercentDiagonal(x,y);
        if(pieceList == null){
            return true;
        }
        return isPiecesUnique(pieceList);
    }
    public Boolean numberCompleted(int number){
        //number在board中是否已经填完。
        int maxX = getMaxX(sudokuType);
        int maxY = getMaxY(sudokuType);
        //每一行，是否该数字都存在
        for(int y=0; y<maxY; y++){
            if(!numberExistRow(pieces[0][y],number)){
                return false;
            }
        }
        //每一列，是否该数字都存在
        for(int x=0; x<maxX; x++){
            if(!numberExistColumn(pieces[x][0],number)){
                return false;
            }
        }
        //每一宫格，是否该数字都存在
        int numberOfSquareX = getNumberOfSquareX(sudokuType);
        int numberOfSquareY = getNumberOfSquareY(sudokuType);
        int maxXOfSquare = getMaxXOfSquare(sudokuType);
        int maxYOfSquare = getMaxYOfSquare(sudokuType);
        int numberOfSquare = numberOfSquareX * numberOfSquareY;
        for(int i=0; i<numberOfSquare; i++){
            int x = i%numberOfSquareX * maxXOfSquare;
            int y = i/numberOfSquareX * maxYOfSquare;
            if(!numberExistSquare(pieces[x][y],number)){
                return false;
            }
        }
        switch(sudokuType){
            case FOUR:
            case SIX:
            case NINE:
                return true;
            case X_STYLE:
                if(!numberExistDiagonal(pieces[0][0],number)){ //对角线一
                    return  false;
                }
                if(!numberExistDiagonal(pieces[0][maxY-1],number)){
                    return false;
                }
                return true;
            case PERCENT:
                if(!numberExistDiagonal(pieces[0][0],number)){ //对角线一
                    return  false;
                }
                if(!numberExistPercentSquare(pieces[1][7],number)) {
                    return false;
                }
                if(!numberExistPercentSquare(pieces[7][1],number)) {
                    return false;
                }
                return true;
            case SUPER:
                //左上
                if(!numberExistPercentSquare(pieces[1][7],number)) {
                    return false;
                }
                //左下
                if(!numberExistPercentSquare(pieces[1][1],number)) {
                    return false;
                }
                //右上
                if(!numberExistPercentSquare(pieces[7][7],number)) {
                    return false;
                }
                //右下
                if(!numberExistPercentSquare(pieces[7][1],number)) {
                    return false;
                }
                return true;
            default:
                return false;
        }
    }
    public List<SudokuPiece> getSquare(int x, int y){
        //返回(x,y)所在的宫格piece，注意不包含百分比，超数独的宫格
        int maxXOfSquare = getMaxXOfSquare(sudokuType);
        int maxYOfSquare = getMaxYOfSquare(sudokuType);
        int numberOfSquareX = getNumberOfSquareX(sudokuType);
        int numberOfSquareY = getNumberOfSquareY(sudokuType);
        int indexOfSquareX = (int)(x/maxXOfSquare);
        int startX = indexOfSquareX * maxXOfSquare;
        int stopX = (indexOfSquareX+1) * maxXOfSquare - 1;
        int indexOfSquareY = (int)(y/maxYOfSquare);
        int startY = indexOfSquareY * maxYOfSquare;
        int stopY = (indexOfSquareY+1) * maxYOfSquare - 1;
        List<SudokuPiece> sudokuPieces = new ArrayList<>();
        for(int i=startX; i<=stopX; i++){
            for(int j=startY; j<=stopY; j++){
                sudokuPieces.add(pieces[i][j]);
            }
        }
        return sudokuPieces;
    }
    public Boolean isInDiagonal(SudokuPiece piece){
        //piece是否在对角线上
        if(piece.x == piece.y){
            return true;
        }else if(piece.x+piece.y == getMaxNumber(sudokuType)-1){
            return true;
        }
        return  false;
    }
    public List<SudokuPiece> getDiagonal(int x, int y){
        //获取(x,y)所在的对角线的所有棋子
        //如果不在对角线上返回nulll
        //如果在对角线上的交叉点，返回两个对角线的集合(交叉点会出现两次）
        List<SudokuPiece> pieceList = new ArrayList<>();
        int maxNumber = getMaxNumber(sudokuType);
        if(x == y){//在对角线一
            for(int i=0; i<maxNumber; i++){
                pieceList.add(pieces[i][i]);
            }
        }
        if(x+y == maxNumber-1){
            for(int i=0; i<maxNumber; i++){
                pieceList.add(pieces[i][maxNumber-1-i]);
            }
        }
        if (pieceList.size() == 0) {
            return null;
        }else{
            return pieceList;
        }
    }
    public List<SudokuPiece> getPercentDiagonal(int x,int y){
        //返回(x,y)所在的百分比对角线，不在返回null
        List<SudokuPiece> pieceList = new ArrayList<>();
        int maxNumber = getMaxNumber(sudokuType);
        if(x == y){//在对角线一
            for(int i=0; i<maxNumber; i++){
                pieceList.add(pieces[i][i]);
            }
        }
        if (pieceList.size() == 0) {
            return null;
        }else{
            return pieceList;
        }
    }
    public Boolean isInPercentDiagonal(SudokuPiece piece){
        //piece是否在百分比的对角线上
        if(piece.x == piece.y){
            return true;
        }
        return false;
    }
    public Boolean isInPercentSquare(SudokuPiece piece){
        //piece是否在百分比宫格中
        //左上百分比宫格
        if(piece.x>=1 && piece.x<=3 && piece.y<=7 && piece.y>=5){
            return true;
        }
        //右下宫格
        if(piece.x>=5 && piece.x<=7 && piece.y>=1 && piece.y<=3){
            return true;
        }
        return false;
    }
    public Boolean isInSuperSquare(SudokuPiece piece){
        //左上宫格
        if(piece.x>=1 && piece.x<=3 && piece.y<=7 && piece.y>=5){
            return true;
        }
        //左下宫格
        if(piece.x>=1 && piece.x<=3 && piece.y>=1 && piece.y<=3){
            return true;
        }
        //右下宫格
        if(piece.x>=5 && piece.x<=7 && piece.y>=1 && piece.y<=3){
            return true;
        }
        if(piece.x>=5 && piece.x<=7 && piece.y>=5 && piece.y<=7){
            return true;
        }
        return false;
    }
    public List<SudokuPiece> getPercentSquare(int pieceX, int pieceY){
        //返回(x,y)所在的百分比宫格，如果不在就返回null
        List<SudokuPiece> pieceList = new ArrayList<>();
        //左上百分比宫格
        if(pieceX>=1 && pieceX<=3 && pieceY<=7 && pieceY>=5){
            for(int x=1; x<=3; x++){
                for(int y=5; y<=7; y++){
                    pieceList.add(pieces[x][y]);
                }
            }
        }
        //右下宫格
        if(pieceX>=5 && pieceX<=7 && pieceY>=1 && pieceY<=3){
            for(int x=5; x<=7; x++){
                for(int y=1; y<=3; y++){
                    pieceList.add(pieces[x][y]);
                }
            }
        }
        if (pieceList.size() == 0) {
            return null;
        }else{
            return pieceList;
        }
    }
    public List<SudokuPiece> getSuperSquare(int pieceX, int pieceY){
        //返回(x,y)所在的super宫格，如果不在就返回null
        List<SudokuPiece> pieceList = new ArrayList<>();
        //左上宫格
        if(pieceX>=1 && pieceX<=3 && pieceY<=7 && pieceY>=5){
            for(int x=1; x<=3; x++){
                for(int y=5; y<=7; y++){
                    pieceList.add(pieces[x][y]);
                }
            }
        }
        //左下宫格
        if(pieceX>=1 && pieceX<=3 && pieceY>=1 && pieceY<=3){
            for(int x=1; x<=3; x++){
                for(int y=1; y<=3; y++){
                    pieceList.add(pieces[x][y]);
                }
            }
        }
        //右上宫格
        if(pieceX>=5 && pieceX<=7 && pieceY<=7 && pieceY>=5){
            for(int x=5; x<=7; x++){
                for(int y=5; y<=7; y++){
                    pieceList.add(pieces[x][y]);
                }
            }
        }
        //右下宫格
        if(pieceX>=5 && pieceX<=7 && pieceY>=1 && pieceY<=3){
            for(int x=5; x<=7; x++){
                for(int y=1; y<=3; y++){
                    pieceList.add(pieces[x][y]);
                }
            }
        }
        if (pieceList.size() == 0) {
            return null;
        }else{
            return pieceList;
        }
    }
    public List<SudokuPiece> getPercentSquareOfLine(int y){
        //返回y行所在的百分比宫格，如果不存在就返回null

        List<SudokuPiece> pieceList = new ArrayList<>();
        if(y >=5 && y<= 7){  //左上百分比宫格
            return getPercentSquare(1,5);
        }
        if(y>=1 && y<=3){ //右下宫格
            return getPercentSquare(5,1);
        }
        return null;
    }
    public List<SudokuPiece> getSuperSquareOfLine(int y,Direction direction){
        //返回y行所在的对应方向direction所在super宫格，如果不存在就返回null

        List<SudokuPiece> pieceList = new ArrayList<>();
        if(y >=5 && y<= 7){  //上宫格
            if(direction == Direction.LEFT) {
                return getSuperSquare(1, 7);
            }else if(direction == Direction.RIGHT){
                return getSuperSquare(7,7);
            }
        }
        if(y>=1 && y<=3){ //下宫格
            if(direction == Direction.LEFT) {
                return getSuperSquare(1, 1);
            }else if(direction == Direction.RIGHT){
                return getSuperSquare(7,1);
            }
        }
        return null;
    }
    public List<SudokuPiece> getPercentSquareOfColumn(int x){
        //返回y行所在的百分比宫格，如果不存在就返回null

        List<SudokuPiece> pieceList = new ArrayList<>();
        if(x >=1 && x<= 3){  //左上百分比宫格
            return getPercentSquare(1,5);
        }
        if(x>=5 && x<=7){ //右下宫格
            return getPercentSquare(5,1);
        }
        return null;
    }
    public List<SudokuPiece> getSuperSquareOfColumn(int x,Direction direction){
        //返回x列所在的对应方向direction所在super宫格，如果不存在就返回null

        List<SudokuPiece> pieceList = new ArrayList<>();
        if(x>=1 && x<=3){ //左宫格
            if(direction == Direction.UP) {
                return getSuperSquare(1, 7);
            }else if(direction == Direction.DOWN){
                return getSuperSquare(1,1);
            }
        }
        if(x >=5 && x<= 7){  //右宫格
            if(direction == Direction.UP) {
                return getSuperSquare(7, 7);
            }else if(direction == Direction.DOWN){
                return getSuperSquare(7,1);
            }
        }
        return null;
    }

    private static List<SudokuPiece> getPiecesByLine(List<SudokuPiece> inPieceList,int y,int miniNumber){
        //输入的piecelist中，找出y行的pieces
        List<SudokuPiece> outPieceList = new ArrayList<>();
        for(int i=0; i<inPieceList.size(); i++){
            if(inPieceList.get(i).y == y && inPieceList.get(i).getNumber() == 0 && inPieceList.get(i).haveMiniNumber(miniNumber)){
                outPieceList.add(inPieceList.get(i));
            }
        }
        return outPieceList;
    }
    private static List<SudokuPiece> getPiecesByColumn(List<SudokuPiece> inPieceList,int x,int miniNumber){
        //输入的piecelist中，找出y行的pieces
        List<SudokuPiece> outPieceList = new ArrayList<>();
        for(int i=0; i<inPieceList.size(); i++){
            if(inPieceList.get(i).x == x && inPieceList.get(i).getNumber() == 0 && inPieceList.get(i).haveMiniNumber(miniNumber)){
                outPieceList.add(inPieceList.get(i));
            }
        }
        return outPieceList;
    }
    private static List<SudokuPiece> getPiecesByPercentDiagnoal(List<SudokuPiece> inPieceList,int miniNumber){
        //获取inPieceList中，处于百分比对角线的piece
        List<SudokuPiece> outPieceList = new ArrayList<>();
        for(int i=0; i<inPieceList.size(); i++){
            SudokuPiece piece = inPieceList.get(i);
            if(piece.x == piece.y && inPieceList.get(i).getNumber() == 0 && inPieceList.get(i).haveMiniNumber(miniNumber)){
                outPieceList.add(inPieceList.get(i));
            }
        }
        return outPieceList;
    }
    private static List<SudokuPiece> getPiecesByXDiagnoal(List<SudokuPiece> inPieceList,int miniNumber){
        //获取inPieceList中，处于百分比对角线的piece
        List<SudokuPiece> outPieceList = new ArrayList<>();
        for(int i=0; i<inPieceList.size(); i++){
            SudokuPiece piece = inPieceList.get(i);
            if(piece.x == 8-piece.y && inPieceList.get(i).getNumber() == 0 && inPieceList.get(i).haveMiniNumber(miniNumber)){
                outPieceList.add(inPieceList.get(i));
            }
        }
        return outPieceList;
    }
    private static Boolean isOtherPlaceHaveMiniExcludeLine(List<SudokuPiece> pieceList, int y,int miniNumber){
        //pieceList中除了y行其他位置是否包含miniNumber
        for(int i=0; i<pieceList.size(); i++){
            SudokuPiece piece = pieceList.get(i);
            //排除y行
            if(piece.y == y){
                continue;
            }
            //排除大数字
            if(piece.getNumber() > 0){
                continue;
            }
            if(piece.haveMiniNumber(miniNumber)){
                return true;
            }
        }
        return false;
    }
    private static Boolean isOtherPlaceHaveMiniExcludeColumn(List<SudokuPiece> pieceList, int x,int miniNumber){
        //pieceList中除了x列其他位置是否包含miniNumber
        for(int i=0; i<pieceList.size(); i++){
            SudokuPiece piece = pieceList.get(i);
            //排除y行
            if(piece.x == x){
                continue;
            }
            //排除大数字
            if(piece.getNumber() > 0){
                continue;
            }
            if(piece.haveMiniNumber(miniNumber)){
                return true;
            }
        }
        return false;
    }
    private static Boolean isOtherPlaceHaveMiniExcludePercentDiagnoal(List<SudokuPiece> pieceList,int miniNumber){
        //检查除了百分比对角线以外，其他地方是否存在备选数字miniNumber
        for(int i=0; i<pieceList.size(); i++){
            SudokuPiece piece = pieceList.get(i);
            if(piece.x == piece.y || piece.getNumber() > 0){
                continue;
            }
            if(piece.haveMiniNumber(miniNumber)){
                return true;
            }
        }
        return false;
    }
    private static Boolean isOtherPlaceHaveMiniExcludeXDiagnoal(List<SudokuPiece> pieceList,int miniNumber){
        //检查除了百分比对角线以外，其他地方是否存在备选数字miniNumber
        //仅适用于X_STYLE
        for(int i=0; i<pieceList.size(); i++){
            SudokuPiece piece = pieceList.get(i);
            if(piece.x == 8 - piece.y || piece.getNumber() > 0){
                continue;
            }
            if(piece.haveMiniNumber(miniNumber)){
                return true;
            }
        }
        return false;
    }
    private static Boolean isInPieceList(List<SudokuPiece> pieceList,SudokuPiece piece){
        //piece是否在pieceList中
        if(pieceList == null){
            return false;
        }
        for(int i=0; i<pieceList.size(); i++){
            if(piece.x == pieceList.get(i).x && piece.y == pieceList.get(i).y){
                return true;
            }
        }
        return false;
    }
    private static String pieceListToString(List<SudokuPiece> pieceList){
        String str = "";
        if(pieceList == null){
            return str;
        }
        for(int i=0; i<pieceList.size(); i++){
            str += pieceList.get(i).toDBString() + "|";
        }
        return str;
    }
    public static int getMaxNumber(SudokuType sudokuType){
        switch (sudokuType) {
            case FOUR:
                return 4;
            case SIX:
                return 6;
            case NINE:
            case X_STYLE:
            case PERCENT:
            case SUPER:
                return 9;
            default:
                return 9;
        }
    }
    public static int getMaxX(SudokuType sudokuType) {
        switch (sudokuType) {
            case FOUR:
                return 4;
            case SIX:
                return 6;
            case NINE:
            case X_STYLE:
            case PERCENT:
            case SUPER:
                return 9;
            default:
                return 9;
        }
    }
    public static int getMaxY(SudokuType sudokuType){
        switch (sudokuType) {
            case FOUR:
                return 4;
            case SIX:
                return 6;
            case NINE:
            case X_STYLE:
            case PERCENT:
            case SUPER:
                return 9;
            default:
                return 0;
        }
    }
    public static int getNumberOfSquareX(SudokuType sudokuType){
        switch (sudokuType) {
            case FOUR:
                return 2;
            case SIX:
                return 2;
            case NINE:
            case X_STYLE:
            case PERCENT:
            case SUPER:
                return 3;
            default:
                return 3;
        }
    }
    public static int getNumberOfSquareY(SudokuType sudokuType){
        switch (sudokuType) {
            case FOUR:
                return 2;
            case SIX:
                return 3;
            case NINE:
            case X_STYLE:
            case PERCENT:
            case SUPER:
                return 3;
            default:
                return 3;
        }
    }
    public static int getMaxXOfSquare(SudokuType sudokuType){
        switch (sudokuType) {
            case FOUR:
                return 2;
            case SIX:
                return 3;
            case NINE:
            case X_STYLE:
            case PERCENT:
            case SUPER:
                return 3;
            default:
                return 3;
        }
    }
    public static int getMaxYOfSquare(SudokuType sudokuType){
        switch (sudokuType) {
            case FOUR:
                return 2;
            case SIX:
                return 2;
            case NINE:
            case X_STYLE:
            case PERCENT:
            case SUPER:
                return 3;
            default:
                return 3;
        }
    }
    public static int getNumberOfSmallBoardX(SudokuType sudokuType){
        switch (sudokuType) {
            case FOUR:
                return 2;
            case SIX:
                return 3;
            case NINE:
            case X_STYLE:
            case PERCENT:
            case SUPER:
                return 3;
            default:
                return 3;
        }
    }
    public static int getNumberOfSmallBoardY(SudokuType sudokuType){
        switch (sudokuType) {
            case FOUR:
                return 2;
            case SIX:
                return 2;
            case NINE:
            case X_STYLE:
            case PERCENT:
            case SUPER:
                return 3;
            default:
                return 3;
        }
    }

    public void printModifiedPieces(){
        //打印修改过的piece
        int maxX = getMaxX(sudokuType);
        int maxY = getMaxY(sudokuType);
        for(int x=0; x<maxX; x++){
            for(int y=0; y<maxY; y++){
                if(pieces[x][y].isModifiable() && pieces[x][y].getNumber() > 0){
                    Log.v("sudoku",String.format("(%d,%d)=%d",x,y,pieces[x][y].getNumber()));
                }
            }
        }
    }
}
enum SudokuType{
    FOUR(0),SIX(1),NINE(2),X_STYLE(3),PERCENT(4),SUPER(5);
    private int value;

    SudokuType(int i) {
        // TODO Auto-generated constructor stub
        this.value = i;
    }
    public static SudokuType toEnum(int value) {
        switch (value) {
            case 0:
                return FOUR;
            case 1:
                return SIX;
            case 2:
                return NINE;
            case 3:
                return X_STYLE;
            case 4:
                return PERCENT;
            case 5:
                return SUPER;
            default:
                return null;
        }
    }
    public  int  toInt(){
        return this.value;
    }
}
enum Direction{
    LEFT(0),RIGHT(1),UP(2),DOWN(3);
    private int value;

    Direction(int i) {
        // TODO Auto-generated constructor stub
        this.value = i;
    }
    public static Direction toEnum(int value) {
        switch (value) {
            case 0:
                return LEFT;
            case 1:
                return RIGHT;
            case 2:
                return UP;
            case 3:
                return DOWN;
            default:
                return null;
        }
    }
    public  int  toInt(){
        return this.value;
    }
}
class Numbers{
    private List<Integer> numbers = new ArrayList<>();
    Numbers(){
        ;
    }
    Numbers(int number){
        numbers.add(number);
    }
    public int count(){
        if(numbers == null){
            return 0;
        }
        return numbers.size();
    }
    public Boolean include(int number){
        if (numbers == null){
            return false;
        }
        for(int i=0; i<numbers.size(); i++){
            if(numbers.get(i) == number){
                return true;
            }
        }
        return false;
    }
    public List<Integer> getNumbers(){
        return numbers;
    }
    Numbers(List<Integer> numbers){
        this.numbers = numbers;
    }
    private Numbers copyNumbers(){
        Numbers newNumbers = new Numbers();
        for(int i=0; i<numbers.size(); i++){
            newNumbers.addNumber(numbers.get(i));
        }
        return newNumbers;
    }
    private void addNumber(int number){
        numbers.add(number);
    }
    public static Boolean isInNumberList(List<Integer> numberList, int number){
        //number是否在numberList中
        for(int i=0; i<numberList.size(); i++){
            if(number == numberList.get(i)){
                return true;
            }
        }
        return false;
    }
    public static List<Numbers> findAllCombination(List<Integer> numberList,int N){
        //从numberList中找出N个的数字组合
        List<Numbers> numbersList = new ArrayList<>();
        if(numberList.size() < N){
            return null;
        }
        else if(numberList.size() == N){
            //把整个numberList作为一个numbers返回
            numbersList.add(new Numbers(numberList));
            return numbersList;
        }

        if(N == 1){
            for(int i=0; i<numberList.size(); i++){
                numbersList.add(new Numbers(numberList.get(i)));
            }
            return numbersList;
        }else if(N == 2){
            for (int i = 0; i < numberList.size(); i++) {
                //先选择第一个数字 numberList.get(i);
                //避免重复的情况下，第二个数字必须大于第一个
                for(int j=i+1; j<numberList.size(); j++) {
                    Numbers numbers = new Numbers(numberList.get(i));
                    numbers.addNumber(numberList.get(j));
                    numbersList.add(numbers);
                }
            }
            return numbersList;
        }else{  // N >= 3
            //debug
            /*String str = "";
            for(int i=0; i<numberList.size(); i++){
                str += String.valueOf(numberList.get(i));
            }
            Log.v("debug",String.format("N=%d;NumberList=%s",N,str));*/
            for(int i=0; i<numberList.size(); i++){
                //先固定第一个，
                // 然后在剩下的数字中选择N-1个数字，为了避免重复，剩下的数字必须大于第一个数字

                int leftCountOfNumbers = numberList.size() - 1 - i;
                if(leftCountOfNumbers < N-1){ //剩下的数字个数< N-1
                    break;
                }
                //生成剩下的数字链表
                List<Integer> numberList2 = new ArrayList<>();
                for(int j=i+1; j<numberList.size(); j++){
                    numberList2.add(numberList.get(j));
                }
                //在剩下的数字链表中找出所有N-1个数字的组合
                List<Numbers> numbersList2 = findAllCombination(numberList2,N-1);
                if(numberList2 == null || numberList2.size() == 0){
                    continue;
                }
                //第一个数字 + 剩下的数字中的N-1个组合 = N个数字的组合
                for(int k=0; k<numbersList2.size(); k++){
                    Numbers newNumbers = numbersList2.get(k).copyNumbers();
                    newNumbers.addNumber(numberList.get(i));  //加上之前固定的数字
                    numbersList.add(newNumbers);
                }
            }
            return numbersList;
        }
    }
    public static List<Numbers> addNumbersList(List<Numbers> list1, List<Numbers> list2){
        if((list1 == null || list1.size() == 0) && (list2 == null || list2.size() == 0)){
            //两个list都为空
            return null;
        }
        if (list1 == null) {
            list1 = new ArrayList<>();
        }
        if(list2 == null){
            return list1;
        }
        for(int i=0; i<list2.size(); i++){
            list1.add(list2.get(i));
        }
        return list1;
    }
    @Override
    public String toString(){
        String str="";
        for(int i=0; i<numbers.size(); i++){
            str += String.valueOf(numbers.get(i));
        }
        return  str;
    }
}
