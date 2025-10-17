package com.javarush.lepeshinskij.model;

import java.io.Serializable;

public class PlayerStats implements Serializable {
    private String playerName;
    private String ipAddress;
    private int gamesPlayed;
    private int wins;
    private int losses;

    public PlayerStats(String playerName) {
        this.playerName = playerName;
        this.gamesPlayed = 0;
        this.wins = 0;
        this.losses = 0;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public int getGamesPlayed() {
        return gamesPlayed;
    }
    
    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }
    
    public int getLosses() {
        return losses;
    }
    
    public void incrementGamesPlayed() {
        this.gamesPlayed++;
    }
    
    public void incrementGamesWon() {
        this.wins++;
    }
    
    public void registerWin() {
        this.gamesPlayed++;
        this.wins++;
    }
    
    public void registerLoss() {
        this.gamesPlayed++;
        this.losses++;
    }
    
    public void incrementLosses() {
        this.losses++;
    }
}