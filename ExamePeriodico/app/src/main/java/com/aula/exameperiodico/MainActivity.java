package com.aula.exameperiodico;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.aula.exameperiodico.database.colaborador.Colaborador;
import com.aula.exameperiodico.database.colaborador.ColaboradorDAO;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.aula.exameperiodico.databinding.ActivityMainBinding;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Colaborador colaboradorAtual;
    private ColaboradorDAO colaboradorDAO;
    private String nomeCliente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        colaboradorDAO = new ColaboradorDAO();

        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_admin)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Ajustado o listener para btnFinalizar
        binding.btnFinalizar.setOnClickListener(v -> {
            // Verifica se há um colaborador atual antes de tentar acessar seus dados
            if (colaboradorAtual == null) {
                Toast.makeText(this, "Nenhum colaborador logado para finalizar atendimento.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verifica se o nome do colaborador está preenchido antes de finalizar o atendimento
            if (nomeCliente != null && !nomeCliente.isEmpty()) {
                finalizarAtendimento();
            } else {
                Toast.makeText(this, "Por favor, preencha o nome do cliente antes de finalizar!", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnAdd.setOnClickListener(v -> {
            // Criação do EditText
            final EditText input = new EditText(MainActivity.this);
            input.setHint("Digite o nome do cliente"); // Revertido para um hint mais genérico
            input.setPadding(40, 30, 40, 30);

            // Criação do AlertDialog
            new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle("Nome do cliente")
                    .setMessage("Digite as informações necessárias:")
                    .setView(input)
                    .setPositiveButton("Salvar", (dialog, which) -> {
                        nomeCliente = input.getText().toString().trim();
                        if (!nomeCliente.isEmpty()) {
                            if (colaboradorAtual != null) {
                                colaboradorAtual.setNomeColaborador(nomeCliente);
                                Toast.makeText(MainActivity.this, "Nome do cliente: " + nomeCliente, Toast.LENGTH_SHORT).show(); // Ajustado a mensagem
                            } else {
                                Toast.makeText(MainActivity.this, "Nenhum cliente ativo.", Toast.LENGTH_SHORT).show();
                                Log.w("MainActivity", "Tentativa de cadastrar nome, mas nomeCliente é null.");
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Por favor, digite algo.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("colaborador_atual")) {
                colaboradorAtual = (Colaborador) intent.getSerializableExtra("colaborador_atual");
                if (colaboradorAtual != null) {
                    Log.d("MainActivity", "Colaborador recebido: " + colaboradorAtual.getNumCracha() + " - " + colaboradorAtual.getNomeColaborador());
                } else {
                    Log.e("MainActivity", "Colaborador recebido é null, mesmo com extra.");
                }
            } else {
                Log.e("MainActivity", "Intent não contém 'colaborador_atual' extra. Pode ser um problema de fluxo.");
            }
        }
    }

    public void setColaboradorAtual(Colaborador colaborador) {
        this.colaboradorAtual = colaborador;
    }

    private void finalizarAtendimento() {

        if (colaboradorAtual == null) {
            Toast.makeText(this, "Nenhum colaborador entrou para finalizar atendimento!", Toast.LENGTH_SHORT).show();
            Log.w("MainActivity", "Erro no método de listagem: colaboradorAtual é null.");
            return;
        }

        colaboradorAtual.setFimAtendimento(new Date());

        colaboradorDAO.cadastrarAtendimento(colaboradorAtual, this);

        colaboradorAtual.setDataHora(new Date());

        Toast.makeText(this, "Atendimento finalizado!", Toast.LENGTH_SHORT).show();

        colaboradorAtual = null; // Limpa o colaborador atual após finalizar o atendimento
        Log.d("MainActivity", "Atendimento finalizado e colaboradorAtual limpo.");
    }
}
