package fit.iuh.kttkpm_nhom15_be.chat.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatMessage;
import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatRoom;
import fit.iuh.kttkpm_nhom15_be.chat.domain.repositories.ChatRepository;
import fit.iuh.kttkpm_nhom15_be.chat.infrastructure.persistence.entities.ChatMessageJpaEntity;
import fit.iuh.kttkpm_nhom15_be.chat.infrastructure.persistence.entities.ChatRoomJpaEntity;
import fit.iuh.kttkpm_nhom15_be.chat.infrastructure.persistence.mappers.ChatDataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// 1. Spring Data JPA Interfaces
interface JpaChatRoomRepository extends JpaRepository<ChatRoomJpaEntity, String> {
    Optional<ChatRoomJpaEntity> findByUserId(String userId);
}

interface JpaChatMessageRepository extends JpaRepository<ChatMessageJpaEntity, String> {
    @Query("SELECT m FROM ChatMessageJpaEntity m WHERE m.room.id = :roomId ORDER BY m.createdAt ASC")
    List<ChatMessageJpaEntity> findByRoomIdOrderByCreatedAtAsc(@Param("roomId") String roomId);
}

// 2. Lớp Implement của Domain
@Repository // <-- Báo cho Spring biết đây là Bean Repository
@RequiredArgsConstructor
public class ChatRepositoryImpl implements ChatRepository {

    private final JpaChatRoomRepository roomRepo;
    private final JpaChatMessageRepository messageRepo;
    private final ChatDataMapper mapper;

    @Override
    public ChatRoom saveRoom(ChatRoom room) {
        ChatRoomJpaEntity entity = mapper.toJpaEntity(room);
        return mapper.toDomainModel(roomRepo.save(entity));
    }

    @Override
    public Optional<ChatRoom> findRoomById(String roomId) {
        return roomRepo.findById(roomId).map(mapper::toDomainModel);
    }

    @Override
    public List<ChatRoom> findActiveRooms() {
        return roomRepo.findAll().stream().map(mapper::toDomainModel).toList();
    }

    @Override
    public Optional<ChatRoom> findActiveRoomByCustomer(String customerId) {
        return roomRepo.findByUserId(customerId).map(mapper::toDomainModel);
    }

    @Override
    public ChatMessage saveMessage(ChatMessage message) {
        ChatMessageJpaEntity entity = new ChatMessageJpaEntity();
        entity.setId(message.getId());
        entity.setContent(message.getContent());
        entity.setSenderId(message.getSenderId());

        // Gắn quan hệ với Room
        roomRepo.findById(message.getRoomId()).ifPresent(entity::setRoom);

        ChatMessageJpaEntity saved = messageRepo.save(entity);

        // Map ngược lại Domain Model
        ChatMessage result = new ChatMessage();
        result.setId(saved.getId());
        result.setRoomId(saved.getRoom().getId());
        result.setSenderId(saved.getSenderId());
        result.setContent(saved.getContent());
        return result;
    }

    @Override
    public List<ChatMessage> findMessagesByRoomId(String roomId) {
        return messageRepo.findByRoomIdOrderByCreatedAtAsc(roomId).stream()
                .map(saved -> {
                    ChatMessage result = new ChatMessage();
                    result.setId(saved.getId());
                    result.setRoomId(saved.getRoom().getId());
                    result.setSenderId(saved.getSenderId());
                    result.setContent(saved.getContent());
                    return result;
                }).toList();
    }
}