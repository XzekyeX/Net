package net.fimitek.network;

/**
 * 
 * @author Mikko Tekoniemi
 *
 */
public class FileNode {
	protected String name;
	protected String path;
	protected long length;

	public FileNode(String name, String path, long length) {
		this.name = name;
		this.path = path;
		this.length = length;
	}

	public String getPath() {
		return path;
	}

	public long getLength() {
		return length;
	}

	@Override
	public String toString() {
		long Kb = 1 * 1024;
		long Mb = Kb * 1024;
		long Gb = Mb * 1024;
		long Tb = Gb * 1024;
		long Pb = Tb * 1024;
		long Eb = Pb * 1024;
		String size = (length > Kb) ? (length > Mb) ? (length > Gb) ? (length > Tb) ? (length > Pb) ? (length > Eb) ? ((length / Eb) + " Eb") : ((length / Pb) + " Pb") : ((length / Tb) + " Tb") : ((length / Gb) + " Gb") : ((length / Mb) + " Mb") : ((length / Kb) + " Kb") : (length + " Bytes");
		return path + " | " + size;
	}
}