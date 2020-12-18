package com.sungold.huarongdao;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class BlockAddActivity extends AppCompatActivity {
    BlockAddView blockAddView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //新建一个方块华容道棋盘，其中名字，棋盘大小，从前面一个activity传递过来
        super.onCreate(savedInstanceState);
        initBlockBoardAdd();

/*        Bundle bundle = this.getIntent().getExtras();
        BlockAddView.name =  bundle.getString("name");
        BlockAddView.maxX =  bundle.getInt("maxX");
        BlockAddView.maxY = bundle.getInt("maxY");
        BlockAddView.exit = bundle.getInt("exit");
        BlockAddView.kingType = bundle.getInt("kingType");
        BlockAddView.kingLength = bundle.getInt("kingLength");

        setContentView(R.layout.activity_block_add); */
    }
    public void initBlockBoardAdd(){
        //通过对话框输入必要信息后，启动BlockBoardAddView
        LayoutInflater factory = LayoutInflater.from(BlockAddActivity.this);
        final View textEntryView = factory.inflate(R.layout.dialog_add_block, null);
        final EditText nameText = (EditText) textEntryView.findViewById(R.id.editText_name);
        final EditText maxXText = (EditText)textEntryView.findViewById(R.id.editText_maxX);
        final EditText maxYText = (EditText)textEntryView.findViewById(R.id.editText_maxY);
        final EditText kingTypeText = (EditText)textEntryView.findViewById(R.id.editText_kingType);
        final EditText kingLengthText  = (EditText)textEntryView.findViewById(R.id.editText_kingLength);

        AlertDialog.Builder builder = new AlertDialog.Builder(BlockAddActivity.this);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setView(textEntryView);
        builder.setTitle("新增棋盘");
        nameText.setText("test");
        maxXText.setText("6");
        maxYText.setText("6");
        kingTypeText.setText("0");
        kingLengthText.setText("2");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                BlockAddView.name = nameText.getText().toString();
                BlockAddView.maxX = Integer.valueOf(maxXText.getText().toString());
                BlockAddView.maxY = Integer.valueOf(maxYText.getText().toString());
                BlockAddView.kingType = Integer.valueOf(kingTypeText.getText().toString());
                BlockAddView.kingLength = Integer.valueOf(kingLengthText.getText().toString());
                if (BlockAddView.kingType == 0){
                    BlockAddView.kingType = BlockPiece.PIECE_HORIZON;
                }else{
                    BlockAddView.kingType = BlockPiece.PIECE_VERTICAL;
                }

                setContentView(R.layout.activity_block_add);
                Toolbar toolbar =  (Toolbar) findViewById(R.id.toolbar_add);
                setSupportActionBar(toolbar);
                TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
                toolbarTitle.setText("新增牌局");
                blockAddView = (BlockAddView) findViewById(R.id.add_board);
                dialog.dismiss();

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.toolbar_add,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()) {
            case R.id.toolbar_save:
                Log.v("MainActivity","click saveboard button");
                //TODO
                if (blockAddView.saveBoard()){
                    Log.v("addboardview","check OK");
                    //Intent intent = new Intent();
                    //setResult(RESULT_SUCCESS,intent);
                    //finish();
                    Intent intent = new Intent(BlockAddActivity.this,BlockBoardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    //Bundle bundle = new Bundle();
                    //bundle.putString("name",blockAddView.board.name);
                    //bundle.putString("pieces",addBoardView.board.piecesToString());
                    //bundle.putString("solution",.getSolution());
                    //intent.putExtras(bundle);
                    //BlockBoardView.startBlockBoard = blockAddView.board;
                    Bundle bundle = new Bundle();
                    bundle.putString("name",blockAddView.board.name);
                    bundle.putString("board",blockAddView.board.toDBString());
                    intent.putExtras(bundle);
                    //bundle.putString("solution",dbBoard.getSolutionString());
                    startActivity(intent);
                    finish();
                }
                //Log.v("addboard","failed to save");
                break;
            case R.id.toolbar_back:
                //setPref();
                break;
            default:
        }
        return true;
    }
}
