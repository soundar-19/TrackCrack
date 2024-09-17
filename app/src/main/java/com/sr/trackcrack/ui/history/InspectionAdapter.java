package com.sr.trackcrack.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sr.trackcrack.R;
import java.util.List;

public class InspectionAdapter extends RecyclerView.Adapter<InspectionAdapter.ViewHolder> {

    private List<Inspection> inspectionList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Inspection inspection);
    }

    public InspectionAdapter(List<Inspection> inspectionList, OnItemClickListener listener) {
        this.inspectionList = inspectionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.inspection_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Inspection inspection = inspectionList.get(position);
        holder.inspectionId.setText("ID: " + inspection.getId());
        holder.inspectionDate.setText("Date: " + inspection.getDate());
        holder.cracksFound.setText("Cracks Found: " + inspection.getCracksFound());

        if (inspection.isSolved()) {
            holder.status.setText("Solved");
            holder.status.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.success_color));
        } else {
            holder.status.setText("Not Solved");
            holder.status.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(inspection));
    }

    @Override
    public int getItemCount() {
        return inspectionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView inspectionId, inspectionDate, cracksFound, status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            inspectionId = itemView.findViewById(R.id.inspection_id);
            inspectionDate = itemView.findViewById(R.id.inspection_date);
            cracksFound = itemView.findViewById(R.id.cracks_found);
            status = itemView.findViewById(R.id.status);
        }
    }
}