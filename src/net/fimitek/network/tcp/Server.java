package net.fimitek.network.tcp;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import net.fimitek.network.FileNode;
import net.fimitek.network.ServerGui;
import net.fimitek.network.ServerInfo;
import net.teamfps.java.serialization.ContainerObject;
import net.teamfps.java.serialization.field.IntField;
import net.teamfps.java.serialization.field.StringField;

/**
 * 
 * @author Mikko Tekoniemi
 *
 */
public class Server implements Runnable {
	private ServerSocket socket;
	private Thread thread;
	private boolean running;
	private int port;
	private List<Client> clients = new ArrayList<Client>();
	private Random rand = new Random();

	private ServerGui gui;

	public Server(int port) {
		this.port = port;
	}

	private boolean create() {
		if (running) return false;
		try {
			socket = new ServerSocket(port);
			gui = new ServerGui();
			gui.getTF().addActionListener((e) -> send(e));
			gui.getBtnDownload().addActionListener((e) -> download(e));
//			gui.getBtnLastFolder().addActionListener((e) -> lastfolder(e));
			ServerInfo.getTree().addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					int selRow = ServerInfo.getTree().getRowForLocation(e.getX(), e.getY());
					TreePath selPath = ServerInfo.getTree().getPathForLocation(e.getX(), e.getY());
					// System.out.println("Root: " + ServerInfo.getTree().getModel().getRoot());
					if (selRow != -1) {
						if (e.getClickCount() == 2) {
							if (selPath != null) {
								Object o = selPath.getLastPathComponent();
								if (o instanceof DefaultMutableTreeNode) {
									DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
									if (node != null && gui.getSelected() != null) {
										boolean isFile = (node.getUserObject() instanceof FileNode);
										DefaultMutableTreeNode n = ((DefaultMutableTreeNode) ServerInfo.getTree().getModel().getRoot());
										if (n != null && n.children().hasMoreElements()) {
											if (!isFile && n.getFirstChild() != node) {
												ContainerObject obj = new ContainerObject("Root");
												obj.add(new StringField("Path", node.toString()));
												gui.getSelected().send(obj);
//												gui.setLastFolder();
												// ServerInfo.println("Root: " + obj);
											}
										} else {
											ServerInfo.println("Node has no children!");
										}
									}
								}
							}
						}
					}
				}
			});

			ServerInfo.println("Server has been started on port: " + port);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

//	private void lastfolder(ActionEvent e) {
//		if (gui.getSelected() != null) {
//			gui.getTF().setText("Root Path \"" + gui.getBtnLastFolder().getText() + "\"");
//		}
//	}

	private void download(ActionEvent e) {
		// DefaultMutableTreeNode node = (DefaultMutableTreeNode) ServerInfo.getTree().getLastSelectedPathComponent();
		TreePath[] paths = ServerInfo.getTree().getSelectionPaths();
		if (paths != null) {
			for (TreePath p : paths) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) p.getLastPathComponent();
				if (node == null || gui.getSelected() == null) return;
				Object nodeInfo = node.getUserObject();
				if (node.isLeaf()) {
					if (nodeInfo instanceof FileNode) {
						FileNode fn = (FileNode) nodeInfo;
						if (fn != null) {
							ContainerObject obj = new ContainerObject("File");
							String data = fn.getPath();
							obj.add(new StringField("Path", data));
							gui.getSelected().send(obj);
							ServerInfo.println("[SERVER]: " + obj + " -> " + gui.getSelected());
						}
					}
				}
			}
		}
	}

	private void send(ActionEvent e) {
		if (gui.getSelected() != null) {
			List<String> list = split(gui.getTF().getText());
			System.out.println(list);
			if (list.size() > 0) {
				ContainerObject obj = new ContainerObject(list.get(0));
				for (int i = 1; i < list.size(); i += 2) {
					int index = i + 1;
					if (index >= list.size()) {
						System.out.println("Break[" + i + "]");
						break;
					}
					String data = list.get(index);
					int val = 0;
					if ((val = isNumber(data)) > 0) {
						IntField sf = new IntField(list.get(i), val);
						obj.add(sf);
					} else {
						StringField sf = new StringField(list.get(i), data);
						obj.add(sf);
					}
				}
				gui.getSelected().send(obj);
				gui.getTF().setText("");
				ServerInfo.println("[SERVER]: " + obj + " -> " + gui.getSelected());
			}
		} else {
			ServerInfo.println("Select \"CLIENT\" before trying to send message!");
		}
	}

	public List<String> split(String str) {
		List<String> result = new ArrayList<String>();
		Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(str);
		while (m.find()) {
			result.add(m.group(1).replaceAll("\"", ""));
		}
		return result;
	}

	private int isNumber(String str) {
		try {
			int val = Integer.parseInt(str);
			return val;
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public void start() {
		if (running || !create()) return;
		running = true;
		thread = new Thread(this, "Server Thread");
		thread.start();
	}

	public void stop() {
		if (!running) return;
		running = false;
		thread.interrupt();
	}

	public void run() {
		Thread update = new Thread(() -> updateSync(), "Update Thread!");
		update.start();
		while (running) {
			try {
				Socket s = socket.accept();
				accept(s);
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}

	private void updateSync() {
		long lastTime = System.nanoTime();
		double delta = 0;
		double ns = 1000000000 / 60.0;
		while (running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while (delta >= 1) {
				delta--;
				update();
			}
		}
	}

	public void update() {
		for (int i = 0; i < clients.size(); i++) {
			if (gui != null) clients.get(i).initUI(gui);
			clients.get(i).update();
			if (!clients.get(i).isRunning()) {
				ServerInfo.println("Client has been disconnected! ID: " + clients.get(i).getID());
				clients.remove(i);
				// if (gui != null) gui.setOnlineClients(clients);
				updateUI();
			}
		}
	}

	public void accept(Socket s) {
		Client c = new Client(s, getAvailableID());
		clients.add(c);
		c.start();
		ServerInfo.println("New client has been accepted! ID: " + c.getID());
		// if (gui != null) gui.setOnlineClients(clients);
		updateUI();
	}

	public void updateUI() {
		if (gui != null) gui.setOnlineClients(clients);
	}

	private int getAvailableID() {
		int id = rand.nextInt(1000);
		for (Client c : clients)
			if (c.getID() == id) return getAvailableID();
		return id;
	}

	public static void main(String[] args) {
		new Server(1997).start();
	}

}
