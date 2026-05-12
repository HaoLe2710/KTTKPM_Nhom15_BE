package fit.iuh.kttkpm_nhom15_be.chat.application.support;

import fit.iuh.kttkpm_nhom15_be.chat.application.commands.SendMessageCommand;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.ChatMessageValidationException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatMessage;
import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatMessageType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ChatMessagePayloadSupport {

    public ChatMessage buildMessage(String roomId, SendMessageCommand command) {
        validateCommand(command);

        return ChatMessage.builder()
                .roomId(roomId)
                .senderId(command.senderId())
                .type(command.type())
                .content(normalize(command.content()))
                .imageUrl(normalize(command.imageUrl()))
                .linkUrl(normalize(command.linkUrl()))
                .productId(normalize(command.productId()))
                .variantId(normalize(command.variantId()))
                .productName(normalize(command.productName()))
                .productImageUrl(normalize(command.productImageUrl()))
                .productPrice(command.productPrice())
                .sentAt(LocalDateTime.now())
                .build();
    }

    public void validateCommand(SendMessageCommand command) {
        if (command.type() == null) {
            throw new ChatMessageValidationException("Loai tin nhan khong duoc de trong.");
        }

        switch (command.type()) {
            case TEXT -> {
                if (isBlank(command.content())) {
                    throw new ChatMessageValidationException("Noi dung tin nhan khong duoc de trong.");
                }
            }
            case IMAGE -> {
                if (isBlank(command.imageUrl())) {
                    throw new ChatMessageValidationException("Tin nhan hinh anh phai co imageUrl.");
                }
            }
            case PRODUCT_LINK -> {
                if (isBlank(command.linkUrl())) {
                    throw new ChatMessageValidationException("Tin nhan san pham phai co linkUrl.");
                }
                if (isBlank(command.productName())) {
                    throw new ChatMessageValidationException("Tin nhan san pham phai co ten san pham.");
                }
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalize(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
