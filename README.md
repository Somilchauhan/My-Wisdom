# My Wisdom - Daily Quotes & Inspiration Engine 📱

![Android](https://img.shields.io/badge/Platform-Android-green) ![Architecture](https://img.shields.io/badge/Architecture-MVVM-blue) ![Database](https://img.shields.io/badge/Database-Firebase_Firestore-orange)

**Live on Play Store:** (https://play.google.com/store/apps/details?id=com.somil.dailywisdom)

## 💡 Overview
My Wisdom is a native Android application designed to deliver curated motivational content. Unlike static apps, this leverages a cloud-based architecture to fetch dynamic content in real-time. The app implements **System Design best practices** including offline caching, efficient state management, and gesture-based navigation.

## 🛠 Tech Stack & Architecture
* **Language:** Java (Native Android)
* **Architecture:** MVVM (Model-View-ViewModel) for robust separation of concerns.
* **Backend:** Firebase Firestore (NoSQL Cloud Database).
* **Reactive UI:** Android LiveData & Observers.
* **Image Loading:** Glide Library.
* **Monetization:** Google AdMob integration.

## ✨ Key Features
1.  **Cloud-Synced Content:** Fetches quotes dynamically from Firestore, allowing remote content updates without app updates.
2.  **Smart State Management:** Uses `ViewModel` to survive configuration changes (screen rotation) and `LiveData` to update UI asynchronously without memory leaks.
3.  **Gesture Navigation:** Custom `GestureDetector` implementation to support "Swipe-to-Navigate" features (e.g., Swipe left to open Messages).
4.  **Social Connectivity:** Deep linking and Intent sharing to share quotes directly to WhatsApp/Instagram.
5.  **Offline Capability:** Caches data to ensure the app works seamlessly even with unstable internet.

## 📱 Code Structure Highlight
The app follows clean coding principles:
* **`MainViewModel.java`**: Handles business logic and data fetching from Firebase. It exposes `MutableLiveData` to the UI, ensuring the View is purely passive.
* **`HomeFragment.java`**: Handles UI rendering and user interactions. It observes the ViewModel logic to update the UI reactively.

## 🚀 Future Improvements
* Migrating to **Kotlin** and **Jetpack Compose**.
* Implementing **Pagination** for fetching quotes to optimize Firestore read costs.

---
*Developed by Somil | 3rd Year B.Tech CSE(DS)*
