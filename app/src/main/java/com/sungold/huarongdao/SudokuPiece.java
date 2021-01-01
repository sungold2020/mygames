package com.sungold.huarongdao;

import android.util.Log;


public class SudokuPiece extends Piece{
    private Boolean modifiable=true;
    private int number = 0;   // 0表示还未设定值
    private int[] miniNumbers = null; //备选数字数组

    @Override
    public void printPiece(){
        if(type == Piece.PIECE_SUDOKU_BOARD){
            //Log.v("sudoku",String.format("(%d,%d):%d",x,y,number));
            Log.v("sudoku",toDBString());
            /*if(number == 0 && miniNumbers != null){
                for(int i=0; i<miniNumbers.length; i++)
            }*/
        }else
        {
            Log.v("sudoku",String.format("%d:%d",type,number));
        }
    }
    SudokuPiece(int number,int type,int x,int y){
        super("",type,1,x,y);
        this.number = number;
    }
    SudokuPiece(int number,int type,int x,int y,Boolean modifiable){
        super("",type,1,x,y);
        this.number = number;
        this.modifiable = modifiable;
    }
    SudokuPiece(int[] miniNumbers,int type,int x,int y){
        super("",Piece.PIECE_SUDOKU_BOARD,1,x,y);
        this.miniNumbers = miniNumbers;
    }
    public Boolean isModifiable(){
        return modifiable;
    }
    public void setModifiable(Boolean modifiable){
        this.modifiable = modifiable;
    }
    public int getNumber(){
        return number;
    }
    public int[] getMiniNumbers(){
        return miniNumbers;
    }
    public void setNumber(int number){
        this.number = number;
    }
    public void setMiniNumbers(int[] miniNumbers){
        this.miniNumbers = miniNumbers;
    }
    public SudokuPiece copyPiece() {
        //复制新的棋子
        if (number > 0){
            return new SudokuPiece(number,type,x,y,modifiable);
        }
        if(miniNumbers == null){
            return new SudokuPiece(null,type,x,y);
        }else {
            int[] newMiniNumbers = new int[miniNumbers.length];
            for (int i = 0; i < miniNumbers.length; i++) {
                newMiniNumbers[i] = miniNumbers[i];
            }
            return new SudokuPiece(newMiniNumbers,type,x,y);
        }
    }
    public Boolean haveMiniNumber(int number){
        if (miniNumbers == null){
            return false;
        }
        for(int i=0; i<miniNumbers.length; i++){
            if (miniNumbers[i] == number){
                return true;
            }
        }
        return false;
    }
    public Boolean addMiniNumber(int number){
        if(miniNumbers == null){
            miniNumbers = new int[1];
            miniNumbers[0] = number;
            return true;
        }
        for(int i=0; i<miniNumbers.length; i++){
            if (miniNumbers[i] == number){
                return false; //已经存在
            }
        }
        int[] newMiniNumbers = new int[miniNumbers.length+1];
        for(int i=0; i<miniNumbers.length; i++){
            newMiniNumbers[i] = miniNumbers[i];
        }
        newMiniNumbers[miniNumbers.length] = number;
        miniNumbers = newMiniNumbers;
        return true;
    }
    public Boolean delMiniNumber(int number){
        if(miniNumbers == null){
            return false;
        }
        if (miniNumbers.length == 1){
            if (miniNumbers[0] == number){
                miniNumbers = null;
                return true;
            }else{
                return false;
            }
        }
        int[] newMiniNumbers = new int[miniNumbers.length-1];
        int newIndex = 0;
        for(int i=0; i<miniNumbers.length; i++){
            if(miniNumbers[i] == number){
                continue;
            }
            if (newIndex >= miniNumbers.length-1){
                return false;  //newIndex已经超过新数组界了。
            }
            newMiniNumbers[newIndex] = miniNumbers[i];
            newIndex += 1;
        }
        miniNumbers = newMiniNumbers;
        return true;
    }
    private String miniNumbersToString(){
        if (miniNumbers == null){
            return "none";
        }
        StringBuffer stringBuffer = new StringBuffer(miniNumbers.length);
        for(int i=0; i<miniNumbers.length; i++){
            stringBuffer.append(String.valueOf(miniNumbers[i]));
        }
        return  stringBuffer.toString();
    }
    private static int[] miniNumbersFromString(String string){
        if (string.equals("none")){
            return null;
        }
        if(string.length() == 0){
            Log.e("SudokuPiece","miniNumbersString length is 0");
            return null;
        }
        int[] miniNumbers = new int[string.length()];
        for(int i=0; i<string.length(); i++){
            miniNumbers[i] = Integer.valueOf(String.valueOf(string.charAt(i)));
        }
        return miniNumbers;
    }
    @Override
    public String toDBString(){
        //转换成一个字符串，用于存储再数据库
        if(isModifiable()){
            return String.format("%d,%d,%d,%d,%s",x,y,number,1,miniNumbersToString());
        }else{
            return String.format("%d,%d,%d,%d,%s",x,y,number,0,miniNumbersToString());
        }
    }

    public static SudokuPiece fromDBString(String string){
        if (string.equals("")) { return null; }
        String[] strings = string.split(",");
        if (strings.length != 5) { return null; }
        int x = Integer.valueOf(strings[0]);
        int y = Integer.valueOf(strings[1]);
        int number = Integer.valueOf(strings[2]);
        int intIsModifiable = Integer.valueOf(strings[3]);
        if (number > 0){
            if(intIsModifiable == 0){
                return new SudokuPiece(number,SudokuPiece.PIECE_SUDOKU_BOARD,x,y,false);
            }else{
                return new SudokuPiece(number,SudokuPiece.PIECE_SUDOKU_BOARD,x,y);
            }
        }
        int[] miniNumbers = SudokuPiece.miniNumbersFromString(strings[4]);


        return new SudokuPiece(miniNumbers,SudokuPiece.PIECE_SUDOKU_BOARD,x,y);
    }
}
