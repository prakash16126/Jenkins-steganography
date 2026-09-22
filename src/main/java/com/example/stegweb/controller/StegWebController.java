package com.example.stegweb.controller;

import com.example.stegweb.service.StegService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Controller
public class StegWebController {

    private final StegService stegService;

    public StegWebController(StegService stegService) {
        this.stegService = stegService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/api/encode")
    @ResponseBody
    public ResponseEntity<?> encodeMessage(@RequestParam("image") MultipartFile file,
                                           @RequestParam("message") String message) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Uploaded image file is empty."));
            }
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Secret message cannot be empty."));
            }

            BufferedImage srcImage = ImageIO.read(file.getInputStream());
            if (srcImage == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Failed to read image file. Please upload a valid PNG image."));
            }

            byte[] payload = message.getBytes(StandardCharsets.UTF_8);
            BufferedImage encodedImage = stegService.encodeLSB(srcImage, payload);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(encodedImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("attachment", "encoded_stego.png");
            headers.setContentLength(imageBytes.length);

            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error encoding message: " + e.getMessage()));
        }
    }

    @PostMapping("/api/decode")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> decodeMessage(@RequestParam("image") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("error", "Uploaded stego-image file is empty.");
                return ResponseEntity.badRequest().body(response);
            }

            BufferedImage img = ImageIO.read(file.getInputStream());
            if (img == null) {
                response.put("success", false);
                response.put("error", "Failed to read image file. Please upload a valid PNG image.");
                return ResponseEntity.badRequest().body(response);
            }

            byte[] decodedBytes = stegService.decodeLSB(img);
            String secretMessage = new String(decodedBytes, StandardCharsets.UTF_8);

            response.put("success", true);
            response.put("message", secretMessage);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Error decoding stego-image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
