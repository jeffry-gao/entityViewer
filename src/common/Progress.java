package common;

public interface Progress {
	public void setMax(int max);
	public void setProgress(int progress);
	public int getProgress();
	public void setCurrentWork(String desc);
}