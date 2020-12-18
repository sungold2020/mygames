package com.sungold.huarongdao;

import org.json.JSONException;
import org.json.JSONObject;

public class HuaPiece {
    //四种棋子+空
    /*public final static int PIECE_CAOCHAO = 1111;  //占4格
    public final static int PIECE_SOLDIER = 111;  //占1格
    public final static int PIECE_HORIZON = 11;  //水平的长方形，占2格
    public final static int PIECE_VERTICAL = 1; //垂直的长方形，占2格
    public final static int PIECE_EMPTY = 5;    //空 */
    public final static int PIECE_CAOCHAO = 0;  //占4格
    public final static int PIECE_SOLDIER = 1;  //占1格
    public final static int PIECE_HORIZON = 2;  //水平的长方形，占2格
    public final static int PIECE_VERTICAL = 3; //垂直的长方形，占2格
    public final static int PIECE_EMPTY = 4;    //空

    public final static int DIRECTION_UP = 0;
    public final static int DIRECTION_DOWN = 1;
    public final static int DIRECTION_LEFT = 2;
    public final static int DIRECTION_RIGHT = 3;

    public String name;
    public int type;
    public int x,y; //以左下角的坐标来记录棋子的位置

    HuaPiece(String name, int type, int x, int y){
        this.name = name;
        this.type = type;
        this.x = x;
        this.y = y;
    }
    public HuaPiece copyPiece(){
        //复制新的棋子
        HuaPiece newPiece = new HuaPiece(name,type,x,y);
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
            case PIECE_EMPTY:
                if (x == this.x && y == this.y){
                    return true;
                }
                return false;
            case PIECE_HORIZON:
                if ((x == this.x || x == this.x+1) && y == this.y){
                    return true;
                }
                return false;
            case PIECE_VERTICAL:
                if (x == this.x && (y == this.y || y == this.y+1)){
                    return true;
                }
                return false;
            default:
                System.out.println("error:unknown type"+String.valueOf(type));
                return false;
        }
    }
    public int getDirection(String direction){
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
    public static String getDirectionName(int direction){
        switch(direction){
            case HuaPiece.DIRECTION_UP:
                return "UP";
            case HuaPiece.DIRECTION_DOWN:
                return "DOWN";
            case HuaPiece.DIRECTION_LEFT:
                return "LEFT";
            case HuaPiece.DIRECTION_RIGHT:
                return "RIGHT";
            default:
                System.out.println("unknow direction");
                return "UNKNOWN";
        }
    }
    public static String getTypeName(int type){
        switch(type){
            case HuaPiece.PIECE_CAOCHAO:
                return "曹";
            case HuaPiece.PIECE_VERTICAL:
                return "将";
            case HuaPiece.PIECE_HORIZON:
                return "帅";
            case HuaPiece.PIECE_SOLDIER:
                return "兵";
            case HuaPiece.PIECE_EMPTY:
                return "空";
            default:
                return "X";
        }
    }
    public String toJsonString() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name",name);
        jsonObject.put("type",type);
        jsonObject.put("x",x);
        jsonObject.put("y",y);
        return jsonObject.toString();
    }
    public static HuaPiece parseFromJson(String string) throws JSONException {
        JSONObject json =  new JSONObject(string);
        String name = json.getString("name");
        int type = json.getInt("type");
        int x = json.getInt("x");
        int y = json.getInt("y");
        return new HuaPiece(name,type,x,y);
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
