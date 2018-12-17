package ordo;

import java.rmi.*;

public interface CallBack extends Remote {
	
	void execMapFinished() throws RemoteException;
	
}

