package com.sungold.huarongdao;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.sungold.huarongdao.HuaPiece.getDirectionName;

class Solution{
    //解法
    List<Step> stepList = new ArrayList<>();
    Solution() {;}
    //通过输入的boardList得到解法
    Solution(List<HuaBoard> boardList){
        if (boardList == null){
            //debug("boardList is null");
            return;
        }
        for (int i=0; i<boardList.size()-1; i++){
            String name = boardList.get(i).nextStepPiece.name;
            int direction = boardList.get(i).nextStepDirection;
            stepList.add(new Step(name,direction));
        }
    }
    //即将被抛弃
    public static Solution fromBlockBoard(List<BlockBoard> boardList){
        Solution solution = new Solution();
        if (boardList == null){
            //debug("boardList is null");
            return null;
        }
        for (int i=0; i<boardList.size()-1; i++){
            String name = boardList.get(i).nextStepBlockPiece.name;
            int direction = boardList.get(i).nextStepDirection;
            solution.stepList.add(new Step(name,direction));
        }
        return solution;
    }
    public static Solution fromBoardList(List<Board> boardList){
        Solution solution = new Solution();
        if (boardList == null){
            //debug("boardList is null");
            return null;
        }
        for (int i=0; i<boardList.size()-1; i++){
            String name = boardList.get(i).nextStepPiece.name;
            int direction = boardList.get(i).nextStepDirection;
            solution.stepList.add(new Step(name,direction));
        }
        return solution;
    }

    public void printSolution(){
        for(int i=0; i<stepList.size(); i++){
            String name = stepList.get(i).pieceName;
            String direction = getDirectionName(stepList.get(i).direction);
            System.out.println(String.format("%s : %s",name,direction));
        }
        System.out.println(String.format("解法共%d步：",stepList.size()));
    }
    public int getSteps() { return stepList.size(); }

    public List<Board> buildBoardList(Board startBoard){
        Board board = startBoard;
        //Board nextBoard = null;
        List <Board> boardList = new ArrayList<>();
        String name;
        int direction;
        for(int i=0; i<stepList.size(); i++) {
            name = stepList.get(i).pieceName;
            direction = stepList.get(i).direction;
            board.nextStepPiece = board.getPieceByName(name);
            board.nextStepDirection = direction;
            boardList.add(board);
            board = board.newBoardAfterMove(name, direction);
            System.out.println(String.format("%s move %s",name, Piece.directionToString(direction)));
            if (board == null) {
                break;
            }
            board.printBoard();
        }
        if (board != null ) { boardList.add(board);  }//把最后一步生成的board加入
        return boardList;
    }
    //给Hua用，待废弃
    public List<HuaBoard> buildHuaBoardList(HuaBoard startBoard){
        HuaBoard board = startBoard;
        //Board nextBoard = null;
        List <HuaBoard> boardList = new ArrayList<>();
        String name;
        int direction;
        for(int i=0; i<stepList.size(); i++) {
            name = stepList.get(i).pieceName;
            direction = stepList.get(i).direction;
            board.nextStepPiece = board.getPieceByName(name);
            board.nextStepDirection = direction;
            boardList.add(board);
            board = board.newBoardAfterMove(name, direction);
            System.out.println(String.format("%s move %s",name, Piece.directionToString(direction)));
            if (board == null) {
                break;
            }
            board.printBoard();
        }
        if (board != null ) { boardList.add(board);  }//把最后一步生成的board加入
        return boardList;
    }
    public List<BlockBoard> buildBlockBoardList(BlockBoard startBoard){
        BlockBoard board = startBoard;
        //Board nextBoard = null;
        List <BlockBoard > boardList = new ArrayList<>();
        String name;
        int direction;
        for(int i=0; i<stepList.size(); i++) {
            name = stepList.get(i).pieceName;
            direction = stepList.get(i).direction;
            board.nextStepBlockPiece = board.getBlockPieceByName(name);
            board.nextStepDirection = direction;
            boardList.add(board);
            board = board.newBoardAfterMove(name, direction);
            System.out.println(String.format("%s move %s",name, HuaPiece.getDirectionName(direction)));
            if (board == null) {
                break;
            }
            //board.printBoard();
        }
        if (board != null ) { boardList.add(board);  }//把最后一步生成的board加入
        return boardList;
    }
    public JSONObject toJson(){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("size", stepList.size());

            JSONArray jsonArray = new JSONArray();
            for(int i=0; i<stepList.size(); i++) {
                jsonArray.put(i,stepList.get(i).toJson());
            }
            jsonObject.put("steps",jsonArray);
            return jsonObject;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public static Solution parseFromJson(JSONObject jsonObject){
        try{
            //JSONObject jsonObject = new JSONObject(string);
            int size = jsonObject.getInt("size");
            JSONArray jsonArray = jsonObject.getJSONArray("steps");
            List<Step> newStepList = new ArrayList<>();
            for(int i=0; i<jsonArray.length(); i++){
                //String stepString = (String) jsonArray.get(i);
                newStepList.add(Step.parseFromJson((JSONObject)jsonArray.get(i)));
            }
            Solution solution = new Solution();
            solution.stepList = newStepList;
            return solution;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public static Solution  parseFromJsonString(String string){
        try{
            JSONObject jsonObject = new JSONObject(string);
            return parseFromJson(jsonObject);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
class Step{
    public String pieceName;
    public int direction;
    Step(String pieceName,int direction){
        this.pieceName = pieceName;
        this.direction = direction;
    }
    public JSONObject toJson(){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", pieceName);
            jsonObject.put("direction", direction);
            return jsonObject;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public static Step parseFromJsonString(String string) {
        try {
            JSONObject json = new JSONObject(string);
            String name = json.getString("name");
            int direction = json.getInt("direction");
            return new Step(name,direction);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public static Step parseFromJson(JSONObject jsonObject) {
        try {
            String name = jsonObject.getString("name");
            int direction = jsonObject.getInt("direction");
            return new Step(name,direction);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}