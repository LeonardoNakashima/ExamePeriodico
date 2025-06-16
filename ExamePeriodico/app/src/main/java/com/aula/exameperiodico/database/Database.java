package com.aula.exameperiodico.database;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

public class Database {

    private static FirebaseFirestore db;

    public static void init() {
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }
        else {
            Log.d(TAG, "Erro na iniciação do database");
        }
    }

    public static FirebaseFirestore getDatabase() {
        if (db == null) init();
        return db;
    }
}
