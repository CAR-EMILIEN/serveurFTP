package server;

import java.io.IOException;
import java.net.Socket;

public class Main {
	
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
}
