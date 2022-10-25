package com.example.quicknotes.Listeners;

import com.example.quicknotes.Entities.Note;

public interface NoteListener {

    void onNoteClicked(Note note, int position);
}
