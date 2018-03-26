/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.udacity.friendlychat;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.provider.EmailProvider;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LastMessages.LastMessagesListener, MessageFragment.MessageFragmentFragmentInteractionListener {

    public static final String ANONYMOUS = "anonymous";
    private static final String TAG = "MainActivity";

    private static final int RC_SIGN_IN = 123;
    public static String userEmail;
    public static android.support.v4.app.Fragment fragment;
    public ProgressBar mProgressBar;
    public String mUsername;
    DatabaseReference userNode;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user1 = firebaseAuth.getCurrentUser();
                if (user1 != null) {
                    Log.d(TAG, "user is onAuthState " + user1.getEmail());
                    userEmail = user1.getEmail();
                    mUsername = user1.getDisplayName();
                    Log.d(TAG, " fragment is " + fragment);

                    if (fragment instanceof MessageFragment) {

                    } else if (fragment instanceof LastMessages) {

                    } else if (fragment == null) {
                        if (userEmail != null) {
                            fragment = (android.support.v4.app.Fragment) LastMessages.newInstance(userEmail);
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, fragment, "messages")
                                    .commitAllowingStateLoss();
                        }

                    }

                } else {
                    OnSignedOutCleanup();

                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.EmailBuilder().build(),
                            new AuthUI.IdpConfig.GoogleBuilder().build()
                    );
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(providers)
                                    .setIsSmartLockEnabled(false)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    private void OnSignedOutCleanup() {
        if (fragment instanceof LastMessages) {
            if (userEmail != null) {
                Log.d(TAG, " Inside OnsignedOutCleanup ");
                fragment = (android.support.v4.app.Fragment) LastMessages.newInstance(userEmail);
                getSupportFragmentManager().beginTransaction().remove(fragment)
                        .commitAllowingStateLoss();
                fragment = null;
            }
        }
        fragment = null;
        Log.d(TAG, "Inside onSignedOutCleanup fragment" + fragment);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, " onActivityResult photo picker" +
                "resultCode is " + resultCode + " RequestCode is " + requestCode);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                Toast.makeText(this,"user is "+user.getEmail(),Toast.LENGTH_LONG).show();
                Log.d(TAG, "user is OnAcitivtyResult " + user.getEmail());
                mUsername = user.getDisplayName();
                userEmail = user.getEmail();
                checkUserPresent();
                // ...
            } else if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mFirebaseAuth != null)
            mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFirebaseAuth != null && mAuthStateListener != null)
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
//        mMessageAdapter.clear();
    }

    public void checkUserPresent() {
        userNode = mFirebaseDatabase.getReference().child("users");
        userNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot != null && snapshot.hasChild(userEmail.replace('.', ','))) {
                    // run some code
                    Log.d(TAG, "In checkUserPresent user is " + userEmail + " user already exists!");
                } else {
                    createUserInDatabase();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, " In checkUserPresent Error is " + databaseError);
                createUserInDatabase();
            }
        });
    }

    private void createUserInDatabase() {
        User user = new User(mUsername, userEmail);
        mFirebaseDatabase.getReference().child("users").child(userEmail.replace('.', ','))
                .setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, " In createUserInDatabase user is created " + userEmail);
            }
        });
    }

    @Override
    public void moveToContacts() {
        fragment = (android.support.v4.app.Fragment) AllContacts.newInstance(userEmail);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, fragment)
                .commitAllowingStateLoss();
    }


    public long getTimestamp() {
        final long timeStamp = (new Date()).getTime();
        return timeStamp;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    public void moveToMessage(String chatId, String messagePersonName) {
        fragment = (android.support.v4.app.Fragment) MessageFragment.newInstance(chatId, messagePersonName);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, fragment)
                .commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        if (fragment instanceof AllContacts) {
            fragment = (android.support.v4.app.Fragment) LastMessages.newInstance(userEmail);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, fragment, "message")
                    .commitAllowingStateLoss();
        } else if (fragment instanceof MessageFragment) {
            fragment = (android.support.v4.app.Fragment) LastMessages.newInstance(userEmail);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, fragment, "message")
                    .commitAllowingStateLoss();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fragment = null;
    }
}
