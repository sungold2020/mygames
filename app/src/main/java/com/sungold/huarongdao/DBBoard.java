package com.sungold.huarongdao;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//存储在数据库中的棋盘格式
public class DBBoard {
    private String name;
    private GameType gameType;
    private String board;
    private int    steps;
    private String solution;
    DBBoard(String name,GameType gameType,String board,int steps,String solution){
        this.name = name;
        this.gameType = gameType;
        this.board = board;
        this.steps = steps;
        this.solution = solution;
    }
     public String getName() { return  name; }
     public String getBoardString() { return board; }
     public int    getSteps()  { return  steps; }
     public String getSolutionString() { return solution; }
     public GameType getGameType() { return gameType; }

     public HuaBoard getBoard(){
        if (gameType != GameType.HUARONGDAO){
            return null;
        }
        HuaBoard board = HuaBoard.fromPiecesString(this.board);
        board.name = name;
        board.bestSolution = Solution.parseFromJsonString(solution);
        return board;
    }

    public Board toBoard(){
        Board board;
        switch(gameType){
            /*case HUARONGDAO:
                baseBoard = Board.fromDBString(board);
                break;*/
            case BLOCK:
                board = BrickBoard.fromDBString(this.board);
                break;
            case BOX:
                board = BoxBoard.fromDBString(this.board);
            case SUDOKU:
                board = SudokuBoard.fromDBString(this.board);
                break;
            default:
                Log.v("dbboard","unknown type");
                return null;
        }
        board.name = name;
        board.bestSolution =  Solution.parseFromJsonString(solution);
        return board;
    }
    public JSONObject toJson(){
        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("command","save");
            jsonObject.put("name",name);
            jsonObject.put("type",gameType.toInt());
            jsonObject.put("board",board);  //String格式
            jsonObject.put("steps",steps);
            jsonObject.put("solution",solution);
            return jsonObject;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public String toJsonString(){
        return toJson().toString();
    }
    public static DBBoard fromJson(JSONObject jsonObject){
        try {
            String name = jsonObject.getString("name");
            GameType gameType = GameType.toEnum(jsonObject.getInt("type"));
            int steps = jsonObject.getInt("steps");
            String board = jsonObject.getString("board");
            String solution = jsonObject.getString("solution");
            return new DBBoard(name, gameType, board, steps, solution);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public static DBBoard fromJsonString(String string){
        try {
            JSONObject jsonObject = new JSONObject(string);
            return fromJson(jsonObject);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public Boolean save(){
        //存储到数据库中，注意不得于mainthread调用该方法.

        MySocket mySocket = new MySocket();
        if (!mySocket.connect()) {
            Log.v("dbboard" ,"连接服务器异常");
            return false;
        }
        String sendString = "";
        JSONObject jsonObject = toJson();
        if (jsonObject != null){
            sendString = jsonObject.toString();
        }

        if (sendString.equals("") || !mySocket.send(sendString)) {
            Log.v("dbboard", "发送消息失败");
            return false;
        }
        String reply = mySocket.recieve().toString();
        if (reply.equals("") || reply.startsWith("failed")){
            Log.v("dbboard", "接收消息失败:"+reply);
            return false ;
        }
        Log.v("dbboard",reply);
        return true;
    }
    public Solution query_solution(){
        //TODO
        MySocket mySocket = new MySocket();
        if (!mySocket.connect()) {
            Log.v("solution","连接服务器异常");
            return null;
        }
        String sendString = "";
        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("command","query_solution");
            jsonObject.put("board",board);
            sendString = jsonObject.toString();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        if (sendString.equals("") || !mySocket.send(sendString)) {
            Log.v("solution", "发送消息失败");
            return null;
        }
        String reply = mySocket.recieve().toString();
        Log.v("solution",reply);
        if (reply.equals("") || reply.startsWith("failed")){
            return null;
        }
        return Solution.parseFromJsonString(reply);
    }
    public static List<DBBoard> query_boards(int select,int type){
        String sendString;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("command", "query_boards");
            jsonObject.put("select",select);
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
                DBBoard dbBoard = DBBoard.fromJson((JSONObject) jsonArray.get(i));
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
        //按照steps升序排序
        Collections.sort(dbBoardList, new Comparator<DBBoard>() {
            @Override
            public int compare(DBBoard t1, DBBoard t2) {
                try {
                    if (t1.getSteps() < t2.getSteps()) {
                        return -1;
                    } else if (t1.getSteps() > t2.getSteps()) {
                        return 1;
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
}
