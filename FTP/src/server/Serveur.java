package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Serveur {
	protected HashMap<String, String> map_user;
	
	public Serveur() throws IOException
	{	
		this.map_user = new HashMap<String,String>();
		this.load_map_user();
	}
	
	public static void main(String[] args) {
		

	}
	/**
	 * read into the file containing authorized user and load them into a HashMap 
	 * 
	 * @param void
	 * @return void 
	 * 
	 * @sideeffect fill map_user with users and their password
	 * 
	 * 		
	 * @throws IOException
	 */
	public void load_map_user() throws IOException
	{
		String s = "";
		String[] tab = null;
		FileReader f = new FileReader("Data/map_user");
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
