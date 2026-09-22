package com.example.stegweb.service;

import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

@Service
public class StegService {

    private static final int MAGIC = 0x53544547; // "STEG" magic header

    /**
     * Encodes raw byte payload into the least significant bits (LSB) of a PNG image.
     */
    public BufferedImage encodeLSB(BufferedImage src, byte[] payload) {
        if (src == null) throw new IllegalArgumentException("Input image cannot be null");
        if (payload == null) payload = new byte[0];

        int width = src.getWidth();
        int height = src.getHeight();
        // 32 bits magic + 32 bits length + payload bits
        int neededBits = 32 + 32 + payload.length * 8;
        int capacityBits = width * height * 3;

        if (neededBits > capacityBits) {
            throw new IllegalArgumentException("Secret payload is too large for this image. Capacity: " 
                    + capacityBits + " bits, Needed: " + neededBits + " bits.");
        }

        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Build BitStream: MAGIC + Length + Payload
        BitStream stream = new BitStream(neededBits);
        stream.add(MAGIC, 32);
        stream.add(payload.length, 32);
        for (byte b : payload) {
            stream.add(b, 8);
        }

        int bitIdx = 0;
        int totalBits = stream.size();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = src.getRGB(x, y);
                int a = (argb >>> 24) & 0xFF;
                int r = (argb >>> 16) & 0xFF;
                int g = (argb >>> 8) & 0xFF;
                int b = (argb) & 0xFF;

                if (bitIdx < totalBits) r = (r & 0xFE) | stream.get(bitIdx++);
                if (bitIdx < totalBits) g = (g & 0xFE) | stream.get(bitIdx++);
                if (bitIdx < totalBits) b = (b & 0xFE) | stream.get(bitIdx++);

                int newArgb = (a << 24) | (r << 16) | (g << 8) | b;
                out.setRGB(x, y, newArgb);
            }
        }
        return out;
    }

    /**
     * Extracts byte payload embedded in the LSB of a PNG image.
     */
    public byte[] decodeLSB(BufferedImage img) {
        if (img == null) throw new IllegalArgumentException("Input image cannot be null");

        int width = img.getWidth();
        int height = img.getHeight();
        int capacityBits = width * height * 3;

        long maxHeaderBits = 64; // Magic (32) + Len (32)
        if (capacityBits < maxHeaderBits) {
            throw new IllegalArgumentException("Image dimensions are too small to contain a steganographic payload.");
        }

        LsbReader reader = new LsbReader(img);
        
        int magic = reader.readInt(32);
        int length;

        if (magic == MAGIC) {
            length = reader.readInt(32);
        } else {
            // Legacy fallback check where first 32 bits represented length
            length = magic;
            if (length <= 0) {
                throw new IllegalArgumentException("No valid steganography payload header found in image.");
            }
        }

        if (length < 0 || length > 200_000_000) {
            throw new IllegalArgumentException("Invalid or missing steganography header. Image does not contain hidden data.");
        }

        long neededDataBits = (long) length * 8;
        if (reader.remaining() < neededDataBits) {
            throw new IllegalArgumentException("Header indicates " + length + " bytes, but image size is insufficient.");
        }

        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = (byte) reader.readInt(8);
        }
        return data;
    }

    private static class BitStream {
        private final int[] bits;
        private int count = 0;

        public BitStream(int capacity) {
            bits = new int[capacity];
        }

        public void add(int value, int numBits) {
            for (int i = numBits - 1; i >= 0; i--) {
                bits[count++] = (value >>> i) & 1;
            }
        }

        public int get(int idx) { return bits[idx]; }
        public int size() { return count; }
    }

    private static class LsbReader {
        private final BufferedImage img;
        private final int width, height;
        private int x = 0, y = 0, ch = 0; // 0=Red, 1=Green, 2=Blue

        public LsbReader(BufferedImage img) {
            this.img = img;
            this.width = img.getWidth();
            this.height = img.getHeight();
        }

        public int readInt(int bits) {
            int val = 0;
            for (int i = 0; i < bits; i++) {
                if (y >= height) throw new IndexOutOfBoundsException("End of image bounds reached");
                int rgb = img.getRGB(x, y);
                int bit = 0;
                if (ch == 0) bit = (rgb >> 16) & 1;
                else if (ch == 1) bit = (rgb >> 8) & 1;
                else bit = rgb & 1;

                val = (val << 1) | bit;
                advance();
            }
            return val;
        }

        public long remaining() {
            long total = (long) width * height * 3;
            long used = (long) y * width * 3 + x * 3 + ch;
            return total - used;
        }

        private void advance() {
            ch++;
            if (ch > 2) {
                ch = 0;
                x++;
                if (x >= width) {
                    x = 0;
                    y++;
                }
            }
        }
    }
}
