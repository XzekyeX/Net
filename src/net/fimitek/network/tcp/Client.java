package net.fimitek.network.tcp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import net.fimitek.network.FileNode;
import net.fimitek.network.FilePacket;
import net.fimitek.network.Network;
import net.fimitek.network.ServerGui;
import net.fimitek.network.ServerInfo;
import net.fimitek.network.UpdateUI;
import net.teamfps.java.serialization.Container;
import net.teamfps.java.serialization.ContainerObject;
import net.teamfps.java.serialization.Utils;
import net.teamfps.java.serialization.field.IntField;
import net.teamfps.java.serialization.field.LongField;
import net.teamfps.java.serialization.field.StringField;

/**
 * 
 * @author Mikko Tekoniemi
 *
 */
public class Client extends Network implements Runnable {
	private boolean running;
	private boolean client = false;
	private Thread thread;
	private Socket socket;
	private String ip;
	private String username;
	private int port;
	private int id;
	private PrintWriter sender;
	private BufferedReader reader;
	private List<FilePacket> packets = new ArrayList<FilePacket>();
	protected long time;
	private Thread retry;
	private UpdateUI ui;

	public Client(Socket socket, int id) {
		this.socket = socket;
		this.id = id;
		this.client = false;
		this.time = Instant.now().toEpochMilli();
	}

	public Client(String ip, int port) {
		this.ip = ip;
		this.port = port;
		this.client = true;
		this.time = Instant.now().toEpochMilli();
	}

	public boolean connect() {
		try {
			this.socket = new Socket(ip, port);
			System.out.println("Successfully connected to: " + ip + ":" + port);
			return true;
		} catch (UnknownHostException e) {
			System.err.println("UnknownHostException: Unable to connect to server!");
		} catch (IOException e) {
			System.err.println("IOException: Unable to connect to server!");
			if (retry == null) {
				retry = new Thread(() -> retry(), "Thread retry");
				retry.start();
			}
		}
		return false;
	}

	private void retry() {
		System.out.println("Thread retry!");
		long timer = System.currentTimeMillis();
		while (true) {
			if (System.currentTimeMillis() - timer >= 1000 * 60) {
				timer += 1000 * 60;
				System.out.println("Trying to connect!");
				if (connect()) {
					start();
					retry.interrupt();
					System.out.println("Break!");
					break;
				}
			}
		}
	}

	public void start() {
		if (running || socket == null) return;
		running = true;
		thread = new Thread(this);
		thread.start();
	}

	public void stop() {
		if (!running) return;
		running = false;
		thread.interrupt();
	}

	public void run() {
		if (isClient()) {
			handleClient();
		} else {
			handleServerClient();
		}
	}

	private void handleClient() {
		while (running) {
			String str = receive();
			byte[] data = toBytes(str);
			if (data != null) {
				List<Container> list = Container.getData(data, 0);
				for (Container c : list) {
					ContainerObject co = ContainerObject.to(c);
					if (co != null) {
						// System.out.println("client! name: " + co.getName());
						switch (co.getName()) {
							case "File":
								ClientFilePacket(co);
								break;
							case "RootBase":
								ClientBaseRootPacket();
								break;
							case "Root":
								ClientRootPacket(co);
								break;
						}
					}
				}
			}
		}
	}

	private void handleServerClient() {
		while (running) {
			String str = receive();
			byte[] data = toBytes(str);
			if (data != null) {
				List<Container> list = Container.getData(data, 0);
				for (Container c : list) {
					ContainerObject co = ContainerObject.to(c);
					if (co != null) {
						// System.out.println("server! name: " + co.getName());
						switch (co.getName()) {
							case "File":
								ServerFilePacket(co);
								break;
							case "RootBase":
								ServerBaseRootPacket(co);
								break;
							case "Root":
								ServerBaseRootPacket(co);
								break;
						}
					}
				}
			}
		}
	}

	private void ClientRootPacket(ContainerObject co) {
		String path = getString(co, "Path");
		if (path.length() > 0) {
			ContainerObject root_base = new ContainerObject("Root");
			File root = new File(path);
			ContainerObject root_obj = new ContainerObject("Root");
			root_obj.add(new StringField("RootFolder", root.getAbsolutePath()));
			File[] files = root.listFiles();
			if (files != null) {
				ContainerObject root_obj_sub = new ContainerObject("RootFolderSub");
				for (File file : files) {
					if (file.isDirectory()) {
						root_obj_sub.add(new StringField("Folder", file.getAbsolutePath()));
					} else {
						ContainerObject file_obj = new ContainerObject("File");
						file_obj.add(new StringField("Path", file.getAbsolutePath()));
						file_obj.add(new LongField("Length", file.length()));
						root_obj_sub.add(file_obj);
					}
				}
				root_obj.add(root_obj_sub);
			}
			root_base.add(root_obj);
			send(root_base);
		}
	}

	private void ServerBaseRootPacket(ContainerObject co) {
		List<Container> roots = co.getAll("Root");
		ServerInfo.println("Roots:" + roots);
		DefaultMutableTreeNode base_root = new DefaultMutableTreeNode();
		for (Container root : roots) {
			ContainerObject root_obj = ContainerObject.to(root);
			if (root_obj != null) {
				String root_name = getString(root_obj, "RootFolder");
				DefaultMutableTreeNode root_node = new DefaultMutableTreeNode(root_name);
				ContainerObject root_folder_sub = ContainerObject.to(root_obj.get("RootFolderSub"));
				if (root_folder_sub != null) {
					List<Container> root_folder_sub_objs = root_folder_sub.getObjects();
					for (Container c : root_folder_sub_objs) {
						if (c.getName().equals("Folder")) {
							StringField sf_folder_name = StringField.to(c);
							if (sf_folder_name != null) {
								root_node.add(new DefaultMutableTreeNode(sf_folder_name.getData()));
							}
						} else if (c.getName().equals("File")) {
							ContainerObject file_obj = ContainerObject.to(c);
							if (file_obj != null) {
								String path = getString(file_obj, "Path");
								long length = getLong(file_obj, "Length");
								if (path.length() > 0 && length > 0) {
									root_node.add(new DefaultMutableTreeNode(new FileNode("", path, length)));
								} else {
									ServerInfo.println("Error: Loading root node! path: " + path + ", length: " + length);
								}
							}
						}
					}
					// System.out.println(root_folder_sub_objs);
				}
				base_root.add(root_node);
				// System.out.println(root);
			}
		}
		ServerInfo.setFolder(base_root);
	}

	private void ClientBaseRootPacket() {
		System.out.println("RootBase Packet!");
		ContainerObject base = new ContainerObject("RootBase");
		File[] roots = File.listRoots();
		if (roots != null) {
			for (File root : roots) {
				ContainerObject root_obj = new ContainerObject("Root");
				root_obj.add(new StringField("RootFolder", root.getAbsolutePath()));
				File[] files = root.listFiles();
				if (files != null) {
					ContainerObject root_obj_sub = new ContainerObject("RootFolderSub");
					for (File file : files) {
						if (file.isDirectory()) {
							root_obj_sub.add(new StringField("Folder", file.getAbsolutePath()));
						} else {
							ContainerObject file_obj = new ContainerObject("File");
							file_obj.add(new StringField("Path", file.getAbsolutePath()));
							file_obj.add(new LongField("Length", file.length()));
							root_obj_sub.add(file_obj);
						}
					}
					root_obj.add(root_obj_sub);
				}
				base.add(root_obj);
			}
			send(base);
		}
	}

	public String format(String str) {
		String path = str;
		if (path.contains("%")) {
			int begin = path.indexOf("%");
			String m = Utils.match(path, "%", "%");
			String env = System.getenv(m);
			int end = begin + m.length() + 2;
			String p = path.substring(end);
			path = env + p;
		}
		return path;
	}

	private void ClientFilePacket(ContainerObject co) {
		String path = getString(co, "Path");
		if (path.contains("%")) {
			int begin = path.indexOf("%");
			String m = Utils.match(path, "%", "%");
			String env = System.getenv(m);
			int end = begin + m.length() + 2;
			String p = path.substring(end);
			path = env + p;
		}
		IntField chunk = IntField.to(co.get("Chunk"));
		int c = chunk != null ? chunk.getData() : 1024;
		File file = new File(path);
		if (file.exists()) {
			send(file, c);
		}
	}

	private void ServerFilePacket(ContainerObject co) {
		String user = getString(co, "User");
		String file = getString(co, "Name");
		int[] size = getValuesOf(co, "Packet");
		FilePacket p = getPacket(file, user);
		if (p != null) {
			p.add(co);
		} else {
			this.username = user;
			FilePacket packet = new FilePacket(this, file, size[1]);
			packet.add(co);
			packets.add(packet);
		}
	}

	public void update() {
		for (int i = 0; i < packets.size(); i++) {
			if (packets.get(i).isDone()) {
				packets.remove(i);
				System.out.println("packet has successfully removed!");
			}
		}
	}

	public FilePacket getPacket(String file, String user) {
		for (FilePacket p : packets) {
			if (p.getFileName().equals(file) && p.getUsername().equals(user)) {
				return p;
			}
		}
		return null;
	}

	public String getPath(String str) {
		int last = str.lastIndexOf("\\");
		return str.substring(0, last);
	}

	@Override
	protected void send(String str) {
		if (socket == null) return;
		try {
			if (sender == null) sender = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			sender.println(str);
			sender.flush();
		} catch (IOException e) {
			System.err.println("[SEND] IOException: " + e.getMessage());
			stop();
		}
	}

	protected String receive() {
		if (socket == null) {
			System.err.println("Socket == null!");
			stop();
			return null;
		}
		try {
			if (reader == null) reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String readed = reader.readLine();
			if (readed != null) return readed;
		} catch (IOException e) {
			System.err.println("[RECEIVE] IOException: " + e.getMessage());
			stop();
		}
		return null;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isClient() {
		return client;
	}

	public int getID() {
		return id;
	}

	public Socket getSocket() {
		return socket;
	}

	public InetAddress getInetAddress() {
		return this.socket != null ? this.socket.getInetAddress() : null;
	}

	public long getTime() {
		return time;
	}

	public String getUsername() {
		return username == null ? "Client" + getID() : username;
	}

	@Override
	public String toString() {
		return "Client" + getID() + " | " + getInetAddress();
	}

	public static void main(String[] args) {
		if (args.length > 1) {
			Client c = new Client(args[0], toPort(args[1]));
			if (c.connect()) {
				c.start();
			}
		} else {
			Client c = new Client("127.0.0.1", 1997);
			if (c.connect()) {
				c.start();
			}
		}
	}

	private static int toPort(String port) {
		try {
			return Integer.parseInt(port);
		} catch (NumberFormatException e) {
			System.err.println("\"" + port + "\" is not valid port!");
		}
		return 1997;
	}

	public void initUI(ServerGui gui) {
		if (ui != null) return;
		ui = new UpdateUI(gui);
		System.out.println("[" + getUsername() + "]: Init UI!");
	}

	public UpdateUI getUI() {
		return ui;
	}

}
