package com.somil.dailywisdom.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.somil.dailywisdom.R;
import com.somil.dailywisdom.adapters.QuoteAdapter;
import com.somil.dailywisdom.utils.NetworkUtils;
import com.somil.dailywisdom.viewmodels.QuoteListViewModel;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;

public class QuoteListFragment extends Fragment {

    private RecyclerView quoteRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private QuoteListViewModel viewModel;
    private QuoteAdapter quoteAdapter;
    private AdView adView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quote_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(QuoteListViewModel.class);

        initViews(view);
        setupRecyclerView();
        observeViewModel();
        setupAds();
        setupToolbar();

        if (NetworkUtils.isNetworkAvailable(requireContext())) {
            viewModel.listenForQuotes();
        } else {
            showEmpty("No internet connection.");
        }
    }

    private void initViews(View view) {
        quoteRecyclerView = view.findViewById(R.id.quoteRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyTextView = view.findViewById(R.id.emptyTextView);
        adView = view.findViewById(R.id.adView);
    }

    private void setupRecyclerView() {
        quoteAdapter = new QuoteAdapter(getContext(), new ArrayList<>());
        quoteRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        quoteRecyclerView.setAdapter(quoteAdapter);
    }

    private void setupToolbar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            Toolbar toolbar = activity.findViewById(R.id.toolbar);
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setTitle("All Quotes");
        }
    }

    private void observeViewModel() {
        viewModel.quoteList.observe(getViewLifecycleOwner(), quotes -> {
            if (quotes != null && !quotes.isEmpty()) {
                showData();
                quoteAdapter.updateQuoteList(quotes);
            } else if (quotes != null && quotes.isEmpty()) {
                showEmpty("No quotes found.");
            }
        });

        viewModel.isFirstPageLoading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                showLoading();
            }
        });

        viewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showEmpty(error);
            }
        });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        quoteRecyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.GONE);
    }

    private void showData() {
        progressBar.setVisibility(View.GONE);
        quoteRecyclerView.setVisibility(View.VISIBLE);
        emptyTextView.setVisibility(View.GONE);
    }

    private void showEmpty(String message) {
        progressBar.setVisibility(View.GONE);
        quoteRecyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.VISIBLE);
        emptyTextView.setText(message);
    }

    private void setupAds() {
        MobileAds.initialize(requireContext());
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

}