package com.jimuel.recifind.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jimuel.recifind.R;
import com.jimuel.recifind.adapters.AdapterUploadingIngredients;
import com.jimuel.recifind.adapters.AdapterUploadingInstructions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BookmarkFragment extends Fragment {

    private RecyclerView recyclering, recyclerins;
    private AdapterUploadingIngredients adapter;
    private List<AdapterUploadingIngredients.RecipeItem> itemList;
    private List<AdapterUploadingInstructions.InstructionItem> itemList2;
    private AdapterUploadingInstructions adapter2;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;

    public BookmarkFragment() {
        // Required empty public constructor
    }

    public static BookmarkFragment newInstance() {
        return new BookmarkFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookmark, container, false);
    }

    private Button addButton, addButton2;
    private ImageButton imageButton;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private EditText editTextRecipeName, editTextServingCount, editTextMinute;
    private Button buttonBreakfast, buttonLunch, buttonDinner, storeButton;
    String selectedCategory;
    private int count = 1;

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseApp.initializeApp(requireContext());

        addButton = view.findViewById(R.id.btnaddingre);
        addButton2 = view.findViewById(R.id.btnaddintru);
        storeButton = view.findViewById(R.id.storeButton);
        imageButton = view.findViewById(R.id.imagebutton);
        editTextRecipeName = view.findViewById(R.id.editTextRecipeName);
        editTextServingCount = view.findViewById(R.id.editTextServingCount);
        editTextMinute = view.findViewById(R.id.editTextMinute);
        imageButton.setOnClickListener(v -> openImagePicker());
        buttonBreakfast = view.findViewById(R.id.buttonBreakfast);
        buttonLunch = view.findViewById(R.id.buttonLunch);
        buttonDinner = view.findViewById(R.id.buttonDinner);

        buttonBreakfast.setOnClickListener(v -> {
            selectedCategory = "Breakfast";
            updateButtonBackground(buttonBreakfast);
        });
        buttonLunch.setOnClickListener(v -> {
            selectedCategory = "Lunch";
            updateButtonBackground(buttonLunch);
        });
        buttonDinner.setOnClickListener(v -> {
            selectedCategory = "Dinner";
            updateButtonBackground(buttonDinner);
        });

        // Set click listener for the add button
        addButton.setOnClickListener(v -> {
            // Create a new RecipeItem object with the entered data
            AdapterUploadingIngredients.RecipeItem item = new AdapterUploadingIngredients.RecipeItem(null, null);

            // Add the item to the itemList
            itemList.add(item);

            // Notify the adapter that a new item is inserted
            adapter.notifyItemInserted(itemList.size() - 1);

        });

        recyclering = view.findViewById(R.id.ingredientRV);
        recyclering.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        itemList = new ArrayList<>();
        adapter = new AdapterUploadingIngredients(itemList);
        recyclering.setAdapter(adapter);


        addButton2.setOnClickListener(v -> {
            // Create a new RecipeItem object with the entered data
            AdapterUploadingInstructions.InstructionItem item2 = new AdapterUploadingInstructions.InstructionItem(String.valueOf(count), null);

            count++;
            // Add the item to the itemList
            itemList2.add(item2);

            // Notify the adapter that a new item is inserted
            adapter2.notifyItemInserted(itemList2.size() - 1);

        });

        recyclerins = view.findViewById(R.id.instructionRV);
        recyclerins.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        itemList2 = new ArrayList<>();
        adapter2 = new AdapterUploadingInstructions(itemList2);
        recyclerins.setAdapter(adapter2);


        storeButton.setOnClickListener(v -> uploadData());


        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        selectedImageUri = data.getData();
                        // Set the selected image to the imageButton
                        imageButton.setImageURI(selectedImageUri);
                    }
                });

    }

    private void updateButtonBackground(Button selectedButton) {
        // Reset the background and text color for all buttons
        buttonBreakfast.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_background));
        buttonBreakfast.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        buttonLunch.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_background));
        buttonLunch.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        buttonDinner.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_background));
        buttonDinner.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));

        // Change the background and text color of the selected button
        selectedButton.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.gradient_green));
        selectedButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
    }
    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    private boolean containsItemWithDot(List<? extends AdapterUploadingIngredients.RecipeItem> itemList) {
        for (AdapterUploadingIngredients.RecipeItem item : itemList) {
            if (item.getIngredient() != null && item.getIngredient().startsWith(".")) {
                return true;
            }
            if (item.getMeasurement() != null && item.getMeasurement().startsWith(".")) {
                return true;
            }
        }
        return false;
    }
    private boolean containsItemWithDot2(List<? extends AdapterUploadingInstructions.InstructionItem> itemList) {
        for (AdapterUploadingInstructions.InstructionItem item : itemList) {
            if (item.getInstructionstep() != null && item.getInstructionstep().startsWith(".")) {
                return true;
            }
            if (item.getInstructionguide() != null && item.getInstructionguide().startsWith(".")) {
                return true;
            }
        }
        return false;
    }
    private void uploadData() {
        String recipename = editTextRecipeName.getText().toString().trim();
        String servingcount = editTextServingCount.getText().toString().trim();
        String minute = editTextMinute.getText().toString().trim();


        // Check if any required field is empty
        if (recipename.isEmpty() || servingcount.isEmpty() || minute.isEmpty()
                || recipename.startsWith(".") || servingcount.startsWith(".") || minute.startsWith(".")) {
            Toast.makeText(getContext(), "Please fill in all the required fields correctly", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedCategory.isEmpty()){
            Toast.makeText(getContext(), "Please select food category", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if ingredients list is empty
        if (itemList.isEmpty() || isItemListEmpty() || containsItemWithDot(itemList)) {
            Toast.makeText(getContext(), "Please add ingredients correctly", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if instructions list is empty
        if (itemList2.isEmpty() || isItemList2Empty() || containsItemWithDot2(itemList2)) {
            Toast.makeText(getContext(), "Please add instructions correctly", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if an image is selected
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        storeButton.setEnabled(false);
        showLoadingScreen();

        DatabaseReference newItemRef = FirebaseDatabase.getInstance().getReference().child("Recipes").push();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images");

        // Upload the selected image to Firebase Cloud Storage
        StorageReference imageRef = storageRef.child(selectedImageUri.getLastPathSegment());
        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Image uploaded successfully, get the download URL
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageURL = uri.toString();
                        // Store the image URL in the Realtime Database
                        newItemRef.child("imageURL").setValue(imageURL);

                        // Store the remaining data
                        storeRemainingData(newItemRef, recipename, servingcount, minute);

                        resetForm();
                        storeButton.setEnabled(true);
                        hideLoadingScreen();
                    }).addOnFailureListener(e -> {
                        // Handle any errors during getting the download URL
                        Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                        storeButton.setEnabled(true);
                        hideLoadingScreen();
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle any errors during the image upload
                    Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    storeButton.setEnabled(true);
                    hideLoadingScreen();
                });
    }


    private void resetForm() {
        Activity activity = requireActivity();
        activity.runOnUiThread(() -> {
            editTextRecipeName.setText("");
            editTextServingCount.setText("");
            editTextMinute.setText("");
            imageButton.setImageDrawable(null);
            selectedImageUri = null;
            itemList.clear();
            adapter.notifyDataSetChanged();
            itemList2.clear();
            adapter2.notifyDataSetChanged();
        });
    }


    private void showLoadingScreen() {
        // Show the loading screen (e.g., progress bar, loading spinner, etc.)
        // You can implement this according to your UI design
        // For example, you can show/hide a ProgressBar or set the visibility of a loading View
        // Here, I'm assuming you have a ProgressBar with id progressBar in your layout
        ProgressBar progressBar = requireView().findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoadingScreen() {
        // Hide the loading screen
        ProgressBar progressBar = requireView().findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
    }

    private boolean isItemListEmpty() {
        for (AdapterUploadingIngredients.RecipeItem item : itemList) {
            String ingredient = item.getIngredient();
            String measurement = item.getMeasurement();

            if ((ingredient != null && !ingredient.trim().isEmpty())
                    || (measurement != null && !measurement.trim().isEmpty())){
                return false;
            }
        }
        return true;
    }

    private boolean isItemList2Empty() {
        // Check if the itemList2 contains any non-empty items
        for (AdapterUploadingInstructions.InstructionItem item : itemList2) {
            String instructionStep = item.getInstructionstep();
            String instructionGuide = item.getInstructionguide();

            // Check if both the step and guide are empty
            if ((instructionStep != null && !instructionStep.trim().isEmpty())
                    || (instructionGuide != null && !instructionGuide.trim().isEmpty())) {
                return false; // Found a non-empty item
            }
        }
        return true; // All items are empty or null
    }



    private void storeRemainingData(DatabaseReference newItemRef, String recipename, String servingcount, String minute) {
        // Store all the data from the RecyclerView
        for (AdapterUploadingIngredients.RecipeItem item : itemList) {
            String ingredient = item.getIngredient();
            String measurement = item.getMeasurement();

            // Store the item data in the Firebase Realtime Database
            if (ingredient != null && measurement != null && !ingredient.isEmpty() && !measurement.isEmpty()) {
                newItemRef.child("Ingredients").child(ingredient).setValue(measurement);
            }
        }
        for (AdapterUploadingInstructions.InstructionItem item2 : itemList2) {
            String ingredient = item2.getInstructionstep();
            String measurement = item2.getInstructionguide();

            // Store the item data in the Firebase Realtime Database
            if (ingredient != null && measurement != null && !ingredient.isEmpty() && !measurement.isEmpty()) {
                newItemRef.child("Instructions").child(ingredient).setValue(measurement);
            }
        }

        newItemRef.child("Title").setValue(recipename);
        newItemRef.child("Serving").setValue(servingcount);
        newItemRef.child("Time").setValue(minute);
        newItemRef.child("Category").setValue(selectedCategory);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String key = auth.getUid();
        newItemRef.child("Owner").setValue(key);

        DatabaseReference tokenref = FirebaseDatabase.getInstance().getReference().child("users");
        tokenref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot tokensnap : snapshot.getChildren()){
                    String token = tokensnap.child("FCMToken").getValue(String.class);
                    if(token!=null){
                        sendNotificationToDevice(token, recipename);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendNotificationToDevice(String token, String recipeName) {
        String serverKey = "AAAAKCKKgcw:APA91bFlTY7SEf6tZHC_SxW15Zybl4ojOE1HLTuUWFS9I3LcRoLrM0HtN_L8Cx9jCBMgUrNq5B71Cpx2rZRqdq7srKaDCn3d5tPF797puUvfOwyT4yTWTgKBVcE2SqcJEgRWDQjlBXjg";
        String notificationTitle = "Recipe Update";
        String notificationMessage = "New Recipe : " + recipeName;

        try {
            JSONObject notification = new JSONObject();
            notification.put("title", notificationTitle);
            notification.put("body", notificationMessage);

            JSONObject data = new JSONObject();
            data.put("key1", "value1");
            data.put("key2", "value2");

            JSONObject payload = new JSONObject();
            payload.put("notification", notification);
            payload.put("data", data);
            payload.put("to", token);

            // Send the payload to the FCM server
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(mediaType, payload.toString());
            Request request = new Request.Builder()
                    .url("https://fcm.googleapis.com/fcm/send")
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "key=" + serverKey)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                }

            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }






}
