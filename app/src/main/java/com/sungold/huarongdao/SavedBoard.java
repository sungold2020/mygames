package com.sungold.huarongdao;

public class SavedBoard {
    //用于存储在哈希表中的棋盘，不直接用class Board，是为了节省内存。
    //roles，二维数组，数组中的值为role的type。
    //short[][] pieces;
    long hash;
    SavedBoard(long hash){
        /*pieces = new short[MAX_X][MAX_Y];
        for(int x=0; x<MAX_X; x++) {
            for(int y=0; y<MAX_Y; y++) {
                pieces[x][y] = -1;
            }
        }*/
        this.hash = hash;
    }
}