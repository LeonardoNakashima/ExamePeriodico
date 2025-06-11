//package com.aula.exameperiodico.database.admin;
//
//import android.content.Context;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//
//import com.aula.exameperiodico.database.Database;
//import com.aula.exameperiodico.database.colaborador.Colaborador;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.firestore.DocumentSnapshot;
//
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.storage.FirebaseStorage;
//
//import java.util.List;
//
//public class AdminDAO {
//
//    public void login(String email, String password, Context c){
//        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
//            Toast.makeText(c, "Email e senha são obrigatórios para login.", Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        Database.getAuth().signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        FirebaseUser user = Database.getAuth().getCurrentUser();
//                        if (user != null && user.isEmailVerified()) {
//                            Toast.makeText(c, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();
//                        } else if (user != null && !user.isEmailVerified()) {
//                            Toast.makeText(c, "Verifique seu e-mail antes de continuar.", Toast.LENGTH_LONG).show();
//                        } else {
//                            Toast.makeText(c, "Usuário não encontrado ou erro inesperado.", Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        Toast.makeText(c, "Erro no login: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
//                    }
//                });
//    }
//}
