package com.somil.dailywisdom.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.somil.dailywisdom.models.QuoteModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import java.util.Random;

public class MainViewModel extends ViewModel {

    public MutableLiveData<QuoteModel> quoteData = new MutableLiveData<>();
    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    public MutableLiveData<String> error = new MutableLiveData<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentUserId;

    public MainViewModel() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        loadNewQuote();
    }

    public void loadNewQuote() {
        isLoading.setValue(true);
        db.collection("quotes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                        int randomIndex = new Random().nextInt(documents.size());
                        DocumentSnapshot doc = documents.get(randomIndex);

                        QuoteModel quote = doc.toObject(QuoteModel.class);
                        if (quote != null) {
                            quote.setQuoteId(doc.getId());
                            quoteData.setValue(quote);
                        } else {
                            error.setValue("Failed to parse quote.");
                        }
                    } else {
                        error.setValue("No quotes found in the database.");
                    }
                    isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    error.setValue("Failed to fetch quote.");
                    isLoading.setValue(false);
                });
    }

    public void toggleLike() {
        if (currentUserId == null || quoteData.getValue() == null) {
            error.postValue("You must be logged in to like a quote.");
            return;
        }

        QuoteModel currentQuote = quoteData.getValue();
        String quoteId = currentQuote.getQuoteId();
        if (quoteId == null || quoteId.isEmpty()){
            error.postValue("Cannot like a quote without an ID.");
            return;
        }

        List<String> likedBy = currentQuote.getLikedBy();
        boolean isCurrentlyLiked = likedBy.contains(currentUserId);

        if (isCurrentlyLiked) {
            likedBy.remove(currentUserId);
            currentQuote.setLikeCount(currentQuote.getLikeCount() - 1);
            db.collection("quotes").document(quoteId)
                    .update("likedBy", FieldValue.arrayRemove(currentUserId), "likeCount", FieldValue.increment(-1));
        } else {
            likedBy.add(currentUserId);
            currentQuote.setLikeCount(currentQuote.getLikeCount() + 1);
            db.collection("quotes").document(quoteId)
                    .update("likedBy", FieldValue.arrayUnion(currentUserId), "likeCount", FieldValue.increment(1));
        }
        quoteData.postValue(currentQuote);
    }
}