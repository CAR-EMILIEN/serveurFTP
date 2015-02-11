package server;

import static util.Messages.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;

public class FtpRequest extends Thread {
	
	protected Socket connexion;
	protected String msg;
	protected String user ;
	protected Serveur serveur;
	protected Boolean active;
	private HashMap<String, String> map;
	
	
	
	
	
	public FtpRequest( Socket connexion, HashMap<String, String> map) {
		this.user = "";
		this.map = map;
		this.active = true;
		this.connexion = connexion;
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
				rep = processQUIT(); 
				this.active = false;
				break;
			case "PASS" :
				rep = processPASS(tmp[1]);
				break;
			case "LIST" :
				rep = processLIST();
				break;
			default:
				rep = "111 error\n";
		}
		System.out.println(rep);
		DataOutputStream out = new DataOutputStream(connexion.getOutputStream()); 
		out.writeBytes(rep);
	}
	
	public String processUSER(String msg){
		String rep = "";
		//System.out.println(msg);
		if(map.containsKey(msg))
		{
			rep = USER_OK;
			this.user = msg;
		}
		else
			rep = USER_NEED_ACCOUNT ;
		return rep;
	}
	
	public String processPASS(String msg)
	{
		String rep = "";
		if(map.get(this.user).equals(msg)){
			rep = PASS_OK;
		}
		else{
			rep = PASS_ERROR;
		}
		return rep;		
	}	
	
	public String processRETR(String msg)
	{
		return "";
	}
	
	public String processSTOR(String msg)
	{
		return "";
	}
	
	//TODO gérer deuxieme socket
	public String processLIST()
	{
		String list = "";
		String[] files = new File(".").list();
		for (int i = 0; i < files.length; i++)
			list += files[i] + " ;";
		list += "\n";
		return list;
	}
	public String processQUIT()
	{
		String rep = "";
		rep = QUIT;
		return rep;
	}
	public void run()
	{
		String message = new String();
		try { //TODO la gestion des exceptions
			InputStream is = connexion.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			DataOutputStream out = new DataOutputStream(connexion.getOutputStream()); 
			out.writeBytes(SERVEUR_SERVICE_READY);
			while ((message = br.readLine()) != null) {
				this.processRequest(message);
				//active = requete.active;
			}
			connexion.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
