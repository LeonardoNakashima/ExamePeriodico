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

        // Certifique-se de que R.id.navigation_login existe no seu nav_graph
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_login).build(); // Ajuste conforme seu mobile_navigation.xml

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Define a visibilidade inicial dos botões baseada no estado do colaborador
        updateButtonVisibility(); // Chamada inicial para o estado de onCreate

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.navigation_home) {
                // Se estiver na tela inicial (navigation_home)
                updateButtonVisibility(); // Atualiza a visibilidade baseada no colaboradorAtual
            } else {
                // CORRIGIDO: Se NÃO estiver na tela inicial, ESCONDE TODOS os botões de ação
                // Independentemente do estado de colaboradorAtual.
                if (binding.btnFinalizar != null) {
                    binding.btnFinalizar.setVisibility(View.GONE);
                }
                if (binding.btnLogout != null) {
                    binding.btnLogout.setVisibility(View.GONE);
                }
                if (binding.btnAdd != null) {
                    binding.btnAdd.setVisibility(View.GONE);
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
                // Após receber o colaborador, a visibilidade será atualizada pela chamada abaixo
            } else {
                Log.e("MainActivity", "Colaborador recebido é nulo, mesmo com extra. Isso não deveria acontecer se o fluxo estiver correto.");
            }
        } else {
            Log.w("MainActivity", "Intent não contém 'colaborador_atual' extra. Pode ser um problema de fluxo ou reinício do app.");
        }

        // Chamada adicional para garantir a visibilidade correta após o recebimento do Intent
        // Isso é importante porque colaboradorAtual é setado APÓS a primeira execução do listener
        updateButtonVisibility();


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

        if (binding.btnAdd != null) {
            binding.btnAdd.setOnClickListener(v -> {
                Intent addIntent = new Intent(MainActivity.this, RegistroActivity.class);
                startActivity(addIntent);
            });
        } else {
            Log.e("MainActivity", "Erro: btnAdd é nulo no layout.");
        }
    }

    // Método para gerenciar a visibilidade dos botões baseado no estado do colaboradorAtual
    private void updateButtonVisibility() {
        if (binding == null) {
            Log.e("MainActivity", "Binding é nulo ao tentar atualizar a visibilidade dos botões.");
            return;
        }

        if (colaboradorAtual != null) {
            // Há um atendimento ativo
            if (binding.btnFinalizar != null) {
                binding.btnFinalizar.setVisibility(View.VISIBLE);
            }
            if (binding.btnLogout != null) {
                binding.btnLogout.setVisibility(View.VISIBLE);
            }
            if (binding.btnAdd != null) {
                binding.btnAdd.setVisibility(View.GONE);
            }
        } else {
            if (binding.btnFinalizar != null) {
                binding.btnFinalizar.setVisibility(View.GONE);
            }
            if (binding.btnLogout != null) {
                binding.btnLogout.setVisibility(View.GONE);
            }
            if (binding.btnAdd != null) {
                binding.btnAdd.setVisibility(View.VISIBLE);
            }
        }
    }


    public void setColaboradorAtual(Colaborador colaborador) {
        this.colaboradorAtual = colaborador;
        updateButtonVisibility(); // Atualiza a visibilidade sempre que o colaboradorAtual muda
    }

    private void finalizarAtendimento() {
        if (colaboradorAtual == null || colaboradorAtual.getDocumentId() == null || colaboradorAtual.getDocumentId().isEmpty()) {
            Toast.makeText(this, "Nenhum atendimento ativo para finalizar ou ID do documento ausente!", Toast.LENGTH_SHORT).show();
            Log.w("MainActivity", "Finalizar Atendimento: Colaborador atual é nulo ou ID do documento ausente/vazio.");
            return;
        }

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Finalizar Atendimento")
                .setMessage("Tem certeza de que deseja finalizar o atendimento?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    if (colaboradorAtual.getInicioAtendimento() == null) {
                        Toast.makeText(this, "Erro: Data de início do atendimento não encontrada.", Toast.LENGTH_LONG).show();
                        Log.e("MainActivity", "Finalizar Atendimento: Data de início do atendimento é nula para o colaborador: " + colaboradorAtual.getNumCracha());
                        return;
                    }

                    Date fimAtendimento = new Date();
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
                    Log.d("MainActivity", "Finalizar Atendimento: Tempo de atendimento para " + colaboradorAtual.getNomeColaborador() + ": " + tempoAtendimentoFormatado);

                    colaboradorDAO.atualizarAtendimento(colaboradorAtual.getDocumentId(), fimAtendimento, tempoAtendimentoFormatado, true, this, new ColaboradorDAO.OperacaoAtendimentoCallback() {
                        @Override
                        public void onSuccess(@Nullable Colaborador resultColaborador) {
                            Toast.makeText(MainActivity.this, "Atendimento finalizado! Duração: " + tempoAtendimentoFormatado, Toast.LENGTH_LONG).show();
                            colaboradorAtual = null;
                            Log.d("MainActivity", "Finalizar Atendimento: Atendimento finalizado e colaboradorAtual limpo.");
                            updateButtonVisibility(); // Atualiza a visibilidade após finalizar
                        }

                        @Override
                        public void onFailure(Exception e) {
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
                    String docIdToClear = null;
                    if (colaboradorAtual != null && colaboradorAtual.getDocumentId() != null && !colaboradorAtual.getDocumentId().isEmpty()) {
                        docIdToClear = colaboradorAtual.getDocumentId();
                    }

                    colaboradorAtual = null;

                    if (docIdToClear != null) {
                        colaboradorDAO.autoRemoverAtendimento(docIdToClear, this, new ColaboradorDAO.OperacaoAtendimentoCallback() {
                            @Override
                            public void onSuccess(@Nullable Colaborador resultColaborador) {
                                Log.d("MainActivity", "Logout: Atendimento ativo removido com sucesso (se existia).");
                                binding.btnAdd.setVisibility(View.VISIBLE);
                                binding.btnFinalizar.setVisibility(View.GONE);
                                binding.btnLogout.setVisibility(View.GONE);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e("MainActivity", "Logout: Erro ao tentar remover atendimento ativo: " + e.getMessage(), e);
                            }
                        });
                    } else {
                        Log.d("MainActivity", "Logout: Nenhum atendimento ativo para remover ou ID do documento ausente.");
                        Intent registroIntent = new Intent(MainActivity.this, RegistroActivity.class);
                        startActivity(registroIntent);
                    }
                })
                .setNegativeButton("Não", null)
                .show();
    }
}