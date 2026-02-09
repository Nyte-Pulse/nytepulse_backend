package NytePulse.backend.auth;


import NytePulse.backend.entity.User;
import NytePulse.backend.exception.TokenRefreshException;
import NytePulse.backend.repository.RefreshTokenRepository;
import NytePulse.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${app.jwt-refresh-expiration-milliseconds}")
    private Long refreshTokenDurationMs;

    @Value("${app.max-refresh-tokens-per-user}")
    private int maxRefreshTokensPerUser;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    public Optional<RefreshToken> findByToken(String token) {
        System.out.println("Finding refresh token: " + token);
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken createRefreshToken(Long userId, String deviceInfo, String ipAddress) {
        try{
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Clean up expired tokens for this user
        cleanupExpiredTokens(userId);

        // Limit the number of active tokens per user
        List<RefreshToken> userTokens = refreshTokenRepository.findByUser(user);
        if (userTokens.size() >= maxRefreshTokensPerUser) {
            // Remove oldest token if limit reached
            RefreshToken oldestToken = userTokens.stream()
                    .min((t1, t2) -> t1.getExpiryDate().compareTo(t2.getExpiryDate()))
                    .orElse(null);
            if (oldestToken != null) {
                refreshTokenRepository.delete(oldestToken);
            }
        }

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setDeviceInfo(deviceInfo);
        refreshToken.setIpAddress(ipAddress);

        return refreshTokenRepository.save(refreshToken);
        }
        catch (Exception e) {
            throw new RuntimeException("Error creating refresh token: " + e.getMessage());
        }
    }


    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(),
                    "Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Transactional
    public int deleteByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return refreshTokenRepository.deleteByUser(user);
    }

    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshTokenRepository::delete);
    }

    @Transactional
    public void cleanupExpiredTokens(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<RefreshToken> tokens = refreshTokenRepository.findByUser(user);
        tokens.stream()
                .filter(t -> t.getExpiryDate().compareTo(Instant.now()) < 0)
                .forEach(refreshTokenRepository::delete);
    }

    public List<RefreshToken> getUserActiveTokens(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return refreshTokenRepository.findByUser(user);
    }
}
