package com.somil.dailywisdom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.somil.dailywisdom.models.QuoteModel;
import com.somil.dailywisdom.utils.NetworkUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddQuoteActivity extends AppCompatActivity {

    private static final String TAG = "AddQuoteActivity";

    private Toolbar toolbar;
    private ImageView imageViewPreview;
    private Button buttonGenerateImage;
    private EditText quoteEditText;
    private EditText authorEditText;

    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private String randomImageUrl = null;

    private String existingQuoteId = null;
    private String existingQuoteText = null;
    private String existingQuoteAuthor = null;
    private String existingQuoteImageUrl = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_quote);
        Log.d(TAG, "onCreate: Activity started.");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        initViews();
        setupToolbar();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("QUOTE_ID")) {
            existingQuoteId = intent.getStringExtra("QUOTE_ID");
            existingQuoteText = intent.getStringExtra("QUOTE_TEXT");
            existingQuoteAuthor = intent.getStringExtra("QUOTE_AUTHOR");
            existingQuoteImageUrl = intent.getStringExtra("QUOTE_IMAGE_URL");

            Log.d(TAG, "onCreate: Edit Mode detected. Quote ID: " + existingQuoteId);
            quoteEditText.setText(existingQuoteText);
            if (authorEditText != null) {
                authorEditText.setText(existingQuoteAuthor);
            }

            if (existingQuoteImageUrl != null && !existingQuoteImageUrl.isEmpty()) {
                randomImageUrl = existingQuoteImageUrl;
                Glide.with(this)
                        .load(existingQuoteImageUrl)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(imageViewPreview);
                Log.d(TAG, "onCreate: Loaded existing image: " + existingQuoteImageUrl);
            }
            buttonGenerateImage.setText("Generate New Image");
        } else {
            Log.d(TAG, "onCreate: Add New Quote Mode.");
            generateRandomImage(); // Generate image on create for new quotes
        }


        buttonGenerateImage.setOnClickListener(v -> {
            Log.d(TAG, "buttonGenerateImage clicked.");
            if (NetworkUtils.isNetworkAvailable(this)) {
                generateRandomImage();
            } else {
                Toast.makeText(this, "No internet connection to generate image.", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "No internet connection for image generation.");
            }
        });
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        imageViewPreview = findViewById(R.id.imageViewPreview);
        buttonGenerateImage = findViewById(R.id.buttonGenerateImage);
        quoteEditText = findViewById(R.id.quoteEditText);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving quote...");
        progressDialog.setCancelable(false);
        Log.d(TAG, "initViews: Views initialized.");
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            if (existingQuoteId != null) {
                getSupportActionBar().setTitle("Edit Quote");
                Log.d(TAG, "setupToolbar: Title set to 'Edit Quote'.");
            } else {
                getSupportActionBar().setTitle("Add New Quote");
                Log.d(TAG, "setupToolbar: Title set to 'Add New Quote'.");
            }
        }
        toolbar.setNavigationOnClickListener(v -> {
            Log.d(TAG, "Toolbar navigation icon clicked. Finishing activity.");
            onBackPressed();
        });
    }

    private void generateRandomImage() {
        randomImageUrl = "https://picsum.photos/800/600?random=" + System.currentTimeMillis();

        String thumbnailUrl = "https://picsum.photos/80/60?random=" + System.currentTimeMillis();

        Toast.makeText(this, "Generating new image...", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "generateRandomImage: Attempting to load image from: " + randomImageUrl);

        Glide.with(this)
                .load(randomImageUrl)
                .thumbnail(Glide.with(this).load(thumbnailUrl))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imageViewPreview);
    }

    private void saveQuoteToFirestore() {
        String quoteText = quoteEditText.getText().toString().trim();
        String authorName = (authorEditText != null && authorEditText.getText() != null) ? authorEditText.getText().toString().trim() : "Anonymous"; // Get author name

        if (quoteText.isEmpty()) {
            Toast.makeText(this, "Please write a quote.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "saveQuoteToFirestore: Quote text is empty.");
            return;
        }

        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to post/edit.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "saveQuoteToFirestore: Current user is null, cannot save quote.");
            return;
        }

        progressDialog.show();
        Log.d(TAG, "saveQuoteToFirestore: Progress dialog shown.");

        if (existingQuoteId != null) {
            Log.d(TAG, "saveQuoteToFirestore: Updating existing quote with ID: " + existingQuoteId);
            Map<String, Object> quoteUpdates = new HashMap<>();
            quoteUpdates.put("text", quoteText);
            quoteUpdates.put("imageUrl", randomImageUrl);
            quoteUpdates.put("author", authorName); // Update author name as well

            db.collection("quotes").document(existingQuoteId)
                    .update(quoteUpdates)
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Quote updated successfully!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Quote updated successfully: " + existingQuoteId);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Error updating quote: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error updating quote: " + existingQuoteId, e);
                    });

        } else {
            Log.d(TAG, "saveQuoteToFirestore: Adding new quote.");
            String authorUid = currentUser.getUid();
            // Fetch author name from users collection or use default
            db.collection("users").document(authorUid).get().addOnSuccessListener(documentSnapshot -> {
                String fetchedAuthorName = "Anonymous";
                if (documentSnapshot.exists() && documentSnapshot.getString("name") != null) {
                    fetchedAuthorName = documentSnapshot.getString("name");
                }
                // If authorEditText is used, prefer its content, otherwise use fetched/default
                String finalAuthorName = (authorEditText != null && !authorName.isEmpty()) ? authorName : fetchedAuthorName;

                QuoteModel newQuote = new QuoteModel(quoteText, finalAuthorName, randomImageUrl, authorUid, System.currentTimeMillis());

                db.collection("quotes").add(newQuote)
                        .addOnSuccessListener(documentReference -> {
                            progressDialog.dismiss();
                            Toast.makeText(this, "Quote saved successfully!", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "New quote saved successfully with ID: " + documentReference.getId());
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(this, "Failed to save quote: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Failed to save new quote: " + e.getMessage(), e);
                        });
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Failed to get author info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to get author info for new quote: " + e.getMessage(), e);
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_quote_menu, menu);
        Log.d(TAG, "onCreateOptionsMenu: Menu created.");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            Log.d(TAG, "action_save menu item clicked.");
            if (NetworkUtils.isNetworkAvailable(this)) {
                saveQuoteToFirestore();
            } else {
                Toast.makeText(this, "No internet connection to save quote.", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "No internet connection for saving quote.");
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}