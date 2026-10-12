package jar;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jar.controller.AuthenticationController;
import jar.controller.HealthController;
import jar.controller.MatchingController;
import jar.data.LoginRequest;
import jar.data.RegisterRequest;
import jar.item.FoundItem;
import jar.item.FoundItemController;
import jar.item.LostItem;
import jar.item.LostItemController;
import jar.model.User;
import jar.service.MatchingService;

@SpringBootTest
public class ApiIntegrationTests {

    @Autowired
    private HealthController healthController;

    @Autowired
    private AuthenticationController authenticationController;

    @Autowired
    private LostItemController lostItemController;

    @Autowired
    private FoundItemController foundItemController;

    @Autowired
    private MatchingController matchingController;

    @Autowired
    private org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource;

    @Test
    void testCorsConfigurationAllowsVercelOrigin() {
        org.springframework.mock.web.MockHttpServletRequest request =
                new org.springframework.mock.web.MockHttpServletRequest("OPTIONS", "/api/auth/login");
        request.addHeader("Origin", "https://ai-lost-found-frontend.vercel.app");
        request.addHeader("Access-Control-Request-Method", "POST");

        org.springframework.web.cors.CorsConfiguration config =
                corsConfigurationSource.getCorsConfiguration(request);

        assertNotNull(config, "CORS configuration should be present for request");
        assertTrue(config.getAllowedOrigins().contains("https://ai-lost-found-frontend.vercel.app"),
                "Allowed origins must include deployed Vercel frontend");
        assertTrue(config.getAllowCredentials());
    }

    @Test
    void testHealthEndpoint() {
        ResponseEntity<Map<String, Object>> response = healthController.health();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
        assertEquals("CONNECTED", response.getBody().get("database"));
    }

    @Test
    void testRootEndpoint() {
        ResponseEntity<Map<String, Object>> response = healthController.root();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("RUNNING", response.getBody().get("status"));
    }

    @Test
    void testUserRegistrationAndLoginFlow() {
        String testEmail = "testuser_" + System.currentTimeMillis() + "@example.com";

        // Register new user
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setName("Test Student");
        registerReq.setEmail(testEmail);
        registerReq.setPassword("Password123!");

        ResponseEntity<?> regResponse = authenticationController.register(registerReq);
        assertEquals(HttpStatus.CREATED, regResponse.getStatusCode());
        assertEquals("User registered successfully", regResponse.getBody());

        // Duplicate registration should return CONFLICT
        ResponseEntity<?> dupResponse = authenticationController.register(registerReq);
        assertEquals(HttpStatus.CONFLICT, dupResponse.getStatusCode());

        // Login with valid credentials
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail(testEmail);
        loginReq.setPassword("Password123!");

        ResponseEntity<?> loginResponse = authenticationController.login(loginReq);
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        User loggedInUser = (User) loginResponse.getBody();
        assertNotNull(loggedInUser);
        tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();
        String json = mapper.writeValueAsString(loggedInUser);
        assertFalse(json.contains("password"), "Password must not be exposed in JSON response");

        // Login with invalid password
        LoginRequest badLogin = new LoginRequest();
        badLogin.setEmail(testEmail);
        badLogin.setPassword("WrongPassword");

        ResponseEntity<?> badResponse = authenticationController.login(badLogin);
        assertEquals(HttpStatus.UNAUTHORIZED, badResponse.getStatusCode());
    }

    @Test
    void testItemCreationAndMatchingFlow() {
        // Register user for this test
        String email = "matching_user_" + System.currentTimeMillis() + "@example.com";
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setName("Item Owner");
        registerReq.setEmail(email);
        registerReq.setPassword("Secure123!");
        authenticationController.register(registerReq);

        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail(email);
        loginReq.setPassword("Secure123!");
        User user = (User) authenticationController.login(loginReq).getBody();
        assertNotNull(user);

        // Report lost item
        LostItem lost = new LostItem();
        lost.setItemName("Blue Hydro Flask");
        lost.setCategory("Bottle");
        lost.setLocation("Library 2nd Floor");
        lost.setDescription("Blue metal water bottle with small dent");
        lost.setLostDate("2026-10-10");

        ResponseEntity<?> lostRes = lostItemController.addLostItem(lost, user.getId());
        assertEquals(HttpStatus.CREATED, lostRes.getStatusCode());
        LostItem savedLost = (LostItem) lostRes.getBody();
        assertNotNull(savedLost);
        assertNotNull(savedLost.getId());

        // Report found item matching the lost item
        FoundItem found = new FoundItem();
        found.setItemName("Blue Hydro Flask");
        found.setCategory("Bottle");
        found.setLocation("Library 2nd Floor");
        found.setDescription("Blue metal water bottle found near tables");
        found.setFoundDate("2026-10-10");

        ResponseEntity<?> foundRes = foundItemController.addFoundItem(found, user.getId());
        assertEquals(HttpStatus.CREATED, foundRes.getStatusCode());
        FoundItem savedFound = (FoundItem) foundRes.getBody();
        assertNotNull(savedFound);

        // Run matching
        ResponseEntity<?> matchRes = matchingController.findMatches(savedLost.getId());
        assertEquals(HttpStatus.OK, matchRes.getStatusCode());
        @SuppressWarnings("unchecked")
        List<MatchingService.MatchResult> matches = (List<MatchingService.MatchResult>) matchRes.getBody();
        assertNotNull(matches);
        assertFalse(matches.isEmpty(), "Expected at least one matching found item");
        assertTrue(matches.get(0).getMatchScore() >= 50, "Match score should be >= 50");
    }
}
