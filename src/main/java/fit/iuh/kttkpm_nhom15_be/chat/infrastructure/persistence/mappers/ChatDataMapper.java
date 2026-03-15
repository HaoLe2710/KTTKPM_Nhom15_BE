package fit.iuh.kttkpm_nhom15_be.chat.infrastructure.persistence.mappers;

import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatRoom;
import fit.iuh.kttkpm_nhom15_be.chat.infrastructure.persistence.entities.ChatRoomJpaEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ChatDataMapper {
    ChatRoomJpaEntity toJpaEntity(ChatRoom domain);
    ChatRoom toDomainModel(ChatRoomJpaEntity entity);

    @AfterMapping
    default void linkMessages(@MappingTarget ChatRoomJpaEntity roomJpa) {
        if (roomJpa.getMessages() != null) roomJpa.getMessages().forEach(m -> m.setRoom(roomJpa));
    }
}