package com.aula.exameperiodico;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aula.exameperiodico.database.colaborador.Colaborador;
import com.aula.exameperiodico.database.colaborador.ColaboradorDAO;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity"; // Tag para logs

    private EditText editTextCracha; // Removido editTextNome
    private Button btnEntrar;
    private ColaboradorDAO colaboradorDAO;

    // A constante EXTRA_NOME_CLIENTE foi removida.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_login);

        colaboradorDAO = new ColaboradorDAO();

        editTextCracha = findViewById(R.id.inputNum);
        // editTextNome = findViewById(R.id.edtNomeCliente);
        btnEntrar = findViewById(R.id.btnLogin);

        btnEntrar.setOnClickListener(v -> registrarEntrada());
    }

    private void registrarEntrada() {
        String crachaStr = editTextCracha.getText().toString().trim();
        // String nomeStr foi removida

        if (crachaStr.isEmpty()) {
            Toast.makeText(this, "Informe o número do crachá", Toast.LENGTH_SHORT).show();
            return;
        }
        // A verificação de nomeStr.isEmpty() foi removida.

        try {
            int numCracha = Integer.parseInt(crachaStr);

            Log.d(TAG, "Tentando buscar crachá: " + numCracha); // Log antes da busca

            colaboradorDAO.buscarColaboradorPorCracha(numCracha, new ColaboradorDAO.ColaboradorCallback() {
                @Override
                public void onColaboradorLoaded(Colaborador colaborador) {
                    if (colaborador != null) {
                        Log.d(TAG, "Crachá encontrado: " + colaborador.getNumCracha()); // Log de sucesso
                        // Crachá encontrado, proceda com o registro de entrada
                        Date agora = new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                        String dataHoraFormatada = sdf.format(agora);

                        colaborador.setInicioAtendimento(agora);

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("colaborador_atual", colaborador);
                        // intent.putExtra(LoginActivity.EXTRA_NOME_CLIENTE, nomeStr); // Removido
                        startActivity(intent);

                        Toast.makeText(LoginActivity.this, "Entrada registrada em: " + dataHoraFormatada, Toast.LENGTH_LONG).show();
                        Toast.makeText(LoginActivity.this, "Bem vindo " + colaborador.getNumCracha(), Toast.LENGTH_LONG).show();
                    } else {
                        Log.d(TAG, "Crachá não encontrado para o número: " + numCracha); // Log de crachá não encontrado
                        // Crachá não encontrado no banco de dados
                        Toast.makeText(LoginActivity.this, "Número de crachá não encontrado.", Toast.LENGTH_SHORT).show();
                    }
                    editTextCracha.setText(""); // Limpa o campo de texto
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Erro na busca por crachá: " + numCracha + ", Mensagem: " + e.getMessage(), e); // Log de falha
                    Toast.makeText(LoginActivity.this, "Erro ao verificar crachá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    editTextCracha.setText(""); // Limpa o campo de texto
                }
            });

        } catch (NumberFormatException e) {
            Log.e(TAG, "Erro de formato do número do crachá: " + crachaStr, e); // Log de NumberFormatException
            Toast.makeText(this, "Número de crachá inválido", Toast.LENGTH_SHORT).show();
        }
    }
}
