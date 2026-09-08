package com.cambistaonline.auth.adapters.outbound.mfa;

import com.cambistaonline.auth.application.ports.outbound.TotpPort;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * Adaptador de salida para TOTP (Time-based One-time Password) según RFC 6238.
 * 100% compatible con Google Authenticator, Microsoft Authenticator y Authy.
 * No requiere librerías externas pesadas: utiliza javax.crypto.Mac estándar con HMAC-SHA1.
 */
@Component
public class TotpAdapter implements TotpPort {

    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int SECRET_BYTE_LENGTH = 20; // 160 bits recomendado por RFC 4226/6238
    private static final int TIME_STEP_SECONDS = 30;
    private static final int DIGITS = 6;
    private static final int WINDOW = 4; // Tolerancia de ±4 intervalos de 30s (±120s para compensar desincronización de reloj móvil/servidor)

    private final SecureRandom random = new SecureRandom();

    @Override
    public String generateSecret() {
        byte[] buffer = new byte[SECRET_BYTE_LENGTH];
        random.nextBytes(buffer);
        return base32Encode(buffer);
    }

    @Override
    public String getOtpAuthUri(String secret, String accountName, String issuer) {
        String cleanIssuer = (issuer != null && !issuer.isBlank()) ? issuer.trim() : "CambistaOnline";
        String cleanAccount = (accountName != null && !accountName.isBlank()) ? accountName.trim() : "user";
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=%d&period=%d",
                cleanIssuer, cleanAccount, secret, cleanIssuer, DIGITS, TIME_STEP_SECONDS);
    }

    @Override
    public boolean verifyCode(String secret, String code) {
        if (secret == null || code == null) {
            return false;
        }

        String cleanCode = code.trim().replaceAll("\\s+", "");
        if (cleanCode.length() != DIGITS) {
            return false;
        }

        long currentInterval = System.currentTimeMillis() / 1000 / TIME_STEP_SECONDS;
        byte[] keyBytes;
        try {
            keyBytes = base32Decode(secret.trim().toUpperCase());
        } catch (Exception e) {
            return false;
        }

        for (int i = -WINDOW; i <= WINDOW; i++) {
            long hashInterval = currentInterval + i;
            String generatedCode = generateTotpCode(keyBytes, hashInterval);
            if (generatedCode.equals(cleanCode)) {
                return true;
            }
        }
        return false;
    }

    String generateTotpCode(byte[] key, long interval) {
        try {
            byte[] data = ByteBuffer.allocate(8).putLong(interval).array();
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);

            int offset = hash[hash.length - 1] & 0xF;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);

            int otp = binary % 1_000_000;
            return String.format("%06d", otp);
        } catch (Exception e) {
            throw new IllegalStateException("Error al calcular HMAC-SHA1 para TOTP", e);
        }
    }

    // ─── Codificador y Decodificador Base32 puro según RFC 4648 ───────────────

    String base32Encode(byte[] data) {
        StringBuilder sb = new StringBuilder((data.length * 8 + 4) / 5);
        int buffer = 0;
        int next = 0;
        int bitsLeft = 0;
        while (next < data.length) {
            buffer <<= 8;
            buffer |= (data[next++] & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                bitsLeft -= 5;
                int index = (buffer >> bitsLeft) & 0x1F;
                sb.append(BASE32_CHARS.charAt(index));
            }
        }
        if (bitsLeft > 0) {
            buffer <<= (5 - bitsLeft);
            int index = buffer & 0x1F;
            sb.append(BASE32_CHARS.charAt(index));
        }
        return sb.toString();
    }

    byte[] base32Decode(String base32) {
        String clean = base32.trim().replace("=", "");
        int outputLength = clean.length() * 5 / 8;
        byte[] result = new byte[outputLength];
        int buffer = 0;
        int bitsLeft = 0;
        int count = 0;

        for (int i = 0; i < clean.length(); i++) {
            char c = clean.charAt(i);
            int val = BASE32_CHARS.indexOf(c);
            if (val < 0) {
                throw new IllegalArgumentException("Carácter Base32 inválido: " + c);
            }
            buffer = (buffer << 5) | val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bitsLeft -= 8;
                if (count < outputLength) {
                    result[count++] = (byte) ((buffer >> bitsLeft) & 0xFF);
                }
            }
        }
        return result;
    }
}
