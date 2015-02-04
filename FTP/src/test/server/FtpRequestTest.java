package test.server;

import static util.Messages.*;
import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import server.FtpRequest;

public class FtpRequestTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test_processUSER() {
		
		String msg = "TOTO";
		HashMap<String,String> hm = new HashMap<>();
		hm.put("TOTO", "963");
		FtpRequest f = new FtpRequest(null, msg, hm);
		String reponse = f.processUSER(msg);
		assertTrue(reponse.equals(USER_OK));
		
		msg = "ARTT";
		reponse = f.processUSER(msg);
		assertFalse(reponse.equals(USER_OK));
		assertTrue(reponse.equals(USER_NEED_ACCOUNT));
		
	}
	
	@Test
	public void test_processQUIT() {
		
		
		
	}

}
