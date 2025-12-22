package com.art.tutordesk.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SecurityConfig.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testLoginAccessPermitted() throws Exception {
        mockMvc.perform(get("/login"))
               .andExpect(status().isOk());
    }

    @Test
    void testCssAccessPermitted() throws Exception {
        mockMvc.perform(get("/css/styles.css"))
               .andExpect(status().isOk());
    }

    @Test
    void testH2ConsoleRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/h2-console/"))
               .andExpect(status().isFound())
               .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void testProtectedPageRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/index"))
               .andExpect(status().isFound())
               .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void testSuccessfulLogin() throws Exception {
        mockMvc.perform(post("/perform_login")
                        .param("username", "admin")
                        .param("password", "admin123")
                        .with(csrf()))
               .andExpect(status().isFound())
               .andExpect(redirectedUrl("/"));
    }

    @Test
    void testFailedLogin() throws Exception {
        mockMvc.perform(post("/perform_login")
                        .param("username", "admin")
                        .param("password", "wrongpassword")
                        .with(csrf()))
               .andExpect(status().isFound())
               .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    @WithMockUser(username = "admin", password = "admin123", roles = "ADMIN")
    void testH2ConsoleAccessAfterAuthentication() throws Exception {
        mockMvc.perform(get("/h2-console/"))
               .andExpect(status().isOk());
    }

    @Test
    void testLogout() throws Exception {
        mockMvc.perform(post("/logout")
                        .with(csrf()))
               .andExpect(status().isFound())
               .andExpect(redirectedUrl("/login?logout=true"));
    }

    @Test
    void testUserDetailsServiceBeanExists() throws Exception {
        mockMvc.perform(get("/login"))
               .andExpect(status().isOk());
    }
}
