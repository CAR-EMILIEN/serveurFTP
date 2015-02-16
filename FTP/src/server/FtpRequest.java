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


/**
*
* La classe qui  gére l'échange avec un client FTP
*
*/

public class FtpRequest extends Thread {
	
	protected Socket connexion;
	protected String msg;
	protected String user ;
	protected Serveur serveur;
	protected Boolean active;
	private HashMap<String, String> map;
	
	protected String client_dpt_addr;
	protected int client_dpt_port;
	protected Socket client_socket = null;
	protected boolean client_active = false;
	
	protected String current_dir;
	private String client_files;
	//protected Socket socket_tmp;
	
	/**
	*
	* Le constructeur pour FtpRequest
	*
	*
	* @param connexion la Socket par lequelle on communique les commandes avec le client Ftp
	* @param map Une HashMap contenant des paires username:password des utilisateurs connus du serveur Ftp
	*
	* @return 
	*/ 	

	public FtpRequest( Socket connexion, HashMap<String, String> map) {
		this.user = "";
		this.map = map;
		this.active = true;
		this.connexion = connexion;
		this.current_dir = "Data/FTP_ressources";
	}	
	
	/**
	*
	* Le constructeur pour FtpRequest 
	*
	*
	* @param connexion la Socket par lequelle on communique les commandes avec le client Ftp
	* @param msg Un message conforme au protocole rfc 959 décrivant le protocole Ftp
	* @param map Une HashMap contenant des paires username:password des utilisateurs connus du serveur Ftp
	*
	* @return 
	*
	* 	
	*/
	public FtpRequest( Socket connexion,String msg, HashMap<String, String> map) {
		this(connexion,map);
		this.msg = msg;
	}	
	
	
	/**
	*	
	* Méthode centrale de traitement des messages Ftp.
	* Elle reçoit des commandes de la part du user-pi et lui renvoi les messages correspondants.
	* 
	* @param msg Un message conforme au protocole rfc 959 décrivant le protocole Ftp et qui va être traité
	* 
	* @return void Ne retourne pas de valeur, mais écrit sur le Stream du client Ftp connecté. 
	* 
	*/	 
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
			case "LIST":
				String arg = ".";
				if (tmp.length>1)
					arg = tmp[1];
				rep = processLIST(arg);
				break;
			default:
				rep = "111 error\n";
		}
		System.out.println(rep);
		DataOutputStream out = new DataOutputStream(connexion.getOutputStream()); 
		out.writeBytes(rep);
		if (client_active) {
			//String file = this.infoFile();
			send_to_dtp(this.client_files);
			out.writeBytes("200\r\n");
			this.client_active = false;
			this.client_socket = null;
		}
	}
	
	/**
	* Méthode pour traiter la commande user.
	* Si l'utilisateur fais partie des utilisateurs autorisés,
	* alors cette méthode renvoi le code 331 pour signaler qu'il doit maintenant
	* rentrer son mot de passe. Si en revanche il ne fait pas partie des
	* utilisateurs autorisés on renvoi un  code 430
	* 
	* @param msg Un message conforme au protocole rfc 959 décrivant le protocole Ftp a traiter.
	*
	* @return le code 331 en cas de succés pour indiquer a l'utilisateur de donner son mot de passe
        *         sinon le code 430 indiquant un nom d'utilisateur incorrect 
	*/
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
	

	/**
	* Méthode pour traiter la commande pass
	* Cette commande suit la commande user, et demande a l'utilisateur si il est autorisé 
	* à s'identifier avec son mot de passe pour lui permettre d'accéder aux fonctions qui ne sont pas 
	* read-only (par exemple store et retrieve)
	*
	*
	* @param msg Un message conforme au protocole rfc 959 décrivant le protocole Ftp a traiter. 
	* 
	* @return le code 230 signifiant que l'utilisateur est bien identifié,
	*         sinon 430 indiquant que le mot de passe etait erroné
	*/
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
	
	
	/**
	* Méthode pour traiter la commande syst.	
	* En pratique on renvoi toujours "UNIX Type: L8\n" puisque
	* on lancera toujours ce programme depuis un OS unix.
	*
	* @param void
	*
	* @return "UNIX Type: L8 \n"
	* 
	*/
	public String processSYST()
	{
		return "UNIX Type: L8\n";
	}
	
	public String processPORT(String msg) throws UnknownHostException, IOException
	/**
	* Méthode pour traiter la commande port
	* Cette commande sert au client-Ftp à spécifier une adresse ip et un 
	* port pour que le serveur ouvre une connexion avec lui pour des échanges de données.
	*	
	* @param msg Un message conforme au protocole rfc 959 décrivant le protocole Ftp
	*
	* @return 200 pour succès,
	*         sinon 520 pour les fautes de syntaxe dans la commande
	*
	*
	*/
	{	
		String rep = "";
		String[] tmp = msg.split(",");
		if (tmp.length==6)
		{
			String addr = tmp[0]+"."+tmp[1]+"."+tmp[2]+"."+tmp[3];
			int port1 = Integer.parseInt(tmp[4]);
			int port2 = Integer.parseInt(tmp[5]);
			String port1s = Integer.toHexString(port1);
			String port2s = Integer.toHexString(port2);
			
			this.client_dpt_addr = addr;
			this.client_dpt_port=Integer.parseInt(port1s+port2s,16);
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

	public String infoFile() {
		String current_dir = this.current_dir +"/";
		String list = "";
		String[] files = new File(current_dir).list();
		for (int i = 0; i < files.length; i++)
			list += files[i] + " ;";
		return list;
	}
	
	/**
	*
	* Méthode pour traiter la commande list
	* 
	* @param msg Un message conforme au protocole rfc 959 décrivant le protocole Ftp a traiter. 
	*
	* @return 
	*
	*/
	
	public String processLIST(String msg) throws UnknownHostException, IOException
	{

		//return send_to_dtp(list);
		this.client_active = true;
		this.client_files = infoFile();
		return "150 About to read directory content!\r\n";
		
	}

	/**
	*
	* Méthode  pour traiter la commande quit
	* 
	* @param void
	*
	* @return le code 221
	*	
	*/
	public String processQUIT()
	{
		String rep = "";
		rep = QUIT;
		return rep;
	}
	
	public String send_to_dtp(String data) throws UnknownHostException, IOException
	{
		System.out.println(this.client_dpt_addr + " : " + this.client_dpt_port);
		/*this.socket_tmp = new Socket(this.client_dpt_addr,this.client_dpt_port);
		System.out.println(this.socket_tmp.getPort());
		DataOutputStream out2 = new DataOutputStream(this.socket_tmp.getOutputStream()); 
		out2.writeBytes(data);
		System.out.println(
				data);
		socket_tmp.close();
		*/
		this.client_socket = new Socket(this.client_dpt_addr,this.client_dpt_port);
		System.out.println(this.client_socket.getPort());
		DataOutputStream out2 = new DataOutputStream(this.client_socket.getOutputStream()); 
		out2.writeBytes(data);
		System.out.println(data);
		out2.close();
		this.client_socket.close();
		return "200 \r\n";
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
				if (!this.active)
					break;
			}
			System.out.println("Fermeture");
			connexion.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
