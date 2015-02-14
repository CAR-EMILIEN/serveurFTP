 package server;

import static util.Messages.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

public class FtpRequest extends Thread {
	
	protected Socket connexion;
	protected String msg;
	protected String user ;
	protected Serveur serveur;
	protected Boolean active;
	private HashMap<String, String> map;
	
	protected String client_dpt_addr;
	protected int client_dpt_port;
	
	
	
	
	public FtpRequest( Socket connexion, HashMap<String, String> map) {
		this.user = "";
		this.map = map;
		this.active = true;
		this.connexion = connexion;
	}	
	
	public FtpRequest( Socket connexion,String msg, HashMap<String, String> map) {
		super(connexion,map);
		this.msg = msg;
	}	
	
	
	public void processRequest(String msg) throws IOException {
		String rep = "";
		String[] tmp = null;
		
		System.out.println(msg);
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
			case "PASS":
				rep = processPASS(tmp[1]);
				break;
			case "SYST":
				rep = processSYST();
				break;
			case "PORT":
				rep = processPORT(tmp[1]);
				break;
			case "NLST":
				if (tmp.length>1)
					rep = processNLST(tmp[1]);
				else
					rep = processNLST("");
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
			rep = USER_INVALID;
		return rep;
	}
	
	public String processPASS(String msg)
	{
		String rep = "";
		if(map.get(this.user).equals(msg)){
			rep = PASS_OK;
		}
		else{
			rep = NOT_LOGGED_IN;
		}
		return rep;		
	}	
	
	
	public String processSYST()
	{
		return "UNIX Type: L8\n";
	}
	
	public String processPORT(String msg)
	{	
		String rep = "";
		String[] tmp = msg.split(",");
		if (tmp.length==6)
		{
			String addr = tmp[0]+"."+tmp[1]+"."+tmp[2]+"."+tmp[3];
			String port = tmp[4]+tmp[5];
			this.client_dpt_addr = addr;
			this.client_dpt_port=Integer.parseInt(port);
			rep = SUCCESS;
			System.out.println(this.client_dpt_addr);
			System.out.println(this.client_dpt_port);
		}
		else
			rep = SYNTAX_ERROR;
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
	public String processNLST(String msg)
	{
		String list = msg;
		String[] files = new File(".").list();
		for (int i = 0; i < files.length; i++)
			list += files[i] + " ;";
		list += "\n";
		return send_to_dtp(list);
	}
	public String processQUIT()
	{
		String rep = "";
		rep = QUIT;
		return rep;
	}
	
	public String send_to_dtp(String data) throws UnknownHostException, IOException
	{
		Socket s = new Socket(this.client_dpt_addr,this.client_dpt_port);
		DataOutputStream out = new DataOutputStream(s.getOutputStream()); 
		out.writeBytes(data);
		s.close();
		
		return "";
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
