/**
 * Shared Firebase configuration and initialization
 * Used across all pages for authentication
 */

import { initializeApp } from 'https://www.gstatic.com/firebasejs/10.7.1/firebase-app.js';
import { getAuth, signInWithEmailAndPassword, createUserWithEmailAndPassword, signInWithPopup, GoogleAuthProvider, signOut, onAuthStateChanged } from 'https://www.gstatic.com/firebasejs/10.7.1/firebase-auth.js';

// Firebase configuration
const firebaseConfig = {
    apiKey: "AIzaSyBxonhWR4zQIgnO8pwiIIbR7c23tkL8I9g",
    authDomain: "the-little-library-2316c.firebaseapp.com",
    projectId: "the-little-library-2316c",
    storageBucket: "the-little-library-2316c.firebasestorage.app",
    messagingSenderId: "791826972952",
    appId: "1:791826972952:web:564e2787ff0b20e9fe5e6d",
    measurementId: "G-ZSLXEWRZM0"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const googleProvider = new GoogleAuthProvider();

// Export Firebase instances and functions
export {
    app,
    auth,
    googleProvider,
    signInWithEmailAndPassword,
    createUserWithEmailAndPassword,
    signInWithPopup,
    signOut,
    onAuthStateChanged
};
