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
                String finalPhotoPath = (photoPath != null && !photoPath.isEmpty()) ? photoPath
                        : currentPlayer.getPhotoPath();
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

    // Generic search for players
    public LinkedList<Player> searchPlayers(String criteria, String query) {
        LinkedList<Player> results = new LinkedList<>();
        String q = query.toLowerCase();
        for (Player player : players) {
            boolean matches = false;
            switch (criteria) {
                case "Player ID":
                    matches = player.getPlayerId().toLowerCase().contains(q);
                    break;
                case "Player Name":
                    matches = player.getPlayerName().toLowerCase().contains(q);
                    break;
                case "Age":
                    matches = String.valueOf(player.getAge()).contains(q);
                    break;
                case "Jersey":
                    matches = String.valueOf(player.getJerseyNo()).contains(q);
                    break;
                case "Position":
                    matches = player.getPosition().toLowerCase().contains(q);
                    break;
                case "Team":
                    matches = player.getTeamName().toLowerCase().contains(q);
                    break;
                default:
                    matches = player.getPlayerName().toLowerCase().contains(q);
                    break;
            }
            if (matches) {
                results.add(player);
            }
        }
        return results;
    }

    // Generic sort for players
    public void sortPlayers(String criteria) {
        int n = players.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                boolean swap = false;
                Player p1 = players.get(j);
                Player p2 = players.get(j + 1);
                switch (criteria) {
                    case "Player ID":
                        swap = p1.getPlayerId().compareTo(p2.getPlayerId()) > 0;
                        break;
                    case "Age":
                        swap = p1.getAge() > p2.getAge();
                        break;
                    case "Jersey":
                        swap = p1.getJerseyNo() > p2.getJerseyNo();
                        break;
                }
                if (swap) {
                    players.set(j, p2);
                    players.set(j + 1, p1);
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