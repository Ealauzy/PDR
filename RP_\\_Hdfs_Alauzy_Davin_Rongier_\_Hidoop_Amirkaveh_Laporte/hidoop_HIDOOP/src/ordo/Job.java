package ordo;

import formats.Format.*;
import formats.KVFormat;
import formats.LineFormat;
import hdfs.HdfsClient;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.CyclicBarrier;

import formats.Format;
import map.MapReduce;

public class Job implements JobInterfaceX {

	//Format d'entrée
	private Format.Type inputFormat;
	//Nom fichier d'entrée
	private String inputFname;
	//Nombre de reduce
	private int nbReduce;
	//Nombre de map
	private int nbMap;
	//Format de sortie
	private Format.Type outputFormat;
	//Nom fichier de sortie
	private String outputFname;
	
	//Constructeur
	public Job() {
		this.inputFormat = Format.Type.LINE;
		this.inputFname = "data/testfile.txt";
		this.nbReduce = 1; //On choisit comme valeur par défaut 1 reduce
		this.nbMap = 3; //On choisit comme valeur par défaut 3 maps
		this.outputFormat = Format.Type.KV;
		this.outputFname = "data/testfile-res.txt";
	}	

	@Override
	public void startJob(MapReduce mr) {
		//Création du reader et du writer
		Format reader = new LineFormat(this.inputFname);
		Format writer = new KVFormat(this.outputFname+"tmp");
		
		//Création du cycleBarrier nécessaire pour la synchro des maps
		CyclicBarrier cycle = new CyclicBarrier(this.nbMap);
		
		//Lancement de map sur les serveurs
		for(int i = 0 ; i < this.nbMap ; i++) {
			try {
				//Récupération du daemon
				Daemon daemon = (Daemon) Naming.lookup("//localhost:4000/Daemon");
				//Création du callback
				CallBack cb = new CallBackImpl(cycle);
				//Lancement de map
				daemon.runMap(mr, reader, writer, cb);
			} catch (MalformedURLException | RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
		}
		
		//Lecture des résultats
		HdfsClient.HdfsRead(outputFname+"tmp", "ResMap");
		
		//Création du reader et du writer
		Format readerl = new KVFormat("ResMap");
		Format writerl = new KVFormat(this.outputFname);
		
		//Ouverture du reader et du writer
		readerl.open(OpenMode.R);
		writerl.open(OpenMode.W);
		
		//Lancement de reduce
		mr.reduce(readerl, writerl);
		
		//Fermeture du reader et du writer
		readerl.close();
		writerl.close();
	}

	
	// Setters et Getters
	
	@Override
	public void setInputFormat(Type ft) {
		if (ft == null) {
			throw new IllegalArgumentException();
		} else {
			this.inputFormat = ft;
		}
	}

	@Override
	public void setInputFname(String fname) {
		if (fname == null) {
			throw new IllegalArgumentException();
		} else {
			this.inputFname = fname;
		}
	}

	@Override
	public void setNumberOfReduces(int tasks) {
		if (tasks < 0) {
			throw new IllegalArgumentException();
		} else {
			this.nbReduce = tasks;
		}
	}

	@Override
	public void setNumberOfMaps(int tasks) {
		if (tasks < 0) {
			throw new IllegalArgumentException();
		} else {
			this.nbMap = tasks;
		}
	}

	@Override
	public void setOutputFormat(Type ft) {
		if (ft == null) {
			throw new IllegalArgumentException();
		} else {
			this.outputFormat = ft;
		}
	}

	@Override
	public void setOutputFname(String fname) {
		if (fname == null) {
			throw new IllegalArgumentException();
		} else {
			this.outputFname = fname;
		}
	}
	
	@Override
	public int getNumberOfReduces() {
		return this.nbReduce;
	}

	@Override
	public int getNumberOfMaps() {
		return this.nbMap;
	}

	@Override
	public Type getInputFormat() {
		return this.inputFormat;
	}

	@Override
	public Type getOutputFormat() {
		return this.outputFormat;
	}

	@Override
	public String getInputFname() {
		return this.inputFname;
	}

	@Override
	public String getOutputFname() {
		return this.outputFname;
	}

}
