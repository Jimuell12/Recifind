package com.jimuel.recifind.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jimuel.recifind.R;
import com.jimuel.recifind.adapters.AdapterInstructions;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InstructionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InstructionFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String recipeId;

    public InstructionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment InstructionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InstructionFragment newInstance(String param1) {
        InstructionFragment fragment = new InstructionFragment();
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
    private AdapterInstructions adapterInstructions;
    private TextToSpeech textToSpeech;
    private CardView arrowcv;
    private TextView recipeNameTextView;

    private WebView ytvideo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_instruction, container, false);
        recipeNameTextView = view.findViewById(R.id.recipeNameTextView);

        recyclerView = view.findViewById(R.id.instructionsrecyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        retrieveEventListFromDatabase();

        arrowcv = view.findViewById(R.id.arrowcv);

        arrowcv.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.popBackStack();
        });

        textToSpeech = new TextToSpeech(requireContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                // TextToSpeech initialization successful
            } else {
                // TextToSpeech initialization failed
            }
        });

        DatabaseReference reciperef = FirebaseDatabase.getInstance().getReference().child("Recipes")
                .child(recipeId).child("Title");
        reciperef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String recipename = snapshot.getValue(String.class);
                recipeNameTextView.setText(recipename);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ytvideo = view.findViewById(R.id.ytvideo);


        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // Stop the text-to-speech when the fragment is paused
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }


    private void retrieveEventListFromDatabase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("Recipes").child(recipeId).child("Instructions");
        DatabaseReference youtubeURL = FirebaseDatabase.getInstance().getReference().child("Recipes").child(recipeId).child("youtubeURL");
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<AdapterInstructions.Event> eventList = new ArrayList<>();

                for (DataSnapshot ingredientSnapshot : dataSnapshot.getChildren()) {
                    String instructionstep = String.valueOf(ingredientSnapshot.getKey());
                    String instructionguide = ingredientSnapshot.getValue(String.class);
                    AdapterInstructions.Event event = new AdapterInstructions.Event(instructionstep, instructionguide);
                    eventList.add(event);
                }


                adapterInstructions = new AdapterInstructions(eventList, textToSpeech);
                recyclerView.setAdapter(adapterInstructions);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error if retrieval is unsuccessful
                // ...
            }
        });

        youtubeURL.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String URL = snapshot.getValue(String.class);
                    String video = "<iframe width=\"100%\" height=\"100%\" src=\""+URL+"&controls=0&showinfo=0\" title=\"YouTube video player\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" allowfullscreen style=\"margin: 0; padding: 0; border-radius: 12px;\"></iframe>";
                    ytvideo.loadData(video, "text/html", "utf-8");
                    ytvideo.getSettings().setJavaScriptEnabled(true);
                    ytvideo.setWebChromeClient(new WebChromeClient());
                }
                else {
                    String errorMessage = "<div style=\"text-align: center; padding: 20px;\">No video available for this recipe.</div>";
                    String videoWithMessage = "<div style=\"position: relative; width: 100%; height: 100%;\"><div style=\"position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);\">" + errorMessage + "</div></div>";
                    ytvideo.loadData(videoWithMessage, "text/html", "utf-8");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}