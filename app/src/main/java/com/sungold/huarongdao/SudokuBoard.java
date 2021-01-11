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
    public final static int CHAIN_STRONG_ALTERNATE = 1; //强入交替
    public final static int CHAIN_WEAK_ALTERNATE = 2;   //弱入交替
    public final static int CHAIN_STRONG_ALWAYS = 3;    //恒强
    public final static int CHAIN_WEAK_ALWAYS = 4;      //恒弱
    public SudokuType sudokuType;

    public SudokuPiece[][] pieces = null;
    int seconds = 0 ;  //花费的时间

    ChainNode[][] chainNet;     //查找链表时暂存用
    List<SudokuPiece> chainList = null;
    List<SudokuPiece> bestChainList = null;  //存储链路最短的提示链表
    int bestChainX,bestStartFlag;    //储存最短链路时的X
    int startFlag;
    int hintMiniNumber;  //用于存储提示的备选数字,在xy链表中提示起始x

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
        //stringBuffer.append(name+"|");
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
        if(strings.length == 3){ //老格式为name|type|board
            name = strings[0];
            sudokuType = SudokuType.toEnum(Integer.valueOf(strings[1]));
            pieces =  SudokuBoard.piecesFromString(sudokuType,strings[2]);
        }else if(strings.length == 2){ //新格式为type|board
            sudokuType = SudokuType.toEnum(Integer.valueOf(strings[0]));
            pieces =  SudokuBoard.piecesFromString(sudokuType,strings[1]);
        }else{//兼容老格式,第一个字符为类型,后面为board
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
                    hintMiniNumber = pieces[x][y].getMiniNumbers()[0];
                    return pieceList;
                }
            }
        }

        //找出同一系列(行，列，等)某一备选数字仅在一个单元格存在的所有单元。
        //同一列
        for(int x=0; x<maxX; x++){
            for(int n=1; n<=getMaxNumber(sudokuType); n++){
                int count = 0;
                SudokuPiece piece = null;
                for(int y=0; y<maxY; y++){
                    if(pieces[x][y].getNumber() == 0 && pieces[x][y].haveMiniNumber(n)){
                        count++;
                        piece = pieces[x][y];
                    }
                }
                if(count == 1){
                    pieceList.add(piece);
                    Log.v("debug",String.format("同一列:%d唯一",n));
                    hintMiniNumber = n;
                    return pieceList;
                }
            }
        }
        //同一行
        for(int y=0; y<maxY; y++){
            for(int n=1; n<=getMaxNumber(sudokuType); n++){
                int count = 0;
                SudokuPiece piece = null;
                for(int x=0; x<maxX; x++){
                    if(pieces[x][y].getNumber() == 0 && pieces[x][y].haveMiniNumber(n)){
                        count++;
                        piece = pieces[x][y];
                    }
                }
                if(count == 1){
                    pieceList.add(piece);
                    Log.v("debug",String.format("同一行:%d唯一",n));
                    hintMiniNumber = n;
                    return pieceList;
                }
            }
        }
        //同一宫格
        int numberOfSquareX = getNumberOfSquareX(sudokuType);
        int numberOfSquareY = getNumberOfSquareY(sudokuType);
        for(int x=0; x<numberOfSquareX; x++){
            for(int y=0; y<numberOfSquareY; y++){
                int piece_x = x*getMaxXOfSquare(sudokuType);
                int pice_y = y*getMaxYOfSquare(sudokuType);
                List<SudokuPiece> squareList = removeBigNumberPiece(getSquare(piece_x,pice_y));
                if(squareList == null || squareList.size() == 0){
                    continue;
                }
                for(int n=1; n<=getMaxNumber(sudokuType); n++){
                    int count = 0;
                    SudokuPiece piece = null;
                    for(int i=0; i<squareList.size(); i++){
                        if(squareList.get(i).getNumber() == 0 && squareList.get(i).haveMiniNumber(n)){
                            count++;
                            piece = squareList.get(i);
                        }
                    }
                    if(count == 1){
                        pieceList.add(piece);
                        Log.v("debug",String.format("同一宫格:%d唯一",n));
                        hintMiniNumber = n;
                        return pieceList;
                    }
                }
            }
        }
        //同一对角线(百分比)
        if(sudokuType == SudokuType.PERCENT || sudokuType == SudokuType.X_STYLE) {
            List<SudokuPiece> percentList = getPercentDiagonal(0, 0);
            for(int n=1; n<=getMaxNumber(sudokuType); n++){
                int count = 0;
                SudokuPiece piece = null;
                for(int i=0; i<percentList.size(); i++){
                    if(percentList.get(i).getNumber() == 0 && percentList.get(i).haveMiniNumber(n)){
                        count++;
                        piece = percentList.get(i);
                    }
                }
                if(count == 1){
                    pieceList.add(piece);
                    Log.v("debug",String.format("同一百分比对角线:%d唯一",n));
                    hintMiniNumber = n;
                    return pieceList;
                }
            }
        }
        //同一对角线(X)
        if(sudokuType == SudokuType.X_STYLE) {
            List<SudokuPiece> xDiagnoalList = getDiagonal(0, 8);
            for(int n=1; n<=getMaxNumber(sudokuType); n++){
                int count = 0;
                SudokuPiece piece = null;
                for(int i=0; i<xDiagnoalList.size(); i++){
                    if(xDiagnoalList.get(i).getNumber() == 0 && xDiagnoalList.get(i).haveMiniNumber(n)){
                        count++;
                        piece = xDiagnoalList.get(i);
                    }
                }
                if(count == 1){
                    pieceList.add(piece);
                    Log.v("debug",String.format("同一X对角线:%d唯一",n));
                    hintMiniNumber = n;
                    return pieceList;
                }
            }
        }
        //同一百分比宫格
        if(sudokuType == SudokuType.PERCENT || sudokuType == SudokuType.SUPER){
            //左上
            List<SudokuPiece> leftPercentList = getPercentSquare(1,7);
            for(int n=1; n<=getMaxNumber(sudokuType); n++){
                int count = 0;
                SudokuPiece piece = null;
                for(int i=0; i<leftPercentList.size(); i++){
                    if(leftPercentList.get(i).getNumber() == 0 && leftPercentList.get(i).haveMiniNumber(n)){
                        count++;
                        piece = leftPercentList.get(i);
                    }
                }
                if(count == 1){
                    pieceList.add(piece);
                    Log.v("debug",String.format("同一百分比宫格:%d唯一",n));
                    hintMiniNumber = n;
                    return pieceList;
                }
            }
            //右下
            List<SudokuPiece> rightPercentList = getPercentSquare(7,1);
            for(int n=1; n<=getMaxNumber(sudokuType); n++){
                int count = 0;
                SudokuPiece piece = null;
                for(int i=0; i<rightPercentList.size(); i++){
                    if(rightPercentList.get(i).getNumber() == 0 && rightPercentList.get(i).haveMiniNumber(n)){
                        count++;
                        piece = rightPercentList.get(i);
                    }
                }
                if(count == 1){
                    pieceList.add(piece);
                    Log.v("debug",String.format("同一百分比宫格:%d唯一",n));
                    hintMiniNumber = n;
                    return pieceList;
                }
            }
        }
        //同一super宫格
        if(sudokuType == SudokuType.SUPER){
            //左下
            List<SudokuPiece> leftPercentList = getSuperSquare(1,1);
            for(int n=1; n<=getMaxNumber(sudokuType); n++){
                int count = 0;
                SudokuPiece piece = null;
                for(int i=0; i<leftPercentList.size(); i++){
                    if(leftPercentList.get(i).getNumber() == 0 && leftPercentList.get(i).haveMiniNumber(n)){
                        count++;
                        piece = leftPercentList.get(i);
                    }
                }
                if(count == 1){
                    pieceList.add(piece);
                    Log.v("debug",String.format("同一super宫格:%d唯一",n));
                    hintMiniNumber = n;
                    return pieceList;
                }
            }
            //右上
            List<SudokuPiece> rightPercentList = getSuperSquare(7,7);
            for(int n=1; n<=getMaxNumber(sudokuType); n++){
                int count = 0;
                SudokuPiece piece = null;
                for(int i=0; i<rightPercentList.size(); i++){
                    if(rightPercentList.get(i).getNumber() == 0 && rightPercentList.get(i).haveMiniNumber(n)){
                        count++;
                        piece = rightPercentList.get(i);
                    }
                }
                if(count == 1){
                    pieceList.add(piece);
                    Log.v("debug",String.format("同一super宫格:%d唯一",n));
                    hintMiniNumber = n;
                    return pieceList;
                }
            }
        }
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
        //同一个宫格中，只有同一行才有备选数字miniNumber，如果宫格以外的同行其他位置存在备选数字，（那么删除本宫格其他行的备选数字。）
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
                Log.v("debug",String.format("miniNumber=%d,No%d square",miniNumber,i));
                Log.v("debug",pieceListToString(pieceList));
                if (!numberExistInList(pieceList,miniNumber) && !isOtherPlaceHaveMiniExcludePercentDiagnoal(pieceList, miniNumber)) {
                    outPieceList = getPiecesByPercentDiagnoal(pieceList,miniNumber);
                    Log.v("debug","找到对角线才有备选数字:"+pieceListToString(outPieceList));
                    //检查除了outPieceList中的piece以外，该对角线其他位置是否包含备选数字
                    for (int j = 0; j < 9; j++) {
                        if (pieces[j][j].getNumber() > 0 || isInPieceList(outPieceList, pieces[j][j])) {
                            continue;
                        }
                        if (pieces[j][j].haveMiniNumber(miniNumber)) {
                            //除了该宫格以外,其他对方也有棋子
                            Log.v("deug","其他地方也有备选数字:"+pieces[j][j].toDBString());
                            return outPieceList;
                        }
                    }
                }
            }
        }
        if(sudokuType == SudokuType.X_STYLE){
            //X-反对角线
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
    //找某一行备选数字，仅在一个宫格中存在的。
    public List<SudokuPiece> findMiniInSquareOfLine(int miniNumber){
        //备选数字miniNumber
        //一行中，只有同一个宫格中才有备选数字miniNumber，如果宫格中本行以外其他位置存在备选数字，（那么删除本行其他宫格的备选数字。）
        // 举例：x,x,x,x,x,x,1,1,1
        //                  1,1,1
        //                  1,1,1

        List<SudokuPiece> outPieceList; //输出的满足条件的pieceList
        int maxY = SudokuBoard.getMaxY(sudokuType);
        int maxX = SudokuBoard.getMaxX(sudokuType);
        Log.v("debug",String.format("mini=%d,maxY=%d",miniNumber,maxY));
        for(int y=0; y<maxY; y++){  //逐行分析
            int maxXOfSquare = getMaxXOfSquare(sudokuType);
            int numberOfSquare = maxX/maxXOfSquare;
            //Log.v("debug",String.format("  y=%d",y));
            if(numberExistRow(pieces[0][y],miniNumber)){ //本行已经有bigNumber=miniNumber的，忽略。
                continue;
            }
            //逐个宫格找是否有满足条件的宫格
            for(int i=0; i<numberOfSquare; i++){
                //获取该宫格的所有piece
                List<SudokuPiece> pieceList = getSquare(i*maxXOfSquare,y);
                //获取该宫格的本行piece
                outPieceList = getPiecesByLine(pieceList,y,miniNumber);
                if(outPieceList == null || outPieceList.size() == 0){
                    continue;
                }
                //如果y行除了outPieceList以外，其他位置不包含miniNumber
                if(!isOtherPlaceOfLineHaveMiniExcludeList(outPieceList,y,miniNumber)){
                    if(isOtherPlaceHaveMiniExcludeLine(pieceList,y,miniNumber)){
                        return outPieceList;
                    }
                }
            }

            //百分比宫格，也是如此
            if(sudokuType == SudokuType.PERCENT){
                //获取该行百分比宫格的所有piece
                List<SudokuPiece> pieceList = getPercentSquareOfLine(y);
                if(pieceList == null || pieceList.size() == 0){
                    continue;
                }
                //获取该宫格的本行piece
                outPieceList = getPiecesByLine(pieceList,y,miniNumber);
                if(outPieceList == null || outPieceList.size() == 0){
                    continue;
                }
                //如果y行除了outPieceList以外，其他位置不包含miniNumber
                if(!isOtherPlaceOfLineHaveMiniExcludeList(outPieceList,y,miniNumber)){
                    if(isOtherPlaceHaveMiniExcludeLine(pieceList,y,miniNumber)){
                        return outPieceList;
                    }
                }
            }
            if(sudokuType == SudokuType.SUPER){
                //获取本行的左super宫格
                List<SudokuPiece> pieceList = getSuperSquareOfLine(y,Direction.LEFT);
                if(pieceList == null || pieceList.size() == 0){
                    continue;
                }
                //获取该宫格的本行piece
                outPieceList = getPiecesByLine(pieceList,y,miniNumber);
                if(outPieceList == null || outPieceList.size() == 0){
                    continue;
                }
                //如果y行除了outPieceList以外，其他位置不包含miniNumber
                if(!isOtherPlaceOfLineHaveMiniExcludeList(outPieceList,y,miniNumber)){
                    if(isOtherPlaceHaveMiniExcludeLine(pieceList,y,miniNumber)){
                        return outPieceList;
                    }
                }
                //获取本行的右super宫格
                pieceList = getSuperSquareOfLine(y,Direction.RIGHT);
                if(pieceList == null || pieceList.size() == 0){
                    continue;
                }
                //获取该宫格的本行piece
                outPieceList = getPiecesByLine(pieceList,y,miniNumber);
                if(outPieceList == null || outPieceList.size() == 0){
                    continue;
                }
                //如果y行除了outPieceList以外，其他位置不包含miniNumber
                if(!isOtherPlaceOfLineHaveMiniExcludeList(outPieceList,y,miniNumber)){
                    if(isOtherPlaceHaveMiniExcludeLine(pieceList,y,miniNumber)){
                        return outPieceList;
                    }
                }
            }
        }
        return null;
    }
    //找某一行备选数字，仅在一个宫格中存在的。
    public List<SudokuPiece> findMiniInSquareOfColumn(int miniNumber){
        //备选数字miniNumber
        //一列中，只有同一个宫格中才有备选数字miniNumber，如果宫格中本列以外其他位置存在备选数字，（那么删除本列其他宫格的备选数字。）

        List<SudokuPiece> outPieceList; //输出的满足条件的pieceList
        int maxY = SudokuBoard.getMaxY(sudokuType);
        int maxX = SudokuBoard.getMaxX(sudokuType);
        Log.v("debug",String.format("mini=%d,maxY=%d",miniNumber,maxY));
        for(int x=0; x<maxX; x++){  //逐列分析
            int maxYOfSquare = getMaxYOfSquare(sudokuType);
            int numberOfSquare = maxY/maxYOfSquare;
            //Log.v("debug",String.format("  y=%d",y));
            if(numberExistColumn(pieces[x][0],miniNumber)){ //本列已经有bigNumber=miniNumber的，忽略。
                continue;
            }
            //逐个宫格找是否有满足条件的宫格
            for(int i=0; i<numberOfSquare; i++){
                //获取该宫格的所有piece
                List<SudokuPiece> pieceList = getSquare(x,i*maxYOfSquare);
                //获取该宫格的本列piece
                outPieceList = getPiecesByColumn(pieceList,x,miniNumber);
                if(outPieceList == null || outPieceList.size() == 0){
                    continue;
                }
                //如果x列除了outPieceList以外，其他位置不包含miniNumber
                if(!isOtherPlaceOfColumnHaveMiniExcludeList(outPieceList,x,miniNumber)){
                    if(isOtherPlaceHaveMiniExcludeColumn(pieceList,x,miniNumber)){
                        return outPieceList;
                    }
                }
            }

            //百分比宫格，也是如此
            if(sudokuType == SudokuType.PERCENT){
                //获取该列百分比宫格的所有piece
                List<SudokuPiece> pieceList = getPercentSquareOfColumn(x);
                if(pieceList == null || pieceList.size() == 0){
                    continue;
                }
                //获取该宫格的本列piece
                outPieceList = getPiecesByColumn(pieceList,x,miniNumber);
                if(outPieceList == null || outPieceList.size() == 0){
                    continue;
                }
                //如果x列除了outPieceList以外，其他位置不包含miniNumber
                if(!isOtherPlaceOfColumnHaveMiniExcludeList(outPieceList,x,miniNumber)){
                    if(isOtherPlaceHaveMiniExcludeColumn(pieceList,x,miniNumber)){
                        return outPieceList;
                    }
                }
            }
            if(sudokuType == SudokuType.SUPER){
                //获取本列的上super宫格
                List<SudokuPiece> pieceList = getSuperSquareOfColumn(x,Direction.UP);
                if(pieceList == null || pieceList.size() == 0){
                    continue;
                }
                //获取该宫格的本列piece
                outPieceList = getPiecesByColumn(pieceList,x,miniNumber);
                if(outPieceList == null || outPieceList.size() == 0){
                    continue;
                }
                //如果x列除了outPieceList以外，其他位置不包含miniNumber
                if(!isOtherPlaceOfColumnHaveMiniExcludeList(outPieceList,x,miniNumber)){
                    if(isOtherPlaceHaveMiniExcludeColumn(pieceList,x,miniNumber)){
                        return outPieceList;
                    }
                }
                //获取本列的下super宫格
                pieceList = getSuperSquareOfColumn(x,Direction.DOWN);
                if(pieceList == null || pieceList.size() == 0){
                    continue;
                }
                //获取该宫格的本列piece
                outPieceList = getPiecesByColumn(pieceList,x,miniNumber);
                if(outPieceList == null || outPieceList.size() == 0){
                    continue;
                }
                //如果x列除了outPieceList以外，其他位置不包含miniNumber
                if(!isOtherPlaceOfColumnHaveMiniExcludeList(outPieceList,x,miniNumber)){
                    if(isOtherPlaceHaveMiniExcludeColumn(pieceList,x,miniNumber)){
                        return outPieceList;
                    }
                }
            }
        }
        return null;
    }
    //找对角线上，仅在一个宫格中存在的。
    public List<SudokuPiece> findMiniInSquareOfDiagonal(int miniNumber){
        List<SudokuPiece> outPieceList;
        if(sudokuType == SudokuType.PERCENT || sudokuType == SudokuType.X_STYLE) {
            //百分比对角线
            if(numberExistPercentDiagonal(pieces[0][0],miniNumber)){
                return null;
            }
            for (int i=0; i<3; i++) {
                List<SudokuPiece> pieceList = getSquare(i * 3, i * 3);
                outPieceList = getPiecesByPercentDiagnoal(pieceList,miniNumber);
                if(!isOtherPlaceOfPercentDiagonalHaveMiniExcludeList(outPieceList,miniNumber)){
                    if(isOtherPlaceHaveMiniExcludePercentDiagnoal(pieceList,miniNumber)){
                        return outPieceList;
                    }
                }
            }
        }
        if(sudokuType == SudokuType.X_STYLE){
            //X-反对角线
            if(numberExistDiagonal(pieces[0][8],miniNumber)){
                return null;
            }
            for (int i=0; i<3; i++) {
                List<SudokuPiece> pieceList = getSquare(i * 3, 8 - i * 3);
                outPieceList = getPiecesByXDiagnoal(pieceList,miniNumber);
                if(!isOtherPlaceOfXDiagonalHaveMiniExcludeList(outPieceList,miniNumber)){
                    if(isOtherPlaceHaveMiniExcludeXDiagnoal(pieceList,miniNumber)){
                        return outPieceList;
                    }
                }
            }
        }
        return null;
    }
    public List<SudokuPiece> findFish(int miniNumber){
        //N*N的fish

        for(int n=2;n<getMaxNumber(sudokuType)-1;n++){
            //0-maxNumber-1中n的组合
            List<Numbers> numbersList = Numbers.findAllCombinationFrom0(getMaxNumber(sudokuType)-1,n);
            //Log.v("debug",String.format("n=%d",n));
            for(int i=0; i<numbersList.size(); i++){
                Numbers numbers = numbersList.get(i);
                //Log.v("debug",String.format("   numbers:%s",numbers.toString()));
                //找出n列，备选数字仅出现在y=numbers行中
                Numbers numbersOfColumn = new Numbers(); //符合ya要求的列
                for(int x=0; x<getMaxX(sudokuType); x++){
                    Boolean otherPlaceHaveMini = false; //x列其他地方是否包含备选数字
                    Boolean haveMini = false; //该列是否包含备选数字
                    for(int y=0; y<getMaxY(sudokuType); y++){
                        if(pieces[x][y].getNumber() > 0 || !pieces[x][y].haveMiniNumber(miniNumber)){
                            continue;
                        }
                        if(!numbers.include(y)){ //y位置不在numbers的行中
                            otherPlaceHaveMini = true;
                            break;
                        }else{
                            haveMini = true;
                        }
                    }
                    if(otherPlaceHaveMini == false && haveMini){
                        numbersOfColumn.addNumber(x);
                    }
                }
                if(numbersOfColumn.count() != n){
                    continue;
                }
                //有n列满足要求，还要看看y=numbers[n]行，除了column[n]列以外，是否还有其他地方出现（可以删除）
                for(int j=0; j<numbers.count(); j++){
                    int y = numbers.getNumbers().get(j);
                    for(int x=0; x<getMaxX(sudokuType); x++){
                        if(pieces[x][y].getNumber() > 0 || !pieces[x][y].haveMiniNumber(miniNumber)){
                            continue;
                        }
                        if(!numbersOfColumn.include(x)){
                            //找到了其他地方还出现备选数字
                            Log.v("debug",String.format("Line:%s,column:%s",numbers,numbersOfColumn));
                            return getPiecesByLineAndColumn(numbers,numbersOfColumn);
                        }
                    }
                }

                //找出n行，备选数字仅出现在x=numbers列中
                Numbers numbersOfLine = new Numbers(); //符合要求的行
                for(int y=0; y<getMaxY(sudokuType); y++){
                    Boolean otherPlaceHaveMini = false; //y行其他地方是否包含备选数字
                    Boolean haveMini = false; //该行是否包含备选数字
                    for(int x=0; x<getMaxX(sudokuType); x++){
                        if(pieces[x][y].getNumber() > 0 || !pieces[x][y].haveMiniNumber(miniNumber)){
                            continue;
                        }
                        if(!numbers.include(x)){ //x位置不在numbers的列中
                            otherPlaceHaveMini = true;
                            break;
                        }else{
                            haveMini = true;
                        }
                    }
                    if(otherPlaceHaveMini == false && haveMini){
                        numbersOfLine.addNumber(y);
                    }
                }
                if(numbersOfLine.count() != n){
                    continue;
                }
                //有n行满足要求，还要看看x=numbers[n]列，除了numbersOfLine[n]行以外，是否还有其他地方出现（可以删除）
                for(int j=0; j<numbers.count(); j++){
                    int x = numbers.getNumbers().get(j);
                    for(int y=0; y<getMaxY(sudokuType); y++){
                        if(pieces[x][y].getNumber() > 0 || !pieces[x][y].haveMiniNumber(miniNumber)){
                            continue;
                        }
                        if(!numbersOfLine.include(y)){
                            //找到了其他地方还出现备选数字
                            Log.v("debug",String.format("return Line:%s,column:%s",numbersOfLine,numbers));
                            return getPiecesByLineAndColumn(numbersOfLine,numbers);
                        }
                    }
                }
            }
        }
        return  null;
    }
    public List<SudokuPiece> findFinnedFish(int miniNumber){
        //111   xxx  1C
        //  1
        //  1A  xxx 1B
        SudokuPiece pieceA,pieceB,pieceC;
        int maxX = getMaxX(sudokuType);
        int maxY = getMaxY(sudokuType);

        //行
        for(int y=0; y<maxY; y++){
            int count = 0;
            int column1 = -1, column2 = -1;
            //统计该行，备选数字miniNumber出现在列位置的个数
            for(int x=0; x<maxX; x++){
                if(pieces[x][y].getNumber() == 0 && pieces[x][y].haveMiniNumber(miniNumber)){
                    count++;
                    if(count > 2){
                        break;
                    }
                    if(column1 == -1){
                        column1 = x;
                    }else{
                        column2 = x;
                    }
                }
            }
            //找一行中，备选数字只出现在两列位置的。
            if (count != 2){
                continue;
            }
            //一、选择column1为A点，column2为B点
            //二、选择column2为A点，column1为B点
            for(int j=0; j<2; j++) {
                if(j== 0){
                    pieceA = pieces[column1][y];
                    pieceB = pieces[column2][y];
                }else{
                    pieceA = pieces[column2][y];
                    pieceB = pieces[column1][y];
                }
                //A和B在同一宫格，忽略
                if(isInTheSameSquare(pieceA,pieceB)){
                    continue;
                }
                Log.v("debug",String.format("mini=%d,行find A(%d,%d) and B(%d,%d)",miniNumber,pieceA.x,pieceA.y,pieceB.x,pieceB.y));
                //1,找C点,和B点同列
                for (int y1 = 0; y1 < maxY; y1++) {
                    if (pieces[pieceB.x][y1].getNumber() > 0 || !pieces[pieceB.x][y1].haveMiniNumber(miniNumber)) {
                        continue;
                    }
                    //忽略B点
                    if(y1 == pieceB.y){
                        continue;
                    }
                    pieceC = pieces[pieceB.x][y1];
                    if(isInTheSameSquare(pieceB,pieceC)){
                        continue;
                    }
                    Log.v("debug",String.format("find C(%d,%d)",pieceC.x,pieceC.y));
                    //找D点(C点所在行，A点列)的对应宫格。
                    List<SudokuPiece> pieceList = getSquare(pieceA.x, pieceC.y);
                    //找出宫格中C点所在行的备选数字
                    List<SudokuPiece> outPieceList = getPiecesByLine(pieceList, pieceC.y, miniNumber);
                    //除了C点，及outPieceList以外，yOfPointC行其他位置不含备选数字miniNumber
                    Boolean isOtherPlaceHaveMini = false;
                    for (int x = 0; x < maxX; x++) {
                        //除了outPieceList以外
                        if (isInPieceList(outPieceList, pieces[x][pieceC.y])) {
                            continue;
                        }
                        //除了C点所在列
                        if (x == pieceC.x) {
                            continue;
                        }
                        if (pieces[x][pieceC.y].getNumber() == 0 && pieces[x][pieceC.y].haveMiniNumber(miniNumber)) {
                            isOtherPlaceHaveMini = true;
                            break;
                        }
                    }
                    if (isOtherPlaceHaveMini) {
                        continue;
                    }
                    //宫格中，A点所在列位置，C点所在行位置以外。是否还有其他备选数字
                    for (int i = 0; i < pieceList.size(); i++) {
                        SudokuPiece piece = pieceList.get(i);
                        if (piece.x != pieceA.x) {
                            continue;
                        }
                        if (piece.y == pieceC.y) {
                            continue;
                        }
                        if (piece.getNumber() == 0 && piece.haveMiniNumber(miniNumber)) {
                            outPieceList.add(piece);
                            outPieceList.add(pieceA); //A
                            outPieceList.add(pieceB); //B
                            outPieceList.add(pieceC); //C
                            Log.v("debug",String.format("A:%d,%d",pieceA.x,pieceA.y));
                            Log.v("debug",String.format("B:%d,%d",pieceB.x,pieceB.y));
                            Log.v("debug",String.format("C:%d,%d",pieceC.x,pieceC.y));
                            Log.v("debug",String.format("piece:%d,%d",piece.x,piece.y));
                            return outPieceList;
                        }
                    }
                }
            }
        }
        //列
        //   x       1
        //   A1     D1 1
        //   x
        //   x
        //   B1    C1
        for(int x=0; x<maxX; x++){
            int count = 0;
            int line1 = -1, line2 = -1;
            //统计该列，备选数字miniNumber出现在行位置的个数
            for(int y=0; y<maxY; y++){
                if(pieces[x][y].getNumber() == 0 && pieces[x][y].haveMiniNumber(miniNumber)){
                    count++;
                    if(count > 2){
                        break;
                    }
                    if(line1 == -1){
                        line1 = y;
                    }else{
                        line2 = y;
                    }
                }
            }
            //找一列中，备选数字只出现在两行位置的。
            if (count != 2){
                continue;
            }
            //在同一宫格，忽略
            if(isInTheSameSquare(pieces[x][line1],pieces[x][line2])){
                continue;
            }

            //一、选择line1为A点，line2为B点
            //二、选择line2为A点，line1为B点
            for(int j=0; j<2; j++) {
                if(j== 0){
                    pieceA = pieces[x][line1];
                    pieceB = pieces[x][line2];
                }else{
                    pieceA = pieces[x][line2];
                    pieceB = pieces[x][line1];
                }
                Log.v("debug",String.format("mini=%d,列find A(%d,%d) and B(%d,%d)",miniNumber,pieceA.x,pieceA.y,pieceB.x,pieceB.y));
                //1,找C点，和B点同行,含有miniNumber的piece
                for (int x1 = 0; x1 < maxX; x1++) {
                    if (pieces[x1][pieceB.y].getNumber() != 0 || !pieces[x1][pieceB.y].haveMiniNumber(miniNumber)) {
                        continue;
                    }
                    //忽略B点
                    if(x1 == pieceB.x){
                        continue;
                    }
                    pieceC = pieces[x1][pieceB.y];
                    if(isInTheSameSquare(pieceB,pieceC)){
                        continue;
                    }
                    Log.v("debug",String.format("find C(%d,%d)",pieceC.x,pieceC.y));
                    //找D点C.x,A.y)所在宫格。
                    List<SudokuPiece> pieceList = getSquare(pieceC.x,pieceA.y);
                    //找出宫格中C所在列的备选数字
                    List<SudokuPiece> outPieceList = getPiecesByColumn(pieceList,pieceC.x, miniNumber);
                    //除了C点，及outPieceList以外，C点所在列其他位置不含备选数字miniNumber
                    Boolean isOtherPlaceHaveMini = false;
                    for (int y = 0; y < maxY; y++) {
                        //除了outPieceList以外
                        if (isInPieceList(outPieceList, pieces[pieceC.x][y])) {
                            continue;
                        }
                        //除了C点外
                        if (y == pieceC.y) {
                            continue;
                        }
                        if (pieces[pieceC.x][y].getNumber() == 0 && pieces[pieceC.x][y].haveMiniNumber(miniNumber)) {
                            isOtherPlaceHaveMini = true;
                            break;
                        }
                    }
                    if (isOtherPlaceHaveMini) {
                        continue;
                    }
                    //宫格中，A点所在行，C点所在列位置以外。是否还有其他备选数字
                    for (int i = 0; i < pieceList.size(); i++) {
                        SudokuPiece piece = pieceList.get(i);
                        if (piece.y != pieceA.y) {
                            continue;
                        }
                        if (piece.x == pieceC.x) {
                            continue;
                        }
                        if (piece.getNumber() == 0 && piece.haveMiniNumber(miniNumber)) {
                            outPieceList.add(piece);
                            outPieceList.add(pieceA); //A
                            outPieceList.add(pieceB); //B
                            outPieceList.add(pieceC); //C
                            return outPieceList;
                        }
                    }
                }


            }
        }
        return null;
    }
    public List<SudokuPiece> findW_Wing(){
        //仅适用于9宫格
        if(sudokuType == SudokuType.FOUR || sudokuType == SudokuType.SIX){
            return null;
        }
        for(int i=0; i<81; i++){
            int x = i/9;
            int y = i%9;
            //找第一个xy
            if(pieces[x][y].getNumber() > 0 || pieces[x][y].getMiniNumbers().length != 2){
                continue;
            }
            int miniNumberX = pieces[x][y].getMiniNumbers()[0];
            int miniNumberY = pieces[x][y].getMiniNumbers()[1];
            //往前找第二个xy
            for(int j=i+1; j<81; j++){
                int x2 = j/9;
                int y2 = j%9;
                if(pieces[x2][y2].getNumber() > 0 || pieces[x2][y2].getMiniNumbers().length != 2){
                    continue;
                }
                if(!pieces[x2][y2].haveMiniNumber(miniNumberX) || !pieces[x2][y2].haveMiniNumber(miniNumberY)){
                    continue;
                }
                //两个xy满足以下条件：
                // 1、不在一条线上
                // 2、不在一个宫格中
                // 3、必须在同一水平宫格，或者垂直宫格上。

                if(x ==x2 || y ==y2){
                    continue;
                }
                Log.v("debug",String.format("piece1:%d,%d; piece2:%d,%d; xy=%d%d",x,y,x2,y2,miniNumberX,miniNumberY ));
                //一、同一水平宫格上
                if(y/3 == y2/3 && x/3 != x2/3){
                    int x3, y3;
                    //找两个点以外的第三个宫格
                    if(x/3 != 0 && x2/3 != 0){
                        x3 = 0;
                    }else if(x/3 != 1 && x2/3 != 1){
                        x3 = 3;
                    }else{
                        x3 = 6;
                    }
                    //找y,y2以外的第三条线
                    y3 = y/3 * 3; //先去宫格的第一条线
                    if(y != y3 && y2 != y3){
                        y3 = y3 + 0;
                    }else if(y != y3+1 && y2 != y3+1){
                        y3 = y3 + 1;
                    }else{
                        y3 = y3 + 2;
                    }
                    //第三个宫格的y3行，必须含有一个x/y。但不能同时含有;
                    Boolean haveX = false, haveY = false;
                    for(int k=0; k<3; k++){
                        SudokuPiece piece = pieces[x3+k][y3];
                        if(piece.getNumber() == miniNumberX || piece.haveMiniNumber(miniNumberX)){
                            haveX = true;
                        }
                        if(piece.getNumber() == miniNumberY || piece.haveMiniNumber(miniNumberY)){
                            haveY = true;
                        }
                    }

                    if(haveX && !haveY){
                        //有x没有y
                        //那么xy组成x的强链（不能同时为假)
                        List<SudokuPiece> outPieceList = getWeakChainPieces(pieces[x][y],pieces[x2][y2],miniNumberX);
                        if(outPieceList != null && outPieceList.size() != 0){
                            outPieceList.add(pieces[x][y]);
                            outPieceList.add(pieces[x2][y2]);
                            hintMiniNumber = miniNumberX;
                            return outPieceList;
                        }
                    }else if(haveY && !haveX){
                        //有y没有x
                        //那么xy组成y的强链（不能同时为假)
                        List<SudokuPiece> outPieceList = getWeakChainPieces(pieces[x][y],pieces[x2][y2],miniNumberY);
                        if(outPieceList != null && outPieceList.size() != 0){
                            outPieceList.add(pieces[x][y]);
                            outPieceList.add(pieces[x2][y2]);
                            hintMiniNumber = miniNumberY;
                            return outPieceList;
                        }
                    }
                }
                //同一垂直宫格上
                if(y/3 != y2/3 && x/3 == x2/3){
                    int x3, y3;
                    //找两个点以外的第三个宫格
                    if(y/3 != 0 && y2/3 != 0){
                        y3 = 0;
                    }else if(y/3 != 1 && y2/3 != 1){
                        y3 = 3;
                    }else{
                        y3 = 6;
                    }
                    //找y,y2以外的第三条线
                    x3 = x/3 * 3; //先去宫格的第一条线
                    if(x != x3 && x2 != x3){
                        x3 = x3 + 0;
                    }else if(x != x3+1 && x2 != x3+1){
                        x3 = x3 + 1;
                    }else{
                        x3 = x3 + 2;
                    }
                    //第三个宫格的y3行，必须含有一个x/y。但不能同时含有;
                    Boolean haveX = false, haveY = false;
                    for(int k=0; k<3; k++){
                        SudokuPiece piece = pieces[x3][y3+k];
                        if(piece.getNumber() == miniNumberX || piece.haveMiniNumber(miniNumberX)){
                            haveX = true;
                        }
                        if(piece.getNumber() == miniNumberY || piece.haveMiniNumber(miniNumberY)){
                            haveY = true;
                        }
                    }

                    if(haveX && !haveY){
                        //有x没有y
                        //那么xy组成x的强链（不能同时为假)
                        List<SudokuPiece> outPieceList = getWeakChainPieces(pieces[x][y],pieces[x2][y2],miniNumberX);
                        if(outPieceList != null && outPieceList.size() != 0){
                            outPieceList.add(pieces[x][y]);
                            outPieceList.add(pieces[x2][y2]);
                            hintMiniNumber = miniNumberX;
                            return outPieceList;
                        }
                    }else if(haveY && !haveX){
                        //有y没有x
                        //那么xy组成y的强链（不能同时为假)
                        List<SudokuPiece> outPieceList = getWeakChainPieces(pieces[x][y],pieces[x2][y2],miniNumberY);
                        if(outPieceList != null && outPieceList.size() != 0){
                            outPieceList.add(pieces[x][y]);
                            outPieceList.add(pieces[x2][y2]);
                            hintMiniNumber = miniNumberY;
                            return outPieceList;
                        }
                    }
                }
            }
        }
        return null;
    }
    public List<SudokuPiece> findHiddenUniqueRectangle(){
        //    A:XY*   =x=      B:X*
        //     ||Xx               |
        //    C:X*   -x-      D:XY
        //  x: A==B,A==C,D=--B,D--C，可以删除D点的Y
        //  假设D是X, 那么D已经不是Y了
        //  假设B是X，那么A就是Y，C是X，如果D也是Y，构成dead pattern，所以D不能是Y
        // 所以无论如何D不能是Y

        //1、先找D点,特征是:(某miniNumber，在行，列，对角线上仅有一个强链点）
        for(int x=0; x<getMaxX(sudokuType); x++){
            for(int y=0; y<getMaxY(sudokuType); y++){
                SudokuPiece pieceA = pieces[x][y];
                for(int miniX = 1; miniX <=getMaxNumber(sudokuType); miniX++) {
                    List<SudokuPiece> strongList = getStrongChainPieces(pieceA, miniX);
                    if(strongList == null || strongList.size() < 2){
                        continue;
                    }
                    //随机选择两个piece
                    List<Numbers> numbersList = Numbers.findAllCombinationFrom0(strongList.size()-1,2);
                    for(int i=0; i<numbersList.size(); i++) {
                        Numbers numbers = numbersList.get(i);
                        SudokuPiece pieceB = strongList.get(numbers.getNumbers().get(0));
                        SudokuPiece pieceC = strongList.get(numbers.getNumbers().get(1));
                        //获取D
                        List<SudokuPiece> pieceDList = getWeakChainPieces(pieceB,pieceC, miniX);
                        if(pieceDList == null){
                            continue;
                        }
                        for(int j=0; j<pieceDList.size(); j++){
                            SudokuPiece pieceD = pieceDList.get(j);
                            //排除掉A
                            if(pieceD.x == pieceA.x || pieceD.y == pieceA.y){
                                continue;
                            }
                            if(pieceD.getMiniNumbers().length != 2){
                                continue;
                            }
                            //获取Y
                            int miniY;
                            if(pieceD.getMiniNumbers()[0] == miniX){
                                miniY = pieceD.getMiniNumbers()[1];
                            }else{
                                miniY = pieceD.getMiniNumbers()[0];
                            }
                            if(pieceA.haveMiniNumber(miniY)){
                                List<SudokuPiece> outPieceList = new ArrayList<>();
                                outPieceList.add(pieceA);
                                outPieceList.add(pieceB);
                                outPieceList.add(pieceC);
                                outPieceList.add(pieceD);
                                hintMiniNumber = miniY;
                                return outPieceList;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    public List<SudokuPiece> findXYWing() {
        //找A:xy-y-B:yz-z-C:xz，如：
        // A:xy       B:yz
        //  xx        C:xz

        for(int x1=0; x1<getMaxX(sudokuType); x1++){
            for(int y1=0; y1<getMaxY(sudokuType); y1++){
                //找A：xy
                if(pieces[x1][y1].getNumber() > 0 || pieces[x1][y1].getMiniNumbers().length != 2){
                    continue;
                }
                SudokuPiece pieceA = pieces[x1][y1];
                int X,Y;
                for(int i=0; i<2; i++){
                    if(i==0){
                        X = pieceA.getMiniNumbers()[0];
                        Y = pieceA.getMiniNumbers()[1];
                    }else{
                        X = pieceA.getMiniNumbers()[1];
                        Y = pieceA.getMiniNumbers()[0];
                    }
                    Log.v("debug","find xy-wing A:"+pieceA.toDBString());
                    //获取A点的y弱链
                    List<SudokuPiece> weakListOfA = getWeakChainPieces(pieceA,Y);
                    if(weakListOfA == null){ continue; }
                    //找B点:YZ
                    for(int j=0; j<weakListOfA.size(); j++){
                        SudokuPiece pieceB = weakListOfA.get(j);
                        if(pieceB.getNumber() > 0 || pieceB.getMiniNumbers().length != 2){
                            continue;
                        }
                        int Z;
                        if(pieceB.getMiniNumbers()[0] == Y){
                            Z = pieceB.getMiniNumbers()[1];
                        }else{
                            Z = pieceB.getMiniNumbers()[0];
                        }
                        if(Z == X){
                            continue;
                        }
                        Log.v("debug","find xy-wing B:"+pieceB.toDBString());
                        //获取B点的z弱链
                        List<SudokuPiece> weakListOfB = getWeakChainPieces(pieceB,Z);
                        if(weakListOfB == null ) { continue; }
                        //找C点：XZ
                        for(int k=0; k<weakListOfB.size(); k++){
                            SudokuPiece pieceC = weakListOfB.get(k);
                            if(pieceC.getNumber() > 0 || pieceC.getMiniNumbers().length != 2){
                                continue;
                            }
                            if(!pieceC.haveMiniNumber(X) || !pieceC.haveMiniNumber(Z)){
                                continue;
                            }
                            if(pieceC.x == pieceA.x && pieceC.y == pieceA.y){ continue; }
                            Log.v("debug","find xy-wing C:"+pieceC.toDBString());
                            //找A,C点的x弱链
                            List<SudokuPiece> weakListOfAC = getWeakChainPieces(pieceA,pieceC,X);
                            if(weakListOfAC != null && weakListOfAC.size() > 0){
                                Log.v("debug","AClist:"+pieceListToString(weakListOfAC));
                                List<SudokuPiece> outPieceList = new ArrayList<>();
                                outPieceList.add(pieceA);
                                outPieceList.add(pieceB);
                                outPieceList.add(pieceC);
                                hintMiniNumber = X;
                                return outPieceList;
                            }
                        }

                    }
                }
            }
        }
        return null;
    }
    public List<SudokuPiece> findXYZWing(){
        //找XYZWing 如：
        // A:xy      B:xyz
        //           xx(不能为x)
        //          C:xz

        for(int x1=0; x1<getMaxX(sudokuType); x1++){
            for(int y1=0; y1<getMaxY(sudokuType); y1++){
                //找A：xy
                if(pieces[x1][y1].getNumber() > 0 || pieces[x1][y1].getMiniNumbers().length != 2){
                    continue;
                }
                SudokuPiece pieceA = pieces[x1][y1];
                int X,Y;
                for(int i=0; i<2; i++){
                    if(i==0){
                        X = pieceA.getMiniNumbers()[0];
                        Y = pieceA.getMiniNumbers()[1];
                    }else{
                        X = pieceA.getMiniNumbers()[1];
                        Y = pieceA.getMiniNumbers()[0];
                    }
                    Log.v("debug",String.format("find xyz-wing A(xy=%d%d):",X,Y)+pieceA.toDBString());
                    //获取A点的y弱链
                    List<SudokuPiece> weakListOfA = getWeakChainPieces(pieceA,Y);
                    if(weakListOfA == null){ continue; }
                    //找B点:XYZ
                    for(int j=0; j<weakListOfA.size(); j++){
                        SudokuPiece pieceB = weakListOfA.get(j);
                        if(pieceB.getNumber() > 0 || pieceB.getMiniNumbers().length != 3){
                            continue;
                        }
                        //必须包含X,Y
                        if(!pieceB.haveMiniNumber(X) || !pieceB.haveMiniNumber(Y)){
                            continue;
                        }
                        int Z=0;
                        for(int n=0; n<pieceB.getMiniNumbers().length; n++){
                            if(pieceB.getMiniNumbers()[n] != X && pieceB.getMiniNumbers()[n] != Y){
                                Z = pieceB.getMiniNumbers()[n];
                            }
                        }
                        Log.v("debug",String.format("find xyz-wing B(z=%d):",Z)+pieceB.toDBString());
                        //获取B点的z弱链
                        List<SudokuPiece> weakListOfB = getWeakChainPieces(pieceB,Z);
                        if(weakListOfB == null ) { continue; }
                        //找C点：XZ
                        for(int k=0; k<weakListOfB.size(); k++){
                            SudokuPiece pieceC = weakListOfB.get(k);
                            if(pieceC.getNumber() > 0 || pieceC.getMiniNumbers().length != 2){
                                continue;
                            }
                            if(!pieceC.haveMiniNumber(X) || !pieceC.haveMiniNumber(Z)){
                                continue;
                            }
                            if(pieceC.x == pieceA.x && pieceC.y == pieceA.y){ continue; }
                            Log.v("debug","find xyz-wing C:"+pieceC.toDBString());
                            //找A,B,C点的X弱链
                            //找A,C点的x弱链
                            List<SudokuPiece> weakListOfAC = getWeakChainPieces(pieceA,pieceC,X);
                            if(weakListOfAC == null || weakListOfAC.size() == 0){
                                continue;
                            }
                            Log.v("debug","AClist:"+pieceListToString(weakListOfAC));
                            //在weakListOfAC中找和B存在弱链关系的点
                            List<SudokuPiece> weakListOfABC = getWeakChainPieces(weakListOfAC,pieceB,X);
                            if(weakListOfABC != null && weakListOfABC.size() > 0){
                                Log.v("debug","ABClist:"+pieceListToString(weakListOfABC));
                                List<SudokuPiece> outPieceList = new ArrayList<>();
                                outPieceList.add(pieceA);
                                outPieceList.add(pieceB);
                                outPieceList.add(pieceC);
                                hintMiniNumber = X;
                                return outPieceList;
                            }
                        }

                    }
                }
            }
        }
        return null;
    }
    public List<SudokuPiece> findWXYZWing(){
        //wxyz,和3个piece存在z弱链关系(并且除了Z以外,仅包含wxy,而且去除Z以后,会形成WXY),除此之外还存在其他piece和这4个节点存在z弱链关系.那么这些piece就可以删除
        for(int x=0; x<getMaxX(sudokuType); x++){
            for(int y=0; y<getMaxY(sudokuType); y++){
                //找WXYZ, pieceA
                SudokuPiece pieceA = pieces[x][y];
                if(pieceA.getNumber() > 0 || pieceA.getMiniNumbers().length != 4){
                    continue;
                }
                for(int i=0; i<4; i++){
                    int Z = pieceA.getMiniNumbers()[i];
                    List<Integer> wxyMiniNumberList = new ArrayList<>();
                    for(int j=0; j<4; j++){
                        if(j != i){
                            wxyMiniNumberList.add(pieceA.getMiniNumbers()[j]);
                        }
                    }
                    //校验wxy是不是3个数字长度
                    if(wxyMiniNumberList.size() != 3){
                        Log.e("debug","error:wxyList.size != 3");
                        return null;
                    }
                    //获取piaceA的z弱链pieces
                    List<SudokuPiece> weakListOfA = getWeakChainPieces(pieceA,Z);
                    if(weakListOfA == null || weakListOfA.size() <= 3){
                        continue;
                    }
                    //寻找仅包含wxyz的节点
                    List<SudokuPiece> wxyzPieceList = new ArrayList<>();
                    for(int j=0; j<weakListOfA.size(); j++){
                        SudokuPiece tempPiece = weakListOfA.get(j);
                        Boolean haveOtherMiniNumber = false; //除了wxyz以外,是否包含其他备选数字
                        int[] miniNumbers = tempPiece.getMiniNumbers();
                        for(int k=0; k<miniNumbers.length; k++){
                            if(pieceA.haveMiniNumber(miniNumbers[k])){ //包含了其他备选数字
                                haveOtherMiniNumber = true;
                                break;
                            }
                        }
                        if(haveOtherMiniNumber){
                            continue;
                        }
                        //仅包含wxyz的piece
                        wxyzPieceList.add(tempPiece);
                    }
                    if(wxyzPieceList.size() < 3){
                        continue;
                    }
                    //随机选择3个节点
                    /*List<Integer> intList = new ArrayList<>();
                    for(int j=0; j<wxyzPieceList.size(); j++){
                        intList.add(j);
                    }
                    List<Numbers> numbersList = Numbers.findAllCombination(intList,3);*/
                    List<Numbers> numbersList = Numbers.findAllCombinationFrom0(wxyzPieceList.size()-1,3);

                    for(int j=0; j<numbersList.size(); j++){
                        //numbers的3个数字代表wxyzPieceList链表的序号
                        Numbers numbers = numbersList.get(i);
                        List<SudokuPiece> pieceBCD = new ArrayList<>();  //存储pieceB/C/D
                        for(int k=0; k<numbers.getNumbers().size(); k++){
                            int index = numbers.getNumbers().get(i);
                            pieceBCD.add(wxyzPieceList.get(index));
                        }
                        //测试这3个piece,如果去除z以后,是否构成wxy
                        if(!isWXYAfterRemoveZ(pieceBCD,wxyMiniNumberList,Z)) {
                            continue;
                        }
                        //找出还有其他piece和pieceBCD都构成z弱链关系(在同一个unit中)
                        for(int k=0; k<weakListOfA.size(); k++){
                            SudokuPiece pieceE = weakListOfA.get(k);
                            if(isInPieceList(pieceBCD,pieceE)){
                                continue;
                            }
                            if(isIntheSameUnit(pieceE,pieceBCD.get(0))
                                && isIntheSameUnit(pieceE,pieceBCD.get(1))
                                && isIntheSameUnit(pieceE,pieceBCD.get(2))){
                                pieceBCD.add(pieceA);
                                hintMiniNumber = Z;
                                return pieceBCD;
                            }

                        }
                    }
                }
            }
        }
        return null;
    }
    public Boolean findChainPieces(){
        //找链路最短的链
        bestChainList = null;
        for(int n=0; n<getMaxNumber(sudokuType); n++){
            findChainPieces(n);
        }
        if(bestChainList != null && bestChainList.size() > 0){
            return true;
        }
        return false;
    }
    public List<SudokuPiece> findChainPieces(int miniNumber){
        //遍历寻找可应用的链,发现更多的链路存储在bestChainList。
        int maxX = getMaxX(sudokuType);
        int maxY = getMaxY(sudokuType);
        //1、建立链关系网
        chainNet = createChainNet(miniNumber);
        hintMiniNumber = miniNumber;
        printChainNet();
        //2、遍历搜索链
        for(int x=0; x<maxX; x++){
            for(int y=0; y<maxY; y++){
                if(pieces[x][y].getNumber() > 0 || !pieces[x][y].haveMiniNumber(miniNumber)){
                    continue;
                }
                //搜索:从强链开始
                chainList = new ArrayList<>();
                startFlag = CHAIN_STRONG_ALTERNATE;
                /*if(searchNextChainNode(pieces[x][y], CHAIN_STRONG_ALTERNATE)){
                    return chainList;
                }*/
                searchNextChainNode(pieces[x][y], CHAIN_STRONG_ALTERNATE);
                //搜索,从弱链开始
                chainList = new ArrayList<>();
                startFlag = CHAIN_WEAK_ALTERNATE;
                /*if(searchNextChainNode(pieces[x][y], CHAIN_WEAK_ALTERNATE)){
                    return chainList;
                }*/
                searchNextChainNode(pieces[x][y], CHAIN_WEAK_ALTERNATE);
            }
        }
        return null;
    }
    public Boolean findXYChain(){
        //寻找xy_chain (xy)--(yz)--(zw)--(wx),那么删除起点和终点的x -weaklist
        //找到就返回true,结果存储在chainList,否则返回false

        bestChainList = null;
        for(int x1=0; x1<getMaxX(sudokuType); x1++){
            for(int y1=0; y1<getMaxY(sudokuType); y1++){
                //找起点：xy
                if(pieces[x1][y1].getNumber() > 0 || pieces[x1][y1].getMiniNumbers().length != 2){
                    continue;
                }
                SudokuPiece startPiece = pieces[x1][y1];

                for(int i=0; i<2; i++){
                    int X,Y;
                    chainList = new ArrayList<>();
                    if(i==0){
                        X = startPiece.getMiniNumbers()[0];
                        Y = startPiece.getMiniNumbers()[1];
                    }else{
                        X = startPiece.getMiniNumbers()[1];
                        Y = startPiece.getMiniNumbers()[0];
                    }
                    chainList.add(startPiece);
                    hintMiniNumber = X;
                    Log.v("debug",String.format("start search xy-chain(X=%d) A:",X,startPiece.toDBString()));
                    //递归寻找xy-chain
                    /*if(searchNextXYChainNode(startPiece,Y)){
                        return true;
                    }*/
                    searchNextXYChainNode(startPiece,Y);
                }
            }
        }
        if(bestChainList != null){
            return true;
        }
        return false;
    }
    private ChainNode[][] createChainNet(int miniNumber){
        //构建链关系网
        int maxX = getMaxX(sudokuType);
        int maxY = getMaxY(sudokuType);
        ChainNode[][] chainNet = new ChainNode[maxX][maxY];
        for(int x=0; x<maxX; x++){
            for(int y=0; y<maxY; y++){
                if(pieces[x][y].getNumber() == 0 && pieces[x][y].haveMiniNumber(miniNumber)) {
                    chainNet[x][y] = new ChainNode(pieces[x][y],
                            getWeakChainPieces(pieces[x][y], miniNumber),
                            getStrongChainPieces(pieces[x][y], miniNumber));
                }else{
                    chainNet[x][y] = null;
                }
            }
        }
        return chainNet;
    }
    private Boolean searchNextXYChainNode(SudokuPiece currentPiece,int currentMiniNumber){
        //currentPiece for example: xy,
        //currentMiniNumber for example: y
        List<SudokuPiece> weakList = getWeakChainPieces(currentPiece,currentMiniNumber);
        if(weakList == null){ return false; }
        //找nextPiece
        for(int j=0; j<weakList.size(); j++) {
            SudokuPiece nextPiece = weakList.get(j);
            //必须仅含两个备选数字
            if (nextPiece.getNumber() > 0 || nextPiece.getMiniNumbers().length != 2) {
                continue;
            }
            // 排除掉chainList中重复的piece
            if(isInPieceList(chainList,nextPiece)){
                continue;
            }
            //push
            chainList.add(nextPiece);
            //获取下一个miniNumber,非currentMiniNumber
            int nextMiniNumber;
            if (nextPiece.getMiniNumbers()[0] == currentMiniNumber) {
                nextMiniNumber = nextPiece.getMiniNumbers()[1];
            } else {
                nextMiniNumber = nextPiece.getMiniNumbers()[0];
            }
            //如果nextMiniNumber等于ChainMiniNumber,那么获取起点和终点交叉的weakList
            if (nextMiniNumber == hintMiniNumber) {
                List<SudokuPiece> outPieceList = getWeakChainPieces(chainList.get(0),nextPiece, hintMiniNumber);
                if(outPieceList != null && outPieceList.size() > 0){
                    Log.v("debug","to delete :"+pieceListToString(outPieceList));
                    //return true;
                    if(bestChainList == null){
                        bestChainList = copyPieceList(chainList);
                        bestChainX = hintMiniNumber;
                    }else{
                        if(bestChainList.size() > chainList.size()){
                            bestChainList = copyPieceList(chainList);
                            bestChainX = hintMiniNumber;
                        }
                    }
                }
                //否则就回退
                chainList.remove(chainList.size()-1);
                return false;
            }
            //继续搜索下一个节点
            if(searchNextXYChainNode(nextPiece,nextMiniNumber)){
                return true;
            }
            //pop
            chainList.remove(chainList.size()-1);
        }
        return false;
    }
    private Boolean searchNextChainNode(SudokuPiece piece, int flag){
        //搜索piece的下一个强/弱关系节点，如果找到可应用的链就存储在bestChainList
        if(piece == null){
            return false;
        }
        //piece已经存在链表中，就不再搜索下去,避免死循环。
        if(isInPieceList(chainList,piece)){
            SudokuPiece startPiece = chainList.get(0);
            if(piece.x == startPiece.x && piece.y == startPiece.y) {
                if (chainList.size() > 2) {
                    //构成了一个环
                    if (chainList.size() % 2 == 1) {
                        //(不包括最后一个）链表的个数是奇数，才会出现强进强出或弱进弱出
                        if (startFlag == CHAIN_STRONG_ALTERNATE) {
                            //强进强出  A==A，A为真
                            //chainList.add(piece);
                            Log.v("debug", String.format("list add cycle Strong:%d,%d", piece.x, piece.y));
                            if(bestChainList == null){
                                bestChainList = copyPieceList(chainList);
                                bestChainList.add(piece);
                                bestChainX = hintMiniNumber;
                                bestStartFlag = startFlag;
                            }else{
                                if(bestChainList.size() > chainList.size()){
                                    bestChainList = copyPieceList(chainList);
                                    bestChainList.add(piece);
                                    bestChainX = hintMiniNumber;
                                    bestStartFlag = startFlag;
                                }
                            }
                            //chainList.remove(piece);
                            return true;
                        } else {
                            //弱进弱出 A--A，A为假
                            //chainList.add(piece);
                            Log.v("debug", String.format("list add cycle weak:%d,%d", piece.x, piece.y));
                            if(bestChainList == null){
                                bestChainList = copyPieceList(chainList);
                                bestChainList.add(piece);
                                bestChainX = hintMiniNumber;
                                bestStartFlag = startFlag;
                            }else{
                                if(bestChainList.size() > chainList.size()){
                                    bestChainList = copyPieceList(chainList);
                                    bestChainList.add(piece);
                                    bestChainX = hintMiniNumber;
                                    bestStartFlag = startFlag;
                                }
                            }
                            //chainList.remove(piece);
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        /*if(flag == CHAIN_STRONG_ALTERNATE) {
            Log.v("debug", String.format("list add STRONG:%d,%d", piece.x, piece.y));
        }else{
            Log.v("debug", String.format("list add WEAK:%d,%d", piece.x, piece.y));
        }*/
        chainList.add(piece);         //piece加入list
        //如果有应用就返回true
        if(chainList.size() > 2 && chainList.size() % 2 == 0){
            if(startFlag == CHAIN_STRONG_ALTERNATE){
                //强进强出，那么起点A和当前pieceD构成强链关系，可以删除A,D之间交叉的弱链
                List<SudokuPiece> outPieceList = getWeakChainPieces(chainList.get(0),piece, hintMiniNumber);
                if(outPieceList != null && outPieceList.size() > 0){
                    Log.v("debug","return true for strong");
                    if(bestChainList == null){
                        bestChainList = copyPieceList(chainList);
                        bestChainX = hintMiniNumber;
                        bestStartFlag = startFlag;
                    }else{
                        if(bestChainList.size() > chainList.size()){
                            bestChainList = copyPieceList(chainList);
                            bestChainX = hintMiniNumber;
                            bestStartFlag = startFlag;
                        }
                    }
                    chainList.remove(piece);
                    return true;
                }
            }
        }
        //否则继续搜索下一个节点
        int x = piece.x;
        int y = piece.y;
        if(flag == CHAIN_STRONG_ALTERNATE){
            if(chainNet[x][y].strongList != null){
                List<SudokuPiece> strongList = chainNet[x][y].strongList;
                for(int i=0; i<strongList.size(); i++){
                    /*if(searchNextChainNode(strongList.get(i), CHAIN_WEAK_ALTERNATE)){
                        return true;
                    }*/
                    searchNextChainNode(strongList.get(i), CHAIN_WEAK_ALTERNATE);
                }
            }
        }else{
            if(chainNet[x][y].weakList != null){
                List<SudokuPiece> weakList = chainNet[x][y].weakList;
                for(int i=0; i<weakList.size(); i++){
                    /*if(searchNextChainNode(weakList.get(i), CHAIN_STRONG_ALTERNATE)){
                        return true;
                    }*/
                    searchNextChainNode(weakList.get(i), CHAIN_STRONG_ALTERNATE);
                }
            }
        }
        //回退，删除最后一个节点
        if(chainList.size() >= 1) {
            chainList.remove(chainList.size() - 1);
            /*if(flag == CHAIN_STRONG_ALTERNATE) {
                Log.v("debug", String.format("list pop STRONG:%d,%d", piece.x, piece.y));
            }else{
                Log.v("debug", String.format("list pop WEAK:%d,%d", piece.x, piece.y));
            }*/
        }
        return false;
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
                }else{
                    //不仅仅包含组合数字的piece,找出包含组合数字中的数字
                    for(int k=0; k<numbers.getNumbers().size(); k++){
                        if(piece.haveMiniNumber(numbers.getNumbers().get(k))){
                            hintMiniNumber = numbers.getNumbers().get(k);
                        }
                    }
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
    private List<SudokuPiece> getWeakChainPieces(SudokuPiece piece1,SudokuPiece piece2,int miniNumber){
        //同时和piece1，piece2存在弱链关系的pieces
        List<SudokuPiece>  pieceList,outPieceList;

        //获取piece1的弱链关系pieces
        pieceList = getWeakChainPieces(piece1,miniNumber);
        Log.v("debug",String.format("piece1:%d,%d,miniNumber=%d",piece1.x,piece1.y,miniNumber));
        Log.v("debug","piece1 weakchain:"+pieceListToString(pieceList));
        //返回pieceList中和piece2存在弱链关系的pieces
        outPieceList = getWeakChainPieces(pieceList,piece2,miniNumber);
        if(outPieceList == null || outPieceList.size() == 0){
            return null;
        }
        //删除piece1
        for(int i=0; i<outPieceList.size(); i++){
            if(outPieceList.get(i).x == piece1.x && outPieceList.get(i).y == piece1.y){
                outPieceList.remove(i);
                i--;
            }
            if(outPieceList.get(i).x == piece2.x && outPieceList.get(i).y == piece2.y){
                outPieceList.remove(i);
                i--;
            }
        }
        if(outPieceList == null || outPieceList.size() == 0){
            return null;
        }
        Log.v("debug",String.format("piece2:%d,%d",piece2.x,piece2.y));
        Log.v("debug","piece2 weakchain:"+pieceListToString(outPieceList));
        return outPieceList;
    }
    private List<SudokuPiece> getWeakChainPieces(List<SudokuPiece> pieceList,SudokuPiece piece,int miniNumber){
        //找piecesList中和piece存在miniNumber弱链关系的pieces

        if(pieceList == null || pieceList.size() == 0){
            return null;
        }
        //同行
        List<SudokuPiece> tempPieceList, outPieceList = new ArrayList<>();
        tempPieceList = getPiecesByLine(pieceList,piece.y,miniNumber);
        if(tempPieceList != null && tempPieceList.size() != 0){
            for(int i=0; i<tempPieceList.size(); i++){
                if(tempPieceList.get(i).x == piece.x && tempPieceList.get(i).y == piece.y){
                    continue;
                }
                outPieceList.add(tempPieceList.get(i));
            }
        }
        //同列
        tempPieceList = getPiecesByColumn(pieceList,piece.x,miniNumber);
        Log.v("debug","同列:"+pieceListToString(tempPieceList));
        if(tempPieceList != null && tempPieceList.size() != 0){
            for(int i=0; i<tempPieceList.size(); i++){
                if(tempPieceList.get(i).x == piece.x && tempPieceList.get(i).y == piece.y){
                    continue;
                }
                outPieceList.add(tempPieceList.get(i));
            }
        }
        //同对角线
        if(piece.x == piece.y && (sudokuType == SudokuType.PERCENT || sudokuType == SudokuType.X_STYLE)) {
            tempPieceList = getPiecesByPercentDiagnoal(pieceList, miniNumber);
            if(tempPieceList != null || tempPieceList.size() != 0) {
                for (int i = 0; i < tempPieceList.size(); i++) {
                    if(tempPieceList.get(i).x == piece.x && tempPieceList.get(i).y == piece.y){
                        continue;
                    }
                    outPieceList.add(tempPieceList.get(i));
                }
            }
        }
        if(sudokuType == SudokuType.X_STYLE && piece.x == 8-piece.y){
            tempPieceList = getPiecesByXDiagnoal(pieceList, miniNumber);
            if(tempPieceList != null || tempPieceList.size() != 0) {
                for (int i = 0; i < tempPieceList.size(); i++) {
                    if(tempPieceList.get(i).x == piece.x && tempPieceList.get(i).y == piece.y){
                        continue;
                    }
                    outPieceList.add(tempPieceList.get(i));
                }
            }
        }
        //同宫格
        for(int i=0; i<pieceList.size(); i++){
            if(pieceList.get(i).getNumber() == 0 && pieceList.get(i).haveMiniNumber(miniNumber) && isInTheSameSquare(pieceList.get(i),piece)){
                outPieceList.add(pieceList.get(i));
            }
        }
        //删除piece自己
        Log.v("debug","删除自己之前:"+pieceListToString(outPieceList));
        for(int i=0; i<outPieceList.size(); i++){
            if(outPieceList.get(i).x == piece.x && outPieceList.get(i).y == piece.y){
                outPieceList.remove(i);
                i--;
            }
        }
        Log.v("debug","删除自己之后:"+pieceListToString(outPieceList));
        if(outPieceList.size() == 0){
            return null;
        }
        return outPieceList;
    }
    private List<SudokuPiece> getWeakChainPieces(SudokuPiece piece,int miniNumber){
        //找和piece存在弱链关系的pieces
        List<SudokuPiece> pieceList = new ArrayList<>();
        //同行
        for(int x=0; x<getMaxNumber(sudokuType); x++){
            if(pieces[x][piece.y].getNumber() > 0 || !pieces[x][piece.y].haveMiniNumber(miniNumber)){
                continue;
            }
            if(x != piece.x){
                pieceList.add(pieces[x][piece.y]);
            }
        }
        //同列
        for(int y=0; y<getMaxNumber(sudokuType); y++) {
            if(pieces[piece.x][y].getNumber() > 0 || !pieces[piece.x][y].haveMiniNumber(miniNumber)){
                continue;
            }
            if (y != piece.y) {
                pieceList.add(pieces[piece.x][y]);
            }
        }
        //同对角线
        if(sudokuType == SudokuType.PERCENT || sudokuType == SudokuType.X_STYLE){
            //百分比对角线
            if(piece.x == piece.y){
                for(int i=0; i<getMaxNumber(sudokuType); i++){
                    if(pieces[i][i].getNumber() > 0 || !pieces[i][i].haveMiniNumber(miniNumber)){
                        continue;
                    }
                    if(i != piece.x){
                        pieceList.add(pieces[i][i]);
                    }
                }
            }
        }
        if(sudokuType == SudokuType.X_STYLE){
            //X对角线
            if(piece.x == 8 - piece.y){
                for(int i=0; i<getMaxNumber(sudokuType); i++){
                    if(pieces[i][8-i].getNumber() > 0 || !pieces[i][8-i].haveMiniNumber(miniNumber)){
                        continue;
                    }
                    if(i != piece.x){
                        pieceList.add(pieces[i][8-i]);
                    }
                }
            }
        }
        //同宫格
        List<SudokuPiece> tempPieceList = getSquare(piece.x,piece.y);
        for(int i=0; i<tempPieceList.size(); i++){
            if(tempPieceList.get(i).getNumber() > 0 || !tempPieceList.get(i).haveMiniNumber(miniNumber)){
                continue;
            }
            if(tempPieceList.get(i).x != piece.x && tempPieceList.get(i).y != piece.y){
                pieceList.add(tempPieceList.get(i));
            }
        }
        //同百分比宫格
        tempPieceList = getPercentSquare(piece.x,piece.y);
        if(tempPieceList != null && tempPieceList.size() != 0){
            for(int i=0; i<tempPieceList.size(); i++){
                if(tempPieceList.get(i).getNumber() > 0 || !tempPieceList.get(i).haveMiniNumber(miniNumber)){
                    continue;
                }
                if(tempPieceList.get(i).x != piece.x && tempPieceList.get(i).y != piece.y){
                    pieceList.add(tempPieceList.get(i));
                }
            }
        }
        //同super宫格
        tempPieceList = getSuperSquare(piece.x,piece.y);
        if(tempPieceList != null && tempPieceList.size() != 0){
            for(int i=0; i<tempPieceList.size(); i++){
                if(tempPieceList.get(i).getNumber() > 0 || !tempPieceList.get(i).haveMiniNumber(miniNumber)){
                    continue;
                }
                if(tempPieceList.get(i).x != piece.x && tempPieceList.get(i).y != piece.y){
                    pieceList.add(tempPieceList.get(i));
                }
            }
        }
        return pieceList;
    }
    private List<SudokuPiece> getStrongChainPieces(SudokuPiece piece,int miniNumber){
        //找和piece存在强链关系的pieces
        //debug
        Log.v("debug",String.format("piece strong:%d,%d",piece.x,piece.y));
        List<SudokuPiece> pieceList = new ArrayList<>();
        int count;
        SudokuPiece piece2 = null;
        //同行，且仅有一个
        count = 0;
        for(int x=0; x<getMaxNumber(sudokuType); x++){
            if(pieces[x][piece.y].getNumber() > 0 || !pieces[x][piece.y].haveMiniNumber(miniNumber)){
                continue;
            }
            if(x != piece.x){
                count++;
                piece2 = pieces[x][piece.y];
            }
        }
        if(count == 1){
            pieceList.add(piece2);
            Log.v("debug",String.format("   add 同行:%d,%d",piece2.x,piece2.y));
        }
        //同列,且仅有一个
        count = 0;
        for(int y=0; y<getMaxNumber(sudokuType); y++) {
            if(pieces[piece.x][y].getNumber() > 0 || !pieces[piece.x][y].haveMiniNumber(miniNumber)){
                continue;
            }
            if (y != piece.y) {
                count ++;
                piece2 = pieces[piece.x][y];
            }
        }
        if(count == 1){
            pieceList.add(piece2);
            Log.v("debug",String.format("   add 同列:%d,%d",piece2.x,piece2.y));
        }
        //同对角线
        if(sudokuType == SudokuType.PERCENT || sudokuType == SudokuType.X_STYLE){
            //百分比对角线
            if(piece.x == piece.y){
                count = 0;
                for(int i=0; i<getMaxNumber(sudokuType); i++){
                    if(pieces[i][i].getNumber() > 0 || !pieces[i][i].haveMiniNumber(miniNumber)){
                        continue;
                    }
                    if(i != piece.x){
                        piece2 = pieces[i][i];
                        count ++;
                    }
                }
                if(count == 1){
                    pieceList.add(piece2);
                    Log.v("debug",String.format("   add 同百分比对角线:%d,%d",piece2.x,piece2.y));
                }
            }
        }
        if(sudokuType == SudokuType.X_STYLE){
            //X对角线
            if(piece.x == 8 - piece.y){
                count = 0;
                for(int i=0; i<getMaxNumber(sudokuType); i++){
                    if(pieces[i][8-i].getNumber() > 0 || !pieces[i][8-i].haveMiniNumber(miniNumber)){
                        continue;
                    }
                    if(i != piece.x){
                        piece2 = pieces[i][8-i];
                        count ++;
                    }
                }
                if(count == 1){
                    pieceList.add(piece2);
                    Log.v("debug",String.format("   add 同X对角线:%d,%d",piece2.x,piece2.y));
                }
            }
        }
        //同宫格
        List<SudokuPiece> tempPieceList = getSquare(piece.x,piece.y);
        count = 0;
        for(int i=0; i<tempPieceList.size(); i++){
            if(tempPieceList.get(i).getNumber() > 0 || !tempPieceList.get(i).haveMiniNumber(miniNumber)){
                continue;
            }
            if(tempPieceList.get(i).x != piece.x || tempPieceList.get(i).y != piece.y){ //除了自己
                piece2 = tempPieceList.get(i);
                count ++;
            }
        }
        if(count == 1){
            pieceList.add(piece2);
            Log.v("debug",String.format("   add 同宫格:%d,%d",piece2.x,piece2.y));
        }
        //同百分比宫格
        tempPieceList = getPercentSquare(piece.x,piece.y);
        if(tempPieceList != null && tempPieceList.size() != 0){
            count = 0;
            for(int i=0; i<tempPieceList.size(); i++){
                if(tempPieceList.get(i).getNumber() > 0 || !tempPieceList.get(i).haveMiniNumber(miniNumber)){
                    continue;
                }
                if(tempPieceList.get(i).x != piece.x || tempPieceList.get(i).y != piece.y){
                    piece2 = tempPieceList.get(i);
                    count ++;
                }
            }
            if(count == 1){
                pieceList.add(piece2);
                Log.v("debug",String.format("   add 同百分比宫:%d,%d",piece2.x,piece2.y));
            }
        }
        //同super宫格
        tempPieceList = getSuperSquare(piece.x,piece.y);
        if(tempPieceList != null && tempPieceList.size() != 0){
            count = 0;
            for(int i=0; i<tempPieceList.size(); i++){
                if(tempPieceList.get(i).getNumber() > 0 || !tempPieceList.get(i).haveMiniNumber(miniNumber)){
                    continue;
                }
                if(tempPieceList.get(i).x != piece.x || tempPieceList.get(i).y != piece.y){
                    piece2 = tempPieceList.get(i);
                    count ++;
                }
            }
            if(count == 1){
                pieceList.add(piece2);
                Log.v("debug",String.format("   add 同super宫格:%d,%d",piece2.x,piece2.y));
            }
        }
        return pieceList;
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
            for(int i=01; i<=getMaxNumber(sudokuType); i++){
                piece.addMiniNumber(i);
            }
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
                if(!numberExistSuperSquare(pieces[1][1],number)) {
                    return false;
                }
                //右上
                if(!numberExistSuperSquare(pieces[7][7],number)) {
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
        if(sudokuType != SudokuType.PERCENT && sudokuType != SudokuType.SUPER){
            return null;
        }
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
        if (sudokuType != SudokuType.SUPER) {
            return null;
        }
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
    private List<SudokuPiece> getPiecesByLineAndColumn(Numbers numbersOfY,Numbers numbersOfX){
        //返回X列s，Y行s交叉点
        if(numbersOfX == null || numbersOfY == null || numbersOfX.count() == 0 || numbersOfY.count() == 0){
            return  null;
        }
        List<SudokuPiece> outPieceList = new ArrayList<>();
        for(int i=0; i<numbersOfX.count(); i++){
            int x = numbersOfX.getNumbers().get(i);
            for(int j=0; j<numbersOfY.count(); j++){
                int y = numbersOfY.getNumbers().get(j);
                outPieceList.add(pieces[x][y]);
            }
        }
        return outPieceList;
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
        if(outPieceList.size() == 0){
            return null;
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
    private Boolean isOtherPlaceOfLineHaveMiniExcludeList(List<SudokuPiece> pieceList,int y,int miniNumber){
        int maxX = getMaxX(sudokuType);
        for(int x=0; x<maxX; x++){
            if(isInPieceList(pieceList,pieces[x][y])){
                continue;
            }
            if(pieces[x][y].haveMiniNumber(miniNumber)){
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
    private Boolean isOtherPlaceOfColumnHaveMiniExcludeList(List<SudokuPiece> pieceList,int x,int miniNumber){
        int maxY = getMaxY(sudokuType);
        for(int y=0; y<maxY; y++){
            if(isInPieceList(pieceList,pieces[x][y])){
                continue;
            }
            if(pieces[x][y].haveMiniNumber(miniNumber)){
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
    private Boolean isOtherPlaceOfPercentDiagonalHaveMiniExcludeList(List<SudokuPiece> pieceList,int miniNumber){
        int maxNumber = getMaxNumber(sudokuType);
        for(int x=0; x<maxNumber; x++){
            if(isInPieceList(pieceList,pieces[x][x])){
                continue;
            }
            if(pieces[x][x].haveMiniNumber(miniNumber)){
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
    private Boolean isOtherPlaceOfXDiagonalHaveMiniExcludeList(List<SudokuPiece> pieceList,int miniNumber){
        int maxNumber = getMaxNumber(sudokuType);
        for(int x=0; x<maxNumber; x++){
            if(isInPieceList(pieceList,pieces[x][8-x])){
                continue;
            }
            if(pieces[x][8-x].haveMiniNumber(miniNumber)){
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
    //piece1和piece2是否在同一个单元(行/列/对角线/宫格)
    private Boolean isIntheSameUnit(SudokuPiece piece1,SudokuPiece piece2){
        //同一行/列
        if(piece1.x == piece2.x || piece1.y == piece2.y){
            return true;
        }
        //同一对角线
        if(sudokuType == SudokuType.PERCENT || sudokuType == SudokuType.X_STYLE){
            if(piece1.x == piece1.y && piece2.x == piece2.y){
                return true;
            }
        }
        if(sudokuType == SudokuType.X_STYLE){
            if(piece1.x == 8 - piece1.y && piece2.x == 8 - piece2.y){
                return  true;
            }
        }
        //同一宫格
        return isInTheSameSquare(piece1,piece2);
    }
    //piece1和piece2是否在同一个宫格中，包括百分比，super宫格
    private Boolean isInTheSameSquare(SudokuPiece piece1,SudokuPiece piece2){

        List<SudokuPiece> pieceList = getSquare(piece1.x,piece1.y);
        //Log.v("debug",pieceListToString(pieceList));
        if(isInPieceList(pieceList,piece2)) {
            return true;
        }
        if(sudokuType == SudokuType.PERCENT){
            pieceList = getPercentSquare(piece1.x,piece1.y);
            if(pieceList != null && pieceList.size() != 0){
                if(isInPieceList(pieceList,piece2)){
                    return true;
                }
            }
        }
        if(sudokuType == SudokuType.SUPER){
            pieceList = getSuperSquare(piece1.x,piece1.y);
            if(pieceList != null && pieceList.size() != 0){
                if(isInPieceList(pieceList,piece2)){
                    return true;
                }
            }
        }
        //Log.v("debug",String.format("piece1(%d,%d) and piece2(%d,%d) are not in a same square",piece1.x,piece1.y,piece2.x,piece2.y));
        return false;
    }
    private Boolean isWXYAfterRemoveZ(List<SudokuPiece> pieceList,List<Integer> wxyList,int Z){
        //pieceList中的三个piece,去除Z备选数字后,是否构成wxy(miniNumberList中的数字)大数字
        if(pieceList == null || pieceList.size() != 3 || wxyList == null || wxyList.size() != 3){
            return false;
        }
        //先复制piece,避免修改piece造成对board的实际修改
        List<SudokuPiece> tempList = new ArrayList<>();
        for(int i=0; i<pieceList.size(); i++){
            //校验一下是否包含大数字
            if(pieceList.get(i).getNumber() > 0 || !pieceList.get(i).haveMiniNumber(Z)){
                Log.e("debug","pieceList中包含大数字");
                return false;
            }
            tempList.add(pieceList.get(i).copyPiece());
        }
        List<SudokuPiece> bigNumberPieceList = new ArrayList<>();
        List<SudokuPiece> miniNumberPieceList = new ArrayList<>();
        //先删除z,如果删除z以后,仅有一个候选,就变成大数字,加入bigNumberPieceList,否则就加入miniNumberPieceList
        for(int i=0; i<tempList.size(); i++){
            SudokuPiece piece = tempList.get(i);
            piece.delMiniNumber(Z);
            if(piece.getMiniNumbers().length == 1){
                piece.setNumber(piece.getMiniNumbers()[0]);
                bigNumberPieceList.add(piece);
            }else{
                miniNumberPieceList.add(piece);
            }
        }
        if(bigNumberPieceList.size() == 0){
            return false;
        }
        //尝试以bigNumberPieceList的大数字去移除miniNumberPieceList
        for(int i=0; i<miniNumberPieceList.size(); i++){
            SudokuPiece piece = miniNumberPieceList.get(i);
            for(int j=0; j<bigNumberPieceList.size(); j++){
                int bigNumber = bigNumberPieceList.get(j).getNumber();
                //尝试以bignumber去删除侯选数字
                if(piece.haveMiniNumber(bigNumber) && isIntheSameUnit(piece,bigNumberPieceList.get(j))){
                    piece.delMiniNumber(bigNumber);
                    if(piece.getMiniNumbers().length == 1){
                        piece.setNumber(piece.getMiniNumbers()[0]);
                        bigNumberPieceList.add(piece);
                        miniNumberPieceList.remove(piece);
                    }
                }
            }
        }
        //再试一次,最多两次
        for(int i=0; i<miniNumberPieceList.size(); i++){
            SudokuPiece piece = miniNumberPieceList.get(i);
            for(int j=0; j<bigNumberPieceList.size(); j++){
                int bigNumber = bigNumberPieceList.get(j).getNumber();
                //尝试以bignumber去删除侯选数字
                if(piece.haveMiniNumber(bigNumber) && isIntheSameUnit(piece,bigNumberPieceList.get(j))){
                    piece.delMiniNumber(bigNumber);
                    if(piece.getMiniNumbers().length == 1){
                        piece.setNumber(piece.getMiniNumbers()[0]);
                        bigNumberPieceList.add(piece);
                        miniNumberPieceList.remove(piece);
                    }
                }
            }
        }
        if(bigNumberPieceList.size() == 1){
            // xy / xy 的组合

            //校验一下
            if(miniNumberPieceList.size() != 2){
                Log.e("debug","bigNumberPieceList.size = 1 but miniNumberPieceList.size != 2");
                return false;
            }
            SudokuPiece pieceX = miniNumberPieceList.get(0);
            SudokuPiece pieceY = miniNumberPieceList.get(0);
            //pieceX和pieceY是否构成xy xy的组合
            if(pieceX.getMiniNumbers().length != 2 || pieceY.getMiniNumbers().length != 2){
                return false;
            }
            if(!pieceX.haveMiniNumber(pieceY.getMiniNumbers()[0]) || !pieceX.haveMiniNumber(pieceY.getMiniNumbers()[1])){
                return false;
            }
            //构成了xy/xy的组合,那么就随意设置pieceX和pieceY为两个不同的数字即可
            pieceX.setNumber(pieceY.getMiniNumbers()[0]);
            pieceY.setNumber(pieceY.getMiniNumbers()[1]);
            bigNumberPieceList.add(pieceX);
            bigNumberPieceList.add(pieceY);
        }
        if(bigNumberPieceList.size() == 3){
            //检查这3个数字,是否和wxyList一致
            Boolean[] findNumber = new Boolean[3]; //对应wxy的三个数字
            for(int i=0; i<bigNumberPieceList.size(); i++){
                int bigNumber = bigNumberPieceList.get(i).getNumber();
                for(int j=0; j<wxyList.size(); i++){
                    if(wxyList.get(i) ==  bigNumber){
                        findNumber[i] = true;
                    }
                }
            }
            //校验findNumber是否都为true
            for(int i=0; i<3; i++){
                if(!findNumber[i]){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static String pieceListToString(List<SudokuPiece> pieceList){
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

    //debug
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
    public void printChainNet(){
        if(chainNet == null){
            return;
        }
        if(startFlag == CHAIN_STRONG_ALTERNATE) {
            Log.v("debug", String.format("chainNet:Strong %d", hintMiniNumber));
        }else{
            Log.v("debug", String.format("chainNet:weak %d", hintMiniNumber));
        }
        for(int x=0; x<getMaxX(sudokuType); x++){
            for(int y=0; y<getMaxY(sudokuType); y++){
                if(chainNet[x][y] == null){
                    continue;
                }
                Log.v("debug",String.format("piece:%d,%d",x,y));
                if(chainNet[x][y].weakList != null){
                    Log.v("debug",String.format("weak:%s",pieceListToString(chainNet[x][y].weakList)));
                }
                if(chainNet[x][y].strongList != null){
                    Log.v("debug",String.format("strong:%s",pieceListToString(chainNet[x][y].strongList)));
                }
            }
        }
    }
    public static List<SudokuPiece> copyPieceList(List<SudokuPiece> pieceList){
        //拷贝pieceList
        if(pieceList == null || pieceList.size() == 0){
            return null;
        }
        List<SudokuPiece> outPieceList = new ArrayList<>();
        for(int i=0; i<pieceList.size(); i++){
            outPieceList.add(pieceList.get(i));
        }
        return outPieceList;
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
    public void addNumber(int number){
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
    public static List<Numbers> findAllCombinationFrom0(int number,int N){
        //从0-number，随机选择N个数字
        List<Integer> intList = new ArrayList<>();
        for(int i=0; i<=number; i++){
            intList.add(i);
        }
        return findAllCombination(intList,N);
    }
    public static List<Numbers> findAllCombinationFrom1(int number,int N){
        //从1-number，随机选择N个数字
        List<Integer> intList = new ArrayList<>();
        for(int i=1; i<=number; i++){
            intList.add(i);
        }
        return findAllCombination(intList,N);
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
class ChainNode{
    SudokuPiece piece;
    List<SudokuPiece> weakList;
    List<SudokuPiece> strongList;
    ChainNode(SudokuPiece piece,List<SudokuPiece> weakList,List<SudokuPiece> strongList){
        this.piece = piece;
        this.weakList = weakList;
        this.strongList = strongList;
    }
}

