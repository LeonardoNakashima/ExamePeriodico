package com.aula.exameperiodico;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.aula.exameperiodico.database.Database;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query; // Importe para usar whereEqualTo

public class LoginAdm extends Fragment {

    private static final String TAG = "LoginAdmFragment";

    public LoginAdm() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_adm, container, false);

        EditText editCracha = view.findViewById(R.id.numCracha);
        EditText editSenha = view.findViewById(R.id.senhaAdm);
        Button btnLogin = view.findViewById(R.id.btnLoginAdm);

        btnLogin.setOnClickListener(v -> {
            String crachaStr = editCracha.getText().toString().trim();
            String senha = editSenha.getText().toString().trim();

            if (crachaStr.isEmpty() || senha.isEmpty()) {
                Toast.makeText(getContext(), "Número do crachá e senha são obrigatórios.", Toast.LENGTH_LONG).show();
                return;
            }

            // Tenta converter o crachá para inteiro
            int numCracha;
            try {
                numCracha = Integer.parseInt(crachaStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Número do crachá deve ser um número válido.", Toast.LENGTH_LONG).show();
                return;
            }

            // Chama o método de login com os dados do Firestore
            loginAdminWithFirestore(numCracha, senha, getContext(), view);
        });

        return view;
    }

    public void loginAdminWithFirestore(int numCracha, String senhaDigitada, Context c, View fragmentView) {
        FirebaseFirestore db = Database.getDatabase();

        db.collection("admins")
                .document("admins_id")
                .collection("collection_adms")
                .whereEqualTo("numCracha", numCracha)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            DocumentSnapshot adminDoc = task.getResult().getDocuments().get(0);
                            // CORREÇÃO AQUI: Recuperar como Long e converter para String
                            Long senhaLong = adminDoc.getLong("senha");
                            String senhaArmazenada = (senhaLong != null) ? String.valueOf(senhaLong) : null;

                            if (senhaArmazenada != null && senhaDigitada.equals(senhaArmazenada)) {
                                // Login bem-sucedido
                                String nomeAdmin = adminDoc.getString("nomeAdm"); // NomeAdm deve ser String no Firestore
                                Toast.makeText(c, "Login de administrador bem-sucedido! Bem-vindo, " + nomeAdmin, Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Login Firestore bem-sucedido para Crachá: " + numCracha);

                                NavController navController = Navigation.findNavController(fragmentView);
                                navController.navigate(R.id.navigation_area);

                            } else {
                                // Senha incorreta ou senha armazenada é nula
                                Toast.makeText(c, "Senha inválida.", Toast.LENGTH_SHORT).show();
                                Log.w(TAG, "Tentativa de login falhou: Senha inválida para Crachá: " + numCracha);
                            }
                        } else {
                            Toast.makeText(c, "Crachá de administrador não encontrado.", Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "Tentativa de login falhou: Crachá não encontrado: " + numCracha);
                        }
                    } else {
                        Toast.makeText(c, "Erro ao verificar credenciais: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Erro ao buscar admin no Firestore: " + task.getException().getMessage(), task.getException());
                    }
                });
    }
}