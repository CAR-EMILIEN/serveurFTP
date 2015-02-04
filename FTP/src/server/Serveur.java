package server;

import static util.Messages.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
		String message = new String();
		//Boolean active = true;
		while (true) {
			Socket connexion = serveur.socket.accept();
			InputStream is = connexion.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			DataOutputStream out = new DataOutputStream(connexion.getOutputStream()); // TODO mettre seulement en 1ere connexion
			out.writeBytes(SERVEUR_SERVICE_READY);
			while ((message = br.readLine()) != null) {
				FtpRequest requete = new FtpRequest(connexion,message,serveur.map_user);
				requete.processRequest(message);
				//active = requete.active;
			}
			connexion.close();
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
			System.out.println(s);
			tab = s.split(" ");
			System.out.println(tab[1]);
			this.map_user.put(tab[0], tab[1]);
		}
		br.close();
		return ;
	}

	public HashMap<String, String> getMap_user() {
		return map_user;
	}

	
	
	

}
