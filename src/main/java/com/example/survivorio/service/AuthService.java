package com.example.survivorio.service;

import com.example.survivorio.dto.AuthRequest;
import com.example.survivorio.dto.AuthResponse;
import com.example.survivorio.entity.AppUser;
import com.example.survivorio.entity.AuthSession;
import com.example.survivorio.repository.AuthSessionRepository;
import com.example.survivorio.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class AuthService {
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final AuthSessionRepository sessionRepository;

    public AuthService(UserRepository userRepository, AuthSessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    public AuthResponse register(AuthRequest request) {
        String username = normalizeUsername(request.username());
        String password = normalizePassword(request.password());

        if (username.length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username must have at least 3 characters");
        }
        if (password.length() < 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must have at least 4 characters");
        }
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(hashPassword(password));
        user.setCreatedAt(Instant.now().toString());

        return createSession(userRepository.save(user));
    }

    public AuthResponse login(AuthRequest request) {
        String username = normalizeUsername(request.username());
        String password = normalizePassword(request.password());

        AppUser user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        if (!verifyPassword(password, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        return createSession(user);
    }

    public AppUser requireUser(String authorizationHeader) {
        String token = readBearerToken(authorizationHeader);

        return sessionRepository.findByToken(token)
                .map(AuthSession::getUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required"));
    }

    public AuthResponse currentUser(String authorizationHeader) {
        AppUser user = requireUser(authorizationHeader);
        return new AuthResponse(readBearerToken(authorizationHeader), user.getUsername());
    }

    @Transactional
    public void logout(String authorizationHeader) {
        String token = readBearerToken(authorizationHeader);
        sessionRepository.deleteByToken(token);
    }

    private AuthResponse createSession(AppUser user) {
        AuthSession session = new AuthSession();
        session.setToken(UUID.randomUUID().toString() + UUID.randomUUID());
        session.setUser(user);
        session.setCreatedAt(Instant.now().toString());

        AuthSession saved = sessionRepository.save(session);
        return new AuthResponse(saved.getToken(), user.getUsername());
    }

    private String readBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
        }

        String token = authorizationHeader.substring("Bearer ".length()).trim();
        if (token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
        }

        return token;
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim();
    }

    private String normalizePassword(String password) {
        return password == null ? "" : password;
    }

    private String hashPassword(String password) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

        return ITERATIONS
                + ":"
                + Base64.getEncoder().encodeToString(salt)
                + ":"
                + Base64.getEncoder().encodeToString(hash);
    }

    private boolean verifyPassword(String password, String storedHash) {
        String[] parts = storedHash.split(":");
        if (parts.length != 3) {
            return false;
        }

        int iterations = Integer.parseInt(parts[0]);
        byte[] salt = Base64.getDecoder().decode(parts[1]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[2]);
        byte[] actualHash = pbkdf2(password.toCharArray(), salt, iterations, expectedHash.length * 8);

        return MessageDigest.isEqual(expectedHash, actualHash);
    }

    private byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (Exception exception) {
            throw new IllegalStateException("Could not hash password", exception);
        }
    }
}
