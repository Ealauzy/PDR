package formats;

import java.io.IOException;

public class LineFormat extends AbstractFormat{


	public LineFormat(String fname) {
		super(fname);
	}

	@Override
	public KV read() {
		try {
			//On lit une ligne du fichier qu'on stocke dans ligne
			String ligne = this.readFormatLigne();
			//On retourne un kv avec pour clef l'index (numero de ligne) et la ligne comme valeur
			return new KV(Long.toString(this.getIndex()), ligne);
		} catch (IOException e) {
			System.out.println("Fichier impossible à lire");
			return null;
		}
	}

	@Override
	public void write(KV record) {
		// On récupère la ligne à partir du kv (valeur du kv)
		String ligne = record.v;
		try {
			this.writeFormatLigne(ligne);
		} catch (IOException e) {
			System.out.println("Impossible d'écrire.");
		}
	}

	public Type getType() {
		return Format.Type.LINE;
	}

}
