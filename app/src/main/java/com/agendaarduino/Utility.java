package com.agendaarduino;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Utility {

    public static CollectionReference getCollectionReferenceForEvents(){
        return FirebaseFirestore.getInstance().collection("events");
    }

    public static CollectionReference getCollectionReferenceForRoutines(){
        return FirebaseFirestore.getInstance().collection("routine");
    }

    public static CollectionReference getCollectionReferenceForLabels(){
        return FirebaseFirestore.getInstance().collection("labels");
    }

}
