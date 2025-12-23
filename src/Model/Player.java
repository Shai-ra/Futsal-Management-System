package Model;

public class Player {
    private String playerId;
    private String playerName;
    private int age;
    private int jerseyNo;
    private String position;
    private String teamName;
    private String photoPath; // Will be set separately
    
    public Player(String playerId, String playerName, int age, int jerseyNo, 
                  String position, String teamName) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.age = age;
        this.jerseyNo = jerseyNo;
        this.position = position;
        this.teamName = teamName;
        this.photoPath = ""; // Initialize as empty
    }
    
    // Getters and Setters
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    public int getJerseyNo() { return jerseyNo; }
    public void setJerseyNo(int jerseyNo) { this.jerseyNo = jerseyNo; }
    
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
}