package com.somil.dailywisdom.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.somil.dailywisdom.models.QuoteModel;
import com.somil.dailywisdom.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ProfileViewModel extends ViewModel {

    public MutableLiveData<UserModel> userProfile = new MutableLiveData<>();
    public MutableLiveData<List<QuoteModel>> userQuotes = new MutableLiveData<>();
    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    public MutableLiveData<String> error = new MutableLiveData<>();

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void fetchUserProfile() {
        isLoading.setValue(true);
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            UserModel user = documentSnapshot.toObject(UserModel.class);
                            userProfile.setValue(user);
                            fetchUserQuotes(uid);
                        } else {
                            error.setValue("User profile not found.");
                            isLoading.setValue(false);
                        }
                    })
                    .addOnFailureListener(e -> {
                        error.setValue("Failed to fetch profile.");
                        isLoading.setValue(false);
                    });
        } else {
            error.setValue("No user is currently logged in.");
            isLoading.setValue(false);
        }
    }

    private void fetchUserQuotes(String uid) {
        // Query to get all quotes where 'authorUid' matches the user's ID
        db.collection("quotes")
                .whereEqualTo("authorUid", uid)
                .orderBy("text", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        List<QuoteModel> quotes = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            QuoteModel quote = doc.toObject(QuoteModel.class);
                            if (quote != null) {
                                quote.setQuoteId(doc.getId());
                                quotes.add(quote);
                            }
                        }
                        userQuotes.setValue(quotes);
                    } else {
                        // It's not an error if a user has no quotes, just an empty list
                        userQuotes.setValue(new ArrayList<>());
                    }
                    isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    error.setValue("Failed to fetch user's quotes.");
                    isLoading.setValue(false);
                });
    }
}