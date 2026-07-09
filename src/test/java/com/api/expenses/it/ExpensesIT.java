package com.api.expenses.it;

import com.api.expenses.TestcontainersConfiguration;
import com.api.expenses.model.dto.ClaimRequest;
import com.api.expenses.model.dto.ClaimResponse;
import com.api.expenses.model.dto.LoginRequest;
import com.api.expenses.model.entity.Category;
import com.api.expenses.model.entity.ClaimStatus;
import com.api.expenses.model.entity.ExpenseClaim;
import com.api.expenses.model.entity.Role;
import com.api.expenses.model.entity.User;
import com.api.expenses.repository.ExpenseClaimRepository;
import com.api.expenses.repository.UserRepository;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExpensesIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseClaimRepository claimRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.api.expenses.repository.AuditLogRepository auditLogRepository;

    @AfterEach
    void cleanup() {
        auditLogRepository.deleteAll();
        claimRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void submitClaim_WithValidClaimRequest_ReturnsCreatedAndAuditPersisted() {
        String username = "employee1";
        String password = "pass123";
        User u = User.builder().username(username).password(passwordEncoder.encode(password)).role(Role.EMPLOYEE).build();
        userRepository.save(u);

        String token = obtainToken(username, password);

        ClaimRequest req = ClaimRequest.builder()
                .description("Hotel stay")
                .amount(new BigDecimal("123.45"))
                .date(LocalDate.now())
                .category(Category.ACCOMODATION)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ClaimRequest> entity = new HttpEntity<>(req, headers);

        ResponseEntity<ClaimResponse> createResp = restTemplate.postForEntity("/api/claims", entity, ClaimResponse.class);
        assertEquals(HttpStatus.CREATED, createResp.getStatusCode());
        ClaimResponse created = createResp.getBody();
        assertNotNull(created);

        ExpenseClaim claimEntity = claimRepository.findById(created.getId()).orElseThrow();
        var audits = auditLogRepository.findAllByClaimOrderByPerformedAtAsc(claimEntity);
        assertNotNull(audits);
        assertEquals("SUBMITTED", audits.get(0).getAction());
    }

    @Test
    void submitClaim_WithUnauthenticatedUser_ReturnsUnauthorized() {
        ClaimRequest req = ClaimRequest.builder()
                .description("Hotel stay")
                .amount(new BigDecimal("123.45"))
                .date(LocalDate.now())
                .category(Category.ACCOMODATION)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ClaimRequest> entity = new HttpEntity<>(req, headers);

        ResponseEntity<String> createResp = restTemplate.postForEntity("/api/claims", entity, String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, createResp.getStatusCode());
    }

    @Test
    void getMyClaims_WithAuthenticatedUser_ReturnsOk() {
        String username = "employee2";
        String password = "pw";
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(Role.EMPLOYEE)
                .build();
        userRepository.save(user);

        String token = obtainToken(username, password);

        ExpenseClaim claim = ExpenseClaim.builder()
                .employee(user)
                .description("d")
                .amount(new BigDecimal("10.00"))
                .date(LocalDate.now())
                .category(Category.MEALS)
                .status(ClaimStatus.PENDING)
                .build();
        claimRepository.save(claim);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> getEntity = new HttpEntity<>(headers);
        ResponseEntity<ClaimResponse[]> listResp = restTemplate.exchange("/api/claims", HttpMethod.GET, getEntity, ClaimResponse[].class);
        assertEquals(HttpStatus.OK, listResp.getStatusCode());
        ClaimResponse[] list = listResp.getBody();
        assertNotNull(list);
    }

    @Test
    void getById_WithAuthenticatedUserButNotAuthorized_ReturnsUnauthorized() {
        User owner = User.builder().username("owner").password(passwordEncoder.encode("p")).role(Role.EMPLOYEE).build();
        userRepository.save(owner);
        ExpenseClaim claim = ExpenseClaim.builder()
                .employee(owner)
                .description("x")
                .amount(new BigDecimal("5.00"))
                .date(LocalDate.now())
                .category(Category.MEALS)
                .status(ClaimStatus.PENDING)
                .build();
        claim = claimRepository.save(claim);

        User other = User.builder()
                .username("other")
                .password(passwordEncoder.encode("p"))
                .role(Role.EMPLOYEE)
                .build();
        userRepository.save(other);
        String token = obtainToken("other", "p");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> getEntity = new HttpEntity<>(headers);

        ResponseEntity<String> resp = restTemplate.exchange("/api/claims/" + claim.getId(), HttpMethod.GET, getEntity, String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }

    @Ignore("To be implemented")
    void approvals_ApproveAndRejectClaimsWithAudit_ReturnsSuccessful() {

    }

    private String obtainToken(final String username, final String password) {
        LoginRequest req = LoginRequest.builder().username(username).password(password).build();
        ResponseEntity<String> resp = restTemplate.postForEntity("/api/auth/login", req, String.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        String body = resp.getBody();
        assertNotNull(body);
        int t = body.indexOf("token");
        int colon = body.indexOf(':', t);
        int quote = body.indexOf('"', colon);
        int end = body.indexOf('"', quote + 1);
        return body.substring(quote + 1, end);
    }
}
