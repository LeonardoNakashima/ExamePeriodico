package com.aula.exameperiodico.database;

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
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
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
