package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * 
 * Classe gérant le serveur FTP.
 * 
 * @author Rousé & Allart
 */
public class Serveur {
	protected HashMap<String, String> map_user;
	protected ServerSocket socket;
	
	public Serveur(int port) throws IOException
	{	
		this.map_user = new HashMap<String,String>();
		this.load_map_user("Data/map_user");
		this.socket = new ServerSocket(port);
	}
	
	// pour les tests 
	public Serveur(int port,String filename) throws IOException
	{
		this.map_user = new HashMap<String,String>();
		this.load_map_user(filename);
		this.socket = new ServerSocket(port);

	}
	
	public static void main(String[] args) throws IOException {
		Serveur serveur = new Serveur(4000);
		//Boolean active = true;
		System.out.println("Demarrage du serveur");
		System.out.println("En attente de connexion\n");
		while (true) {
			Socket connexion = serveur.socket.accept();
			FtpRequest requete = new FtpRequest(connexion, serveur.map_user);
			requete.start();
			System.out.println("nouveau client ");
		}

	}
	/**
	 * read into the file containing authorized user and load them into a HashMap 
	 * 
	 * @param  filename the file to open and to load into the HashMap
	 * @return void 
	 * 
	 * @sideeffect fill map_user with users and their password
	 * 
	 * 		
	 * @throws IOException
	 */
	public void load_map_user(String filename) throws IOException
	{
		String s = "";
		String[] tab = null;
		FileReader f = new FileReader(filename);
		BufferedReader br = new BufferedReader(f);
		while((s = br.readLine())!=null)
		{
			//System.out.println(s);
			tab = s.split(" ");
			//System.out.println(tab[1]);
			this.map_user.put(tab[0], tab[1]);
		}
		br.close();
		return ;
	}

	public HashMap<String, String> getMap_user() {
		return map_user;
	}

	
	
	

}
