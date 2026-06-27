package io.github.bmarwell.jfmt.test.charset;

/**
 * Test file with Windows-1252 (Cp1252) specific characters.
 * This file contains characters that are specific to the Windows-1252 encoding:
 * - Euro sign: €
 * - Trademark: ™
 * - Copyright: ©
 * - Registered: ®
 */
public class Cp1252Test {
    
    // Windows-1252 specific characters in comments
    // Euro: € (U+20AC)
    // Trademark: ™ (U+2122)
    
    private static final String CURRENCY = "Price: 100€";
    private static final String TRADEMARK = "Product™";
    private static final String COPYRIGHT = "© 2024 Company";
    private static final String REGISTERED = "Brand®";
    
    /**
     * Method with special characters in documentation.
     * @param price The price in euros (€)
     * @return formatted string with trademark (™)
     */
    public String formatPrice(double price) {
        return String.format("%.2f€", price);
    }
    
    public String getCopyright() {
        return COPYRIGHT;
    }
}

// Made with Bob
