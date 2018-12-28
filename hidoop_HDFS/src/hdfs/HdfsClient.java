package hdfs;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import config.Project;
import formats.Commande;
import formats.Format;
import formats.Format1;
import formats.KV;
import formats.KVFormat;
import formats.LineFormat;
import formats.Format.OpenMode;
import formats.Format.Type;

public class HdfsClient {

	//Taille d'un fragment (= nombre de kv max dans un fragment)
	private static int taille_frag = 3 ;
	
	//Adresse et noms des serveurs
	//private static String hosts[] = Project.HOSTS;
	//private static int ports[] = Project.PORTS;
	//private static int nb_servers = Project.NB_SERVERS;
	
	
    private static void usage() {
        System.out.println("Usage: java HdfsClient read <file>");
        System.out.println("Usage: java HdfsClient write <line|kv> <file>");
        System.out.println("Usage: java HdfsClient delete <file>");
    }
    
    private static Format1 CreerFormat (String fname, Format.Type fmt) throws FormatNonConformeException{
    	Format1 fichier = null;
    	
		//Créer format selon le type fmt indiqué
    	switch (fmt){
    	case KV :
    		fichier = new KVFormat(fname);
    		break;
    	case LINE :
    		fichier = new LineFormat(fname);
    		break;
    	default :
    		throw new FormatNonConformeException();
    	}
    	
    	return fichier;
    }
    
    
	
    public static void HdfsDelete(String hdfsFname) {
    	
    	try {
    		
    		for (int id_server=0; id_server<Project.NB_SERVERS; id_server++) {
    	
    			// Ouverture connexion au serveur numéro id_server
    			Socket s = new Socket(Project.HOSTS[id_server], Project.PORTS[id_server]);
			
    			ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
    			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());

    			//Ecriture sur le serveur
    			oos.writeObject(Commande.CMD_DELETE);
    			oos.writeObject(hdfsFname);
    			
        		ois.close();
    			oos.close();
    			s.close();
    		}
    		
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	
    }
	
    public static void HdfsWrite(Format.Type fmt, String localFSSourceFname, 
     int repFactor) {
    	
    	//Initialisation des variables utilisées
    	Format1 in = null;
    	int frag_num = 0;
		ArrayList<KV> fragment = new ArrayList<KV>();   // Créer le fragment = une liste de kv (type KV)
    	
    	//Construction du fichier au format souhaité
		try {
			in = CreerFormat(localFSSourceFname, fmt);
		} catch (FormatNonConformeException e) {
			e.printStackTrace();
			return;
		}
		
    	// Ouverture du fichier
    	in.open(Format.OpenMode.R);

    	// Lecture du fichier : lecture du premier kv
    	KV kv = in.read();

    	// Tant que la lecture est possible, cad qu'il y a quelque chose à lire 
		// alors construire les fragments et les envoyer
		
    	while (kv.getV() != null) {
    		fragment.add(kv);
    		
    		if (fragment.size() == taille_frag) {
    			sendFragment(fragment, fmt, localFSSourceFname, frag_num, Commande.CMD_WRITE);
        		frag_num++;
    		}

    		//Lecture des kv suivants
    		kv = in.read();
    	}
		if (fragment.size() > 0) {
			sendFragment(fragment, fmt, localFSSourceFname, frag_num, Commande.CMD_WRITE);
		}
		
		//Fermeture du fichier une fois lecture finie
		in.close();
    }

    
   
    public static void sendFragment(ArrayList<KV> fragment, Format.Type fmt, String localFSSourceFnamenew, int frag_num, Commande command) {

		try {
			// Ouverture connexion au serveur numéro id_server = frag_num%nb_servers
			Socket s = new Socket(Project.HOSTS[frag_num%Project.NB_SERVERS], Project.PORTS[frag_num%Project.NB_SERVERS]);
			
			ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());

			//Ecriture sur le serveur
			oos.writeObject(command);
			
			//oos.writeObject(fmt);
			String nomType;
			if (fmt == Type.KV) {
				nomType = "KV";
			} else { nomType = "LINE";}
			oos.writeObject(nomType);
			
			oos.writeObject(localFSSourceFnamenew);
			oos.writeObject(frag_num);
			oos.writeObject(fragment); 
			//fragment est une liste donc object serializable donc pas besoin d'écrire 
			// un msg pour indiquer la fin du fragment

			
			// Fermeture connexion au serveur
			ois.close();
			oos.close();
			s.close();
			
			//Vider fragment
			fragment.clear();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    
    
    
    
    public static void HdfsRead(String hdfsFname, String localFSDestFname) { 
    	System.out.println("reponse");
    	
		//initiliasation des variables utilisées
		int frag_num = 0;
		String reponse = "OK";
		String fmt = null;
		Format1 formatOut = null;

		try {
			// Tant que le serveur trouve
			while (reponse.equals("OK")) {

				//Envoi au serveur
				
				//Ouvertre de la connexion au serveur n° frag_num%nb_servers
				Socket s = new Socket(Project.HOSTS[frag_num%Project.NB_SERVERS], Project.PORTS[frag_num%Project.NB_SERVERS]);

				ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
				ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
			
				//Ecriture sur le serveur
				oos.writeObject(Commande.CMD_READ);
				oos.writeObject(hdfsFname);
				oos.writeObject(frag_num);
				
				//Lecture
				reponse = (String) ois.readObject();
				
				//Si c'est le premier fragment on vérifie que le premier serveur connait bien le fichier
				if (frag_num == 0) {
					if (!(reponse.equals("OK"))){
						System.out.println(reponse);
					}
				}
				
				//Si le serveur trouve le fragment
				if (reponse.equals("OK")) {
					
					//On récupère le format
					fmt = (String)ois.readObject();
					
					// Si c'est le premier fragment on crée le fichier du bon format et on l'ouvre
					if (frag_num == 0) {
						Format.Type type = null;
						if (fmt.equals("LINE")) type = Format1.Type.LINE;
		                else if(fmt.equals("KV")) type = Format1.Type.KV;
		                else throw new FormatNonConformeException();
						
						formatOut = CreerFormat(hdfsFname+"new", type);
						formatOut.open(OpenMode.W);
					}
					
					//On récupère une liste de KV qu'on écrit dans le fichier kv par kv
					ArrayList<KV> fragment = (ArrayList<KV>) ois.readObject();
					for (KV elt : fragment) {
						formatOut.write(elt);
					}
					
					//On passe au fragment suivant
					frag_num++;
				
				} else {
					//C'est la fin de la transmission
					System.out.println("Fin de la transmission du fichier.");
				}
				//Fermeture de la connexion
				s.close();
				ois.close();
				oos.close();
			}
		//on ferme le fichier
		formatOut.close();
			
		} catch (IOException | ClassNotFoundException | FormatNonConformeException e) {
			e.printStackTrace();
		}
    }
    
  

	
    public static void main(String[] args) {
        // java HdfsClient <read|write> <line|kv> <file>

        try {
            if (args.length<2) {usage(); return;}

            switch (args[0]) {
              case "read": HdfsRead(args[1],null); break;
              case "delete": HdfsDelete(args[1]); break;
              case "write": 
                Format1.Type fmt;
                if (args.length != 3) {usage(); return;}
                if (args[1].equals("line")) fmt = Format1.Type.LINE;
                else if(args[1].equals("kv")) fmt = Format1.Type.KV;
                else {usage(); return;}
                HdfsWrite(fmt,args[2],1);
                break;
              default : usage();
            }	
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
