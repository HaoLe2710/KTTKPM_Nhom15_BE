package fit.iuh.kttkpm_nhom15_be.users.domain.exceptions;

public class AddressNotFoundException extends RuntimeException {
    public AddressNotFoundException(String id) {
        super("Không tìm thấy địa chỉ với ID: " + id);
    }
}
