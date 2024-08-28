package com.jimuel.recifind.adapters;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jimuel.recifind.R;

import java.util.List;

public class AdapterInstructions extends RecyclerView.Adapter<AdapterInstructions.ViewHolder> {
    private List<Event> eventList;
    private TextToSpeech textToSpeech;

    public AdapterInstructions(List<Event> eventList, TextToSpeech textToSpeech) {
        this.eventList = eventList;
        this.textToSpeech = textToSpeech;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_instructions, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.bind(event);
        holder.itemView.setOnClickListener(v -> {
            String textToRead = event.getInstructionguide(); // Replace with the appropriate text from your event object
            if (textToRead != null && !textToRead.isEmpty()) {
                textToSpeech.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null);
            }
        });

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
        private TextView instructionstep;
        private TextView instructionguide;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            instructionstep = itemView.findViewById(R.id.instructionstep);
            instructionguide = itemView.findViewById(R.id.instructionguide);
        }

        public void bind(Event event) {
            instructionstep.setText(event.getInstructionstep());
            instructionguide.setText(event.getInstructionguide());
        }
    }

    public static class Event {
        private String instructionstep;
        private String instructionguide;

        public Event(String instructionstep, String instructionguide) {
            this.instructionstep = instructionstep;
            this.instructionguide = instructionguide;
        }


        public String getInstructionstep() {
            return instructionstep;
        }

        public String getInstructionguide() {
            return instructionguide;
        }

    }

}
