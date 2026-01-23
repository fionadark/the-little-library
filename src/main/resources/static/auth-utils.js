import { auth, signOut } from './firebase-config.js';

/**
 * Get the current authenticated user
 * @returns {Object|null} The current Firebase user or null
 */
export function getCurrentUser() {
    return auth.currentUser;
}

/**
 * Get the ID token for the current user
 * @returns {Promise<string>} The Firebase ID token
 * @throws {Error} If no user is signed in
 */
export async function getAuthToken() {
    const user = getCurrentUser();
    if (!user) {
        throw new Error('No user is signed in');
    }
    return await user.getIdToken();
}

/**
 * Check if a user is currently signed in
 * @returns {boolean} True if user is signed in, false otherwise
 */
export function isUserSignedIn() {
    return getCurrentUser() !== null;
}

/**
 * Sign out the current user
 * @returns {Promise<void>}
 * @throws {Error} If sign out fails
 */
export async function logoutUser() {
    await signOut(auth);
}

/**
 * Logout and redirect to a specific page
 * @param {string} redirectUrl - URL to redirect to after logout
 * @param {Function} showMessageFn - Optional function to show error messages
 */
export async function logoutAndRedirect(redirectUrl, showMessageFn = null) {
    try {
        await logoutUser();
        window.location.href = redirectUrl;
    } catch (error) {
        if (showMessageFn) {
            showMessageFn('Sign out failed: ' + error.message);
        } else {
            console.error('Sign out failed:', error);
        }
    }
}

/**
 * Get auth token with error handling and user check
 * @param {Function} showMessageFn - Function to show error messages
 * @returns {Promise<string|null>} The token or null if user not signed in
 */
export async function getAuthTokenSafe(showMessageFn) {
    try {
        const user = getCurrentUser();
        if (!user) {
            showMessageFn('Please sign in first');
            return null;
        }
        return await user.getIdToken();
    } catch (error) {
        showMessageFn('Failed to get authentication token: ' + error.message);
        return null;
    }
}
