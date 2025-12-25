package Model;

import java.util.LinkedList;

public class PlayerManager {
    private LinkedList<Player> players;
    
    public PlayerManager() {
        players = new LinkedList<>();
        prepareInitialPlayerData();
    }
    
    private void prepareInitialPlayerData() {
        players.add(new Player("P001", "Lionel Messi", 36, 10, "Forward", "Inter Miami"));
        players.add(new Player("P002", "Cristiano Ronaldo", 38, 7, "Forward", "Al Nassr"));
        players.add(new Player("P003", "Neymar Jr", 31, 10, "Forward", "Al Hilal"));
        players.add(new Player("P004", "Kevin De Bruyne", 32, 17, "Midfielder", "Manchester City"));
    }
    
    // CRUD Operations
    public boolean addPlayer(String playerId, String playerName, int age, 
                            int jerseyNo, String position, String teamName, 
                            String photoPath) {
        // Check if player ID already exists
        for (Player player : players) {
            if (player.getPlayerId().equals(playerId)) {
                return false;
            }
        }
        
        // Create new player
        Player newPlayer = new Player(playerId, playerName, age, jerseyNo, position, teamName);
        
        // Set photo path if provided
        if (photoPath != null && !photoPath.isEmpty()) {
            newPlayer.setPhotoPath(photoPath);
        }
        
        players.add(newPlayer);
        return true;
    }
    
    public boolean updatePlayer(String playerId, String playerName, int age, 
                               int jerseyNo, String position, String teamName, 
                               String photoPath) {
        // Find the current player
        Player currentPlayer = getPlayerById(playerId);
        if (currentPlayer == null) {
            return false;
        }
        
        // Find and update player in LinkedList
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getPlayerId().equals(playerId)) {
                Player updatedPlayer = new Player(playerId, playerName, age, jerseyNo, position, teamName);
                
                // Keep existing photo if no new photo is provided
                String finalPhotoPath = (photoPath != null && !photoPath.isEmpty()) ? 
                                        photoPath : currentPlayer.getPhotoPath();
                updatedPlayer.setPhotoPath(finalPhotoPath);
                
                players.set(i, updatedPlayer);
                return true;
            }
        }
        return false;
    }
    
    public boolean deletePlayer(String playerId) {
        // Find and remove player from LinkedList
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getPlayerId().equals(playerId)) {
                players.remove(i);
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
    
    // Sort players by age (bubble sort)
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
    
    // Check if jersey number exists for a team (excluding a specific player)
    public boolean isJerseyNumberExists(int jerseyNumber, String teamName, String excludePlayerId) {
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
    
    // Getter for players LinkedList
    public LinkedList<Player> getPlayers() {
        return players;
    }
    
    // Get player count
    public int getPlayerCount() {
        return players.size();
    }
    
    // Check if player exists
    public boolean playerExists(String playerId) {
        for (Player player : players) {
            if (player.getPlayerId().equals(playerId)) {
                return true;
            }
        }
        return false;
    }
}