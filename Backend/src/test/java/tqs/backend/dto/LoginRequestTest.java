package tqs.backend.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginRequestTest {

    private Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenValid_thenNoViolations() {
        LoginRequest login = new LoginRequest();
        login.setEmail("user@example.com");
        login.setPassword("securepass");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(login);
        assertThat(violations).isEmpty();
    }

    @Test
    void whenInvalidEmail_thenViolation() {
        LoginRequest login = new LoginRequest();
        login.setEmail("invalid");
        login.setPassword("securepass");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(login);
        assertThat(violations).extracting("propertyPath").anyMatch(path -> path.toString().equals("email"));
    }

    @Test
    void whenBlankPassword_thenViolation() {
        LoginRequest login = new LoginRequest();
        login.setEmail("user@example.com");
        login.setPassword("");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(login);
        assertThat(violations).extracting("propertyPath").anyMatch(path -> path.toString().equals("password"));
    }
}
