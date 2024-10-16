//package com.rio_rishabhNEU.UserApp;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//
//@SpringBootTest
//@ActiveProfiles("test")
//class UserAppApplicationTests {
//
//	@Test
//	void contextLoads() {
//	}
//
//}



package com.rio_rishabhNEU.UserApp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = UserAppApplication.class)
@ActiveProfiles("test")
class UserAppApplicationTests {

	@Test
	void contextLoads() {
		// This test will fail if the application context cannot be loaded
	}

}