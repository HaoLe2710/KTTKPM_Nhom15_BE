package fit.iuh.kttkpm_nhom15_be.chat.infrastructure.persistence.mappers;

import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatMessage;
import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatRoom;
import fit.iuh.kttkpm_nhom15_be.chat.infrastructure.persistence.entities.ChatMessageJpaEntity;
import fit.iuh.kttkpm_nhom15_be.chat.infrastructure.persistence.entities.ChatRoomJpaEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ChatDataMapper {

    // --- MAPPING CHO CHAT ROOM ---

    @Mapping(source = "userId", target = "customerId")
    ChatRoom toDomainModel(ChatRoomJpaEntity entity);

    @Mapping(source = "customerId", target = "userId")
    ChatRoomJpaEntity toJpaEntity(ChatRoom domain);

    // --- MAPPING CHO CHAT MESSAGE ---

    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "createdAt", target = "sentAt")
    ChatMessage toDomainModel(ChatMessageJpaEntity entity);

    @Mapping(target = "room", ignore = true)
    @Mapping(source = "sentAt", target = "createdAt")
    ChatMessageJpaEntity toJpaEntity(ChatMessage domain);

    @AfterMapping
    default void linkMessages(@MappingTarget ChatRoomJpaEntity roomJpa) {
        if (roomJpa.getMessages() != null) {
            roomJpa.getMessages().forEach(m -> m.setRoom(roomJpa));
        }
    }
}