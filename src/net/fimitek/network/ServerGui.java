package net.fimitek.network;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.fimitek.network.tcp.Client;
import net.teamfps.java.serialization.ContainerObject;

/**
 * 
 * @author Mikko Tekoniemi
 *
 */
public class ServerGui extends JFrame {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JList<Client> online = new JList<Client>();
	private JTextField tf = new JTextField();
	private JLabel lblSelected = new JLabel("Selected: ");
	private Client selectedClient;
	private JButton btnDownload = new JButton("Download");
	private JProgressBar progressBar = new JProgressBar();

	/**
	 * Create the frame.
	 */
	public ServerGui() {
		setTitle("Server GUI");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 430);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(0, 2, 0, 0));

		JScrollPane tsp = new JScrollPane();
		tsp.setViewportView(ServerInfo.tree);
		contentPane.add(tsp);

		JPanel right_panel = new JPanel();
		contentPane.add(right_panel);
		right_panel.setLayout(new GridLayout(0, 1, 0, 0));

		JPanel online_panel = new JPanel();
		right_panel.add(online_panel);
		online_panel.setLayout(new BorderLayout(0, 0));

		JPanel panel_1 = new JPanel();
		online_panel.add(panel_1, BorderLayout.NORTH);
		panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		panel_1.add(lblSelected);

		lblSelected.setFont(new Font("Tahoma", Font.BOLD, 14));

		JScrollPane sp1 = new JScrollPane();
		online_panel.add(sp1);

		online.setCellRenderer(new ClientCellRenderer());
		online.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		online.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				selectedClient = online.getSelectedValue();
				setSelectedText("" + selectedClient);
			}
		});
		online.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount() == 2) {
					if(selectedClient != null) {
						ContainerObject obj = new ContainerObject("RootBase");
						selectedClient.send(obj);
					}
				}
			}
		});
		sp1.setViewportView(online);

		JPanel info_panel = new JPanel();
		right_panel.add(info_panel);
		info_panel.setLayout(new BorderLayout(0, 0));

		JScrollPane sp = new JScrollPane();
		info_panel.add(sp, BorderLayout.CENTER);
		sp.setViewportView(ServerInfo.ta);

		JPanel action_panel = new JPanel();
		info_panel.add(action_panel, BorderLayout.SOUTH);
		action_panel.setLayout(new GridLayout(2, 1, 0, 0));

		JPanel panel = new JPanel();
		action_panel.add(panel);
		panel.setLayout(new BorderLayout(0, 0));
		panel.add(tf);
		tf.setColumns(10);

		JPanel button_panel = new JPanel();
		panel.add(button_panel, BorderLayout.EAST);

		JButton btnSend = new JButton("Send");
		button_panel.add(btnSend);

		JButton btnClear = new JButton("Clear");
		button_panel.add(btnClear);
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ServerInfo.ta.setText("");
			}
		});

		JPanel download_panel = new JPanel();
		action_panel.add(download_panel);
		download_panel.setLayout(new BorderLayout(0, 0));

		download_panel.add(progressBar);
		download_panel.add(btnDownload, BorderLayout.EAST);
		ServerInfo.tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
		ServerInfo.tree.setRootVisible(true);
		ServerInfo.ta.setEditable(false);
		setVisible(true);
	}

	public void setOnlineClients(List<Client> clients) {
		online.setListData(toList(clients));
	}

	public void updateUI() {
		online.updateUI();
	}

	private Client[] toList(List<Client> clients) {
		Client[] result = new Client[clients.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = clients.get(i);
		}
		return result;
	}

	class ClientCellRenderer extends JLabel implements ListCellRenderer<Client> {
		private static final long serialVersionUID = 1L;

		public ClientCellRenderer() {
			setOpaque(true);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends Client> list, Client value, int index, boolean isSelected, boolean cellHasFocus) {
			setText(value.getUsername() + " | " + value.getInetAddress());
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			return this;
		}
	}

	public void setSelectedText(String text) {
		lblSelected.setText("Selected: " + text);
	}

	public Client getSelected() {
		return selectedClient;
	}

	public JTextField getTF() {
		return tf;
	}

	public JButton getBtnDownload() {
		return btnDownload;
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}
}
