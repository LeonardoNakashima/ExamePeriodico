package com.aula.exameperiodico.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.exameperiodico.R;
import com.aula.exameperiodico.databinding.FragmentHomeBinding;
import com.aula.exameperiodico.recyclerView.ExameMedico;
import com.aula.exameperiodico.recyclerView.ExameMedicoAdapter;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private RecyclerView recyclerView;
    private ExameMedicoAdapter adapter;
    private List<ExameMedico> listaExames = new ArrayList<>();

    // private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = binding.rv;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ExameMedicoAdapter(listaExames);
        recyclerView.setAdapter(adapter);

        // db = FirebaseFirestore.getInstance();

        carregarExamesLocaisParaTeste();
    }

    private void carregarExamesLocaisParaTeste() {
        listaExames.clear();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            listaExames.add(new ExameMedico(
                    12345,
                    "João Silva",
                    sdf.parse("05/06/2025 10:00"),
                    sdf.parse("05/06/2025 10:00"),
                    sdf.parse("05/06/2025 10:15")
            ));

            listaExames.add(new ExameMedico(
                    12345,
                    "João Silva",
                    sdf.parse("05/06/2025 10:00"),
                    sdf.parse("05/06/2025 10:00"),
                    sdf.parse("05/06/2025 10:15")
            ));

            listaExames.add(new ExameMedico(
                    12345,
                    "João Silva",
                    sdf.parse("05/06/2025 10:00"),
                    sdf.parse("05/06/2025 10:00"),
                    sdf.parse("05/06/2025 10:15")
            ));

            listaExames.add(new ExameMedico(
                    12345,
                    "João Silva",
                    sdf.parse("05/06/2025 10:00"),
                    sdf.parse("05/06/2025 10:00"),
                    sdf.parse("05/06/2025 10:15")
            ));

            listaExames.add(new ExameMedico(
                    67890,
                    "Maria Oliveira",
                    sdf.parse("05/06/2025 11:30"),
                    sdf.parse("05/06/2025 11:30"),
                    sdf.parse("05/06/2025 11:45")
            ));

            listaExames.add(new ExameMedico(
                    11223,
                    "Carlos Pereira",
                    sdf.parse("05/06/2025 14:00"),
                    sdf.parse("05/06/2025 14:00"),
                    sdf.parse("05/06/2025 14:20")
            ));

            listaExames.add(new ExameMedico(
                    44556,
                    "Ana Souza",
                    sdf.parse("05/06/2025 15:45"),
                    sdf.parse("05/06/2025 15:45"),
                    sdf.parse("05/06/2025 16:00")
            ));

            adapter.notifyDataSetChanged();
            Toast.makeText(getContext(), "Dados locais carregados com datas reais", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Erro ao gerar dados de teste", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
