package fit.iuh.kttkpm_nhom15_be.chat.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "chat_rooms")
@Getter
@Setter
@NoArgsConstructor
public class ChatRoomJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "user_id")
    private String userId;

    private String staffId;

    @Column(name = "is_closed")
    private boolean isClosed;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessageJpaEntity> messages;
}