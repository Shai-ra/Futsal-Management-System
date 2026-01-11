/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author WELCOME
 */
import java.io.*;
import java.util.LinkedList;
import javax.swing.JOptionPane;

public class UserManager {
    private static final String USER_DATA_FILE = "users.txt";
    private LinkedList<User> users;
    
    public UserManager() {
        users = new LinkedList<>();
        loadUsersFromFile();
    }
    
    public boolean registerUser(String username, String password, String firstName, String lastName, String dob) {
        // Check if username already exists in LinkedList
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return false; // Username already exists
            }
        }
        
        User newUser = new User(username, password, firstName, lastName, dob);
        users.add(newUser); // Add to LinkedList
        saveUsersToFile();
        return true;
    }
    
    public User authenticateUser(String username, String password) {
        // Search in LinkedList
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }
    
    public boolean userExists(String username) {
        // Search in LinkedList
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }
    
    // Search users by first name
    public LinkedList<User> searchUsersByFirstName(String firstName) {
        LinkedList<User> results = new LinkedList<>();
        for (User user : users) {
            if (user.getFirstName().toLowerCase().contains(firstName.toLowerCase())) {
                results.add(user);
            }
        }
        return results;
    }
    
    // Get user by username
    public User getUserByUsername(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }
    
    private void loadUsersFromFile() {
        try {
            File file = new File(USER_DATA_FILE);
            if (!file.exists()) {
                file.createNewFile();
                return;
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    User user = new User(parts[0], parts[1], parts[2], parts[3], parts[4]);
                    users.add(user); // Add to LinkedList
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading user data: " + e.getMessage(), 
                                         "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveUsersToFile() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(USER_DATA_FILE));
            for (User user : users) {
                String line = user.getUsername() + "," + 
                             user.getPassword() + "," + 
                             user.getFirstName() + "," + 
                             user.getLastName() + "," + 
                             user.getDateOfBirth();
                writer.write(line);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saving user data: " + e.getMessage(), 
                                         "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Get user count
    public int getUserCount() {
        return users.size();
    }
}