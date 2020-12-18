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
import androidx.core.content.res.ResourcesCompat;

import org.json.JSONObject;


public class Piece {
    //四种棋子+空
    public final static int PIECE_CAOCHAO = 0;  //占4格
    public final static int PIECE_SOLDIER = 1;  //占1格
    public final static int PIECE_HORIZON = 2;  //水平的长方形，占2格
    public final static int PIECE_VERTICAL = 3; //垂直的长方形，占2格
    public final static int PIECE_EMPTY = 4;    //空
    public final static int PIECE_BLOCK = 5;    //推箱子游戏中的砖块
    public final static int PIECE_BOX   = 6;    //推箱子游戏中的箱子
    public final static int PIECE_BOY   = 7;    //推箱子游戏中的男孩
    public final static int PIECE_DEST = 8;     //推箱子游戏中的目标
    public final static int PIECE_DEST_BOY = 9; //推箱子游戏中目标和BOY重叠
    public final static int PIECE_DEST_BOX = 10; //推箱子游戏中目标和BLOCK重叠
    public final static int PIECE_SUDOKU_BOARD = 100;  //数独棋盘上的数字
    public final static int PIECE_SUDOKU_NUMBER = 101; //下方左边大数字棋盘的数字
    public final static int PIECE_SUDOKU_MINI_NUMBER = 102; //下方右边小数字棋盘的数字

    public final static int DIRECTION_UP = 0;
    public final static int DIRECTION_DOWN = 1;
    public final static int DIRECTION_LEFT = 2;
    public final static int DIRECTION_RIGHT = 3;

    public String name;
    public int type;
    public int length;
    public int x,y; //以左下角的坐标来记录棋子的位置

    Piece(String name, int type, int length, int x, int y){
        this.name = name;
        this.type = type;
        this.length = length;
        this.x = x;
        this.y = y;
    }
    public int getWidth(){
        switch (type){
            case PIECE_CAOCHAO:
                return 2;
            case PIECE_HORIZON:
                return length;
            default:
                return 1;
        }
    }
    public int getHeight(){
        switch (type){
            case PIECE_CAOCHAO:
                return 2;
            case PIECE_VERTICAL:
                return length;
            default:
                return 1;
        }
    }
    public Piece copyPiece() {
        //复制新的棋子
        Piece newPiece = new Piece(name,type,length,x,y);
        return newPiece;
    }
    public Boolean isOccupied(int x,int y){
        switch(type){
            case PIECE_CAOCHAO:
                if ((x == this.x || x == this.x+1) && (y == this.y || y == this.y+1)){
                    return true;
                }
                return false;
            case PIECE_SOLDIER:
                //case PIECE_EMPTY:
            case PIECE_BLOCK:
            case PIECE_BOX:
            case PIECE_BOY:
                if (x == this.x && y == this.y){
                    return true;
                }
                return false;
            case PIECE_HORIZON:
                if ( y != this.y) { return false; }

                for (int i = 0; i < length; i++) {
                    if (x == this.x + i) {
                        return true;
                    }
                }
                return false;
            case PIECE_VERTICAL:
                if (x != this.x) { return false; }
                for(int i=0; i<length; i++) {
                    if (y == this.y + i) {
                        return true;
                    }
                }
                return false;
            default:
                System.out.println("error:unknown type"+String.valueOf(type));
                return false;
        }
    }
    public static int directionFromString(String direction){
        switch(direction.toLowerCase()){
            case "up":
                return DIRECTION_UP;
            case "down":
                return DIRECTION_DOWN;
            case "left":
                return DIRECTION_LEFT;
            case "right":
                return DIRECTION_RIGHT;
            default:
                return  -1;
        }
    }
    public void printPiece(){
        System.out.println(String.format("%s:%d,%d",name,x,y));
    }
    public static String directionToString(int direction){
        switch(direction){
            case Piece.DIRECTION_UP:
                return "UP";
            case Piece.DIRECTION_DOWN:
                return "DOWN";
            case Piece.DIRECTION_LEFT:
                return "LEFT";
            case Piece.DIRECTION_RIGHT:
                return "RIGHT";
            default:
                System.out.println("unknow direction");
                return "UNKNOWN";
        }
    }
    public static String getTypeName(int type){
        switch(type){
            case Piece.PIECE_CAOCHAO:
                return "曹";
            case Piece.PIECE_VERTICAL:
                return "竖";
            case Piece.PIECE_HORIZON:
                return "横";
            case Piece.PIECE_SOLDIER:
                return "兵";
            case Piece.PIECE_EMPTY:
                return "空";
            case Piece.PIECE_BLOCK:
                return "砖";
            case Piece.PIECE_BOX:
                return "箱";
            case Piece.PIECE_BOY:
                return "男";
            default:
                return "X";
        }
    }
    public Bitmap getBitmap(View context){
        //在box游戏中，如果目标和boy/box重叠
        if(type == PIECE_DEST_BOY){
            return ((BitmapDrawable) context.getResources().getDrawable(R.drawable.dest_boy)).getBitmap();
        }else if(type == PIECE_DEST_BOX){
            return ((BitmapDrawable) context.getResources().getDrawable(R.drawable.dest_box)).getBitmap();
        }

        if (name.equals("将一")){
            return ((BitmapDrawable) context.getResources().getDrawable(R.drawable.jiang1)).getBitmap();
        }else if(name.equals("将二")){
            return ((BitmapDrawable) context.getResources().getDrawable(R.drawable.jiang2)).getBitmap();
        }else if(name.equals("将三")){
            return ((BitmapDrawable) context.getResources().getDrawable(R.drawable.jiang3)).getBitmap();
        }else if (name.equals("将四")){
            return ((BitmapDrawable) context.getResources().getDrawable(R.drawable.jiang4)).getBitmap();
        }else if (name.equals("将五")){
            return ((BitmapDrawable) context.getResources().getDrawable(R.drawable.jiang5)).getBitmap();
        }else if (name.startsWith("将")) {
            return ((BitmapDrawable) context.getResources().getDrawable(R.drawable.jiang5)).getBitmap();
        }else if (name.equals("帅一")){
            return ((BitmapDrawable) context.getResources().getDrawable(R.drawable.shuai1)).getBitmap();
        }else if (name.equals("帅二")){
            return ((BitmapDrawable) context.getResources().getDrawable(R.drawable.shuai2)).getBitmap();
        }else if (name.equals("帅三")){
            return ((BitmapDrawable) context.getResources().getDrawable(R.drawable.shuai3)).getBitmap();
        }else if (name.equals("帅四")){
            return ((BitmapDrawable) context.getResources().getDrawable(R.drawable.shuai4)).getBitmap();
        }else if (name.equals("帅五")){
            return ((BitmapDrawable) context.getResources().getDrawable(R.drawable.shuai5)).getBitmap();
        }else if (name.startsWith("帅")) {
            return ((BitmapDrawable) context.getResources().getDrawable(R.drawable.jiang5)).getBitmap();
        }else if (name.equals("曹操")){
            return ((BitmapDrawable) context.getResources().getDrawable(R.drawable.caochao)).getBitmap();
        }else if (name.startsWith("竖")){
            return ((BitmapDrawable) context.getResources().getDrawable(R.drawable.block_block)).getBitmap();
        }else if (name.startsWith("横")) {
            return ((BitmapDrawable) context.getResources().getDrawable(R.drawable.block_block)).getBitmap();
        }else if (name.equals("王")) {
            return ((BitmapDrawable) context.getResources().getDrawable(R.drawable.block_king)).getBitmap();
        }else if (name.startsWith("目标")){
            return ((BitmapDrawable) context.getResources().getDrawable(R.drawable.dest)).getBitmap();
        }else if (name.startsWith("兵")){
            return ((BitmapDrawable) context.getResources().getDrawable(R.drawable.bing)).getBitmap();
        }else if (name.startsWith("空")){
            return ((BitmapDrawable) context.getResources().getDrawable(R.drawable.space)).getBitmap();
        }else if (name.startsWith("箱")){
            return ((BitmapDrawable) context.getResources().getDrawable(R.drawable.box)).getBitmap();
        }else if (name.startsWith("砖")){
            return ((BitmapDrawable) context.getResources().getDrawable(R.drawable.block)).getBitmap();
        }else if (name.equals("boy")){
            return ((BitmapDrawable) context.getResources().getDrawable(R.drawable.boy)).getBitmap();
        }else{
            Log.v("piece",name+":can't find bitmap");
            return null;
        }
    }
    public JSONObject toJson()  {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", name);
            jsonObject.put("type", type);
            jsonObject.put("x", x);
            jsonObject.put("y", y);
            return jsonObject;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public static Piece parseFromJson(String string)  {
        try {
            JSONObject json = new JSONObject(string);
            String name = json.getString("name");
            int type = json.getInt("type");
            int length = json.getInt("length");
            int x = json.getInt("x");
            int y = json.getInt("y");
            return new Piece(name, type, length, x, y);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public String toDBString(){
        //转换成一个字符串，用于存储再数据库
        return String.format("%s,%d,%d,%d,%d",name,type,length,x,y);
    }
    public static Piece fromDBString(String string){
        if (string.equals("")) { return null; }
        String[] strings = string.split(",");
        if (strings.length != 5) { return null; }
        String name = strings[0];
        int type = Integer.valueOf(strings[1]);
        int length  = Integer.valueOf(strings[2]);
        int x  = Integer.valueOf(strings[3]);
        int y  = Integer.valueOf(strings[4]);
        return new Piece(name,type,length,x,y);
    }
    public static String numberToChinese(int number){
        //把数字转换为中文数字
        switch (number){
            case 0:
                return "零";
            case 1:
                return "一";
            case 2:
                return "二";
            case 3:
                return "三";
            case 4:
                return "四";
            case 5:
                return "五";
            case 6:
                return "六";
            case 7:
                return "七";
            case 8:
                return "八";
            case 9:
                return "九";
            case 10:
                return "十";
            default:
                return "";
        }
    }
}
