package server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

public class FtpRequest extends Thread {
	
	protected Socket connexion;
	protected String msg;
	protected String user ;
	protected Serveur s;
	protected Boolean active;
	private HashMap<String, String> map;
	
	public FtpRequest(Socket connexion, String msg, HashMap<String, String> map) {
		this.connexion = connexion;
		this.msg = msg;
		this.user = "";
		this.map = map;
		this.active = true;
		
	}	
	
	public void processRequest(String msg) throws IOException {
		String rep = "";
		String[] tmp = null;
		
		tmp = msg.split(" ",2);
		switch(tmp[0])
		{
			case "USER":
				rep = processUSER(tmp[1]);
				break;
			case "QUIT":
				rep = processQUIT(); // TODO verifier paske la on coupe avant denvoyer le msg!
				this.active = false;
				break;
			default:
				rep = "111 error";
		}
		DataOutputStream out = new DataOutputStream(connexion.getOutputStream()); 
		out.writeBytes(rep);
	}

	public String processUSER(String msg){
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
	
	public String processPASS(String msg)
	{
		return "";		
	}	
	
	public String processRETR(String msg)
	{
		return "";
	}
	
	public String processSTOR(String msg)
	{
		return "";
	}
	public String processLIST(String msg)
	{
		return "";
	}
	public String processQUIT()
	{
		String rep = "";
		rep = "355 quit";
		return rep;
	}
	public void run()
	{
		
	}
}
