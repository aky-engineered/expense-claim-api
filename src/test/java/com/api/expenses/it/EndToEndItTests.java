package com.api.expenses.it;

import com.api.expenses.TestcontainersConfiguration;
import com.api.expenses.model.dto.ClaimRequest;
import com.api.expenses.model.dto.ClaimResponse;
import com.api.expenses.model.dto.LoginRequest;
import com.api.expenses.model.dto.RejectionRequest;
import com.api.expenses.model.entity.Category;
import com.api.expenses.model.entity.ClaimStatus;
import com.api.expenses.model.entity.ExpenseClaim;
import com.api.expenses.model.entity.Role;
import com.api.expenses.model.entity.User;
import com.api.expenses.repository.ExpenseClaimRepository;
import com.api.expenses.repository.UserRepository;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EndToEndItTests {

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

    private String obtainToken(String username, String password) {
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

    @Test
    void submitClaim_happyPath_createsAudit() {
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
        assertTrue(audits.size() >= 1);
        assertEquals("SUBMITTED", audits.get(0).getAction());
    }

    @Test
    void submitClaim_unauthenticated_returns401() {
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
    void getMyClaims_happyPath() {
        String username = "employee2";
        String password = "pw";
        User u = User.builder().username(username).password(passwordEncoder.encode(password)).role(Role.EMPLOYEE).build();
        userRepository.save(u);

        String token = obtainToken(username, password);

        // create claim directly
        ExpenseClaim c = ExpenseClaim.builder().employee(u).description("d").amount(new BigDecimal("10.00")).date(LocalDate.now()).category(Category.MEALS).status(ClaimStatus.PENDING).build();
        claimRepository.save(c);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> getEntity = new HttpEntity<>(headers);
        ResponseEntity<ClaimResponse[]> listResp = restTemplate.exchange("/api/claims", HttpMethod.GET, getEntity, ClaimResponse[].class);
        assertEquals(HttpStatus.OK, listResp.getStatusCode());
        ClaimResponse[] list = listResp.getBody();
        assertNotNull(list);
        assertTrue(list.length >= 1);
    }

    @Test
    void getById_notOwner_returns403() {
        User owner = User.builder().username("owner").password(passwordEncoder.encode("p")).role(Role.EMPLOYEE).build();
        userRepository.save(owner);
        ExpenseClaim claim = ExpenseClaim.builder().employee(owner).description("x").amount(new BigDecimal("5.00")).date(LocalDate.now()).category(Category.MEALS).status(ClaimStatus.PENDING).build();
        claim = claimRepository.save(claim);

        User other = User.builder().username("other").password(passwordEncoder.encode("p")).role(Role.EMPLOYEE).build();
        userRepository.save(other);
        String token = obtainToken("other", "p");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> getEntity = new HttpEntity<>(headers);

        ResponseEntity<String> resp = restTemplate.exchange("/api/claims/" + claim.getId(), HttpMethod.GET, getEntity, String.class);
        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
    }

    @Test
    void approvals_getPending_and_approve_and_reject_happyPath_withAudit() {
        User employee = User.builder().username("emp2").password(passwordEncoder.encode("p")).role(Role.EMPLOYEE).build();
        userRepository.save(employee);

        User approver = User.builder().username("approver1").password(passwordEncoder.encode("p")).role(Role.APPROVER).build();
        userRepository.save(approver);

        ExpenseClaim claim = ExpenseClaim.builder()
                .employee(employee)
                .description("Taxi")
                .amount(new BigDecimal("20.00"))
                .date(LocalDate.now())
                .category(Category.TRAVEL)
                .status(ClaimStatus.PENDING)
                .build();
        claim = claimRepository.save(claim);

        String token = obtainToken("approver1", "p");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<ClaimResponse[]> pendingResp = restTemplate.exchange("/api/approvals/pending", HttpMethod.GET, entity, ClaimResponse[].class);
        assertEquals(HttpStatus.OK, pendingResp.getStatusCode());

        ResponseEntity<ClaimResponse> approveResp = restTemplate.postForEntity("/api/approvals/" + claim.getId() + "/approve", entity, ClaimResponse.class);
        assertEquals(HttpStatus.OK, approveResp.getStatusCode());
        ClaimResponse approved = approveResp.getBody();
        assertNotNull(approved);
        assertEquals(ClaimStatus.APPROVED, approved.getStatus());

        ExpenseClaim updated = claimRepository.findById(claim.getId()).orElseThrow();
        var audits = auditLogRepository.findAllByClaimOrderByPerformedAtAsc(updated);
        assertTrue(audits.stream().anyMatch(a -> a.getAction().equals("APPROVED")));

        ExpenseClaim claim2 = ExpenseClaim.builder()
                .employee(employee)
                .description("Lunch")
                .amount(new BigDecimal("15.00"))
                .date(LocalDate.now())
                .category(Category.MEALS)
                .status(ClaimStatus.PENDING)
                .build();
        claim2 = claimRepository.save(claim2);

        RejectionRequest req = RejectionRequest.builder().reason("Not allowed").build();
        HttpEntity<RejectionRequest> rejectionEntity = new HttpEntity<>(req, headers);
        ResponseEntity<ClaimResponse> rejectResp = restTemplate.postForEntity("/api/approvals/" + claim2.getId() + "/reject", rejectionEntity, ClaimResponse.class);
        assertEquals(HttpStatus.OK, rejectResp.getStatusCode());
        ClaimResponse rejected = rejectResp.getBody();
        assertNotNull(rejected);
        assertEquals(ClaimStatus.REJECTED, rejected.getStatus());

        ExpenseClaim updated2 = claimRepository.findById(claim2.getId()).orElseThrow();
        var audits2 = auditLogRepository.findAllByClaimOrderByPerformedAtAsc(updated2);
        assertTrue(audits2.stream().anyMatch(a -> a.getAction().equals("REJECTED")));
    }
}
