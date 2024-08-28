package com.jimuel.recifind.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jimuel.recifind.R;

import java.util.List;

public class AdapterUploadingInstructions extends RecyclerView.Adapter<AdapterUploadingInstructions.InstructionViewHolder> {

    private List<InstructionItem> itemList;

    public AdapterUploadingInstructions(List<InstructionItem> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public InstructionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_uplodinstrustions, parent, false);
        return new InstructionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InstructionViewHolder holder, int position) {
        InstructionItem item = itemList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class InstructionViewHolder extends RecyclerView.ViewHolder {
        private TextView instructionstep;
        private EditText instructionguide;

        public InstructionViewHolder(@NonNull View itemView) {
            super(itemView);
            instructionstep = itemView.findViewById(R.id.tvstep);
            instructionguide = itemView.findViewById(R.id.edtinstruction);

            instructionguide.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    itemList.get(getAdapterPosition()).setInstructionguide(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        public void bind(InstructionItem item) {
            instructionstep.setText(item.getInstructionstep());
            instructionguide.setText(item.getInstructionguide());
        }
    }

    public static class InstructionItem {
        private String instructionstep;
        private String instructionguide;

        public InstructionItem(String instructionstep, String instructionguide) {
            this.instructionstep = instructionstep;
            this.instructionguide = instructionguide;
        }

        public String getInstructionstep() {
            return instructionstep;
        }

        public void setInstructionstep(String instructionstep) {
            this.instructionstep = instructionstep;
        }

        public String getInstructionguide() {
            return instructionguide;
        }

        public void setInstructionguide(String instructionguide) {
            this.instructionguide = instructionguide;
        }
    }

}
