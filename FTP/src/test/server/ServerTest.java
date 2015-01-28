package test.server;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import server.FTPRequest;
import server.Server;

public class ServerTest{
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() throws IOException {
		// on lance le server
		// il recoit une requete de connexion
		// il renvoit "code ok"
		String msg = "USER TOTO";
		Server s = new Server();
		
		FTPRequest req = new FTPRequest(null,msg,s.getMap_user());

		assertEquals("comparaison du code d'erreur","331 User name okay, need password.\n",req.response(msg));
	}

	

}
