package nl.uurapp.uurapp.util;

public interface HttpTaskListener {
	public abstract void taskFinishedWithData(String data, HttpPostTask task);
	public abstract void taskDidFail(HttpPostTask task);
}

