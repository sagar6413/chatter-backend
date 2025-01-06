package com.chatapp.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false, unique = true)
    private Chat chat;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    private String description;

    @Column(name = "max_members")
    private Integer maxMembers = 256;

    @Column(name = "is_public")
    private boolean isPublic = false;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private Set<GroupParticipant> groupParticipants = new HashSet<>();

}