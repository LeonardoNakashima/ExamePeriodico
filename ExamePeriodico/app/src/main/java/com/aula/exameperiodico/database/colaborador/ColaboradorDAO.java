package com.aula.exameperiodico.database.colaborador;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.aula.exameperiodico.database.Database;
import com.aula.exameperiodico.recyclerView.ExameMedico; // Importar ExameMedico
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColaboradorDAO {

    private static final String TAG = "ColaboradorDAO";
    private static final String EXAMES_COLLECTION = "exames";
    private static final String ID_COUNTER_DOCUMENT = "exames_id";
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

    // NOVA INTERFACE: Para listar ExameMedico
    public interface ExamesListCallback {
        void onExamesLoaded(List<ExameMedico> exames);
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
                        .whereEqualTo("numCracha", colaborador.getNumCracha())
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                // Já existe um atendimento com este crachá
                                Toast.makeText(context, "Atendimento já cadastrado para o crachá: " + colaborador.getNumCracha(), Toast.LENGTH_SHORT).show();
                                Log.w(TAG, "Atendimento duplicado para crachá: " + colaborador.getNumCracha());
                                callback.onFailure(new Exception("Atendimento já cadastrado para este crachá."));
                            } else {
                                // Nenhum atendimento com esse crachá, pode cadastrar
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
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Erro ao verificar crachá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Erro na verificação de crachá: " + e.getMessage(), e);
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
        updates.put("tempoAtendimento", tempoAtendimentoFormatado);
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

        // 1. Primeiro, buscar o documento para verificar a data
        getDb().collection(EXAMES_COLLECTION)
                .document(ID_COUNTER_DOCUMENT)
                .collection(ATENDIMENTOS_SUBCOLLECTION)
                .document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // O documento existe, agora vamos verificar a data
                        Date terminoAtendimento = null;
                        // Tentar obter a data de fimAtendimento. No Firestore, Date é mapeado para Timestamp.
                        Timestamp timestamp = documentSnapshot.getTimestamp("fimAtendimento"); // Use o nome do campo como está no Firestore
                        if (timestamp != null) {
                            terminoAtendimento = timestamp.toDate();
                        }

                        if (terminoAtendimento == null) {
                            // Se fimAtendimento não existe ou é nulo, não podemos aplicar a regra de 1 mês
                            Toast.makeText(c, "Atendimento não pode ser removido: Data de término não encontrada.", Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "Tentativa de remoção falhou: fimAtendimento é nulo para ID: " + documentId);
                            callback.onFailure(new Exception("Data de término do atendimento não encontrada."));
                            return;
                        }

                        // Calcular a data de 1 mês atrás
                        Calendar umMesAtras = Calendar.getInstance();
                        umMesAtras.add(Calendar.MONTH, -1);
                        Date dataLimite = umMesAtras.getTime();

                        // Comparar as datas
                        if (terminoAtendimento.before(dataLimite)) {
                            // A data de término é MAIOR que 1 mês atrás (ou seja, mais antiga que 1 mês atrás)
                            // PROSSEGUIR COM A EXCLUSÃO
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
                        } else {
                            // A data de término NÃO é maior que 1 mês atrás (ou seja, é recente)
                            Toast.makeText(c, "Atendimento só pode ser removido após 1 mês do término.", Toast.LENGTH_LONG).show();
                            Log.d(TAG, "Tentativa de remoção falhou: atendimento muito recente. ID: " + documentId);
                            callback.onFailure(new Exception("Atendimento muito recente para remoção."));
                        }

                    } else {
                        // O documento não existe
                        Toast.makeText(c, "Atendimento não encontrado para remoção.", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Tentativa de remoção falhou: Documento não existe para ID: " + documentId);
                        callback.onFailure(new Exception("Atendimento não encontrado."));
                    }
                })
                .addOnFailureListener(e -> {
                    // Erro ao buscar o documento
                    Toast.makeText(c, "Erro ao verificar atendimento para remoção: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Erro ao buscar documento para verificação de remoção (ID: " + documentId + "): " + e.getMessage(), e);
                    callback.onFailure(e);
                });
    }
    public void autoRemoverAtendimento(String documentId, Context c, final OperacaoAtendimentoCallback callback) {
        if (documentId == null || documentId.trim().isEmpty()) {
            Toast.makeText(c, "ID do atendimento inválido para remoção.", Toast.LENGTH_SHORT).show();
            callback.onFailure(new IllegalArgumentException("ID do documento é nulo ou vazio."));
            return;
        }

        getDb().collection(EXAMES_COLLECTION)
                .document(ID_COUNTER_DOCUMENT)
                .collection(ATENDIMENTOS_SUBCOLLECTION)
                .document(documentId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(c, "Atendimento removido com sucesso!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Atendimento com ID: " + documentId + " removido automaticamente.");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(c, "Erro ao remover atendimento: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Erro ao remover atendimento com ID: " + documentId, e);
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

    // NOVO MÉTODO: Para listar ExameMedico
    public void listarExamesMedicos(final ExamesListCallback callback) {
        getDb().collection(EXAMES_COLLECTION)
                .document(ID_COUNTER_DOCUMENT)
                .collection(ATENDIMENTOS_SUBCOLLECTION) // Assumindo que os ExameMedico estão nesta subcoleção
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e(TAG, "Erro ao listar exames médicos: " + error.getMessage(), error);
                            callback.onFailure(error);
                            return;
                        }
                        if (value == null) {
                            Log.w(TAG, "QuerySnapshot é nulo para listarExamesMedicos.");
                            callback.onExamesLoaded(new ArrayList<>());
                            return;
                        }

                        List<ExameMedico> examesList = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            // Tenta converter o documento para ExameMedico.class
                            ExameMedico exame = doc.toObject(ExameMedico.class);
                            if (exame != null) {
                                exame.setDocumentId(doc.getId()); // Popula o documentId
                                examesList.add(exame);
                            } else {
                                Log.w(TAG, "Documento " + doc.getId() + " não pôde ser convertido para ExameMedico.");
                            }
                        }
                        callback.onExamesLoaded(examesList);
                        Log.d(TAG, "Lista de exames médicos atualizada. Total: " + examesList.size());
                    }
                });
    }

    // Este método não será mais usado na LoginActivity com o novo fluxo de "sempre criar novo"
    // Não incluído aqui, pois o último snippet não o tinha. Se precisar, adicione-o de volta.
}