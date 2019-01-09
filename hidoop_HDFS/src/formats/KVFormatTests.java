package formats;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import formats.Format.OpenMode;

public class KVFormatTests {
	private KVFormat kvFormat;
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
		this.kvFormat = new KVFormat(this.nomFichierI);
	}
	
	@Test
	public void testGetType() {
		assertEquals("Format", this.kvFormat.getType() , Format.Type.KV);
	}
	
	@Test
	public void testFName() {
		assertEquals("Name", this.kvFormat.getFname(), this.nomFichierI);
		this.kvFormat.setFname(nomFichier2);
		assertEquals("Name", this.kvFormat.getFname(), this.nomFichier2);
	}

	@Test
	public void testIndexAndReadAndWrite() {
		
		assertEquals("Index", this.kvFormat.getIndex(),0);
		
		this.kvFormat.open(OpenMode.W);
		this.kvFormat.write(kv1);
		this.kvFormat.write(kv2);
		this.kvFormat.write(kv3);
		this.kvFormat.write(kv4);
		this.kvFormat.write(kv5);
		this.kvFormat.write(kv6);
		this.kvFormat.close();
		
		this.kvFormat.open(OpenMode.R);
		
		ligne = kvFormat.read();
		assertEquals("Ligne 1", kv1.toString(), ligne.toString());
		assertEquals("Index", this.kvFormat.getIndex(),1);
		
		ligne = kvFormat.read();
		assertEquals("Ligne 2", kv2.toString(), ligne.toString());
		assertEquals("Index", this.kvFormat.getIndex(),2);
		
		ligne = kvFormat.read();
		assertEquals("Ligne 3", kv3.toString(), ligne.toString());
		assertEquals("Index", this.kvFormat.getIndex(),3);
		
		ligne = kvFormat.read();
		assertEquals("Ligne 4", kv4.toString(), ligne.toString());
		assertEquals("Index", this.kvFormat.getIndex(),4);
		
		ligne = kvFormat.read();
		assertEquals("Ligne 5", kv5.toString(), ligne.toString());
		assertEquals("Index", this.kvFormat.getIndex(),5);
		
		ligne = kvFormat.read();
		assertEquals("Ligne 6", kv6.toString(), ligne.toString());
		assertEquals("Index", this.kvFormat.getIndex(),6);
		
		this.kvFormat.close();
	}
	
	
	@Test
	public void testGetEtat() {
		this.kvFormat.open(OpenMode.W);
		assertEquals("Etat:Write", this.kvFormat.getEtat(), AbstractFormat.TypeEtat.OpenWrite);
		
		this.kvFormat.close();
		assertEquals("Etat:Closed", this.kvFormat.getEtat(), AbstractFormat.TypeEtat.Close);
		
		this.kvFormat.open(OpenMode.R);
		assertEquals("Etat:Read", this.kvFormat.getEtat(), AbstractFormat.TypeEtat.OpenRead);
		
		this.kvFormat.close();
	}
}
