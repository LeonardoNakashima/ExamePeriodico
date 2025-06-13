package com.aula.exameperiodico;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aula.exameperiodico.database.colaborador.Colaborador;
import com.aula.exameperiodico.database.colaborador.ColaboradorDAO;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText editTextCracha, editTextNome;
    private Button btnEntrar;
    private ColaboradorDAO colaboradorDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_login);

        colaboradorDAO = new ColaboradorDAO();

        editTextCracha = findViewById(R.id.inputNum);
        editTextNome = findViewById(R.id.inputNome);
        btnEntrar = findViewById(R.id.btnLogin);

        btnEntrar.setOnClickListener(v -> registrarEntrada());
    }

    private void registrarEntrada() {
        String crachaStr = editTextCracha.getText().toString().trim();
        String collaboratorNameInput = editTextNome.getText().toString().trim();

        if (crachaStr.isEmpty()) {
            Toast.makeText(this, "Informe o número do crachá", Toast.LENGTH_SHORT).show();
            return;
        }
        if (collaboratorNameInput.isEmpty()) {
            Toast.makeText(this, "Informe o nome do colaborador", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int numCracha = Integer.parseInt(crachaStr);
            Date agora = new Date();

            Colaborador novoAtendimento = new Colaborador();
            novoAtendimento.setNumCracha(numCracha);
            novoAtendimento.setNomeColaborador(collaboratorNameInput);
            novoAtendimento.setInicioAtendimento(agora);
            novoAtendimento.setStatus(false);

            colaboradorDAO.cadastrarAtendimento(novoAtendimento, this, new ColaboradorDAO.OperacaoAtendimentoCallback() {
                @Override
                public void onSuccess(@Nullable Colaborador colaboradorSalvo) {
                    // Este bloco executa DEPOIS que o registro foi salvo no Firebase
                    // e o 'colaboradorSalvo' agora tem o 'documentId' populado.
                    if (colaboradorSalvo == null || colaboradorSalvo.getDocumentId() == null) {
                        Toast.makeText(LoginActivity.this, "Erro interno: Colaborador salvo ou ID é nulo.", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "onSuccess: colaboradorSalvo ou documentId é nulo após cadastro.");
                        return;
                    }

                    Log.d(TAG, "Atendimento salvo. Preparando para MainActivity. Document ID: " + colaboradorSalvo.getDocumentId());

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("colaborador_atual", colaboradorSalvo); // Passa o objeto completo com o documentId

                    editTextCracha.setText("");
                    editTextNome.setText("");
                    startActivity(intent);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    String dataHoraFormatada = sdf.format(agora);
                    Toast.makeText(LoginActivity.this, "Entrada registrada em: " + dataHoraFormatada + "\n Bem vindo: " + collaboratorNameInput, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(LoginActivity.this, "Falha ao registrar entrada: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Falha ao registrar entrada no banco: " + e.getMessage(), e);
                    editTextCracha.setText("");
                    editTextNome.setText("");
                }
            });

        } catch (NumberFormatException e) {
            Log.e(TAG, "Erro de formato do número do crachá: " + crachaStr, e);
            Toast.makeText(this, "Número de crachá inválido", Toast.LENGTH_SHORT).show();
        }
    }
}