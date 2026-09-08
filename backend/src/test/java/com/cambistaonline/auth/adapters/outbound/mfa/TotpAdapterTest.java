package com.cambistaonline.auth.adapters.outbound.mfa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TotpAdapterTest {

    private TotpAdapter totpAdapter;

    @BeforeEach
    void setUp() {
        totpAdapter = new TotpAdapter();
    }

    @Test
    @DisplayName("Debe generar un secreto Base32 válido de 32 caracteres")
    void shouldGenerateValidBase32Secret() {
        String secret = totpAdapter.generateSecret();

        assertNotNull(secret);
        assertEquals(32, secret.length());
        assertTrue(secret.matches("^[A-Z2-7]+$"), "El secreto debe cumplir con la especificación Base32 RFC 4648");
    }

    @Test
    @DisplayName("Debe generar URI compatible con Google Authenticator")
    void shouldGenerateOtpAuthUri() {
        String secret = "JBSWY3DPEHPK3PXPJBSWY3DPEHPK3PXP";
        String uri = totpAdapter.getOtpAuthUri(secret, "usuario@correo.com", "CambistaOnline");

        assertNotNull(uri);
        assertTrue(uri.startsWith("otpauth://totp/CambistaOnline:usuario%40correo.com"));
        assertTrue(uri.contains("secret=JBSWY3DPEHPK3PXPJBSWY3DPEHPK3PXP"));
        assertTrue(uri.contains("issuer=CambistaOnline"));
    }

    @Test
    @DisplayName("Debe rechazar códigos nulos, vacíos o con longitud incorrecta")
    void shouldRejectInvalidCodes() {
        String secret = totpAdapter.generateSecret();

        assertFalse(totpAdapter.verifyCode(secret, null));
        assertFalse(totpAdapter.verifyCode(secret, ""));
        assertFalse(totpAdapter.verifyCode(secret, "12345"));
        assertFalse(totpAdapter.verifyCode(secret, "1234567"));
        assertFalse(totpAdapter.verifyCode(null, "123456"));
    }
}
