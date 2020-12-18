package com.sungold.huarongdao;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//棋盘，所有棋子的位置,构成了棋盘
public class SudokuBoard extends Board{
    public SudokuType sudokuType;

    public SudokuPiece[][] pieces = null;

    //生成一个棋盘，但棋盘中的棋子未设置
    SudokuBoard(SudokuType sudokuType){
        super("",GameType.SUDOKU,getMaxX(sudokuType),getMaxY(sudokuType));
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
    SudokuBoard(SudokuType sudokuType, SudokuPiece[][] pieces) {
        super("",GameType.SUDOKU,getMaxX(sudokuType),getMaxY(sudokuType));
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

    public SudokuPiece getPiece(int x, int y) {
        if (isOutOfBoard(x, y)) {
            return null;
        }
        return pieces[x][y];
    }

    public SudokuBoard copyBoard() {
        SudokuPiece[][] newPieces;
        int mxX = getMaxX(sudokuType);
        int maxY = getMaxY(sudokuType);
        newPieces = new SudokuPiece[maxX][maxY];
        for(int x=0; x<mxX; x++){
            for(int y=0; y<maxY; y++){
                newPieces[x][y] = pieces[x][y].copyPiece();
            }
        }
        return new SudokuBoard(sudokuType,newPieces);
    }

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
    public Boolean isFull() {
        int maxNumber = getMaxNumber(sudokuType);
        for (int x = 0; x < maxNumber; x++) {
            for (int y = 0; y < maxNumber; y++) {
                if (pieces[x][y].getNumber() <= 0 || pieces[x][y].getNumber() > maxNumber) {
                    return false;
                }
            }
        }
        return true;
    }
    public Boolean isPiecesUnique(){
        //检查每一行，每一列，每一宫格数字是否重复

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
        int squareOfX, squareOfY;  //宫格X的个数，宫格Y的个数
        int numberOfX, numberOfY; //每一宫格x的个数,y的个数
        switch(maxNumber){
            case 4:
                squareOfX = squareOfY = 2;
                numberOfX = numberOfY = 2;
                break;
            case 6:
                squareOfX = 2; squareOfY = 3;
                numberOfX = 3; numberOfY = 2;
                break;
            case 9:
                squareOfX = squareOfY = numberOfX = numberOfY = 3;
                break;
            default:
                return false;
        }
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
    public void addMiniNumber(int x,int y, int number){
        //(x,y)单元格增加mininumber
        //如果单元格已经包含该miniNumber，就删除它，否则就增加它
        if(pieces[x][y].haveMiniNumber(number)) {
            Log.v("sdoku","del mininumber"+String.valueOf(number));
            pieces[x][y].delMiniNumber(number);
        }else{
            pieces[x][y].addMiniNumber(number);
        }
    }

    public String checkSolution(){
        //检查是否有解及唯一解
        SudokuBoard startBoard = copyBoard();
        SudokuFindSolution findSolution = new SudokuFindSolution(startBoard);
        findSolution.findSolution();
        if(findSolution.solutionCount() == 1){
            return "ok";
        }else if(findSolution.solutionCount() == 0){
            Log.v("sudoku","there is no solution");
            return "there is no solution";
        }else{
            Log.v("sudoku","there is 1+ solution");
            findSolution.printSolution();
            return "there is 1+ solution";
        }
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

    public void fillMiniNumbers(){
        //填入所有备选数字
        int maxX = getMaxX(sudokuType);
        int maxY = getMaxY(sudokuType);
        for(int x=0; x<maxX; x++){
            for(int y=0; y<maxY; y++){
                fillMiniNumbers(pieces[x][y]);
            }
        }
    }
    public void fillMiniNumbers(SudokuPiece piece){
        //piece填入所有可能的miniNumber
        /*if(piece.getNumber() > 0){

        }*/
        for(int i=1; i<=getMaxNumber(sudokuType); i++){
            piece.addMiniNumber(i);
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
        pieceList = getPercent(piece.x,piece.y);
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
            if(numberExistPercent(piece,number) || numberExistPercentDiagonal(piece,number)){
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
    public Boolean numberExistPercent(SudokuPiece piece,int number){
        //number在对角线中是否存在
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
                if(!numberExistPercent(pieces[1][maxY-2],number)) {
                    return false;
                }
                return true;
            default:
                return false;
        }
    }
    public List<SudokuPiece> getSquare(int x, int y){
        //返回(x,y)所在的宫格piece
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
    public List<SudokuPiece> getPercent(int pieceX, int pieceY){
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
    public String toDBString() {
        //第一个数字为type，后面数字为每一个单元格的数字
        StringBuffer stringBuffer = new StringBuffer(100);
        stringBuffer.append(sudokuType.toInt());
        for(int x=0; x<getMaxX(sudokuType); x++){
            for(int y=0; y<getMaxY(sudokuType); y++){
                stringBuffer.append(pieces[x][y].getNumber());
            }
        }
        return stringBuffer.toString();
    }
    public static SudokuBoard fromDBString(String string){
        int type = Integer.valueOf(String.valueOf(string.charAt(0)));
        SudokuType sudokuType = SudokuType.toEnum(type);
        if(sudokuType == null){
            Log.e("sudoku",String.format("unknown sudokutype:%d",type));
            return null;
        }
        int maxX = SudokuBoard.getMaxX(sudokuType);
        int maxY = SudokuBoard.getMaxY(sudokuType);
        if(string.length() != maxX*maxY+1){
            Log.e("sudoku",String.format("length = %d",string.length()));
            return null;
        }
        SudokuPiece[][] pieces = new SudokuPiece[maxX][maxY];
        for(int i=1; i<string.length(); i++){
            int x = (i-1) / maxY;
            int y = (i-1) % maxY;
            int number = Integer.valueOf(String.valueOf(string.charAt(i)));
            //Log.v("sudoku",String.format("%d,%d:%d",x,y,number));
            if(number == 0) {
                pieces[x][y] = new SudokuPiece(0,Piece.PIECE_SUDOKU_BOARD,x,y);
            }else{
                pieces[x][y] = new SudokuPiece(number,Piece.PIECE_SUDOKU_BOARD,x,y,false);
            }
        }
        return new SudokuBoard(sudokuType,pieces);
    }
    public String toSavedString(){
        StringBuffer stringBuffer = new StringBuffer(1000);
        stringBuffer.append(String.format("%d|",sudokuType.toInt()));
        if (pieces != null) {
            for (int x=0; x<maxX; x++) {
                for(int y=0; y<maxY; y++) {
                    stringBuffer.append(pieces[x][y].toDBString()+"|");
                }
            }
        }
        return stringBuffer.toString();
    }
    public static SudokuBoard fromSavedString(String string) {
        if (string.equals("")) { return null; }
        String[] strings = string.split("\\|");
        if (strings.length <= 1) { return null; }
        SudokuType sudokuType = SudokuType.toEnum(Integer.valueOf(strings[0]));
        if (sudokuType == null){
            return  null;
        }
        int maxX = getMaxX(sudokuType);
        int maxY = getMaxY(sudokuType);
        /*if(strings.length != maxX*maxY){

        }*/
        SudokuPiece[][] pieces = new SudokuPiece[maxX][maxY];
        for(int i=1; i<strings.length; i++){
            SudokuPiece piece = SudokuPiece.fromDBString(strings[i]);
            if(piece == null){
                Log.v("sudoku",String.format("piece is null from %d: %s ",i,strings[i]));
                break;
            }
            pieces[piece.x][piece.y] = piece;
        }
        return new SudokuBoard(sudokuType, pieces);
    }
    public DBBoard toDBBoard(){
        return new DBBoard("",GameType.SUDOKU,toDBString(),0,"");
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
                return 9;
            default:
                return 0;
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
                return 9;
            default:
                return 0;
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
                return 3;
            default:
                return 0;
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
                return 3;
            default:
                return 0;
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
                return 3;
            default:
                return 0;
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
                return 3;
            default:
                return 0;
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
                return 3;
            default:
                return 0;
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
                return 3;
            default:
                return 0;
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
    FOUR(0),SIX(1),NINE(2),X_STYLE(3),PERCENT(4);
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
            default:
                return null;
        }
    }
    public  int  toInt(){
        return this.value;
    }
}