package com.aula.exameperiodico.database.colaborador;

import android.content.Context;
import android.net.Uri;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ColaboradorDAO {

    // --- Constantes para Caminhos do Firestore ---
    private static final String TAG = "ColaboradorDAO"; // Tag para Logcat
    private static final String EXAMES_COLLECTION = "exames";
    private static final String ID_COUNTER_DOCUMENT = "exames_id"; // Documento fixo para contador de crachás
    private static final String ATENDIMENTOS_SUBCOLLECTION = "atendimentos"; // Subcoleção onde os atendimentos são salvos

    // --- Interfaces de Callback ---
    public interface ImageUrlCallback {
        void onSuccess(String imageUrl);
        void onFailure(Exception e);
    }

    public interface ColaboradoresListCallback {
        void onColaboradoresLoaded(List<Colaborador> colaboradores);
        void onFailure(Exception e);
    }

    public interface ColaboradorCallback {
        // Retorna um Colaborador (que agora representa um Atendimento)
        void onColaboradorLoaded(Colaborador colaborador);
        void onFailure(Exception e);
    }

    // --- Helper para obter a instância do FirebaseFirestore ---
    private FirebaseFirestore getDb() {
        return Database.getDatabase(); // Assumindo que Database.getDatabase() retorna FirebaseFirestore.getInstance()
    }

    // --- Métodos de Gerenciamento ---

    /**
     * Cadastra um novo atendimento para um colaborador.
     * O atendimento é salvo como um documento com ID automático na subcoleção
     * "exames/exames_id/atendimentos".
     *
     * @param colaborador O objeto Colaborador, que contém os dados do atendimento a serem salvos.
     * @param context O Contexto Android.
     */
    public void cadastrarAtendimento(Colaborador colaborador, Context context) {
        try {
            getDb().collection(EXAMES_COLLECTION)
                    .document(ID_COUNTER_DOCUMENT)
                    .collection(ATENDIMENTOS_SUBCOLLECTION)
                    .whereEqualTo("numCracha", colaborador.getNumCracha())
                    .limit(1) // Assumindo que numCracha é único para atendimentos que você quer atualizar
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            String documentIdToUpdate = querySnapshot.getDocuments().get(0).getId();
                            getDb().collection(EXAMES_COLLECTION)
                                    .document(ID_COUNTER_DOCUMENT)
                                    .collection(ATENDIMENTOS_SUBCOLLECTION)
                                    .add(colaborador) // Adiciona outro documento
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, "Atendimento com crachá " + colaborador.getNumCracha() + " cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "Atendimento com crachá " + colaborador.getNumCracha() + " cadastrado com sucesso. (ID: " + documentIdToUpdate + ").");
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Erro ao cadastrar atendimento: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, "Erro ao cadastrar atendimento: " + e.getMessage(), e);
                                    });
                        } else {
                            Toast.makeText(context, "Erro: Atendimento com crachá " + colaborador.getNumCracha() + " não encontrado para cadastramento.", Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "Atendimento com crachá " + colaborador.getNumCracha() + " não encontrado para cadastramento.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Erro ao buscar atendimento para cadastro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Erro ao buscar atendimento para cadastro: " + e.getMessage(), e);
                    });
        } catch (Exception e) {
            Toast.makeText(context, "Erro geral: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Erro geral em cadastrarAtendimento: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    /**
     * Remove um registro de atendimento.
     * Ele busca o atendimento pelo campo 'numCracha' dentro da subcoleção 'exames/exames_id/atendimentos'
     * e o remove se a dataHora não for muito recente (lógica original preservada).
     *
     * @param argColaborador O objeto Colaborador contendo o numCracha do atendimento a ser removido.
     * @param c O Contexto Android.
     */
    public void removerAtendimento(Colaborador argColaborador, Context c) {
        if (argColaborador.getNumCracha() == 0) {
            Toast.makeText(c, "Crachá inválido para remoção de atendimento.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Tentativa de remover atendimento com crachá 0.");
            return;
        }

        // Primeiro, encontre o documento do atendimento pelo campo 'numCracha'
        getDb().collection(EXAMES_COLLECTION)
                .document(ID_COUNTER_DOCUMENT)
                .collection(ATENDIMENTOS_SUBCOLLECTION)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        Timestamp dataHora = document.getTimestamp("dataHora");
                        Instant umMesAtras = Instant.now().minus(1, ChronoUnit.MONTHS);
                        String docIdToDelete = document.getId();

                        if (dataHora == null || dataHora.toDate().toInstant().isBefore(umMesAtras)) {
                            getDb().collection(EXAMES_COLLECTION)
                                    .document(ID_COUNTER_DOCUMENT)
                                    .collection(ATENDIMENTOS_SUBCOLLECTION)
                                    .document(docIdToDelete)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(c, "Atendimento (Crachá: " + argColaborador.getNumCracha() + ") removido com sucesso!", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "Atendimento com crachá " + argColaborador.getNumCracha() + " (ID: " + docIdToDelete + ") removido.");
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(c, "Erro ao remover atendimento (Crachá: " + argColaborador.getNumCracha() + "): " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, "Erro ao remover atendimento com crachá " + argColaborador.getNumCracha() + ": " + e.getMessage(), e);
                                    });
                        } else {
                            Toast.makeText(c, "Atendimento (Crachá: " + argColaborador.getNumCracha() + ") é muito recente para ser removido. Data do atendimento: " + dataHora.toDate().toString(), Toast.LENGTH_LONG).show();
                            Log.w(TAG, "Tentativa de remover atendimento muito recente: " + dataHora.toDate().toString());
                        }
                    } else {
                        Toast.makeText(c, "Nenhum atendimento encontrado para o crachá: " + argColaborador.getNumCracha() + ".", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Nenhum atendimento encontrado para remoção com crachá: " + argColaborador.getNumCracha() + ".");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(c, "Erro ao buscar atendimento para remoção: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Erro ao buscar atendimento para remoção: " + e.getMessage(), e);
                });
    }

    /**
     * Lista todos os atendimentos da subcoleção "exames/exames_id/atendimentos".
     * Utiliza um listener em tempo real para atualizações contínuas.
     *
     * @param callback O callback para receber a lista de objetos Colaborador (que representam atendimentos).
     */
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
                            // Converte cada documento em um objeto Colaborador (que aqui representa um Atendimento)
                            Colaborador atendimento = doc.toObject(Colaborador.class);
                            if (atendimento != null) {
                                atendimentosList.add(atendimento);
                            } else {
                                Log.w(TAG, "Documento " + doc.getId() + " não pôde ser convertido para Atendimento (Colaborador).");
                            }
                        }
                        callback.onColaboradoresLoaded(atendimentosList);
                        Log.d(TAG, "Lista de atendimentos atualizada. Total: " + atendimentosList.size());
                    }
                });
    }

    /**
     * Busca um atendimento pelo valor do campo 'numCracha' dentro dos documentos
     * da subcoleção "exames/exames_id/atendimentos".
     *
     * @param numCracha O número do crachá a ser pesquisado (valor do campo).
     * @param callback O callback para lidar com o resultado (Colaborador/Atendimento ou erro).
     */
    public void buscarColaboradorPorCracha(int numCracha, final ColaboradorCallback callback) {
        if (numCracha == 0) {
            Log.e(TAG, "Não é possível buscar atendimento com crachá 0.");
            callback.onColaboradorLoaded(null);
            return;
        }

        getDb().collection(EXAMES_COLLECTION)
                .document(ID_COUNTER_DOCUMENT)
                .collection(ATENDIMENTOS_SUBCOLLECTION)
                .whereEqualTo("numCracha", numCracha) // Procura o campo 'numCracha'
                .limit(1) // Assume que cada crachá corresponde a um único atendimento que você está buscando
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "buscarColaboradorPorCracha (por campo): " + numCracha + " - Sucesso!");
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            Colaborador colaborador = document.toObject(Colaborador.class);
                            callback.onColaboradorLoaded(colaborador);
                        } else {
                            Log.d(TAG, "Atendimento com crachá " + numCracha + " não encontrado.");
                            callback.onColaboradorLoaded(null); // Atendimento não encontrado
                        }
                    } else {
                        Log.e(TAG, "Erro ao buscar atendimento por crachá " + numCracha + ": " + task.getException().getMessage(), task.getException());
                        callback.onFailure(task.getException());
                    }
                });
    }

    // --- Gerenciamento de Imagens (Mantido como estava, usando numCracha para nome do arquivo) ---

    /**
     * Busca a URL de download da imagem de um colaborador no Firebase Storage.
     * As imagens são armazenadas em "colaborador_images/{numCracha}.jpg".
     *
     * @param numCracha O número do crachá do colaborador.
     * @param callback O callback para receber a URL da imagem ou um erro.
     */
    public void buscarImagemColaborador(int numCracha, final ImageUrlCallback callback) {
        if (numCracha == 0) {
            callback.onFailure(new IllegalArgumentException("Crachá inválido para buscar imagem."));
            return;
        }
        StorageReference imageRef = FirebaseStorage.getInstance().getReference()
                .child("colaborador_images")
                .child(numCracha + ".jpg");

        imageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Log.d(TAG, "Imagem encontrada para crachá " + numCracha + ": " + uri.toString());
                    callback.onSuccess(uri.toString());
                })
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Erro ao buscar imagem para o crachá " + numCracha + ": " + exception.getMessage(), exception);
                    callback.onFailure(exception);
                });
    }

    /**
     * Faz o upload de uma imagem para um colaborador no Firebase Storage.
     * A imagem será salva em "colaborador_images/{numCracha}.jpg".
     *
     * @param numCracha O número do crachá do colaborador.
     * @param imageUri A Uri da imagem a ser enviada (do armazenamento local).
     * @param c O Contexto Android.
     * @param callback O callback para receber a URL da imagem enviada ou um erro.
     */
    public void uploadImagemColaborador(int numCracha, Uri imageUri, Context c, final ImageUrlCallback callback) {
        if (imageUri == null) {
            callback.onFailure(new IllegalArgumentException("URI da imagem não pode ser nula."));
            Toast.makeText(c, "Erro: URI da imagem não pode ser nula.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (numCracha == 0) {
            callback.onFailure(new IllegalArgumentException("Crachá inválido para upload de imagem."));
            Toast.makeText(c, "Erro: Crachá inválido para upload de imagem.", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference imageRef = FirebaseStorage.getInstance().getReference()
                .child("colaborador_images")
                .child(numCracha + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Toast.makeText(c, "Imagem enviada com sucesso!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Imagem enviada para crachá " + numCracha + ". URL: " + uri.toString());
                        callback.onSuccess(uri.toString());
                    }).addOnFailureListener(e -> {
                        Toast.makeText(c, "Erro ao obter URL da imagem após o upload: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Erro ao obter URL da imagem para crachá " + numCracha + ": " + e.getMessage(), e);
                        callback.onFailure(e);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(c, "Erro ao enviar imagem: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Erro ao enviar imagem para crachá " + numCracha + ": " + e.getMessage(), e);
                    callback.onFailure(e);
                });
    }
}