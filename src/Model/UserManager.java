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
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;

public class UserManager {
    private static final String USER_DATA_FILE = "users.txt";
    private Map<String, User> users = new HashMap<>();
    
    public UserManager() {
        loadUsersFromFile();
    }
    
    public boolean registerUser(String username, String password, String firstName, String lastName, String dob) {
        if (users.containsKey(username)) {
            return false; // Username already exists
        }
        
        User newUser = new User(username, password, firstName, lastName, dob);
        users.put(username, newUser);
        saveUsersToFile();
        return true;
    }
    
    public User authenticateUser(String username, String password) {
        User user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }
    
    public boolean userExists(String username) {
        return users.containsKey(username);
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
                    users.put(parts[0], user);
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
            for (User user : users.values()) {
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
}

