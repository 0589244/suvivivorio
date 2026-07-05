package com.example.survivorio.service;

import com.example.survivorio.dto.AuthRequest;
import com.example.survivorio.dto.AuthResponse;
import com.example.survivorio.entity.AppUser;
import com.example.survivorio.entity.AuthSession;
import com.example.survivorio.repository.AuthSessionRepository;
import com.example.survivorio.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthSessionRepository sessionRepository;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, sessionRepository);
    }

    @Test
    void registerHashesPasswordAndCreatesSession() {
        AtomicReference<AppUser> savedUser = new AtomicReference<>();

        when(userRepository.existsByUsernameIgnoreCase("Hero")).thenReturn(false);
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setId(1L);
            savedUser.set(user);
            return user;
        });
        when(sessionRepository.save(any(AuthSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.register(new AuthRequest(" Hero ", "secret"));

        assertThat(response.username()).isEqualTo("Hero");
        assertThat(response.token()).isNotBlank();
        assertThat(savedUser.get().getPasswordHash())
                .isNotBlank()
                .doesNotContain("secret")
                .contains(":");

        ArgumentCaptor<AuthSession> sessionCaptor = ArgumentCaptor.forClass(AuthSession.class);
        verify(sessionRepository).save(sessionCaptor.capture());
        assertThat(sessionCaptor.getValue().getUser()).isSameAs(savedUser.get());
    }

    @Test
    void loginRejectsUnknownUser() {
        when(userRepository.findByUsernameIgnoreCase("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new AuthRequest("missing", "secret")))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void requireUserNeedsBearerToken() {
        assertThatThrownBy(() -> authService.requireUser(null))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
