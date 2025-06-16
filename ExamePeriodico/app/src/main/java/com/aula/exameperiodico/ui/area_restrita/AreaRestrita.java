package com.aula.exameperiodico.ui.area_restrita;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aula.exameperiodico.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.Date;

public class AreaRestrita extends Fragment {

    private Button buttonExcluir;
    private FirebaseFirestore db;
    private static final String ADMIN_SENHA = "admin123";  // Troque a senha se quiser

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_area_restrita, container, false);

        buttonExcluir = view.findViewById(R.id.buttonExcluirRegistros);
        db = FirebaseFirestore.getInstance();

        buttonExcluir.setOnClickListener(v -> solicitarSenha());

        return view;
    }

    private void solicitarSenha() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Senha de Admin");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Confirmar", (dialog, which) -> {
            String senhaDigitada = input.getText().toString();
            if (senhaDigitada.equals(ADMIN_SENHA)) {
                excluirRegistrosMesAnterior();
            } else {
                Toast.makeText(getContext(), "Senha incorreta!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void excluirRegistrosMesAnterior() {
        Calendar calendar = Calendar.getInstance();

        // Início do mês anterior
        calendar.add(Calendar.MONTH, -1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date inicioMesAnterior = calendar.getTime();

        // Fim do mês anterior
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date fimMesAnterior = calendar.getTime();

        db.collection("atendimentos")
                .whereGreaterThanOrEqualTo("inicioAtendimento", inicioMesAnterior)
                .whereLessThanOrEqualTo("inicioAtendimento", fimMesAnterior)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(getContext(), "Nenhum registro encontrado para exclusão.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        db.collection("atendimentos").document(document.getId()).delete();
                    }

                    Toast.makeText(getContext(), "Registros excluídos com sucesso!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Erro ao excluir: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
