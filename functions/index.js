// functions/index.js

const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp(); // Initialize Firebase Admin SDK

// This function will be triggered whenever a quote document is updated in Firestore
// Specifically, when 'quotes/{quoteId}' document changes
exports.sendLikeNotification = functions.firestore
    .document("quotes/{quoteId}")
    .onUpdate(async (change, context) => {
        const newValue = change.after.data();    // The new data of the document
        const previousValue = change.before.data(); // The data before the update

        // 1. Check if it's actually a 'like' (likeCount increased)
        if (newValue.likeCount <= previousValue.likeCount) {
            console.log("No new like detected or likeCount decreased. Skipping notification.");
            return null;
        }

        // 2. Identify the user who liked the quote
        const newLikedByUsers = newValue.likedBy || [];
        const previousLikedByUsers = previousValue.likedBy || [];

        let likerUserId = null;
        if (newLikedByUsers.length > previousLikedByUsers.length) {
            // Find the ID of the user who was just added to the 'likedBy' array
            const newlyAddedUsers = newLikedByUsers.filter(userId => !previousLikedByUsers.includes(userId));
            if (newlyAddedUsers.length > 0) {
                likerUserId = newlyAddedUsers[0]; // Assume only one user likes at a time
            }
        }

        if (!likerUserId) {
            console.log("Could not determine the liker's user ID. Skipping notification.");
            return null;
        }

        // 3. Get the author ID of the quote
        const quoteAuthorId = newValue.userId; // Assuming 'userId' in the quote document is the author's ID
        const quoteText = newValue.text;     // The text of the quote for notification body

        // 4. Don't send notification if a user likes their own quote
        if (likerUserId === quoteAuthorId) {
            console.log("Liker is the author. No notification sent for self-like.");
            return null;
        }

        // 5. Get the name of the user who liked the quote
        const likerUserDoc = await admin.firestore().collection("users").doc(likerUserId).get();
        const likerUserName = likerUserDoc.exists && likerUserDoc.data().name ? likerUserDoc.data().name : "Someone";

        // 6. Get the FCM Token of the quote's author (the recipient of the notification)
        const authorUserDoc = await admin.firestore().collection("users").doc(quoteAuthorId).get();
        const recipientFCMToken = authorUserDoc.exists && authorUserDoc.data().fcmToken ? authorUserDoc.data().fcmToken : null;

        if (!recipientFCMToken) {
            console.log(`Recipient (author ID: ${quoteAuthorId}) does not have an FCM token. Skipping notification.`);
            return null;
        }

        // 7. Construct the notification payload
        const payload = {
            notification: {
                title: "Your quote got a like!",
                body: `${likerUserName} liked your quote: "${quoteText.substring(0, Math.min(quoteText.length, 50))}..."`, // Truncate quote text
                icon: "default", // You can specify a custom icon here
                // click_action: "YOUR_APP_ACTIVITY" // Optional: specify activity to open on click (e.g., "MainActivity")
            },
            data: {
                type: "quote_liked",
                quoteId: context.params.quoteId,
                likerId: likerUserId,
                authorId: quoteAuthorId
            }
        };

        // 8. Send the notification via FCM
        try {
            const response = await admin.messaging().sendToDevice(recipientFCMToken, payload);
            console.log("Successfully sent notification message:", response);
            // Handle success/failure responses here, e.g., cleaning up invalid tokens
        } catch (error) {
            console.error("Error sending notification message:", error);
        }

        return null; // Function completed
    });