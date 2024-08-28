package com.jimuel.recifind.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.jimuel.recifind.adapters.ChatAdapter;
import com.jimuel.recifind.OpenAiApiService;
import com.jimuel.recifind.R;

import java.util.ArrayList;
import java.util.List;

public class BotFragment extends Fragment {

    private List<ChatMessage> chatMessages;
    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private OpenAiApiService openAiApiService;

    public BotFragment() {
        // Required empty public constructor
    }

    public static BotFragment newInstance() {
        return new BotFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bot, container, false);
    }
    private Handler handler;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the chat conversation list
        chatMessages = new ArrayList<>();

        // Initialize the RecyclerView and its adapter
        recyclerView = view.findViewById(R.id.recyclerViewChat);
        adapter = new ChatAdapter(chatMessages);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Set click listener for the send button
        ImageButton sendButton = view.findViewById(R.id.buttonSend);
        sendButton.setOnClickListener(v -> sendMessage());

        // Initialize the OpenAI API client
        handler = new Handler(Looper.getMainLooper());
        openAiApiService = new OpenAiApiService();
        addBotMessage("Hi, I'm AI Chef Assistant");

        // Fetch the initial message from the bot
        String initialMessage = getString(R.string.prompt); // Get the initial bot message from resources
        openAiApiService.sendMessage(initialMessage, new OpenAiApiService.ResponseListener() {
            @Override
            public void onSuccess(String response) {
                // Handle the response from the OpenAI API
                handler.post(() -> {

                });
            }

            @Override
            public void onFailure(Exception e) {
                // Handle the API call failure
            }
        });
    }

    private void sendMessage() {
        // Get the user input from the EditText
        EditText userInputEditText = requireView().findViewById(R.id.editTextUserInput);
        String userInput = userInputEditText.getText().toString().trim();

        if (!userInput.isEmpty()) {
            // Create a new user message object
            ChatMessage userMessage = new ChatMessage(userInput, true);

            // Add the user message to the chatMessages list and notify the adapter
            chatMessages.add(userMessage);
            adapter.notifyItemInserted(chatMessages.size() - 1);

            // Clear the user input EditText
            userInputEditText.setText("");

            // Send the user message to the OpenAI API for processing
            openAiApiService.sendMessage(userInput, new OpenAiApiService.ResponseListener() {
                @Override
                public void onSuccess(String response) {
                    // Handle the response from the OpenAI API
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            addBotMessage(response);
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    // Handle the API call failure
                    // Run UI-related operations on the main thread
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Update UI here if needed
                        }
                    });
                }
            });
        }
    }

    private void addBotMessage(String message) {
        // Create a new bot message object
        ChatMessage botMessage = new ChatMessage(message, false);

        // Add the bot message to the chatMessages list and notify the adapter
        chatMessages.add(botMessage);
        adapter.notifyItemInserted(chatMessages.size() - 1);

        // Scroll the RecyclerView to the bottom to show the latest message
        recyclerView.scrollToPosition(chatMessages.size() - 1);

        // Run UI-related operations on the main thread
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Update UI here if needed
            }
        });
    }

    public class ChatMessage {
        private String message;
        private boolean isUserMessage;

        public ChatMessage(String message, boolean isUserMessage) {
            this.message = message;
            this.isUserMessage = isUserMessage;
        }

        public String getMessage() {
            return message;
        }

        public boolean isUserMessage() {
            return isUserMessage;
        }
    }
}
