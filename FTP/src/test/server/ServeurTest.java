package test.server;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import server.FtpRequest;
import server.Serveur;

public class ServeurTest{
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test_load_map_user() throws IOException {
		String filename = "Data/Test/test_load_map_user.txt";
		//creation dun fichier pour les tests
		PrintWriter writer = new PrintWriter(filename);
		//on écrit deux lignes
		writer.println("TOTO 123");
		writer.println("EMILIEN 321");
		writer.close();
			
		Serveur s = new Serveur(4000,filename);
		HashMap<String, String> hm = s.getMap_user();
		
		assertTrue("123".equals(hm.get("TOTO")));
		assertTrue("321".equals(hm.get("EMILIEN"))); 
		assertTrue(null == hm.get("AZERTY"));	
	}


}
