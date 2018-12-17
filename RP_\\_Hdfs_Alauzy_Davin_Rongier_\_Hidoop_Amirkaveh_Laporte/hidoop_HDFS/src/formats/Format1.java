package formats;

import java.io.Serializable;

public interface Format1 extends Format{

	public void open(OpenMode mode);
	public void close();
	public long getIndex();
	public String getFname();
	public void setFname(String fname);
	public Type getType();
	
}

