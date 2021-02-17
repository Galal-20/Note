package com.example.note.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.note.helper.Constants;
import com.example.note.R;
import com.example.note.adapters.NotesAdapter;
import com.example.note.database.NotesDatabase;
import com.example.note.entities.Note;
import com.example.note.listeners.NotesListeners;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesListeners {

    private RecyclerView notesRecyclerView;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;

    private int noteClickedPosition =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageAddNoteMain = findViewById(R.id.imageAddNoteMain);
        imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(getApplicationContext() , CreateNoteActivity.class), Constants.REQUEST_CODE_ADD_NOTE);
            }
        });
//Recycle
        notesRecyclerView = findViewById(R.id.notesRecycleView);
        notesRecyclerView.setLayoutManager(new
                StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL));

        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList , this);
        notesRecyclerView.setAdapter(notesAdapter);

        getNotes(Constants.REQUEST_CODE_SHOW_UPDATES);
    }

    //Get async task to save a note
    //get note from database

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext() , CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate" , true);
        intent.putExtra("note" , note);
        startActivityForResult(intent , Constants.REQUEST_CODE_UPDATE_NOTE);
    }

    private void getNotes(final  int requestCode){

        @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void , Void , List<Note>>{

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase
                        .getDatabase(getApplicationContext())
                        .noteDao()
                        .getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                //to display notes
                if (requestCode == Constants.REQUEST_CODE_SHOW_UPDATES){
                    noteList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                }else if (requestCode == Constants.REQUEST_CODE_ADD_NOTE){
                    noteList.add(0, notes.get(0));
                    notesAdapter.notifyItemChanged(0);
                    notesRecyclerView.smoothScrollToPosition(0);
                }else if (requestCode == Constants.REQUEST_CODE_UPDATE_NOTE){
                    noteList.remove(noteClickedPosition);
                    noteList.add(noteClickedPosition , notes.get(noteClickedPosition));
                    notesAdapter.notifyItemChanged(noteClickedPosition);
                }
            }
        }
        new GetNotesTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK){
            getNotes(Constants.REQUEST_CODE_ADD_NOTE);
        }else if (requestCode == Constants.REQUEST_CODE_UPDATE_NOTE && requestCode == RESULT_OK){
            if(data != null){
                getNotes(Constants.REQUEST_CODE_UPDATE_NOTE);
            }
        }
    }
}