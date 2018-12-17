/* une PROPOSITION de squelette, incomplète et adaptable... */

package hdfs;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

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
	private static int n = 3 ;
	
	//Adresse et noms des serveurs
	private static String hosts[] = {"localhost", "localhost", "localhost"};
	private static int ports[] = {8080, 8081, 8082};
	private static int nb_server = 3;
	
    private static void usage() {
        System.out.println("Usage: java HdfsClient read <file>");
        System.out.println("Usage: java HdfsClient write <line|kv> <file>");
        System.out.println("Usage: java HdfsClient delete <file>");
    }
	
    public static void HdfsDelete(String hdfsFname) {
    	
    	try {
    		
    		for (int id_server=0; id_server<nb_server; id_server++) {
    	
    			// Ouverture connexion au serveur numéro id_server
    			Socket s = new Socket(hosts[id_server], ports[id_server]);
			
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
    	
    	//faire un switch case selon le type ? et faire une méthode du reste du code
    	Format1 in = null;
    	switch (fmt){
    	case KV :
    		//Créer format selon le type fmt indiqué
    		in = new KVFormat(localFSSourceFname);
    		break;
    	case LINE :
    		//Créer format selon le type fmt indiqué
    		in = new LineFormat(localFSSourceFname);
    		break;
    	default :
    		System.out.println("Format non reconnu.");
    		return;
    	}
    	// Ouverture du fichier
    	in.open(Format.OpenMode.R);
    	// Lecture du fichier : lecture du premier kv
    	KV kv = in.read();
    	
  		// Créer le fragment = une liste de kv (type KV)
		ArrayList<KV> fragment = new ArrayList<KV>();
		
    	// Tant que la lecture est possible, cad qu'il y a quelque chose à lire alors construire les fragments
    	
		int id_server = 0; 
    	int i = 0;
		
    	while (kv.getV() != null) {
    		fragment.add(kv);

    		if (fragment.size() == n) {
    			sendFragment(fragment, fmt, localFSSourceFname, i, id_server, Commande.CMD_WRITE);

        		if (id_server <nb_server-1) { id_server++;}
        		else {id_server = 0;}
        		i++;
    		}

    		//Lecture des kv suivants
    		kv = in.read();
    	}
		if (fragment.size() > 0) {
			sendFragment(fragment, fmt, localFSSourceFname, i, id_server, Commande.CMD_WRITE);
		}
		
		//Fermeture du fichier une fois lecture finie
		in.close();
    }

    public static void sendFragment(ArrayList<KV> fragment, Format.Type fmt, String localFSSourceFnamenew, int i, int id_server, Commande command) {

		try {
			// Ouverture connexion au serveur numéro id_server
			Socket s = new Socket(hosts[id_server], ports[id_server]);
			
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
			oos.writeObject(i);
			oos.writeObject(fragment); //fragment est une liste donc object serializable donc pas besoin d'écrire un msg pour indiquer la fin du fragment

			// Fermeture connexion au serveur n° id_server
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
		try {
			int frag_num = 0;
			String reponse = "OK";
			String fmt = null;
			Format1 formatOut = null;
    	
			//Envoi au serveur
			Socket s = new Socket(hosts[frag_num%nb_server], ports[frag_num%nb_server]);

			ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
			
			oos.writeObject(Commande.CMD_READ);
			oos.writeObject(hdfsFname);
			oos.writeObject(frag_num);
			
			reponse = (String) ois.readObject();
			if (reponse.equals("OK")) {
				fmt = (String)ois.readObject();
				
				switch (fmt){
		    	case "KV" :
		    		formatOut = new KVFormat(hdfsFname+"new");
		    		break;
		    	case "LINE" :
		    		formatOut = new LineFormat(hdfsFname+"new");
		    		break;
		    	default :
		    		System.out.println("format non connu.");
				}
				formatOut.open(OpenMode.W);
				ArrayList<KV> fragment = (ArrayList<KV>) ois.readObject();
				for (KV elt : fragment) {
					formatOut.write(elt);
				}
			
			} else {
				System.out.println("Problème.");
				return;
			}
			s.close();
			ois.close();
			oos.close();
    	
			frag_num++;
				// Tant que le serveur trouve
			while (reponse.equals("OK")) {
				
				//Envoi au serveur
				s = new Socket(hosts[frag_num%nb_server], ports[frag_num%nb_server]);
				
				oos = new ObjectOutputStream(s.getOutputStream());
				ois = new ObjectInputStream(s.getInputStream());
				
				oos.writeObject(Commande.CMD_READ);
				oos.writeObject(hdfsFname);
				oos.writeObject(frag_num);
						
				reponse = (String) ois.readObject();
				
				
				if (reponse.equals("OK")) {
					// On récupère le fragment
					fmt = (String)ois.readObject();
					ArrayList<KV> fragment = (ArrayList<KV>) ois.readObject();

					//on reecrit le fragment
					for (KV elt : fragment) {
						formatOut.write(elt);
					}
				}
				
				ois.close();
				oos.close();
				//s.close();
				
				frag_num++;
			}
			formatOut.close();
			
			System.out.println("Fin de la transmission du fichier.");
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
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
                if (args.length<3) {usage(); return;}
                if (args[1].equals("line")) fmt = Format1.Type.LINE;
                else if(args[1].equals("kv")) fmt = Format1.Type.KV;
                else {usage(); return;}
                System.out.println(fmt);
                HdfsWrite(fmt,args[2],1);
                
            }	
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
