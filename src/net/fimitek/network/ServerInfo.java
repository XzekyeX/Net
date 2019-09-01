package net.fimitek.network;

import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * 
 * @author Mikko Tekoniemi
 *
 */
public class ServerInfo {
	protected static JTextArea ta = new JTextArea();
	// protected static JScrollPane sp = new JScrollPane();
	protected static JTree tree = new JTree();

	// private static void fixScrollBar() {
	// JScrollBar sb = sp.getVerticalScrollBar();
	// sb.setValue(sb.getMaximum());
	// }

	public static void println(String str) {
		ta.append(str + "\n");
		System.out.println(str);
		// fixScrollBar();
		ta.setCaretPosition(ta.getDocument().getLength());
	}

	public static void setFolder(DefaultMutableTreeNode folders) {
		tree.setModel(new DefaultTreeModel(folders));
	}

	public static JTree getTree() {
		return tree;
	}

}
