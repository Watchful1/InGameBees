import com.sun.javaws.Globals;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

public class Main extends JFrame {

	public Main() {
		this.setTitle("InGameBees"); // Set the window title
		this.setPreferredSize(new Dimension(400, 400)); // and the initial size
		//this.setLayout(new BoxLayout(null, BoxLayout.Y_AXIS));

		JButton selectFolderButton = new JButton("Select Folder");
		selectFolderButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				runOperation();
			}
		});
		this.add(selectFolderButton);

		pack();
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setVisible(true);
	}

	public void runOperation() {
		JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home")); //TODO change this to the working folder
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fileChooser.showOpenDialog(null);
		File minecraftFolder;
		if (returnVal == JFileChooser.APPROVE_OPTION) minecraftFolder = fileChooser.getSelectedFile();
		else return;
		System.out.println("PING1");

		if(minecraftFolder.exists() && minecraftFolder.isDirectory() &&
				(minecraftFolder.getName().equals("minecraft") || minecraftFolder.getName().equals(".minecraft"))) {
			File dump = new File(minecraftFolder + File.separator + "dumps/biome.csv");
			if(dump.exists()) {
				System.out.println("PING2");
				String csv = readFile(dump);
				ArrayList<ArrayList<String>> biomes = new ArrayList<ArrayList<String>>();
				boolean first = true;
				for(String line : csv.split("\n")) {
					if(first) {
						first = false;
						continue;
					}
					ArrayList<String> temp = new ArrayList<String>();
					String[] items = line.split(",");
					temp.add(items[0]);
					temp.add(items[2]);
					temp.add(items[3]);
					temp.add(items[7]);
					biomes.add(temp);
				}
				System.out.println("PING3");
				StringBuilder bldr = new StringBuilder();
				bldr.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<config>\n" +
						"	<lines at=\"topleft\">\n		<line>\n			<operation>\n" +
						"				<str>EQ</str>\n				<var>biomeid</var>\n");
				for(ArrayList<String> biome : biomes) {
					bldr.append("				<num>");
					bldr.append(biome.get(0));
					bldr.append("</num>\n");
				}
				for(ArrayList<String> biome : biomes) {
					bldr.append("				<str>");
					bldr.append("Temp: ");
					bldr.append(Math.round(Float.parseFloat(biome.get(1)) * 100));
					bldr.append("%  Humd: ");
					bldr.append(Math.round(Float.parseFloat(biome.get(2)) * 100));
					bldr.append("%");
					bldr.append("</str>\n");
				}
				bldr.append("			</operation>\n		</line>\n	</lines>\n</config>");
				System.out.println("PING4");
				File config = new File(minecraftFolder+File.separator+"config/InGameInfo.xml");
				writeFile(bldr.toString(), config);
				System.out.println("PING5");
			}
		}
	}

	public static boolean writeFile(String string, File location) {
		if(!location.exists()) location.getParentFile().mkdirs();
		try{
			// Create file
			FileWriter fstream = new FileWriter(location);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(string);
			//Close the output stream
			out.close();
		} catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
			return false;
		}
		return true;
	}

	public static String readFile(File location) {
		if(!location.exists()) return null;
		BufferedReader br = null;
		StringBuilder bldr = new StringBuilder();
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(location));
			while ((sCurrentLine = br.readLine()) != null) {
				bldr.append(sCurrentLine+"\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return bldr.toString();
	}

	public static void main(String[] args) {
		new Main();
	}
}