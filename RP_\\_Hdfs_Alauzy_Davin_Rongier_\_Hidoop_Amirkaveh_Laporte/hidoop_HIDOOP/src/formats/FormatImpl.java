package formats;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import config.Project;

public abstract class FormatImpl implements Format {
	
	//Mode d'ouverture
	private OpenMode mode;
	//Indice du "curseur" de lecture
	private int index;
	//Nom du fichier
	private String fName;
	//Buffer de lecture
	protected BufferedReader buffR;
	//Buffer d'écriture
	private BufferedWriter buffW;
	//FileReader
	private FileReader FileR;
	//FileWriter
	protected FileWriter FileW;
	

	//Constructeur
	public FormatImpl(String name) {
		this.index = 0;
		this.mode = null;
		this.fName = name;
	}

	
	@Override
	public abstract KV read();

	@Override
	public abstract void write(KV record);

	@Override
	public void open(OpenMode mode) {
		
		this.mode = mode;
		File file = new File(Project.PATH + this.fName);
		//Ouverture du fichier en mode lecture
		if (mode == Format.OpenMode.R) {			
			try {
				this.buffR = new BufferedReader(this.FileR = new FileReader(file));
			} catch (FileNotFoundException e) {
			    e.printStackTrace();
			} catch (IOException e) {
			    e.printStackTrace();
			}
		//Ouverture du fichier en mode écriture
		} else {
			try {
				this.buffW = new BufferedWriter(this.FileW = new FileWriter(file));
			} catch (FileNotFoundException e) {
			    e.printStackTrace();
			} catch (IOException e) {
			    e.printStackTrace();
			}
		}

	}

	@Override
	public void close() {
		//Fermeture du fichier en mode écriture
		if (this.mode == OpenMode.W) {
			try {
				this.FileW.close();
				this.buffW.close();
				this.mode = null;
			} catch (IOException e) {}
		//Fermeture du fichier en mode lecture
		} else {
			this.mode = null;
			this.index = 0;
			try {
				this.buffR.close();
				this.FileR.close();
			} catch (IOException e) {}
		}
	}

	@Override
	public long getIndex() {
		return this.index;
	}

	@Override
	public String getFname() {
		return this.fName;
	}

	@Override
	public void setFname(String fname) {
		this.fName = fname;

	}

}