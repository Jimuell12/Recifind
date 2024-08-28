package com.jimuel.recifind.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jimuel.recifind.R;

import java.util.List;

public class AdapterUploadingIngredients extends RecyclerView.Adapter<AdapterUploadingIngredients.RecipeViewHolder> {

    private List<RecipeItem> itemList;

    public AdapterUploadingIngredients(List<RecipeItem> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_upload, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        RecipeItem item = itemList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class RecipeViewHolder extends RecyclerView.ViewHolder {
        private EditText editIngredient;
        private EditText editMeasurement;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            editIngredient = itemView.findViewById(R.id.ingredient);
            editMeasurement = itemView.findViewById(R.id.measurement);

            editIngredient.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    itemList.get(getAdapterPosition()).setIngredient(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            editMeasurement.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    itemList.get(getAdapterPosition()).setMeasurement(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        public void bind(RecipeItem item) {
            editIngredient.setText(item.getIngredient());
            editMeasurement.setText(item.getMeasurement());
        }
    }

    public static class RecipeItem {
        private String ingredient;
        private String measurement;

        public RecipeItem(String ingredient, String measurement) {
            this.ingredient = ingredient;
            this.measurement = measurement;
        }

        public String getIngredient() {
            return ingredient;
        }

        public void setIngredient(String ingredient) {
            this.ingredient = ingredient;
        }

        public String getMeasurement() {
            return measurement;
        }

        public void setMeasurement(String measurement) {
            this.measurement = measurement;
        }
    }
}
