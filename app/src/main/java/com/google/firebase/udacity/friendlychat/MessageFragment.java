package com.google.firebase.udacity.friendlychat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.udacity.friendlychat.MainActivity.ANONYMOUS;
import static com.google.firebase.udacity.friendlychat.MainActivity.userEmail;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link MessageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MessageFragment extends Fragment {
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int RC_PHOTO_PICKER = 234;
    public static String TAG = MessageFragment.class.getSimpleName();
    @BindView(R.id.messageListView)
    RecyclerView recyclerView;
    List<FriendlyMessage> friendlyMessages;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private MessageFragmentFragmentInteractionListener mListener;
    private View view;
    //    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private ProgressBar mProgressBar;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;
    private String mUsername;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReference;
    private ChildEventListener mChildEventListener;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatPhotosStorageReference;
    private MainActivity mainActivity;

    public MessageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment MessageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MessageFragment newInstance(String param1, String param2) {
        MessageFragment fragment = new MessageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment\
        view = inflater.inflate(R.layout.fragment_message, container, false);
        ButterKnife.bind(this, view);
        mUsername = ANONYMOUS;
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        Log.d(TAG, " ChatID is " + mParam1);
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("messages").child(mParam1);
        mChatPhotosStorageReference = mFirebaseStorage.getReference().child("chat_photos");

        // Initialize references to views
//        mMessageListView = (ListView) view.findViewById(R.id.messageListView);
        mPhotoPickerButton = (ImageButton) view.findViewById(R.id.photoPickerButton);
        mMessageEditText = (EditText) view.findViewById(R.id.messageEditText);
        mSendButton = (Button) view.findViewById(R.id.sendButton);

        // Initialize message ListView and its adapter
        friendlyMessages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(getContext(), friendlyMessages, mParam1);
//        mMessageListView.setAdapter(mMessageAdapter);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setStackFromEnd(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(mMessageAdapter);
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Fire an intent to show an image picker
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        mainActivity = (MainActivity) getActivity();
        changeBanner();
        mUsername = mainActivity.mUsername;
//        assert mainActivity != null;
//        mainActivity.getSupportActionBar().setTitle(mUsername);
        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Send messages on click\
                final String msgContent = mMessageEditText.getText().toString();
                final String timeStamp = mainActivity != null ? "" + mainActivity.getTimestamp() : "";
                FriendlyMessage friendlyMessage = new FriendlyMessage(msgContent, mUsername, null, userEmail, timeStamp);
                mMessagesDatabaseReference.push().setValue(friendlyMessage)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mFirebaseDatabase.getReference().child("chats")
                                        .child(mParam1)
                                        .setValue(new chatIdModel(msgContent, timeStamp, mUsername))
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, " set to lastMessage Node Success");
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, " set to lastMessage Node Failed");
                                    }
                                });
                            }
                        });
                // Clear input box
                mMessageEditText.setText("");
            }
        });
        if (mainActivity == null)
            mainActivity = (MainActivity) getActivity();
        mainActivity.mProgressBar.setVisibility(View.GONE);
        attachDatabaseReadListener();

        return view;
    }

    public void changeBanner() {
        final String[] msgBanner = new String[1];
        DatabaseReference userNode;
        userNode = mFirebaseDatabase.getReference().child("users").child(mParam2.replace('.', ',')).child("username");
        userNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot != null) {
                    // run some code
                    Log.d(TAG, "In checkUserPresent user is " + mParam2 + " user already exists!");
                    if (snapshot.getValue() != null)
                        msgBanner[0] = snapshot.getValue().toString();
                    Log.d(TAG, " bannerName is " + msgBanner[0]);
                    if (mainActivity != null && mainActivity.getSupportActionBar() != null)
                        mainActivity.getSupportActionBar().setTitle(msgBanner[0]);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, " In checkUserPresent Error is " + databaseError);
                /*mFirebaseDatabase.getReference().push().setValue("users").addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        createUserInDatabase();
                    }
                });*/
            }
        });

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MessageFragmentFragmentInteractionListener) {
            mListener = (MessageFragmentFragmentInteractionListener) context;
        } /*else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.d(TAG, " Message onChildAdded " + dataSnapshot);

                    FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
                    friendlyMessage.setKey(dataSnapshot.getKey());
                    friendlyMessages.add(friendlyMessage);
                    mMessageAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, " On child Removed Called ");
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void messageCleanup() {
        mUsername = ANONYMOUS;
        friendlyMessages.clear();
        mMessageAdapter.notifyDataSetChanged();
        detachDatabaseReadListener();
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mMessagesDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, " OnActivityResult called");
        if (requestCode == RC_PHOTO_PICKER) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUrl = data.getData();
                Log.d(TAG, " Image Uri is " + selectedImageUrl);
                if (selectedImageUrl != null) {
                    StorageReference photoRef = mChatPhotosStorageReference.child(selectedImageUrl.getLastPathSegment());
                    photoRef.putFile(selectedImageUrl).addOnSuccessListener((Activity) getContext(), new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            final String timeStamp = mainActivity != null ? "" + mainActivity.getTimestamp() : "";
                            FriendlyMessage friendlyMessage = new FriendlyMessage(null, mUsername, downloadUrl.toString(), userEmail, timeStamp);
                            mMessagesDatabaseReference.push().setValue(friendlyMessage).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFirebaseDatabase.getReference().child("chats")
                                            .child(mParam1)
                                            .setValue(new chatIdModel("photo", timeStamp, mUsername));
                                }
                            });

                        }
                    }).addOnFailureListener((Activity) getContext(), new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, " Image Upload Failed " + e);
                        }
                    });
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        messageCleanup();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface MessageFragmentFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
