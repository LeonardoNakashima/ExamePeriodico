package com.aula.exameperiodico.database.admin;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.aula.exameperiodico.database.Database;
import com.aula.exameperiodico.database.colaborador.Colaborador;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

public class AdminDAO {

    public void cadastrarAdmin(Admin argAdmin, String password, Context c) {
        if (argAdmin.getNumCracha() == 0){
            Database.getDatabase()
                    .collection("admins").document("admins_id")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            int numCracha = 1;
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists() && document.getLong("numCracha") != null) {
                                    numCracha = document.getLong("numCracha").intValue() + 1;
                                }
                            }
                            argAdmin.setNumCracha(numCracha);

                            String email = numCracha + "@exameperiodico.com";

                            if (password == null || password.isEmpty() || password.length() < 6) {
                                Toast.makeText(c, "Senha inválida. Deve ter no mínimo 6 caracteres.", Toast.LENGTH_LONG).show();
                                return;
                            }

                            Database.getAuth().createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(authTask -> {
                                        if (authTask.isSuccessful()) {
                                            FirebaseUser user = Database.getAuth().getCurrentUser();
                                            if (user != null) {
                                                user.sendEmailVerification()
                                                        .addOnCompleteListener(emailVerificationTask -> {
                                                            if (emailVerificationTask.isSuccessful()) {
                                                                Toast.makeText(c, "Conta criada! Verifique seu e-mail: " + email, Toast.LENGTH_LONG).show();
                                                            } else {
                                                                Toast.makeText(c, "Erro ao enviar verificação de e-mail.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }

                                            argAdmin.setEmail(email);

                                            Database.getDatabase()
                                                    .collection("admins")
                                                    .document(String.valueOf(argAdmin.getNumCracha()))
                                                    .set(argAdmin)
                                                    .addOnCompleteListener(firestoreTask -> {
                                                        if (firestoreTask.isSuccessful()) {
                                                            Toast.makeText(c, "Dados do admin salvos no Firestore.", Toast.LENGTH_SHORT).show();
                                                            Database.getDatabase().collection("admins").document("admins_id")
                                                                    .update("numCracha", argAdmin.getNumCracha())
                                                                    .addOnFailureListener(e -> Toast.makeText(c, "Erro ao atualizar contador de crachá: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                                        } else {
                                                            Toast.makeText(c, "Erro ao salvar dados do admin no Firestore: " + firestoreTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                            if (user != null) {
                                                                user.delete().addOnCompleteListener(deleteTask -> {
                                                                    if (deleteTask.isSuccessful()) {
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });

                                        } else {
                                            String errorMessage = "Erro ao criar conta: " + authTask.getException().getMessage();
                                            Toast.makeText(c, errorMessage, Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    });
        } else {
            Database.getDatabase()
                    .collection("admins").document(argAdmin.getNumCracha() + "")
                    .set(argAdmin)
                    .addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Toast.makeText(c, "Dados do admin atualizados no Firestore.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(c, "Erro ao atualizar dados do admin: " + updateTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }

        Database.getDatabase()
                .collection("admins").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Colaborador> colaboradores = task.getResult().toObjects(Colaborador.class);
                    } else {
                        Toast.makeText(c, "Erro ao carregar lista de admins: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void removerAdmin(Admin argAdmin, Context c) {
        FirebaseStorage.getInstance().getReference("admin_images/" + argAdmin.getNumCracha() + ".jpg")
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Database.getDatabase()
                            .collection("admins").document(String.valueOf(argAdmin.getNumCracha()))
                            .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(c, "Admin e imagem (se existia) removidos com sucesso.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(c, "Erro ao remover admin: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(c, "Aviso: Não foi possível remover a imagem (pode não existir ou erro de permissão). Removendo apenas o admin.", Toast.LENGTH_LONG).show();
                    Database.getDatabase()
                            .collection("admins").document(String.valueOf(argAdmin.getNumCracha()))
                            .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(c, "Admin removido com sucesso.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(c, "Erro ao remover admin: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                });
    }

    public void login(String email, String password, Context c){
        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(c, "Email e senha são obrigatórios para login.", Toast.LENGTH_LONG).show();
            return;
        }

        Database.getAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = Database.getAuth().getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            Toast.makeText(c, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();
                        } else if (user != null && !user.isEmailVerified()) {
                            Toast.makeText(c, "Verifique seu e-mail antes de continuar.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(c, "Usuário não encontrado ou erro inesperado.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(c, "Erro no login: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
