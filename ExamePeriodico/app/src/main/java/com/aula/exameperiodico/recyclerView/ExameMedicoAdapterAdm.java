package com.aula.exameperiodico.recyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.exameperiodico.R;
import com.aula.exameperiodico.recyclerView.ExameMedico;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExameMedicoAdapterAdm extends RecyclerView.Adapter<ExameMedicoAdapterAdm.ExameViewHolder> {

    private List<ExameMedico> listaExames;
    private OnItemLongClickListener longClickListener;

    public ExameMedicoAdapterAdm(List<ExameMedico> listaExames) {
        this.listaExames = listaExames != null ? listaExames : new ArrayList<>();
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(ExameMedico exameMedico); // Agora passa um ExameMedico
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void updateData(List<ExameMedico> newExames) {
        this.listaExames = newExames;
        notifyDataSetChanged();
    }

    public void removeItem(ExameMedico exameMedico) {
        int position = listaExames.indexOf(exameMedico);
        if (position != -1) {
            listaExames.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public ExameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.exame_item, parent, false);
        return new ExameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExameViewHolder holder, int position) {
        ExameMedico exame = listaExames.get(position);
        holder.bind(exame); // Chama o método bind no ViewHolder

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(exame);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return listaExames.size();
    }

    public static class ExameViewHolder extends RecyclerView.ViewHolder {
        TextView tvCracha, tvDataHora, tvNomeColaborador, tvInicioAtendimento, tvTerminoAtendimento, tvStatus;

        public ExameViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCracha = itemView.findViewById(R.id.tvCracha);
            tvDataHora = itemView.findViewById(R.id.tvDataInsercao);
            tvNomeColaborador = itemView.findViewById(R.id.tvNomeAdm);
            tvInicioAtendimento = itemView.findViewById(R.id.tvInicioAtendimento);
            tvTerminoAtendimento = itemView.findViewById(R.id.tvTerminoAtendimento);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        public void bind(ExameMedico exame) {
            tvCracha.setText(String.format(Locale.getDefault(), "Crachá: %d", exame.getNumCracha()));
            tvDataHora.setText(String.format("Tempo de atendimento: %s", exame.getDataHora())); // Assumindo que DataHora é o tempo formatado
            tvNomeColaborador.setText(String.format("Nome: %s", exame.getNomeColaborador()));

            // Usando os métodos formatados do ExameMedico
            tvInicioAtendimento.setText(String.format("Início: %s", exame.getFormattedInicioAtendimento()));

            String terminoAtendimento = exame.getFormattedTerminoAtendimento();
            if (!terminoAtendimento.isEmpty()) {
                tvTerminoAtendimento.setVisibility(View.VISIBLE);
                tvTerminoAtendimento.setText(String.format("Término: %s", terminoAtendimento));
            } else {
                tvTerminoAtendimento.setVisibility(View.GONE);
            }

            tvStatus.setText(String.format("Status: %s", (exame.getStatus() ? "Finalizada" : "Em andamento")));
        }
    }
}