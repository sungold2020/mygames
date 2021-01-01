package com.sungold.huarongdao;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
/*
public class GoingDBBoard extends DBBoard{
    int seconds;
    String saveDate;

    GoingDBBoard(String name, GameType gameType, String board, int seconds, String saveDate) {
        super(name, gameType, board, 0, "");
        this.seconds = seconds;
        this.saveDate = saveDate;
    }
    public String getSaveDate(){
        return saveDate;
    }
    public static List<DBBoard> query_going_boards(int type){
        String sendString;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("command", "query_going_boards");
            jsonObject.put("gameType",type);
            sendString = jsonObject.toString();
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        MySocket mySocket = new MySocket();
        if (!mySocket.connect()) {
            Log.v("dbboard", "刷新失败：连接服务器异常");
            return null;
        }
        if (!mySocket.send(sendString)) {
            Log.v("dbboard", "刷新失败：发送服务器请求失败");
            return null;
        }
        StringBuffer reply = mySocket.recieve();
        List <DBBoard> dbBoardList = listFromDBString(reply.toString());
        if (dbBoardList == null) {
            Log.v("dbboard", "接收消息错误："+reply.toString());
            return null;
        }
        return dbBoardList;
    }
    public static List<DBBoard> listFromDBString(String string){
        List <DBBoard> dbBoardList = new ArrayList<>();
        try{
            JSONObject jsonObject = new JSONObject(string);
            int size = jsonObject.getInt("size");
            JSONArray jsonArray = jsonObject.getJSONArray("boards");
            for(int i=0; i<jsonArray.length(); i++){
                GoingDBBoard dbBoard = GoingDBBoard.fromJson((JSONObject) jsonArray.get(i));
                if (dbBoard == null) {
                    Log.v("dbboard", "fromJson error");
                    return null;
                }
                dbBoardList.add(dbBoard);
            }
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
        //按照saveDate升序排序
        Collections.sort(dbBoardList, new Comparator<DBBoard>() {
            @Override
            public int compare(DBBoard t1, DBBoard t2) {

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date dt1 = format.parse(((GoingDBBoard)t1).getSaveDate());
                    Date dt2 = format.parse(((GoingDBBoard)t2).getSaveDate());
                    if (dt1.getTime() < dt2.getTime()) {
                        return 1;
                    } else if (dt1.getTime() > dt2.getTime()) {
                        return -1;
                    } else {
                        return 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });
        return dbBoardList;
    }
    public static GoingDBBoard fromJson(JSONObject jsonObject){
        try {
            String name = jsonObject.getString("name");
            GameType gameType = GameType.toEnum(jsonObject.getInt("type"));
            int seconds = jsonObject.getInt("seconds");
            String board = jsonObject.getString("board");
            String saveDate = jsonObject.getString("saveDate");
            return new GoingDBBoard(name, gameType, board, seconds, saveDate);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
*/