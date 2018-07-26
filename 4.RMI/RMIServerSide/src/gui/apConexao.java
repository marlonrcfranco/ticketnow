package gui;

import java.awt.Color;
import java.rmi.RemoteException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import rmi.Server;

public class apConexao {

	private JFrame frame;
	private int iPorta;

	public JFrame getFrame() {
		return frame;
	}

	public void setFrame(JFrame frame) {
		this.frame = frame;
	}

	/**
	 * Create the application.
	 */
	public apConexao(int iPorta) {
		this.iPorta = iPorta;
		initialize();
		this.frame.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle("ServidorRMI");
		frame.setBounds(100, 100, 287, 97);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Status", TitledBorder.LEADING,
				TitledBorder.TOP, null, new Color(51, 51, 51)));
		panel.setBounds(12, 12, 193, 42);
		frame.getContentPane().add(panel);
		panel.setLayout(null);

		JLabel lblServidorRunning = new JLabel("Running...");
		lblServidorRunning.setVerticalAlignment(SwingConstants.BOTTOM);
		lblServidorRunning.setHorizontalAlignment(SwingConstants.CENTER);
		lblServidorRunning.setBounds(12, 12, 147, 21);
		panel.add(lblServidorRunning);

		JPanel panel_1 = new JPanel();
		panel_1.setBounds(217, 12, 57, 42);
		frame.getContentPane().add(panel_1);
		panel_1.setLayout(null);

		JLabel lblImage = new JLabel("");
		lblImage.setIcon(new ImageIcon(apConexao.class.getResource("/resources/icons/runnning-icon.png")));
		lblImage.setBounds(0, 0, 57, 42);
		panel_1.add(lblImage);

		Server oServer = new Server();
		oServer.setiPorta(iPorta);
		try {
			oServer.Inicializa();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}
}
