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

//存储在数据库中的棋盘格式
public class DBBoard {
    public final static int DBBOARD_TYPE_START = 1; //boards
    public final static int DBBOARD_TYPE_GOING = 2; //going_boards
    private String name;
    private GameType gameType;
    private String board;
    private int steps;      //sudoku下，存储seconds
    private String solution = "";
    private String saveDate = "";  //DBBOARD_TYPE_GOING模式下可用
    int dbType = DBBOARD_TYPE_START;

    DBBoard(String name, GameType gameType, String board, int steps, String solution) {
        this.name = name;
        this.gameType = gameType;
        this.board = board;
        this.steps = steps;
        this.solution = solution;
    }
    DBBoard(int dbType, String name, GameType gameType, String board, int seconds, String saveDate) {
        this.dbType = dbType;
        this.name = name;
        this.gameType = gameType;
        this.board = board;
        this.steps = seconds;
        this.saveDate = saveDate;
    }
    public String getName() {
        return name;
    }

    public String getBoardString() {
        return board;
    }

    public int getSteps() {
        return steps;
    }
    public int getSeconds(){
        return steps;
    }
    public String getSolutionString() {
        return solution;
    }

    public String getSaveDate() {
        return saveDate;
    }

    public GameType getGameType() {
        return gameType;
    }

    public HuaBoard getBoard() {
        if (gameType != GameType.HUARONGDAO) {
            return null;
        }
        HuaBoard board = HuaBoard.fromPiecesString(this.board);
        board.name = name;
        board.bestSolution = Solution.parseFromJsonString(solution);
        return board;
    }

    public Board toBoard() {
        Board board;
        switch (gameType) {
            /*case HUARONGDAO:
                baseBoard = Board.fromDBString(board);
                break;*/
            case BLOCK:
                board = BrickBoard.fromDBString(this.board);
                break;
            case BOX:
                board = BoxBoard.fromDBString(this.board);
            case SUDOKU:
                if(dbType == DBBOARD_TYPE_START) {
                    board = SudokuBoard.fromDBString(this.board);
                }else{
                    board = SudokuBoard.fromGoingDBString(this.board);
                    ((SudokuBoard)board).seconds = getSeconds();
                }
                board.name = name;
                break;
            default:
                Log.v("dbboard", "unknown type");
                return null;
        }
        board.name = name;
        board.bestSolution = Solution.parseFromJsonString(solution);
        return board;
    }

    public JSONObject toJson() {
        //根据dbtype的不同，生成不同的json
        //同时加上command
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("db_type",dbType);
            jsonObject.put("name", name);
            jsonObject.put("type", gameType.toInt());
            jsonObject.put("board", board);  //String格式
            if(dbType == DBBOARD_TYPE_START) {
                //jsonObject.put("command", "save");
                jsonObject.put("steps", steps);
                jsonObject.put("solution", solution);
            }else{
                //jsonObject.put("command", "save_going");
                jsonObject.put("seconds", steps);
                jsonObject.put("save_date", saveDate);
            }
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String toJsonString() {
        return toJson().toString();
    }

    public static DBBoard fromJson(JSONObject jsonObject) {
        //两种情况：
        //一：START
        //
        //二：GOING
        //			 json.put("name",name);
        //			 json.put("type", type);
        //			 json.put("board",board);
        //			 json.put("seconds", seconds);
        //			 json.put("save_date", saveDate);
        try {
            int dbType = jsonObject.getInt("db_type");
            String name = jsonObject.getString("name");
            String board = jsonObject.getString("board");
            GameType gameType = GameType.toEnum(jsonObject.getInt("type"));
            if(dbType == DBBOARD_TYPE_START) {
                int steps = jsonObject.getInt("steps");
                String solution = jsonObject.getString("solution");
                return new DBBoard(name, gameType, board, steps, solution);
            }else{
                int seconds = jsonObject.getInt("seconds");
                String saveDate = jsonObject.getString("save_date");
                return new DBBoard(DBBOARD_TYPE_GOING,name,gameType,board,seconds,saveDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static DBBoard fromJsonString(String string) {
        try {
            JSONObject jsonObject = new JSONObject(string);
            return fromJson(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String save() {
        //存储到数据库中，注意不得于mainthread调用该方法.

        MySocket mySocket = new MySocket();
        if (!mySocket.connect()) {
            Log.v("dbboard", "连接服务器异常");
            return "连接服务器异常";
        }
        String sendString = "";
        JSONObject jsonObject = toJson();
        try {
            if (dbType == DBBOARD_TYPE_START) {
                jsonObject.put("command", "save");
            } else {
                jsonObject.put("command", "save_going");
            }
        }catch (Exception e){
            e.printStackTrace();
            return "组装json出错";
        }
        if (jsonObject != null) {
            sendString = jsonObject.toString();
        }

        if (sendString.equals("") || !mySocket.send(sendString)) {
            Log.v("dbboard", "发送消息失败");
            return "发送消息失败";
        }
        String reply = mySocket.recieve().toString();
        if (reply.equals("") || reply.startsWith("failed")) {
            Log.v("dbboard", "接收消息失败:" + reply);
            return "接收消息失败:" + reply;
        }
        Log.v("dbboard", reply);
        return reply;
    }
    public String deleteBoard(){
        //删除存储在boards上的记录

        MySocket mySocket = new MySocket();
        if (!mySocket.connect()) {
            Log.v("dbboard", "连接服务器异常");
            return "连接服务器异常";
        }
        String sendString = "";
        JSONObject jsonObject = toJson();
        try {
            jsonObject.put("command", "del_board");
            jsonObject.put("name",name);
            jsonObject.put("gameType",GameType.SUDOKU.toInt());
            jsonObject.put("board",board);
        }catch (Exception e){
            e.printStackTrace();
            return "组装json出错";
        }
        if (jsonObject != null) {
            sendString = jsonObject.toString();
        }

        if (sendString.equals("") || !mySocket.send(sendString)) {
            Log.v("dbboard", "发送消息失败");
            return "发送消息失败";
        }
        String reply = mySocket.recieve().toString();
        if (reply.equals("") || reply.startsWith("failed")) {
            Log.v("dbboard", "接收消息失败:" + reply);
            return "接收消息失败:" + reply;
        }
        Log.v("dbboard", reply);
        return reply;
    }
    public String deleteGoingBoard(){
        //删除存储在going_boards上的记录

        MySocket mySocket = new MySocket();
        if (!mySocket.connect()) {
            Log.v("dbboard", "连接服务器异常");
            return "连接服务器异常";
        }
        String sendString = "";
        JSONObject jsonObject = toJson();
        try {
            jsonObject.put("command", "del_going_board");
            jsonObject.put("name",name);
            jsonObject.put("gameType",GameType.SUDOKU.toInt());
        }catch (Exception e){
            e.printStackTrace();
            return "组装json出错";
        }
        if (jsonObject != null) {
            sendString = jsonObject.toString();
        }

        if (sendString.equals("") || !mySocket.send(sendString)) {
            Log.v("dbboard", "发送消息失败");
            return "发送消息失败";
        }
        String reply = mySocket.recieve().toString();
        if (reply.equals("") || reply.startsWith("failed")) {
            Log.v("dbboard", "接收消息失败:" + reply);
            return "接收消息失败:" + reply;
        }
        Log.v("dbboard", reply);
        return reply;
    }

    public Solution query_solution() {
        //TODO
        MySocket mySocket = new MySocket();
        if (!mySocket.connect()) {
            Log.v("solution", "连接服务器异常");
            return null;
        }
        String sendString = "";
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("command", "query_solution");
            jsonObject.put("board", board);
            sendString = jsonObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if (sendString.equals("") || !mySocket.send(sendString)) {
            Log.v("solution", "发送消息失败");
            return null;
        }
        String reply = mySocket.recieve().toString();
        Log.v("solution", reply);
        if (reply.equals("") || reply.startsWith("failed")) {
            return null;
        }
        return Solution.parseFromJsonString(reply);
    }

    public static List<DBBoard> query_boards(int select, int type) {
        String sendString;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("command", "query_boards");
            jsonObject.put("select", select);
            jsonObject.put("gameType", type);
            sendString = jsonObject.toString();
        } catch (Exception e) {
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
        List<DBBoard> dbBoardList = listFromDBString(reply.toString());
        if (dbBoardList == null) {
            Log.v("dbboard", "接收消息错误：" + reply.toString());
            return null;
        }
        return dbBoardList;
    }

    public static List<DBBoard> query_going_boards(int type) {
        String sendString;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("command", "query_going_boards");
            jsonObject.put("gameType", type);
            sendString = jsonObject.toString();
        } catch (Exception e) {
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
        List<DBBoard> dbBoardList = listFromGoingDBString(reply.toString());
        if (dbBoardList == null) {
            Log.v("dbboard", "接收消息错误：" + reply.toString());
            return null;
        }
        return dbBoardList;
    }

    public static List<DBBoard> listFromDBString(String string) {
        List<DBBoard> dbBoardList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(string);
            int size = jsonObject.getInt("size");
            JSONArray jsonArray = jsonObject.getJSONArray("boards");
            for (int i = 0; i < jsonArray.length(); i++) {
                DBBoard dbBoard = DBBoard.fromJson((JSONObject) jsonArray.get(i));
                if (dbBoard == null) {
                    Log.v("dbboard", "fromJson error");
                    return null;
                }
                dbBoardList.add(dbBoard);
            }
        } catch (Exception e) {
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

    public static List<DBBoard> listFromGoingDBString(String string) {
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
        //按照saveDate升序排序
        Collections.sort(dbBoardList, new Comparator<DBBoard>() {
            @Override
            public int compare(DBBoard t1, DBBoard t2) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date dt1 = format.parse((t1).getSaveDate());
                    Date dt2 = format.parse((t2).getSaveDate());
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
}
