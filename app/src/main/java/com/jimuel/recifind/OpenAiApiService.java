package com.jimuel.recifind;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OpenAiApiService {
    private static final String TAG = "OpenAiApiService";
    private static final String API_KEY = "pk-QXtOnNqEHmyCozvFCEziAqEssPEFOKEjUXHWGeiUoJjLYEKY";
    private static final String API_URL = "https://api.openai.com/v1/completions";

    private final OkHttpClient client = new OkHttpClient();
    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public interface ResponseListener {
        void onSuccess(String responseText);
        void onFailure(Exception e);
    }

    private String chatHistory = ""; // Maintain chat history

    public void sendMessage(String message, ResponseListener listener) {
        try {
            // Append user message to chat history
            chatHistory += message + "\n";

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "text-davinci-003");
            jsonBody.put("prompt", chatHistory); // Use chat history as prompt
            jsonBody.put("max_tokens", 256);
            jsonBody.put("temperature", 0);
            RequestBody requestBody = RequestBody.create(JSON, jsonBody.toString());

            // Create the HTTP request
            Request request = new Request.Builder()
                    .url(API_URL)
                    .header("Authorization", "Bearer " + API_KEY)
                    .post(requestBody)
                    .build();

            // Send the asynchronous API request
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        // Get the response body as a string
                        String responseBody = response.body() != null ? response.body().string() : "";
                        Log.d(TAG, "API response: " + responseBody);

                        try {
                            // Extract the text response from choice.text
                            JSONObject responseObject = new JSONObject(responseBody);
                            JSONArray choicesArray = responseObject.getJSONArray("choices");
                            if (choicesArray.length() > 0) {
                                String responseText = choicesArray.getJSONObject(0).getString("text");

                                // Append AI response to chat history
                                chatHistory += responseText + "\n";

                                // Pass the text response to the success listener
                                listener.onSuccess(responseText);
                            } else {
                                // No choices found in the response
                                listener.onFailure(new Exception("No response choices found"));
                            }
                        } catch (JSONException e) {
                            // Error parsing the response JSON
                            listener.onFailure(e);
                        }
                    } else {
                        // Handle the API response error
                        listener.onFailure(new Exception("API response unsuccessful: " + response.code()));
                    }
                }

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    // Handle the API call failure
                    listener.onFailure(e);
                }
            });
        } catch (JSONException e) {
            // Handle JSON exception
            listener.onFailure(e);
        }
    }
}
