package com.aula.exameperiodico;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
    private Colaborador colaboradorAtual;
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

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() != R.id.navigation_home) {
                // Esconde os botões quando não estiver na tela inicial
                if (binding.btnFinalizar != null) {
                    binding.btnFinalizar.setVisibility(View.GONE);
                }
                if (binding.btnLogout != null) {
                    binding.btnLogout.setVisibility(View.GONE);
                }
            } else {
                // Mostra os botões na tela inicial
                if (binding.btnFinalizar != null) {
                    binding.btnFinalizar.setVisibility(View.VISIBLE);
                }
                if (binding.btnLogout != null) {
                    binding.btnLogout.setVisibility(View.VISIBLE);
                }
            }
        });

        // Recuperar o colaborador atual da Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("colaborador_atual")) {
            colaboradorAtual = (Colaborador) intent.getSerializableExtra("colaborador_atual");
            if (colaboradorAtual != null) {
                Log.d("MainActivity", "Colaborador recebido: " + colaboradorAtual.getNumCracha() +
                        " - " + colaboradorAtual.getNomeColaborador() +
                        " (Document ID: " + colaboradorAtual.getDocumentId() + ")");
            } else {
                Log.e("MainActivity", "Colaborador recebido é nulo.");
            }
        } else {
            Log.w("MainActivity", "Intent não contém o extra 'colaborador_atual'.");
        }

        // Configurar Listeners para os botões
        if (binding.btnFinalizar != null) {
            binding.btnFinalizar.setOnClickListener(v -> finalizarAtendimento());
        } else {
            Log.e("MainActivity", "Erro: btnFinalizar é nulo no layout.");
        }

        if (binding.btnLogout != null) {
            binding.btnLogout.setOnClickListener(v -> logout());
        } else {
            Log.e("MainActivity", "Erro: btnLogout é nulo no layout.");
        }
    }

    public void setColaboradorAtual(Colaborador colaborador) {
        this.colaboradorAtual = colaborador;
    }

    private void finalizarAtendimento() {
        // Validação inicial: garantir que temos um colaborador e um documentId válido
        if (colaboradorAtual == null || colaboradorAtual.getDocumentId() == null || colaboradorAtual.getDocumentId().isEmpty()) {
            Toast.makeText(this, "Nenhum atendimento ativo para finalizar ou ID do documento ausente!", Toast.LENGTH_SHORT).show();
            Log.w("MainActivity", "Finalizar Atendimento: Colaborador atual é nulo ou ID do documento ausente/vazio.");
            return;
        }

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Finalizar Atendimento")
                .setMessage("Tem certeza de que deseja finalizar o atendimento?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    // Verificar se o início do atendimento está disponível para cálculo de duração
                    if (colaboradorAtual.getInicioAtendimento() == null) {
                        Toast.makeText(this, "Erro: Data de início do atendimento não encontrada.", Toast.LENGTH_LONG).show();
                        Log.e("MainActivity", "Finalizar Atendimento: Data de início do atendimento é nula para o colaborador: " + colaboradorAtual.getNumCracha());
                        return;
                    }

                    Date fimAtendimento = new Date(); // Obter a data e hora de finalização

                    // Calcular a duração
                    long diffInMillies = Math.abs(fimAtendimento.getTime() - colaboradorAtual.getInicioAtendimento().getTime());
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillies);
                    long hours = TimeUnit.MILLISECONDS.toHours(diffInMillies);
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillies) % 60; // Segundos restantes após remover minutos

                    String tempoAtendimentoFormatado;
                    if (hours > 0) {
                        tempoAtendimentoFormatado = String.format("%d horas e %d minutos", hours, minutes % 60);
                    } else if (minutes > 0) {
                        tempoAtendimentoFormatado = String.format("%d minutos e %d segundos", minutes, seconds);
                    } else {
                        tempoAtendimentoFormatado = String.format("%d segundos", seconds);
                    }
                    Log.d("MainActivity", "Finalizar Atendimento: Tempo de atendimento para " + colaboradorAtual.getNomeColaborador() + ": " + tempoAtendimentoFormatado);


                    // CHAMA O NOVO MÉTODO NO DAO PARA ATUALIZAR O ATENDIMENTO PELO DOCUMENT ID
                    colaboradorDAO.atualizarAtendimento(colaboradorAtual.getDocumentId(), fimAtendimento, tempoAtendimentoFormatado, true, this, new ColaboradorDAO.OperacaoAtendimentoCallback() {
                        @Override
                        public void onSuccess(@Nullable Colaborador resultColaborador) {
                            colaboradorAtual = null; // Limpa o colaborador atual após finalizar com sucesso
                            Log.d("MainActivity", "Finalizar Atendimento: Atendimento finalizado e colaboradorAtual limpo.");
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // Ocorreu um erro na atualização do banco
                            Toast.makeText(MainActivity.this, "Erro ao finalizar atendimento no banco: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e("MainActivity", "Finalizar Atendimento: Erro ao finalizar atendimento no banco: " + e.getMessage(), e);
                        }
                    });
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void logout() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Tem certeza de que deseja sair?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    // Verifica se há um atendimento ativo para remover antes de fazer logout
                    if (colaboradorAtual != null && colaboradorAtual.getDocumentId() != null && !colaboradorAtual.getDocumentId().isEmpty()) {
                        // Chama o método para remover o atendimento (se ele não foi finalizado)
                        colaboradorDAO.removerAtendimento(colaboradorAtual.getDocumentId(), this, new ColaboradorDAO.OperacaoAtendimentoCallback() {
                            @Override
                            public void onSuccess(@Nullable Colaborador resultColaborador) {
                                Log.d("MainActivity", "Logout: Atendimento ativo removido com sucesso (se existia).");
                                // Após a remoção (ou tentativa de remoção), prossiga com o logout
                                colaboradorAtual = null; // Limpa o colaborador atual
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e("MainActivity", "Logout: Erro ao tentar remover atendimento ativo: " + e.getMessage(), e);
                                // Mesmo que falhe a remoção, o logout ainda deve ocorrer, pois a intenção é sair.
                                colaboradorAtual = null; // Limpa o colaborador atual
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    } else {
                        Log.d("MainActivity", "Logout: Nenhum atendimento ativo para remover ou ID do documento ausente.");
                        // Se não há atendimento ativo para remover, prossiga diretamente com o logout
                        colaboradorAtual = null; // Limpa o colaborador atual
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("Não", null)
                .show();
    }
}