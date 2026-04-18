package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

import fit.iuh.kttkpm_nhom15_be.users.presentation.requests.AddAddressRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddAddressRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void validatePassesWhenRequestIsValid() {
        AddAddressRequest request = validRequest();

        Set<ConstraintViolation<AddAddressRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void validateFailsWhenReceiverNameIsBlank() {
        AddAddressRequest request = validRequest();
        request.setReceiverName(" ");

        Set<ConstraintViolation<AddAddressRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertTrue(violations.stream().anyMatch(v ->
                "receiverName".equals(v.getPropertyPath().toString())
                        && "Tên người nhận không được để trống.".equals(v.getMessage())
        ));
    }

    @Test
    void validateFailsWhenPhoneIsBlank() {
        AddAddressRequest request = validRequest();
        request.setPhone("");

        Set<ConstraintViolation<AddAddressRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v ->
                "phone".equals(v.getPropertyPath().toString())
                        && "Số điện thoại không được để trống.".equals(v.getMessage())
        ));
    }

    @Test
    void validateFailsWhenPhoneHasInvalidFormat() {
        AddAddressRequest request = validRequest();
        request.setPhone("12345abc");

        Set<ConstraintViolation<AddAddressRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v ->
                "phone".equals(v.getPropertyPath().toString())
                        && "Số điện thoại không đúng định dạng.".equals(v.getMessage())
        ));
    }

    @Test
    void validateFailsWhenAddressIsBlank() {
        AddAddressRequest request = validRequest();
        request.setAddress(" ");

        Set<ConstraintViolation<AddAddressRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v ->
                "address".equals(v.getPropertyPath().toString())
                        && "Địa chỉ không được để trống.".equals(v.getMessage())
        ));
    }

    @Test
    void validateFailsWhenCityIsBlank() {
        AddAddressRequest request = validRequest();
        request.setCity(" ");

        Set<ConstraintViolation<AddAddressRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v ->
                "city".equals(v.getPropertyPath().toString())
                        && "Thành phố không được để trống.".equals(v.getMessage())
        ));
    }

    @Test
    void validateFailsWhenDistrictIsBlank() {
        AddAddressRequest request = validRequest();
        request.setDistrict(" ");

        Set<ConstraintViolation<AddAddressRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v ->
                "district".equals(v.getPropertyPath().toString())
                        && "Quận/Huyện không được để trống.".equals(v.getMessage())
        ));
    }

    @Test
    void validateFailsWhenWardIsBlank() {
        AddAddressRequest request = validRequest();
        request.setWard(" ");

        Set<ConstraintViolation<AddAddressRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v ->
                "ward".equals(v.getPropertyPath().toString())
                        && "Phường/Xã không được để trống.".equals(v.getMessage())
        ));
    }

    private AddAddressRequest validRequest() {
        AddAddressRequest request = new AddAddressRequest();
        request.setReceiverName("Nguyen Van A");
        request.setPhone("0909123456");
        request.setAddress("12 Nguyen Trai");
        request.setCity("Ho Chi Minh");
        request.setDistrict("Quan 1");
        request.setWard("Ben Nghe");
        request.setDefault(true);
        return request;
    }
}
