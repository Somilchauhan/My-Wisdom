package com.somil.dailywisdom.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.somil.dailywisdom.R;
import com.somil.dailywisdom.models.QuoteModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class QuoteAdapter extends RecyclerView.Adapter<QuoteAdapter.QuoteViewHolder> {

    private static final String TAG = "QuoteAdapterDebug";

    private final Context context;
    private List<QuoteModel> quoteList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentUserId;


    public QuoteAdapter(Context context, List<QuoteModel> quoteList) {
        this.context = context;
        this.quoteList = quoteList;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        setHasStableIds(true);
        Log.d(TAG, "QuoteAdapter constructor called. Instance hash: " + this.hashCode() + ". Stable IDs enabled.");
    }

    @Override
    public long getItemId(int position) {
        if (quoteList != null && position >= 0 && position < quoteList.size() && quoteList.get(position).getQuoteId() != null) {
            return quoteList.get(position).getQuoteId().hashCode();
        }
        return RecyclerView.NO_ID;
    }

    @NonNull
    @Override
    public QuoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_quote, parent, false);
        Log.d(TAG, "onCreateViewHolder: New ViewHolder created. Adapter Instance hash: " + this.hashCode());
        return new QuoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuoteViewHolder holder, int position) {
        if (quoteList != null && position < quoteList.size()) {
            QuoteModel quote = quoteList.get(position);
            holder.bind(quote);
            Log.d(TAG, "onBindViewHolder: Bound quote at position " + position + " - " + quote.getText() + ". Adapter Instance hash: " + this.hashCode());
        } else {
            Log.w(TAG, "onBindViewHolder: Attempted to bind out of bounds position: " + position + ". Adapter Instance hash: " + this.hashCode());
        }
    }

    @Override
    public int getItemCount() {
        return quoteList != null ? quoteList.size() : 0;
    }


    public void updateQuoteList(List<QuoteModel> newQuoteList) {
        this.quoteList = newQuoteList;
        notifyDataSetChanged();
        Log.d(TAG, "updateQuoteList called on Adapter instance " + this.hashCode() + ". New list size: " + (newQuoteList != null ? newQuoteList.size() : 0));
    }

    public class QuoteViewHolder extends RecyclerView.ViewHolder {
        TextView quoteText, quoteAuthor, likeCountText;
        ImageView quoteImage;
        ImageButton btnCopy, btnShare, btnLike;

        public QuoteViewHolder(@NonNull View itemView) {
            super(itemView);
            quoteText = itemView.findViewById(R.id.quoteText);
            quoteAuthor = itemView.findViewById(R.id.quoteAuthor);
            likeCountText = itemView.findViewById(R.id.likeCountText);
            quoteImage = itemView.findViewById(R.id.quoteImage);
            btnCopy = itemView.findViewById(R.id.btnCopy);
            btnShare = itemView.findViewById(R.id.btnShare);
            btnLike = itemView.findViewById(R.id.btnLike);


            Log.d(TAG, "QuoteViewHolder created. Associated with Adapter instance hash: " + QuoteAdapter.this.hashCode());

            btnLike.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    toggleLikeStatus(position);
                    Log.d(TAG, "Like button clicked at position: " + position + ". Adapter Instance hash: " + QuoteAdapter.this.hashCode());
                }
            });

            btnCopy.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    QuoteModel quote = quoteList.get(position);
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    String textToCopy = "\"" + quote.getText() + "\" — " + quote.getAuthor();
                    ClipData clip = ClipData.newPlainText("Quote", textToCopy);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(context, "Quote copied!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Copy button clicked at position: " + position + ". Adapter Instance hash: " + QuoteAdapter.this.hashCode());
                }
            });

            btnShare.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    QuoteModel quote = quoteList.get(position);
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    String shareText = "\"" + quote.getText() + "\" — " + quote.getAuthor();
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                    context.startActivity(Intent.createChooser(shareIntent, "Share Quote via"));
                    Log.d(TAG, "Share button clicked at position: " + position + ". Adapter Instance hash: " + QuoteAdapter.this.hashCode());
                }
            });

        }

        void bind(QuoteModel quote) {
            quoteText.setText(quote.getText());
            quoteAuthor.setText("~ " + quote.getAuthor());
            likeCountText.setText(String.valueOf(quote.getLikeCount()));

            if (currentUserId != null && quote.getLikedBy().contains(currentUserId)) {
                btnLike.setImageResource(R.drawable.ic_favorite_filled);
                btnLike.setColorFilter(ContextCompat.getColor(context, R.color.red));
            } else {
                btnLike.setImageResource(R.drawable.ic_favorite_outline);
                btnLike.setColorFilter(ContextCompat.getColor(context, R.color.grey));
            }

            if (quote.getImageUrl() != null && !quote.getImageUrl().isEmpty()) {
                quoteImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(quote.getImageUrl())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(quoteImage);
            } else {
                quoteImage.setVisibility(View.GONE);
                Glide.with(context).clear(quoteImage);
            }

        }
    }

    private void toggleLikeStatus(int position) {
        if (currentUserId == null) {
            Toast.makeText(context, "You must be logged in to like a quote.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Attempted to like without being logged in.");
            return;
        }

        QuoteModel quote = quoteList.get(position);
        String quoteId = quote.getQuoteId();

        if (quoteId == null || quoteId.isEmpty()) {
            Log.e(TAG, "Cannot toggle like status: Quote ID is null or empty for position " + position);
            return;
        }

        boolean isCurrentlyLiked = quote.getLikedBy().contains(currentUserId);

        if (isCurrentlyLiked) {
            quote.getLikedBy().remove(currentUserId);
            quote.setLikeCount(quote.getLikeCount() - 1);
        } else {
            quote.getLikedBy().add(currentUserId);
            quote.setLikeCount(quote.getLikeCount() + 1);
        }
        notifyItemChanged(position);

        if (isCurrentlyLiked) {
            db.collection("quotes").document(quoteId)
                    .update("likedBy", FieldValue.arrayRemove(currentUserId), "likeCount", FieldValue.increment(-1))
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Successfully unliked quote: " + quoteId))
                    .addOnFailureListener(e -> {
                        quote.getLikedBy().add(currentUserId);
                        quote.setLikeCount(quote.getLikeCount() + 1);
                        notifyItemChanged(position);
                        Toast.makeText(context, "Failed to unlike. Please try again.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to unlike quote: " + quoteId, e);
                    });
        } else {
            db.collection("quotes").document(quoteId)
                    .update("likedBy", FieldValue.arrayUnion(currentUserId), "likeCount", FieldValue.increment(1))
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Successfully liked quote: " + quoteId))
                    .addOnFailureListener(e -> {
                        quote.getLikedBy().remove(currentUserId);
                        quote.setLikeCount(quote.getLikeCount() - 1);
                        notifyItemChanged(position);
                        Toast.makeText(context, "Failed to like. Please try again.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to like quote: " + quoteId, e);
                    });
        }
    }
}