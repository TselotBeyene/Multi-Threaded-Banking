package com.tselot.banking.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "sessions")
public class SessionEntity {
    @Id
    @Column(name = "session_id", nullable = false, updatable = false, length = 64)
    private String sessionId;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "login_time", nullable = false)
    private Instant loginTime;

    protected SessionEntity() {
    }

    public SessionEntity(String sessionId, String ownerName, Instant loginTime) {
        this.sessionId = sessionId;
        this.ownerName = ownerName;
        this.loginTime = loginTime;
    }

    @JsonProperty("sessionId")
    public String getSessionId() {
        return sessionId;
    }

    @JsonProperty("ownerName")
    public String getOwnerName() {
        return ownerName;
    }

    @JsonProperty("loginTime")
    public Instant getLoginTime() {
        return loginTime;
    }
}
