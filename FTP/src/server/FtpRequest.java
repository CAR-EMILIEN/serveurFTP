package server;

import static util.Messages.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

/**
 * 
 * La classe qui gére l'échange avec un client FTP
 * 
 */

public class FtpRequest extends Thread {

	protected Socket connexion;
	protected String msg;
	protected String user;
	protected Serveur serveur;
	protected Boolean active;
	private HashMap<String, String> map;
	
	
	protected String client_dpt_addr;
	protected int client_dpt_port;
	protected Socket client_socket = null;
	protected boolean client_active = false;

	/* La racine des documents du serveur (passé en paramètre au lancement du serveur)*/
	protected String root;
	/* Le dossier dans lequel l'utilisateur se trouve actuellement.
	 * Ce dossier est forcement un dossier fils de root.
	 */
	protected String current_dir;
	
	
	private String client_files;
	private boolean client_retrieving = false;
	private boolean client_storing = false;
	private final int BLOC_SIZE = 1024;


	/**
	 * 
	 * Le constructeur pour FtpRequest
	 * 
	 * 
	 * @param connexion
	 *            la Socket par lequelle on communique les commandes avec le
	 *            client Ftp
	 * @param map
	 *            Une HashMap contenant des paires username:password des
	 *            utilisateurs connus du serveur Ftp
	 * @param path
	 *             Le chemin vers le dossier qui sera la racine du serveur
	 * 
	 * @return
	 */

	public FtpRequest(Socket connexion, HashMap<String, String> map, String path) {
		this.user = "";
		this.map = map;
		this.active = true;
		this.connexion = connexion;
		this.current_dir = path;
		this.root = path;
	}	/**
	 * 
	 * Le constructeur pour FtpRequest (surtout utilisé pour les tests
	 * 
	 * 
	 * @param connexion
	 *            la Socket par lequelle on communique les commandes avec le
	 *            client Ftp
	 * @param msg
	 *            Un message conforme au protocole rfc 959 décrivant le
	 *            protocole Ftp
	 * @param map
	 *            Une HashMap contenant des paires username:password des
	 *            utilisateurs connus du serveur Ftp
	 * 
	 * @return
	 * 
	 * 
	 */
	public FtpRequest( Socket connexion,String msg, HashMap<String, String> map,String path) {
		this(connexion,map,path);
		this.msg = msg;
	}

	/**
	 * 
	 * Méthode centrale de traitement des messages Ftp. Elle reçoit des
	 * commandes de la part du user-pi et lui renvoi les messages
	 * correspondants.
	 * 
	 * @param msg
	 *            Un message conforme au protocole rfc 959 décrivant le
	 *            protocole Ftp et qui va être traité
	 * 
	 * @return void Ne retourne pas de valeur, mais écrit sur le Stream du
	 *         client Ftp connecté.
	 * @throws InterruptedException
	 * 
	 */
	public void processRequest(String msg) throws IOException, InterruptedException {
		String rep = "";
		String[] tmp = null;

		
		tmp = msg.split(" ",2);
		String arg = "";
		if (tmp.length>1)
			arg = tmp[1];
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
				rep = processLIST(arg);
				this.client_active = true;
				break;
			case "PWD":
				rep = processPWD();
				break;
			case "CWD":
				rep = processCWD(arg);
				break;
			case "STORE":
				rep = processSTOR(tmp[1]);
				break;
			case "RETR":
				rep = processRETR(tmp[1]);
				if (!rep.equals("451 The file cannot be found on the server\r\n"))
					this.client_retrieving = true;
				break;
			default:
				rep = NOT_IMPLEMENTED;
		}
		DataOutputStream out = new DataOutputStream(connexion.getOutputStream()); 

		out.writeBytes(rep);
		
		if (client_active) {
				send_to_dtp(this.client_files);
				out.writeBytes(SUCCESS);
				this.client_active = false;
				this.client_socket = null;
		}
		if (client_storing) {
			rep = send_file(tmp[1]);
			out.writeBytes(rep);
			this.client_storing = false;
			this.client_socket = null;
		}
		if (client_retrieving) {
			rep = retrieve_file(tmp[1]);
			this.client_retrieving = false;
			this.client_socket = null;
			out.writeBytes(rep);
		}
	}

	/**
	 * Méthode pour traiter la commande user. Si l'utilisateur fais partie des
	 * utilisateurs autorisés, alors cette méthode renvoi le code 331 pour
	 * signaler qu'il doit maintenant rentrer son mot de passe. Si en revanche
	 * il ne fait pas partie des utilisateurs autorisés on renvoi un code 430
	 * 
	 * @param msg
	 *            Un message conforme au protocole rfc 959 décrivant le
	 *            protocole Ftp a traiter.
	 * 
	 * @return le code 331 en cas de succés pour indiquer a l'utilisateur de
	 *         donner son mot de passe sinon le code 430 indiquant un nom
	 *         d'utilisateur incorrect
	 */
	public String processUSER(String msg) {
		String rep = "";
		// System.out.println(msg);
		if (map.containsKey(msg)) {
			rep = USER_OK;
			this.user = msg;
		} else
			rep = USER_INVALID;
		return rep;
	}

	/**
	 * Méthode pour traiter la commande pass Cette commande suit la commande
	 * user, et demande a l'utilisateur si il est autorisé à s'identifier avec
	 * son mot de passe pour lui permettre d'accéder aux fonctions qui ne sont
	 * pas read-only (par exemplehis.client_files = infoFile(msg) store et retrieve)
	 * 
	 * 
	 * @param msg
	 *            Un message conforme au protocole rfc 959 décrivant le
	 *            protocole Ftp a traiter.
	 * 
	 * @return le code 230 signifiant que l'utilisateur est bien identifié,
	 *         sinon 430 indiquant que le mot de passe etait erroné
	 */
	public String processPASS(String msg) {
		String rep = "";
		if (map.get(this.user).equals(msg)) {
			rep = PASS_OK;
		} else {
			rep = NOT_LOGGED_IN;
		}
		return rep;
	}

	/**
	 * Méthode pour traiter la commande syst. En pratique on renvoi toujours
	 * "UNIX Type: L8\n" puisque on lancera toujours ce programme depuis un OS
	 * unix.
	 * 
	 * @param void
	 * 
	 * @return "UNIX Type: L8 \n"
	 * 
	 */
	public String processSYST() {
		return "UNIX Type: L8\r\n";
	}

	/**
	 * Méthode pour traiter la commande port Cette commande sert au client-Ftp à
	 * spécifier une adresse ip et un port pour que le serveur ouvre une
	 * connexion avec lui pour des échanges de données.
	 * 
	 * @param msg
	 *            Un message conforme au protocole rfc 959 décrivant le
	 *            protocole Ftp
	 * 
	 * @return 200 pour succès, sinon 520 pour les fautes de syntaxe dans la
	 *         commande
	 * 
	 * 
	 */
	public String processPORT(String msg) throws UnknownHostException,
			IOException

	{

		String rep = "";
		String[] tmp = msg.split(",");
		if (tmp.length == 6) {
			String addr = tmp[0] + "." + tmp[1] + "." + tmp[2] + "." + tmp[3];
			int port1 = Integer.parseInt(tmp[4]);
			int port2 = Integer.parseInt(tmp[5]);
			String port1s = Integer.toHexString(port1);
			String port2s = Integer.toHexString(port2);

			this.client_dpt_addr = addr;
			this.client_dpt_port = Integer.parseInt(port1s + port2s, 16);
			rep = SUCCESS;
			System.out.println(this.client_dpt_addr);
			System.out.println(this.client_dpt_port);
		} else
			rep = SYNTAX_ERROR;
		return rep;
	}

	public String processRETR(String file) {
		File fileToR = new File(this.current_dir+ "/" + file);
		if (!fileToR.exists()) 
			return "451 The file cannot be found on the server\r\n";
		return "150 Going to send the file\r\n";
	}
	
	public String retrieve_file(String file) throws UnknownHostException, IOException{
		File fileToR = new File(this.current_dir+ "/" + file);
		this.client_socket = new Socket(this.client_dpt_addr,this.client_dpt_port);
		FileInputStream fis = new FileInputStream(fileToR);
		DataOutputStream cos = new DataOutputStream(this.client_socket.getOutputStream());
		byte[] buffer = new byte[BLOC_SIZE];
		int read = 0;
		while ((read = fis.read(buffer)) > 0) {
			cos.write(buffer, 0, read);
		}
		cos.close();
		fis.close();
		this.client_socket.close();
		return RETRIEVE_OK;

	}
	
	/**
	 * 
	 * Méthode pour traiter la commande stor
	 * 
	 * @param msg
	 *            Un message conforme au protocole rfc 959 décrivant le
	 *            protocole Ftp a traiter.
	 * 
	 * @return
	 * @throws 
	 * 
	 */
	public String processSTOR(String file) {
		File f = new File(this.current_dir+"/"+file);
		if (!f.isFile())
			return ABORTED_LOCAL_ERROR;
		client_storing = true;
		return STORING_READY;
	}


	 /** 
	 * Donne des informations sur un repertoire/fichier donné.
	 * (utilise ls)
	 * 
	 * @param path le chemin du repertoire/fichier dont on veut des informations
	 *  
	 * @return les informations obtenues sur le fichier/dossier si il existe
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public String infoFile(String path) throws IOException,InterruptedException {
		String pathRep = "";
		File dir = null;
		if (path == "") {
			pathRep = this.current_dir;
			dir = new File(this.current_dir);
		} else {
			pathRep = this.current_dir + "/" + path;
			dir = new File(pathRep);
		}
		if (!dir.isFile() && !dir.isDirectory())
			return ABORTED_LOCAL_ERROR; 
		String cmd = "ls -l " + pathRep;
		String content = "";
		Process p = Runtime.getRuntime().exec(cmd);
		p.waitFor();
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = reader.readLine();
		while (line != null) {
			System.out.println(line);
			content += line + "\r\n";
			line = reader.readLine();
		}
		return content;
	}

	/**
	 * 
	 * Méthode pour traiter la commande list
	 * 
	 * @param msg
	 *            Un message conforme au protocole rfc 959 décrivant le
	 *            protocole Ftp a traiter.
	 * 
	 * @return
	 * @throws InterruptedException
	 * 
	 */

	public String processLIST(String msg) throws UnknownHostException,IOException, InterruptedException {
		this.client_files = infoFile(msg);
		if (this.client_files.equals(ABORTED_LOCAL_ERROR))
			return ABORTED_LOCAL_ERROR;
		this.client_active = true;
		return READING_CONTENT;
	}

	/**
	 * 
	 * Méthode pour traiter la commande pwd
	 * 
	 * @param
	 * 
	 * @return
	 * 
	 */

	public String processPWD() {
		return "257 " + this.current_dir + "\r\n";
	}
	
	/**
	 * Méthode pour traiter la commande CWD
	 * 
	 * @param path Le chemin vers lequel on veut se déplacer
	 * 
	 * @return Le message qu'on renvoi au client Ftp
	 */
	public String processCWD(String path) {
		String rep = "333 TOTO";
		
		File f = new File(this.current_dir,path);
		
		if (path.equals("") || path.equals("."))
		{
			rep = FILE_OK+ " directory is still" +this.current_dir + "\n";
		}
		else if (path.equals(".."))
		{
			//on regarde qu'on n'est pas à la racine
			if (this.current_dir.equals(this.root))
			{
				rep = FILE_OK + " cant go behind root_directory. directory is " + this.current_dir + "\n";
			}
			//et on remonte dans le parent
			else
			{
				File tmp  = new File(this.current_dir);
				this.current_dir = tmp.getParent();
				rep = FILE_OK +" directory is now " + this.current_dir + "\n";
			}
		}
		else 
		{
			if (f.isDirectory())
			{
				this.current_dir = f.getPath();
				rep = FILE_OK + this.current_dir + "\n";
			}
			else
			{
				rep = ABORTED_LOCAL_ERROR;
			}
		}
		
		return rep;
	}

	/**
	 * 
	 * Méthode pour traiter la commande quit
	 * 
	 * @param void
	 * 
	 * @return le code 221
	 * 
	 */
	public String processQUIT() {
		String rep = "";
		rep = QUIT;
		return rep;
	}
	
	/**
	* Méthode pour envoyer des données à l'adresse et au port spécifié par le client par la
	* connexion qui sert à transférer des données.
	* 
	* @param data Les données a envoyer au client_dtp
	* 
	* @throws UnknownHostException
	* @throws IOException 
	*
	*/
	public String send_to_dtp(String data) throws UnknownHostException,IOException {
		this.client_socket = new Socket(this.client_dpt_addr,this.client_dpt_port);
		DataOutputStream out2 = new DataOutputStream(this.client_socket.getOutputStream());
		out2.writeBytes(data);
		out2.close();
		this.client_socket.close();
		return SUCCESS;
	}
	
	/**
	* Méthodes pour envoyer un fichier à l'adresse et au port spécifié par le client par la
	* connexion qui sert à transférer des données.
	* 
	* @param pathfile chemin vers un fichier/dossier
	* 
	* @throws UnknownHostException
	* @throws IOException 
	*
	*/
	public String send_file(String pathfile) throws UnknownHostException, IOException {
		File f = new File(this.current_dir+"/"+pathfile);
		this.client_socket = new Socket(this.client_dpt_addr, this.client_dpt_port);
		byte[] buffer = new byte[BLOC_SIZE];
		DataInputStream dis = new DataInputStream(this.client_socket.getInputStream());
		FileOutputStream fos = new FileOutputStream(f);
		int read = 0;
		while ((read = dis.read(buffer)) > 0) {
			fos.write(buffer, 0, read);
		}
		fos.close();
		dis.close();
		this.client_socket.close();
		return STORE_OK;
	}
	
	/**
	* Méthode appelé par Thread.start().
	*
	*/
	public void run() {
		String message = new String();

		try { 
			InputStream is = connexion.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			DataOutputStream out = new DataOutputStream(
					connexion.getOutputStream());
			out.writeBytes(SERVEUR_SERVICE_READY);
			while ((message = br.readLine()) != null) {
				this.processRequest(message);
				if (!this.active)
					break;
			}
			System.out.println("Fermeture");
			connexion.close();
		} catch (IOException e) {
			throw new RuntimeException(e);

		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
