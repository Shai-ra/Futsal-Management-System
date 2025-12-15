/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

/**
 *
 * @author WELCOME
 */
import Model.Player;
import Model.Team;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.io.*;

public class PlayerController {
    private static final String PLAYER_DATA_FILE = "players.txt";
    private List<Player> players;
    
    public PlayerController() {
        players = new ArrayList<>();
        loadPlayersFromFile();
    }
    
    // CRUD Operations
    public boolean addPlayer(String playerId, String playerName, String age, 
                            String jerseyNo, String position, String teamName) {
        // Validation
        if (playerId.isEmpty() || playerName.isEmpty() || age.isEmpty() || 
            jerseyNo.isEmpty() || position.isEmpty() || teamName.isEmpty()) {
            JOptionPane.showMessageDialog(null, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Check if player ID already exists
        for (Player player : players) {
            if (player.getPlayerId().equals(playerId)) {
                JOptionPane.showMessageDialog(null, "Player ID already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        
        // Check if team exists (optional - you can remove this if you don't want validation)
        // Team validation should be done in the view layer
        
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
            
            // Create new player
            Player newPlayer = new Player(playerId, playerName, playerAge, jerseyNumber, position, teamName);
            players.add(newPlayer);
            savePlayersToFile();
            
            JOptionPane.showMessageDialog(null, "Player added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            return true;
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Age and Jersey Number must be valid integers!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    public boolean updatePlayer(String playerId, String playerName, String age, 
                               String jerseyNo, String position, String teamName) {
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
            
            // Find and update player
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i).getPlayerId().equals(playerId)) {
                    Player updatedPlayer = new Player(playerId, playerName, playerAge, jerseyNumber, position, teamName);
                    players.set(i, updatedPlayer);
                    savePlayersToFile();
                    
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
        
        // Find and remove player
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getPlayerId().equals(playerId)) {
                players.remove(i);
                savePlayersToFile();
                
                JOptionPane.showMessageDialog(null, "Player deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                return true;
            }
        }
        
        JOptionPane.showMessageDialog(null, "Player not found!", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
    }
    
    public void readPlayers(DefaultTableModel tableModel) {
        // Clear existing rows
        tableModel.setRowCount(0);
        
        // Add all players to table
        for (Player player : players) {
            Object[] rowData = {
                player.getPlayerId(),
                player.getPlayerName(),
                player.getAge(),
                player.getJerseyNo(),
                player.getPosition(),
                player.getTeamName()
            };
            tableModel.addRow(rowData);
        }
    }
    
    // Load player data from table selection
    public void loadPlayerFromTable(String playerId, javax.swing.JTextField playerIdField,
                                    javax.swing.JTextField playerNameField,
                                    javax.swing.JTextField ageField,
                                    javax.swing.JTextField jerseyNoField,
                                    javax.swing.JTextField positionField,
                                    javax.swing.JTextField teamNameField) {
        if (playerId.isEmpty()) {
            return;
        }
        
        for (Player player : players) {
            if (player.getPlayerId().equals(playerId)) {
                playerIdField.setText(player.getPlayerId());
                playerNameField.setText(player.getPlayerName());
                ageField.setText(String.valueOf(player.getAge()));
                jerseyNoField.setText(String.valueOf(player.getJerseyNo()));
                positionField.setText(player.getPosition());
                teamNameField.setText(player.getTeamName());
                break;
            }
        }
    }
    
    // File Operations
    private void loadPlayersFromFile() {
        try {
            File file = new File(PLAYER_DATA_FILE);
            if (!file.exists()) {
                file.createNewFile();
                return;
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    try {
                        String playerId = parts[0];
                        String playerName = parts[1];
                        int age = Integer.parseInt(parts[2]);
                        int jerseyNo = Integer.parseInt(parts[3]);
                        String position = parts[4];
                        String teamName = parts[5];
                        
                        Player player = new Player(playerId, playerName, age, jerseyNo, position, teamName);
                        players.add(player);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid data format in players file: " + line);
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void savePlayersToFile() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(PLAYER_DATA_FILE));
            for (Player player : players) {
                String line = player.getPlayerId() + "," + 
                             player.getPlayerName() + "," + 
                             player.getAge() + "," + 
                             player.getJerseyNo() + "," + 
                             player.getPosition() + "," + 
                             player.getTeamName();
                writer.write(line);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Getter for players list
    public List<Player> getPlayers() {
        return players;
    }
}
