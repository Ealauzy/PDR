package ordo;

import map.MapReduce;
import formats.Format;

public interface JobInterfaceX  extends JobInterface {
    public void setNumberOfReduces(int tasks);
    public void setNumberOfMaps(int tasks);
    public void setOutputFormat(Format.Type ft);
    public void setOutputFname(String fname);    
    public int getNumberOfReduces();
    public int getNumberOfMaps();
    public Format.Type getInputFormat();
    public Format.Type getOutputFormat();
    public String getInputFname();
    public String getOutputFname();
}