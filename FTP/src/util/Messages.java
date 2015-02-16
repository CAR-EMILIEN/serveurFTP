package util;

/**
 * Classe contenant les messages renvoyés par le serveur
 */
public class Messages {

	/**
	 * Constructeur privé pour que la classe ne soit jamais instancié.
	 * On veut juste se servir des champs statiques.
	 */
	private Messages()
	{
		
	}
	
	public static final String SERVEUR_SERVICE_READY = "220 Service Ready for new user\n";
	
	public static final String USER_OK = "331 User name okay, need password.\n";
	public static final String USER_NEED_ACCOUNT = "332 Need account for login.\n";
	public static final String USER_INVALID = "430 Invalid username or password\n";
	
	public static final String PASS_OK = "230 User logged in, proceed. Logged out if appropriate.\n";
	public static final String NOT_LOGGED_IN = "530 Not logged in.\n";
	
	public static final String QUIT = "221\n";
	public static final String SUCCESS = "200 command okay\n";
    public static final String SYNTAX_ERROR = "520 \n";
     

	public static final String toto = "";		
			
			
			
			
}
