package Model;

public class Team {
    private String teamId;
    private String teamName;
    private String manager;
    private int noOfPlayers;
    
    public Team(String teamId, String teamName, String manager, int noOfPlayers) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.manager = manager;
        this.noOfPlayers = noOfPlayers;
    }
    
    // Getters and Setters
    public String getTeamId() {
        return teamId; 
    }
    public void setTeamId(String teamId) { 
        this.teamId = teamId; 
    }
    
    public String getTeamName() {
        return teamName;
    }
    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
    
    public String getManager() { 
        return manager;
    }
    public void setManager(String manager) { 
        this.manager = manager; 
    }
    
    public int getNoOfPlayers() {
        return noOfPlayers; 
    }
    public void setNoOfPlayers(int noOfPlayers) {
        this.noOfPlayers = noOfPlayers; 
    }
}