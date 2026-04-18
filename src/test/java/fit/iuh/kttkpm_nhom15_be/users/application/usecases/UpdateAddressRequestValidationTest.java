package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

import fit.iuh.kttkpm_nhom15_be.users.presentation.requests.UpdateAddressRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateAddressRequestValidationTest {

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
    void validateInheritsAllRulesFromAddAddressRequest() {
        UpdateAddressRequest request = new UpdateAddressRequest();
        request.setReceiverName(" ");
        request.setPhone("09876abcde");
        request.setAddress(" ");
        request.setCity(" ");
        request.setDistrict(" ");
        request.setWard(" ");

        Set<ConstraintViolation<UpdateAddressRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v -> "receiverName".equals(v.getPropertyPath().toString())));
        assertTrue(violations.stream().anyMatch(v -> "phone".equals(v.getPropertyPath().toString()) && "Số điện thoại không đúng định dạng.".equals(v.getMessage())));
        assertTrue(violations.stream().anyMatch(v -> "address".equals(v.getPropertyPath().toString())));
        assertTrue(violations.stream().anyMatch(v -> "city".equals(v.getPropertyPath().toString())));
        assertTrue(violations.stream().anyMatch(v -> "district".equals(v.getPropertyPath().toString())));
        assertTrue(violations.stream().anyMatch(v -> "ward".equals(v.getPropertyPath().toString())));
    }
}
