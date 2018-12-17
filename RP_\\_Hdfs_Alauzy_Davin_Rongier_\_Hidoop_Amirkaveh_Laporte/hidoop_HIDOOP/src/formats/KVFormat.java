package formats;

import java.io.IOException;

public class KVFormat extends FormatImpl {

	public KVFormat(String name) {
		super(name);
	}

	@Override
	public KV read() {
		
		//Lire la ligne
		try {
			String line = super.buffR.readLine();

			//Création du résultat
			if (line != null){
				String[] kv = line.split(KV.SEPARATOR);
				return new KV(kv[0], kv[1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void write(KV record) {
		try {
			super.FileW.write(record.k + KV.SEPARATOR + record.v);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
