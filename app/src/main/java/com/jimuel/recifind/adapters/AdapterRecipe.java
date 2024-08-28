package com.jimuel.recifind.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jimuel.recifind.fragments.IngredientsFragment;
import com.jimuel.recifind.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdapterRecipe extends RecyclerView.Adapter<AdapterRecipe.ViewHolder> {
    private List<Event> eventList;
    private List<Event> eventListFull;
    private String selectedCategory;

    public AdapterRecipe(List<Event> eventList) {
        this.eventList = eventList;
        this.eventListFull = new ArrayList<>(eventList);
        this.selectedCategory = "All";
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_home, parent, false);
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

    public void filterByMatchingIds(List<String> matchingRecipeIds) {
        List<Event> filteredList = new ArrayList<>();

        for (String recipeId : matchingRecipeIds) {
            for (Event event : eventListFull) {
                if (event.getId().equals(recipeId)) {
                    filteredList.add(event);
                }
            }
        }

        Collections.sort(filteredList, (event1, event2) -> event2.getId().compareTo(event1.getId()));


        eventList.clear();
        eventList.addAll(filteredList);
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private String recipeId;
        private ImageView imageView;
        private TextView titleTextView;
        private TextView subtitleTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            subtitleTextView = itemView.findViewById(R.id.subtitleTextView);
            itemView.setOnClickListener(v -> {
                if (recipeId != null) {
                    // Create a new fragment instance and pass the recipe ID
                    IngredientsFragment fragment = IngredientsFragment.newInstance(recipeId);

                    // Switch to the new fragment
                    FragmentManager fragmentManager = ((AppCompatActivity) itemView.getContext()).getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.framelayout1, fragment) // Replace R.id.container with your fragment container ID
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        public void bind(Event event) {
            Picasso.get().load(event.getImageView()).into(imageView);
            titleTextView.setText(event.getTitleTextView());
            subtitleTextView.setText(event.getSubtitleTextView());
            recipeId = event.getId();
        }
    }

    public static class Event {
        private String id;
        private String imageView;
        private String titleTextView;
        private String subtitleTextView;
        private String category;

        public Event(String id, String imageView, String titleTextView, String subtitleTextView, String category) {
            this.id = id;
            this.imageView = imageView;
            this.titleTextView = titleTextView;
            this.subtitleTextView = subtitleTextView;
            this.category = category;
        }

        public String getId() {
            return id;
        }
        public String getImageView() {
            return imageView;
        }

        public String getTitleTextView() {
            return titleTextView;
        }

        public String getSubtitleTextView() {
            return subtitleTextView;
        }
        public String getCategory() { return category;}
    }
    public void filter(String query) {
        query = query.toLowerCase().trim();
        List<Event> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList.addAll(eventListFull);
        } else {
            for (Event event : eventListFull) {
                if (event.getTitleTextView().toLowerCase().contains(query)) {
                    filteredList.add(event);
                }
            }
        }

        eventList.clear();
        eventList.addAll(filteredList);
        notifyDataSetChanged();
    }

    public void setCategory(String category) {
        selectedCategory = category;
        applyFilter();
    }

    private void applyFilter() {
        eventList.clear();

        // Apply the filter based on the selected category
        if (selectedCategory.equals("All")) {
            eventList.addAll(eventListFull);
        } else {
            for (Event event : eventListFull) {
                // Check if the event belongs to the selected category
                if (event.getCategory().equals(selectedCategory)) {
                    eventList.add(event);
                }
            }
        }

        notifyDataSetChanged();
    }
}
