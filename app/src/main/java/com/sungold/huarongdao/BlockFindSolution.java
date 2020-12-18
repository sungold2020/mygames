package com.sungold.huarongdao;

import android.os.Handler;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.sungold.huarongdao.BlockBoard.HASH_CONST_X;
import static com.sungold.huarongdao.BlockBoard.HASH_CONST_Y;

public class BlockFindSolution extends Thread{
    //每走一步，棋盘状态发生变化，生产一个新的棋盘，棋盘列表就构成了走棋的步骤

    public int maxX=6,maxY=6;
    public final static int MAX_STEPS = 200;

    public BlockBoard startBlockBoard; //起始棋盘状态
    public Handler handler = null;
    public Solution solution = null;

    public  List<BlockBoard> boardList = new ArrayList<>(); //一个链表存储当前检索过的步骤。

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

    BlockFindSolution(BlockBoard board){
        startBlockBoard = board;
        maxX = startBlockBoard.maxX;
        maxY = startBlockBoard.maxY;
        //初始化hash表，取最大hash值来分配数组
        int maxHash =  board.getMaxHash();
        boardHashTable = new MyList[maxHash];
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
        moveNextStep(startBlockBoard);

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

    public void moveNextStep(BlockBoard board) {
        //搜索最优解，通过移动棋子的方式进行移动
        if (board.isSuccess()) {
            if (boardList.size() <= bestSteps) { //如果解法步数小于之前的解法，储存并打印
                pushBlockBoard(board);
                solution = Solution.fromBlockBoard(boardList);
                bestSteps = solution.getSteps();
                //solution.printSolution();
                popBlockBoard(board);
                if (handler != null) {
                    //Log.v("solution", "发送消息");
                    handler.sendEmptyMessage(0x11); //发现更优解
                    message += String.format("找到更优解：%s 步\n", solution.getSteps());
                }
            }
            return;
        }

        if (boardList.size() >= bestSteps) {
            //已经超出最优步骤，回退
            return;
        }
        pushBlockBoard(board); //把当前棋盘状态存入链表
        BlockBoard nextBlockBoard = null;

        //移动king
        if (board.kingBlock.type == BlockPiece.PIECE_VERTICAL) {
            nextBlockBoard = board.newBoardAfterMove(board.kingBlock, BlockPiece.DIRECTION_UP);
            if (isNewBlockBoard(nextBlockBoard)) {
                moveNextStep(nextBlockBoard);
            }
            nextBlockBoard = board.newBoardAfterMove(board.kingBlock, BlockPiece.DIRECTION_DOWN);
            if (isNewBlockBoard(nextBlockBoard)) {
                moveNextStep(nextBlockBoard);
            }
        } else {
            nextBlockBoard = board.newBoardAfterMove(board.kingBlock, BlockPiece.DIRECTION_LEFT);
            if (isNewBlockBoard(nextBlockBoard)) {
                moveNextStep(nextBlockBoard);
            }
            nextBlockBoard = board.newBoardAfterMove(board.kingBlock, BlockPiece.DIRECTION_RIGHT);
            if (isNewBlockBoard(nextBlockBoard)) {
                moveNextStep(nextBlockBoard);
            }
        }
        if (board.verticalBlocks != null) {
            for (int i = 0; i < board.verticalBlocks.length; i++) {
                //移动将
                nextBlockBoard = board.newBoardAfterMove(board.verticalBlocks[i], BlockPiece.DIRECTION_UP);
                if (isNewBlockBoard(nextBlockBoard)) {
                    moveNextStep(nextBlockBoard);
                }
                nextBlockBoard = board.newBoardAfterMove(board.verticalBlocks[i], BlockPiece.DIRECTION_DOWN);
                if (isNewBlockBoard(nextBlockBoard)) {
                    moveNextStep(nextBlockBoard);
                }
            }
        }
        if (board.horizonBlocks != null) {
            for (int i = 0; i < board.horizonBlocks.length; i++) {
                //移动将
                nextBlockBoard = board.newBoardAfterMove(board.horizonBlocks[i], BlockPiece.DIRECTION_LEFT);
                if (isNewBlockBoard(nextBlockBoard)) {
                    moveNextStep(nextBlockBoard);
                }
                nextBlockBoard = board.newBoardAfterMove(board.horizonBlocks[i], BlockPiece.DIRECTION_RIGHT);
                if (isNewBlockBoard(nextBlockBoard)) {
                    moveNextStep(nextBlockBoard);
                }
            }
        }
        //所有可能的步骤都走完了，就回退
        popBlockBoard(board);
        return;
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

    public void pushBlockBoard(BlockBoard board){
        //debug
        /*if(boardList.size() == 0) {
             System.out.println("startBlockBoard:");
             startBlockBoard.printBlockBoard();
             System.out.println("");
        }else{
            System.out.print("走棋->");
            boardList.get(boardList.size()-1).printStep();
            board.printBlockBoard();
             System.out.println("");
        }*/

        int hash = board.getHash();
        if (boardHashTable[hash] == null ){
            boardHashTable[hash] = new MyList();
        }
        boardHashTable[hash].add(board,boardList.size()); //加入哈希表的对应hash值的链表
        boardList.add(board);//注意：必须在hashtable之后执行，否则bordList.size()不对
    }
    public void popBlockBoard(BlockBoard board){
        boardList.remove(boardList.size()-1);
        /*System.out.println("回退到：");
        currentBlockBoard().printBlockBoard();
        System.out.println("");*/
    }
    public BlockBoard currentBlockBoard(){
        return boardList.get(boardList.size()-1);
    }
    public Boolean isNewBlockBoard(BlockBoard board){
        //非空，且和已有棋盘列表中的棋盘不重复，就返回true
        if (board == null) { return false; }

        int hash = board.getHash();
        if (boardHashTable[hash] == null) { return true ; }
        if (boardHashTable[hash].find(board,boardList.size())){
            //对应hash值的链表中如果找到重复的棋盘，就认为是重复棋盘
            //注意传递boradList.size()，也即depth，目的用于比较重复棋盘的深度，如果已经存在相同状态的棋盘而且深度更浅（步数更少），就不需要检索，也即认为是新棋盘。
            return false;
        }
        return true;
    }
    public Boolean isSameBlockBoard(BlockBoard.SavedBlockBoard board1, BlockBoard.SavedBlockBoard board2) {

        /*if (board1.king != board2.king){
            return false;
        }
        if (board1.vertical != null) {
            for (int i = 0; i < board1.vertical.length; i++) {
                if ( board1.vertical[i] != board2.vertical[i] ){
                    return false;
                }
            }
        }
        if (board1.horizon != null) {
            for (int i = 0; i < board1.horizon.length; i++) {
                if ( board1.horizon[i] != board2.horizon[i] ){
                    return false;
                }
            }
        }
        return true;*/
        if (board1.board== board2.board){
            return true;
        }else{
            return false;
        }
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


    private class MyList{
        //savedBlockBoard对应的链表，用于存储在哈希表
        public Node head;
        public MyList(){
            head = null;
        }
        public void add(BlockBoard board,int depth){
            //加在链表头部
            Node node = new Node(board,depth);
            node.next = head;
            head = node;
        }
        public Boolean remove(BlockBoard board){
            Node pointer = head;
            Node prePointer = null;
            while(pointer != null){
                if(isSameBlockBoard(pointer.board,board.savedBoard())){
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
        public Boolean find(BlockBoard board,int depth){
            //从列表中找是否有和board相同的链表。
            //还有一个参数depth（深度或者步数），当找到相同的棋盘时，如果链表中的depth更大，而board的depth更小，
            // 为了求得最优解，不应该认为是重复棋盘。（同时还要删除这个depth更大的棋盘。）
            Node pointer = head;
            Node prePointer = null;
            while(pointer != null){
                //if(pointer.depth <= depth && isSameBlockBoard(pointer.board,board)){
                if(isSameBlockBoard(pointer.board,board.savedBoard())){
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
        public void printBlockBoard(){
            Node pointer = head;
            while(pointer != null){
                //pointer.board.printBlockBoard();
                pointer = pointer.next;
            }
        }
        class Node{
            public BlockBoard.SavedBlockBoard board;
            public int depth; //棋盘加入时的深度(步数)
            public Node next;
            public Node(BlockBoard board,int depth){
                this.board = board.savedBoard();
                this.depth = depth;
                this.next = null;
            }
        }
    }

    public List<BlockBoard> copyBlockBoardList(){
        List <BlockBoard> newList = new ArrayList<>();
        for(int i=0; i<boardList.size(); i++){
            BlockBoard newBlockBoard = boardList.get(i).copyBoard();
            newList.add(newBlockBoard);
        }
        return newList;
    }
    public void printSolution(List <BlockBoard> list){
        for(int i=0; i<list.size(); i++){
            if (list.get(i).nextStepBlockPiece != null) {
                String name = list.get(i).nextStepBlockPiece.name;
                String direction = getDirectionName(list.get(i).nextStepDirection);
                System.out.println(String.format("%s : %s", name, direction));
            }
        }
        System.out.println(String.format("解法共%d步：",list.size()));
    }

    private Boolean querySolution(){
        solution = startBlockBoard.toDBBoard().query_solution();
        if (solution != null) {
            return true;
        }
        return false;
    }
    private void save(){
        startBlockBoard.bestSolution = solution;
        DBBoard dbBoard = startBlockBoard.toDBBoard();
        dbBoard.save();
    }
}
