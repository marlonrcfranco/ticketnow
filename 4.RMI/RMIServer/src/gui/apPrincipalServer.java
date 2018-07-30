package gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.Font;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;
import javax.swing.border.LineBorder;
import java.awt.Color;

public class apPrincipalServer {

	private apConexao telaConexao;
	private JFrame frame;
	private JTextField txtPorta;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					apPrincipalServer window = new apPrincipalServer();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public apPrincipalServer() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle("ServidorRMI");
		frame.setBounds(100, 100, 440, 197);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Endere\u00E7o do Servidor:", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
		panel.setBounds(12, 12, 414, 100);
		frame.getContentPane().add(panel);
		panel.setLayout(null);
		
		JLabel lblPorta = new JLabel("Porta:");
		lblPorta.setBounds(12, 23, 79, 32);
		panel.add(lblPorta);
		
		txtPorta = new JTextField();
		txtPorta.setToolTipText("Somente numeros");
		txtPorta.setBounds(70, 30, 114, 19);
		panel.add(txtPorta);
		txtPorta.setColumns(10);
		
		JLabel lblobsOIp = new JLabel("(Obs.: o IP configurado é o IP desta máquina)");
		lblobsOIp.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblobsOIp.setVerticalAlignment(SwingConstants.BOTTOM);
		lblobsOIp.setBounds(22, 47, 361, 24);
		panel.add(lblobsOIp);
		
		JLabel label = new JLabel("");
		label.setIcon(new ImageIcon(apPrincipalServer.class.getResource("/resources/icons/globe-icon.png")));
		label.setBounds(332, 12, 70, 70);
		panel.add(label);
		
		JButton btnConfirmar = new JButton("Confirmar");
		btnConfirmar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (txtPorta.equals("") || txtPorta.equals(null) || Integer.parseInt(txtPorta.getText()) <= 0 || Integer.parseInt(txtPorta.getText()) > 65000) {
					JOptionPane.showMessageDialog(null, "Porta deve conter somente numeros", "ERRO", JOptionPane.INFORMATION_MESSAGE);
				}else {
					telaConexao = new apConexao(Integer.parseInt(txtPorta.getText()));
					frame.setVisible(false);
				}
			}
		});
		btnConfirmar.setBounds(170, 124, 117, 25);
		frame.getContentPane().add(btnConfirmar);
	}
}
