package com.google.firebase.udacity.friendlychat;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.udacity.friendlychat.Adapters.LastAdapter;
import com.google.firebase.udacity.friendlychat.Adapters.LastMessageAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.google.firebase.udacity.friendlychat.MainActivity.userEmail;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LastMessages.LastMessagesListener} interface
 * to handle interaction events.
 * Use the {@link LastMessages#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LastMessages extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static String TAG = LastMessages.class.getSimpleName();
    public MainActivity mainActivity;
    public ArrayList<String> nameList = new ArrayList<>();
    public LastAdapter lastAdapter;
    ArrayList<LastMessage> lmsg = new ArrayList<>();
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    View view;
    LastMessageAdapter lastMessageAdapter;
    HashMap<String, String> hashMap = new HashMap<>();
    private DatabaseReference mLastMessageReference;
    private ChildEventListener mChildEventListener;
    private FirebaseDatabase mFirebaseDatabase;
    //    // TODO: Rename and change types of parameters
    private String mParam1;
    //    private String mParam2;
    private LastMessagesListener mListener;

    public LastMessages() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment LastMessages.
     */
    // TODO: Rename and change types and number of parameters
    public static LastMessages newInstance(String param1) {
        LastMessages fragment = new LastMessages();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_last_messages, container, false);
        ButterKnife.bind(this, view);

        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);       // for linear
        nameList = new ArrayList<String>();
        lmsg = new ArrayList<>();
        lastAdapter = new LastAdapter(getContext(), lmsg);
        mRecyclerView.setAdapter(lastAdapter);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mLastMessageReference = mFirebaseDatabase.getReference().child("users")
                .child(userEmail.replace('.', ',')).child("chats");
        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mListener.moveToContacts();
            }
        });
        mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        mainActivity.getSupportActionBar().setTitle("Recent Chats");
        return view;
    }

    public void checkNodeExist() {
        mLastMessageReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "In lastDatabase Messages is" + dataSnapshot);
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    attachDatabaseReadListener();
                } else {
                    if (mainActivity == null)
                        mainActivity = (MainActivity) getActivity();
                    mainActivity.mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "In lastDatabase Messages Error is" + databaseError);

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        checkNodeExist();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        detachDatabaseReadListener();
    }

    public void detachDatabaseReadListener() {
        Log.d(TAG, " Inside detachDatabaseReadListener lmg is " + lmsg
                + " lastAdapter is " + lastAdapter + " mChildEventList is " + mChildEventListener);
        if (lmsg != null)
            lmsg.clear();
        if (lastAdapter != null)
            lastAdapter.notifyDataSetChanged();
        if (mChildEventListener != null) {
            mLastMessageReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    public void attachDatabaseReadListener() {
        Log.d(TAG, " in attachDatabaseReadListener mChildEventReadListener " + mChildEventListener);
        if (mChildEventListener == null) {
            nameList.clear();
            lmsg.clear();
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                    FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
//                    mMessageAdapter.add(friendlyMessage);
                    Log.d(TAG, "In lastDatabase Inside Onchild added Messages is" + dataSnapshot);
                    mainActivity = (MainActivity) getActivity();
                    if (mainActivity != null) {
                        mainActivity.mProgressBar.setVisibility(View.GONE);
                    }
//                    if (!dataSnapshot.getKey().equalsIgnoreCase(userEmail.replace('.',',')))
                    nameList.add(dataSnapshot.getValue().toString());
//                        hashMap.put(dataSnapshot.getValue().toString(),dataSnapshot.getKey());
                    getAllLastMessages(dataSnapshot.getValue().toString(), dataSnapshot.getKey());

//                    lastMessageAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "In lastDatabase Messages Error is" + databaseError);
                }
            };
            mLastMessageReference.addChildEventListener(mChildEventListener);
        }
    }

    private void getAllLastMessages(String key, final String name) {

        mFirebaseDatabase.getReference().child("chats").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LastMessage lastMessage = dataSnapshot.getValue(LastMessage.class);
                if (lastMessage != null) {
                    lastMessage.setKey(dataSnapshot.getKey());
                    lastMessage.setTitle(name.replace(',', '.'));
                }
                lmsg.add(lastMessage);
                lastAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
//        }

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LastMessagesListener) {
            mListener = (LastMessagesListener) context;
        }/* else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface LastMessagesListener {
        // TODO: Update argument type and name
        void moveToContacts();
    }
}
