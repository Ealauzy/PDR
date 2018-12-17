package formats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

import formats.Format.Type;

public abstract class AbstractFormat implements Format1 {
	
	public enum TypeEtat {OpenRead, OpenWrite, Close};
	
	private String fname;
	private BufferedReader lecture;
	private FileWriter ecriture;
	private long index; //position courante dans le fichier (numero de ligne)
	private TypeEtat etat;
	
	public AbstractFormat(String fname) {
		this.fname = fname;
		this.index = 0;
		this.lecture = null;
		this.ecriture = null;
		this.etat = TypeEtat.Close;
	}
	
	public void open(OpenMode mode){
		
		//Est ce qu'on verifie l'etat du fichier pour autoriser ou non l'ecriture et la lecture
		
		File fichier = new File(this.fname);
		
		switch (mode) {
		case R : 
			try {
				this.lecture = new BufferedReader( new FileReader(fichier));
				this.etat = TypeEtat.OpenRead;
				System.out.println("Fichier ouvert en mode lecture.");
			} catch (FileNotFoundException e) {
				System.out.println("Fichier non trouvé");
			}
			break;
		case W :
			try {
				this.ecriture = new FileWriter(fichier);
				this.etat = TypeEtat.OpenWrite;
				System.out.println("Fichier ouvert en mode écriture.");
			} catch (IOException e) {
				System.out.println("Fichier impossible à ouvrir");
			}
			break;
			
		default :
			System.out.println("Veuillez préciser un mode d'ouverture.");
		}
	};
	
	
	public void close(){
		switch (etat) {
		case Close : 
			System.out.println("Fichier déjà fermé.");
			break;
		case OpenWrite :
			try {
				this.ecriture.close();
				this.etat = TypeEtat.Close;
			} catch (IOException e) {
				System.out.println("Fichier " + this.fname + "est fermé.");
			}
			break;
		case OpenRead :
			try {
				this.lecture.close();
				this.etat = TypeEtat.Close;
				this.index = 0;
			} catch (IOException e) {
				System.out.println("Fichier " + this.fname + "est fermé.");
			}
			break;
		}
		
		
	};
	
	public long getIndex(){
		return this.index;
	};
	
	public String getFname(){
		return this.fname;
	};
	
	public void setFname(String fname){
		this.fname = fname;
	};

	public String readFormatLigne() throws IOException {
		String ligne = this.lecture.readLine();
		index++;
		return ligne;
	}
	
	public void writeFormatLigne(String ligne) throws IOException {
		this.ecriture.write(ligne + "\n");
	}

}
