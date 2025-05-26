package com.example.campusgo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RoomResetReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String room = intent.getStringExtra("room");
        if (room == null) return;

        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(room);
        roomRef.child("availability").setValue("Available");
        roomRef.child("markedBy").setValue("");
        roomRef.child("startTime").setValue("");
        roomRef.child("endTime").setValue("");
        roomRef.child("markedAt").setValue("");

        Log.d("RoomResetReceiver", "Room " + room + " reset to Available due to overtime.");
    }
}
