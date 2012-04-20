package github.javamonkey79;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileFilter;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class FindClass {
	JFrame mainFrame;

	JTextField textJarDir;
	JTextField textClassName;
	JTextArea textResult;

	public static final String APP_NAME = "Find Jar";
	protected static final String lcOSName = System.getProperty("os.name")
			.toLowerCase();
	protected static final boolean IS_MAC = lcOSName.startsWith("mac os x");
	private final static int MIN_WINDOWS_WIDTH = 800;
	private final static int MIN_WINDOWS_HEIGHT = 600;

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception {
		if (IS_MAC) {
			// take the menu bar off the jframe
			System.setProperty("apple.laf.useScreenMenuBar", "true");

			// set the name of the application menu item
			System.setProperty(
					"com.apple.mrj.application.apple.menu.about.name", APP_NAME);
		}

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		new FindClass().open();
	}

	private boolean processingFlag = false;

	private void open() {
		mainFrame = new JFrame(APP_NAME);
		WindowListener wndCloser = new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				if (!processingFlag) {
					System.exit(0);
				}
			}

		};
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(wndCloser);
		ComponentListener compListener = new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				Component c = (Component) e.getSource();
				if (c instanceof JFrame) {
					Dimension newSize = c.getSize();
					int newWidth = (int) newSize.getWidth();
					int newHeight = (int) newSize.getHeight();
					if (newWidth < MIN_WINDOWS_WIDTH) {
						newWidth = MIN_WINDOWS_WIDTH;
					}
					if (newHeight < MIN_WINDOWS_HEIGHT) {
						newHeight = MIN_WINDOWS_HEIGHT;
					}
					c.setSize(newWidth, newHeight);
				}
			}
		};
		mainFrame.addComponentListener(compListener);

		JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(BorderFactory.createTitledBorder(""));

		GridBagConstraints c = new GridBagConstraints();
		// c.insets = new Insets(5, 5, 5, 5);
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1.0;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;

		textJarDir = new JTextField();
		mainPanel.add(textJarDir, c);

		c.gridx = 1;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		JButton btnSelectdir = new JButton("Select Folder");
		mainPanel.add(btnSelectdir, c);
		btnSelectdir.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				if (IS_MAC) {
					selectDistFileAWT();
				} else {
					selectDistFileSwing();
				}
			}
		});

		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		textClassName = new JTextField();
		mainPanel.add(textClassName, c);

		c.gridx = 1;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		JButton btnSearch = new JButton("Search");
		mainPanel.add(btnSearch, c);
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				if (textJarDir.getText().length() == 0) {
					JOptionPane.showMessageDialog(null,
							"Please enter a jar folder to find!", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
				if (textClassName.getText().length() == 0) {
					JOptionPane.showMessageDialog(null,
							"Please enter class name to find!", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
				final File jarDir = new File(textJarDir.getText());
				if (!jarDir.exists() || !jarDir.isDirectory()) {
					JOptionPane.showMessageDialog(null, textJarDir.getText()
							+ " is not exist or not a folder!", "Error",
							JOptionPane.ERROR_MESSAGE);
				}

				new Thread() {
					@Override
					public void run() {
						searchJarDir(jarDir, textClassName.getText(), 0);
					}
				}.start();
			}
		});

		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 1;
		c.weighty = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel("Search Result"), c);

		textResult = new JTextArea();
		textResult.setEnabled(false);
		c.gridx = 0;
		c.gridy = 3;
		c.weightx = 1;
		c.weighty = 1;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		mainPanel.add(new JScrollPane(textResult), c);

		mainFrame.getContentPane().add(mainPanel);

		mainFrame.setSize(MIN_WINDOWS_WIDTH, MIN_WINDOWS_HEIGHT);
		mainFrame.setVisible(true);
	}

	FileFilter fileFilter = new FileFilter() {
		public boolean accept(final File f) {
			if (f.isDirectory()) {
				return true;
			}
			String name = f.getName().toLowerCase();
			if (name.endsWith(".jar") || name.endsWith(".zip")) {
				return true;
			}
			return false;
		}
	};

	private void searchJarDir(final File jarDir, final String className,
			final int level) {
		File[] fileArray = jarDir.listFiles(fileFilter);
		if (fileArray != null && fileArray.length > 0) {
			for (File sub : fileArray) {
				if (sub.isDirectory()) {
					searchJarDir(sub, className, level + 1);
				} else {
					searchJarFile(sub, className);
				}
			}
		}
		if (level == 0) {
			textResult
					.setText(textResult.getText() + "Search completed" + "\n");
		}
		return;
	}

	private void searchJarFile(final File jarFile, final String className) {
		String nameForClass = className + ".class";
		String nameForSource = className + ".java";
		try {
			ZipFile zipFile = new ZipFile(jarFile);
			Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
			while (zipEntries.hasMoreElements()) {
				ZipEntry entry = zipEntries.nextElement();
				String name = entry.getName();
				if (name.endsWith(nameForClass)) {
					addSearchResult(jarFile, TYPE_CLASS);
				} else if (name.endsWith(nameForSource)) {
					addSearchResult(jarFile, TYPE_SOURCE);
				}
				// Process the name, here we just print it out
				System.out.println(name);
			}
		} catch (Exception e) {
			e.printStackTrace();
			addError("problem with jar file: " + jarFile.getName(), e);
		}
	}

	private final static int TYPE_CLASS = 1;
	private final static int TYPE_SOURCE = 2;

	private void addSearchResult(final File jarFile, final int type) {
		textResult.append((type == TYPE_CLASS ? "Found Class in: "
				: "Found Source in:") + jarFile.getAbsolutePath() + "\n");
	}

	private void addError(String message, final Exception e) {
		textResult.append(String.format("%s - exception message: %s\n", message , e.getMessage()));
	}

	protected void selectDistFileAWT() {
		FileDialog fd = new FileDialog(mainFrame, "Select Jar Folder",
				FileDialog.LOAD);
		fd.setAlwaysOnTop(true);
		System.setProperty("apple.awt.fileDialogForDirectories", "true");
		fd.setVisible(true);
		System.setProperty("apple.awt.fileDialogForDirectories", "false");
		String fs = fd.getFile();

		String fileName = (fs != null) ? fd.getDirectory() + fs : "";
		textJarDir.setText(fileName);
	}

	private void selectDistFileSwing() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		int option = chooser.showSaveDialog(mainFrame);
		if (option == JFileChooser.APPROVE_OPTION) {
			String fileName = (chooser.getSelectedFile() != null) ? chooser
					.getSelectedFile().getPath() : "";
			textJarDir.setText(fileName);
		}
	}

}
