package com.sungold.huarongdao;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;


//棋盘，所有棋子的位置,构成了棋盘
public class BlockBoard {

    public final static int SPACE_NOT_CONNECTED = -1;
    public final static int SPACE_VERTICAL = 1;
    public final static int SPACE_HORIZON = 2;

    public final static int HASH_CONST_X = 7;
    public final static int HASH_CONST_Y = 13;

    public  int maxX = 6; //棋盘大小
    public  int maxY = 6;

    public  int destPointer = 6;  //king到达该位置


    public String name; //棋盘名
    //当前所有棋子及位置
    public BlockPiece kingBlock = null; //king单列
    public BlockPiece[] verticalBlocks = null; //存储垂直的木条：竖
    public BlockPiece[] horizonBlocks  = null; //存储水平的木条：横

    //为了提高性能，每次生成新的棋盘，都转换成二维数组
    public BlockPiece[][] blocks = null;

    //每走一步，记录下移动的棋子和方向
    public BlockPiece nextStepBlockPiece = null;
    public int nextStepDirection = -1;

    public long hash = 0;

    public Solution bestSolution = null;

    //生成一个棋盘，但棋盘中的棋子未设置
    BlockBoard(int maxX,int maxY) {
        this.maxX = maxX;
        this.maxY = maxY;
        blocks = new BlockPiece[maxX][maxY];
        for(int x=0; x<maxX; x++){
            for(int y=0; y<maxY; y++){
                blocks[x][y] = null;
            }
        }
    }

    //根据输入的棋子生成一个棋盘
    BlockBoard(String name,int maxX,int maxY,int destPointer,BlockPiece kingBlock,BlockPiece[] verticalBlocks,BlockPiece[] horizonBlocks){
        this.name = name;
        this.maxX = maxX;
        this.maxY = maxY;

        this.destPointer = destPointer;
        this.kingBlock = kingBlock;
        this.verticalBlocks = verticalBlocks;
        this.horizonBlocks = horizonBlocks;
        blocks = new BlockPiece[maxX][maxY];
        for(int x=0; x<maxX; x++){
            for(int y=0; y<maxY; y++){
                blocks[x][y] = null;
            }
        }
        convertBoard();
    }

    public void convertBoard() {
        //blocks转换成二维数组
        for(int i=0; i<kingBlock.length; i++){
            int x = kingBlock.x, y = kingBlock.y;
            if (kingBlock.type == BlockPiece.PIECE_HORIZON){
                blocks[x+i][y] = kingBlock;
            }else{
                blocks[x][y+i] = kingBlock;
            }
        }

        if (verticalBlocks != null){
            for(int i=0; i<verticalBlocks.length; i++){
                for(int j=0; j<verticalBlocks[i].length; j++){
                    int x = verticalBlocks[i].x, y = verticalBlocks[i].y;
                    blocks[x][y+j] = verticalBlocks[i];
                }
            }
        }

        if (horizonBlocks != null){
            for(int i=0; i<horizonBlocks.length; i++){
                for(int j=0; j<horizonBlocks[i].length; j++){
                    int x = horizonBlocks[i].x, y = horizonBlocks[i].y;
                    blocks[x+j][y] = horizonBlocks[i];
                }
            }
        }
    }
    private Boolean isEmpty(int x,int y){
        //根据二维数组（前提：已经执行过convertBoard)确定，(x,y)位置是否为空
        if (isOutOfBoard(x,y)) { return false; }
        if (blocks[x][y] == null) { return true; }
        else { return false; }
    }

    public BlockBoard newBoardAfterMove(String name, int direction){
        BlockPiece block = getBlockPieceByName(name);
        if (block == null) {
            System.out.println("error get block null："+name);
            return null; }
        return newBoardAfterMove(block,direction);
    }
    public BlockPiece getBlockPieceByName(String name){
        if (kingBlock.name.equals(name)){
            return kingBlock;
        }
        if (verticalBlocks != null) {
            for (int i = 0; i < verticalBlocks.length; i++) {
                if(verticalBlocks[i].name.equals(name)) { return verticalBlocks[i]; }
            }
        }
        if (horizonBlocks != null) {
            for (int i = 0; i < horizonBlocks.length; i++) {
                if(horizonBlocks[i].name.equals(name)) { return horizonBlocks[i]; }
            }
        }
        return null;
    }
    public BlockBoard newBoardAfterMove(BlockPiece block, int direction) {
        //block被移动的棋子
        //direction，移动的方向

        int x = block.x;
        int y = block.y;

        nextStepBlockPiece = block;
        nextStepDirection = direction;

        BlockBoard newBoard = copyBoard();
        //下面根据移动的棋子和方向，重新设置新的空格位置
        block = newBoard.getBlockPiece(x, y);     //获取新棋盘对应的block，因为要更新的时新棋盘
        if (block.type == BlockPiece.PIECE_VERTICAL){
            if (direction == BlockPiece.DIRECTION_UP){
                if (!isEmpty(x,y+block.length)) { return null; } //即将占据的位置不为空
                newBoard.blocks[x][y] = null;    //空下来的位置设为null
                newBoard.blocks[x][y+block.length] = block; //占据了新的位置
                block.y += 1;                    //位置往上移一格
            }else if(direction == BlockPiece.DIRECTION_DOWN){
                if (!isEmpty(x,y-1)) { return null; }
                newBoard.blocks[x][y+block.length-1] = null;    //最顶上的一格将为空
                newBoard.blocks[x][y-1] = block; //占据了新的位置
                block.y -= 1;                    //位置往上移一格
            }else{
                Log.v("error","垂直木条移动方向只能是上下");
            }
        }else if(block.type == BlockPiece.PIECE_HORIZON){
            if (direction == BlockPiece.DIRECTION_LEFT){
                if (!isEmpty(x-1,y)) { return null; } //即将占据的位置不为空
                newBoard.blocks[x+block.length-1][y] = null;    //空下来的位置设为null
                newBoard.blocks[x-1][y] = block; //占据了新的位置
                block.x -= 1;                    //位置往左移一格
            }else if(direction == BlockPiece.DIRECTION_RIGHT){
                if (!isEmpty(x+block.length,y)) { return null; }
                newBoard.blocks[x][y] = null;    //最顶上的一格将为空
                newBoard.blocks[x+block.length][y] = block; //占据了新的位置
                block.x += 1;                    //位置右移一格
            }else{
                Log.v("error","水平木条移动方向只能是左右");
            }
        }else{
            Log.v("error","unknown type in newBoardAfterMove");
        }
        return newBoard;
    }

    public String getNextStepName() {
        if (nextStepBlockPiece == null) {
            return "";
        }
        return nextStepBlockPiece.name;
    }

    public String getNextStepDirection() {
        switch (nextStepDirection) {
            case BlockPiece.DIRECTION_UP:
                return "UP";
            case BlockPiece.DIRECTION_DOWN:
                return "DOWN";
            case BlockPiece.DIRECTION_LEFT:
                return "LEFT";
            case BlockPiece.DIRECTION_RIGHT:
                return "RIGHT";
            default:
                return "";
        }
    }


    public BlockPiece getBlockPiece(int x, int y) {
        if (isOutOfBoard(x, y)) {
            return null;
        }
        return blocks[x][y];
    }

    public BlockBoard copyBoard() {
        //复制一个新棋盘（同时转换好二维数组)
        BlockBoard newBoard = new BlockBoard(maxX,maxY);
        newBoard.name = name;
        newBoard.destPointer = destPointer;
        newBoard.kingBlock = kingBlock.copyPiece();
        if (verticalBlocks != null) {
            newBoard.verticalBlocks = new BlockPiece[verticalBlocks.length];
            for (int i = 0; i < verticalBlocks.length; i++) {
                newBoard.verticalBlocks[i] = verticalBlocks[i].copyPiece();
            }
        }
        if (horizonBlocks != null) {
            newBoard.horizonBlocks = new BlockPiece[horizonBlocks.length];
            for (int i = 0; i < horizonBlocks.length; i++) {
                newBoard.horizonBlocks[i] = horizonBlocks[i].copyPiece();
            }
        }
        newBoard.convertBoard();
        return newBoard;
    }

    public SavedBlockBoard savedBoard() {
        //SavedBlockBoard savedBoard;
        //king,verticalBlocks *n,horizonBlocks *n;
        if (hash == 0) {
            hash += kingBlock.x * Math.pow(maxX, verticalBlocks.length + horizonBlocks.length);
            for (int i = 0; i < verticalBlocks.length; i++) {
                hash += verticalBlocks[i].y * Math.pow(maxY, i + horizonBlocks.length);
            }
            for (int i = 0; i < horizonBlocks.length; i++) {
                hash += horizonBlocks[i].x * Math.pow(maxX, i);
            }
        }
        return new SavedBlockBoard(hash);
    }

    public Boolean isSuccess() {
        if (kingBlock.type == BlockPiece.PIECE_HORIZON) {
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

    public Boolean isOutOfBoard(int x, int y) {
        if (x < 0 || x >= maxX || y < 0 || y >= maxY) {
            return true;
        }
        return false;
    }
    public Boolean isOccupiedByOther(BlockPiece selfPiece, int x, int y) {
        //坐标(x,y)是否为其他棋子占据，如果为空，否则占据的是自己，就返回false，否则返回true
        BlockPiece occupiedPiece = blocks[x][y];
        if (occupiedPiece == null) {
            return false;
        }
        if (occupiedPiece.type == selfPiece.type && occupiedPiece.x == selfPiece.x && occupiedPiece.y == selfPiece.y) {
            //占据的是自己
            return false;
        }
        //被其他piece占据
        return true;
    }

    //增加棋子，为棋盘布局所调用
    public void addBlockPiece(BlockPiece block) {
        //增加一个棋子，首先把这个木条加入棋盘，同时还要更新二维数组(因为在布局时需要判断该木条能否放入相应位置）
        int x = block.x;
        int y = block.y;
        if (block.name.equals("王")){
            kingBlock = block;
            for(int i=0; i<block.length; i++){
                if (block.type == BlockPiece.PIECE_VERTICAL){
                    blocks[x][y+i] = kingBlock;
                }else{
                    blocks[x+i][y] = kingBlock;
                }
            }
            return;
        }
        if (block.type == BlockPiece.PIECE_VERTICAL){
            verticalBlocks = blockArrayAdd(verticalBlocks,block); //数组增加一个成员
            for(int i=0; i<block.length; i++){
                blocks[x][y+i] = block;
            }
        }else if(block.type == BlockPiece.PIECE_HORIZON){
            horizonBlocks = blockArrayAdd(horizonBlocks,block); //数组增加一个成员
            for(int i=0; i<block.length; i++){
                blocks[x+i][y] = block;
            }
        }else{
            Log.v("block","增加一个错误的木条");
        }
    }

    //删除棋子
    public void delBlockPiece(BlockPiece block) {
        int x = block.x;
        int y = block.y;

        if (block.type == BlockPiece.PIECE_VERTICAL){
            verticalBlocks = blockArrayDel(verticalBlocks,block);
            for(int i=0; i<block.length; i++){
                blocks[x][y+i] = null;
            }
        }else if(block.type == BlockPiece.PIECE_HORIZON) {
            horizonBlocks = blockArrayDel(horizonBlocks,block);
            for(int i=0; i<block.length; i++){
                blocks[x+i][y] = null;
            }
        }else{
            Log.v("block","unknown block type");
        }
    }

    public static BlockPiece[] blockArrayAdd(BlockPiece[] blockArray,BlockPiece block){
        //blockArray数组增添一个成员block，返回新的数组
        BlockPiece[] newArray;
        if (blockArray == null){
            newArray = new BlockPiece[1];
            newArray[0] = block;
            return newArray;
        }
        newArray = new BlockPiece[blockArray.length + 1];
        for (int i = 0; i < blockArray.length; i++) {
            newArray[i] = blockArray[i];     //不需要拷贝，直接引用原对象
        }
        newArray[blockArray.length] = block.copyPiece();
        return newArray;
    }

    public static BlockPiece[] blockArrayDel(BlockPiece[] blockArray,BlockPiece block){
        //blockArray数组增添一个成员block，返回新的数组
        //TODO
        if (blockArray == null) {
            Log.v("block", "error:del block, but blockarray is null");
            return null;
        }
        if (blockArray.length == 1) {
            if (blockArray[0].type == block.type && blockArray[0].x == block.x && blockArray[0].y == block.y) {
                return null;
            }else{
                Log.v("block","not find block in array");
                return blockArray;
            }
        }
        int stopIndex = -1;
        BlockPiece[] newArray = new BlockPiece[blockArray.length - 1];
        for (int i = 0; i < blockArray.length; i++) {
            if (blockArray[i].type == block.type && blockArray[i].x == block.x && blockArray[i].y == block.y) {
                stopIndex = i;
                break;
            }
            newArray[i] = blockArray[i];
        }
        if (stopIndex == -1) {
            //没有找到
            Log.v("block", "del block,can't find block");
            return blockArray;
        }
        for (int i = stopIndex + 1; i < blockArray.length; i++) {
            newArray[i - 1] = blockArray[i];
        }
       return newArray;
    }
    public int getHash() {
        /*hash = 0;
        hash += kingBlock.type * (kingBlock.x * HASH_CONST_X + kingBlock.y * HASH_CONST_Y);
        if (verticalBlocks != null) {
            for (int i = 0; i < verticalBlocks.length; i++) {
                hash += verticalBlocks[i].type * (verticalBlocks[i].x * HASH_CONST_X + verticalBlocks[i].y * HASH_CONST_Y);
            }
        }
        if (horizonBlocks != null) {
            for (int i = 0; i < horizonBlocks.length; i++) {
                hash += horizonBlocks[i].type * (horizonBlocks[i].x * HASH_CONST_X + horizonBlocks[i].y * HASH_CONST_Y);
            }
        }
        return hash;
        */
        if (hash == 0) {
            hash += kingBlock.x * Math.pow(maxX, verticalBlocks.length + horizonBlocks.length);
            for (int i = 0; i < verticalBlocks.length; i++) {
                hash += verticalBlocks[i].y * Math.pow(maxY, i + horizonBlocks.length);
            }
            for (int i = 0; i < horizonBlocks.length; i++) {
                hash += horizonBlocks[i].x * Math.pow(maxX, i);
            }
        }
        return (int) (hash % getMaxHash());
    }
    public int getMaxHash(){
        /*hash = 0;
        hash += kingBlock.type * (maxX * HASH_CONST_X + maxY * HASH_CONST_Y);
        if (verticalBlocks != null) {
            for (int i = 0; i < verticalBlocks.length; i++) {
                hash += verticalBlocks[i].type * (verticalBlocks[i].x * HASH_CONST_X + maxY * HASH_CONST_Y);
            }
        }
        if (horizonBlocks != null) {
            for (int i = 0; i < horizonBlocks.length; i++) {
                hash += horizonBlocks[i].type * (maxX * HASH_CONST_X + horizonBlocks[i].y * HASH_CONST_Y);
            }
        }
        return hash;*/
        return 1024*1024*10;
    }
    public DBBoard toDBBoard(){
        if(bestSolution == null){
            return new DBBoard(name,GameType.BLOCK,toDBString(),0,"");
        }else{
            return new DBBoard(name,GameType.BLOCK,toDBString(),bestSolution.getSteps(),bestSolution.toJson().toString());
        }
    }

    public String toDBString(){
        //把board转换为存储再数据库中的字符串

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
    public static BlockBoard fromDBString(String boardString){

        if (boardString.equals("")) { return null; }

        String[] strList = boardString.split("\\|");
        if (strList.length <= 2) { return  null; }

        String[] infoList = strList[0].split(",");
        if (infoList.length != 4) { return  null; }
        String name = infoList[0];
        int maxX = Integer.valueOf(infoList[1]);
        int maxY = Integer.valueOf(infoList[2]);
        int destPointer = Integer.valueOf(infoList[3]);

        BlockPiece kingBlock = BlockPiece.fromDBString(strList[1]);

        BlockPiece[] blockPieces = new BlockPiece[strList.length-2];
        BlockPiece[] verticalBlocks = null, horizonBlocks = null;
        for(int i=2; i<strList.length; i++){
            BlockPiece blockPiece = BlockPiece.fromDBString(strList[i]);
            if (blockPiece == null) { return null; }
            if (blockPiece.type == BlockPiece.PIECE_VERTICAL) {
                verticalBlocks = blockArrayAdd(verticalBlocks,blockPiece);
            }else{
                horizonBlocks = blockArrayAdd(horizonBlocks,blockPiece);
            }
        }
        return new BlockBoard(name,maxX,maxY,destPointer,kingBlock,verticalBlocks,horizonBlocks);
    }
    public JSONObject toJson() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", name);
            jsonObject.put("maxX",maxX);
            jsonObject.put("maxY",maxY);
            jsonObject.put("destPointer",destPointer);
            jsonObject.put("kingBlock",kingBlock.toJson());

            JSONArray jsonArrayVertial = new JSONArray();
            if (verticalBlocks != null) {
                for (int i = 0; i < verticalBlocks.length; i++) {
                    jsonArrayVertial.put(i, verticalBlocks[i].toJson());
                }
            }
            jsonObject.put("verticalBlocks",jsonArrayVertial);

            JSONArray jsonArrayHorizon = new JSONArray();
            if (horizonBlocks != null) {
                for (int i = 0; i < horizonBlocks.length; i++) {
                    jsonArrayHorizon.put(i, horizonBlocks[i].toJson());
                }
            }
            jsonObject.put("horizonBlocks",jsonArrayHorizon);
            return jsonObject;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public static BlockBoard parseFromJson(String string) {
        try {
            JSONObject jsonObject = new JSONObject(string);
            String name = jsonObject.getString("name");
            int maxX = jsonObject.getInt("maxX");
            int maxY = jsonObject.getInt("maxY");
            int destPointer = jsonObject.getInt("destPointer");
            BlockPiece kingBlock = BlockPiece.parseFromJson(jsonObject.getJSONObject("kingBlock"));

            BlockPiece[] verticalBlocks = null;
            JSONArray verticalJSONArray = jsonObject.getJSONArray("verticalBlocks");
            if (verticalJSONArray != null){
                verticalBlocks = new BlockPiece[verticalJSONArray.length()];
                for(int i=0; i<verticalJSONArray.length(); i++){
                    verticalBlocks[i] = BlockPiece.parseFromJson((JSONObject)verticalJSONArray.get(i));
                }
            }
            BlockPiece[] horizonBlocks = null;
            JSONArray horizonJSONArray = jsonObject.getJSONArray("horizonBlocks");
            if (horizonJSONArray != null){
                horizonBlocks = new BlockPiece[horizonJSONArray.length()];
                for(int i=0; i<horizonJSONArray.length(); i++){
                    horizonBlocks[i] = BlockPiece.parseFromJson((JSONObject)horizonJSONArray.get(i));
                }
            }
            return new BlockBoard(name, maxX,maxY,destPointer,kingBlock,verticalBlocks,horizonBlocks);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /*public class SavedBlockBoard {
        //用于存储在哈希表中的棋盘，不直接用class Board，是为了节省内存。只要取能够唯一标识棋盘特征的变量
        int king; //king的位置，横条取x，竖条取y
        int[] vertical = null;  //竖条的位置，取y,舍去x，因为x不会变化
        int[] horizon = null;   //横条的位置，取x，舍y，因为y不会变
        SavedBlockBoard(BlockPiece king,BlockPiece[] vertical,BlockPiece[] horizon){

            if (king.type == BlockPiece.PIECE_HORIZON) { this.king = king.x; }
            else { this.king = king.y; }

            if (vertical != null){
                this.vertical = new int[vertical.length];
                for (int i = 0; i < vertical.length; i++) {
                    this.vertical[i] = vertical[i].y;
                }
            }

            if (horizon != null){
                this.horizon = new int[horizon.length];
                for (int i = 0; i < horizon.length; i++) {
                    this.horizon[i] = horizon[i].x;
                }
            }
        }
    }*/
    public class SavedBlockBoard{
        long board;
        SavedBlockBoard(long board){
            this.board = board;
        }
    }
}