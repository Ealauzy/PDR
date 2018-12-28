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
import formats.Format;
import formats.Format1;
import formats.KV;
import formats.KVFormat;
import formats.LineFormat;
import formats.Format.OpenMode;
import formats.Format.Type;

public class HdfsServer {
	
	private static int PORT = 8082; //Numéro du Port à changer selon le serveur lancé
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
		 
		//On ferme le fichier
		formatOut.close();
		System.out.println("Fin Write.");
		
	}
	
	
	
	private static void readWithServer() throws IOException, ClassNotFoundException, FormatNonConformeException {

		//On récupère le nom du fichier et le numéro du fragment
		String name = (String) ois.readObject();
		Integer frag_num = (int) ois.readObject();
		
		//Si le nom du fichier ou le numéro de fragment ne correspond pas à ceux mémorisés
		//On le signale
		if (files.get(name) == null) {
			oos.writeObject("Le fichier n'existe pas.");
		} else if (files.get(name).get(frag_num) == null) {
			oos.writeObject("Le fragment n'existe pas.");
		} else {

			//Sinon on récupère le fragment et on
			Format1 in = files.get(name).get(frag_num);
			in.open(OpenMode.R);

			//On crée une liste de KV à envoyer au Client (ArrayList est Serializable)
			ArrayList<KV> fragment = new ArrayList <KV>();
			KV kv = in.read();

			while (kv.getV() != null) {
				fragment.add(kv);
				kv = in.read();
			}
			
			//On récupère le format du fichier sous la forme d'un string qui pourra être envoyé
			//(car Format.Type n'est pas serializable
			String fmt = "";
			if (in.getType()==Format.Type.LINE) fmt = "LINE";
            else if(in.getType()==Format.Type.KV) fmt = "KV";
            else throw new FormatNonConformeException();
			
			//On envoie "OK" suivit du format et du fragment
			oos.writeObject("OK");
			oos.writeObject(fmt);
			oos.writeObject(fragment);
			
			//On ferme le fichier créé
			in.close();
			
			System.out.println("Fin Read.");
			
		}
	}
	
	
	private static void deleteWithServer() {
		
		//On récupère le nom du fihcier à supprimer
		String name;
		try {
			name = (String)ois.readObject();
		
		//Si on a des fragments du fichier en mémoire
		if (files.containsKey(name)){
			//On supprime tous les fragments correspondant au fichier de la mémoire
			for(Map.Entry elt : files.get(name).entrySet()) {
				new File(name+Integer.toString((Integer)elt.getKey())).delete();
			}
			//On supprime tous les fragments du fichier de files
			 files.remove(name);
		 }
		else {
			//Sinon on indique qu'on ne connait pas le fichier
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
			
		} catch (IOException | ClassNotFoundException | FormatNonConformeException e) {
			e.printStackTrace();
		}
		
	}
}
