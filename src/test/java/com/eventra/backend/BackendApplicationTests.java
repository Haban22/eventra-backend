package com.eventra.backend;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requires running PostgreSQL and Redis — run against a real environment only")
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
