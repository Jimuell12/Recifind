package com.jimuel.recifind.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jimuel.recifind.R;
import com.jimuel.recifind.fragments.BotFragment;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private List<BotFragment.ChatMessage> chatMessages;

    public ChatAdapter(List<BotFragment.ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_chat, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        BotFragment.ChatMessage message = chatMessages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewMessage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.tvmessage);
        }

        public void bind(BotFragment.ChatMessage message) {
            textViewMessage.setText(message.getMessage());

            // Adjust the appearance based on whether it's a user message or bot message
            if (message.isUserMessage()) {
                // Set the background and alignment for user messages
                textViewMessage.setBackgroundResource(R.drawable.user_background);
                textViewMessage.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            } else {
                // Set the background and alignment for bot messages
                textViewMessage.setBackgroundResource(R.drawable.bot_background);
                textViewMessage.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            }
        }
    }
}
