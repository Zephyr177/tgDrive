package com.skydevs.tgdrive;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

@SpringBootTest
public class TemporaryPasswordGeneratorTest {
    @Test
    public void generator() {
        Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

        // admin 密码: 123456
        String adminPassword = "123456";
        String adminHash = encoder.encode(adminPassword);
        System.out.println("Admin Hash: " + adminHash);

        // visitor 密码: hello
        String visitorPassword = "hello";
        String visitorHash = encoder.encode(visitorPassword);
        System.out.println("Visitor Hash: " + visitorHash);
    }
}
