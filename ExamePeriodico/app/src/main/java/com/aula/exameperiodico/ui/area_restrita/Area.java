package com.aula.exameperiodico.ui.area_restrita;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.exameperiodico.R;
import com.aula.exameperiodico.recyclerView.ExameMedico;
import com.aula.exameperiodico.database.colaborador.ColaboradorDAO;
import com.aula.exameperiodico.database.colaborador.Colaborador; // Ainda pode ser necessário para OperacaoAtendimentoCallback
import com.aula.exameperiodico.recyclerView.ExameMedicoAdapterAdm;

import java.util.ArrayList;
import java.util.List;

public class Area extends Fragment implements ExameMedicoAdapterAdm.OnItemLongClickListener {

    private RecyclerView recyclerViewExames;
    private ExameMedicoAdapterAdm exameMedicoAdapterAdm;
    private ColaboradorDAO colaboradorDAO;
    private List<ExameMedico> listaExames = new ArrayList<>();

    private static final String TAG = "AreaFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_area, container, false);

        recyclerViewExames = view.findViewById(R.id.rv); // Certifique-se que o ID 'rv' está correto em fragment_area.xml
        recyclerViewExames.setLayoutManager(new LinearLayoutManager(getContext()));

        exameMedicoAdapterAdm = new ExameMedicoAdapterAdm(listaExames);
        exameMedicoAdapterAdm.setOnItemLongClickListener(this);
        recyclerViewExames.setAdapter(exameMedicoAdapterAdm);

        colaboradorDAO = new ColaboradorDAO();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        carregarExamesMedicos();
    }

    private void carregarExamesMedicos() {
        // CORRIGIDO: Agora chamamos listarExamesMedicos que retorna List<ExameMedico>
        colaboradorDAO.listarExamesMedicos(new ColaboradorDAO.ExamesListCallback() {
            @Override
            public void onExamesLoaded(List<ExameMedico> exames) {
                listaExames.clear();
                listaExames.addAll(exames);
                exameMedicoAdapterAdm.updateData(listaExames);
                Log.d(TAG, "Exames médicos carregados: " + exames.size());
                if (exames.isEmpty()) {
                    Toast.makeText(getContext(), "Nenhum exame encontrado.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Erro ao carregar exames: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Erro ao carregar exames: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public void onItemLongClick(ExameMedico exameMedico) {
        if (exameMedico.getDocumentId() == null || exameMedico.getDocumentId().isEmpty()) {
            Toast.makeText(getContext(), "Não foi possível remover: ID do exame ausente.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Tentativa de remover exame sem DocumentId: " + exameMedico.getNomeColaborador());
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Remover Exame Médico")
                .setMessage("Tem certeza de que deseja remover o exame de " + exameMedico.getNomeColaborador() + "?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    removerExameDoBanco(exameMedico);
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void removerExameDoBanco(ExameMedico exameParaRemover) {
        // O método removerAtendimento no ColaboradorDAO ainda espera OperacaoAtendimentoCallback<Colaborador>
        // mas como ele passa 'null' no sucesso e estamos interessados na remoção, isso é funcional.
        colaboradorDAO.removerAtendimento(exameParaRemover.getDocumentId(), getContext(), new ColaboradorDAO.OperacaoAtendimentoCallback() {
            @Override
            public void onSuccess(@Nullable Colaborador resultColaborador) {
                Toast.makeText(getContext(), "Exame removido com sucesso!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Exame removido com sucesso: " + exameParaRemover.getDocumentId());
                exameMedicoAdapterAdm.removeItem(exameParaRemover);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Erro ao remover exame: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Erro ao remover exame: " + exameParaRemover.getDocumentId() + " - " + e.getMessage(), e);
            }
        });
    }
}