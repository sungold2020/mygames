package com.sungold.huarongdao;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

public class BoxBoard extends Board {
    Piece boy = null;    //男孩
    Piece[] blocks = null; //砖块
    Piece[] boxs = null;   //箱子
    Piece[] destPointers = null; //箱子目标位置

    int[][] flag = null; //迷宫用来记录是否可以走，block不能走，已经走过也不能再走
    int destX,destY;
    BoxBoard(String name, int maxX, int maxY) {
        //BaseBoard()
        super(name, GameType.BOX, maxX, maxY);
        pieces = new Piece[maxX][maxY];  //maxX,maxY不包含边界
    }

    @Override
    public void convertBoard() {
        if (boy != null) {
            pieces[boy.x][boy.y] = boy;
        }
        if (blocks != null) {
            for (int i = 0; i < blocks.length; i++) {
                pieces[blocks[i].x][blocks[i].y] = blocks[i];
            }
        }
        if (boxs != null) {
            for (int i = 0; i < boxs.length; i++) {
                pieces[boxs[i].x][boxs[i].y] = boxs[i];
            }
        }
    }

    @Override
    public Piece getPieceByName(String name) {
        if (boy != null) {
            if (boy.name.equals(name)) {
                return boy;
            }
        }
        if (blocks != null) {
            for (int i = 0; i < blocks.length; i++) {
                if (blocks[i].name.equals(name)) {
                    return blocks[i];
                }
            }
        }
        if (boxs != null) {
            for (int i = 0; i < boxs.length; i++) {
                if (boxs[i].name.equals(name)) {
                    return boxs[i];
                }
            }
        }
        if (destPointers != null) {
            for (int i = 0; i < destPointers.length; i++) {
                if (destPointers[i].name.equals(name)) {
                    return destPointers[i];
                }
            }
        }
        return null;
    }

    @Override
    public Board copyBoard() {
        BoxBoard boxBoard = (BoxBoard) copyBoardWithoutConvert();
        boxBoard.convertBoard();
        return boxBoard;
    }
    public Board copyBoardWithoutConvert(){
        BoxBoard boxBoard = new BoxBoard(name, maxX, maxY);
        if (boy != null) {
            boxBoard.boy = boy.copyPiece();
        }
        if (blocks != null) {
            boxBoard.blocks = new Piece[blocks.length];
            for (int i = 0; i < blocks.length; i++) {
                boxBoard.blocks[i] = blocks[i].copyPiece();
            }
        }
        if (boxs != null) {
            boxBoard.boxs = new Piece[boxs.length];
            for (int i = 0; i < boxs.length; i++) {
                boxBoard.boxs[i] = boxs[i].copyPiece();
            }
        }
        if (destPointers != null) {
            boxBoard.destPointers = new Piece[destPointers.length];
            for (int i = 0; i < destPointers.length; i++) {
                boxBoard.destPointers[i] = destPointers[i].copyPiece();
            }
        }
        return boxBoard;
    }

    @Override
    public Boolean isSuccess() {
        //printBoard();
        for (int i = 0; i < destPointers.length; i++) {
            int x = destPointers[i].x, y = destPointers[i].y;
            if (pieces[x][y] == null || pieces[x][y].type != Piece.PIECE_BOX) {
                return false;
            }
        }
        return true;
    }
    public Boolean canBoxBeMoved(Piece piece,int direction){
        //仅适用于box
        //棋子是否能按照direction方向移动，两个条件：1、该方向的前面一个位置空，2、boy能到棋子反方向位置的一个棋子
        if(piece.type != Piece.PIECE_BOX) { return false; }
        int x = piece.x, y = piece.y;
        switch (direction){
            case Piece.DIRECTION_UP:
                if (isBlocked(x,y+1) || isBlocked(x,y-1)) { return false; } //前后的位置必须为空
                if(!canBoyReach(x,y-1)){ return false; } //反向位置boy要可达
                return true;
            case Piece.DIRECTION_DOWN:
                if (isBlocked(x,y+1) || isBlocked(x,y-1)) { return false; } //前后的位置必须为空
                if(!canBoyReach(x,y+1)){ return false; } //反向位置boy要可达
                return true;
            case Piece.DIRECTION_LEFT:
                if (isBlocked(x-1,y) || isBlocked(x+1,y)) { return false; } //前后的位置必须为空
                if(!canBoyReach(x+1,y)){ return false; } //反向位置boy要可达
                return true;
            case Piece.DIRECTION_RIGHT:
                if (isBlocked(x-1,y) || isBlocked(x+1,y)) { return false; } //前后的位置必须为空
                if(!canBoyReach(x-1,y)){ return false; } //反向位置boy要可达
                return true;
            default:
                Log.v("board","unknown direction");
                return false;
        }
    }
    private Boolean isBlocked(int x,int y){
        //该位置是否被堵塞，如果越界或者位置为BOX/BLOCK返回true
        if(isOutOfBoard(x,y)) { return true; }
        if(pieces[x][y] == null) { return false; }
        if(pieces[x][y].type == Piece.PIECE_BOX || pieces[x][y].type == Piece.PIECE_BLOCK) { return true; }
        return false;
    }
    public Boolean canBoyReach(int x,int y) {
        //boy是否到到达目标位置
        //board转换flag
        flag = new int[maxX][maxY];
        for (int i = 0; i < maxX; i++) {
            for (int j = 0; j < maxY; j++) {
                flag[i][j] = 0;
            }
        }
        for (int i = 0; i < boxs.length; i++) {
            flag[boxs[i].x][boxs[i].y] = 1;
        }
        if (blocks != null) {
            for (int i = 0; i < blocks.length; i++) {
                flag[blocks[i].x][blocks[i].y] = 1;
            }
        }
        destX = x; destY = y;
        return isTherePath(boy.x, boy.y);
    }
    private Boolean isTherePath(int startX,int startY){
        if(startX == destX && startY == destY) { return true; }
        flag[startX][startY] = 1; //这个位置已经走过不用再走了
        //四个方向
        if(startY+1 < maxY && flag[startX][startY+1] == 0) {
            if(isTherePath(startX,startY+1)) { return true; }
        }
        if(startY-1 >= 0 && flag[startX][startY-1] == 0) {
            if(isTherePath(startX,startY-1)) { return true; }
        }
        if(startX-1 >= 0 && flag[startX-1][startY] == 0) {
            if(isTherePath(startX-1,startY)) { return true; }
        }
        if(startX+1 < maxY && flag[startX+1][startY] == 0) {
            if(isTherePath(startX+1,startY)) { return true; }
        }
        return false;
    }
    @Override
    public Board newBoardAfterMove(String name,int direction){
        BoxBoard newBoard ;
        Piece piece = getPieceByName(name);
        if(piece.type == Piece.PIECE_BOX) {
            int index = getIndexOfBox(piece);
            newBoard = newBoardAfterMoveBox(index,direction);
        }else
        {
            newBoard = (BoxBoard) super.newBoardAfterMove(name,direction);
        }
        return newBoard;
    }
    public BoxBoard newBoardAfterMoveBoy(int x,int y){
        if(pieces[x][y] != null && (pieces[x][y].type == Piece.PIECE_BOX || pieces[x][y].type == Piece.PIECE_BLOCK)){
            return null;
        }
        if(canBoyReach(x,y)) {
            BoxBoard newBoard = (BoxBoard) copyBoard();
            newBoard.pieces[boy.x][boy.y] = null;
            newBoard.boy.x = x;
            newBoard.boy.y = y;
            newBoard.pieces[x][y] = newBoard.boy;
            return newBoard;
        }
        return null;
    }
    public BoxBoard newBoardAfterMoveBox(int index,int direction) {
        //index是box在数组中的序号，direction，移动的方向
        //前提是box前后位置都已经判断过可移动

        nextStepPiece = boxs[index];
        nextStepDirection = direction;

        BoxBoard newBoard = (BoxBoard) copyBoardWithoutConvert();

        switch(direction){
            case Piece.DIRECTION_UP:
                newBoard.boxs[index].y += 1;
                break;
            case Piece.DIRECTION_DOWN:
                newBoard.boxs[index].y -= 1;
                break;
            case Piece.DIRECTION_LEFT:
                newBoard.boxs[index].x -= 1;
                break;
            case Piece.DIRECTION_RIGHT:
                newBoard.boxs[index].x += 1;
                break;
            default:
                break;
        }
        //boy置为box原来的位置
        newBoard.boy.x = boxs[index].x;
        newBoard.boy.y = boxs[index].y;

        newBoard.convertBoard();
        return newBoard;
    }
    private int getIndexOfBox(Piece piece){
        //把piece转换为index，for box
        for(int i=0; i<boxs.length; i++){
            if(boxs[i].name.equals(piece.name)) { return i; }
        }
        return -1;
    }
    @Override
    public void printBoard(){

        super.printBoard();
        if(boy != null){
            Log.v("boxboard",String.format("boy:%d,%d",boy.x,boy.y));
        }
        if(boxs != null){
            for(int i=0; i<boxs.length; i++) {
                Log.v("boxboard", String.format("box:%d,%d", boxs[i].x, boxs[i].y));
            }
        }
        if(blocks != null){
            for(int i=0; i<blocks.length; i++) {
                Log.v("boxboard", String.format("blocks:%d,%d", blocks[i].x, blocks[i].y));
            }
        }
        if(destPointers != null){
            for(int i=0; i<destPointers.length; i++) {
                Log.v("boxboard", String.format("destPointers:%d,%d", destPointers[i].x, destPointers[i].y));
            }
        }
    }
    @Override
    public Piece getPiece(int x,int y){
        Piece piece = super.getPiece(x,y);
        if(piece != null){
            return piece;
        }
        if (destPointers != null) {
            for (int i = 0; i < destPointers.length; i++) {
                if(destPointers[i].x == x && destPointers[i].y == y){
                    return destPointers[i];
                }
            }
        }
        return null;
    }
    //新增布局中增加棋子调用
    @Override
    public void addPiece(Piece piece) {
        switch (piece.type) {
            case Piece.PIECE_BOY:
                boy = piece;
                break;
            case Piece.PIECE_BLOCK:
                blocks = pieceArrayAdd(blocks, piece);
                break;
            case Piece.PIECE_BOX:
                boxs = pieceArrayAdd(boxs, piece);
                break;
            case Piece.PIECE_DEST:
                destPointers = pieceArrayAdd(destPointers, piece);
                return;   //目标不在pieces中置位
            default:
                Log.v("box", "unknown piece type");
                return;
        }
        pieces[piece.x][piece.y] = piece;
    }

    //新增布局中移除棋子调用
    @Override
    public void delPiece(Piece piece) {
        switch (piece.type) {
            case Piece.PIECE_BOY:
                boy = null;
                break;
            case Piece.PIECE_BLOCK:
                blocks = pieceArrayDel(blocks, piece);
                break;
            case Piece.PIECE_BOX:
                boxs = pieceArrayDel(boxs, piece);
                break;
            case Piece.PIECE_DEST:
                destPointers = pieceArrayDel(destPointers, piece);
                break;
            default:
                Log.v("box", "unknown piece type");
                return;
        }
        pieces[piece.x][piece.y] = null;
    }
    @Override
    public String checkBoard(){
        if (boy == null){
            return "男孩还未添加";
        }

        if (destPointers == null){
            return "目标还未设定";
        }
        for(int i=0; i<destPointers.length; i++){
           destPointers[i].name += Piece.numberToChinese(i);
        }

        if (boxs == null){
            return "箱子为空";
        }
        for(int i=0; i<boxs.length; i++){
            boxs[i].name += Piece.numberToChinese(i);
        }

        if(boxs.length < destPointers.length){
            return "箱子数目少于目标";
        }
        return "OK";
    }
    public void checkPieces(){
        //debug，检查pieces数组的一致性
        //boy
        if(pieces[boy.x][boy.y] != boy){
            printBoard();
            Thread.dumpStack();
            System.exit(0);
        }
        for(int i=0; i<boxs.length; i++){
            if(pieces[boxs[i].x][boxs[i].y] != boxs[i]){
                printBoard();
                Thread.dumpStack();
                System.exit(0);
            }
        }
    }
    @Override
    public int getHash() {
        if (hash == 0) {
            //变化的是box的位置和人的位置，因此用一个值来记录他们的位置的可能变化即可。
            // 每一个piece(人或者box)可能的位置有，max*maxy种可能。
            // TODO 如果boxs.length太大，会导致hash大于long的最大值。
            hash = (long) ((boy.x * maxY + boy.y) * Math.pow(maxX * maxY, boxs.length));
            for (int i = 0; i < boxs.length; i++) {
                hash += (boxs[i].x * maxY + boxs[i].y) * Math.pow(maxX * maxY, i);
            }
        }
        return (int) (hash % maxHash);
    }

    @Override
    public JSONObject toJson() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", name);
            jsonObject.put("type", gameType);
            jsonObject.put("maxX", maxX);
            jsonObject.put("maxY", maxY);
            //jsonObject.put("destPointer",destPointer);
            jsonObject.put("boy", boy.toJson());

            JSONArray jsonArrayBlocks = new JSONArray();
            if (blocks != null) {
                for (int i = 0; i < blocks.length; i++) {
                    jsonArrayBlocks.put(i, blocks[i].toJson());
                }
            }
            jsonObject.put("blocks", jsonArrayBlocks);

            JSONArray jsonArrayBoxs = new JSONArray();
            if (boxs != null) {
                for (int i = 0; i < boxs.length; i++) {
                    jsonArrayBoxs.put(i, boxs[i].toJson());
                }
            }
            jsonObject.put("boxs", jsonArrayBoxs);

            JSONArray jsonArrayDest = new JSONArray();
            if (boxs != null) {
                for (int i = 0; i < destPointers.length; i++) {
                    jsonArrayBoxs.put(i, destPointers[i].toJson());
                }
            }
            jsonObject.put("dests", jsonArrayDest);
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toDBString(){
        //存储在数据库中的字符串
        String dbString  = String.format("%s,%d,%d|",name,maxX,maxY);
        dbString += boy.toDBString() + "|";
        if (boxs != null){
            for(int i=0; i<boxs.length; i++){
                dbString += boxs[i].toDBString() + "|";
            }
        }
        if (blocks != null){
            for(int i=0; i<blocks.length; i++){
                dbString += blocks[i].toDBString() + "|";
            }
        }
        if (destPointers != null){
            for(int i=0; i<destPointers.length; i++){
                dbString += destPointers[i].toDBString() + "|";
            }
        }
        return dbString;
    }
    public static Board fromDBString(String string){
        if (string.equals("")) { return null; }

        String[] strList = string.split("\\|");
        if (strList.length <= 3) { return  null; }

        String[] infoList = strList[0].split(",");
        if (infoList.length != 3) { return  null; }
        String name = infoList[0];
        int maxX = Integer.valueOf(infoList[1]);
        int maxY = Integer.valueOf(infoList[2]);

        BoxBoard board = new BoxBoard(name,maxX,maxY);
        board.boy = Piece.fromDBString(strList[1]);

        for(int i=2; i<strList.length; i++){
            Piece piece = Piece.fromDBString(strList[i]);
            if (piece == null) { return null; }
            switch (piece.type){
                case Piece.PIECE_BLOCK:
                    board.blocks = pieceArrayAdd(board.blocks,piece);
                    break;
                case Piece.PIECE_BOX:
                    board.boxs = pieceArrayAdd(board.boxs,piece);
                    break;
                case Piece.PIECE_DEST:
                    board.destPointers = pieceArrayAdd(board.destPointers,piece);
                    break;
                default:
                    Log.v("box","error type in fromDBtring");
                    return null;
            }
        }
        board.convertBoard();
        return board;
    }
    /*public static BaseBoard fromDBBoard(DBBoard dbBoard){

        return null;
    }*/
}
