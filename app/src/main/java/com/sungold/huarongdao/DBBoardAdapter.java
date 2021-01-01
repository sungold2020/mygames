package com.sungold.huarongdao;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class DBBoardAdapter extends BaseAdapter {
    private List<DBBoard> dbBoardList = null;
    private LayoutInflater layoutInflater = null;

    public DBBoardAdapter(Context context,  List<DBBoard> dbBoardList) {
        this.dbBoardList = dbBoardList;
        layoutInflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return dbBoardList.size();
    }

    @Override
    public Object getItem(int position) {
        return dbBoardList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null){
            holder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.board_item,null);
            holder.text_name = (TextView) convertView.findViewById(R.id.board_name);
            holder.text_steps = (TextView) convertView.findViewById(R.id.board_steps);
            holder.text_adddate = (TextView) convertView.findViewById(R.id.board_adddate);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        DBBoard dbBoard = dbBoardList.get(position);
        if (dbBoard != null){
            holder.text_name.setText(dbBoard.getName());
            if(dbBoard.dbType == DBBoard.DBBOARD_TYPE_GOING) {
               switch (dbBoard.getGameType()){
                   case HUARONGDAO:
                   case BLOCK:
                   case BOX:
                       holder.text_steps.setText(String.format("最佳步数:%d", dbBoard.getSteps()));
                       break;
                   case SUDOKU:
                       holder.text_steps.setText(String.format("已用时:%d",dbBoard.getSeconds()));
                       break;
                   default:
                       break;
               }
               holder.text_adddate.setText(dbBoard.getSaveDate());
            }else{
                holder.text_steps.setText(String.format("最佳步数:%d", dbBoard.getSteps()));
                holder.text_adddate.setVisibility(View.GONE);
            }
        }
        return convertView;
    }
    public void remove(int position){
        dbBoardList.remove(position);
        notifyDataSetChanged();
    }
    @Override
    public int getItemViewType(int position) {
        return 0;
    }
    class ViewHolder{
        TextView text_name;
        TextView text_steps;
        TextView text_adddate;
    }
}
