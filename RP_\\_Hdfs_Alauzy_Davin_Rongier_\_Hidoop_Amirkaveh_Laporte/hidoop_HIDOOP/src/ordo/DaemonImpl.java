package ordo;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.ArrayList;

import formats.Format;
import formats.Format.OpenMode;
import formats.KV;
import map.Mapper;

public class DaemonImpl extends UnicastRemoteObject implements Daemon {
	
	//Nom du serveur
	private String name;

	//Constructeur 
	public DaemonImpl(String name) throws RemoteException {
		this.name = name;
	}

	
	@Override
	public void runMap(Mapper m, Format reader, Format writer, CallBack cb) throws RemoteException {
		//Ouverture du reader et du writer
		reader.open(OpenMode.R);
		writer.open(OpenMode.W);
		
		//Appel de la fonction map
		m.map(reader, writer);
		
		//Fermeture du reader et du writer
		reader.close();
		writer.close();
		
		//Appel du callback à la fin de l'exécution
		cb.execMapFinished();
	}

	public static void main(String args[]) {
		int port = 4000;
		try {
			LocateRegistry.createRegistry(port);
			Daemon daemon = new DaemonImpl(args[0]);
			String URL = "//"+InetAddress.getLocalHost().getHostName()+":"+port+"/Daemon";
			Naming.rebind(URL, daemon);
		} catch (Exception exc) {};
	}

}
