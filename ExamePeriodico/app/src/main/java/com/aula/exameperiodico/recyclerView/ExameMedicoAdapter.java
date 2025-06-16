package com.aula.exameperiodico.recyclerView;

import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.exameperiodico.R;

import java.util.List;

public class ExameMedicoAdapter extends RecyclerView.Adapter<ExameMedicoAdapter.ExameViewHolder> {

    private List<ExameMedico> listaExames;

    public ExameMedicoAdapter(List<ExameMedico> listaExames) {
        this.listaExames = listaExames;
    }

    public static class ExameViewHolder extends RecyclerView.ViewHolder {
        TextView tvCracha, tvDataHora, tvNomeColaborador, tvInicioAtendimento, tvTerminoAtendimento, tvStatus;

        public ExameViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCracha = itemView.findViewById(R.id.tvCracha);
            tvDataHora = itemView.findViewById(R.id.tvDataInsercao);
            tvNomeColaborador = itemView.findViewById(R.id.tvNome);
            tvInicioAtendimento = itemView.findViewById(R.id.tvInicioAtendimento);
            tvTerminoAtendimento = itemView.findViewById(R.id.tvTerminoAtendimento);
            tvStatus = itemView.findViewById(R.id.tvStatus);
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

        holder.tvCracha.setText("Crachá: " + exame.getNumCracha());
        holder.tvDataHora.setText("Tempo de atendimento: " + exame.getDataHora());
        holder.tvNomeColaborador.setText("Nome: " + exame.getNomeColaborador());
        holder.tvInicioAtendimento.setText("Início: " + exame.getFormattedInicioAtendimento());
        String statusTexto = exame.getStatus() ? "Finalizada" : "Em andamento";

        int statusColor = holder.tvStatus.getContext().getResources().getColor(
                exame.getStatus() ? R.color.md_theme_primaryFixed_mediumContrast : R.color.md_theme_errorContainer_mediumContrast
        );
        SpannableString spannable = new SpannableString("Status: " + statusTexto);
        int start = "Status: ".length();
        int end = start + statusTexto.length();
        spannable.setSpan(new android.text.style.ForegroundColorSpan(statusColor), start, end, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        holder.tvStatus.setText(spannable);

        String terminoAtendimento = exame.getFormattedTerminoAtendimento();
        if (!terminoAtendimento.isEmpty()) {
            holder.tvTerminoAtendimento.setVisibility(View.VISIBLE);
            holder.tvTerminoAtendimento.setText("Término: " + terminoAtendimento);

        } else {
            holder.tvTerminoAtendimento.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return listaExames.size();
    }
}
