package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

import fit.iuh.kttkpm_nhom15_be.users.presentation.requests.UpdateProfileRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateProfileRequestValidationTest {

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
        UpdateProfileRequest request = validRequest();

        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void validateFailsWhenEmailIsBlank() {
        UpdateProfileRequest request = validRequest();
        request.setEmail(" ");

        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v ->
                "email".equals(v.getPropertyPath().toString())
                        && "Email không được để trống".equals(v.getMessage())
        ));
    }

    @Test
    void validateFailsWhenEmailHasInvalidFormat() {
        UpdateProfileRequest request = validRequest();
        request.setEmail("sai-định-dạng");

        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v ->
                "email".equals(v.getPropertyPath().toString())
                        && "Email không hợp lệ".equals(v.getMessage())
        ));
    }

    @Test
    void validateFailsWhenPhoneIsBlank() {
        UpdateProfileRequest request = validRequest();
        request.setPhone(" ");

        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v ->
                "phone".equals(v.getPropertyPath().toString())
                        && "SĐT không được để trống".equals(v.getMessage())
        ));
    }

    @Test
    void validateFailsWhenFullNameIsBlank() {
        UpdateProfileRequest request = validRequest();
        request.setFullName(" ");

        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v ->
                "fullName".equals(v.getPropertyPath().toString())
                        && "Họ tên không được để trống".equals(v.getMessage())
        ));
    }

    private UpdateProfileRequest validRequest() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setEmail("user@example.com");
        request.setPhone("0909123456");
        request.setFullName("Nguyen Van A");
        request.setAvatar("https://cdn.example/avatar.png");
        return request;
    }
}
