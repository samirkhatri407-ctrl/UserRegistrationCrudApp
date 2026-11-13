package com.example.userregistration.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void clearUsers() {
        userRepository.deleteAll();
    }

    @Test
    void registerUserAndReadItBackWithoutReturningPasswordHash() throws Exception {
        String responseBody = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegistration()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Taylor"))
                .andExpect(jsonPath("$.lastName").value("Jordan"))
                .andExpect(jsonPath("$.email").value("taylor@example.com"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode response = objectMapper.readTree(responseBody);
        assertThat(response.has("passwordHash")).isFalse();
        assertThat(response.has("password")).isFalse();

        Long userId = response.get("id").asLong();
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.country").value("Canada"));

        User savedUser = userRepository.findById(userId).orElseThrow();
        assertThat(savedUser.getPasswordHash()).isNotEqualTo("StrongPass123!");
        assertThat(savedUser.getPasswordHash()).startsWith("$2");
    }

    @Test
    void rejectsDuplicateEmailRegardlessOfLetterCase() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegistration()))
                .andExpect(status().isCreated());

        String duplicateEmail = validRegistration()
                .replace("taylor@example.com", "TAYLOR@example.com");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateEmail))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "A user with this email address already exists."
                ));
    }

    @Test
    void updatesAndDeletesAUser() throws Exception {
        String responseBody = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegistration()))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long userId = objectMapper.readTree(responseBody).get("id").asLong();

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Taylor",
                                  "lastName": "Jordan",
                                  "email": "taylor@example.com",
                                  "dateOfBirth": "1995-06-15",
                                  "city": "Ottawa",
                                  "country": "Canada",
                                  "active": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Ottawa"))
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidRegistrationFields() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "",
                                  "lastName": "Jordan",
                                  "email": "not-an-email",
                                  "password": "short",
                                  "dateOfBirth": "2999-01-01"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isArray());
    }

    private String validRegistration() {
        return """
                {
                  "firstName": "Taylor",
                  "lastName": "Jordan",
                  "email": "taylor@example.com",
                  "password": "StrongPass123!",
                  "dateOfBirth": "1995-06-15",
                  "phoneNumber": "555-0100",
                  "addressLine1": "12 Example Street",
                  "addressLine2": "",
                  "city": "Toronto",
                  "stateProvince": "Ontario",
                  "postalCode": "M5V 2T6",
                  "country": "Canada"
                }
                """;
    }
}
