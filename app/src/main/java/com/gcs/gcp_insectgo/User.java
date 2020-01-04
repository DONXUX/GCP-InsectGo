package com.gcs.gcp_insectgo;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class User {
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private FirebaseUser user;
    private String email;


    User(){
        loadUser();
    }

    public String getEmail(){
        return email;
    }

    private void loadUser(){
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if(mAuth.getCurrentUser() != null)
            email = mAuth.getCurrentUser().getEmail();
    }

    public DatabaseReference getDatabaseReference(){
        return mDatabase;
    }
}
