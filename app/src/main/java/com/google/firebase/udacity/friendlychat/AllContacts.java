package com.google.firebase.udacity.friendlychat;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.firebase.udacity.friendlychat.Adapters.LastMessageAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AllContacts.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AllContacts#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllContacts extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static String TAG = AllContacts.class.getSimpleName();
    public ArrayList<String> nameList;
    @BindView(R.id.recycler_view_in_allcontacts)
    RecyclerView mRecyclerView;
    View view;
    LastMessageAdapter lastMessageAdapter;
    DatabaseReference databaseReference;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private FirebaseDatabase firebaseDatabase;
    private ChildEventListener mChildEventListener;

    public AllContacts() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment AllContacts.
     */
    // TODO: Rename and change types and number of parameters
    public static AllContacts newInstance(String param1) {
        AllContacts fragment = new AllContacts();
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
        view = inflater.inflate(R.layout.fragment_all_contacts, container, false);
        ButterKnife.bind(this, view);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);       // for linear
        nameList = new ArrayList<String>();
        lastMessageAdapter = new LastMessageAdapter(getContext(), nameList);
        mRecyclerView.setAdapter(lastMessageAdapter);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        attachDatabaseReadListener();
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        mainActivity.getSupportActionBar().setTitle("Contacts");
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
        detachDatabaseReadListener();
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                    FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
//                    nameList.add(friendlyMessage);
                    Log.d(TAG, " IN All contacts " + dataSnapshot);
                    if (!dataSnapshot.getKey().equalsIgnoreCase(mParam1.replace('.', ',')))
                        nameList.add(dataSnapshot.getKey().replace(",", "."));
                    lastMessageAdapter.notifyDataSetChanged();
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

                }
            };
//            databaseReference.child("users").child(mParam1.replace('.',',')).addChildEventListener(mChildEventListener);
            databaseReference.child("users").addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        nameList.clear();
        if (mChildEventListener != null) {
            databaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
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
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
