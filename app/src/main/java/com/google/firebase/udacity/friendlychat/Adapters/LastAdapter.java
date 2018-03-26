package com.google.firebase.udacity.friendlychat.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.udacity.friendlychat.LastMessage;
import com.google.firebase.udacity.friendlychat.MainActivity;
import com.google.firebase.udacity.friendlychat.R;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.google.firebase.udacity.friendlychat.MainActivity.userEmail;


public class LastAdapter extends RecyclerView.Adapter<LastAdapter.ViewHolder> {

    public static final String TAG = LastAdapter.class.getSimpleName();
    public static String messagePersonName;
    public String chatId;
    Context context;
    List<LastMessage> nameList;

    public LastAdapter(Context context, List<LastMessage> nameList) {
        this.context = context;
        this.nameList = nameList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.single_element_in_last_messages, parent, false);
        return new ViewHolder(view);
    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("hh:mm dd-MM-yyyy", cal).toString();
        return date;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Log.d(TAG, " nameList is " + nameList);
        holder.nameInListView.setText(nameList.get(position).getTitle());
        try {
            holder.dateInList.setText(getDate(Long.parseLong(nameList.get(position).getTimestamp())));

        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.textInList.setText(nameList.get(position).getLastMessage());
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) context;
                if (mainActivity != null)
                    mainActivity.mProgressBar.setVisibility(View.VISIBLE);
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                chatId = nameList.get(position).getKey();
                messagePersonName = nameList.get(position).getTitle();
                if (mainActivity != null) {
                    mainActivity.moveToMessage(chatId, nameList.get(position).getTitle());
                    if (mainActivity.getSupportActionBar() != null)
                        mainActivity.getSupportActionBar().setTitle("");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return nameList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameInListView, dateInList, textInList;
        LinearLayout linearLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            linearLayout = itemView.findViewById(R.id.list_item_list);
            nameInListView = itemView.findViewById(R.id.name_in_last);
            dateInList = itemView.findViewById(R.id.date_in_last);
            textInList = itemView.findViewById(R.id.text_in_last);
        }
    }

}