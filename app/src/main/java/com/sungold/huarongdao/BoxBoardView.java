package com.sungold.huarongdao;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

public class BoxBoardView extends BoardView {
    public BoxBoardView(Context context) {
        super(context);
    }
    public BoxBoardView(Context context, AttributeSet attrs){
        super(context,attrs);
    }
    @Override
    public void drawPieces(Canvas canvas){
        if(board == null) { return; }
        BoxBoard boxBoard = (BoxBoard) board;
        if(boxBoard.boy != null) {
            drawPiece(canvas, boxBoard.boy);
        }
        //画blocks
        if(boxBoard.blocks != null) {
            for (int i = 0; i < boxBoard.blocks.length; i++) {
                drawPiece(canvas, boxBoard.blocks[i]);
            }
        }
        //画boxs
        if(boxBoard.boxs != null) {
            for (int i = 0; i < boxBoard.boxs.length; i++) {
                drawPiece(canvas, boxBoard.boxs[i]);
            }
        }
        //画目标，分几种情况，1、和boy重叠，2、和box重叠，3、未重叠
        if(boxBoard.destPointers != null){
            for(int i=0; i < boxBoard.destPointers.length; i++){
                int x = boxBoard.destPointers[i].x;
                int y = boxBoard.destPointers[i].y;
                Piece newPiece = boxBoard.getPiece(x,y);
                if(newPiece == null){
                    drawPiece(canvas,boxBoard.destPointers[i]);
                }else if(newPiece.type == Piece.PIECE_BOX){ //和BOX重叠
                    newPiece.type = Piece.PIECE_DEST_BOX;
                    drawPiece(canvas,newPiece);
                }else if(newPiece.type == Piece.PIECE_BOY){
                    newPiece.type = Piece.PIECE_DEST_BOY;
                    drawPiece(canvas,newPiece);
                }
            }
        }
    }
}
