package com.sargent.mark.todolist;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.sargent.mark.todolist.data.Contract;

import static com.sargent.mark.todolist.MainActivity.adapter;
import static com.sargent.mark.todolist.MainActivity.refresh;

/**
 * Created by mark on 7/4/17.
 */

public class ToDoListAdapter extends RecyclerView.Adapter<ToDoListAdapter.ItemHolder> {

    //private CheckBoxItem mCheck;
    private Cursor cursor;
    private ItemClickListener listener;
    private String TAG = "todolistadapter";

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.item, parent, false);
        ItemHolder holder = new ItemHolder(view);



        return holder;
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        holder.bind(holder, position);
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    //Update will be changed to include category ------------------------------------------------------
    public interface ItemClickListener {
        void onItemClick(int pos, String description, String duedate, String category,  long id);

    }

    public interface CheckboxItem{
        void mCheck(long id, String check);
    }


    public ToDoListAdapter(Cursor cursor, ItemClickListener listener) {
        this.cursor = cursor;
        this.listener = listener;
    }

    public void swapCursor(Cursor newCursor){
        if (cursor != null) cursor.close();
        cursor = newCursor;
        if (newCursor != null) {
            // Force the RecyclerView to refresh
            this.notifyDataSetChanged();
        }
    }



    //Update will be changed to include category ------------------------------------------------------
    class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView descr;
        TextView due;
        TextView categ;
        CheckBox checkBox;
        String check;
        String duedate;
        String description;
        String category;
        long id;

        //categ and checkbox were added to include category and checkbox status
        ItemHolder(View view) {
            super(view);
            descr = (TextView) view.findViewById(R.id.description);
            due = (TextView) view.findViewById(R.id.dueDate);
            categ = (TextView) view.findViewById(R.id.category);
            checkBox = (CheckBox) view.findViewById(R.id.checkbox);
            view.setOnClickListener(this);
        }


        public void bind(ItemHolder holder, int pos) {
            cursor.moveToPosition(pos);
            id = cursor.getLong(cursor.getColumnIndex(Contract.TABLE_TODO._ID));
            Log.d(TAG, "deleting id: " + id);

            //attain values row by row
            duedate = cursor.getString(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE));
            description = cursor.getString(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION));
            category = cursor.getString(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_CATEGORY));
            check = cursor.getString(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_STATUS));

            //listens for when check is clicked
            checkBox.setOnClickListener(new View.OnClickListener(){

                //once clicked it calls updateStatus to update database
                @Override
                public void onClick(View v) {
                    MainActivity.updateStatus(id, check);
                }
            });


            descr.setText(description);
            due.setText(duedate);
            categ.setText(category);
            holder.itemView.setTag(id);

            //checks to see if check box should be checked or not
            if(check.equals("Not Done")) {
                checkBox.setChecked(false);
            }
            else {
                checkBox.setChecked(true);
            }

        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            listener.onItemClick(pos, description, duedate, category, id);
        }



    }



}
