package com.jimuel.recifind.fragments;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.camera.core.CameraX;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jimuel.recifind.CameraPreviewActivity;
import com.jimuel.recifind.R;
import com.jimuel.recifind.adapters.AdapterRecipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    private RecyclerView recyclerView;
    private AdapterRecipe adapterRecipe;
    private Button allButton, breakfastButton, lunchButton, dinnerButton, recommendedButton;
    private SearchView searchView;
    private String selectedCategory = "All";
    private ImageView notifimage;
    private ImageButton cameraButton;
    private List<String> matchingRecipeIds = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        requestPermissionNotification();

        if (!matchingRecipeIds.isEmpty()) {
            adapterRecipe.filterByMatchingIds(matchingRecipeIds);
        }

        // Create an instance of CameraX
        cameraButton = view.findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(v -> {
            requestPermissionCameraAccess();
        });
        notifimage = view.findViewById(R.id.notifimage);
        notifimage.setOnClickListener(v -> {
            requestPermissionNotification();
        });

        recyclerView = view.findViewById(R.id.recyclerview1);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        retrieveEventListFromDatabase();
        adapterRecipe = new AdapterRecipe(new ArrayList<>()); // Initialize adapter
        recyclerView.setAdapter(adapterRecipe);

        searchView = view.findViewById(R.id.searchview);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapterRecipe.filter(newText);
                return false;
            }
        });
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                searchView.setQuery("", false);
            }
        });

        allButton = view.findViewById(R.id.All);
        breakfastButton = view.findViewById(R.id.Breakfast);
        lunchButton = view.findViewById(R.id.Lunch);
        dinnerButton = view.findViewById(R.id.Dinner);
        recommendedButton = view.findViewById(R.id.recommendedButton);
        updateButtonBackground(allButton);
        allButton.setOnClickListener(v -> {
            selectedCategory = "All";
            adapterRecipe.setCategory(selectedCategory);
            updateButtonBackground(allButton);
        });

        breakfastButton.setOnClickListener(v -> {
            selectedCategory = "Breakfast";
            adapterRecipe.setCategory(selectedCategory);
            updateButtonBackground(breakfastButton);
        });

        lunchButton.setOnClickListener(v -> {
            selectedCategory = "Lunch";
            adapterRecipe.setCategory(selectedCategory);
            updateButtonBackground(lunchButton);
        });

        dinnerButton.setOnClickListener(v -> {
            selectedCategory = "Dinner";
            adapterRecipe.setCategory(selectedCategory);
            updateButtonBackground(dinnerButton);
        });

        recommendedButton.setOnClickListener(v -> {
            selectedCategory = "Recommended";
            updateButtonBackground(recommendedButton);

            // Filter the RecyclerView based on recommended recipe IDs
            filterRecipesByRecommendedIds();
        });

        checkUserRecommendedRecipes();


        return view;
    }

    private Map<String, List<String>> recipeIngredientsMap = new HashMap<>();

    private void retrieveEventListFromDatabase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("Recipes");

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<AdapterRecipe.Event> eventList = new ArrayList<>();

                for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                    String recipeId = recipeSnapshot.getKey();
                    String imageUrl = recipeSnapshot.child("imageURL").getValue(String.class);
                    String title = recipeSnapshot.child("Title").getValue(String.class);
                    String ingredients = String.valueOf(recipeSnapshot.child("Ingredients").getChildrenCount());
                    String time = recipeSnapshot.child("Time").getValue(String.class);
                    String category = recipeSnapshot.child("Category").getValue(String.class);

                    DataSnapshot ingredientsSnapshot = recipeSnapshot.child("Ingredients");
                    List<String> ingredientNames = new ArrayList<>();
                    for (DataSnapshot ingredientSnapshot : ingredientsSnapshot.getChildren()) {
                        String ingredientName = ingredientSnapshot.getKey();
                        ingredientNames.add(ingredientName.toLowerCase(Locale.ROOT));
                    }
                    recipeIngredientsMap.put(recipeId, ingredientNames);
                    AdapterRecipe.Event event = new AdapterRecipe.Event(recipeId, imageUrl, title, ingredients + " Ingredients | " + time + " Min", category);
                    eventList.add(event);
                }


                adapterRecipe = new AdapterRecipe(eventList);
                recyclerView.setAdapter(adapterRecipe);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error if retrieval is unsuccessful
                // ...
            }
        });
    }

    private void updateButtonBackground(Button selectedButton) {
        // Reset the background and text color for all buttons
        allButton.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_background));
        allButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        breakfastButton.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_background));
        breakfastButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        lunchButton.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_background));
        lunchButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        dinnerButton.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_background));
        dinnerButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        recommendedButton.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_background));
        recommendedButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));

        // Change the background and text color of the selected button
        selectedButton.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.gradient_green));
        selectedButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
    }


    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1002;
    private void requestPermissionCameraAccess(){
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // If not granted, request the CAMERA permission
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            Intent cameraPreviewIntent = new Intent(getActivity(), CameraPreviewActivity.class);
            cameraPreviewLauncher.launch(cameraPreviewIntent);
        }
    }

    private final ActivityResultLauncher<Intent> cameraPreviewLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        List<String> detectedLabels = data.getStringArrayListExtra("detectedLabels");
                        if (detectedLabels != null) {
                            matchingRecipeIds.clear();
                            Log.d(TAG, String.valueOf(detectedLabels));
                            Toast.makeText(getContext(), detectedLabels.toString(), Toast.LENGTH_SHORT).show();

                            // Loop through all recipes in the list
                            for (Map.Entry<String, List<String>> entry : recipeIngredientsMap.entrySet()) {
                                String recipeId = entry.getKey();
                                List<String> ingredientNamesForRecipe = entry.getValue();

                                for (String label : detectedLabels) {
                                    if (ingredientNamesForRecipe != null && ingredientNamesForRecipe.contains(label)) {
                                        // The detected label is an ingredient name for the current recipe
                                        // Store the recipe ID in the matchingRecipeIds list
                                        matchingRecipeIds.add(recipeId);
                                    }
                                }

                                // Display a Toast message after processing each recipe
                                String toastMessage;
                                if (matchingRecipeIds.contains(recipeId)) {
                                    toastMessage = "Match found for recipe ID: " + recipeId;
                                    recommendedButton.performClick();
                                    String uid = FirebaseAuth.getInstance().getUid();
                                    DatabaseReference recipeRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
                                    for (String matchingRecipeId : matchingRecipeIds) {
                                        recipeRef.child("recommended").child(matchingRecipeId).setValue(true);
                                    }
                                } else {
                                    toastMessage = "No Match for recipe ID: " + recipeId;
                                }
                                Toast.makeText(getContext(), toastMessage, Toast.LENGTH_SHORT).show();
                                Log.d(TAG, toastMessage);
                            }
                        }
                    }
                }
            }
    );



    private void requestPermissionNotification(){
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
        } else {

        }
    }

    private void checkUserRecommendedRecipes() {
        // Get the user's UID
        String uid = FirebaseAuth.getInstance().getUid();

        if (uid != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("recommended");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // User has recommended recipes, click the "Recommended" button
                        recommendedButton.performClick();
                        // Filter the RecyclerView based on the recommended recipes
                        adapterRecipe.filterByMatchingIds(getRecommendedRecipeIds(dataSnapshot));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle any errors that may occur during database retrieval
                    // ...
                }
            });
        }
    }

    private List<String> getRecommendedRecipeIds(DataSnapshot dataSnapshot) {
        List<String> recommendedRecipeIds = new ArrayList<>();
        for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
            Toast.makeText(getContext(), recipeSnapshot.getKey(), Toast.LENGTH_SHORT).show();
        }
        return recommendedRecipeIds;
    }

    // Add this function to retrieve recommended recipe IDs from the database
    private List<String> getRecommendedRecipeIdsFromDatabase() {
        // You need to implement this function to fetch the recommended recipe IDs
        // from the user's database node "recommended."
        // Here is where you will read the data from Firebase and return it as a List<String>.
        // You can refer to your existing code in checkUserRecommendedRecipes.

        // For example:
        String uid = FirebaseAuth.getInstance().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("recommended");

        List<String> recommendedRecipeIds = new ArrayList<>();

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                    // Get the recommended recipe IDs and add them to the list
                    recommendedRecipeIds.add(recipeSnapshot.getKey());
                }

                // After fetching the recommended IDs, filter the recipes
                adapterRecipe.filterByMatchingIds(recommendedRecipeIds);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors that may occur during database retrieval
                // ...
            }
        });

        return recommendedRecipeIds;
    }

    // Add this function to filter recipes by recommended IDs
    private void filterRecipesByRecommendedIds() {
        // Get the recommended recipe IDs
        List<String> recommendedRecipeIds = getRecommendedRecipeIdsFromDatabase();

        // Use the adapter to filter recipes by matching IDs
        adapterRecipe.filterByMatchingIds(recommendedRecipeIds);
    }
}