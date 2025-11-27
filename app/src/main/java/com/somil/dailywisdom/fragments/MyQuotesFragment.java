package com.somil.dailywisdom.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.somil.dailywisdom.R;
import com.somil.dailywisdom.adapters.QuoteAdapter;
import com.somil.dailywisdom.models.QuoteModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class MyQuotesFragment extends Fragment {

    private static final String TAG = "MyQuotesFragment"; // For easier logging

    private RecyclerView myQuotesRecyclerView;
    private TextView noQuotesTextView;
    private QuoteAdapter quoteAdapter;
    private List<QuoteModel> myQuotesList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    public MyQuotesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach called. Fragment instance hash: " + this.hashCode());
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (myQuotesList == null) {
            myQuotesList = new ArrayList<>();
            Log.d(TAG, "myQuotesList initialized in onAttach.");
        }
        if (quoteAdapter == null) {
            quoteAdapter = new QuoteAdapter(context, myQuotesList);
            Log.d(TAG, "QuoteAdapter initialized in onAttach. Adapter hash: " + quoteAdapter.hashCode());
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called. Fragment instance hash: " + this.hashCode());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called. Fragment instance hash: " + this.hashCode());
        View view = inflater.inflate(R.layout.fragment_my_quotes, container, false);

        myQuotesRecyclerView = view.findViewById(R.id.myQuotesRecyclerView);
        noQuotesTextView = view.findViewById(R.id.noQuotesTextView);

        myQuotesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        if (quoteAdapter == null) {
            quoteAdapter = new QuoteAdapter(requireContext(), myQuotesList);
            Log.d(TAG, "QuoteAdapter initialized in onCreateView (fallback). Adapter hash: " + quoteAdapter.hashCode());
        }
        myQuotesRecyclerView.setAdapter(quoteAdapter);

        fetchMyQuotes();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called. Fragment instance hash: " + this.hashCode());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView called. Fragment instance hash: " + this.hashCode());
        myQuotesRecyclerView = null;
        noQuotesTextView = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach called. Fragment instance hash: " + this.hashCode());
        quoteAdapter = null;
        myQuotesList = null;
    }

    private void fetchMyQuotes() {
        if (currentUser == null) {
            noQuotesTextView.setVisibility(View.VISIBLE);
            Log.d(TAG, "fetchMyQuotes: Current user is null. Showing no quotes text.");
            return;
        }

        Log.d(TAG, "fetchMyQuotes: Fetching quotes for user: " + currentUser.getUid());
        db.collection("quotes")
                .whereEqualTo("authorId", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(requireContext(), "Error loading quotes: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        noQuotesTextView.setVisibility(View.VISIBLE);
                        Log.e(TAG, "Error loading quotes: " + error.getMessage(), error);
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        myQuotesList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            QuoteModel quote = doc.toObject(QuoteModel.class);
                            quote.setQuoteId(doc.getId());
                            myQuotesList.add(quote);
                        }
                        if (quoteAdapter != null) {
                            quoteAdapter.updateQuoteList(myQuotesList);
                            Log.d(TAG, "Quotes fetched successfully. Count: " + myQuotesList.size() + ". Adapter hash: " + quoteAdapter.hashCode());
                        } else {
                            Log.e(TAG, "Error: quoteAdapter is NULL during fetchMyQuotes update!");
                        }
                        noQuotesTextView.setVisibility(View.GONE);
                    } else {
                        myQuotesList.clear();
                        if (quoteAdapter != null) {
                            quoteAdapter.updateQuoteList(myQuotesList);
                        }
                        noQuotesTextView.setVisibility(View.VISIBLE);
                        Log.d(TAG, "No quotes found for the current user.");
                    }
                });
    }

}