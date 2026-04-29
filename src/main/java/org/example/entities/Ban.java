package org.example.entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Ban {

    private int           id;
    private int           userId;
    private String        reason;
    private int           bannedBy;
    private String        banType;   // "TEMPORARY" | "PERMANENT" | "WARNING"
    private boolean       isActive;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt; // null = permanent

    // ── Constructor ────────────────────────────────────────────────
    public Ban() {}

    public Ban(int id, int userId, String reason, int bannedBy,
               String banType, boolean isActive,
               LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.id        = id;
        this.userId    = userId;
        this.reason    = reason;
        this.bannedBy  = bannedBy;
        this.banType   = banType;
        this.isActive  = isActive;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    // ── Helper: human-readable expiry ──────────────────────────────
    public String getFormattedExpiry() {
        if (expiresAt == null) return "Permanent";
        return expiresAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    // ── Helper: is a WARNING (not a real ban) ──────────────────────
    public boolean isWarning() {
        return "WARNING".equalsIgnoreCase(banType);
    }

    // ── Getters / Setters ──────────────────────────────────────────
    public int           getId()        { return id; }
    public int           getUserId()    { return userId; }
    public String        getReason()    { return reason; }
    public int           getBannedBy()  { return bannedBy; }
    public String        getBanType()   { return banType; }
    public boolean       isActive()     { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }

    public void setId(int id)                     { this.id        = id; }
    public void setUserId(int userId)             { this.userId    = userId; }
    public void setReason(String reason)           { this.reason    = reason; }
    public void setBannedBy(int bannedBy)         { this.bannedBy  = bannedBy; }
    public void setBanType(String banType)         { this.banType   = banType; }
    public void setActive(boolean active)         { this.isActive  = active; }
    public void setCreatedAt(LocalDateTime v)      { this.createdAt = v; }
    public void setExpiresAt(LocalDateTime v)      { this.expiresAt = v; }
}