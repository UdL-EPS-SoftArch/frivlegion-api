package com.frivlegion.platform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "jwt.secret=test-secret-key-that-is-long-enough-for-hmac-sha-signing-2026")
class FrivLegionApplicationTests {

	@Test
	void contextLoads() {
	}

}
