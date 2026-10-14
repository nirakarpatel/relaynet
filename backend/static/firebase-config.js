// Firebase Web Configuration for relaynet-001
import { initializeApp } from "https://www.gstatic.com/firebasejs/13.0.0/firebase-app.js";
import { getAnalytics } from "https://www.gstatic.com/firebasejs/13.0.0/firebase-analytics.js";

export const firebaseConfig = {
  apiKey: "AIzaSyA4NHj-TH6eBZDiCff8AglliOfUuzE4PRg",
  authDomain: "relaynet-001.firebaseapp.com",
  projectId: "relaynet-001",
  storageBucket: "relaynet-001.firebasestorage.app",
  messagingSenderId: "752084871127",
  appId: "1:752084871127:web:ac9a252a138a76796f8d92",
  measurementId: "G-WTC3KN78BD"
};

// Initialize Firebase
export const app = initializeApp(firebaseConfig);
export const analytics = getAnalytics(app);
