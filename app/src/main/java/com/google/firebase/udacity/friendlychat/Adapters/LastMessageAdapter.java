package com.google.firebase.udacity.friendlychat.Adapters;

import android.content.Context;
import android.icu.text.LocaleDisplayNames;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.LoginFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.udacity.friendlychat.AllContacts;
import com.google.firebase.udacity.friendlychat.LastMessage;
import com.google.firebase.udacity.friendlychat.MainActivity;
import com.google.firebase.udacity.friendlychat.MessageFragment;
import com.google.firebase.udacity.friendlychat.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.MIDI_SERVICE;
import static com.google.firebase.udacity.friendlychat.MainActivity.userEmail;

/**
 * Created by sandeep on 25-03-2018.
 */

public class LastMessageAdapter extends RecyclerView.Adapter<LastMessageAdapter.ViewHolder> {

    public static final String TAG = LastMessageAdapter.class.getSimpleName();
    public String chatId;
    Context context;
    List<String> nameList;

    public LastMessageAdapter(Context context, List<String> nameList) {
        this.context = context;
        this.nameList = nameList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.name_in_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Log.d(TAG, " nameList is " + nameList);
        holder.nameInListView.setText(nameList.get(position));
        holder.nameInListView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) context;
                if (mainActivity != null)
                    mainActivity.mProgressBar.setVisibility(View.VISIBLE);
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

                final DatabaseReference databaseReference = firebaseDatabase.
                        getReference("users/" + userEmail.replace('.', ',') + "/chats/" + nameList.
                                get(position).replace('.', ','));
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, " onChildAdded datasnapshot is " + dataSnapshot);
                        if (dataSnapshot.getValue() != null) {
                            Log.d(TAG, " ChildId is already created " + dataSnapshot.getValue());
                            chatId = dataSnapshot.getValue().toString();
                            MainActivity mainActivity = (MainActivity) context;
                            mainActivity.moveToMessage(chatId, nameList.get(position));

                        } else {
                            createNewChatId(nameList.get(position));
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, " onChildAdded Chat databaseError is " + databaseError);
                    }
                });
            }
        });
    }

    private void createNewChatId(final String userName) {
        MainActivity mainActivity = (MainActivity) context;

        Log.d(TAG, " Inside createNewChatId(), Creating new chatId");
        FirebaseDatabase.getInstance().getReference().child("chats").push()
                .setValue(new LastMessage("", "", "" + mainActivity.getTimestamp()), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseReference.getKey() != null) {
                            Log.d(TAG, " Inside createNewChatId onComplete(), Successfully created ChatId " + databaseReference.getKey());
                            chatId = databaseReference.getKey();
                            settingChatIDToallEmails(userName, databaseReference.getKey());
                            MainActivity mainActivity = (MainActivity) context;

                        }
                    }
                });
    }

    private void settingChatIDToallEmails(final String userNames, String chatId) {

        FirebaseDatabase.getInstance().getReference().child("users")
                .child(userEmail.replace('.', ',')).child("chats")
                .child(userNames.replace('.', ','))
                .setValue(chatId, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseReference.getKey() != null) {
                            Log.d(TAG, " Inside createNewChatId onComplete()," +
                                    " Successfully created ChatId in userEmail " + userEmail);
                        }
                    }
                });
        FirebaseDatabase.getInstance().getReference().child("users")
                .child(userNames.replace('.', ',')).child("chats")
                .child(userEmail.replace(('.'), ','))
                .setValue(chatId, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseReference.getKey() != null) {
                            Log.d(TAG, " Inside createNewChatId onComplete()," +
                                    " Successfully created ChatId in userEmail " + userNames);
                        }
                    }
                });
//        }
        MainActivity mainActivity = (MainActivity) context;
        mainActivity.moveToMessage(chatId, userNames);
    }

    @Override
    public int getItemCount() {
        return nameList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameInListView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameInListView = itemView.findViewById(R.id.name_in_list_view);
        }
    }

}