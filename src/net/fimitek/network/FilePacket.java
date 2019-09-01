package net.fimitek.network;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.fimitek.network.tcp.Client;
import net.teamfps.java.serialization.ContainerObject;
import net.teamfps.java.serialization.array.ByteArray;

/**
 * 
 * @author Mikko Tekoniemi
 *
 */
public class FilePacket {
	protected List<ContainerObject> packets = new ArrayList<ContainerObject>();
	protected String file;
	protected int size;
	protected Client client;
	protected boolean done = false;

	public FilePacket(Client client, String fileName, int size) {
		this.file = fileName;
		this.size = size;
		this.client = client;
	}

	public void add(ContainerObject c) {
		packets.add(c);
		ServerInfo.println("[CLIENT" + client.getID() + "]: Constructing " + file + " " + packets.size() + "/" + size);
		if (client.getUI() != null) {
			client.getUI().setProgressStringPainted(true);
			client.getUI().setProgress(packets.size(), size);
			client.getUI().setProgressText("Constructing " + file + " " + String.format("%.1f", client.getUI().getProgress()));
		}
		if (size == packets.size()) {
			byte[] data = construct();
			client.getUI().setProgressText("Saving " + file);
			save("C:/Net/" + client.getUsername() + "/" + client.getTime(), file, data);
			client.getUI().setProgressText("Done!");
			done = true;
		}
	}

	public boolean save(String path, String file, byte[] data) {
		try {
			File p = new File(path);
			if (!p.exists() && p.mkdirs()) ServerInfo.println("new folder has been created!");
			File f = new File(p, file);
			if (!f.exists()) {
				if (!f.isDirectory() && f.createNewFile()) {
					ServerInfo.println("new file has been created!");
				}
			}
			FileOutputStream fos = new FileOutputStream(f);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			ServerInfo.println("Saving " + file);
			bos.write(data);
			bos.close();
			fos.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public Comparator<ContainerObject> sorter = new Comparator<ContainerObject>() {
		@Override
		public int compare(ContainerObject o1, ContainerObject o2) {
			return client.getValuesOf(o1, "Packet")[0] > client.getValuesOf(o2, "Packet")[0] ? 1 : client.getValuesOf(o1, "Packet")[0] < client.getValuesOf(o2, "Packet")[0] ? -1 : 0;
		}
	};

	public byte[] construct() {
		packets.sort(sorter);
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		for (ContainerObject c : packets) {
			ByteArray ba = ByteArray.to(c.get("Data"));
			if (ba != null) {
				try {
					data.write(ba.getData());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return data.toByteArray();
	}

	public String getFileName() {
		return file;
	}

	public String getUsername() {
		return client.getUsername();
	}
	
	public boolean isDone() {
		return done;
	}
}