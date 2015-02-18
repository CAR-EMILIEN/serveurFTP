package server;

import java.io.IOException;
import java.net.Socket;

public class Main {
	
	public static void main(String[] args) throws IOException {
		if  (args.length != 1) {
			System.out.println("usage: server path_server");
			System.exit(1);
		}
		Serveur serveur = new Serveur(4000);
		//Boolean active = true;
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
