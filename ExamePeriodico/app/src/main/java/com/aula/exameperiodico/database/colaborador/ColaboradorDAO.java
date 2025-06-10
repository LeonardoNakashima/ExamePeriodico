package com.aula.exameperiodico.database.colaborador;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.aula.exameperiodico.database.Database;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

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
        if (argColaborador.getNumCracha() == 0){ // Novo colaborador, precisa de um novo crachá
            // Primeiro, obtem o contador do crachá
            Database.getDatabase()
                    .collection("exames").document("exames_id")
                    .get()
                    .addOnCompleteListener(task -> {
                        int numCracha = 1; // Valor padrão para o primeiro crachá
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Long lastNumCracha = document.getLong("numCracha");
                                if (lastNumCracha != null) {
                                    numCracha = lastNumCracha.intValue() + 1;
                                }
                            }
                        }
                        // Define o novo número do crachá para o colaborador
                        argColaborador.setNumCracha(numCracha);

                        // Atualiza o contador de crachás no Firestore
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("numCracha", argColaborador.getNumCracha());

                        Database.getDatabase().collection("exames").document("exames_id")
                                .set(updates, SetOptions.merge()) // Usa merge para não sobrescrever outros campos se existirem
                                .addOnSuccessListener(aVoid -> {
                                    // Se o contador foi atualizado com sucesso, salva o colaborador
                                    Database.getDatabase()
                                            .collection("exames")
                                            .document(String.valueOf(argColaborador.getNumCracha()))
                                            .set(argColaborador) // Usa o objeto colaborador completo
                                            .addOnSuccessListener(aVoid2 -> Toast.makeText(c, "Colaborador cadastrado com sucesso! Crachá: " + argColaborador.getNumCracha(), Toast.LENGTH_SHORT).show())
                                            .addOnFailureListener(e -> Toast.makeText(c, "Erro ao cadastrar colaborador: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                })
                                .addOnFailureListener(e -> Toast.makeText(c, "Erro ao atualizar contador de crachá: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    });
        } else { // Colaborador existente, apenas atualiza
            Database.getDatabase()
                    .collection("exames").document(argColaborador.getNumCracha() + "")
                    .set(argColaborador)
                    .addOnSuccessListener(aVoid -> Toast.makeText(c, "Colaborador atualizado com sucesso!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(c, "Erro ao atualizar colaborador: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
        // A chamada ".get()" redundante no final foi removida.
    }


    public void removerColaborador(Colaborador argColaborador, Context c) {
        FirebaseStorage.getInstance().getReference("colaborador_images/" + argColaborador.getNumCracha() + ".jpg")
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Database.getDatabase()
                            .collection("exames").document(String.valueOf(argColaborador.getNumCracha()))
                            .delete()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(c, "Colaborador e imagem (se existia) removidos com sucesso!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(c, "Erro ao remover colaborador: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(c, "Aviso: Não foi possível remover a imagem (pode não existir ou erro de permissão). Removendo apenas o colaborador.", Toast.LENGTH_LONG).show();
                    Database.getDatabase()
                            .collection("exames").document(String.valueOf(argColaborador.getNumCracha()))
                            .delete()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(c, "Colaborador removido com sucesso!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(c, "Erro ao remover colaborador: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
                .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                .addOnFailureListener(exception -> {
                    System.err.println("Erro ao buscar imagem para o crachá " + numCracha + ": " + exception.getMessage());
                    callback.onFailure(exception);
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
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Toast.makeText(c, "Imagem enviada com sucesso!", Toast.LENGTH_SHORT).show();
                        callback.onSuccess(uri.toString());
                    }).addOnFailureListener(e -> {
                        Toast.makeText(c, "Erro ao obter URL da imagem após o upload: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        callback.onFailure(e);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(c, "Erro ao enviar imagem: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    callback.onFailure(e);
                });
    }
}
