package com.aula.exameperiodico.database.colaborador;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.aula.exameperiodico.database.Database;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColaboradorDAO {

    private static final String TAG = "ColaboradorDAO";
    private static final String EXAMES_COLLECTION = "exames";
    private static final String ID_COUNTER_DOCUMENT = "exames_id"; // Este parece ser um documento fixo, não um contador
    private static final String ATENDIMENTOS_SUBCOLLECTION = "atendimentos";

    public interface ColaboradoresListCallback {
        void onColaboradoresLoaded(List<Colaborador> colaboradores);
        void onFailure(Exception e);
    }

    public interface OperacaoAtendimentoCallback {
        void onSuccess(@Nullable Colaborador resultColaborador);
        void onFailure(Exception e);
    }

    public interface ColaboradorCallback {
        void onColaboradorLoaded(Colaborador colaborador);
        void onFailure(Exception e);
    }

    private FirebaseFirestore getDb() {
        return Database.getDatabase();
    }

    public void cadastrarAtendimento(Colaborador colaborador, Context context, final OperacaoAtendimentoCallback callback) {
        try {
            getDb().collection(EXAMES_COLLECTION)
                    .document(ID_COUNTER_DOCUMENT)
                    .collection(ATENDIMENTOS_SUBCOLLECTION)
                    .add(colaborador)
                    .addOnSuccessListener(documentReference -> {
                        String newDocumentId = documentReference.getId();
                        colaborador.setDocumentId(newDocumentId);

                        Toast.makeText(context, "Atendimento cadastrado: " + colaborador.getNumCracha(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Atendimento cadastrado para Crachá: " + colaborador.getNumCracha() + ". ID do Documento: " + newDocumentId);
                        callback.onSuccess(colaborador);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Erro ao cadastrar atendimento: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Erro ao cadastrar atendimento: " + e.getMessage(), e);
                        callback.onFailure(e);
                    });
        } catch (Exception e) {
            Toast.makeText(context, "Erro geral ao cadastrar atendimento: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Erro geral em cadastrarAtendimento: " + e.getMessage(), e);
            callback.onFailure(e);
        }
    }

    public void atualizarAtendimento(String documentId, Date fimAtendimento, String tempoAtendimentoFormatado, boolean status, Context context, final OperacaoAtendimentoCallback callback) {
        if (documentId == null || documentId.isEmpty()) {
            Toast.makeText(context, "ID do atendimento inválido para atualização.", Toast.LENGTH_SHORT).show();
            callback.onFailure(new IllegalArgumentException("ID do documento é nulo ou vazio."));
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("fimAtendimento", fimAtendimento);
        updates.put("tempoAtendimento", tempoAtendimentoFormatado); // Nome do campo deve ser consistente com o modelo Colaborador
        updates.put("status", status);

        getDb().collection(EXAMES_COLLECTION)
                .document(ID_COUNTER_DOCUMENT)
                .collection(ATENDIMENTOS_SUBCOLLECTION)
                .document(documentId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Atendimento atualizado: " + documentId, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Atendimento com ID: " + documentId + " atualizado.");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Erro ao atualizar atendimento: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Erro ao atualizar atendimento (ID: " + documentId + "): " + e.getMessage(), e);
                    callback.onFailure(e);
                });
    }

    public void removerAtendimento(String documentId, Context c, final OperacaoAtendimentoCallback callback) {
        if (documentId == null || documentId.isEmpty()) {
            Toast.makeText(c, "ID do atendimento inválido para remoção.", Toast.LENGTH_SHORT).show();
            callback.onFailure(new IllegalArgumentException("ID do documento é nulo ou vazio."));
            return;
        }

        getDb().collection(EXAMES_COLLECTION)
                .document(ID_COUNTER_DOCUMENT)
                .collection(ATENDIMENTOS_SUBCOLLECTION)
                .document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(c, "Atendimento (ID: " + documentId + ") removido com sucesso!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Atendimento com ID: " + documentId + " removido.");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(c, "Erro ao remover atendimento (ID: " + documentId + "): " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Erro ao remover atendimento (ID: " + documentId + "): " + e.getMessage(), e);
                    callback.onFailure(e);
                });
    }

    public void listarAtendimentos(final ColaboradoresListCallback callback) {
        getDb().collection(EXAMES_COLLECTION)
                .document(ID_COUNTER_DOCUMENT)
                .collection(ATENDIMENTOS_SUBCOLLECTION)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e(TAG, "Erro ao listar atendimentos: " + error.getMessage(), error);
                            callback.onFailure(error);
                            return;
                        }
                        if (value == null) {
                            Log.w(TAG, "QuerySnapshot é nulo para listarAtendimentos.");
                            callback.onColaboradoresLoaded(new ArrayList<>());
                            return;
                        }

                        List<Colaborador> atendimentosList = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Colaborador atendimento = doc.toObject(Colaborador.class);
                            if (atendimento != null) {
                                atendimento.setDocumentId(doc.getId());
                                atendimentosList.add(atendimento);
                            } else {
                                Log.w(TAG, "Documento " + doc.getId() + " não pôde ser convertido para Colaborador.");
                            }
                        }
                        callback.onColaboradoresLoaded(atendimentosList);
                        Log.d(TAG, "Lista de atendimentos atualizada. Total: " + atendimentosList.size());
                    }
                });
    }
}