package org.example.utils;

import org.example.entities.User;

/**
 * A simple static class to manage the current user's session.
 * This allows easy access to the logged-in user's data throughout the application
 * without passing the User object between controllers.
 */
public class SessionManager {

    private static User currentUser;

    /**
     * Logs a user in by setting them as the current user.
     * @param user The user who has successfully logged in.
     */
    public static void login(User user) {
        currentUser = user;
    }

    /**
     * Logs the current user out by clearing the session.
     */
    public static void logout() {
        currentUser = null;
    }

    /**
     * Retrieves the currently logged-in user.
     * @return The current User object, or null if no one is logged in.
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Checks if a user is currently logged in.
     * @return true if a user is logged in, false otherwise.
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
