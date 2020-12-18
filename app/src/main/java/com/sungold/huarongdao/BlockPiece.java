package com.sungold.huarongdao;

import org.json.JSONException;
import org.json.JSONObject;

public class BlockPiece {
    //四种棋子+空
    //public final static int PIECE_CAOCHAO = 1111;  //
    public final static int PIECE_HORIZON = 11;  //水平的木条
    public final static int PIECE_VERTICAL = 1; //垂直的木条
    public final static int PIECE_EMPTY = 5;    //空

    public final static int DIRECTION_UP = 0;
    public final static int DIRECTION_DOWN = 1;
    public final static int DIRECTION_LEFT = 2;
    public final static int DIRECTION_RIGHT = 3;

    public String name; //命名规则：竖一，横一等，王为待解救的木条
    public int type;
    public int length;
    public int x,y; //以左下角的坐标来记录棋子的位置

    BlockPiece(String name, int type, int length,int x, int y){
        this.name = name;
        this.type = type;
        this.length = length;
        this.x = x;
        this.y = y;
    }
    public BlockPiece copyPiece(){
        //复制新的棋子
        BlockPiece newPiece = new BlockPiece(name,type,length,x,y);
        return newPiece;
    }
    /*public Boolean isOccupied(int x,int y){
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
    }*/
    /*public int getDirection(String direction){
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
    }*/
    public void printPiece(){
        System.out.println(String.format("%s:%d,%d",name,x,y));
    }
    public static String getDirectionName(int direction){
        switch(direction){
            case BlockPiece.DIRECTION_UP:
                return "UP";
            case BlockPiece.DIRECTION_DOWN:
                return "DOWN";
            case BlockPiece.DIRECTION_LEFT:
                return "LEFT";
            case BlockPiece.DIRECTION_RIGHT:
                return "RIGHT";
            default:
                System.out.println("unknow direction");
                return "UNKNOWN";
        }
    }
    public static String getTypeName(int type){
        switch(type){
            case BlockPiece.PIECE_VERTICAL:
                return "竖";
            case BlockPiece.PIECE_HORIZON:
                return "横";
            case BlockPiece.PIECE_EMPTY:
                return "空";
            default:
                return "X";
        }
    }
    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name",name);
        jsonObject.put("type",type);
        jsonObject.put("lenght",length);
        jsonObject.put("x",x);
        jsonObject.put("y",y);
        return jsonObject;
    }
    public static BlockPiece parseFromJson(JSONObject json) throws JSONException {
        String name = json.getString("name");
        int type = json.getInt("type");
        int length = json.getInt("length");
        int x = json.getInt("x");
        int y = json.getInt("y");
        return new BlockPiece(name,type,length,x,y);
    }
    public static BlockPiece parseFromJson(String string) throws JSONException {
        JSONObject json =  new JSONObject(string);
        return parseFromJson(json);
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
                return "X";
        }
    }
    public String toDBString(){
        //转换成一个字符串，用于存储再数据库
        return String.format("%s,%d,%d,%d,%d",name,type,length,x,y);
    }
    public static BlockPiece fromDBString(String string){
        if (string.equals("")) { return null; }
        String[] strings = string.split(",");
        if (strings.length != 5) { return null; }
        String name = strings[0];
        int type = Integer.valueOf(strings[1]);
        int length  = Integer.valueOf(strings[2]);
        int x  = Integer.valueOf(strings[3]);
        int y  = Integer.valueOf(strings[4]);
        return new BlockPiece(name,type,length,x,y);
    }
}
