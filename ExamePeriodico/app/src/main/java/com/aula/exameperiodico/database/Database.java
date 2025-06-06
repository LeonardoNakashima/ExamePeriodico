package com.aula.exameperiodico.database;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
public class Database {

    private static FirebaseFirestore db;

    public static void init() {
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }
    }

    public static FirebaseFirestore getDatabase() {
        if (db == null) init();
        return db;
    }

}
