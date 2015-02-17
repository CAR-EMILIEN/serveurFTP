Serveur Ftp
Emilie Allart & Julien Rousé
17/02/15


### 1/ Introduction:

Le logiciel vise à implémenter une portion des fonctionalitées d'un serveur Ftp en se basant sur
les spécifications du RFC 959 (https://www.ietf.org/rfc/rfc959.txt).


### 2/ Architecture:



### 3/ Parcours du code (code samples)


Voici la méthode qui prend un chemin et si ce chemin est un dossier(ou un fichier),
renvoi des informations sur ce dossier(fichier).


    public String infoFile(String path) throws IOException,InterruptedException {
	    String pathRep = "";
	    File dir = null;
	    if (path == "") {
		    pathRep = "";
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


Voici la méthode qui prend une String représentant des données (par exemple celles produite par la fonction précédente)
et les envoies au client_dtp par la connexion-data.

    public String send_to_dtp(String data) throws UnknownHostException,IOException {
		    this.client_socket = new Socket(this.client_dpt_addr,this.client_dpt_port);
		    DataOutputStream out2 = new DataOutputStream(this.client_socket.getOutputStream());
		    out2.writeBytes(data);
		    out2.close();
		    this.client_socket.close();
		    return SUCCESS;
	    }


Cette méthode est le coeur de la connexion entre le client et le serveur.
Le client tente une connexion et si elle est réussi, le serveur lui renvoie un message
signalant la réussite. On maintient ensuite la connexion jusqu'au message QUIT ou jusqu'a la
fermeture de la socket.

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

