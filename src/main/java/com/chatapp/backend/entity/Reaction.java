package com.chatapp.backend.entity;

import com.chatapp.backend.entity.enums.ReactionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reactions", indexes = {
        @Index(name = "idx_reaction_message_id", columnList = "message_id"),
        @Index(name = "idx_reaction_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", length = 20)
    private ReactionType type;

}