package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Server {
	protected HashMap<String, String> map_user;
	
	public Server() throws IOException
	{	
		this.map_user = new HashMap<String,String>();
		this.load_map_user();
	}
	
	public static void main(String[] args) {
		

	}
	
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
		return ;
	}

	public HashMap<String, String> getMap_user() {
		return map_user;
	}

	
	
	

}
