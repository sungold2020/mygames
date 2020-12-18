package com.sungold.huarongdao;

import android.os.Handler;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HuaFindSolution extends Thread{
    //每走一步，棋盘状态发生变化，生产一个新的棋盘，棋盘列表就构成了走棋的步骤
    public final static int MAX_X=4;
    public final static int MAX_Y=5;



    public final static int DEST_X=2;
    public final static int DEST_Y=1;

    public final static int MAX_STEPS = 200;

    public HuaBoard startBoard; //起始棋盘状态
    public Handler handler = null;
    public Solution solution = null;

    public  List<HuaBoard> boardList = new ArrayList<>(); //一个链表存储当前检索过的步骤。

    public  int bestSteps = MAX_STEPS;  //bestSteps记录之前最优解的最小步数

    //棋盘的哈希表，用于快速检索,hash值用于检索数组序号，
    //表中存储的是一个链表（相同hash值的节点组装成一个链表。
    //链表的每一个节点，存储的是棋盘及该棋盘加入时的depth（步数），
    //为什么需要depth？检索重复棋盘时，如果重复的棋盘就不再进行搜索的话，可能出现这样一种情况：
    // 某一个棋盘状态可能曾经被搜索到，但是深度比较深（步数比较多），结果继续来去的解就不是最优解。
    // 下一次可能最优解走到这个棋盘状态时，如果判断重复，就会回退，从而找不出最优解。
    public MyList[] boardHashTable;

    //用于给UI显示的消息队列
    public String message = "";

    HuaFindSolution(HuaBoard board){
        startBoard = board;

        //初始化hash表，取最大hash值来分配数组
        int maxHash=0;
        /*maxHash = Piece.PIECE_CAOCHAO * (MAX_X * HASH_CONST_X + MAX_Y * HASH_CONST_Y);
        if (board.jiang != null){
            maxHash += board.jiang.length * (Piece.PIECE_VERTICAL * (MAX_X * HASH_CONST_X + MAX_Y * HASH_CONST_Y));
        }
        if (board.shuai != null){
            maxHash += board.shuai.length * (Piece.PIECE_HORIZON * (MAX_X * HASH_CONST_X + MAX_Y * HASH_CONST_Y));
        }
        if (board.bing != null){
            maxHash += board.bing.length * (Piece.PIECE_SOLDIER * (MAX_X * HASH_CONST_X + MAX_Y * HASH_CONST_Y));
        }
        if (board.space != null){
            maxHash += board.space.length * (Piece.PIECE_EMPTY * (MAX_X * HASH_CONST_X + MAX_Y * HASH_CONST_Y));
        } */
        boardHashTable = new MyList[startBoard.maxHash];
        for(int i=0; i<maxHash; i++){  boardHashTable[i] = null; }
        System.out.println(boardHashTable.length);
    }
    public void run(){
        if (querySolution()) {
            Log.v("solution","从服务器查询到最优解");
            if (solution != null)  {
                if (handler != null) { handler.sendEmptyMessage(0x13); }
                return;
            }
        }
        if (startBoard.space.length == 2){
            moveNextStepSpace(startBoard);
        }else{
            moveNextStepPiece(startBoard);
        }
        //return solution;
        if (handler != null){
            handler.sendEmptyMessage(0x12); //结束
            Log.v("solution","结束");
            //message += "处理结束\n";
        }
        if (solution == null){
            Log.v("solution","无解");
            return;
        }
        String string = solution.toJson().toString();
        Log.v("solution",string);
        solution = Solution.parseFromJsonString(string);
        save();
    }

    public void moveNextStepSpace(HuaBoard board){
        //搜索最优解，通过检测空格方式来移动棋子，效率高
        if(board.isSuccess()){

            if (boardList.size() <= bestSteps){ //如果解法步数小于之前的解法，储存并打印
                pushBoard(board);
                solution = new Solution(boardList);
                bestSteps = solution.getSteps();
                //solution.printSolution();
                popBoard(board);
                if (handler != null){
                    Log.v("solution","发送消息");
                    handler.sendEmptyMessage(0x11); //发现更优解
                    message += String.format("找到更优解：%s 步\n",solution.getSteps());
                }
            }
            return;
        }

        if (boardList.size() >= bestSteps){
            //已经超出最优步骤，回退
            return;
        }

        pushBoard(board); //把当前棋盘状态存入链表
        HuaBoard nextBoard=null;
        nextBoard = board.moveSpace(0, HuaPiece.DIRECTION_UP);
        if (isNewBoard(nextBoard)){
            moveNextStepSpace(nextBoard);
        }
        nextBoard = board.moveSpace(0, HuaPiece.DIRECTION_DOWN);
        if (isNewBoard(nextBoard)){
            moveNextStepSpace(nextBoard);
        }
        nextBoard = board.moveSpace(0, HuaPiece.DIRECTION_LEFT);
        if (isNewBoard(nextBoard)){
            moveNextStepSpace(nextBoard);
        }
        nextBoard = board.moveSpace(0, HuaPiece.DIRECTION_RIGHT);
        if (isNewBoard(nextBoard)){
            moveNextStepSpace(nextBoard);
        }
        nextBoard = board.moveSpace(1, HuaPiece.DIRECTION_UP);
        if (isNewBoard(nextBoard)){
            moveNextStepSpace(nextBoard);
        }
        nextBoard = board.moveSpace(1, HuaPiece.DIRECTION_DOWN);
        if (isNewBoard(nextBoard)){
            moveNextStepSpace(nextBoard);
        }
        nextBoard = board.moveSpace(1, HuaPiece.DIRECTION_LEFT);
        if (isNewBoard(nextBoard)){
            moveNextStepSpace(nextBoard);
        }
        nextBoard = board.moveSpace(1, HuaPiece.DIRECTION_RIGHT);
        if (isNewBoard(nextBoard)){
            moveNextStepSpace(nextBoard);
        }
        if (board.space[0].x == board.space[1].x){
            nextBoard = board.moveTwoSpace(HuaPiece.DIRECTION_LEFT);

            if (isNewBoard(nextBoard)){
                moveNextStepSpace(nextBoard);
            }
            nextBoard = board.moveTwoSpace(HuaPiece.DIRECTION_RIGHT);
            if (isNewBoard(nextBoard)){
                moveNextStepSpace(nextBoard);
            }
        }else if (board.space[0].y == board.space[1].y){
            nextBoard = board.moveTwoSpace(HuaPiece.DIRECTION_UP);
            if (isNewBoard(nextBoard)){
                moveNextStepSpace(nextBoard);
            }
            nextBoard = board.moveTwoSpace(HuaPiece.DIRECTION_DOWN);
            if (isNewBoard(nextBoard)){
                moveNextStepSpace(nextBoard);
            }
        }else{
            ; //两个空格没有连一起
        }
        //所有可能的步骤都走完了，就回退
        popBoard(board);

        return;
    }
    public void moveNextStepPiece(HuaBoard board){
        //搜索最优解，通过移动棋子的方式进行移动
        if(board.isSuccess()){
            if (boardList.size() <= bestSteps){ //如果解法步数小于之前的解法，储存并打印
                pushBoard(board);
                solution = new Solution(boardList);
                bestSteps = solution.getSteps();
                //solution.printSolution();
                popBoard(board);
                if (handler != null){
                    Log.v("solution","发送消息");
                    handler.sendEmptyMessage(0x11); //发现更优解
                    message += String.format("找到更优解：%s 步\n",solution.getSteps());
                }
            }
            return;
        }

        if (boardList.size() >= bestSteps){
            //已经超出最优步骤，回退
            return;
        }
        pushBoard(board); //把当前棋盘状态存入链表
        HuaBoard nextBoard=null;

        //移动曹操
        nextBoard = board.newBoardAfterMove(board.caochao, HuaPiece.DIRECTION_UP);
        if (isNewBoard(nextBoard)){
            moveNextStepPiece(nextBoard);
        }
        nextBoard = board.newBoardAfterMove(board.caochao, HuaPiece.DIRECTION_DOWN);
        if (isNewBoard(nextBoard)){
            moveNextStepPiece(nextBoard);
        }
        nextBoard = board.newBoardAfterMove(board.caochao, HuaPiece.DIRECTION_LEFT);
        if (isNewBoard(nextBoard)){
            moveNextStepPiece(nextBoard);
        }
        nextBoard = board.newBoardAfterMove(board.caochao, HuaPiece.DIRECTION_RIGHT);
        if (isNewBoard(nextBoard)){
            moveNextStepPiece(nextBoard);
        }
        if (board.jiang != null){
            for(int i=0; i<board.jiang.length; i++){
                //移动将
                nextBoard = board.newBoardAfterMove(board.jiang[i], HuaPiece.DIRECTION_UP);
                if (isNewBoard(nextBoard)){
                    moveNextStepPiece(nextBoard);
                }
                nextBoard = board.newBoardAfterMove(board.jiang[i], HuaPiece.DIRECTION_DOWN);
                if (isNewBoard(nextBoard)){
                    moveNextStepPiece(nextBoard);
                }
                nextBoard = board.newBoardAfterMove(board.jiang[i], HuaPiece.DIRECTION_LEFT);
                if (isNewBoard(nextBoard)){
                    moveNextStepPiece(nextBoard);
                }
                nextBoard = board.newBoardAfterMove(board.jiang[i], HuaPiece.DIRECTION_RIGHT);
                if (isNewBoard(nextBoard)){
                    moveNextStepPiece(nextBoard);
                }
            }
        }
        if (board.shuai != null){
            for(int i=0; i<board.shuai.length; i++){
                //移动帅
                nextBoard = board.newBoardAfterMove(board.shuai[i], HuaPiece.DIRECTION_UP);
                if (isNewBoard(nextBoard)){
                    moveNextStepPiece(nextBoard);
                }
                nextBoard = board.newBoardAfterMove(board.shuai[i], HuaPiece.DIRECTION_DOWN);
                if (isNewBoard(nextBoard)){
                    moveNextStepPiece(nextBoard);
                }
                nextBoard = board.newBoardAfterMove(board.shuai[i], HuaPiece.DIRECTION_LEFT);
                if (isNewBoard(nextBoard)){
                    moveNextStepPiece(nextBoard);
                }
                nextBoard = board.newBoardAfterMove(board.shuai[i], HuaPiece.DIRECTION_RIGHT);
                if (isNewBoard(nextBoard)){
                    moveNextStepPiece(nextBoard);
                }
            }
        }
        if (board.bing != null){
            for(int i=0; i<board.bing.length; i++){
                //移动兵
                nextBoard = board.newBoardAfterMove(board.bing[i], HuaPiece.DIRECTION_UP);
                if (isNewBoard(nextBoard)){
                    moveNextStepPiece(nextBoard);
                }
                nextBoard = board.newBoardAfterMove(board.bing[i], HuaPiece.DIRECTION_DOWN);
                if (isNewBoard(nextBoard)){
                    moveNextStepPiece(nextBoard);
                }
                nextBoard = board.newBoardAfterMove(board.bing[i], HuaPiece.DIRECTION_LEFT);
                if (isNewBoard(nextBoard)){
                    moveNextStepPiece(nextBoard);
                }
                nextBoard = board.newBoardAfterMove(board.bing[i], HuaPiece.DIRECTION_RIGHT);
                if (isNewBoard(nextBoard)){
                    moveNextStepPiece(nextBoard);
                }
            }
        }
        //所有可能的步骤都走完了，就回退
        popBoard(board);
        return;
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

    public void pushBoard(HuaBoard board){
        //debug
        /*if(boardList.size() == 0) {
             System.out.println("startBoard:");
             startBoard.printBoard();
             System.out.println("");
        }else{
            System.out.print("走棋->");
            boardList.get(boardList.size()-1).printStep();
            board.printBoard();
             System.out.println("");
        }*/

        int hash = board.getHash();
        if (boardHashTable[hash] == null) { boardHashTable[hash] = new MyList(); }
        boardHashTable[hash].add(board,boardList.size()); //加入哈希表的对应hash值的链表
        boardList.add(board);//注意：必须在hashtable之后执行，否则bordList.size()不对
    }
    public void popBoard(HuaBoard board){
        boardList.remove(boardList.size()-1);
        /*System.out.println("回退到：");
        currentBoard().printBoard();
        System.out.println("");*/
    }
    public HuaBoard currentBoard(){
        return boardList.get(boardList.size()-1);
    }
    public Boolean isNewBoard(HuaBoard board){
        //非空，且和已有棋盘列表中的棋盘不重复，就返回true
        if (board == null) { return false; }

        int hash = board.getHash();
        if (boardHashTable[hash] == null) { return true; }
        if (boardHashTable[hash].find(board,boardList.size())){
            //对应hash值的链表中如果找到重复的棋盘，就认为是重复棋盘
            //注意传递boradList.size()，也即depth，目的用于比较重复棋盘的深度，如果已经存在相同状态的棋盘而且深度更浅（步数更少），就不需要检索，也即认为是新棋盘。
            return false;
        }
        return true;
    }
    public Boolean isSameBoard(SavedBoard board1,SavedBoard board2){

       /* for(int x=0; x<MAX_X; x++){
            for(int y=0; y<MAX_Y; y++){
                if(board1.pieces[x][y] != board2.pieces[x][y]) { return false;}
            }
        }*/
        if (board1.hash != board2.hash) { return  false; }
        return true;

    }
    public void printStep(){
        System.out.println("步骤如下：");
        for(int i=0; i<boardList.size(); i++){
            String name = boardList.get(i).getNextStepName();
            String direction = boardList.get(i).getNextStepDirection();
            System.out.println(String.format("%s move %s",name,direction));
        }
        System.out.println(String.format("total step:%d",boardList.size()-1));
    }


    class MyList{
        //savedBoard对应的链表，用于存储在哈希表
        public Node head;
        public MyList(){
            head = null;
        }
        public void add(HuaBoard board, int depth){
            //加在链表头部
            Node node = new Node(board,depth);
            node.next = head;
            head = node;
        }
        public Boolean remove(HuaBoard board){
            Node pointer = head;
            Node prePointer = null;
            while(pointer != null){
                if(isSameBoard(pointer.board,board.savedBoard())){
                    //delete this node
                    if(prePointer != null){
                        prePointer.next = pointer.next;
                        return true;
                    }else{
                        head = pointer.next;
                        return true;
                    }
                }
                prePointer = pointer;
                pointer = pointer.next;
            }
            return false;
        }
        public Boolean find(HuaBoard board, int depth){
            //从列表中找是否有和board相同的链表。
            //还有一个参数depth（深度或者步数），当找到相同的棋盘时，如果链表中的depth更大，而board的depth更小，
            // 为了求得最优解，不应该认为是重复棋盘。（同时还要删除这个depth更大的棋盘。）
            Node pointer = head;
            Node prePointer = null;
            while(pointer != null){
                //if(pointer.depth <= depth && isSameBoard(pointer.board,board)){
                if(isSameBoard(pointer.board,board.savedBoard())){
                    if (pointer.depth <= depth){
                        // board的depth更深，认为是重复棋盘
                        return true;
                    }else{
                        //删除链表中这个更深的board
                        if(prePointer != null){
                            prePointer.next = pointer.next;
                        }else{
                            head = pointer.next;
                        }
                    }
                }
                pointer = pointer.next;
                prePointer = pointer;
            }
            return false;
        }
        public void printBoard(){
            Node pointer = head;
            while(pointer != null){
                //pointer.board.printBoard();
                pointer = pointer.next;
            }
        }
        class Node{
            public SavedBoard board;
            public int depth; //棋盘加入时的深度(步数)
            public Node next;
            public Node(HuaBoard board, int depth){
                this.board = board.savedBoard();
                this.depth = depth;
                this.next = null;
            }
        }
    }

    public List<HuaBoard> copyBoardList(){
        List <HuaBoard> newList = new ArrayList<>();
        for(int i=0; i<boardList.size(); i++){
            HuaBoard newBoard = boardList.get(i).copyBoard();
            newList.add(newBoard);
        }
        return newList;
    }
    public void printSolution(List <HuaBoard> list){
        for(int i=0; i<list.size(); i++){
            if (list.get(i).nextStepPiece != null) {
                String name = list.get(i).nextStepPiece.name;
                String direction = getDirectionName(list.get(i).nextStepDirection);
                System.out.println(String.format("%s : %s", name, direction));
            }
        }
        System.out.println(String.format("解法共%d步：",list.size()));
    }

    private Boolean querySolution(){
        MySocket mySocket = new MySocket();
        if (!mySocket.connect()) {
            Log.v("solution","连接服务器异常");
            return false;
        }
        String sendString = "";
        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("command","query_solution");
            jsonObject.put("board",startBoard.toJsonString());
            sendString = jsonObject.toString();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        if (sendString.equals("") || !mySocket.send(sendString)) {
            Log.v("solution", "发送消息失败");
            return false;
        }
        String reply = mySocket.recieve().toString();
        Log.v("solution",reply);
        if (reply.equals("") || reply.startsWith("failed")){
            return false;
        }
        try {
            //JSONObject jsonObject = new JSONObject(reply);
            //String stepsString = jsonObject.getString("steps");
            solution = Solution.parseFromJsonString(reply);
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            return  false;
        }
    }
    private void save(){
        startBoard.bestSolution = solution;
        DBBoard dbBoard = startBoard.toDBBoard();
        dbBoard.save();
    }
}
