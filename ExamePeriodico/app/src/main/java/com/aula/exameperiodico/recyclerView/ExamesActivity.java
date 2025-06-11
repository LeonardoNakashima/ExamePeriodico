package com.aula.exameperiodico.recyclerView;

import com.aula.exameperiodico.R;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ExamesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ExameMedicoAdapter adapter;
    private List<ExameMedico> listaExames = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exames);

        recyclerView = findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExameMedicoAdapter(listaExames);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        carregarExamesDoFirebase();
    }

    private void carregarExamesDoFirebase() {
        db.collection("examesMedicos")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaExames.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ExameMedico exame = document.toObject(ExameMedico.class);
                            listaExames.add(exame);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Erro ao carregar exames", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
