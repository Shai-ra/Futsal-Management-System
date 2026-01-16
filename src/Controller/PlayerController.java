package Controller;

import Model.Player;
import Model.PlayerManager;
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
    private PlayerManager playerManager;

    public PlayerController() {
        this.playerManager = new PlayerManager();
    }

   
    public String uploadPhoto() {
        JFileChooser fileChooser = new JFileChooser();

        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Image Files", "jpg", "jpeg", "png", "gif", "bmp");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            String fileName = selectedFile.getName().toLowerCase();
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
                    fileName.endsWith(".png") || fileName.endsWith(".gif") ||
                    fileName.endsWith(".bmp")) {

                return selectedFile.getName();
            } else {
                JOptionPane.showMessageDialog(null,
                        "Please select a valid image file (jpg, jpeg, png, gif, bmp)",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        return null;
    }

    public void displayPhoto(String photoFileName, JLabel photoLabel) {
        if (photoFileName == null || photoFileName.isEmpty()) {
            photoLabel.setIcon(null);
            photoLabel.setText("No Photo");
            return;
        }

        File photoFile = new File("player_photos/" + photoFileName);

        if (photoFile.exists()) {
            try {
                ImageIcon icon = new ImageIcon(photoFile.getAbsolutePath());

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

    // CRUD Operations using PlayerManager
    public boolean addPlayer(String playerId, String playerName, String age,
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
                JOptionPane.showMessageDialog(null, "Age must be between 1 and 100!", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            if (jerseyNumber <= 0 || jerseyNumber > 99) {
                JOptionPane.showMessageDialog(null, "Jersey number must be between 1 and 99!", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Check if jersey number already exists for the same team
            if (playerManager.isJerseyNumberExists(jerseyNumber, teamName, null)) {
                JOptionPane.showMessageDialog(null,
                        "Jersey number " + jerseyNumber + " already exists for team " + teamName + "!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Add player through PlayerManager
            boolean success = playerManager.addPlayer(playerId, playerName, playerAge, jerseyNumber,
                    position, teamName, photoPath);

            if (success) {
                JOptionPane.showMessageDialog(null, "Player added successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Player ID already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return success;

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Age and Jersey Number must be valid integers!", "Error",
                    JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(null, "Age must be between 1 and 100!", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            if (jerseyNumber <= 0 || jerseyNumber > 99) {
                JOptionPane.showMessageDialog(null, "Jersey number must be between 1 and 99!", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Check if jersey number already exists for the same team (excluding current
            // player)
            if (playerManager.isJerseyNumberExists(jerseyNumber, teamName, playerId)) {
                JOptionPane.showMessageDialog(null,
                        "Jersey number " + jerseyNumber + " already exists for team " + teamName + "!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Update player through PlayerManager
            boolean success = playerManager.updatePlayer(playerId, playerName, playerAge, jerseyNumber,
                    position, teamName, photoPath);

            if (success) {
                JOptionPane.showMessageDialog(null, "Player updated successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Player not found!", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return success;

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Age and Jersey Number must be valid integers!", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean deletePlayer(String playerId) {
        if (playerId.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Player ID is required!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Delete player through PlayerManager
        boolean success = playerManager.deletePlayer(playerId);

        if (success) {
            JOptionPane.showMessageDialog(null, "Player deleted successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Player not found!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return success;
    }

    public void loadPlayersToTable(DefaultTableModel tableModel) {
        tableModel.setRowCount(0); // Clear existing rows

        for (Player player : playerManager.getPlayers()) {
            Object[] row = {
                    player.getPlayerId(),
                    player.getPlayerName(),
                    player.getAge(),
                    player.getJerseyNo(),
                    player.getPosition(),
                    player.getTeamName(),
                    player.getPhotoPath()
            };
            tableModel.addRow(row);
        }
    }

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

        Player player = playerManager.getPlayerById(playerId);
        if (player != null) {
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
        }
    }

 
    public Player getPlayerById(String playerId) {
        return playerManager.getPlayerById(playerId);
    }

    public void searchPlayers(String criteria, String query, DefaultTableModel tableModel) {
        LinkedList<Player> results = playerManager.searchPlayers(criteria, query);
        tableModel.setRowCount(0);
        for (Player player : results) {
            Object[] row = {
                    player.getPlayerId(),
                    player.getPlayerName(),
                    player.getAge(),
                    player.getJerseyNo(),
                    player.getPosition(),
                    player.getTeamName(),
                    player.getPhotoPath()
            };
            tableModel.addRow(row);
        }
    }

    public void sortPlayers(String criteria, DefaultTableModel tableModel) {
        playerManager.sortPlayers(criteria);
        loadPlayersToTable(tableModel);
    }

    public LinkedList<Player> getPlayers() {
        return playerManager.getPlayers();
    }

    public int getPlayerCount() {
        return playerManager.getPlayerCount();
    }

    public boolean playerExists(String playerId) {
        return playerManager.playerExists(playerId);
    }
    
}