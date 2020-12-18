package com.sungold.huarongdao;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

public class BrickBoard extends Board {
    Piece kingBlock;
    Piece[] verticalBlocks;
    Piece[] horizonBlocks;
    int destPointer = -1;
    BrickBoard(String name,int maxX,int maxY,int destPointer){
        super(name,GameType.BLOCK,maxX,maxY);
        this.destPointer = destPointer;
    }
    BrickBoard(String name,int maxX,int maxY,int destPointer,Piece kingBlock,Piece[] verticalBlocks,Piece[] horizonBlocks){
        super(name,GameType.BLOCK,maxX,maxY);
        this.destPointer = destPointer;
        this.kingBlock = kingBlock;
        this.verticalBlocks = verticalBlocks;
        this.horizonBlocks = horizonBlocks;
        convertBoard();
    }
    @Override
    public void convertBoard() {
        if(kingBlock != null) {
            for (int i = 0; i < kingBlock.length; i++) {
                int x = kingBlock.x, y = kingBlock.y;
                if (kingBlock.type == BlockPiece.PIECE_HORIZON) {
                    pieces[x + i][y] = kingBlock;
                }else{
                    pieces[x][y + i] = kingBlock;
                }
            }
        }

        if (verticalBlocks != null){
            for(int i=0; i<verticalBlocks.length; i++){
                for(int j=0; j<verticalBlocks[i].length; j++){
                    int x = verticalBlocks[i].x, y = verticalBlocks[i].y;
                    pieces[x][y+j] = verticalBlocks[i];
                }
            }
        }

        if (horizonBlocks != null){
            for(int i=0; i<horizonBlocks.length; i++){
                for(int j=0; j<horizonBlocks[i].length; j++){
                    int x = horizonBlocks[i].x, y = horizonBlocks[i].y;
                    pieces[x+j][y] = horizonBlocks[i];
                }
            }
        }
    }

    @Override
    public Piece getPieceByName(String name) {
        if(kingBlock != null){
            if(kingBlock.name.equals(name)) { return kingBlock; }
        }
        if(verticalBlocks != null){
            for(int i=0; i<verticalBlocks.length; i++){
                if(verticalBlocks[i].name.equals(name)) { return verticalBlocks[i]; }
            }
        }
        if(horizonBlocks != null){
            for(int i=0; i<horizonBlocks.length; i++) { return horizonBlocks[i]; }
        }
        return null;
    }

    @Override
    public Board copyBoard() {
        BrickBoard newBoard = new BrickBoard(name,maxX,maxY,destPointer);
        newBoard.kingBlock = kingBlock.copyPiece();
        if (verticalBlocks != null) {
            newBoard.verticalBlocks = new Piece[verticalBlocks.length];
            for (int i = 0; i < verticalBlocks.length; i++) {
                newBoard.verticalBlocks[i] = verticalBlocks[i].copyPiece();
            }
        }
        if (horizonBlocks != null) {
            newBoard.horizonBlocks = new Piece[horizonBlocks.length];
            for (int i = 0; i < horizonBlocks.length; i++) {
                newBoard.horizonBlocks[i] = horizonBlocks[i].copyPiece();
            }
        }
        newBoard.convertBoard();
        return newBoard;
    }

    @Override
    public Boolean isSuccess() {
        if (kingBlock.type == Piece.PIECE_HORIZON) {
            if (kingBlock.x == destPointer || kingBlock.x+kingBlock.length == destPointer) {
                return true;
            }
            return false;
        }else{
            if (kingBlock.y == destPointer || kingBlock.y+kingBlock.length == destPointer) {
                return true;
            }
            return false;
        }
    }

    public Boolean isKing(Piece piece){
        if (kingBlock == null){
            return false;
        }
        if(kingBlock.x == piece.x && kingBlock.y == piece.y){
            return true;
        }
        return false;
    }
    public void setKing(Piece piece){
        //把piece变成king
        if(kingBlock != null){
            Log.e("block","已经有king");
            return;
        }
        kingBlock = piece;
        kingBlock.name = "王";

        if(kingBlock.type == Piece.PIECE_VERTICAL){
            destPointer = 0;  //固定向下
           verticalBlocks =  pieceArrayDel(verticalBlocks,piece);
        }else if((kingBlock.type == Piece.PIECE_HORIZON)){
            destPointer = maxX; //固定向右
            horizonBlocks = pieceArrayDel(horizonBlocks,piece);
        }else{
            Log.e("block","piece既不是竖也不是横");
        }
    }
    public void unsetKing(Piece piece){
        if(!isKing(piece)){
            Log.e("block","piece不是king, unsetKing");
            return;
        }
        kingBlock = null;

        if(piece.type == Piece.PIECE_VERTICAL){
            piece.name = "竖";
            verticalBlocks = pieceArrayAdd(verticalBlocks,piece);
        }else if(piece.type == Piece.PIECE_HORIZON){
            piece.name = "横";
            horizonBlocks = pieceArrayAdd(horizonBlocks,piece);
        }else{
            Log.e("block","piece既不是竖也不是横");
        }
    }

    @Override
    public void addPiece(Piece piece) {
        int x = piece.x;
        int y = piece.y;
        if (piece.name.equals("王")){
            kingBlock = piece;
            for(int i=0; i<piece.length; i++){
                if (piece.type == BlockPiece.PIECE_VERTICAL){
                    pieces[x][y+i] = kingBlock;
                }else{
                    pieces[x+i][y] = kingBlock;
                }
            }
            return;
        }
        if (piece.type == Piece.PIECE_VERTICAL){
            verticalBlocks = pieceArrayAdd(verticalBlocks,piece); //数组增加一个成员
            for(int i=0; i<piece.length; i++){
                pieces[x][y+i] = piece;
            }
        }else if(piece.type == Piece.PIECE_HORIZON){
            horizonBlocks = pieceArrayAdd(horizonBlocks,piece); //数组增加一个成员
            for(int i=0; i<piece.length; i++){
                pieces[x+i][y] = piece;
            }
        }else{
            Log.v("block","增加一个错误的木条");
        }
    }

    @Override
    public void delPiece(Piece piece) {
        int x = piece.x;
        int y = piece.y;

        if (piece.type == BlockPiece.PIECE_VERTICAL){
            verticalBlocks = pieceArrayAdd(verticalBlocks,piece);
            for(int i=0; i<piece.length; i++){
                pieces[x][y+i] = null;
            }
        }else if(piece.type == BlockPiece.PIECE_HORIZON) {
            horizonBlocks = pieceArrayAdd(horizonBlocks,piece);
            for(int i=0; i<piece.length; i++){
                pieces[x+i][y] = null;
            }
        }else{
            Log.v("block","unknown block type");
        }
    }

    @Override
    public String checkBoard() {
        if(kingBlock == null) { return "王还未添加"; }
        if(verticalBlocks != null) {
            for (int i = 0; i < verticalBlocks.length; i++) {
                if(verticalBlocks[i].name.equals("竖")) {
                    verticalBlocks[i].name += Piece.numberToChinese(i);
                }else{
                    Log.v("block","竖条名字不为竖");
                    return "竖条名字不为竖";
                }
            }
        }
        if(horizonBlocks != null) {
            for (int i = 0; i < horizonBlocks.length; i++) {
                if(horizonBlocks[i].name.equals("横")) {
                    horizonBlocks[i].name += Piece.numberToChinese(i);
                }else{
                    Log.v("block","横条名字不为横");
                    return "横条名字不为横";
                }
            }
        }
        return "OK";
    }
    @Override
    public void printBoard(){
        super.printBoard();
        if(kingBlock != null){
            Log.v("block",String.format("king:%s (%d,%d)",kingBlock.name,kingBlock.x,kingBlock.y));
        }
        if(verticalBlocks != null){
            for(int i=0; i<verticalBlocks.length; i++){
                Log.v("block",String.format("竖:%s (%d,%d)",verticalBlocks[i].name,verticalBlocks[i].x,verticalBlocks[i].y));
            }
        }
        if(horizonBlocks != null){
            for(int i=0; i<horizonBlocks.length; i++){
                Log.v("block",String.format("横:%s (%d,%d)",horizonBlocks[i].name,horizonBlocks[i].x,horizonBlocks[i].y));
            }
        }
    }

    @Override
    public int getHash() {
        if (hash == 0) {
            hash += kingBlock.x * Math.pow(maxX, verticalBlocks.length + horizonBlocks.length);
            for (int i = 0; i < verticalBlocks.length; i++) {
                hash += verticalBlocks[i].y * Math.pow(maxY, i + horizonBlocks.length);
            }
            for (int i = 0; i < horizonBlocks.length; i++) {
                hash += horizonBlocks[i].x * Math.pow(maxX, i);
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
            jsonObject.put("destPointer",destPointer);
            //jsonObject.put("destPointer",destPointer);
            jsonObject.put("king", kingBlock.toJson());

            JSONArray jsonArrayBlocks = new JSONArray();
            if (verticalBlocks != null) {
                for (int i = 0; i < verticalBlocks.length; i++) {
                    jsonArrayBlocks.put(i, verticalBlocks[i].toJson());
                }
            }
            jsonObject.put("verticals", jsonArrayBlocks);

            JSONArray jsonArrayBoxs = new JSONArray();
            if (horizonBlocks != null) {
                for (int i = 0; i < horizonBlocks.length; i++) {
                    jsonArrayBoxs.put(i, horizonBlocks[i].toJson());
                }
            }
            jsonObject.put("boxs", jsonArrayBoxs);

            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toDBString() {
        String dbString  = String.format("%s,%d,%d,%d|",name,maxX,maxY,destPointer);
        dbString += kingBlock.toDBString() + "|";
        if (verticalBlocks != null){
            for(int i=0; i<verticalBlocks.length; i++){
                dbString += verticalBlocks[i].toDBString() + "|";
            }
        }
        if (horizonBlocks != null){
            for(int i=0; i<horizonBlocks.length; i++){
                dbString += horizonBlocks[i].toDBString() + "|";
            }
        }
        return dbString;
    }
    public static Board fromDBString(String string){
        if (string.equals("")) { return null; }

        String[] strList = string.split("\\|");
        if (strList.length <= 1) { return  null; }

        String[] infoList = strList[0].split(",");
        if (infoList.length != 4) { return  null; }
        String name = infoList[0];
        int maxX = Integer.valueOf(infoList[1]);
        int maxY = Integer.valueOf(infoList[2]);
        int destPointer = Integer.valueOf(infoList[3]);

        BrickBoard board = new BrickBoard(name,maxX,maxY,destPointer);
        board.kingBlock = Piece.fromDBString(strList[1]);

        for(int i=2; i<strList.length; i++){
            Piece piece = Piece.fromDBString(strList[i]);
            if (piece == null) { return null; }
            switch (piece.type){
                case Piece.PIECE_HORIZON:
                    board.horizonBlocks = pieceArrayAdd(board.horizonBlocks,piece);
                    break;
                case Piece.PIECE_VERTICAL:
                    board.verticalBlocks = pieceArrayAdd(board.verticalBlocks,piece);
                    break;
                default:
                    Log.v("box","error type in fromDBtring");
                    return null;
            }
        }
        board.convertBoard();
        return board;
    }
}
