package com.sargent.mark.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


import com.sargent.mark.todolist.data.Contract;
import com.sargent.mark.todolist.data.DBHelper;


import static com.sargent.mark.todolist.data.Contract.TABLE_TODO.TABLE_NAME;


public class MainActivity extends AppCompatActivity implements AddToDoFragment.OnDialogCloseListener, UpdateToDoFragment.OnUpdateDialogCloseListener{

    private static RecyclerView rv;
    private FloatingActionButton button;
    private static DBHelper helper;
    private static Cursor cursor;
    private static SQLiteDatabase db;
    static ToDoListAdapter adapter;
    String[] temp = null;
    static String[] tempArr = null;
    static Intent refresh;




    private final String TAG = "mainactivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "oncreate called in main activity");

        button = (FloatingActionButton) findViewById(R.id.addToDo);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                        FragmentManager fm = getSupportFragmentManager();
                        AddToDoFragment frag = new AddToDoFragment();
                        frag.show(fm, "addtodofragment");
                }

        });

        rv = (RecyclerView) findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));

    }

    //Was created to call main menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    //when clicking one of the options from the menu items it sets temp to specific category
    // then calls getAllItems and does the appropriate query
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       temp = new String[1];

        if (item.getItemId() == R.id.all_button) {
            temp = null;
        }else if (item.getItemId() == R.id.homework_button) {
            temp[0] = "Homework";
        }else if (item.getItemId() == R.id.lab_button){
            temp[0] = "Labs";
        }else if (item.getItemId() == R.id.test_button){
            temp[0] = "Tests";
        }else if (item.getItemId() == R.id.quiz_button){
            temp[0] = "Quizzes";

        }
        cursor = getAllItems(db);
        adapter.swapCursor(cursor);

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (db != null) db.close();
        if (cursor != null) cursor.close();
    }

    @Override
    protected void onStart() {
        super.onStart();

        helper = new DBHelper(this);
        db = helper.getWritableDatabase();
        cursor = getAllItems(db);

        adapter = new ToDoListAdapter(cursor, new ToDoListAdapter.ItemClickListener() {

            //Update will be changed to include category ------------------------------------------------------
            @Override
            public void onItemClick(int pos, String description, String duedate, String category, long id) {
                Log.d(TAG, "item click id: " + id);
                String[] dateInfo = duedate.split("-");
                int year = Integer.parseInt(dateInfo[0].replaceAll("\\s",""));
                int month = Integer.parseInt(dateInfo[1].replaceAll("\\s",""));
                int day = Integer.parseInt(dateInfo[2].replaceAll("\\s",""));

                FragmentManager fm = getSupportFragmentManager();
                //updated to include category
                UpdateToDoFragment frag = UpdateToDoFragment.newInstance(year, month, day, description, category, id);
                frag.show(fm, "updatetodofragment");
            }

        });

        rv.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                long id = (long) viewHolder.itemView.getTag();
                Log.d(TAG, "passing id: " + id);
                removeToDo(db, id);
                adapter.swapCursor(getAllItems(db));
            }
        }).attachToRecyclerView(rv);

    }
    //updated to included category
    @Override
    public void closeDialog(int year, int month, int day, String description, String category) {
        addToDo(db, description, category, formatDate(year, month, day));
        cursor = getAllItems(db);
        adapter.swapCursor(cursor);
    }

    public String formatDate(int year, int month, int day) {
        return String.format("%04d-%02d-%02d", year, month + 1, day);
    }


//once the category has been taken in it goes through if statements looking for the right
// category to get the right query to use by default if no category has been choosen it will do a select * from todoitems
// giving back all items.
    private Cursor getAllItems(SQLiteDatabase db) {
        if(!(temp == null) ) {
            if ((temp[0]).contains("Homework")) {
                return db.rawQuery("Select * From todoitems where category = ? ", temp);

            }else if ((temp[0]).contains("Labs")) {
                return db.rawQuery("Select * From todoitems where category = ? ", temp);

            }else if ((temp[0]).contains("Tests")) {
                return db.rawQuery("Select * From todoitems where category = ? ", temp);

            }else if ((temp[0]).contains("Quizzes")) {
                return db.rawQuery("Select * From todoitems where category = ? ", temp);

            }
        }
        else {
            return db.rawQuery("Select * From todoitems; ", null);
        }

        return null;
    }
    //was updated to include category
    private long addToDo(SQLiteDatabase db, String description, String category, String duedate) {
        ContentValues cv = new ContentValues();
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION, description);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_CATEGORY, category);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE, duedate);
        return db.insert(TABLE_NAME, null, cv);
    }

    private boolean removeToDo(SQLiteDatabase db, long id) {
        Log.d(TAG, "deleting id: " + id);
        return db.delete(TABLE_NAME, Contract.TABLE_TODO._ID + "=" + id, null) > 0;
    }

    //was updated to include category
    private int updateToDo(SQLiteDatabase db, int year, int month, int day, String description, String category, long id){

        String duedate = formatDate(year, month - 1, day);

        ContentValues cv = new ContentValues();
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION, description);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_CATEGORY, category);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE, duedate);

        return db.update(TABLE_NAME, cv, Contract.TABLE_TODO._ID + "=" + id, null);
    }
    //updated to include category
    @Override
    public void closeUpdateDialog(int year, int month, int day, String description, String category, long id) {
        updateToDo(db, year, month, day, description, category, id);
        adapter.swapCursor(getAllItems(db));
    }

    //this method is used to update the status of the checkbox onclick called from the ToDoListAdapter
    public static void updateStatus(long id, String check){
        tempArr = new String[2];
        String tempId = Long.toString(id);
        tempArr[1] = tempId;

         if (check.equals("Not Done")) {
            tempArr[0] = "Done";
             cursor = db.rawQuery("Update todoitems Set status = ? where _id = ?", tempArr);

         } else {
             tempArr[0] = "Not Done";
             cursor = db.rawQuery("Update todoitems Set status = ? where _id = ?", tempArr);

        }

        cursor.moveToFirst();
        //closes cursor to begin the next cursor after using update with rawQuery inorder to fully function
        // properly prior to adding this app would return a blank screen  
        cursor.close();
        cursor = db.rawQuery("Select * From todoitems; ", null);
        adapter.swapCursor(cursor);


    }

}