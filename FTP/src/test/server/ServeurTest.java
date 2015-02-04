package test.server;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import server.FtpRequest;
import server.Serveur;

public class ServeurTest{
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() throws IOException {
		// on lance le server
		// il recoit une requete de connexion
		// il renvoit "code ok"
		String msg = "USER TOTO";
		Serveur s = new Serveur();
		
		FtpRequest req = new FtpRequest(null,msg,s.getMap_user());

		assertEquals("comparaison du code d'erreur","331 User name okay, need password.\n",req.response(msg));
	}

	

}
