package com.jimuel.recifind.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jimuel.recifind.LoginActivity;
import com.jimuel.recifind.R;
import com.jimuel.recifind.adapters.AdapterProfile;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
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
    private com.jimuel.recifind.adapters.AdapterProfile AdapterProfile;
    private LinearLayout faqLayout, privacyLayout, termsLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        faqLayout = view.findViewById(R.id.faqLayout);
        privacyLayout = view.findViewById(R.id.privacyLayout);
        termsLayout = view.findViewById(R.id.termsLayout);
        ImageView profileiv = view.findViewById(R.id.profileiv);
        TextView displayNameTextView = view.findViewById(R.id.displayNameTextView);
        TextView emailTextView = view.findViewById(R.id.emailTextView);

        faqLayout.setOnClickListener(v -> {
            // Open the FAQ fragment
            FAQFragment faqFragment = new FAQFragment();
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.framelayout1, faqFragment)
                    .addToBackStack(null)
                    .commit();
        });

        privacyLayout.setOnClickListener(v -> {
            // Open the Privacy Policy fragment
            PrivacyFragment privacyFragment = new PrivacyFragment();
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.framelayout1, privacyFragment)
                    .addToBackStack(null)
                    .commit();
        });

        termsLayout.setOnClickListener(v -> {
            // Open the Terms and Conditions fragment
            TermsFragment termsFragment = new TermsFragment();
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.framelayout1, termsFragment)
                    .addToBackStack(null)
                    .commit();
        });

        FirebaseAuth.AuthStateListener authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in
                String photoUrl = user.getPhotoUrl().toString();
                String displayName = user.getDisplayName();
                String displayemail = user.getEmail();
                // Handle the photoUrl as needed (e.g., pass it to another activity)
                Picasso.get().load(photoUrl).into(profileiv);
                displayNameTextView.setText(displayName);
                emailTextView.setText(displayemail);

            } else {
                // User is signed out
            }
        };

        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);

        Button logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        });


        recyclerView = view.findViewById(R.id.recyclerown);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        retrieveEventListFromDatabase();
        return view;
    }

    private void retrieveEventListFromDatabase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("Recipes");

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<AdapterProfile.Event> eventList = new ArrayList<>();

                for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {

                    String recipeId = recipeSnapshot.getKey();
                    String imageUrl = recipeSnapshot.child("imageURL").getValue(String.class);
                    String title = recipeSnapshot.child("Title").getValue(String.class);
                    String ingredients = String.valueOf(recipeSnapshot.child("Ingredients").getChildrenCount());
                    String time = recipeSnapshot.child("Time").getValue(String.class);
                    String category = recipeSnapshot.child("Category").getValue(String.class);
                    String owner = recipeSnapshot.child("Owner").getValue(String.class);

                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    String uid = auth.getUid();

                    if(owner.equals(uid)){
                        AdapterProfile.Event event = new AdapterProfile.Event(recipeId, imageUrl, title, ingredients + " Ingredients | " + time + " Min", category);
                        eventList.add(event);
                    }

                }


                AdapterProfile = new AdapterProfile(eventList);
                recyclerView.setAdapter(AdapterProfile);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error if retrieval is unsuccessful
                // ...
            }
        });
    }
}