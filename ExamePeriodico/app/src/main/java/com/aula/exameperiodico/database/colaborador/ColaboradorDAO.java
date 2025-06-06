package com.aula.exameperiodico.database.colaborador;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aula.exameperiodico.database.Database;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.List;
import java.util.HashMap;

public class ColaboradorDAO {

    public interface ImageUrlCallback {
        void onSuccess(String imageUrl);
        void onFailure(Exception e);
    }

    public interface ColaboradoresListCallback {
        void onColaboradoresLoaded(List<Colaborador> colaboradores);
        void onFailure(Exception e);
    }

    public void cadastrarColaborador(Colaborador argColaborador, Context c) {
        if (argColaborador.getNumCracha() == 0){
            Database.getDatabase()
                    .collection("exames").document("exames_id")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            int numCracha = 1;
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Long lastNumCracha = document.getLong("numCracha");
                                    if (lastNumCracha != null) {
                                        numCracha = lastNumCracha.intValue() + 1;
                                    }
                                }
                            }
                            argColaborador.setNumCracha(numCracha);

                            HashMap<String, Object> updates = new HashMap<>();
                            updates.put("numCracha", argColaborador.getNumCracha());

                            Database.getDatabase().collection("exames").document("exames_id")
                                    .set(updates)
                                    .addOnFailureListener(e -> Toast.makeText(c, "Erro ao atualizar contador de crachá: " + e.getMessage(), Toast.LENGTH_SHORT).show());


                            Database.getDatabase()
                                    .collection("exames")
                                    .document(String.valueOf(argColaborador.getNumCracha()))
                                    .set(argColaborador)
                                    .addOnSuccessListener(aVoid -> Toast.makeText(c, "Colaborador cadastrado com sucesso!", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(c, "Erro ao cadastrar colaborador: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    });
        } else {
            Database.getDatabase()
                    .collection("exames").document(argColaborador.getNumCracha() + "")
                    .set(argColaborador)
                    .addOnSuccessListener(aVoid -> Toast.makeText(c, "Colaborador atualizado com sucesso!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(c, "Erro ao atualizar colaborador: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
        Database.getDatabase()
                .collection("exames").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Colaborador> colaboradores = task.getResult().toObjects(Colaborador.class);
                    } else {
                        Toast.makeText(c, "Erro ao buscar lista de colaboradores: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private static int getNumCracha(@NonNull Task<DocumentSnapshot> task, Colaborador argColaborador) {
        int numCracha = argColaborador.getNumCracha();
        if (task.isSuccessful()) {
            DocumentSnapshot document = task.getResult();
            if (document.exists()) {
                Long lastNumCracha = document.getLong("numCracha");
                if (lastNumCracha != null) {
                    numCracha = lastNumCracha.intValue() + 1;
                }
            }
        }
        return numCracha;
    }

    public void removerColaborador(Colaborador argColaborador, Context c) {
        FirebaseStorage.getInstance().getReference("colaborador_images/" + argColaborador.getNumCracha() + ".jpg")
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Database.getDatabase()
                            .collection("exames").document(String.valueOf(argColaborador.getNumCracha()))
                            .delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(c, "Colaborador e imagem (se existia) removidos com sucesso!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(c, "Erro ao remover colaborador: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(c, "Aviso: Não foi possível remover a imagem (pode não existir ou erro de permissão). Removendo apenas o colaborador.", Toast.LENGTH_LONG).show();
                    Database.getDatabase()
                            .collection("exames").document(String.valueOf(argColaborador.getNumCracha()))
                            .delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(c, "Colaborador removido com sucesso!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(c, "Erro ao remover colaborador: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                });
    }

    public void listarColaborador(final ColaboradoresListCallback callback) {
        Database.getDatabase()
                .collection("exames")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            System.err.println("Erro ao listar colaboradores: " + error.getMessage());
                            callback.onFailure(error);
                            return;
                        }
                        assert value != null;

                        List<Colaborador> colaboradores = value.toObjects(Colaborador.class);
                        callback.onColaboradoresLoaded(colaboradores);
                        System.out.println("Lista de colaboradores atualizada. Total: " + colaboradores.size());
                    }
                });
    }

    public void buscarImagemColaborador(int numCracha, final ImageUrlCallback callback) {
        StorageReference imageRef = FirebaseStorage.getInstance().getReference()
                .child("colaborador_images")
                .child(numCracha + ".jpg");

        imageRef.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        callback.onSuccess(uri.toString());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        System.err.println("Erro ao buscar imagem para o crachá " + numCracha + ": " + exception.getMessage());
                        callback.onFailure(exception);
                    }
                });
    }

    public void uploadImagemColaborador(int numCracha, Uri imageUri, Context c, final ImageUrlCallback callback) {
        if (imageUri == null) {
            callback.onFailure(new IllegalArgumentException("URI da imagem não pode ser nula."));
            return;
        }

        StorageReference imageRef = FirebaseStorage.getInstance().getReference()
                .child("colaborador_images")
                .child(numCracha + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Toast.makeText(c, "Imagem enviada com sucesso!", Toast.LENGTH_SHORT).show();
                                callback.onSuccess(uri.toString());
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(c, "Erro ao obter URL da imagem após o upload: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                callback.onFailure(e);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(c, "Erro ao enviar imagem: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        callback.onFailure(e);
                    }
                });
    }
}
