package com.jimuel.recifind.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jimuel.recifind.R;

import java.util.List;

public class AdapterIngredients extends RecyclerView.Adapter<AdapterIngredients.ViewHolder> {
    private List<Event> eventList;

    public AdapterIngredients(List<Event> eventList) {
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_ingredients, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public void setEventList(List<Event> eventList) {
        this.eventList = eventList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView ingredientname;
        private TextView ingredientmeasurement;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ingredientname = itemView.findViewById(R.id.ingredientname);
            ingredientmeasurement = itemView.findViewById(R.id.ingredientmmeasurement);
        }

        public void bind(Event event) {
            ingredientname.setText(event.getIngredientname());
            ingredientmeasurement.setText(event.getIngredientmmeasurement());
        }
    }

    public static class Event {
        private String ingredientname;
        private String ingredientmeasurement;

        public Event(String ingredientname, String ingredientmeasurement) {
            this.ingredientname = ingredientname;
            this.ingredientmeasurement = ingredientmeasurement;
        }


        public String getIngredientname() {
            return ingredientname;
        }

        public String getIngredientmmeasurement() {
            return ingredientmeasurement;
        }

    }

}
