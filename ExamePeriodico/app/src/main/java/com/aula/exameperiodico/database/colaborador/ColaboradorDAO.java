package com.aula.exameperiodico.database.colaborador;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aula.exameperiodico.database.Database;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class ColaboradorDAO {
    Database db = new Database();

    public void cadastrarColaborador(Colaborador argColaborador, Context c) {
        if (argColaborador.getNumCracha() == 0){
            // Obter id para nota
            db.getDatabase()
                    .collection("exames").document("exames_id")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            int numCracha = 1;
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    numCracha = document.getLong("numCracha").intValue() + 1;
                                }
                            }
                            argColaborador.setNumCracha(numCracha);
                            db.getDatabase()
                                    .collection("exames")
                                    .document(String.valueOf(argColaborador.getNumCracha()))
                                    .set(new Colaborador(argColaborador.getNumCracha(), argColaborador.getNome(), argColaborador.getDataInsercao(), argColaborador.getInicioAtendimento(), argColaborador.getFimAtendimento()));
                        }
                    });
            // Inserir nova nota
            db.getDatabase()
                    .collection("exames").document().set(argColaborador);
        } else {
            // Atualizar nota
            db.getDatabase()
                    .collection("exames").document(argColaborador.getNumCracha() + "")
                    .set(argColaborador);
        }
        db.getDatabase()
                .collection("exames").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Colaborador> colaboradores = task.getResult().toObjects(Colaborador.class);
            }
        });
    }

    public void removerColaborador(Colaborador argColaborador, Context c) {
        db.getDatabase()
                .collection("exames").document(String.valueOf(argColaborador.getNumCracha()))
                .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(c, "Sucesso", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(c, "Erro", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void listarColaborador() {
        db.getDatabase()
                .collection("exames")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            System.out.println("Deu bosta 😝");
                            return;
                        }
                        assert value != null;

                        // Colocar adapterColaboradores lista aqui

                        // For de documentos snapshot
                    }
                });
    }
}
