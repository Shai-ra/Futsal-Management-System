/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

/**
 *
 * @author WELCOME
 */
import javax.swing.JOptionPane;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class AdminController {
    private static final String USER_DATA_FILE = "users.txt";
    private Map<String, String[]> users = new HashMap<>();

    public AdminController() {
        loadUsersFromFile();
    }

    public boolean registerUser(String username, String password, String retypePassword,
            String firstName, String lastName, String dob) {
        // Validation
        if (username.isEmpty() || password.isEmpty() || retypePassword.isEmpty() ||
                firstName.isEmpty() || lastName.isEmpty() || dob.isEmpty()) {
            return false;
        }

        if (!password.equals(retypePassword)) {
            return false;
        }

        // Check if username already exists
        if (users.containsKey(username)) {
            return false;
        }

        // Save user
        users.put(username, new String[] { password, firstName, lastName, dob });
        saveUsersToFile();
        return true;
    }

    public boolean loginUser(String username, String password) {
        if (!users.containsKey(username)) {
            return false;
        }

        String storedPassword = users.get(username)[0];
        return password.equals(storedPassword);
    }

    public String[] getUserData(String username) {
        return users.get(username);
    }

    public boolean validateAdminPrivilege(String username) {
        return username.endsWith(".admin");
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
                    users.put(parts[0], new String[] { parts[1], parts[2], parts[3], parts[4] });
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveUsersToFile() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(USER_DATA_FILE));
            for (Map.Entry<String, String[]> entry : users.entrySet()) {
                String[] userData = entry.getValue();
                String line = entry.getKey() + "," +
                        userData[0] + "," + // password
                        userData[1] + "," + // firstName
                        userData[2] + "," + // lastName
                        userData[3]; // dob
                writer.write(line);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}