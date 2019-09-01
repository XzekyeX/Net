package net.fimitek.network;
/**
 * 
 * @author Mikko Tekoniemi
 *
 */
public class UpdateUI implements Action {
	private ServerGui gui;

	public UpdateUI(ServerGui gui) {
		this.gui = gui;
	}

	@Override
	public void action() {
		gui.updateUI();
		System.out.println("updateUI!");
	}

	public void setProgress(int value, int max) {
		setProgressValue(value);
		setProgressMax(max);
	}

	public void setProgressValue(int value) {
		gui.getProgressBar().setValue(value);
	}

	public void setProgressMax(int max) {
		gui.getProgressBar().setMaximum(max);
	}

	public void setProgressStringPainted(boolean value) {
		gui.getProgressBar().setStringPainted(value);
	}

	public void setProgressText(String text) {
		gui.getProgressBar().setString(text);
	}

	public ServerGui getGui() {
		return gui;
	}

	public float getProgress() {
		// TODO Auto-generated method stub
		float v0 = (float) gui.getProgressBar().getMaximum();
		float v1 = (float) gui.getProgressBar().getValue();
		return (v1 / v0) * 100.0F;
	}

}
