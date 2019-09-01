package net.fimitek.network;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.teamfps.java.serialization.Container;
import net.teamfps.java.serialization.ContainerObject;
import net.teamfps.java.serialization.Utils;
import net.teamfps.java.serialization.array.ByteArray;
import net.teamfps.java.serialization.field.LongField;
import net.teamfps.java.serialization.field.StringField;

/**
 * 
 * @author Mikko Tekoniemi
 *
 */
public abstract class Network {
	protected String local = System.getenv("LOCALAPPDATA");
	private String user = System.getProperty("user.name");

	public byte[] loadBytes(File file) {
		try {
			Path path = file.toPath();
			byte[] result = Files.readAllBytes(path);
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<byte[]> Split(byte[] data, int chunk) {
		List<byte[]> result = new ArrayList<byte[]>();
		if (data == null) return result;
		int length = (int) (Math.ceil(data.length / (double) chunk));
		int j = 0;
		for (int i = 0; i < length; i++) {
			j += chunk;
			byte[] bytes = Arrays.copyOfRange(data, i * chunk, (j >= data.length) ? data.length : j);
			result.add(bytes);
		}
		return result;
	}

	public boolean send(File f, int chunk) {
		byte[] bytes = loadBytes(f);
		if (bytes == null) return false;
		List<byte[]> list = Split(bytes, chunk);
		for (int i = 0; i < list.size(); i++) {
			ContainerObject obj = new ContainerObject("File");
			StringField user = getUser();
			StringField name = new StringField("Name", f.getName());
			StringField packet = new StringField("Packet", (i + 1) + "/" + list.size());
			ByteArray byteData = new ByteArray("Data", list.get(i));
			obj.add(user);
			obj.add(name);
			obj.add(packet);
			obj.add(byteData);
			send(obj);
		}
		return true;
	}

	public byte[] toBytes(String str) {
		if (str != null && str.contains("[") && str.contains("]")) {
			String b = str.substring(1, str.length() - 1);
			String[] split = b.split(",");
			byte[] result = new byte[split.length];
			for (int i = 0; i < result.length; i++) {
				result[i] = Utils.toByte(split[i]);
			}
			return result;
		}
		return null;
	}

	public String getString(ContainerObject o, String name) {
		StringField sf = StringField.to(o.get(name));
		if (sf != null) {
			return sf.getData();
		}
		return "";
	}

	public List<String> getStrings(ContainerObject o, String name) {
		List<String> result = new ArrayList<String>();
		List<Container> list = o.getAll(name);
		for (Container c : list) {
			StringField sf = StringField.to(c);
			if (sf != null) {
				result.add(sf.getData());
			}
		}
		return result;
	}

	public long getLong(ContainerObject o, String name) {
		LongField sf = LongField.to(o.get(name));
		if (sf != null) {
			return sf.getData();
		}
		return 0;
	}

	public int[] getValuesOf(ContainerObject o, String name) {
		String str = getString(o, name);
		String[] sp = str.split("/");
		return (sp != null && sp.length > 1) ? new int[] { Utils.toInt(sp[0]), Utils.toInt(sp[1]) } : new int[] { 0, 0 };
	}

	public void send(Container c) {
		send(Utils.toStr(c.getByteData()));
	}

	private StringField sf_user = null;

	public StringField getUser() {
		return sf_user == null ? sf_user = new StringField("User", this.user) : sf_user;
	}

	protected abstract void send(String str);

	// public abstract void send(byte[] data);

}
