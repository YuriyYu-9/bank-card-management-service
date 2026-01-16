package com.example.bankcards.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_card_id", nullable = false)
    private Card fromCard;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_card_id", nullable = false)
    private Card toCard;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    // created_at выставляется БД
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public Card getFromCard() { return fromCard; }
    public Card getToCard() { return toCard; }
    public long getAmountCents() { return amountCents; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setFromCard(Card fromCard) { this.fromCard = fromCard; }
    public void setToCard(Card toCard) { this.toCard = toCard; }
    public void setAmountCents(long amountCents) { this.amountCents = amountCents; }
}
