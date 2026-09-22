package com.example.stegweb;

import com.example.stegweb.service.StegService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StegWebApplicationTests {

    @Autowired
    private StegService stegService;

    @Test
    void contextLoads() {
        assertNotNull(stegService);
    }

    @Test
    void testEncodeAndDecodeSecretMessage() {
        // Create 100x100 RGB image
        BufferedImage src = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        String secret = "Top Secret Steganography Test Payload";

        byte[] payload = secret.getBytes(StandardCharsets.UTF_8);
        BufferedImage stegoImg = stegService.encodeLSB(src, payload);

        assertNotNull(stegoImg);

        byte[] decodedBytes = stegService.decodeLSB(stegoImg);
        String decodedSecret = new String(decodedBytes, StandardCharsets.UTF_8);

        assertEquals(secret, decodedSecret);
    }

    @Test
    void testDecodeEmptyOrInvalidImageThrowsException() {
        BufferedImage src = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        assertThrows(IllegalArgumentException.class, () -> stegService.decodeLSB(src));
    }
}
