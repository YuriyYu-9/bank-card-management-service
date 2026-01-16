package com.example.bankcards.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "pan_encrypted", nullable = false, length = 512)
    private String panEncrypted;

    @Column(name = "pan_last4", nullable = false, length = 4)
    private String panLast4;

    @Column(name = "expiry_month", nullable = false)
    private int expiryMonth;

    @Column(name = "expiry_year", nullable = false)
    private int expiryYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CardStatus status;

    @Column(name = "balance_cents", nullable = false)
    private long balanceCents;

    @Column(nullable = false, length = 3)
    private String currency;

    @Version
    @Column(nullable = false)
    private Long version;

    // created_at выставляется БД через defaultValueComputed: CURRENT_TIMESTAMP в Liquibase
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public User getOwner() { return owner; }
    public String getPanEncrypted() { return panEncrypted; }
    public String getPanLast4() { return panLast4; }
    public int getExpiryMonth() { return expiryMonth; }
    public int getExpiryYear() { return expiryYear; }
    public CardStatus getStatus() { return status; }
    public long getBalanceCents() { return balanceCents; }
    public String getCurrency() { return currency; }
    public Long getVersion() { return version; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setOwner(User owner) { this.owner = owner; }
    public void setPanEncrypted(String panEncrypted) { this.panEncrypted = panEncrypted; }
    public void setPanLast4(String panLast4) { this.panLast4 = panLast4; }
    public void setExpiryMonth(int expiryMonth) { this.expiryMonth = expiryMonth; }
    public void setExpiryYear(int expiryYear) { this.expiryYear = expiryYear; }
    public void setStatus(CardStatus status) { this.status = status; }
    public void setBalanceCents(long balanceCents) { this.balanceCents = balanceCents; }
    public void setCurrency(String currency) { this.currency = currency; }
    public void setVersion(Long version) { this.version = version; }
}
