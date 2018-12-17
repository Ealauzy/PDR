package ordo;

import java.util.List;
import java.rmi.Remote;
import java.rmi.RemoteException;

import map.Mapper;
import formats.Format;
import formats.KV;

public interface Daemon extends Remote {
	
	public void runMap (Mapper m, Format reader, Format writer, CallBack cb) throws RemoteException;
}