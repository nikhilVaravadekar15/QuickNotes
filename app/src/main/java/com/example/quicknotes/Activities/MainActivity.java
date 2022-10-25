package com.example.quicknotes.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.quicknotes.Adapters.NotesAdapter;
import com.example.quicknotes.Database.NotesDatabase;
import com.example.quicknotes.Entities.Note;
import com.example.quicknotes.Listeners.NoteListener;
import com.example.quicknotes.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteListener {

    private static final String TAG = "MainActivity";

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTES = 3;
    public static final int REQUEST_CODE_SELECT_IMAGE = 4;
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 5;


    private List<Note> noteList;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter notesAdapter;

    private int noteClickedPosition = -1;

    private AlertDialog dialogAddUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageAddNoteMain = findViewById(R.id.imageAddNoteMain);
        imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(getApplicationContext(), CreateNoteActivity.class),
                        REQUEST_CODE_ADD_NOTE
                );
            }
        });

        noteList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView_notes);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );
        notesAdapter = new NotesAdapter(noteList, this);
        recyclerView.setAdapter(notesAdapter);

        getNotes(REQUEST_CODE_SHOW_NOTES, false);


        EditText inputSearchNote = findViewById(R.id.editText_inputSearchNote);
        inputSearchNote.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                notesAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
//                if (noteList.size() != 0){
//                    notesAdapter.searchNotes(s.toString());
//                }
            }
        });


        findViewById(R.id.imageAddNote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(getApplicationContext(), CreateNoteActivity.class),
                        REQUEST_CODE_ADD_NOTE
                );
            }
        });

        findViewById(R.id.imageAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
                } else {
                    selectImage();
                }
            }
        });
        findViewById(R.id.imageAddWebLink).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddUrlDialog();
            }
        });


//        onCreate END;
    }


    private void getNotes(final int requestCode, final boolean isNoteDeleted) {

        @SuppressLint("StaticFieldLeak")
        class GetNoteTask extends AsyncTask<Void, Void, List<Note>> {

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase.getNotesDatabase(getApplicationContext()).noteDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
//                Log.d(TAG, "onPostExecute: " + notes.toString());

                if (requestCode == REQUEST_CODE_SHOW_NOTES) {
                    noteList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                } else if (requestCode == REQUEST_CODE_ADD_NOTE) {
                    noteList.add(0, notes.get(0));
                    notesAdapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(0);
                } else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
                    noteList.remove(noteClickedPosition);

                    if (isNoteDeleted) {
                        notesAdapter.notifyItemRemoved(noteClickedPosition);
                    } else {
                        noteList.add(noteClickedPosition, notes.get(noteClickedPosition));
                        notesAdapter.notifyItemChanged(noteClickedPosition);
                    }
                }

            }
        }

        new GetNoteTask().execute();

//  getNotes END
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes(REQUEST_CODE_ADD_NOTE, false);
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));
            }
        } else if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        String selectedImagePath = getPathFromURi(selectedImageUri);
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions", true);
                        intent.putExtra("quickActionType", "Image");
                        intent.putExtra("imagePath", selectedImagePath);
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                    } catch (Exception exception) {
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }


    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);

    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(this, "Permissions Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getPathFromURi(Uri contentUri) {
        String filePath;
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    private void showAddUrlDialog(){
        if (dialogAddUrl == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(this)
                    .inflate(R.layout.layout_add_weburl, (ViewGroup) findViewById(R.id.layoutAddWebUrlContainer));
            builder.setView(view);

            dialogAddUrl = builder.create();
            if (dialogAddUrl.getWindow() != null){
                dialogAddUrl.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText inputUrl = view.findViewById(R.id.inputURL);
            inputUrl.requestFocus();

            view.findViewById(R.id.textAddUrl).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (inputUrl.getText().toString().trim().isEmpty()){
                        inputUrl.setError("Enter URL");
                    }
                    else if (!Patterns.WEB_URL.matcher(inputUrl.getText().toString()).matches()){
                        inputUrl.setError("Enter a valid URL");
                    }
                    else {
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions", true);
                        intent.putExtra("quickActionType", "URL");
                        intent.putExtra("webUrl", inputUrl.getText().toString());
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                        dialogAddUrl.dismiss();
                    }
                }
            });

            view.findViewById(R.id.textCancelUrl).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogAddUrl.dismiss();
                }
            });

        }

        dialogAddUrl.show();

    }


    //    MainActivity END
}

