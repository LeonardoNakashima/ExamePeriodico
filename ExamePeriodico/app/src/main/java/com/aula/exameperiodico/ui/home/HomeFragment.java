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

        // Layout normal (sem reverseLayout)
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
                colaboradores.sort((a, b) -> b.getDataHora().compareTo(a.getDataHora()));

                for (Colaborador col : colaboradores) {
                    listaExames.add(new ExameMedico(
                            col.getNumCracha(),
                            col.getNomeColaborador(),
                            col.getDataHora(),
                            col.getInicioAtendimento(),
                            col.getFimAtendimento()
                    ));
                }

                adapter.notifyDataSetChanged();

                recyclerView.post(() -> recyclerView.scrollToPosition(0));
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Erro ao carregar exames do Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.d(TAG, "Erro ao carregar exames: " + e.getMessage());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
