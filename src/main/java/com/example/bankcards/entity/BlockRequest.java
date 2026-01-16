package com.example.bankcards.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "block_requests")
public class BlockRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private User requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BlockRequestStatus status;

    // created_at выставляется БД
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public Card getCard() { return card; }
    public User getRequestedBy() { return requestedBy; }
    public BlockRequestStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setCard(Card card) { this.card = card; }
    public void setRequestedBy(User requestedBy) { this.requestedBy = requestedBy; }
    public void setStatus(BlockRequestStatus status) { this.status = status; }
}
