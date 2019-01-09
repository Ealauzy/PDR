package formats;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import formats.Format.OpenMode;

public class LineTests {
	private LineFormat lFormat;
	private String nomFichierI = "testLine";
	private String nomFichier2 = "testLine";
	private KV ligne;
	
	private KV kv1 =  new KV("1", "valeur1");
	private KV kv2 =  new KV("2", "valeur2");
	private KV kv3 =  new KV("3", "valeur3");
	private KV kv4 =  new KV("4", "valeur4");
	private KV kv5 =  new KV("5", "valeur5");
	private KV kv6 =  new KV("6", "valeur6");
	
	
	@Before
	public void SetUp(){
		this.lFormat = new LineFormat(this.nomFichierI);
	}
	
	@Test
	public void testGetType() {
		assertEquals("Format", this.lFormat.getType() , Format.Type.LINE);
	}
	
	@Test
	public void testFName() {
		assertEquals("Name", this.lFormat.getFname(), this.nomFichierI);
		this.lFormat.setFname(nomFichier2);
		assertEquals("Name", this.lFormat.getFname(), this.nomFichier2);
	}

	@Test
	public void testIndexAndReadAndWrite() {
		
		assertEquals("Index", this.lFormat.getIndex(),0);
		
		this.lFormat.open(OpenMode.W);
		this.lFormat.write(kv1);
		this.lFormat.write(kv2);
		this.lFormat.write(kv3);
		this.lFormat.write(kv4);
		this.lFormat.write(kv5);
		this.lFormat.write(kv6);
		this.lFormat.close();
		
		this.lFormat.open(OpenMode.R);
		
		ligne = lFormat.read();
		assertEquals("Ligne 1", kv1.toString(), ligne.toString());
		assertEquals("Index", this.lFormat.getIndex(),1);
		
		ligne = lFormat.read();
		assertEquals("Ligne 2", kv2.toString(), ligne.toString());
		assertEquals("Index", this.lFormat.getIndex(),2);
		
		ligne = lFormat.read();
		assertEquals("Ligne 3", kv3.toString(), ligne.toString());
		assertEquals("Index", this.lFormat.getIndex(),3);
		
		ligne = lFormat.read();
		assertEquals("Ligne 4", kv4.toString(), ligne.toString());
		assertEquals("Index", this.lFormat.getIndex(),4);
		
		ligne = lFormat.read();
		assertEquals("Ligne 5", kv5.toString(), ligne.toString());
		assertEquals("Index", this.lFormat.getIndex(),5);
		
		ligne = lFormat.read();
		assertEquals("Ligne 6", kv6.toString(), ligne.toString());
		assertEquals("Index", this.lFormat.getIndex(),6);
		
		this.lFormat.close();
	}
	
	
	@Test
	public void testGetEtat() {
		this.lFormat.open(OpenMode.W);
		assertEquals("Etat:Write", this.lFormat.getEtat(), AbstractFormat.TypeEtat.OpenWrite);
		
		this.lFormat.close();
		assertEquals("Etat:Closed", this.lFormat.getEtat(), AbstractFormat.TypeEtat.Close);
		
		this.lFormat.open(OpenMode.R);
		assertEquals("Etat:Read", this.lFormat.getEtat(), AbstractFormat.TypeEtat.OpenRead);
		
		this.lFormat.close();
	}
}
