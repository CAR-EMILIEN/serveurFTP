package server;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
/**
 * 
 * 
 * 
 * @author rouse & allart
 *
 */
public class Main {
	
	public static void main(String[] args) throws IOException {
		if  (args.length != 1) {
			System.out.println("usage: server path_server");
			System.exit(1);
		}
		else {
			File f = new File(args[0]);
			if (!f.isDirectory())
			{
				System.out.println( args[0]);
				System.out.println("arg invalid");
				System.exit(1);
			}
		}
		Serveur serveur = new Serveur(4000);
		System.out.println("Demarrage du serveur");
		System.out.println("En attente de connexion\n");
		while (true) {
			Socket connexion = serveur.socket.accept();
			FtpRequest requete = new FtpRequest(connexion, serveur.map_user,args[0]);
			requete.start();
			System.out.println("nouveau client ");
		}
		
	}
}
