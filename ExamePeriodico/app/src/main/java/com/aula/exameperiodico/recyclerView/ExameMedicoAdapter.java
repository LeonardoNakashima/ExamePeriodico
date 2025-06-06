package com.aula.exameperiodico.recyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.exameperiodico.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ExameMedicoAdapter extends RecyclerView.Adapter<ExameMedicoAdapter.ExameViewHolder> {

    private List<ExameMedico> listaExames;

    public ExameMedicoAdapter(List<ExameMedico> listaExames) {
        this.listaExames = listaExames;
    }

    public static class ExameViewHolder extends RecyclerView.ViewHolder {
        TextView tvCracha, tvDataHora, tvNomeColaborador, tvInicioAtendimento, tvTerminoAtendimento;

        public ExameViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCracha = itemView.findViewById(R.id.tvCracha);
            tvDataHora = itemView.findViewById(R.id.tvDataInsercao);
            tvNomeColaborador = itemView.findViewById(R.id.tvNomeAdm);
            tvInicioAtendimento = itemView.findViewById(R.id.tvInicioAtendimento);
            tvTerminoAtendimento = itemView.findViewById(R.id.tvTerminoAtendimento);
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

        holder.tvCracha.setText("Crachá: " + exame.getCracha());
        holder.tvNomeColaborador.setText("Nome: " + exame.getNomeColaborador());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        holder.tvDataHora.setText("Data/Hora: " + sdf.format(exame.getDataHora()));
        holder.tvInicioAtendimento.setText("Início: " + sdf.format(exame.getInicioAtendimento()));

        if (exame.getTerminoAtendimento() != null) {
            holder.tvTerminoAtendimento.setVisibility(View.VISIBLE);
            holder.tvTerminoAtendimento.setText("Término: " + sdf.format(exame.getTerminoAtendimento()));
        } else {
            holder.tvTerminoAtendimento.setVisibility(View.GONE);
        }
    }




    @Override
    public int getItemCount() {
        return listaExames.size();
    }
}
