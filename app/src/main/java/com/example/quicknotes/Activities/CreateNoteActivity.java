package com.example.quicknotes.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quicknotes.Database.NotesDatabase;
import com.example.quicknotes.Entities.Note;
import com.example.quicknotes.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {

    private EditText inputNoteTitle, inputNoteSubtitle, inputNoteText;
    private TextView textDateTime;
    private ImageView imageView_back, imageView_save;

    private View viewSubtitleIndicator;
    private ImageView noteImageView;
    private TextView textViewWebUrl;
    private LinearLayout linearLayoutWebUrl;

    private String selectedNoteColor;
    private String selectedImagePath;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;

    private AlertDialog dialogAddUrl;
    private AlertDialog dialogDeleteNote;

    private Note alreadyAvailableNote;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        imageView_back = findViewById(R.id.imageBack);
        inputNoteTitle = findViewById(R.id.inputNoteTitle);
        inputNoteSubtitle = findViewById(R.id.inputNoteSubtitle);
        inputNoteText = findViewById(R.id.inputNote);
        textDateTime = findViewById(R.id.textDateTime);
        imageView_save = findViewById(R.id.imageSave);
        viewSubtitleIndicator = findViewById(R.id.viewSubtitleIndicator);

        noteImageView = findViewById(R.id.imageNote);
        textViewWebUrl = findViewById(R.id.textWebUrl);
        linearLayoutWebUrl = findViewById(R.id.linearLayout_webUrl);

        textDateTime.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy, HH:mm a", Locale.getDefault())
                        .format(new Date())
        );

        imageView_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        imageView_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });


        selectedNoteColor = "#333333";
        selectedImagePath = "";
        initLayoutMiscellaneous();
        setViewSubtitleIndicatorColor();


        if (getIntent().getBooleanExtra("isViewOrUpdate", false)){
            alreadyAvailableNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }

        if (linearLayoutWebUrl.getVisibility() == View.VISIBLE){
            linearLayoutWebUrl.setVisibility(View.VISIBLE);
            textViewWebUrl.setText(textViewWebUrl.getText().toString());

        }

        findViewById(R.id.imageRemoveWebUrl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 textViewWebUrl.setText(null);
                 linearLayoutWebUrl.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.imageRemoveImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 noteImageView.setImageBitmap(null);
                 noteImageView.setVisibility(View.GONE);
                 findViewById(R.id.imageRemoveImage).setVisibility(View.GONE);
                 selectedImagePath = "";
            }
        });


        if (getIntent().getBooleanExtra("isFromQuickActions", false)){
            String type = getIntent().getStringExtra("quickActionType");
            if (type != null){
                if (type.equals("Image")){
                    selectedImagePath = getIntent().getStringExtra("imagePath");
                    noteImageView.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
                    noteImageView.setVisibility(View.VISIBLE);
                    findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);
                }else if (type.equals("webUrl")){
                    textViewWebUrl.setText(getIntent().getStringExtra("webUrl"));
                    linearLayoutWebUrl.setVisibility(View.VISIBLE);
                }
            }
        }

        //        onCreate END;
    }


    private void setViewOrUpdateNote(){

        inputNoteTitle.setText(alreadyAvailableNote.getTitle());
        inputNoteSubtitle.setText(alreadyAvailableNote.getSubtitle());
        inputNoteText.setText(alreadyAvailableNote.getNoteText());
        textDateTime.setText(alreadyAvailableNote.getDateTime());

        if (alreadyAvailableNote.getImagePath() != null && !alreadyAvailableNote.getImagePath().trim().isEmpty()){
            noteImageView.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableNote.getImagePath()));
            noteImageView.setVisibility(View.VISIBLE);
            findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);
            selectedImagePath = alreadyAvailableNote.getImagePath();
        }
        if (alreadyAvailableNote.getWebLink() != null && !alreadyAvailableNote.getWebLink().trim().isEmpty()){
            textViewWebUrl.setText(alreadyAvailableNote.getWebLink());
            linearLayoutWebUrl.setVisibility(View.VISIBLE);
        }

    }



    private void  saveNote() {
        if (inputNoteTitle.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Note title can not be empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (inputNoteSubtitle.getText().toString().trim().isEmpty() &&
                inputNoteText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note can not be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        final Note note = new Note();
        note.setTitle(inputNoteTitle.getText().toString());
        note.setSubtitle(inputNoteSubtitle.getText().toString());
        note.setNoteText(inputNoteText.getText().toString());
        note.setDateTime(textDateTime.getText().toString());
        note.setColor(selectedNoteColor);
        note.setImagePath(selectedImagePath);

        if (linearLayoutWebUrl.getVisibility() == View.VISIBLE){
            note.setWebLink(textViewWebUrl.getText().toString());
        }

        if (alreadyAvailableNote != null){
            note.setId(alreadyAvailableNote.getId());
        }

        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void>{

            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getNotesDatabase(getApplicationContext()).noteDao().insertNotes(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }

        new SaveNoteTask().execute();
//  saveNote END
    }


    private void initLayoutMiscellaneous(){
        final LinearLayout linearLayoutMiscellaneous = findViewById(R.id.linearLayout_miscellaneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(linearLayoutMiscellaneous);
        linearLayoutMiscellaneous.findViewById(R.id.view_miscellaneous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BottomSheetBehavior.STATE_EXPANDED != bottomSheetBehavior.getState()){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        final ImageView imageViewColor1 = linearLayoutMiscellaneous.findViewById(R.id.imageColor1);
        final ImageView imageViewColor2 = linearLayoutMiscellaneous.findViewById(R.id.imageColor2);
        final ImageView imageViewColor3 = linearLayoutMiscellaneous.findViewById(R.id.imageColor3);
        final ImageView imageViewColor4 = linearLayoutMiscellaneous.findViewById(R.id.imageColor4);
        final ImageView imageViewColor5 = linearLayoutMiscellaneous.findViewById(R.id.imageColor5);

        linearLayoutMiscellaneous.findViewById(R.id.imageColor1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#333333";
                imageViewColor1.setImageResource(R.drawable.ic_done);
                imageViewColor2.setImageResource(0);
                imageViewColor3.setImageResource(0);
                imageViewColor4.setImageResource(0);
                imageViewColor5.setImageResource(0);
                setViewSubtitleIndicatorColor();
            }
        });

        linearLayoutMiscellaneous.findViewById(R.id.imageColor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#EBC77B";
                imageViewColor1.setImageResource(0);
                imageViewColor2.setImageResource(R.drawable.ic_done);
                imageViewColor3.setImageResource(0);
                imageViewColor4.setImageResource(0);
                imageViewColor5.setImageResource(0);
                setViewSubtitleIndicatorColor();
            }
        });

        linearLayoutMiscellaneous.findViewById(R.id.imageColor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#BE8584";
                imageViewColor1.setImageResource(0);
                imageViewColor2.setImageResource(0);
                imageViewColor3.setImageResource(R.drawable.ic_done);
                imageViewColor4.setImageResource(0);
                imageViewColor5.setImageResource(0);
                setViewSubtitleIndicatorColor();
            }
        });

        linearLayoutMiscellaneous.findViewById(R.id.imageColor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#8892D9";
                imageViewColor1.setImageResource(0);
                imageViewColor2.setImageResource(0);
                imageViewColor3.setImageResource(0);
                imageViewColor4.setImageResource(R.drawable.ic_done);
                imageViewColor5.setImageResource(0);
                setViewSubtitleIndicatorColor();
            }
        });

        linearLayoutMiscellaneous.findViewById(R.id.imageColor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#8D7CAD";
                imageViewColor1.setImageResource(0);
                imageViewColor2.setImageResource(0);
                imageViewColor3.setImageResource(0);
                imageViewColor4.setImageResource(0);
                imageViewColor5.setImageResource(R.drawable.ic_done);
                setViewSubtitleIndicatorColor();
            }
        });


        if (alreadyAvailableNote != null && alreadyAvailableNote.getColor() != null && !alreadyAvailableNote.getColor().trim().isEmpty()){
            switch (alreadyAvailableNote.getColor()){
                case "#EBC77B":
                    linearLayoutMiscellaneous.findViewById(R.id.viewColor2).performClick();
                    break;
                case "#BE8584":
                    linearLayoutMiscellaneous.findViewById(R.id.viewColor3).performClick();
                    break;
                case "#8892D9":
                    linearLayoutMiscellaneous.findViewById(R.id.viewColor4).performClick();
                    break;
                case "#8D7CAD":
                    linearLayoutMiscellaneous.findViewById(R.id.viewColor5).performClick();
                    break;

            }
        }

        linearLayoutMiscellaneous.findViewById(R.id.layoutAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(CreateNoteActivity.this,
                             new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
                }
                else {
                    selectImage();
                }

            }
        });

        linearLayoutMiscellaneous.findViewById(R.id.layoutAddWebUrl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    showAddUrlDialog();
            }
        });


        if (alreadyAvailableNote != null ){
            linearLayoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setVisibility(View.VISIBLE);
            linearLayoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    showDeleteNoteDialog();
                }
            });
        }


//  initLayoutMiscellaneous END
    }

    private void showDeleteNoteDialog(){

        if (dialogDeleteNote == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(getApplicationContext()).inflate(
                    R.layout.layout_delete_note,
                    (ViewGroup) findViewById(R.id.layoutDeleteNoteContainer));
            builder.setView(view);
            dialogDeleteNote = builder.create();
            if (dialogDeleteNote.getWindow() != null){
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            view.findViewById(R.id.textDeleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    @SuppressLint("StaticFieldLeak")
                    class DeleteNoteTask extends AsyncTask<Void, Void, Void>{

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NotesDatabase.getNotesDatabase(getApplicationContext()).noteDao().DeleteNote(alreadyAvailableNote);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            Intent intent = new Intent();
                            intent.putExtra("isNoteDeleted", true);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }

                    new DeleteNoteTask().execute();
                }
            });

            view.findViewById(R.id.textDeleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogDeleteNote.dismiss();
                }
            });

        }

        dialogDeleteNote.show();
    }

    private void  setViewSubtitleIndicatorColor(){
        //    set background color
        GradientDrawable gradientDrawable = (GradientDrawable) viewSubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }

    private void selectImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();
            }
            else {
                Toast.makeText(this, "Permissions Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if(data != null){
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null){
                     try {
                             InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                             Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                             noteImageView.setImageBitmap(bitmap);
                             noteImageView.setVisibility(View.VISIBLE);

                             findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);

                             selectedImagePath = getPathFromURi(selectedImageUri);
                     }
                     catch (Exception exception){
                         Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                     }
                }
            }
        }
    }

    private String getPathFromURi(Uri contentUri){
        String filePath;
        Cursor cursor = getContentResolver().query(contentUri, null, null,null,null);
        if (cursor == null ){
            filePath = contentUri.getPath();
        }else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }


    private void showAddUrlDialog(){
        if (dialogAddUrl == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(CreateNoteActivity.this)
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
                        textViewWebUrl.setText(inputUrl.getText().toString());
                        linearLayoutWebUrl.setVisibility(View.VISIBLE);
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