package test.server;

import static util.Messages.*;
import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import server.FtpRequest;

public class FtpRequestTest {

	FtpRequest f;
	@Before
	public void setUp() throws Exception {
		String msg_init = "USER TOTO";
		HashMap<String,String> hm = new HashMap<>();
		hm.put("TOTO", "963");
		f = new FtpRequest(null,msg_init,hm);
	}

	@Test
	public void test_processUSER() {
		
		String msg = "TOTO";
		
		String reponse = f.processUSER(msg);
		assertTrue(reponse.equals(USER_OK));
		
		msg = "ARTT";
		reponse = f.processUSER(msg);
		assertFalse(reponse.equals(USER_OK));
		assertTrue(reponse.equals(USER_INVALID));
		
	}
	
	@Test
	public void test_processPASS() {
		String user = "TOTO";
		String msg = "963";
		
		f.processUSER(user);
		String reponse = f.processPASS(msg);
		assertTrue(reponse.equals(PASS_OK));
		
		String msg1 = "SKLQKQ";
		reponse = f.processPASS(msg1);
		assertTrue(reponse.equals(NOT_LOGGED_IN));
	}
	
	@Test
	public void test_processLIST(){
		String msg = "";
			
		
	}
	
	@Test
	public void test_processQUIT() {
		assertTrue(f.processQUIT().equals(QUIT));
	}

}
