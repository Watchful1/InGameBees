import com.sun.javaws.Globals;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

public class Main extends JFrame implements ActionListener {
	public boolean usePercent;
	public static String percentsString = "Display Percents";
	public static String levelsString = "Display Levels";
	public static String folderString = "Select Minecraft Folder";

	public Main() {
		this.setTitle("InGameBees"); // Set the window title

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
		usePercent = false;

		JRadioButton levelButton = new JRadioButton(levelsString);
		levelButton.setSelected(true);
		JRadioButton percentButton = new JRadioButton(percentsString);

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(levelButton);
		buttonGroup.add(percentButton);
		mainPanel.add(levelButton);
		mainPanel.add(percentButton);
		percentButton.addActionListener(this);
		levelButton.addActionListener(this);

		JButton selectFolderButton = new JButton(folderString);
		selectFolderButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				runOperation();
			}
		});
		mainPanel.add(selectFolderButton);

		this.add(mainPanel);
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

		if(minecraftFolder.exists() && minecraftFolder.isDirectory() &&
				(minecraftFolder.getName().equals("minecraft") || minecraftFolder.getName().equals(".minecraft"))) {
			File dump = new File(minecraftFolder + File.separator + "dumps/biome.csv");
			if(dump.exists()) {
				String csv = null;
				try {
					csv = readFile(dump);
				} catch (IOException e) {
					JOptionPane.showMessageDialog(this, "Found the biome data, but couldn't read it");
					return;
				}
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
					int firstQuote = line.indexOf("\"");
					int secondQuote = line.indexOf("\"", firstQuote + 1);
					if(firstQuote == -1 || secondQuote == -1) temp.add("");
					else temp.add(line.substring(firstQuote + 1, secondQuote - 1));
					biomes.add(temp);
				}
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
					bldr.append(tempHumdString(Math.round(Float.parseFloat(biome.get(1)) * 100), Math.round(Float.parseFloat(biome.get(2)) * 100), biome.get(3)));
					bldr.append("</str>\n");
				}
				bldr.append("			</operation>\n		</line>\n	</lines>\n</config>");
				File config = new File(minecraftFolder+File.separator+"config/InGameInfo.xml");
				try {
					writeFile(bldr.toString(), config);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this, "Could not write out the xml file");
					return;
				}
			} else {
				JOptionPane.showMessageDialog(this, "Couldn't find the biome data,\ndid you use NEI to dump it?");
			}
		} else {
			JOptionPane.showMessageDialog(this, "That doesn't look like a minecraft folder");
		}
	}

	public String tempHumdString(int temp, int humd, String attribs) {
		StringBuilder bldr = new StringBuilder();
		if(usePercent) {
			bldr.append("Temp: ");
			bldr.append(temp);
			bldr.append("% Humd: ");
			bldr.append(humd);
			bldr.append("%");
		} else {
			if(temp < 5) bldr.append("Icy");
			else if(temp < 20) bldr.append("Cold");
			else if(temp < 95) bldr.append("Normal");
			else if(temp < 200) bldr.append("Warm");
			else {
				if(attribs.toLowerCase().contains("nether")) bldr.append("Hellish");
				else bldr.append("Hot");
			}

			bldr.append(" / ");

			if(humd < 10) bldr.append("Arid");
			else if(humd < 90) bldr.append("Normal");
			else bldr.append("Damp");
		}
		return bldr.toString();
	}

	public static void writeFile(String string, File location) throws Exception {
		if(!location.exists()) location.getParentFile().mkdirs();
		try{
			// Create file
			FileWriter fstream = new FileWriter(location);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(string);
			//Close the output stream
			out.close();
		} catch (Exception e){//Catch exception if any
			throw e;
		}
	}

	public static String readFile(File location) throws IOException {
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
			throw e;
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				throw ex;
			}
		}
		return bldr.toString();
	}

	public static void main(String[] args) {
		new Main();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals(percentsString)) usePercent = true;
		else usePercent = false;
	}
}
