package fit.iuh.kttkpm_nhom15_be.users.presentation.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddAddressRequest {

    @NotBlank(message = "Tên người nhận không được để trống.")
    @JsonProperty("receiver_name")
    private String receiverName;

    @NotBlank(message = "Số điện thoại không được để trống.")
    @Pattern(
            regexp = "^(0|\\+84)(3|5|7|8|9)[0-9]{8}$",
            message = "Số điện thoại không đúng định dạng."
    )
    private String phone;

    @NotBlank(message = "Địa chỉ không được để trống.")
    private String address;

    @NotBlank(message = "Thành phố không được để trống.")
    private String city;

    @NotBlank(message = "Quận/Huyện không được để trống.")
    private String district;

    @NotBlank(message = "Phường/Xã không được để trống.")
    private String ward;

    @JsonProperty("is_default")
    private boolean isDefault;
}
