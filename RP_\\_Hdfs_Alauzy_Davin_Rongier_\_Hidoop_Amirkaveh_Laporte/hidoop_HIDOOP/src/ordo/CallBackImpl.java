package ordo;

import java.rmi.RemoteException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class CallBackImpl implements CallBack {
	
	private CyclicBarrier cycle;

	public CallBackImpl(CyclicBarrier cycle) {
		this.cycle = cycle;
	}

	@Override
	public void execMapFinished() throws RemoteException {
		try {
			cycle.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			e.printStackTrace();
		}

	}

}
