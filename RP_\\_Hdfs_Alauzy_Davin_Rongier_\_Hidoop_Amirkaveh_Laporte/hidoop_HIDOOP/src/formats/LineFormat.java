package formats;

import java.io.IOException;

public class LineFormat extends FormatImpl {

	public LineFormat(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public KV read() {
		try {
			String line = super.buffR.readLine();

			//Création du résultat
			if (line != null){
				return new KV("" + getIndex(), line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void write(KV record) {
		try {
			super.FileW.write(record.v);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
