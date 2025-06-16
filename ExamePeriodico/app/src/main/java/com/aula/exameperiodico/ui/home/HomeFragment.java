package com.aula.exameperiodico.ui.home;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.exameperiodico.databinding.FragmentHomeBinding;
import com.aula.exameperiodico.recyclerView.ExameMedico;
import com.aula.exameperiodico.recyclerView.ExameMedicoAdapter;
import com.aula.exameperiodico.database.colaborador.ColaboradorDAO;
import com.aula.exameperiodico.database.colaborador.Colaborador;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator; // Importado para usar Comparator
import java.util.Date;       // Importado para usar Date::compareTo

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private RecyclerView recyclerView;
    private ExameMedicoAdapter adapter;
    private List<ExameMedico> listaExames = new ArrayList<>();
    private ColaboradorDAO colaboradorDAO;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = binding.rv;

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemViewCacheSize(20);

        adapter = new ExameMedicoAdapter(listaExames);
        recyclerView.setAdapter(adapter);

        colaboradorDAO = new ColaboradorDAO();

        carregarExamesDoFirebase();
    }

    private void carregarExamesDoFirebase() {
        colaboradorDAO.listarAtendimentos(new ColaboradorDAO.ColaboradoresListCallback() {
            @Override
            public void onColaboradoresLoaded(List<Colaborador> colaboradores) {
                listaExames.clear();

                // Ordena por dataHora decrescente (mais recente primeiro)
                colaboradores.sort((a, b) -> {
                    if (b.getDataHora() == null && a.getDataHora() == null) return 0;
                    if (b.getDataHora() == null) return -1;  // null vai para o final da lista
                    if (a.getDataHora() == null) return 1;
                    return b.getDataHora().compareTo(a.getDataHora());
                });
                // --- FIM DA ORDENAÇÃO ---


                for (Colaborador col : colaboradores) {
                    listaExames.add(new ExameMedico(
                            col.getNumCracha(),
                            col.getNomeColaborador(),
                            col.getDataHora(), // DataHora aqui é a String de duração
                            col.getInicioAtendimento(),
                            col.getFimAtendimento(),
                            col.getStatus()
                    ));
                }

                adapter.notifyDataSetChanged();

                // Rola para a primeira posição para mostrar o item mais recente
                recyclerView.post(() -> recyclerView.scrollToPosition(0));
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Erro ao carregar exames do Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Erro ao carregar exames: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}