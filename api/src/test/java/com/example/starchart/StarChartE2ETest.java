package com.example.starchart;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(properties = {
  "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration"
})

class StarChartE2ETest {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("starchart")
      .withUsername("starchart")
      .withPassword("starchart");

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", postgres::getJdbcUrl);
    r.add("spring.datasource.username", postgres::getUsername);
    r.add("spring.datasource.password", postgres::getPassword);

    // IMPORTANT: ensure Flyway runs in tests (it should by default)
    r.add("spring.flyway.enabled", () -> "true");
  }

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper om;

  private String parentToken;
  private String viewerToken;
  private String childId;

  @BeforeEach
  void setup() throws Exception {
    // register parent + login
    register("parent1@test.com", "Passw0rd!");
    parentToken = login("parent1@test.com", "Passw0rd!");

    // create family
    mvc.perform(post("/api/v1/family")
        .header("Authorization", "Bearer " + parentToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"name\":\"Kotecha Family\"}"))
      .andExpect(status().isOk());

    // create child
    var childRes = mvc.perform(post("/api/v1/children")
        .header("Authorization", "Bearer " + parentToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"name\":\"Arya\"}"))
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();

    childId = om.readTree(childRes).get("id").asText();
    assertThat(childId).isNotBlank();

    // create viewer + login
    mvc.perform(post("/api/v1/viewers")
        .header("Authorization", "Bearer " + parentToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"email\":\"viewer1@test.com\",\"password\":\"Passw0rd!\"}"))
      .andExpect(status().isOk());

    viewerToken = login("viewer1@test.com", "Passw0rd!");
  }

  @Test
  void viewer_can_read_stars_but_cannot_write() throws Exception {
    // parent adds stars
    mvc.perform(post("/api/v1/children/" + childId + "/stars")
        .header("Authorization", "Bearer " + parentToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"delta\":1,\"reason\":\"Brushed teeth\"}"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.totalStars").value(1));

    // viewer can read
    mvc.perform(get("/api/v1/children/" + childId + "/stars")
        .header("Authorization", "Bearer " + viewerToken))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.totalStars").value(1));

    // viewer cannot write
    mvc.perform(post("/api/v1/children/" + childId + "/stars")
        .header("Authorization", "Bearer " + viewerToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"delta\":1,\"reason\":\"Should fail\"}"))
      .andExpect(status().isForbidden());
  }

  // ---------- helpers ----------

  private void register(String email, String password) throws Exception {
    // register may be idempotent in your API; if it returns 400 for duplicates, that's fine too.
    mvc.perform(post("/api/v1/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
      .andExpect(result -> {
        int s = result.getResponse().getStatus();
        assertThat(s == 200 || s == 201 || s == 400).isTrue();
      });
  }

  private String login(String email, String password) throws Exception {
    var res = mvc.perform(post("/api/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();

    JsonNode json = om.readTree(res);
    String token = json.path("accessToken").asText("");
    assertThat(token).isNotBlank();
    return token;
  }
}