package com.jimuel.recifind.fragments;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jimuel.recifind.R;
import com.jimuel.recifind.adapters.AdapterIngredients;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link IngredientsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class IngredientsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String recipeId;

    public IngredientsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment IngredientsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static IngredientsFragment newInstance(String param1) {
        IngredientsFragment fragment = new IngredientsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            recipeId = getArguments().getString(ARG_PARAM1);
        }
    }
    private RecyclerView recyclerView;
    private AdapterIngredients adapterIngredients;
    private TextView recipeNameTextView, ingredientsTextView, servingCountTextView;
    private ImageView recipeImageView;
    private CardView arrowcv;
    private FloatingActionButton fabStartCooking;
    private CardView infoCardView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ingredients, container, false);
        infoCardView = view.findViewById(R.id.info);
        recipeNameTextView = view.findViewById(R.id.recipeNameTextView);
        ingredientsTextView = view.findViewById(R.id.ingredientsTextView);
        servingCountTextView = view.findViewById(R.id.servingCountTextView);
        recipeImageView = view.findViewById(R.id.recipeImageView);
        arrowcv = view.findViewById(R.id.arrowcv);
        fabStartCooking = view.findViewById(R.id.fabStartCooking);

        fabStartCooking.setOnClickListener(v -> {
            InstructionFragment fragment = InstructionFragment.newInstance(recipeId);
            FragmentManager fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.framelayout1, fragment) // Replace R.id.container with your fragment container ID
                    .addToBackStack(null)
                    .commit();
        });

        arrowcv.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.popBackStack();
        });
        recyclerView = view.findViewById(R.id.ingredientsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        retrieveEventListFromDatabase();

        DatabaseReference recipeRef = FirebaseDatabase.getInstance().getReference().child("Recipes").child(recipeId);

            recipeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String recipename = snapshot.child("Title").getValue(String.class);
                        recipeNameTextView.setText(recipename);
                        String imageURL = snapshot.child("imageURL").getValue(String.class);
                        Picasso.get().load(imageURL).into(recipeImageView);
                        String ingredientcount = String.valueOf(snapshot.child("Ingredients").getChildrenCount());
                        ingredientsTextView.setText("Ingredients ("+ingredientcount+")");
                        String servingcount = snapshot.child("Serving").getValue(String.class);
                        servingCountTextView.setText(servingcount + " Serving");
                        String owner = snapshot.child("Owner").getValue(String.class);
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        String uid = auth.getUid();
                        if(owner.equals(uid)){
                            infoCardView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        // You can also navigate back or perform any other appropriate action
                        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                        fragmentManager.popBackStack();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            infoCardView.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(requireContext(), v);
                popupMenu.getMenuInflater().inflate(R.menu.menu_info, popupMenu.getMenu());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    popupMenu.setForceShowIcon(true);
                }
                popupMenu.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.menu_edit) {
                        EditFragment fragment = EditFragment.newInstance(recipeId);
                        FragmentManager fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.framelayout1, fragment) // Replace R.id.container with your fragment container ID
                                .addToBackStack(null)
                                .commit();
                        return true;
                    } else if (itemId == R.id.menu_delete) {
                        showDeleteConfirmationDialog();
                        return true;
                    } else {
                        return false;
                    }
                });
                popupMenu.show();

            });

        return view;
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Delete Confirmation")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Perform the delete action
                    deleteItem();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Cancel the delete action
                    dialog.dismiss();
                })
                .show();
    }

    private void deleteItem() {
        // Perform the delete action in the Realtime Database
        DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference().child("Recipes").child(recipeId);
        itemRef.removeValue();
    }

    private void retrieveEventListFromDatabase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("Recipes").child(recipeId).child("Ingredients");

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<AdapterIngredients.Event> eventList = new ArrayList<>();

                for (DataSnapshot ingredientSnapshot : dataSnapshot.getChildren()) {
                    String ingredientname = String.valueOf(ingredientSnapshot.getKey());
                    String ingredientmeasure = ingredientSnapshot.getValue(String.class);
                    AdapterIngredients.Event event = new AdapterIngredients.Event(ingredientname, ingredientmeasure);
                    eventList.add(event);
                }


                adapterIngredients = new AdapterIngredients(eventList);
                recyclerView.setAdapter(adapterIngredients);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error if retrieval is unsuccessful
                // ...
            }
        });
    }
}