package com.sungold.huarongdao;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SudokuFindSolution{
    SudokuBoard board;
    List<SudokuBoard> boardList = new ArrayList<>();
    //int solutionCount = 0;
    int maxX , maxY, maxNumber;
    SudokuFindSolution(SudokuBoard sudokuBoard){
        this.board = sudokuBoard.copyBoard();
        maxX = SudokuBoard.getMaxX(board.sudokuType);
        maxY = SudokuBoard.getMaxY(board.sudokuType);
        maxNumber = SudokuBoard.getMaxNumber(board.sudokuType);
        for(int x=0; x<maxX; x++){
            for(int y=0; y<maxY; y++){
                if(board.pieces[x][y].getNumber() > 0) {
                    board.pieces[x][y].setModifiable(false);
                }
            }
        }
    }
    public int solutionCount(){
        if(boardList == null){
            return 0;
        }
        return boardList.size();
    }
    public void findSolution(){
        nextPiece(board.pieces[0][0]);
    }
    public  void nextPiece(SudokuPiece piece){
        //从piece开始递归各种可能性

        if(solutionCount() >= 2){
            return;
        }
        Boolean full = true;  //是否所有数字都填满了.
        //遍历设置每一个单元的每一个数值
        for(int x=piece.x; x<maxX; x++){
            for(int y=0; y<maxY; y++){
                if(x == piece.x && y < piece.y){  //从(x,y)开始
                    continue;
                }
                if(board.pieces[x][y].getNumber() > 0){ //已经填写了数字
                    continue;
                }
                full = false;
                for(int i=1; i<=maxNumber; i++) {   //尝试每一个数字的可能性
                    if(board.numberExist(board.pieces[x][y],i)){ //数字已经存在冲突
                        continue;
                    }
                    board.pieces[x][y].setNumber(i);
                    nextPiece(board.pieces[x][y]); //从下一个数开始递归
                    board.pieces[x][y].setNumber(0); //回退
                }
                return;
            }
        }
        //只有所有数字都填满了，才能走到这。
        if(full){
            addSolution(board);
            board.printModifiedPieces();
        }
    }
    private void addSolution(SudokuBoard board){
        //加入boardList，加入前检查是否重复
        if(boardList == null || boardList.size() == 0){
            boardList.add(board.copyBoard());
            return;
        }
        for(int i=0; i<boardList.size(); i++){
            if(boardList.get(i).isSameBoard(board)){
                return;
            }
        }
        boardList.add(board.copyBoard());
    }
    public void printSolution(){
        if(boardList == null || boardList.size() == 0){
            return ;
        }
        for(int i=0; i<boardList.size(); i++){
            Log.v("sudoku",String.format("solution:%d",i));
            boardList.get(i).printModifiedPieces();
        }
    }
}
