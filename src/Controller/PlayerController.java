package Controller;

import Model.Player;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.util.LinkedList;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import java.awt.Image;

public class PlayerController {
    private LinkedList<Player> players = new LinkedList<>();
    
    public PlayerController() {
        players = new LinkedList<>();
        prepareInitialPlayerData();
    }
    
    // Add initial sample data
    private void prepareInitialPlayerData() {
        players.add(new Player("P001", "Lionel Messi", 36, 10, "Forward", "Inter Miami"));
        players.add(new Player("P002", "Cristiano Ronaldo", 38, 7, "Forward", "Al Nassr"));
        players.add(new Player("P003", "Neymar Jr", 31, 10, "Forward", "Al Hilal"));
        players.add(new Player("P004", "Kevin De Bruyne", 32, 17, "Midfielder", "Manchester City"));
    }
    
    // Method to handle photo upload (separate from player data)
    public String uploadPhoto() {
    JFileChooser fileChooser = new JFileChooser();
    
    // Set file filter for images only
    FileNameExtensionFilter filter = new FileNameExtensionFilter(
        "Image Files", "jpg", "jpeg", "png", "gif", "bmp");
    fileChooser.setFileFilter(filter);
    
    int result = fileChooser.showOpenDialog(null);
    
    if (result == JFileChooser.APPROVE_OPTION) {
        File selectedFile = fileChooser.getSelectedFile();
        
        // Check if it's an image file
        String fileName = selectedFile.getName().toLowerCase();
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || 
            fileName.endsWith(".png") || fileName.endsWith(".gif") || 
            fileName.endsWith(".bmp")) {
            
            // Return only the filename, not full path
            return selectedFile.getName();
        } else {
            JOptionPane.showMessageDialog(null, 
                "Please select a valid image file (jpg, jpeg, png, gif, bmp)", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    return null; // User cancelled
}
    // Display photo in a JLabel (separate from LinkedList operations)
public void displayPhoto(String photoFileName, JLabel photoLabel) {
    if (photoFileName == null || photoFileName.isEmpty()) {
        photoLabel.setIcon(null);
        photoLabel.setText("No Photo");
        return;
    }
    
    // Look for photo in player_photos directory
    File photoFile = new File("player_photos/" + photoFileName);
    
    if (photoFile.exists()) {
        try {
            ImageIcon icon = new ImageIcon(photoFile.getAbsolutePath());
            
            // Scale image
            if (photoLabel.getWidth() > 0 && photoLabel.getHeight() > 0) {
                Image scaledImage = icon.getImage().getScaledInstance(
                    photoLabel.getWidth(), photoLabel.getHeight(), Image.SCALE_SMOOTH);
                photoLabel.setIcon(new ImageIcon(scaledImage));
            } else {
                Image scaledImage = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                photoLabel.setIcon(new ImageIcon(scaledImage));
            }
            photoLabel.setText("");
        } catch (Exception e) {
            photoLabel.setIcon(null);
            photoLabel.setText("Error loading photo");
            System.err.println("Error loading photo: " + e.getMessage());
        }
    } else {
        photoLabel.setIcon(null);
        photoLabel.setText("Photo not found at: " + photoFile.getPath());
    }
}
    
    // CRUD Operations - Photo handling is separate
    public boolean addPlayer(String playerId, String playerName, String age, 
                            String jerseyNo, String position, String teamName, 
                            String photoPath) {
        // Validation
        if (playerId.isEmpty() || playerName.isEmpty() || age.isEmpty() || 
            jerseyNo.isEmpty() || position.isEmpty() || teamName.isEmpty()) {
            JOptionPane.showMessageDialog(null, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Check if player ID already exists in LinkedList
        for (Player player : players) {
            if (player.getPlayerId().equals(playerId)) {
                JOptionPane.showMessageDialog(null, "Player ID already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        
        try {
            // Validate numeric fields
            int playerAge = Integer.parseInt(age);
            int jerseyNumber = Integer.parseInt(jerseyNo);
            
            if (playerAge <= 0 || playerAge > 100) {
                JOptionPane.showMessageDialog(null, "Age must be between 1 and 100!", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            if (jerseyNumber <= 0 || jerseyNumber > 99) {
                JOptionPane.showMessageDialog(null, "Jersey number must be between 1 and 99!", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Check if jersey number already exists for the same team
            if (isJerseyNumberExists(jerseyNumber, teamName, null)) {
                JOptionPane.showMessageDialog(null, "Jersey number " + jerseyNumber + " already exists for team " + teamName + "!", 
                                             "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Create new player and add to LinkedList
            Player newPlayer = new Player(playerId, playerName, playerAge, jerseyNumber, position, teamName);
            
            // Set photo path separately if provided
            if (photoPath != null && !photoPath.isEmpty()) {
                newPlayer.setPhotoPath(photoPath);
            }
            
            players.add(newPlayer); // Add to end of LinkedList
            
            JOptionPane.showMessageDialog(null, "Player added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            return true;
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Age and Jersey Number must be valid integers!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    public boolean updatePlayer(String playerId, String playerName, String age, 
                               String jerseyNo, String position, String teamName, 
                               String photoPath) {
        // Validation
        if (playerId.isEmpty() || playerName.isEmpty() || age.isEmpty() || 
            jerseyNo.isEmpty() || position.isEmpty() || teamName.isEmpty()) {
            JOptionPane.showMessageDialog(null, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        try {
            int playerAge = Integer.parseInt(age);
            int jerseyNumber = Integer.parseInt(jerseyNo);
            
            if (playerAge <= 0 || playerAge > 100) {
                JOptionPane.showMessageDialog(null, "Age must be between 1 and 100!", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            if (jerseyNumber <= 0 || jerseyNumber > 99) {
                JOptionPane.showMessageDialog(null, "Jersey number must be between 1 and 99!", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Find the current player in LinkedList
            Player currentPlayer = null;
            for (Player player : players) {
                if (player.getPlayerId().equals(playerId)) {
                    currentPlayer = player;
                    break;
                }
            }
            
            if (currentPlayer == null) {
                JOptionPane.showMessageDialog(null, "Player not found!", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Check if jersey number already exists for the same team (excluding the current player)
            if (isJerseyNumberExists(jerseyNumber, teamName, playerId)) {
                JOptionPane.showMessageDialog(null, "Jersey number " + jerseyNumber + " already exists for team " + teamName + "!", 
                                             "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Find and update player in LinkedList
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i).getPlayerId().equals(playerId)) {
                    // Create updated player
                    Player updatedPlayer = new Player(playerId, playerName, playerAge, jerseyNumber, position, teamName);
                    
                    // Keep existing photo if no new photo is provided
                    String finalPhotoPath = (photoPath != null && !photoPath.isEmpty()) ? 
                                            photoPath : currentPlayer.getPhotoPath();
                    updatedPlayer.setPhotoPath(finalPhotoPath);
                    
                    players.set(i, updatedPlayer); // Update at specific index
                    
                    JOptionPane.showMessageDialog(null, "Player updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    return true;
                }
            }
            
            JOptionPane.showMessageDialog(null, "Player not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Age and Jersey Number must be valid integers!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    public boolean deletePlayer(String playerId) {
        if (playerId.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Player ID is required!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Find and remove player from LinkedList
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getPlayerId().equals(playerId)) {
                players.remove(i); // Remove from specific index
                JOptionPane.showMessageDialog(null, "Player deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                return true;
            }
        }
        
        JOptionPane.showMessageDialog(null, "Player not found!", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
    }
    
    // Load players to table
    public void loadPlayersToTable(DefaultTableModel tableModel) {
        tableModel.setRowCount(0); // Clear existing rows
        
        for (Player player : players) {
            Object[] row = {
                player.getPlayerId(),
                player.getPlayerName(),
                player.getAge(),
                player.getJerseyNo(),
                player.getPosition(),
                player.getTeamName(),
                player.getPhotoPath() // This will show the file path or be empty
            };
            tableModel.addRow(row);
        }
    }
    
    // Load player data from table selection
    public void loadPlayerFromTable(String playerId, javax.swing.JTextField playerIdField,
                                    javax.swing.JTextField playerNameField,
                                    javax.swing.JTextField ageField,
                                    javax.swing.JTextField jerseyNoField,
                                    javax.swing.JTextField positionField,
                                    javax.swing.JTextField teamNameField,
                                    JLabel photoLabel) {
        if (playerId.isEmpty()) {
            return;
        }
        
        // Search in LinkedList
        for (Player player : players) {
            if (player.getPlayerId().equals(playerId)) {
                playerIdField.setText(player.getPlayerId());
                playerNameField.setText(player.getPlayerName());
                ageField.setText(String.valueOf(player.getAge()));
                jerseyNoField.setText(String.valueOf(player.getJerseyNo()));
                positionField.setText(player.getPosition());
                teamNameField.setText(player.getTeamName());
                
                // Display photo if available
                if (player.getPhotoPath() != null && !player.getPhotoPath().isEmpty()) {
                    displayPhoto(player.getPhotoPath(), photoLabel);
                } else {
                    photoLabel.setIcon(null);
                    photoLabel.setText("No Photo");
                }
                break;
            }
        }
    }
    
    // Helper method to check if jersey number exists for a team
    private boolean isJerseyNumberExists(int jerseyNumber, String teamName, String excludePlayerId) {
        for (Player player : players) {
            // Skip the player we're updating (if excludePlayerId is provided)
            if (excludePlayerId != null && player.getPlayerId().equals(excludePlayerId)) {
                continue;
            }
            
            if (player.getJerseyNo() == jerseyNumber && player.getTeamName().equals(teamName)) {
                return true;
            }
        }
        return false;
    }
    
    // Get player by ID
    public Player getPlayerById(String playerId) {
        for (Player player : players) {
            if (player.getPlayerId().equals(playerId)) {
                return player;
            }
        }
        return null;
    }
    
    // Search players by name
    public LinkedList<Player> searchPlayersByName(String name) {
        LinkedList<Player> results = new LinkedList<>();
        for (Player player : players) {
            if (player.getPlayerName().toLowerCase().contains(name.toLowerCase())) {
                results.add(player);
            }
        }
        return results;
    }
    
    // Get players by team
    public LinkedList<Player> getPlayersByTeam(String teamName) {
        LinkedList<Player> teamPlayers = new LinkedList<>();
        for (Player player : players) {
            if (player.getTeamName().equals(teamName)) {
                teamPlayers.add(player);
            }
        }
        return teamPlayers;
    }
    
    // Sort players by age (bubble sort implementation for LinkedList)
    public void sortPlayersByAge() {
        int n = players.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (players.get(j).getAge() > players.get(j + 1).getAge()) {
                    // Swap players
                    Player temp = players.get(j);
                    players.set(j, players.get(j + 1));
                    players.set(j + 1, temp);
                }
            }
        }
    }
    
    // Sort players by jersey number
    public void sortPlayersByJersey() {
        int n = players.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (players.get(j).getJerseyNo() > players.get(j + 1).getJerseyNo()) {
                    // Swap players
                    Player temp = players.get(j);
                    players.set(j, players.get(j + 1));
                    players.set(j + 1, temp);
                }
            }
        }
    }
    
    // Getter for players LinkedList
    public LinkedList<Player> getPlayers() {
        return players;
    }
    
    // Get player count
    public int getPlayerCount() {
        return players.size();
    }
    
    // Check if player exists by ID
    public boolean playerExists(String playerId) {
        for (Player player : players) {
            if (player.getPlayerId().equals(playerId)) {
                return true;
            }
        }
        return false;
    }
}