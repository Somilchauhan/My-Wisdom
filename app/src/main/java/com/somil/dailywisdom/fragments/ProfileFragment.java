package com.somil.dailywisdom.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.somil.dailywisdom.LoginActivity;
import com.somil.dailywisdom.R;
import com.somil.dailywisdom.adapters.QuoteAdapter;
import com.somil.dailywisdom.viewmodels.ProfileViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    private Toolbar toolbar;
    private TextView textViewName, textViewEmail;
    private RecyclerView myQuotesRecyclerView;
    private ProfileViewModel viewModel;
    private QuoteAdapter quoteAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        initViews(view);
        setupToolbar();
        setupRecyclerView();
        observeViewModel();
        viewModel.fetchUserProfile();
    }

    private void initViews(View view) {
        toolbar = requireActivity().findViewById(R.id.toolbar);
        textViewName = view.findViewById(R.id.textViewName);
        textViewEmail = view.findViewById(R.id.textViewEmail);
        myQuotesRecyclerView = view.findViewById(R.id.myQuotesRecyclerView);
    }

    private void setupToolbar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setTitle("My Profile");
            }
        }
    }

    private void setupRecyclerView() {
        quoteAdapter = new QuoteAdapter(getContext(), new ArrayList<>());
        myQuotesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        myQuotesRecyclerView.setAdapter(quoteAdapter);
    }

    private void observeViewModel() {
        viewModel.userProfile.observe(getViewLifecycleOwner(), userModel -> {
            if (userModel != null) {
                textViewName.setText(userModel.getName());
                textViewEmail.setText(userModel.getEmail());
            }
        });
        viewModel.userQuotes.observe(getViewLifecycleOwner(), quotes -> {
            if (quotes != null) {
                quoteAdapter.updateQuoteList(quotes);
            }
        });
        viewModel.error.observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.profile_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            showLogoutConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Yes", (dialog, which) -> logoutUser())
                .show();
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}