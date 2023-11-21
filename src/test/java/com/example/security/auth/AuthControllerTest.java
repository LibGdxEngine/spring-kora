package com.example.security.auth;

import com.example.security.auth.dto.AuthenticationResponse;
import com.example.security.auth.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AuthControllerTest {
    @LocalServerPort
    private int port;
    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    public void testRegister() {
        // Create a RegisterRequest object
        RegisterRequest registerRequest = new RegisterRequest("ahmed",
                "fathy",
                "ahmed@gmail.com",
                "123456");

        // Send a POST request to /api/v1/auth/register
        ResponseEntity<AuthenticationResponse> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/auth/register",
                registerRequest,
                AuthenticationResponse.class
        );

        // Assertions for the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // Add more assertions as needed
    }

}
