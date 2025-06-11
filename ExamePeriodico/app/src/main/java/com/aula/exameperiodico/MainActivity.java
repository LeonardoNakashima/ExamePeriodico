package com.aula.exameperiodico;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.aula.exameperiodico.database.colaborador.Colaborador;
import com.aula.exameperiodico.database.colaborador.ColaboradorDAO;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.aula.exameperiodico.databinding.ActivityMainBinding;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Colaborador colaboradorAtual; // Este objeto agora terá o documentId
    private ColaboradorDAO colaboradorDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(findViewById(R.id.toolbar));

        colaboradorDAO = new ColaboradorDAO();
        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_admin).build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("colaborador_atual")) {
                colaboradorAtual = (Colaborador) intent.getSerializableExtra("colaborador_atual");
                if (colaboradorAtual != null) {
                    Log.d("MainActivity", "Colaborador recebido: " + colaboradorAtual.getNumCracha() +
                            " - " + colaboradorAtual.getNomeColaborador() +
                            " (Document ID: " + colaboradorAtual.getDocumentId() + ")"); // Log do ID do documento
                } else {
                    Log.e("MainActivity", "Colaborador recebido é null, mesmo com extra.");
                }
            } else {
                Log.e("MainActivity", "Intent não contém 'colaborador_atual' extra. Pode ser um problema de fluxo.");
            }
        }
        if (binding.btnFinalizar != null) {
            binding.btnFinalizar.setOnClickListener(v -> finalizarAtendimento());
        } else {
            Log.e("MainActivity", "btnFinalizar is null. Check your layout XML for R.id.btnFinalizar.");
        }
    }

    public void setColaboradorAtual(Colaborador colaborador) {
        this.colaboradorAtual = colaborador;
    }

    private void finalizarAtendimento() {
        // Validação inicial: garantir que temos um colaborador e um documentId válido
        if (colaboradorAtual == null || colaboradorAtual.getDocumentId() == null || colaboradorAtual.getDocumentId().isEmpty()) {
            Toast.makeText(this, "Nenhum atendimento ativo para finalizar ou ID do documento ausente!", Toast.LENGTH_SHORT).show();
            Log.w("MainActivity", "Erro: colaboradorAtual é null ou Document ID ausente/vazio.");
            return;
        }

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Finalizar Atendimento")
                .setMessage("Tem certeza de que deseja finalizar o atendimento?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    // Verificar se o início do atendimento está disponível para cálculo de duração
                    if (colaboradorAtual.getInicioAtendimento() == null) {
                        Toast.makeText(this, "Erro: Data de início do atendimento não encontrada.", Toast.LENGTH_LONG).show();
                        Log.e("MainActivity", "InicioAtendimento is null for colaborador: " + colaboradorAtual.getNumCracha());
                        return;
                    }

                    Date fimAtendimento = new Date(); // Obter a data e hora de finalização

                    // Calcular a duração
                    long diffInMillies = Math.abs(fimAtendimento.getTime() - colaboradorAtual.getInicioAtendimento().getTime());
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillies);
                    long hours = TimeUnit.MILLISECONDS.toHours(diffInMillies);
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillies) % 60;

                    String tempoAtendimentoFormatado;
                    if (hours > 0) {
                        tempoAtendimentoFormatado = String.format("%d horas e %d minutos", hours, minutes % 60);
                    } else if (minutes > 0) {
                        tempoAtendimentoFormatado = String.format("%d minutos e %d segundos", minutes, seconds);
                    } else {
                        tempoAtendimentoFormatado = String.format("%d segundos", seconds);
                    }
                    Log.d("MainActivity", "Tempo de atendimento para " + colaboradorAtual.getNomeColaborador() + ": " + tempoAtendimentoFormatado);


                    // CHAMA O NOVO MÉTODO NO DAO PARA ATUALIZAR O ATENDIMENTO PELO DOCUMENT ID
                    colaboradorDAO.atualizarAtendimento(colaboradorAtual.getDocumentId(), fimAtendimento, tempoAtendimentoFormatado, true, this, new ColaboradorDAO.OperacaoAtendimentoCallback() {
                        @Override
                        public void onSuccess(@Nullable Colaborador resultColaborador) {
                            // A atualização no banco foi bem-sucedida
                            Toast.makeText(MainActivity.this, "Atendimento finalizado! Duração: " + tempoAtendimentoFormatado, Toast.LENGTH_LONG).show();
                            colaboradorAtual = null; // Limpa o colaborador atual após finalizar
                            Log.d("MainActivity", "Atendimento finalizado e colaboradorAtual limpo.");
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // Ocorreu um erro na atualização do banco
                            Toast.makeText(MainActivity.this, "Erro ao finalizar atendimento no banco: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e("MainActivity", "Erro ao finalizar atendimento: " + e.getMessage(), e);
                        }
                    });
                })
                .setNegativeButton("Não", null)
                .show();
    }
}