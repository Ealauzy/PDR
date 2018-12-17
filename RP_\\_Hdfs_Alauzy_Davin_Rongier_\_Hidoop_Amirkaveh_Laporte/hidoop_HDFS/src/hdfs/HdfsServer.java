package hdfs;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import formats.Commande;
import formats.Format1;
import formats.KV;
import formats.KVFormat;
import formats.LineFormat;
import formats.Format.OpenMode;
import formats.Format.Type;

public class HdfsServer {
	
	private static int PORT = 808; //Numéro du Port à changer selon le serveur lancé
	private static ObjectOutputStream oos;
	private static ObjectInputStream ois;
	private static HashMap<String, HashMap<Integer,Format1>> files;
	public HdfsServer() {
		
	}
	
	private static void writeWithServer() throws ClassNotFoundException, IOException {
		
		
		//Récupérer le type envoyé, le nom du fichier et le fragment.
		String nomType = (String) ois.readObject();
		String name = (String) ois.readObject();
		int i = (int) ois.readObject();
		ArrayList<KV> fragment = (ArrayList<KV>) ois.readObject();
		
		//Créer fichier du type reçu
		Format1 formatOut = null;
	
		switch(nomType) {
		case "KV" :
			formatOut = new KVFormat(name + Integer.toString(i));
			break;
		case "LINE" :
			formatOut = new LineFormat(name + Integer.toString(i));
			break;
		}
		
		//Ecrire  le fragment récupéré dans le fichier créé
		formatOut.open(OpenMode.W);
		for (KV elt : fragment) {
			formatOut.write(elt);
		}

		//On stocke les informations sur les fichiers (Format1, nom et numéro du fragment)
		 if (!files.containsKey(name)){
			 files.put(name, new HashMap<Integer, Format1>());
		 }
		 files.get(name).put(i, formatOut);
		 
		formatOut.close();
		System.out.println("Fin Write");
		
	}
	
	private static void readWithServer() throws IOException, ClassNotFoundException {

		String name = (String) ois.readObject();
		Integer frag_num = (int)ois.readObject();
		
		if (files.get(name) == null) {
			System.out.println("Le fichier n'existe pas.");
			oos.writeObject("Probleme.");
		} else if (files.get(name).get(frag_num) == null) {
			System.out.println("Le fragment n'existe pas.");
			oos.writeObject("Probleme.");
		} else {

			Format1 in = files.get(name).get(frag_num);
			in.open(OpenMode.R);

			ArrayList<KV> fragment = new ArrayList <KV>();
			KV kv = in.read();

			while (kv.getV() != null) {
				fragment.add(kv);
				kv = in.read();
			}
			
			String fmt = "";
			switch (in.getType()){
	    	case KV :
	    		fmt = "KV";
	    		break;
	    	case LINE :
	    		fmt = "LINE";
	    		break;
	    	default :
			}
			
			oos.writeObject("OK");
			oos.writeObject(fmt);
			oos.writeObject(fragment);
			
			in.close();
			
			System.out.println("Fin Read");
			
		}
	}
	
	
	private static void deleteWithServer() {
		
		String name;
		try {
			name = (String)ois.readObject();
		if (files.containsKey(name)){
			//On supprime tous les fragments correspondant au fichier de la mémoire
			for(Map.Entry elt : files.get(name).entrySet()) {
				new File(name+Integer.toString((Integer)elt.getKey())).delete();
			}
			//On supprime tous les fragments du fichier de files
			 files.remove(name);
		 }
		else {
			System.out.println("Fichier inconnu.");
		}
		System.out.println("Fin Delete.");
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		try {
			files =  new HashMap<String, HashMap<Integer,Format1>>();
			//Ouverture connexion
			ServerSocket ss = new ServerSocket(PORT);
			
			while (true) {
				Socket s = ss.accept();
				ois = new ObjectInputStream(s.getInputStream());
				oos = new ObjectOutputStream(s.getOutputStream());
				
				Commande command = (Commande) ois.readObject();
				
				switch(command) {
				case CMD_WRITE : 
					writeWithServer();
					break;
				case CMD_READ :
					readWithServer();
					break;
				case CMD_DELETE :
					deleteWithServer();
					break;
				}
				
				//Fermeture connexion
				s.close();
				ois.close();
				oos.close();
			}
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
				e.printStackTrace();
		}
		
	}
}
