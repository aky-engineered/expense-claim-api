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
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

    @AfterEach
    void cleanup() {
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
    void submitClaim_and_getMyClaims_and_getById_happyPath() {
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
        assertEquals("employee1", created.getEmployeeUserName());

        HttpEntity<Void> getEntity = new HttpEntity<>(headers);
        ResponseEntity<ClaimResponse[]> listResp = restTemplate.exchange("/api/claims", HttpMethod.GET, getEntity, ClaimResponse[].class);
        assertEquals(HttpStatus.OK, listResp.getStatusCode());
        ClaimResponse[] list = listResp.getBody();
        assertNotNull(list);
        assertTrue(list.length >= 1);

        Integer id = created.getId();
        ResponseEntity<ClaimResponse> byIdResp = restTemplate.exchange("/api/claims/" + id, HttpMethod.GET, getEntity, ClaimResponse.class);
        assertEquals(HttpStatus.OK, byIdResp.getStatusCode());
        ClaimResponse byId = byIdResp.getBody();
        assertNotNull(byId);
        assertEquals(id, byId.getId());
    }

    @Test
    void approvals_happyPath_pending_and_approve_and_reject() {
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
        ClaimResponse[] pendings = pendingResp.getBody();
        assertNotNull(pendings);
        assertTrue(pendings.length >= 1);

        ResponseEntity<ClaimResponse> approveResp = restTemplate.postForEntity("/api/approvals/" + claim.getId() + "/approve", entity, ClaimResponse.class);
        assertEquals(HttpStatus.OK, approveResp.getStatusCode());
        ClaimResponse approved = approveResp.getBody();
        assertNotNull(approved);
        assertEquals(ClaimStatus.APPROVED, approved.getStatus());

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
    }
