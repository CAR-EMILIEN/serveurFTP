package server;

import java.awt.BufferCapabilities;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;

public class FTPRequest extends Thread {
	
	protected Socket connexion;
	protected String msg;
	protected String user ;
	protected Server s;
	private HashMap<String, String> map;
	
	public FTPRequest(Socket connexion, String msg,HashMap<String, String> map) {
		this.connexion = connexion;
		this.msg = msg;
		this.user = "";
		this.map = map;
		
	}
	
	/*
	 * recupere la requete
	 */
	public String receive(Socket connexion) throws IOException{
		String s = "";
		InputStream is = connexion.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		DataOutputStream out = new DataOutputStream(connexion.getOutputStream()); // TODO mettre seulement en 1ere connexion
		out.writeBytes("220 Service Ready for new user\n");
		s = br.readLine();
		return s;
	}
		
	public String response(String msg) throws IOException {
		String rep = "";
		String[] tmp = null;
		
		tmp = msg.split(" ",2);
		switch(tmp[0])
		{
			case "USER":
				rep = command_user(tmp[1]);
				break;
			case "QUIT":
				connexion.close();
				rep = "355 quit"; // TODO verifier paske la on coupe avant denvoyer le msg!
				break;
			default:
				rep = "111 error";
		}
		return rep;
	}

	public String command_user(String msg){
		String rep = "";
		System.out.println(msg);
		if(map.containsKey(msg))
		{
			rep = "331 User name okay, need password.\n";
			this.user = msg;
		}
		else
			rep = "332 Need account for login.\n";
		return rep;
	}
	
	public void run()
	{
		
	}
}
