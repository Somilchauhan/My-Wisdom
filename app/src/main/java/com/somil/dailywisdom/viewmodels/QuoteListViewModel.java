package com.somil.dailywisdom.viewmodels;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.somil.dailywisdom.models.QuoteModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class QuoteListViewModel extends ViewModel {

    private static final String TAG = "QuoteListVM";

    public MutableLiveData<List<QuoteModel>> quoteList = new MutableLiveData<>();
    public MutableLiveData<Boolean> isFirstPageLoading = new MutableLiveData<>();
    public MutableLiveData<String> error = new MutableLiveData<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration quoteListener;

    // This is the missing method that your QuoteListFragment needs
    public void listenForQuotes() {
        if (quoteListener != null) {
            quoteListener.remove();
        }

        isFirstPageLoading.setValue(true);

        Query query = db.collection("quotes")
                .orderBy("text")
                .limit(50);

        quoteListener = query.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                error.setValue("Failed to load quotes.");
                isFirstPageLoading.setValue(false);
                return;
            }

            if (snapshots != null) {
                List<QuoteModel> newQuotes = new ArrayList<>();
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    QuoteModel quote = doc.toObject(QuoteModel.class);
                    if (quote != null) {
                        quote.setQuoteId(doc.getId());
                        newQuotes.add(quote);
                    }
                }
                quoteList.setValue(newQuotes);
            }
            isFirstPageLoading.setValue(false);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (quoteListener != null) {
            quoteListener.remove();
        }
    }
}