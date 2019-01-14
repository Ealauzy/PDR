package formats;

import java.io.IOException;

public class KVFormat extends AbstractFormat{
	
	public KVFormat(String fname) {
		super(fname);
	}

	@Override
	public KV read() {
		try {
			//On lit une ligne du fichier qu'on stocke dans ligne
			String ligne = this.readFormatLigne();
			// On sectionne la ligne pour récupérer la clef et la valeur
			if (ligne == null){
				return new KV(null, null);
			}
			else {
				String[] result = ligne.split(KV.SEPARATOR);
				return new KV(result[0], result[1]);
			}
		} catch (IOException e) {
			System.out.println("Fichier impossible à lire");
			return null;
		}
	}

	@Override
	public void write(KV record) {
		// On recrée la ligne à partir du kv
		String ligne = record.k + KV.SEPARATOR + record.v;
		try {
			this.writeFormatLigne(ligne);
		} catch (IOException e) {
			System.out.println("Impossible d'écrire.");
		}
	}

	
	public KVFormat copieVide (int i){
		return new KVFormat(this.getFname()+Integer.toString(i));
	}

	@Override
	public Type getType() {
		return Format.Type.KV;
	}
	
}
