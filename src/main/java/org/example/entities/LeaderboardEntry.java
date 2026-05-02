package org.example.entities;

import java.sql.Timestamp;

public class LeaderboardEntry {

    private int       id;
    private int       score;
    private String    badges;
    private Timestamp createdAt;
    private String    reponses;
    private int       userId;
    private int       challengeId;

    public LeaderboardEntry() {}

    public LeaderboardEntry(int score, String badges,
                            String reponses, int userId, int challengeId) {
        this.score       = score;
        this.badges      = badges;
        this.reponses    = reponses;
        this.userId      = userId;
        this.challengeId = challengeId;
    }

    public int       getId()                      { return id; }
    public void      setId(int id)                { this.id = id; }

    public int       getScore()                   { return score; }
    public void      setScore(int score)          { this.score = score; }

    public String    getBadges()                  { return badges; }
    public void      setBadges(String badges)     { this.badges = badges; }

    public Timestamp getCreatedAt()                       { return createdAt; }
    public void      setCreatedAt(Timestamp createdAt)    { this.createdAt = createdAt; }

    public String    getReponses()                        { return reponses; }
    public void      setReponses(String reponses)         { this.reponses = reponses; }

    public int       getUserId()                          { return userId; }
    public void      setUserId(int userId)                { this.userId = userId; }

    public int       getChallengeId()                     { return challengeId; }
    public void      setChallengeId(int challengeId)      { this.challengeId = challengeId; }
}
