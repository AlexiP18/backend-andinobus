package com.andinobus.backendsmartcode.usuarios.api.controllers;

import com.andinobus.backendsmartcode.usuarios.api.dto.AuthDtos;
import com.andinobus.backendsmartcode.usuarios.application.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private AuthService authService = mock(AuthService.class);

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(authService);
    }
    @Test
    void login_delegatesToService() {
        AuthDtos.LoginRequest req = new AuthDtos.LoginRequest();
        req.setEmail("a@b.com");
        req.setPassword("pwd");

        AuthDtos.AuthResponse resp = AuthDtos.AuthResponse.builder()
                .token("tok")
                .userId(10L)
                .email("a@b.com")
                .rol("CLIENTE")
                .build();

        when(authService.login(req)).thenReturn(resp);

        AuthDtos.AuthResponse out = controller.login(req);

        assertSame(resp, out);
        verify(authService).login(req);
    }

    @Test
    void register_delegatesAndReturnsCreated() {
        AuthDtos.RegisterRequest req = new AuthDtos.RegisterRequest();
        req.setEmail("new@b.com");
        req.setPassword("pwd");

        AuthDtos.AuthResponse resp = AuthDtos.AuthResponse.builder().token("r").userId(11L).rol("CLIENTE").build();
        when(authService.register(req)).thenReturn(resp);

        AuthDtos.AuthResponse out = controller.register(req);
        assertSame(resp, out);
        verify(authService).register(req);
    }
    @Test
    void me_usesDemoTokenIfPresent() {
        AuthDtos.MeResponse resp = AuthDtos.MeResponse.builder().userId(1L).email("u@b.com").rol("CLIENTE").build();
        when(authService.getMeByToken("demo-token")).thenReturn(resp);

        AuthDtos.MeResponse out = controller.me(null, "demo-token");
        assertSame(resp, out);
        verify(authService).getMeByToken("demo-token");
    }

    @Test
    void me_usesAuthorizationHeaderWhenDemoTokenMissing() {
        AuthDtos.MeResponse resp = AuthDtos.MeResponse.builder().userId(2L).email("u2@b.com").rol("CLIENTE").build();
        when(authService.getMeByToken("bearer-token")).thenReturn(resp);

        AuthDtos.MeResponse out = controller.me("Bearer bearer-token", null);
        assertSame(resp, out);
        verify(authService).getMeByToken("bearer-token");
    }

    @Test
    void me_throwsWhenNoTokenProvided() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> controller.me(null, null));
        assertTrue(ex.getMessage().contains("Token"));
        verifyNoInteractions(authService);
    }

    @Test
    void logout_callsServiceWhenTokenPresent() {
        controller.logout("Bearer mytoken");
        verify(authService).logout("mytoken");
    }

    @Test
    void logout_doesNotCallServiceWhenNoToken() {
        controller.logout(null);
        verify(authService, never()).logout(anyString());
    }
}
