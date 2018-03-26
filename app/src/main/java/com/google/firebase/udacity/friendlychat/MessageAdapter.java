package com.google.firebase.udacity.friendlychat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;

import java.awt.font.TextAttribute;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final String TAG = MessageAdapter.class.getSimpleName();
    public static String messagePersonName;
    public String chatId;
    Context context;
    List<FriendlyMessage> objects;
    FriendlyMessage message;
    int finalPosition;

    public MessageAdapter(Context context, List<FriendlyMessage> objects, String chatId) {
        this.context = context;
        this.objects = objects;
        this.chatId = chatId;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_message, parent, false);
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
        Log.d(TAG, "objects is " + objects);
        message = objects.get(position);
        boolean isPhoto = message.getPhotoUrl() != null;
        if (isPhoto) {
            holder.messageTextView.setVisibility(View.GONE);
            holder.photoImageView.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(message.getPhotoUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.photoImageView);
        } else {
            holder.messageTextView.setVisibility(View.VISIBLE);
            holder.photoImageView.setVisibility(View.GONE);
            holder.messageTextView.setText(message.getText());
        }
        holder.photoImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        finalPosition = position;
        holder.photoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (message.getKey() != null && chatId != null && position >= 0)
                    makeAlert(message.getKey(), position);
            }
        });
        MainActivity mainActivity = (MainActivity) context;

        if (mainActivity.mUsername.equalsIgnoreCase(message.getName())) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.END;
            holder.messageTextView.setLayoutParams(params);
            holder.photoImageView.setLayoutParams(params);
        }

    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    private void makeAlert(final String key, final int finalPosition) {
        AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.todoDialogLight).create();
        alertDialog.setTitle("Delete Image");
        alertDialog.setMessage("Do you want to delete this image");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
//                        MessageFragment.deleteImage();
                        FriendlyMessage friendlyMessage = new FriendlyMessage();
                        FirebaseDatabase.getInstance().getReference().child("messages").child(chatId).child(key)
                                .setValue(friendlyMessage).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("Message Adapter ", " Delete Image Successs " + key + " position is " + finalPosition);
                                objects.remove(finalPosition);
                                notifyDataSetChanged();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("Message Adapter ", " Delete Image Failed! " + key);

                            }
                        });
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView, authorTextView;
        ImageView photoImageView;
        LinearLayout linearLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            photoImageView = (ImageView) itemView.findViewById(R.id.photoImageView);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            authorTextView = (TextView) itemView.findViewById(R.id.nameTextView);
        }
    }
}
