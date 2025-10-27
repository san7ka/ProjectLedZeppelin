package com.javarush.lepeshinskij.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_statistics", indexes = {
    @Index(name = "idx_user_statistics_user", columnList = "user_id")
})
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "userStatistics")
public class UserStatistics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true, 
                foreignKey = @ForeignKey(name = "fk_user_statistics_user"))
    private User user;
    
    @Column(name = "total_games", nullable = false)
    private Integer totalGames = 0;
    
    @Column(name = "completed_games", nullable = false)
    private Integer completedGames = 0;
    
    @Column(name = "won_games", nullable = false)
    private Integer wonGames = 0;
    
    @Column(name = "lost_games", nullable = false)
    private Integer lostGames = 0;
    
    @Column(name = "total_playtime_minutes", nullable = false)
    private Long totalPlaytimeMinutes = 0L;
    
    @Column(name = "best_time_minutes")
    private Long bestTimeMinutes;
    
    @Column(name = "average_time_minutes")
    private Double averageTimeMinutes;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public UserStatistics() {}
    
    public UserStatistics(User user) {
        this.user = user;
        this.totalGames = 0;
        this.completedGames = 0;
        this.wonGames = 0;
        this.lostGames = 0;
        this.totalPlaytimeMinutes = 0L;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Integer getTotalGames() {
        return totalGames;
    }
    
    public void setTotalGames(Integer totalGames) {
        this.totalGames = totalGames;
    }
    
    public Integer getCompletedGames() {
        return completedGames;
    }
    
    public void setCompletedGames(Integer completedGames) {
        this.completedGames = completedGames;
    }
    
    public Integer getWonGames() {
        return wonGames;
    }
    
    public void setWonGames(Integer wonGames) {
        this.wonGames = wonGames;
    }
    
    public Integer getLostGames() {
        return lostGames;
    }
    
    public void setLostGames(Integer lostGames) {
        this.lostGames = lostGames;
    }
    
    public Long getTotalPlaytimeMinutes() {
        return totalPlaytimeMinutes;
    }
    
    public void setTotalPlaytimeMinutes(Long totalPlaytimeMinutes) {
        this.totalPlaytimeMinutes = totalPlaytimeMinutes;
    }
    
    public Long getBestTimeMinutes() {
        return bestTimeMinutes;
    }
    
    public void setBestTimeMinutes(Long bestTimeMinutes) {
        this.bestTimeMinutes = bestTimeMinutes;
    }
    
    public Double getAverageTimeMinutes() {
        return averageTimeMinutes;
    }
    
    public void setAverageTimeMinutes(Double averageTimeMinutes) {
        this.averageTimeMinutes = averageTimeMinutes;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public void incrementTotalGames() {
        this.totalGames++;
    }
    
    public void incrementCompletedGames() {
        this.completedGames++;
    }
    
    public void incrementWonGames() {
        this.wonGames++;
    }
    
    public void incrementLostGames() {
        this.lostGames++;
    }
    
    public void addPlaytime(Long minutes) {
        this.totalPlaytimeMinutes += minutes;
        updateAverageTime();
        updateBestTime(minutes);
    }
    
    public void recordWin(Long playtimeMinutes) {
        incrementCompletedGames();
        incrementWonGames();
        addPlaytime(playtimeMinutes);
    }
    
    public void recordLoss(Long playtimeMinutes) {
        incrementCompletedGames();
        incrementLostGames();
        addPlaytime(playtimeMinutes);
    }
    
    public void recordGameStart() {
        incrementTotalGames();
    }
    
    private void updateAverageTime() {
        if (completedGames > 0) {
            this.averageTimeMinutes = (double) totalPlaytimeMinutes / completedGames;
        }
    }
    
    private void updateBestTime(Long minutes) {
        if (bestTimeMinutes == null || minutes < bestTimeMinutes) {
            this.bestTimeMinutes = minutes;
        }
    }
    
    public Double getWinRate() {
        if (completedGames == 0) {
            return 0.0;
        }
        return (double) wonGames / completedGames * 100;
    }
    
    public Double getCompletionRate() {
        if (totalGames == 0) {
            return 0.0;
        }
        return (double) completedGames / totalGames * 100;
    }
    
    @Override
    public String toString() {
        return "UserStatistics{" +
                "id=" + id +
                ", user='" + (user != null ? user.getUsername() : "null") + '\'' +
                ", totalGames=" + totalGames +
                ", completedGames=" + completedGames +
                ", wonGames=" + wonGames +
                ", lostGames=" + lostGames +
                ", totalPlaytimeMinutes=" + totalPlaytimeMinutes +
                ", bestTimeMinutes=" + bestTimeMinutes +
                ", averageTimeMinutes=" + averageTimeMinutes +
                '}';
    }
}
