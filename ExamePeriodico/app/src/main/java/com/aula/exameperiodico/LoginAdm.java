package com.aula.exameperiodico;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aula.exameperiodico.database.Database;
import com.google.firebase.auth.FirebaseUser;

public class LoginAdminFragment extends Fragment {

    public LoginAdminFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_admin, container, false);

        EditText editCracha = view.findViewById(R.id.numCracha);
        EditText editSenha = view.findViewById(R.id.senhaAdm);
        Button btnLogin = view.findViewById(R.id.logarAdm);

        btnLogin.setOnClickListener(v -> {
            String cracha = editCracha.getText().toString().trim();
            String senha = editSenha.getText().toString().trim();
            login(cracha, senha, getContext());
        });

        return view;
    }

    public void login(String email, String password, Context c) {
        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(c, "Número do crachá e senha são obrigatórios.", Toast.LENGTH_LONG).show();
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
                            Toast.makeText(c, "Erro inesperado no login.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(c, "Erro no login: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}

