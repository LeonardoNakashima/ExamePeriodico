package com.aula.exameperiodico.database;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

public class Database {

    private static FirebaseFirestore db;
    private static FirebaseAuth auth;

    public static void init() {
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }
        else {
            Log.d(TAG, "Erro na iniciação do database");
        }
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        else {
            Log.d(TAG, "Erro na iniciação do autenticação");
        }
    }

    public static FirebaseFirestore getDatabase() {
        if (db == null) init();
        return db;
    }

    public static FirebaseAuth getAuth() {
        if (auth == null) init();
        return auth;
    }
}
