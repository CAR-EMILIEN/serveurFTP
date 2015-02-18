package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;

/**
 * 
 * Classe créant des instances
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
	

	/**
	 * Lit dans le fichier contenant les utilisateurs autorisé, et les
	 * chargent dans une HashMap 
	 * 
	 * @param  filename le fichier a charger dans la hashmap
	 * @return void 
	 * 
	 * @sideeffect rempli la map
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
