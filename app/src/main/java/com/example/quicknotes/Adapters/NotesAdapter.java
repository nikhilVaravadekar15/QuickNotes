package com.example.quicknotes.Adapters;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quicknotes.Entities.Note;
import com.example.quicknotes.Listeners.NoteListener;
import com.example.quicknotes.R;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewAdapter> {

    private List<Note> noteList;
    private NoteListener noteListener;
    private Timer timer;
    private List<Note> noteSourceList;

    public NotesAdapter(List<Note> noteList, NoteListener noteListener) {
        this.noteList = noteList;
        this.noteListener = noteListener;
        noteSourceList = noteList;
    }


    @NonNull
    @Override
    public NoteViewAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_note, parent, false);
        return new NoteViewAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewAdapter holder, int position) {
//        holder.setNote(noteList.get(position));
        Note currentNote = noteList.get(position);

        holder.textView_title.setText(currentNote.getTitle());
        if (currentNote.getSubtitle().trim().isEmpty()) {
            holder.textView_subtitle.setVisibility(View.GONE);
        } else {
            holder.textView_subtitle.setText(currentNote.getSubtitle());
        }
        holder.textView_dateTime.setText(currentNote.getDateTime());
        GradientDrawable gradientDrawable = (GradientDrawable) holder.noteLinearLayout.getBackground();
        if (currentNote.getColor() != null) {
            gradientDrawable.setColor(Color.parseColor(currentNote.getColor()));
        } else {
            gradientDrawable.setColor(Color.parseColor("#333333"));
        }

        if (currentNote.getImagePath() != null) {
            holder.roundedImageView.setImageBitmap(BitmapFactory.decodeFile(currentNote.getImagePath()));
            holder.roundedImageView.setVisibility(View.VISIBLE);
        } else {
            holder.roundedImageView.setVisibility(View.GONE);
        }


        holder.noteLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteListener.onNoteClicked(currentNote, position);
            }
        });


    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NoteViewAdapter extends RecyclerView.ViewHolder {

        TextView textView_title, textView_subtitle, textView_dateTime;
        LinearLayout noteLinearLayout;
        RoundedImageView roundedImageView;

        public NoteViewAdapter(@NonNull View itemView) {
            super(itemView);
            textView_title = itemView.findViewById(R.id.textTitle);
            textView_subtitle = itemView.findViewById(R.id.textSubtitle);
            textView_dateTime = itemView.findViewById(R.id.textDateTime);
            noteLinearLayout = itemView.findViewById(R.id.linearLayout_note);
            roundedImageView = itemView.findViewById(R.id.imagePath);

        }

//    NoteViewAdapter END
    }


    public void searchNotes(final String searchKeyWord) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (searchKeyWord.trim().isEmpty()) {
                    noteList = noteSourceList;
                } else {
                    ArrayList<Note> tempNoteList = new ArrayList<>();
                    for (Note note : noteSourceList) {
                        if (note.getTitle().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
                                note.getSubtitle().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
                                note.getNoteText().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
                                note.getWebLink().contains(searchKeyWord)){
                            tempNoteList.add(note);
                        }
                    }
                    noteList = tempNoteList;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }, 750);
    }

    public void cancelTimer(){
        if (timer != null){
            timer.cancel();
        }
    }

}
