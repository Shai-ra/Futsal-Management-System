/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

/**
 *
 * @author WELCOME
 */
import Model.UserManager;
import Model.User;
import javax.swing.JOptionPane;

public class UserController {
    private UserManager userManager;

    public UserController() {
        this.userManager = new UserManager();
    }

    public boolean registerUser(String username, String password, String retypePassword,
            String firstName, String lastName, String dob) {
        // Validation logic
        if (username.isEmpty() || password.isEmpty() || retypePassword.isEmpty() ||
                firstName.isEmpty() || lastName.isEmpty() || dob.isEmpty()) {
            return false;
        }

        if (!password.equals(retypePassword)) {
            return false;
        }

        // Check if username already exists
        if (userManager.userExists(username)) {
            return false;
        }

        // Register user through UserManager
        return userManager.registerUser(username, password, firstName, lastName, dob);
    }

    public User loginUser(String username, String password) {
        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            return null;
        }

        // Check for admin privilege (ends with .admin)
        if (!username.endsWith(".admin")) {
            return null;
        }

        // Authenticate through UserManager
        return userManager.authenticateUser(username, password);
    }

    public boolean userExists(String username) {
        return userManager.userExists(username);
    }
    
    
}
