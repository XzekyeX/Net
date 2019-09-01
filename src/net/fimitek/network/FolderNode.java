package net.fimitek.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
/**
 * 
 * @author Mikko Tekoniemi
 *
 */
public class FolderNode {
	protected FolderNode parent;
	protected String name, path;
	protected HashMap<String, FolderNode> folder_nodes = new HashMap<String, FolderNode>();
	protected HashMap<String, FileNode> file_nodes = new HashMap<String, FileNode>();

	public FolderNode(String name, String path) {
		this.name = name;
		this.path = path;
	}

	public void addFolderNode(FolderNode n) {
		if (folder_nodes.containsKey(n.path)) {
			folder_nodes.get(n.path).folder_nodes = n.folder_nodes;
			System.out.println("Updating nodes!");
			return;
		}
		n.parent = this;
		folder_nodes.put(n.path, n);
	}

	public void addFolderNodes(List<FolderNode> subs) {
		for (FolderNode n : subs) {
			addFolderNode(n);
		}
	}

	public List<FolderNode> findFolder(String name) {
		List<FolderNode> result = new ArrayList<FolderNode>();
		for (Entry<String, FolderNode> e : folder_nodes.entrySet()) {
			FolderNode node = e.getValue();
			if (node != null) {
				if (name.equals(node.name)) {
					result.add(node);
				}
			}
		}
		return result;
	}

	public List<FolderNode> findFolderPath(String path) {
		List<FolderNode> result = new ArrayList<FolderNode>();
		for (Entry<String, FolderNode> e : folder_nodes.entrySet()) {
			FolderNode node = e.getValue();
			if (node != null) {
				if (path.equals(node.path)) {
					result.add(node);
				}
			}
		}
		return result;
	}

	public List<FolderNode> getChildFolderNodes(String name, int attempt) {
		List<FolderNode> result = new ArrayList<FolderNode>();
		if (attempt >= 5) return result;
		for (Entry<String, FolderNode> e : folder_nodes.entrySet()) {
			FolderNode val = e.getValue();
			if (val.name.equals(name)) result.add(val);
			List<FolderNode> childs = val.getChildFolderNodes(name, attempt++);
			if (childs.size() > 0) {
				result.addAll(childs);
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return "Node[Name = " + name + ", Parent = " + (parent != null ? parent.name : "") + ", Folder Nodes = " + folder_nodes + "]";
	}

}
