package com.sungold.huarongdao;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

//棋盘，所有棋子的位置,构成了棋盘
public class HuaBoard {
    public final static int MAX_X = 4;
    public final static int MAX_Y = 5;

    public final static int DEST_X = 1;
    public final static int DEST_Y = 0;

    public final static int SPACE_NOT_CONNECTED = -1;
    public final static int SPACE_VERTICAL = 1;
    public final static int SPACE_HORIZON = 2;

    public final static int HASH_CONST_X = 7;
    public final static int HASH_CONST_Y = 13;

    public String name;
    //当前所有棋子及位置
    public HuaPiece caochao = null; //曹操
    public HuaPiece[] jiang = null; //将，竖形
    public HuaPiece[] shuai = null; //帅，横形
    public HuaPiece[] bing = null;  //兵(卒)
    public HuaPiece[] space = null; //空格，空格固定为2个

    /* 一个二维数组存储，每一次坐标(x,y)存储的是占据该位置的棋子。
    也即另外一种形式表达棋盘状态，需要调用convertBoard()才能转换出
    */
    public HuaPiece[][] pieces = null;

    //每走一步，记录下移动的棋子和方向
    public HuaPiece nextStepPiece = null;
    public int nextStepDirection = -1;

    public long hash = 0;
    public int maxHash = 1024 * 1024 * 10;

    public Solution bestSolution = null;

    //生成一个棋盘，但棋盘中的棋子未设置
    HuaBoard() {
        pieces = new HuaPiece[MAX_X][MAX_Y];
        for (int x = 0; x < MAX_X; x++) {
            for (int y = 0; y < MAX_Y; y++) {
                pieces[x][y] = null;
            }
        }
    }

    //根据输入的棋子生成一个棋盘
    HuaBoard(String name, HuaPiece[] pieces) {
        this.name = name;

        int numberOfJiang = 0;
        int numberOfShuai = 0;
        int numberOfBing = 0;
        int numberOfSpace = 0;
        //统计各种棋子的数量
        for (int i = 0; i < pieces.length; i++) {
            switch (pieces[i].type) {
                case HuaPiece.PIECE_CAOCHAO:
                    break;
                case HuaPiece.PIECE_VERTICAL:
                    numberOfJiang++;
                    break;
                case HuaPiece.PIECE_HORIZON:
                    numberOfShuai++;
                    break;
                case HuaPiece.PIECE_SOLDIER:
                    numberOfBing++;
                    break;
                case HuaPiece.PIECE_EMPTY:
                    break;
                default:
                    System.out.println("unknown type");
            }
        }
        //根据统计的数量，初始化数组大小
        jiang = new HuaPiece[numberOfJiang];
        shuai = new HuaPiece[numberOfShuai];
        bing = new HuaPiece[numberOfBing];
        numberOfSpace = MAX_X*MAX_Y - 1 - numberOfJiang - numberOfShuai - numberOfBing;
        space = new HuaPiece[numberOfSpace];

        //赋值
        int currentIndexOfJiang = 0;
        int currentIndexOfShuai = 0;
        int currentIndexOfBing = 0;
        int currentIndexOfSpace = 0;
        for (int i = 0; i < pieces.length; i++) {
            switch (pieces[i].type) {
                case HuaPiece.PIECE_CAOCHAO:
                    caochao = pieces[i].copyPiece();
                    break;
                case HuaPiece.PIECE_VERTICAL:
                    jiang[currentIndexOfJiang] = pieces[i].copyPiece();
                    currentIndexOfJiang++;
                    break;
                case HuaPiece.PIECE_HORIZON:
                    shuai[currentIndexOfShuai] = pieces[i].copyPiece();
                    currentIndexOfShuai++;
                    break;
                case HuaPiece.PIECE_SOLDIER:
                    bing[currentIndexOfBing] = pieces[i].copyPiece();
                    currentIndexOfBing++;
                    break;
                case HuaPiece.PIECE_EMPTY:
                    space[currentIndexOfSpace] = pieces[i].copyPiece();
                    currentIndexOfSpace++;
                    break;
                default:
                    System.out.println("unknown type");
            }
        }
        convertBoard();
    }

    public void convertBoard() {
        //从pieces转换二维数组表达的棋盘
        pieces = new HuaPiece[MAX_X][MAX_Y];
        pieces[caochao.x][caochao.y] = caochao;
        pieces[caochao.x + 1][caochao.y] = caochao;
        pieces[caochao.x][caochao.y + 1] = caochao;
        pieces[caochao.x + 1][caochao.y + 1] = caochao;

        if (jiang != null) {
            for (int i = 0; i < jiang.length; i++) {
                pieces[jiang[i].x][jiang[i].y] = jiang[i];
                pieces[jiang[i].x][jiang[i].y + 1] = jiang[i];
            }
        }

        if (shuai != null) {
            for (int i = 0; i < shuai.length; i++) {
                pieces[shuai[i].x][shuai[i].y] = shuai[i];
                pieces[shuai[i].x + 1][shuai[i].y] = shuai[i];
            }
        }

        if (bing != null) {
            for (int i = 0; i < bing.length; i++) {
                pieces[bing[i].x][bing[i].y] = bing[i];
            }
        }
        if (space != null) {
            for (int i = 0; i < space.length; i++) {
                pieces[space[i].x][space[i].y] = space[i];
            }
        }
    }

    public HuaBoard newBoardAfterMove(String name, int direction){
        HuaPiece piece = getPieceByName(name);
        if (piece == null) {
            System.out.println("error get piece null："+name);
            return null; }
        return newBoardAfterMove(piece,direction);
    }
    public HuaPiece getPieceByName(String name){
        if (caochao.name.equals(name)){
            return caochao;
        }
        if (jiang != null) {
            for (int i = 0; i < jiang.length; i++) {
                if(jiang[i].name.equals(name)) { return jiang[i]; }
            }
        }
        if (shuai != null) {
            for (int i = 0; i < shuai.length; i++) {
                if(shuai[i].name.equals(name)) { return shuai[i]; }
            }
        }
        if (bing != null) {
            for (int i = 0; i < bing.length; i++) {
                if(bing[i].name.equals(name)) { return bing[i]; }
            }
        }
        if (space != null) {
            for (int i = 0; i < space.length; i++) {
                if(space[i].name.equals(name)) { return space[i]; }
            }
        }
        return null;
    }
    public HuaBoard newBoardAfterMove(HuaPiece piece, int direction) {
        //piece被移动的棋子
        //direction，移动的方向

        int x = piece.x;
        int y = piece.y;

        nextStepPiece = piece;
        nextStepDirection = direction;

        HuaBoard newBoard = copyBoard();
        //下面根据移动的棋子和方向，重新设置新的空格位置
        HuaPiece spacePiece  = null;
        HuaPiece spacePiece2 = null;
        piece = newBoard.getPiece(x, y);     //获取新棋盘对应的piece，因为要更新的时新棋盘
        switch (piece.type) {
            case HuaPiece.PIECE_CAOCHAO:
                switch (direction) {
                    case HuaPiece.DIRECTION_UP:
                        spacePiece = newBoard.getPiece(x,y+2);       //获取即将被占用的空白棋子
                        spacePiece2 = newBoard.getPiece(x+1,y+2);
                        if (spacePiece == null || spacePiece.type != HuaPiece.PIECE_EMPTY
                            || spacePiece2 == null || spacePiece2.type != HuaPiece.PIECE_EMPTY){
                            return null;
                        }
                        newBoard.setPiece(spacePiece,x,y);              //把空白棋子位置更新为移动后留下来的空白位置
                        newBoard.setPiece(spacePiece2,x+1,y);
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        /*newBoard.setSpace(0, x, y);     //曹操的原来下排位置为新的空格
                        newBoard.setSpace(1, x + 1, y);*/
                        break;
                    case HuaPiece.DIRECTION_DOWN:
                        spacePiece = newBoard.getPiece(x,y-1);       //获取即将被占用的空白棋子
                        spacePiece2 = newBoard.getPiece(x+1,y-1);
                        if (spacePiece == null || spacePiece.type != HuaPiece.PIECE_EMPTY
                                || spacePiece2 == null || spacePiece2.type != HuaPiece.PIECE_EMPTY){
                            return null;
                        }
                        newBoard.setPiece(spacePiece,x,y+1);              //把空白棋子位置更新为移动后留下来的空白位置
                        newBoard.setPiece(spacePiece2,x+1,y+1);
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        break;
                    case HuaPiece.DIRECTION_LEFT:
                        spacePiece = newBoard.getPiece(x-1,y);       //获取即将被占用的空白棋子
                        spacePiece2 = newBoard.getPiece(x-1,y+1);
                        if (spacePiece == null || spacePiece.type != HuaPiece.PIECE_EMPTY
                                || spacePiece2 == null || spacePiece2.type != HuaPiece.PIECE_EMPTY){
                            return null;
                        }
                        newBoard.setPiece(spacePiece,x+1,y);              //把空白棋子位置更新为移动后留下来的空白位置
                        newBoard.setPiece(spacePiece2,x+1,y+1);
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        break;
                    case HuaPiece.DIRECTION_RIGHT:
                        spacePiece = newBoard.getPiece(x+2,y);       //获取即将被占用的空白棋子
                        spacePiece2 = newBoard.getPiece(x+2,y+1);
                        if (spacePiece == null || spacePiece.type != HuaPiece.PIECE_EMPTY
                                || spacePiece2 == null || spacePiece2.type != HuaPiece.PIECE_EMPTY){
                            return null;
                        }
                        newBoard.setPiece(spacePiece,x,y);              //把空白棋子位置更新为移动后留下来的空白位置
                        newBoard.setPiece(spacePiece2,x,y+1);
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        break;
                    default:
                        System.out.println("error:unknown direction" + String.valueOf(direction));
                }
                break;
            case HuaPiece.PIECE_VERTICAL:
                switch (direction) {
                    case HuaPiece.DIRECTION_UP:
                        spacePiece = newBoard.getPiece(x,y+2); //将上方的棋子为原来的空格
                        if(spacePiece == null || spacePiece.type != HuaPiece.PIECE_EMPTY){
                            return null;
                        }
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        newBoard.setPiece(spacePiece,x,y);
                        break;
                    case HuaPiece.DIRECTION_DOWN:
                        spacePiece = newBoard.getPiece(x,y-1); //将下方的棋子为原来的空格
                        if(spacePiece == null || spacePiece.type != HuaPiece.PIECE_EMPTY){
                            return null;
                        }
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        newBoard.setPiece(spacePiece,x,y+1);
                        break;
                    case HuaPiece.DIRECTION_LEFT:
                        spacePiece = newBoard.getPiece(x-1,y);       //获取即将被占用的空白棋子
                        spacePiece2 = newBoard.getPiece(x-1,y+1);
                        if (spacePiece == null || spacePiece.type != HuaPiece.PIECE_EMPTY
                                || spacePiece2 == null || spacePiece2.type != HuaPiece.PIECE_EMPTY){
                            return null;
                        }
                        newBoard.setPiece(spacePiece,x,y);              //把空白棋子位置更新为移动后留下来的空白位置
                        newBoard.setPiece(spacePiece2,x,y+1);
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        break;
                    case HuaPiece.DIRECTION_RIGHT:
                        spacePiece = newBoard.getPiece(x+1,y);       //获取即将被占用的空白棋子
                        spacePiece2 = newBoard.getPiece(x+1,y+1);
                        if (spacePiece == null || spacePiece.type != HuaPiece.PIECE_EMPTY
                                || spacePiece2 == null || spacePiece2.type != HuaPiece.PIECE_EMPTY){
                            return null;
                        }
                        newBoard.setPiece(spacePiece,x,y);              //把空白棋子位置更新为移动后留下来的空白位置
                        newBoard.setPiece(spacePiece2,x,y+1);
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        break;
                    default:
                        System.out.println("error:unknown direction" + String.valueOf(direction));
                }
                break;
            case HuaPiece.PIECE_HORIZON:
                switch (direction) {
                    case HuaPiece.DIRECTION_UP:
                        spacePiece = newBoard.getPiece(x,y+1);       //获取即将被占用的空白棋子
                        spacePiece2 = newBoard.getPiece(x+1,y+1);
                        if (spacePiece == null || spacePiece.type != HuaPiece.PIECE_EMPTY
                                || spacePiece2 == null || spacePiece2.type != HuaPiece.PIECE_EMPTY){
                            return null;
                        }
                        newBoard.setPiece(spacePiece,x,y);              //把空白棋子位置更新为移动后留下来的空白位置
                        newBoard.setPiece(spacePiece2,x+1,y);
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        break;
                    case HuaPiece.DIRECTION_DOWN:
                        spacePiece = newBoard.getPiece(x,y-1);       //获取即将被占用的空白棋子
                        spacePiece2 = newBoard.getPiece(x+1,y-1);
                        if (spacePiece == null || spacePiece.type != HuaPiece.PIECE_EMPTY
                                || spacePiece2 == null || spacePiece2.type != HuaPiece.PIECE_EMPTY){
                            return null;
                        }
                        newBoard.setPiece(spacePiece,x,y);              //把空白棋子位置更新为移动后留下来的空白位置
                        newBoard.setPiece(spacePiece2,x+1,y);
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        break;
                    case HuaPiece.DIRECTION_LEFT:
                        spacePiece = newBoard.getPiece(x-1,y); //将下方的棋子为原来的空格
                        if(spacePiece == null || spacePiece.type != HuaPiece.PIECE_EMPTY){
                            return null;
                        }
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        newBoard.setPiece(spacePiece,x+1,y);
                        break;
                    case HuaPiece.DIRECTION_RIGHT:
                        spacePiece = newBoard.getPiece(x+2,y); //将下方的棋子为原来的空格
                        if(spacePiece == null || spacePiece.type != HuaPiece.PIECE_EMPTY){
                            return null;
                        }
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        newBoard.setPiece(spacePiece,x,y);
                        break;
                    default:
                        System.out.println("error:unknown direction" + String.valueOf(direction));
                }
                break;
            case HuaPiece.PIECE_SOLDIER:
                switch(direction){
                    case HuaPiece.DIRECTION_UP:
                        spacePiece = newBoard.getPiece(x,y+1); //将下方的棋子为原来的空格
                        if(spacePiece == null || spacePiece.type != HuaPiece.PIECE_EMPTY){
                            return null;
                        }
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        newBoard.setPiece(spacePiece,x,y);
                        break;
                    case HuaPiece.DIRECTION_DOWN:
                        spacePiece = newBoard.getPiece(x,y-1); //将下方的棋子为原来的空格
                        if(spacePiece == null || spacePiece.type != HuaPiece.PIECE_EMPTY){
                            return null;
                        }
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        newBoard.setPiece(spacePiece,x,y);
                        break;
                    case HuaPiece.DIRECTION_LEFT:
                        spacePiece = newBoard.getPiece(x-1,y); //将下方的棋子为原来的空格
                        if(spacePiece == null || spacePiece.type != HuaPiece.PIECE_EMPTY){
                            return null;
                        }
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        newBoard.setPiece(spacePiece,x,y);
                        break;
                    case HuaPiece.DIRECTION_RIGHT:
                        spacePiece = newBoard.getPiece(x+1,y); //将下方的棋子为原来的空格
                        if(spacePiece == null || spacePiece.type != HuaPiece.PIECE_EMPTY){
                            return null;
                        }
                        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
                        newBoard.setPiece(spacePiece,x,y);
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

    public HuaBoard newBoardAfterMove(HuaPiece piece, int direction, int indexOfSpace) {
        //piece被移动的棋子
        //direction，移动的方向
        //indexOfPiece，被占据的空格序号（它将变为重新生成的空格位置)

        int x = piece.x;
        int y = piece.y;

        nextStepPiece = piece;
        nextStepDirection = direction;

        HuaBoard newBoard = copyBoard();
        piece = newBoard.getPiece(x, y);     //获取新棋盘对应的piece
        newBoard.setPiece(piece, direction); //新棋盘重新设置该piece的新位置
        //下面根据移动的棋子和方向，重新设置新的空格位置
        switch (piece.type) {
            case HuaPiece.PIECE_CAOCHAO:
                switch (direction) {
                    case HuaPiece.DIRECTION_UP:
                        newBoard.setSpace(0, x, y);     //曹操的原来下排位置为新的空格
                        newBoard.setSpace(1, x + 1, y);
                        break;
                    case HuaPiece.DIRECTION_DOWN:
                        newBoard.setSpace(0, x, y + 1);
                        newBoard.setSpace(1, x + 1, y + 1);
                        break;
                    case HuaPiece.DIRECTION_LEFT:
                        newBoard.setSpace(0, x + 1, y);
                        newBoard.setSpace(1, x + 1, y + 1);
                        break;
                    case HuaPiece.DIRECTION_RIGHT:
                        newBoard.setSpace(0, x, y);
                        newBoard.setSpace(1, x, y + 1);
                        break;
                    default:
                        System.out.println("error:unknown direction" + String.valueOf(direction));
                }
                break;
            case HuaPiece.PIECE_VERTICAL:
                switch (direction) {
                    case HuaPiece.DIRECTION_UP:
                        newBoard.setSpace(indexOfSpace, x, y);
                        break;
                    case HuaPiece.DIRECTION_DOWN:
                        newBoard.setSpace(indexOfSpace, x, y + 1);
                        break;
                    case HuaPiece.DIRECTION_LEFT:
                        newBoard.setSpace(0, x, y);
                        newBoard.setSpace(1, x, y + 1);
                        break;
                    case HuaPiece.DIRECTION_RIGHT:
                        newBoard.setSpace(0, x, y);
                        newBoard.setSpace(1, x, y + 1);
                        break;
                    default:
                        System.out.println("error:unknown direction" + String.valueOf(direction));
                }
                break;
            case HuaPiece.PIECE_HORIZON:
                switch (direction) {
                    case HuaPiece.DIRECTION_UP:
                        newBoard.setSpace(0, x, y);
                        newBoard.setSpace(1, x + 1, y);
                        break;
                    case HuaPiece.DIRECTION_DOWN:
                        newBoard.setSpace(0, x, y);
                        newBoard.setSpace(1, x + 1, y);
                        break;
                    case HuaPiece.DIRECTION_LEFT:
                        newBoard.setSpace(indexOfSpace, x + 1, y);
                        break;
                    case HuaPiece.DIRECTION_RIGHT:
                        newBoard.setSpace(indexOfSpace, x, y);
                        break;
                    default:
                        System.out.println("error:unknown direction" + String.valueOf(direction));
                }
                break;
            case HuaPiece.PIECE_SOLDIER:
                newBoard.setSpace(indexOfSpace, x, y);
                break;
            default:
                System.out.println(String.format("error type in newBoardAfterMove:%d", piece.type));
                System.exit(1);
        }
        return newBoard;
    }

    public HuaBoard newBoardAfterMove(HuaPiece movingPiece, HuaPiece destPiece) {
        if (destPiece.type != HuaPiece.PIECE_EMPTY) {
            return null;
        }
        nextStepPiece = movingPiece;
        HuaBoard newBoard = copyBoard();
        HuaPiece newMovingPiece = newBoard.getPiece(movingPiece.x, movingPiece.y);
        int newIndexOfSpace = newBoard.getIndexOfSpace(destPiece.x, destPiece.y);
        int spaceConnected = isSpaceConnected();
        switch (movingPiece.type) {
            case HuaPiece.PIECE_CAOCHAO:
                switch (spaceConnected) {
                    case SPACE_VERTICAL:
                        if (movingPiece.y == Math.min(space[0].y, space[1].y)) {
                            if (movingPiece.x == space[0].x - 2) {
                                //曹操在空格的左边
                                nextStepDirection = HuaPiece.DIRECTION_RIGHT;
                                newBoard.setPiece(newBoard.caochao, HuaPiece.DIRECTION_RIGHT);
                                newBoard.setSpace(0, movingPiece.x, movingPiece.y);
                                newBoard.setSpace(1, movingPiece.x, movingPiece.y + 1);
                            } else if (movingPiece.x == space[0].x + 1) {
                                //曹操在空格的右边
                                nextStepDirection = HuaPiece.DIRECTION_LEFT;
                                newBoard.setSpace(0, movingPiece.x + 1, movingPiece.y);
                                newBoard.setSpace(1, movingPiece.x + 1, movingPiece.y + 1);
                                newBoard.setPiece(newBoard.caochao, HuaPiece.DIRECTION_LEFT);
                            } else {
                                return null;
                            }
                        } else {
                            return null;
                        }
                        break;
                    case SPACE_HORIZON:
                        if (movingPiece.x == Math.min(space[0].x, space[1].x)) {
                            if (movingPiece.y == space[0].y + 1) {
                                //曹操在空格的上方
                                nextStepDirection = HuaPiece.DIRECTION_DOWN;
                                newBoard.setPiece(newBoard.caochao, HuaPiece.DIRECTION_DOWN);
                                newBoard.setSpace(0, movingPiece.x, movingPiece.y + 1);
                                newBoard.setSpace(1, movingPiece.x + 1, movingPiece.y + 1);
                            } else if (movingPiece.y == space[0].y - 2) {
                                //曹操在空格的下方
                                nextStepDirection = HuaPiece.DIRECTION_UP;
                                newBoard.setSpace(0, movingPiece.x, movingPiece.y);
                                newBoard.setSpace(1, movingPiece.x + 1, movingPiece.y);
                                newBoard.setPiece(newBoard.caochao, HuaPiece.DIRECTION_UP);
                            } else {
                                return null;
                            }
                        } else {
                            return null;
                        }
                        break;
                    case SPACE_NOT_CONNECTED:
                        return null;
                    default:
                        Log.v("error", "unknown SPACE_CONNECTED");
                }
                return newBoard;
            case HuaPiece.PIECE_HORIZON:
                if (destPiece.y == movingPiece.y) { //空格和帅在一个水平线上，帅只能左右移动
                    if (movingPiece.x == destPiece.x + 1) {
                        //帅在空格的右边
                        nextStepDirection = HuaPiece.DIRECTION_LEFT;
                        newBoard.setSpace(newIndexOfSpace, movingPiece.x + 1, movingPiece.y);
                        newBoard.setPiece(newMovingPiece, HuaPiece.DIRECTION_LEFT);
                    } else if (movingPiece.x == destPiece.x - 2) {
                        //帅在空格的左边
                        nextStepDirection = HuaPiece.DIRECTION_RIGHT;
                        newBoard.setSpace(newIndexOfSpace, movingPiece.x, movingPiece.y);
                        newBoard.setPiece(newMovingPiece, HuaPiece.DIRECTION_RIGHT);
                    } else {
                        return null;
                    }
                } else { //空格和帅不在一个水平线上，帅只能上下移动
                    if (spaceConnected != SPACE_HORIZON) {//两个空格美誉水平相连
                        return null;
                    }
                    if (movingPiece.x != Math.min(space[0].x, space[1].x)) { //空格起点和帅七点位置不一样
                        return null;
                    }
                    if (movingPiece.y == space[0].y + 1) {
                        //帅在空格的上方
                        nextStepDirection = HuaPiece.DIRECTION_DOWN;
                        newBoard.setPiece(newMovingPiece, HuaPiece.DIRECTION_DOWN);
                        newBoard.setSpace(0, movingPiece.x, movingPiece.y);
                        newBoard.setSpace(1, movingPiece.x + 1, movingPiece.y);
                    } else if (movingPiece.y == space[0].y - 1) {
                        //帅在空格的下方
                        nextStepDirection = HuaPiece.DIRECTION_UP;
                        newBoard.setPiece(newMovingPiece, HuaPiece.DIRECTION_UP);
                        newBoard.setSpace(0, movingPiece.x, movingPiece.y);
                        newBoard.setSpace(1, movingPiece.x + 1, movingPiece.y);
                    } else {
                        return null;
                    }
                }
                return newBoard;
            case HuaPiece.PIECE_VERTICAL:
                if (movingPiece.x == destPiece.x) { //将和空格在同一垂直线上，将只能上下移动
                    if (movingPiece.y == destPiece.y + 1) {
                        //将在空格的上方
                        nextStepDirection = HuaPiece.DIRECTION_DOWN;
                        newBoard.setPiece(newMovingPiece, HuaPiece.DIRECTION_DOWN);
                        newBoard.setSpace(newIndexOfSpace, movingPiece.x, movingPiece.y + 1);
                    } else if (movingPiece.y == destPiece.y - 2) {
                        //将在空格的下方
                        nextStepDirection = HuaPiece.DIRECTION_UP;
                        newBoard.setSpace(newIndexOfSpace, movingPiece.x, movingPiece.y);
                        newBoard.setPiece(newMovingPiece, HuaPiece.DIRECTION_UP);
                    } else {
                        return null;
                    }
                } else {//将和空格不在同一垂直线上，只能左右移动
                    if (spaceConnected != SPACE_VERTICAL) {
                        return null;
                    }
                    if (movingPiece.y != Math.min(space[0].y, space[1].y)) {
                        return null;
                    }
                    if (movingPiece.x == destPiece.x + 1) {
                        //将在空格的右边
                        nextStepDirection = HuaPiece.DIRECTION_LEFT;
                        newBoard.setSpace(0, movingPiece.x, movingPiece.y);
                        newBoard.setSpace(1, movingPiece.x, movingPiece.y + 1);
                        newBoard.setPiece(newMovingPiece, HuaPiece.DIRECTION_LEFT);
                    } else if (movingPiece.x == destPiece.x - 1) {
                        //将在空格的左边
                        nextStepDirection = HuaPiece.DIRECTION_RIGHT;
                        newBoard.setSpace(0, movingPiece.x, movingPiece.y);
                        newBoard.setSpace(1, movingPiece.x, movingPiece.y + 1);
                        newBoard.setPiece(newMovingPiece, HuaPiece.DIRECTION_RIGHT);
                    } else {
                        return null;
                    }
                }
                return newBoard;
            case HuaPiece.PIECE_SOLDIER:
                if (movingPiece.x == destPiece.x && movingPiece.y == destPiece.y + 1) {
                    //兵在空格的上方
                    nextStepDirection = HuaPiece.DIRECTION_DOWN;
                    newBoard.setSpace(newIndexOfSpace, movingPiece.x, movingPiece.y);
                    newBoard.setPiece(newMovingPiece, HuaPiece.DIRECTION_DOWN);
                } else if (movingPiece.x == destPiece.x && movingPiece.y == destPiece.y - 1) {
                    //兵在空格的下方
                    nextStepDirection = HuaPiece.DIRECTION_UP;
                    newBoard.setSpace(newIndexOfSpace, movingPiece.x, movingPiece.y);
                    newBoard.setPiece(newMovingPiece, HuaPiece.DIRECTION_UP);
                } else if (movingPiece.y == destPiece.y && movingPiece.x == destPiece.x + 1) {
                    //兵在空格的右边
                    nextStepDirection = HuaPiece.DIRECTION_LEFT;
                    newBoard.setSpace(newIndexOfSpace, movingPiece.x, movingPiece.y);
                    newBoard.setPiece(newMovingPiece, HuaPiece.DIRECTION_LEFT);
                } else if (movingPiece.y == destPiece.y && movingPiece.x == destPiece.x - 1) {
                    //兵在空格的左边
                    nextStepDirection = HuaPiece.DIRECTION_RIGHT;
                    newBoard.setSpace(newIndexOfSpace, movingPiece.x, movingPiece.y);
                    newBoard.setPiece(newMovingPiece, HuaPiece.DIRECTION_RIGHT);
                } else {
                    return null;
                }
                return newBoard;
            default:
                return null;
        }
        //return null;
    }

    public int isSpaceConnected() {
        if (space[0].x == space[1].x && (Math.abs(space[0].y - space[1].y) == 1)) {
            return SPACE_VERTICAL;
        } else if (space[0].y == space[1].y && (Math.abs(space[0].x - space[1].x) == 1)) {
            return SPACE_HORIZON;
        } else {
            return SPACE_NOT_CONNECTED;
        }
    }

    public void setPiece(HuaPiece piece, int direction) {
        //piece被移动的棋子
        //direction，移动的方向
        //同步更新二维数组pieces中的值(只更新piece占据的坐标对应的棋子）
        int x = piece.x;
        int y = piece.y;
        switch (piece.type) {
            case HuaPiece.PIECE_CAOCHAO:
                switch (direction) {
                    case HuaPiece.DIRECTION_UP:
                        piece.y += 1;
                        pieces[x][y + 2] = piece;
                        pieces[x + 1][y + 2] = piece;
                        break;
                    case HuaPiece.DIRECTION_DOWN:
                        piece.y -= 1;
                        pieces[x][y - 1] = piece;
                        pieces[x + 1][y - 1] = piece;
                        break;
                    case HuaPiece.DIRECTION_LEFT:
                        piece.x -= 1;
                        pieces[x - 1][y] = piece;
                        pieces[x - 1][y + 1] = piece;
                        break;
                    case HuaPiece.DIRECTION_RIGHT:
                        piece.x += 1;
                        pieces[x + 2][y] = piece;
                        pieces[x + 2][y + 1] = piece;
                        break;
                    default:
                        System.out.println("error:unknown direction" + String.valueOf(direction));
                }
                break;
            case HuaPiece.PIECE_VERTICAL:
                switch (direction) {
                    case HuaPiece.DIRECTION_UP:
                        piece.y += 1;
                        pieces[x][y + 2] = piece;
                        break;
                    case HuaPiece.DIRECTION_DOWN:
                        piece.y -= 1;
                        pieces[x][y - 1] = piece;
                        break;
                    case HuaPiece.DIRECTION_LEFT:
                        piece.x -= 1;
                        pieces[x - 1][y] = piece;
                        pieces[x - 1][y + 1] = piece;
                        break;
                    case HuaPiece.DIRECTION_RIGHT:
                        piece.x += 1;
                        pieces[x + 1][y] = piece;
                        pieces[x + 1][y + 1] = piece;
                        break;
                    default:
                        System.out.println("error:unknown direction" + String.valueOf(direction));
                }
                break;
            case HuaPiece.PIECE_HORIZON:
                switch (direction) {
                    case HuaPiece.DIRECTION_UP:
                        piece.y += 1;
                        pieces[x][y + 1] = piece;
                        pieces[x + 1][y + 1] = piece;
                        break;
                    case HuaPiece.DIRECTION_DOWN:
                        piece.y -= 1;
                        pieces[x][y - 1] = piece;
                        pieces[x + 1][y - 1] = piece;
                        break;
                    case HuaPiece.DIRECTION_LEFT:
                        piece.x -= 1;
                        pieces[x - 1][y] = piece;
                        break;
                    case HuaPiece.DIRECTION_RIGHT:
                        piece.x += 1;
                        pieces[x + 2][y] = piece;
                        break;
                    default:
                        System.out.println("error:unknown direction" + String.valueOf(direction));
                }
                break;
            case HuaPiece.PIECE_SOLDIER:
                switch (direction) {
                    case HuaPiece.DIRECTION_UP:
                        piece.y += 1;
                        pieces[x][y + 1] = piece;
                        break;
                    case HuaPiece.DIRECTION_DOWN:
                        piece.y -= 1;
                        pieces[x][y - 1] = piece;
                        break;
                    case HuaPiece.DIRECTION_LEFT:
                        piece.x -= 1;
                        pieces[x - 1][y] = piece;
                        break;
                    case HuaPiece.DIRECTION_RIGHT:
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

    public void setPiece(HuaPiece piece, int x, int y){
        //把piece设置为新位置
        piece.x = x;
        piece.y = y;
        switch(piece.type){
            case HuaPiece.PIECE_CAOCHAO:
                pieces[x][y] = piece;
                pieces[x+1][y] = piece;
                pieces[x][y+1] = piece;
                pieces[x+1][y+1] = piece;
                break;
            case HuaPiece.PIECE_VERTICAL:
               pieces[x][y] = piece;
               pieces[x][y+1] = piece;
               break;
           case HuaPiece.PIECE_HORIZON:
               pieces[x][y] = piece;
               pieces[x+1][y] = piece;
               break;
            case HuaPiece.PIECE_EMPTY:
            case HuaPiece.PIECE_SOLDIER:
                pieces[x][y] = piece;
                break;
            default:
        }
    }
    public void setSpace(int indexOfSpace, int x, int y) {
        //更新空格的新位置(包括同步二维数组)
        //indexOfSpace，表示待更新的空格序号
        space[indexOfSpace].x = x;
        space[indexOfSpace].y = y;
        pieces[x][y] = space[indexOfSpace];
    }

    public int getIndexOfSpace(int x, int y) {
        for (int i=0; i <space.length; i++){
            if  (space[i].x == x && space[i].y == y) {
                return i;
            }
        }
        return -1;
    }

    public void printBoard() {
        System.out.println("name:"+name);
        if (pieces != null) {
            for (int y = MAX_Y - 1; y >= 0; y--) {
                String line = "";
                for (int x = 0; x < MAX_X; x++) {
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

    public String getNextStepName() {
        if (nextStepPiece == null) {
            return "";
        }
        return nextStepPiece.name;
    }

    public String getNextStepDirection() {
        switch (nextStepDirection) {
            case HuaPiece.DIRECTION_UP:
                return "UP";
            case HuaPiece.DIRECTION_DOWN:
                return "DOWN";
            case HuaPiece.DIRECTION_LEFT:
                return "LEFT";
            case HuaPiece.DIRECTION_RIGHT:
                return "RIGHT";
            default:
                return "";
        }
    }

    public String getCharOfLocation(int x, int y) {
        if (caochao.isOccupied(x, y)) {
            return (caochao.name.subSequence(0, 1)).toString();
        }
        for (int i = 0; i < jiang.length; i++) {
            if (jiang[i].isOccupied(x, y)) {
                return (jiang[i].name.subSequence(0, 1)).toString();
            }
        }
        for (int i = 0; i < shuai.length; i++) {
            if (shuai[i].isOccupied(x, y)) {
                return (shuai[i].name.subSequence(0, 1)).toString();
            }
        }
        for (int i = 0; i < bing.length; i++) {
            if (bing[i].isOccupied(x, y)) {
                return (bing[i].name.subSequence(0, 1)).toString();
            }
        }
        return "空";
    }

    public HuaPiece getPiece(int x, int y) {
        if (isOutOfBoard(x, y)) {
            return null;
        }
        return pieces[x][y];
    }

    public HuaBoard copyBoard() {
        //复制一个新棋盘（同时转换好二维数组)
        HuaBoard newBoard = new HuaBoard();
        newBoard.name = name;
        newBoard.caochao = caochao.copyPiece();
        if (jiang != null) {
            newBoard.jiang = new HuaPiece[jiang.length];
            for (int i = 0; i < jiang.length; i++) {
                newBoard.jiang[i] = jiang[i].copyPiece();
            }
        }
        if (shuai != null) {
            newBoard.shuai = new HuaPiece[shuai.length];
            for (int i = 0; i < shuai.length; i++) {
                newBoard.shuai[i] = shuai[i].copyPiece();
            }
        }
        if (bing != null) {
            newBoard.bing = new HuaPiece[bing.length];
            for (int i = 0; i < bing.length; i++) {
                newBoard.bing[i] = bing[i].copyPiece();
            }
        }
        if (space != null) {
            newBoard.space = new HuaPiece[space.length];
            for (int i = 0; i < space.length; i++) {
                newBoard.space[i] = space[i].copyPiece();
            }
        }
        newBoard.convertBoard();
        return newBoard;
    }

    public SavedBoard savedBoard() {
        /*SavedBoard savedBoard = new SavedBoard();
        for (int x = 0; x < MAX_X; x++) {
            for (int y = 0; y < MAX_Y; y++) {
                savedBoard.pieces[x][y] = (short) pieces[x][y].type;
            }
        }*/
        if (hash == 0){
            for(int x=0; x<MAX_X; x++){
                for(int y=0;y<MAX_Y; y++){
                    int index = x * MAX_Y + y;
                    hash += pieces[x][y].type * Math.pow(5,index); //棋子有5种可能曹,将,帅,兵,空
                }
            }
        }
        return new SavedBoard(hash);
    }

    public Boolean isSuccess() {
        if (caochao != null && caochao.x == DEST_X && caochao.y == DEST_Y) {
            return true;
        }
        return false;
    }

    public Boolean isOutOfBoard(int x, int y) {
        if (x < 0 || x >= MAX_X || y < 0 || y >= MAX_Y) {
            return true;
        }
        return false;
    }

    public Boolean isOccupiedByOther(HuaPiece selfPiece, int x, int y) {
        HuaPiece occupiedPiece = pieces[x][y];
        if (occupiedPiece == null) {
            return false;
        }
        if (occupiedPiece.type == HuaPiece.PIECE_EMPTY) {
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
    public void addPiece(HuaPiece piece) {
        int x = piece.x;
        int y = piece.y;
        switch (piece.type) {
            case HuaPiece.PIECE_CAOCHAO:
                caochao = piece.copyPiece();
                pieces[x][y] = caochao;
                pieces[x][y + 1] = caochao;
                pieces[x + 1][y] = caochao;
                pieces[x + 1][y + 1] = caochao;
                break;
            case HuaPiece.PIECE_VERTICAL:
                if (jiang == null) {
                    jiang = new HuaPiece[1];
                    jiang[0] = piece.copyPiece();
                } else {
                    HuaPiece[] newJiang = new HuaPiece[jiang.length + 1];
                    for (int i = 0; i < jiang.length; i++) {
                        newJiang[i] = jiang[i];
                    }
                    newJiang[jiang.length] = piece.copyPiece();
                    jiang = newJiang;
                }
                pieces[x][y] = jiang[jiang.length - 1];
                pieces[x][y + 1] = jiang[jiang.length - 1];
                break;
            case HuaPiece.PIECE_HORIZON:
                if (shuai == null) {
                    shuai = new HuaPiece[1];
                    shuai[0] = piece.copyPiece();
                } else {
                    HuaPiece[] newShuai = new HuaPiece[shuai.length + 1];
                    for (int i = 0; i < shuai.length; i++) {
                        newShuai[i] = shuai[i];
                    }
                    newShuai[shuai.length] = piece.copyPiece();
                    shuai = newShuai;
                }
                pieces[x][y] = shuai[shuai.length - 1];
                pieces[x + 1][y] = shuai[shuai.length - 1];
                break;
            case HuaPiece.PIECE_SOLDIER:
                if (bing == null) {
                    bing = new HuaPiece[1];
                    bing[0] = piece.copyPiece();
                } else {
                    HuaPiece[] newBing = new HuaPiece[bing.length + 1];
                    for (int i = 0; i < bing.length; i++) {
                        newBing[i] = bing[i];
                    }
                    newBing[bing.length] = piece.copyPiece();
                    bing = newBing;
                }
                pieces[x][y] = bing[bing.length - 1];
                break;
            case HuaPiece.PIECE_EMPTY:
                if (space == null) {
                    space = new HuaPiece[1];
                    space[0] = piece.copyPiece();
                } else {
                    HuaPiece[] newSpace = new HuaPiece[space.length + 1];
                    for (int i = 0; i < space.length; i++) {
                        newSpace[i] = space[i];
                    }
                    newSpace[space.length] = piece.copyPiece();
                    space = newSpace;
                }
                pieces[x][y] = space[space.length - 1];
                break;
            default:
                Log.v("board", "unknown type");
        }
    }

    //删除棋子
    public void delPiece(HuaPiece piece) {
        int x = piece.x;
        int y = piece.y;
        int stopIndex = -1;
        switch (piece.type) {
            case HuaPiece.PIECE_CAOCHAO:
                caochao = null;
                pieces[x][y] = null;
                pieces[x][y + 1] = null;
                pieces[x + 1][y] = null;
                pieces[x + 1][y + 1] = null;
                break;
            case HuaPiece.PIECE_VERTICAL:
                if (jiang == null) {
                    Log.v("board", "error:del jiang, but jiang is null");
                    return;
                } else if (jiang.length == 1) {
                    jiang = null;
                } else {
                    HuaPiece[] newJiang = new HuaPiece[jiang.length - 1];
                    for (int i = 0; i < jiang.length; i++) {
                        if (jiang[i].x == x && jiang[i].y == y) {
                            stopIndex = i;
                            break;
                        }
                        newJiang[i] = jiang[i];
                    }
                    if (stopIndex == -1) {
                        //没有找到
                        Log.v("board", "del piece,can't find piece");
                        return;
                    }
                    for (int i = stopIndex + 1; i < jiang.length; i++) {
                        newJiang[i - 1] = jiang[i];
                    }
                    jiang = newJiang;
                }
                pieces[x][y] = null;
                pieces[x][y + 1] = null;
                break;
            case HuaPiece.PIECE_HORIZON:
                if (shuai == null) {
                    Log.v("board", "error:del shuai,but  it is null");
                    return;
                } else if (shuai.length == 1) {
                    shuai = null;
                } else {
                    HuaPiece[] newShuai = new HuaPiece[shuai.length - 1];
                    for (int i = 0; i < shuai.length; i++) {
                        if (shuai[i].x == x && shuai[i].y == y) {
                            stopIndex = i;
                            break;
                        }
                        newShuai[i] = shuai[i];
                    }
                    if (stopIndex == -1) {
                        //没有找到
                        Log.v("board", "del piece,can't find piece");
                        return;
                    }
                    for (int i = stopIndex + 1; i < shuai.length; i++) {
                        newShuai[i - 1] = shuai[i];
                    }
                    shuai = newShuai;
                }
                pieces[x][y] = null;
                pieces[x + 1][y] = null;
                break;
            case HuaPiece.PIECE_SOLDIER:
                if (bing == null) {
                    Log.v("board", "error:del bing,but  it is null");
                    return;
                } else if (bing.length == 1) {
                    bing = null;
                } else {
                    HuaPiece[] newBing = new HuaPiece[bing.length - 1];
                    for (int i = 0; i < bing.length; i++) {
                        if (bing[i].x == x && bing[i].y == y) {
                            stopIndex = i;
                            break;
                        }
                        newBing[i] = bing[i];
                    }
                    if (stopIndex == -1) {
                        //没有找到
                        Log.v("board", "del piece,can't find piece");
                        return;
                    }
                    for (int i = stopIndex + 1; i < bing.length; i++) {
                        newBing[i - 1] = bing[i];
                    }
                    bing = newBing;
                }
                pieces[x][y] = null;
                break;
            default:
                Log.v("board", "del piece,unknown type");
        }
    }


    public int getHash() {
        /*hash += caochao.type * (caochao.x * HASH_CONST_X + caochao.y * HASH_CONST_Y);
        if (jiang != null) {
            for (int i = 0; i < jiang.length; i++) {
                hash += jiang[i].type * (jiang[i].x * HASH_CONST_X + jiang[i].y * HASH_CONST_Y);
            }
        }
        if (shuai != null) {
            for (int i = 0; i < shuai.length; i++) {
                hash += shuai[i].type * (shuai[i].x * HASH_CONST_X + shuai[i].y * HASH_CONST_Y);
            }
        }
        for (int i = 0; i < bing.length; i++) {
            hash += bing[i].type * (bing[i].x * HASH_CONST_X + bing[i].y * HASH_CONST_Y);
        }
        for (int i = 0; i < space.length; i++) {
            hash += space[i].type * (space[i].x * HASH_CONST_X + space[i].y * HASH_CONST_Y);
        }*/
        if (hash == 0){
            for(int x=0; x<MAX_X; x++){
                for(int y=0;y<MAX_Y; y++){
                    int index = x * MAX_Y + y;
                    hash += pieces[x][y].type * Math.pow(5,index); //棋子有5种可能曹,将,帅,兵,空
                }
            }
        }
        return (int) (hash % maxHash) ;
    }

    /*public int getMaxHash(){
        return 1024*1024*100;
    }*/
    public HuaBoard moveSpace(int indexOfSpace, int direction) {
        //indexOfSpace表示即将被移动的空格
        //direction表示空格即将被移动的方向（和棋子移动方向刚好相反）
        //例如，空格往上移动，那么就需要上方的棋子往下移动

        int x = space[indexOfSpace].x;
        int y = space[indexOfSpace].y;
        HuaBoard newBoard = null;
        HuaPiece piece = null;
        switch (direction) {
            case HuaPiece.DIRECTION_UP:
                //空格上移，则将或者兵需要从上方往下移动
                piece = getPiece(x, y + 1);
                if (piece == null) {
                    return null;
                }
                if (piece.type != HuaPiece.PIECE_SOLDIER && piece.type != HuaPiece.PIECE_VERTICAL) {
                    return null;
                }
                if (piece.x != x || piece.y != y + 1) {
                    return null;
                } //兵或者将的起始位置必须为(x,y+1)
                //移动步骤
                nextStepDirection = HuaPiece.DIRECTION_DOWN;
                nextStepPiece = piece;
                //生成新的board
                newBoard = newBoardAfterMove(piece, HuaPiece.DIRECTION_DOWN, indexOfSpace);
                return newBoard;
            case HuaPiece.DIRECTION_DOWN:
                //空格下移，则将或者兵需要从下方往上移动
                piece = getPiece(x, y - 1);
                if (piece == null) {
                    return null;
                }
                if (piece.type == HuaPiece.PIECE_SOLDIER) {
                    ;//if (piece.x != x || piece.y != y-1) { return null;}  //兵不需要判断
                } else if (piece.type == HuaPiece.PIECE_VERTICAL) {
                    if (piece.x != x || piece.y != y - 2) {
                        return null;
                    } //将的位置必须为(x,y-2);
                } else {
                    return null;
                }
                //移动步骤
                nextStepDirection = HuaPiece.DIRECTION_UP;
                nextStepPiece = piece;
                //生成新的board
                newBoard = newBoardAfterMove(piece, HuaPiece.DIRECTION_UP, indexOfSpace);
                return newBoard;
            case HuaPiece.DIRECTION_LEFT:
                //空格左移，则帅或者兵需要从左往右移动
                piece = getPiece(space[indexOfSpace].x - 1, space[indexOfSpace].y);
                if (piece == null) {
                    return null;
                }
                if (piece.type == HuaPiece.PIECE_SOLDIER) {
                    ;
                } else if (piece.type == HuaPiece.PIECE_HORIZON) {
                    if (piece.x != x - 2 || piece.y != y) {
                        return null;
                    } //帅的起始位置必须(x-2,y);
                } else {
                    return null;
                }
                //移动步骤
                nextStepDirection = HuaPiece.DIRECTION_RIGHT;
                nextStepPiece = piece;
                //生成新的board
                newBoard = newBoardAfterMove(piece, HuaPiece.DIRECTION_RIGHT, indexOfSpace);
                return newBoard;
            case HuaPiece.DIRECTION_RIGHT:
                //空格右移，则帅或者兵需要从右往左移动
                piece = getPiece(space[indexOfSpace].x + 1, space[indexOfSpace].y);
                if (piece == null) {
                    return null;
                }
                if (piece.type != HuaPiece.PIECE_SOLDIER && piece.type != HuaPiece.PIECE_HORIZON) {
                    return null;
                }
                if (piece.x != x + 1 || piece.y != y) {
                    return null;
                } //帅的起始位置必须为(x+1,y)
                //移动步骤
                nextStepDirection = HuaPiece.DIRECTION_LEFT;
                nextStepPiece = piece;
                //生成新的board
                newBoard = newBoardAfterMove(piece, HuaPiece.DIRECTION_LEFT, indexOfSpace);

                return newBoard;
            default:
                System.out.println("error:unknown direction" + String.valueOf(direction));
                return null;
        }
    }

    //两空格一起移动
    public HuaBoard moveTwoSpace(int direction) {
        //direction表示空格即将被移动的方向（和棋子移动方向刚好相反）
        //前提条件：已经判断过两个空格在同一横线/竖线，但未判断是否相邻
        int index;
        HuaBoard newBoard = null;
        HuaPiece piece = null;
        switch (direction) {
            case HuaPiece.DIRECTION_UP://空格上移，则上面的帅或者曹操往下移
                //先判断是否相邻，并确定基准点(左边的是哪一个空格)
                if (space[0].x == space[1].x + 1) {
                    //space[0]在space[1]的右边，取space[1]为基准点
                    index = 1;
                } else if (space[0].x + 1 == space[1].x) {
                    index = 0;
                } else {
                    return null; //不相邻
                }
                //找出基准点上方的棋子
                piece = getPiece(space[index].x, space[index].y + 1);
                if (piece == null) {
                    return null;
                }
                if (piece.type != HuaPiece.PIECE_CAOCHAO && piece.type != HuaPiece.PIECE_HORIZON) {
                    return null;
                }
                if (piece.x != space[index].x || piece.y != space[index].y + 1) {
                    return null;
                } //帅或者曹操的起始位置必须wei(x,y+1);
                nextStepDirection = HuaPiece.DIRECTION_DOWN;
                nextStepPiece = piece;
                newBoard = newBoardAfterMove(piece, HuaPiece.DIRECTION_DOWN, -1);
                return newBoard;
            case HuaPiece.DIRECTION_DOWN://空格下移，则下面的帅或者曹操往上移
                //先判断是否相邻，并确定基准点(左边的是哪一个空格)
                if (space[0].x == space[1].x + 1) {
                    //space[0]在space[1]的右边，取space[1]为基准点
                    index = 1;
                } else if (space[0].x + 1 == space[1].x) {
                    index = 0;
                } else {
                    return null; //不相邻
                }
                //找出基准点下方的棋子
                piece = getPiece(space[index].x, space[index].y - 1);
                if (piece == null) {
                    return null;
                }
                if (piece.type == HuaPiece.PIECE_CAOCHAO) {
                    if (piece.x != space[index].x || piece.y != space[index].y - 2) {
                        return null;
                    } //曹操的起始位置必须为(x,y-2)
                } else if (piece.type == HuaPiece.PIECE_HORIZON) {
                    if (piece.x != space[index].x || piece.y != space[index].y - 1) {
                        return null;
                    } //帅的起始位置必须为(x,y-1)
                } else {
                    return null;
                }
                nextStepDirection = HuaPiece.DIRECTION_UP;
                nextStepPiece = piece;
                newBoard = newBoardAfterMove(piece, HuaPiece.DIRECTION_UP, -1);
                return newBoard;
            case HuaPiece.DIRECTION_LEFT: //空格左移，左边的将/曹操往右移
                //先判断是否相邻，并确定基准点(下方的是哪一个空格)
                if (space[0].y == space[1].y + 1) {
                    //space[0]在space[1]的上方，取space[1]为基准点
                    index = 1;
                } else if (space[0].y + 1 == space[1].y) {
                    index = 0;
                } else {
                    return null; //不相邻
                }
                //找出基准点左边的棋子
                piece = getPiece(space[index].x - 1, space[index].y);
                if (piece == null) {
                    return null;
                }
                if (piece.type == HuaPiece.PIECE_CAOCHAO) {
                    if (piece.x != space[index].x - 2 || piece.y != space[index].y) {
                        return null;
                    } //曹操的起始位置必须为(x-2,y);
                } else if (piece.type == HuaPiece.PIECE_VERTICAL) {
                    if (piece.x != space[index].x - 1 || piece.y != space[index].y) {
                        return null;
                    } //帅的起始位置必须为(x-1,y);
                } else {
                    return null;
                }
                nextStepDirection = HuaPiece.DIRECTION_RIGHT;
                nextStepPiece = piece;
                newBoard = newBoardAfterMove(piece, HuaPiece.DIRECTION_RIGHT, -1);
                return newBoard;
            case HuaPiece.DIRECTION_RIGHT: //空格右移，右边的将/曹操往左移
                //先判断是否相邻，并确定基准点(下方的是哪一个空格)
                if (space[0].y == space[1].y + 1) {
                    //space[0]在space[1]的上方，取space[1]为基准点
                    index = 1;
                } else if (space[0].y + 1 == space[1].y) {
                    index = 0;
                } else {
                    return null; //不相邻
                }
                //找出基准点右边的棋子
                piece = getPiece(space[index].x + 1, space[index].y);
                if (piece == null) {
                    return null;
                }
                if (piece.type != HuaPiece.PIECE_CAOCHAO && piece.type != HuaPiece.PIECE_VERTICAL) {
                    return null;
                }
                if (piece.x != space[index].x + 1 || piece.y != space[index].y) {
                    return null;
                } //将或者曹操的起始位置必须wei(x+1,y);
                nextStepDirection = HuaPiece.DIRECTION_LEFT;
                nextStepPiece = piece;
                newBoard = newBoardAfterMove(piece, HuaPiece.DIRECTION_LEFT, -1);
                return newBoard;
            default:
                System.out.println("error:unknown direction" + String.valueOf(direction));
                return null;
        }
    }

    public String toJsonString() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", name);

            JSONArray jsonArray = new JSONArray();
            int numberOfArray = 0;
            if (caochao != null) {
                jsonArray.put(numberOfArray, caochao.toJsonString());
                numberOfArray++;
            }
            if (jiang != null) {
                for (int i = 0; i < jiang.length; i++) {
                    jsonArray.put(numberOfArray, jiang[i].toJsonString());
                    numberOfArray++;
                }
            }
            if (shuai != null) {
                for (int i = 0; i < shuai.length; i++) {
                    jsonArray.put(numberOfArray, shuai[i].toJsonString());
                    numberOfArray++;
                }
            }
            if (bing != null) {
                for (int i = 0; i < bing.length; i++) {
                    jsonArray.put(numberOfArray, bing[i].toJsonString());
                    numberOfArray++;
                }
            }
            if (space != null) {
                for (int i = 0; i < space.length; i++) {
                    jsonArray.put(numberOfArray, space[i].toJsonString());
                    numberOfArray++;
                }
            }
            jsonObject.put("pieces", jsonArray);
            return jsonObject.toString();
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }
    public static HuaBoard parseFromJson(String string) {
        try {
            JSONObject jsonObject = new JSONObject(string);
            String name = jsonObject.getString("name");
            JSONArray jsonArray = jsonObject.getJSONArray("pieces");
            HuaPiece[] pieces = new HuaPiece[jsonArray.length()];
            Log.v("main", String.format("pieces length=%d", jsonArray.length()));
            for (int i = 0; i < jsonArray.length(); i++) {
                String pieceString = (String) jsonArray.get(i);
                Log.v("main", pieceString);
                pieces[i] = HuaPiece.parseFromJson(pieceString);
            }
            return new HuaBoard(name, pieces);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public String piecesToString() {
        String string = "";
        //把4*5格式组成一个字符串
        for(int y=MAX_Y-1; y>=0; y--) {
            for(int x=0; x<MAX_X; x++) {
                string += HuaPiece.getTypeName(pieces[x][y].type);
            }
        }
        return string;
    }
    public static HuaBoard fromPiecesString(String string){
        //根据4*5格式的字符串恢复board;
        HuaBoard board = new HuaBoard();
        //把字符串转换成一个二位字符数组
        char[][] charArray = new char[MAX_X][MAX_Y];
        int index = 0;
        for(int y=MAX_Y-1; y>= 0; y--){
            for(int x=0; x<MAX_X; x++){
                charArray[x][y] = string.charAt(index);
                index++;
            }
        }
        //找曹操，找出最小坐标
        int x1=-1,y1=-1;
        for(int x=0; x<MAX_X; x++){
            for(int y=0; y<MAX_Y; y++){
                if (charArray[x][y] == '曹'){
                    if (x1 == -1 || x < x1 || y <y1){ //坐标更小
                        x1 = x; y1 = y;
                    }
                }
            }
        }
        board.caochao = new HuaPiece("曹操", HuaPiece.PIECE_CAOCHAO,x1,y1);
        //找将
        int numberOfJiang = 0;
        HuaPiece[] newJiang = new HuaPiece[MAX_Y*MAX_X/2];
        while(true){
            y1 = -1; //先找坐标y最小的将
            for(int x=0; x<MAX_X; x++) {
                for (int y = 0; y < MAX_Y; y++) {
                    if(charArray[x][y] == '将'){
                        if (y1 == -1 || y < y1){
                            x1 = x; y1 = y;
                        }
                    }
                }
            }
            if (y1 >= 0){ //找到了一个
                newJiang[numberOfJiang] = new HuaPiece("将"+ HuaPiece.numberToChinese(numberOfJiang+1), HuaPiece.PIECE_VERTICAL,x1,y1);
                numberOfJiang ++;
                //校验一下charArray相应位置是否对应将
                if (charArray[x1][y1] != '将' || charArray[x1][y1+1] != '将'){
                    System.out.println("将寻找错误");
                    return null;
                }
                charArray[x1][y1] = ' ';
                charArray[x1][y1+1] = ' ';
            }else{ //没找到，跳出循环
                break;
            }
        }
        if (numberOfJiang > 0){  //把找到的将复制到board
            board.jiang = new HuaPiece[numberOfJiang];
            for (int i=0; i<numberOfJiang; i++){
                board.jiang[i] = newJiang[i].copyPiece();
            }
        }
        //找帅
        int numberOfShuai = 0;
        HuaPiece[] newShuai = new HuaPiece[MAX_Y*MAX_X/2];
        while(true){
            //先找坐标x最小的将
            x1 = -1;
            for(int x=0; x<MAX_X; x++) {
                for (int y = 0; y < MAX_Y; y++) {
                    if(charArray[x][y] == '帅'){
                        if (x1 == -1 || x < x1){
                            x1 = x; y1 = y;
                        }
                    }
                }
            }
            if (x1 >= 0){ //找到了一个
                newShuai[numberOfShuai] = new HuaPiece("帅"+ HuaPiece.numberToChinese(numberOfShuai+1), HuaPiece.PIECE_HORIZON,x1,y1);
                if (charArray[x1][y1] != '帅' || charArray[x1+1][y1] != '帅'){
                    System.out.println("帅寻找错误");
                    return null;
                }
                numberOfShuai ++;
                charArray[x1][y1] = ' ';
                charArray[x1+1][y1] = ' ';
            }else{ //没找到，跳出循环
                break;
            }
        }
        if (numberOfShuai > 0){  //把找到的将复制到board
            board.shuai = new HuaPiece[numberOfShuai];
            for (int i=0; i<numberOfShuai; i++){
                board.shuai[i] = newShuai[i].copyPiece();
            }
        }
        //找兵
        int numberOfBing = 0;
        HuaPiece[] newBing = new HuaPiece[MAX_Y*MAX_X];
        for(int x=0; x<MAX_X; x++) {
            for (int y = 0; y < MAX_Y; y++) {
                if(charArray[x][y] == '兵'){
                    newBing[numberOfBing] = new HuaPiece("兵"+ HuaPiece.numberToChinese(numberOfBing+1), HuaPiece.PIECE_SOLDIER,x,y);
                    numberOfBing ++;
                }
            }
        }

        if (numberOfBing > 0){
            board.bing = new HuaPiece[numberOfBing];
            for (int i=0; i<numberOfBing; i++){
                board.bing[i] = newBing[i].copyPiece();
            }
        }
        //找空格
        int numberOfSpace = 0;
        HuaPiece[] newSpace = new HuaPiece[MAX_Y*MAX_X];
        for(int x=0; x<MAX_X; x++) {
            for (int y = 0; y < MAX_Y; y++) {
                if(charArray[x][y] == '空'){
                    newSpace[numberOfSpace] = new HuaPiece("空"+ HuaPiece.numberToChinese(numberOfSpace+1), HuaPiece.PIECE_EMPTY,x,y);
                    numberOfSpace ++;
                }
            }
        }
        if (numberOfSpace > 0){
            board.space = new HuaPiece[numberOfSpace];
            for (int i=0; i<numberOfSpace; i++){
                board.space[i] = newSpace[i].copyPiece();
            }
        }
        board.name = "";
        board.convertBoard(); //生成pieces
        return board;
    }
    public DBBoard toDBBoard(){
        if (bestSolution == null){
            return new DBBoard(name,GameType.HUARONGDAO,piecesToString(),0,"");
        }else {
            return new DBBoard(name, GameType.HUARONGDAO, piecesToString(), bestSolution.getSteps(), bestSolution.toJson().toString());
        }
    }
    public static HuaBoard fromDBBoard(DBBoard dbBoard){
        return dbBoard.getBoard();
    }
}