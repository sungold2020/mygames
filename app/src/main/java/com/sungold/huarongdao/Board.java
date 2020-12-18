package com.sungold.huarongdao;

import android.util.Log;

import org.json.JSONObject;

//棋盘，所有棋子的位置,构成了棋盘
public abstract class Board {
    public final static int maxHash = 1024 * 1024 * 10;

    public String name;
    public GameType gameType;
    public int maxX,maxY;
    public long hash = 0;


    public Piece[][] pieces;
    //每走一步，记录下移动的棋子和方向
    public Piece nextStepPiece = null;
    public int nextStepDirection = -1;
    public Solution bestSolution = null;


    //生成一个棋盘，但棋盘中的棋子未设置
    Board(String name, GameType gameType, int maxX, int maxY) {
        this.name = name;
        this.gameType = gameType;
        this.maxX = maxX;
        this.maxY = maxY;

        pieces = new Piece[maxX][maxY];
        for (int x = 0; x < maxX; x++) {
            for (int y = 0; y < maxY; y++) {
                pieces[x][y] = null;
            }
        }
    }

    public abstract void convertBoard() ;

    public Board newBoardAfterMove(String name, int direction){
        Piece piece = getPieceByName(name);
        if (piece == null) {
            System.out.println("error get piece null："+name);
            return null; }
        return newBoardAfterMove(piece,direction);
    }
    public abstract Piece getPieceByName(String name);

    public Board newBoardAfterMove(Piece piece, int direction) {
        //piece被移动的棋子
        //direction，移动的方向
        if(piece == null) { return null; }

        int x = piece.x;
        int y = piece.y;
        int length = piece.length;

        nextStepPiece = piece;
        nextStepDirection = direction;

        //Log.v("board",String.format("待移动piece(%d,%d,%s)到方向%s",x,y,piece.name,Piece.directionToString(direction)));
        Board newBoard = copyBoard();

        //下面根据移动的棋子和方向，重新设置新的空格位置
        piece = newBoard.getPiece(x, y);     //获取新棋盘对应的piece，因为要更新的是新棋盘
        Piece spacePiece,spacePiece2;
        switch (piece.type) {
            case Piece.PIECE_CAOCHAO:
                switch (direction) {
                    case Piece.DIRECTION_UP:
                        if (!isSpace(x,y+2) || !isSpace(x+1,y+2)) { return null; }

                        spacePiece = newBoard.getPiece(x,y+2);       //获取即将被占用的空白棋子
                        spacePiece2 = newBoard.getPiece(x+1,y+2);
                        newBoard.setPiece(spacePiece,x,y);              //把空白棋子位置更新为移动后留下来的空白位置
                        newBoard.setPiece(spacePiece2,x+1,y);
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置

                        break;
                    case Piece.DIRECTION_DOWN:
                        if (!isSpace(x,y-1) || !isSpace(x+1,y-1)) { return null; }

                        spacePiece = newBoard.getPiece(x,y-1);       //获取即将被占用的空白棋子
                        spacePiece2 = newBoard.getPiece(x+1,y-1);
                        newBoard.setPiece(spacePiece,x,y+1);              //把空白棋子位置更新为移动后留下来的空白位置
                        newBoard.setPiece(spacePiece2,x+1,y+1);
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        break;
                    case Piece.DIRECTION_LEFT:
                        if (!isSpace(x-1,y) || !isSpace(x-1,y+1)) { return null; }

                        spacePiece = newBoard.getPiece(x-1,y);       //获取即将被占用的空白棋子
                        spacePiece2 = newBoard.getPiece(x-1,y+1);
                        newBoard.setPiece(spacePiece,x+1,y);              //把空白棋子位置更新为移动后留下来的空白位置
                        newBoard.setPiece(spacePiece2,x+1,y+1);
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        break;
                    case Piece.DIRECTION_RIGHT:
                        if (!isSpace(x+2,y) || !isSpace(x+2,y+1)) { return null; }

                        spacePiece = newBoard.getPiece(x+2,y);       //获取即将被占用的空白棋子
                        spacePiece2 = newBoard.getPiece(x+2,y+1);
                        newBoard.setPiece(spacePiece,x,y);              //把空白棋子位置更新为移动后留下来的空白位置
                        newBoard.setPiece(spacePiece2,x,y+1);
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        break;
                    default:
                        System.out.println("error:unknown direction" + String.valueOf(direction));
                }
                break;
            case Piece.PIECE_VERTICAL:
                switch (direction) {
                    case Piece.DIRECTION_UP:
                        if (!isSpace(x,y+length) ) { return null; }

                        spacePiece = newBoard.getPiece(x,y+length); //将上方的棋子为原来的空格
                        newBoard.setPiece(spacePiece,x,y);
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置

                        break;
                    case Piece.DIRECTION_DOWN:
                        if (!isSpace(x,y-1) ) { return null; }
                        spacePiece = newBoard.getPiece(x,y-1); //将下方的棋子为原来的空格
                        newBoard.setPiece(spacePiece,x,y+length-1);
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        break;
                    case Piece.DIRECTION_LEFT:
                        for(int i=0; i<length; i++){
                            if (!isSpace(x-1,y+i)) { return null; }
                        }
                        for(int i=0; i<length; i++){
                            spacePiece = newBoard.getPiece(x-1,y+i); //获取新棋盘原来的空格
                            newBoard.setPiece(spacePiece,x,y+i);        //置为新棋盘的新空位
                        }
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        break;
                    case Piece.DIRECTION_RIGHT:
                        for(int i=0; i<length; i++){
                            if (!isSpace(x+1,y+i)) { return null; }
                        }
                        for(int i=0; i<length; i++){
                            spacePiece = newBoard.getPiece(x+1,y+i); //获取新棋盘原来的空格
                            newBoard.setPiece(spacePiece,x,y+i);        //置为新棋盘的新空位
                        }
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        break;
                    default:
                        System.out.println("error:unknown direction" + String.valueOf(direction));
                }
                break;
            case Piece.PIECE_HORIZON:
                switch (direction) {
                    case Piece.DIRECTION_UP:
                        for(int i=0; i<length; i++){
                            if (!isSpace(x+i,y+1)) { return null; }
                        }
                        for(int i=0; i<length; i++){
                            spacePiece = newBoard.getPiece(x+i,y+1); //获取新棋盘原来的空格
                            newBoard.setPiece(spacePiece,x+i,y);        //置为新棋盘的新空位
                        }
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        break;
                    case Piece.DIRECTION_DOWN:
                        spacePiece = newBoard.getPiece(x,y-1);       //获取即将被占用的空白棋子
                        for(int i=0; i<length; i++){
                            if (!isSpace(x+i,y-1)) { return null; }
                        }
                        for(int i=0; i<length; i++){
                            spacePiece = newBoard.getPiece(x+i,y-1); //获取新棋盘原来的空格
                            newBoard.setPiece(spacePiece,x+i,y);        //置为新棋盘的新空位
                        }
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        break;
                    case Piece.DIRECTION_LEFT:
                        if (!isSpace(x-1,y)) { return null; }
                        spacePiece = newBoard.getPiece(x-1,y);
                        newBoard.setPiece(spacePiece,x+length-1,y);
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        break;
                    case Piece.DIRECTION_RIGHT:
                        if (!isSpace(x+length,y)) { return null; }
                        spacePiece = newBoard.getPiece(x+length,y);
                        newBoard.setPiece(spacePiece,x,y);
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        break;
                    default:
                        System.out.println("error:unknown direction" + String.valueOf(direction));
                }
                break;
            case Piece.PIECE_SOLDIER:
            case Piece.PIECE_BOX:
                switch(direction){
                    case Piece.DIRECTION_UP:
                        if(!isSpace(x,y+1)) { return null; }
                        spacePiece = newBoard.getPiece(x,y+1); //将下方的棋子为原来的空格
                        newBoard.setPiece(spacePiece,x,y);
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        break;
                    case Piece.DIRECTION_DOWN:
                        if(!isSpace(x,y-1)) { return null; }
                        spacePiece = newBoard.getPiece(x,y-1); //将下方的棋子为原来的空格
                        newBoard.setPiece(spacePiece,x,y);
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        break;
                    case Piece.DIRECTION_LEFT:
                        if(!isSpace(x-1,y)) { return null; }
                        spacePiece = newBoard.getPiece(x-1,y); //将下方的棋子为原来的空格
                        newBoard.setPiece(spacePiece,x,y);
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        break;
                    case Piece.DIRECTION_RIGHT:
                        if(!isSpace(x+1,y)) { return null; }
                        spacePiece = newBoard.getPiece(x+1,y); //将下方的棋子为原来的空格
                        newBoard.setPiece(spacePiece,x,y);
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        break;
                    default:
                        break;
                }
                break;
            case Piece.PIECE_BOY:
                switch(direction){
                    case Piece.DIRECTION_UP:
                        if(isSpace(x,y+1)) {
                            newBoard.setPiece(null, x, y);
                            newBoard.setPiece(piece, x,y+1);
                        }else if(isBox(x,y+1)){
                            //box上面是不是空
                            if(!isSpace(x,y+2)) { return null; }
                            //把box移到上面
                            Piece box = newBoard.getPiece(x,y+1);
                            newBoard.setPiece(box,x,y+2);
                            newBoard.setPiece(piece,x,y+1);
                            newBoard.setPiece(null,x,y);
                        }
                        break;
                    case Piece.DIRECTION_DOWN:
                        if(isSpace(x,y-1)) {
                            newBoard.setPiece(null, x, y);
                            newBoard.setPiece(piece, x,y-1);
                        }else if(isBox(x,y-1)){
                            //box下面是不是空
                            if(!isSpace(x,y-2)) { return null; }
                            //把box移到下面
                            Piece box = newBoard.getPiece(x,y-1);
                            newBoard.setPiece(box,x,y-2);
                            newBoard.setPiece(piece,x,y-1);
                            newBoard.setPiece(null,x,y);
                        }
                        break;
                    case Piece.DIRECTION_LEFT:
                        if(isSpace(x-1,y)) {
                            newBoard.setPiece(null, x, y);
                            newBoard.setPiece(piece, x-1,y); //新棋盘重新设置该piece的新位置
                        }else if(isBox(x-1,y)){
                            //box左边是不是空
                            if(!isSpace(x-2,y)) { return null; }
                            //把box移到上面
                            Piece box = newBoard.getPiece(x-1,y);
                            newBoard.setPiece(box,x-2,y);
                            newBoard.setPiece(piece,x-1,y);
                            newBoard.setPiece(null,x,y);
                        }
                        break;
                    case Piece.DIRECTION_RIGHT:
                        if(isSpace(x+1,y)) {
                            newBoard.setPiece(null, x, y);
                            newBoard.setPiece(piece, x+1,y); //新棋盘重新设置该piece的新位置
                        }else if(isBox(x+1,y)){
                            //box左边是不是空
                            if(!isSpace(x+2,y)) { return null; }
                            //把box移到上面
                            Piece box = newBoard.getPiece(x+1,y);
                            newBoard.setPiece(box,x+2,y);
                            newBoard.setPiece(piece,x+1,y);
                            newBoard.setPiece(null,x,y);
                        }
                        break;
                    default:
                        break;
                }
                break;
            default:
                System.out.println(String.format("error type in newBoardAfterMove:%d", piece.type));
                //System.exit(1);
        }
        return newBoard;
    }
    public void printBoard(){
        Log.v("board","name=:"+name);
        for(int x=0; x<maxX; x++){
            for(int y=0;y<maxY;y++) {
                if (pieces[x][y] != null) {
                    Log.v("board", String.format("(%d,%d):%s", x, y, pieces[x][y].name));
                }
            }
        }
    }

    public void setPiece(Piece piece, int direction) {
        //piece被移动的棋子
        //direction，移动的方向
        //同步更新二维数组pieces中的值(只更新piece占据的坐标对应的棋子）
        int x = piece.x;
        int y = piece.y;
        int length = piece.length;
        switch (piece.type) {
            case Piece.PIECE_CAOCHAO:
                switch (direction) {
                    case Piece.DIRECTION_UP:
                        piece.y += 1;
                        pieces[x][y + 2] = piece;
                        pieces[x + 1][y + 2] = piece;
                        break;
                    case Piece.DIRECTION_DOWN:
                        piece.y -= 1;
                        pieces[x][y - 1] = piece;
                        pieces[x + 1][y - 1] = piece;
                        break;
                    case Piece.DIRECTION_LEFT:
                        piece.x -= 1;
                        pieces[x - 1][y] = piece;
                        pieces[x - 1][y + 1] = piece;
                        break;
                    case Piece.DIRECTION_RIGHT:
                        piece.x += 1;
                        pieces[x + 2][y] = piece;
                        pieces[x + 2][y + 1] = piece;
                        break;
                    default:
                        System.out.println("error:unknown direction" + String.valueOf(direction));
                }
                break;
            case Piece.PIECE_VERTICAL:
                switch (direction) {
                    case Piece.DIRECTION_UP:
                        piece.y += 1;
                        pieces[x][y + length] = piece;
                        break;
                    case Piece.DIRECTION_DOWN:
                        piece.y -= 1;
                        pieces[x][y - 1] = piece;
                        break;
                    case Piece.DIRECTION_LEFT:
                        piece.x -= 1;
                        for(int i=0; i<length; i++){
                            pieces[x-1][y+i] = piece;
                        }
                        break;
                    case Piece.DIRECTION_RIGHT:
                        piece.x += 1;
                        for(int i=0; i<length; i++){
                            pieces[x+1][y+i] = piece;
                        }
                        break;
                    default:
                        System.out.println("error:unknown direction" + String.valueOf(direction));
                }
                break;
            case Piece.PIECE_HORIZON:
                switch (direction) {
                    case Piece.DIRECTION_UP:
                        piece.y += 1;
                        for(int i=0; i<length; i++){
                            pieces[x+i][y+1] = piece;
                        }
                        break;
                    case Piece.DIRECTION_DOWN:
                        piece.y -= 1;
                        for(int i=0; i<length; i++){
                            pieces[x+i][y-1] = piece;
                        }
                        break;
                    case Piece.DIRECTION_LEFT:
                        piece.x -= 1;
                        pieces[x - 1][y] = piece;
                        break;
                    case Piece.DIRECTION_RIGHT:
                        piece.x += 1;
                        pieces[x + length][y] = piece;
                        break;
                    default:
                        System.out.println("error:unknown direction" + String.valueOf(direction));
                }
                break;
            case Piece.PIECE_EMPTY:
            case Piece.PIECE_SOLDIER:
            case Piece.PIECE_BOX:
            case Piece.PIECE_BOY:
                switch (direction) {
                    case Piece.DIRECTION_UP:
                        piece.y += 1;
                        pieces[x][y + 1] = piece;
                        break;
                    case Piece.DIRECTION_DOWN:
                        piece.y -= 1;
                        pieces[x][y - 1] = piece;
                        break;
                    case Piece.DIRECTION_LEFT:
                        piece.x -= 1;
                        pieces[x - 1][y] = piece;
                        break;
                    case Piece.DIRECTION_RIGHT:
                        piece.x += 1;
                        pieces[x + 1][y] = piece;
                        break;
                    default:
                        System.out.println("error:unknown direction" + String.valueOf(direction));
                }
                break;
            default:
                System.out.println(String.format("error type in newBoardAfterMove:%d", piece.type));
                System.exit(1);
        }
    }

    public void setPiece(Piece piece, int x, int y){
        //把piece设置为新位置
        if(piece == null){ //piece为空
            pieces[x][y] = null;
            return ;
        }
        piece.x = x;
        piece.y = y;
        int length = piece.length;
        switch(piece.type){
            case Piece.PIECE_CAOCHAO:
                pieces[x][y] = piece;
                pieces[x+1][y] = piece;
                pieces[x][y+1] = piece;
                pieces[x+1][y+1] = piece;
                break;
            case Piece.PIECE_VERTICAL:
                for(int i=0; i<length; i++){
                    pieces[x][y+i] = piece;
                }
                break;
            case Piece.PIECE_HORIZON:
                for(int i=0; i<length; i++){
                    pieces[x+i][y] = piece;
                }
                break;
            case Piece.PIECE_EMPTY:
            case Piece.PIECE_SOLDIER:
            case Piece.PIECE_BLOCK:
            case Piece.PIECE_BOY:
            case Piece.PIECE_BOX:
            case Piece.PIECE_SUDOKU_BOARD:
                pieces[x][y] = piece;
                break;
            default:
        }
    }

    /*public void printBoard() {
        System.out.println("name:"+name);
        if (pieces != null) {
            for (int y = maxY - 1; y >= 0; y--) {
                String line = "";
                for (int x = 0; x < maxX; x++) {
                    line = line + pieces[x][y].name.subSequence(0, 1).toString();
                }
                System.out.println(line);
            }
        } else {
            System.out.println("");
            System.out.println(String.format("棋盘，hash：%d"));
            for (int y = MAX_Y - 1; y >= 0; y--) {
                String line = "";
                for (int x = 0; x < MAX_X; x++) {
                    line = line + getCharOfLocation(x, y);
                }
                System.out.println(line);
            }
        }
        //debug
        caochao.printPiece();
        if (jiang != null) {
            for (int i = 0; i < jiang.length; i++) {
                jiang[i].printPiece();
            }
        }
        if (shuai != null) {
            for (int i = 0; i < shuai.length; i++) {
                shuai[i].printPiece();
            }
        }
        if (bing != null) {
            for (int i = 0; i < bing.length; i++) {
                bing[i].printPiece();
            }
        }
        if (space != null) {
            for (int i = 0; i < space.length; i++) {
                space[i].printPiece();
            }
        }
    }
    */
    public String getNextStepName() {
        if (nextStepPiece == null) {
            return "";
        }
        return nextStepPiece.name;
    }

    public String getNextStepDirection() {
        switch (nextStepDirection) {
            case Piece.DIRECTION_UP:
                return "UP";
            case Piece.DIRECTION_DOWN:
                return "DOWN";
            case Piece.DIRECTION_LEFT:
                return "LEFT";
            case Piece.DIRECTION_RIGHT:
                return "RIGHT";
            default:
                return "";
        }
    }

    public Piece getPiece(int x, int y) {
        if (isOutOfBoard(x, y)) {
            return null;
        }
        return pieces[x][y];
    }

    public abstract Board copyBoard();

    public SavedBoard savedBoard() {
        if (hash == 0) {
            getHash();
        }
        return new SavedBoard(hash);
    }

    public abstract Boolean isSuccess();

    public Boolean isOutOfBoard(int x, int y) {
        if (x < 0 || x >= maxX || y < 0 || y >= maxY) {
            return true;
        }
        return false;
    }

    public Boolean isOccupiedByOther(Piece selfPiece, int x, int y) {
        Piece occupiedPiece = pieces[x][y];
        if (occupiedPiece == null) {
            return false;
        }
        if (occupiedPiece.type == Piece.PIECE_EMPTY) {
            return false;
        }
        if (occupiedPiece.type == selfPiece.type && occupiedPiece.x == selfPiece.x && occupiedPiece.y == selfPiece.y) {
            //占据的是自己
            return false;
        }
        //被其他piece占据
        return true;
    }
    public Boolean isSpace(int x,int y){
        if(isOutOfBoard(x,y)) { return false; }
        if (pieces[x][y] == null || pieces[x][y].type == Piece.PIECE_EMPTY || pieces[x][y].type == Piece.PIECE_DEST){
            return true;
        }
        return false;
    }
    public Boolean isBox(int x,int y){
        if(pieces[x][y] != null && pieces[x][y].type == Piece.PIECE_BOX){
            return true;
        }
        return false;
    }

    //增加棋子，为棋盘布局所调用
    public abstract void addPiece(Piece piece);
    //删除棋子
    public abstract void delPiece(Piece piece);

    public static Piece[] pieceArrayAdd(Piece[] pieceArray, Piece piece){
        //blockArray数组增添一个成员block，返回新的数组
        Piece[] newArray;
        if (pieceArray == null){
            newArray = new Piece[1];
            newArray[0] = piece;
            return newArray;
        }
        newArray = new Piece[pieceArray.length + 1];
        for (int i = 0; i < pieceArray.length; i++) {
            newArray[i] = pieceArray[i];     //不需要拷贝，直接引用原对象
        }
        newArray[pieceArray.length] = piece.copyPiece();
        return newArray;
    }

    public static Piece[] pieceArrayDel(Piece[] pieceArray, Piece piece){
        //blockArray数组增添一个成员block，返回新的数组
        if (pieceArray == null) {
            Log.v("block", "error:del block, but blockarray is null");
            return null;
        }
        if (pieceArray.length == 1) {
            if (pieceArray[0].type == piece.type && pieceArray[0].x == piece.x && pieceArray[0].y == piece.y) {
                Log.v("block","仅有一条记录，删除后，返回空");
                return null;
            }else{
                Log.v("block","not find block in array");
                return pieceArray;
            }
        }
        int stopIndex = -1;
        Piece[] newArray = new Piece[pieceArray.length - 1];
        for (int i = 0; i < pieceArray.length; i++) {
            if (pieceArray[i].type == piece.type && pieceArray[i].x == piece.x && pieceArray[i].y == piece.y) {
                stopIndex = i;
                break;
            }
            newArray[i] = pieceArray[i];
        }
        if (stopIndex == -1) {
            //没有找到
            Log.v("block", "del block,can't find block");
            return pieceArray;
        }
        for (int i = stopIndex + 1; i < pieceArray.length; i++) {
            newArray[i - 1] = pieceArray[i];
        }
        return newArray;
    }
    public abstract String checkBoard();  //add_board调用，检查board必要组件是否完成,返回OK,否则为其他错误提示信息
    public abstract  int getHash() ;
    public abstract JSONObject toJson();

    //public static abstract BaseBoard parseFromJson(JSONObject jsonObject);

    /*public static BaseBoard fromPiecesString(String string);*/
    //public abstract BaseBoard fromDBBoard(DBBoard dbBoard);
    public abstract String toDBString();
    public  DBBoard toDBBoard(){
        if(bestSolution == null){
            return new DBBoard(name,gameType,toDBString(),0,"");
        }else{
            return new DBBoard(name,gameType,toDBString(),bestSolution.getSteps(),bestSolution.toJson().toString());
        }
    }
    // public static BaseBoard fromDBBoard(DBBoard dbBoard);
}

