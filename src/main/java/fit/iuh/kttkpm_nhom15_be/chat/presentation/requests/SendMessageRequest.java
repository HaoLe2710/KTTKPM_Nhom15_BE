package fit.iuh.kttkpm_nhom15_be.chat.presentation.requests;

import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatMessageType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SendMessageRequest {
    private String roomId;

    @NotNull(message = "Loai tin nhan khong duoc de trong.")
    private ChatMessageType type;

    private String content;

    private String imageUrl;

    private String linkUrl;

    private String productId;

    private String variantId;

    private String productName;

    private String productImageUrl;

    private BigDecimal productPrice;
}
