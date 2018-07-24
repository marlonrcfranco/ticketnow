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

public class apPrincipal {

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
					apPrincipal window = new apPrincipal();
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
	public apPrincipal() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle("ServidorRMI");
		frame.setBounds(100, 100, 450, 177);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Configura\u00E7\u00F5es:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setBounds(12, 12, 414, 83);
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
		lblobsOIp.setBounds(53, 47, 361, 24);
		panel.add(lblobsOIp);
		
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
		btnConfirmar.setBounds(170, 107, 117, 25);
		frame.getContentPane().add(btnConfirmar);
	}
}
