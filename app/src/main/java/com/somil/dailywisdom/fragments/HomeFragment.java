package com.somil.dailywisdom.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector; // Added
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent; // Added
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.bumptech.glide.Glide;
import com.somil.dailywisdom.MessagesPlaceholderActivity; // Added
import com.somil.dailywisdom.R;
import com.somil.dailywisdom.utils.NetworkUtils;
import com.somil.dailywisdom.viewmodels.MainViewModel;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import android.util.Log; // Added for debugging gestures

public class HomeFragment extends Fragment {

    private MainViewModel mainViewModel;
    private TextView quoteText, quoteAuthor, likeCountText;
    private ImageView imageBackground;
    private ImageButton btnCopy, btnShare, btnLike;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AdView adView;

    private GestureDetector gestureDetector;
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        gestureDetector = new GestureDetector(getContext(), new MyGestureListener());

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        initViews(view);
        setupToolbar(view);
        setupListeners();
        setupAds();
        observeViewModel();
    }

    private void initViews(View view) {
        quoteText = view.findViewById(R.id.quote_text);
        quoteAuthor = view.findViewById(R.id.quote_author);
        likeCountText = view.findViewById(R.id.like_count_text);
        imageBackground = view.findViewById(R.id.image_background);
        btnCopy = view.findViewById(R.id.btn_copy);
        btnShare = view.findViewById(R.id.btn_share);
        btnLike = view.findViewById(R.id.btn_like);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        adView = view.findViewById(R.id.adView);
    }

    private void setupToolbar(View view) {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            Toolbar toolbar = activity.findViewById(R.id.toolbar);
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setTitle(getString(R.string.app_name));
        }
    }

    private void observeViewModel() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (currentUserId == null) return;

        mainViewModel.quoteData.observe(getViewLifecycleOwner(), quoteModel -> {
            if (quoteModel != null) {
                quoteText.setText(quoteModel.getText());
                quoteAuthor.setText("~ " + quoteModel.getAuthor());
                likeCountText.setText(String.valueOf(quoteModel.getLikeCount()));

                String imageUrl = quoteModel.getImageUrl();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    imageBackground.setVisibility(View.VISIBLE);
                    Glide.with(requireContext()).load(imageUrl).centerCrop().into(imageBackground);
                } else {
                    imageBackground.setVisibility(View.GONE);
                }

                if (quoteModel.getLikedBy().contains(currentUserId)) {
                    btnLike.setImageResource(R.drawable.ic_favorite_filled);
                    btnLike.setColorFilter(ContextCompat.getColor(requireContext(), R.color.red));
                } else {
                    btnLike.setImageResource(R.drawable.ic_favorite_outline);
                    btnLike.setColorFilter(ContextCompat.getColor(requireContext(), R.color.secondaryTextColor));
                }
            }
        });

        mainViewModel.isLoading.observe(getViewLifecycleOwner(), loading -> {
            swipeRefreshLayout.setRefreshing(loading);
        });

        mainViewModel.error.observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (NetworkUtils.isNetworkAvailable(requireContext())) {
                mainViewModel.loadNewQuote();
            } else {
                Toast.makeText(getContext(), "No internet connection.", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        btnCopy.setOnClickListener(v -> copyQuote());
        btnShare.setOnClickListener(v -> shareQuote());
        btnLike.setOnClickListener(v -> mainViewModel.toggleLike());
    }

    private void setupAds() {
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_share_app) {
            shareApp();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareApp() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareMessage = "Check out this awesome Quotes App!\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + requireActivity().getPackageName();
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Share app via"));
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error sharing app.", Toast.LENGTH_SHORT).show();
        }
    }

    private void copyQuote() {
        ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Quote", "\"" + quoteText.getText().toString() + "\" " + quoteAuthor.getText().toString());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getContext(), "Quote copied!", Toast.LENGTH_SHORT).show();
    }

    private void shareQuote() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String shareText = "\"" + quoteText.getText().toString() + "\" " + quoteAuthor.getText().toString();
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    // --- MyGestureListener Inner Class for Swipe Detection ---
    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX < 0) { // It's a left swipe
                        // Start MessagesPlaceholderActivity
                        Intent intent = new Intent(getActivity(), MessagesPlaceholderActivity.class);
                        startActivity(intent);
                        Toast.makeText(getActivity(), "Opening Messages!", Toast.LENGTH_SHORT).show(); // Optional: User feedback
                        Log.d("HomeFragment", "Left swipe detected - Opening MessagesPlaceholderActivity");
                    }
                    result = true;
                }
            } catch (Exception exception) {
                Log.e("HomeFragment", "Error detecting swipe: " + exception.getMessage());
                exception.printStackTrace();
            }
            return result;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }
}