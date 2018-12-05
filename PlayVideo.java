import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.lang.Math.*;
import java.util.concurrent.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.awt.Shape;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.OverlayLayout;
import java.awt.event.*;

import javax.imageio.ImageIO;
import java.io.FileReader; 
import java.util.Iterator; 
import java.util.Map;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PlayVideo {

	private final int WIDTH = 352;
	private final int HEIGHT = 288;
	private final int MAX_FRAME = 9000;

	private byte[][] red = new byte[HEIGHT][WIDTH];
	private byte[][] green = new byte[HEIGHT][WIDTH];
	private byte[][] blue = new byte[HEIGHT][WIDTH];

	private int frameNumber;
	private JLabel lbIm1;
	private JLabel frameNumLbl;
	private JFrame frame;
	private GridBagConstraints c;
	static String folderName = null;
	private File file;
	static PlayVideo ren;
	private ScheduledExecutorService executor;
	private JButton importVideo;
	private JButton play;
	private JButton pause;
	private JButton stop;
	static PlaySound playSound;
	static Thread thread;
	private boolean threadIsAlive = false;

	public static void main(String[] args) {
		ren = new PlayVideo();
		
		thread = (new Thread(){
    		public void run(){
      			startSound();
    		}
		});
		
		ren.runApp();
	}

	public void runApp (){
		BufferedImage original, result;

		// Use labels to display the images
		frame = new JFrame();
		frame.setSize(500, 500);
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;

		/*
		//READ IN FROM JSON FILE HERE
		Object obj = new JSONParser().parse(new FileReader("NYOne.json"));
		
		JSONObject jo = (JSONObject) obj;
		
		
		String 
		*/

		importVideo = new JButton("Import Video");
		importVideo.setSize(40, 40);
		importVideo.setVisible(true);
		importVideo.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        openFile();
		        if (folderName != null) resetFrame();
		    }
		});
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 0;
		frame.getContentPane().add(importVideo, c);

		play = new JButton("Play");
		play.setSize(40, 40);
		play.setVisible(true);
		play.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        runVideo();
		    }
		});
		play.setEnabled(false);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 1;
		frame.getContentPane().add(play, c);

		pause = new JButton("Pause");
		pause.setSize(40, 40);
		pause.setVisible(true);
		pause.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        pauseVideo();
		    }
		});
		pause.setEnabled(false);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 2;
		frame.getContentPane().add(pause, c);

		stop = new JButton("Stop");
		stop.setSize(40, 40);
		stop.setVisible(true);
		stop.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        stopVideo();
		    }
		});
		stop.setEnabled(false);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 3;
		frame.getContentPane().add(stop, c);

		try {
			File image = new File("base.png");
			BufferedImage img = ImageIO.read(image);
			lbIm1 = new JLabel(new ImageIcon(img));
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = 1;
			frame.getContentPane().add(lbIm1, c);
		} catch (Exception e) {}

		frameNumLbl = new JLabel("Playing Frame " + Integer.toString(frameNumber));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 2;
		frame.getContentPane().add(frameNumLbl, c);

		frame.pack();
		frame.setVisible(true);
	}

	private void resetFrame () {
		frameNumber = 0;
		frame.getContentPane().remove(lbIm1);
		frame.getContentPane().remove(frameNumLbl);
	    String filename = folderName.substring(folderName.lastIndexOf("/") + 1) + String.format("%04d", frameNumber+1) + ".rgb";
		processImgFile(new File(folderName + "/" + filename));

	    BufferedImage img = processImgFile(new File(folderName + "/" + filename));
		lbIm1 = new JLabel(new ImageIcon(img))/* {
			@Override
			public void paintComponent(Graphics g){
				super.paintComponent(g);
				g.drawRect(10, 10, 100, 100);
			}
		}*/;

		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbIm1, c);

		frameNumLbl = new JLabel("Playing Frame " + Integer.toString(frameNumber));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 2;
		frame.getContentPane().add(frameNumLbl, c);

		frame.pack();
		frame.setVisible(true);
		frame.repaint();
	}

	private void openFile () {
        try {
        	JFileChooser folderChooser = new JFileChooser();

	        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		    // disable the "All files" option.
		    folderChooser.setAcceptAllFileFilterUsed(false);

		    if (folderChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
		    	play.setEnabled(true);
		    	folderName = folderChooser.getSelectedFile().getAbsolutePath();
		    	String soundFilename = folderName + "/" + folderName.substring(folderName.lastIndexOf("/") + 1) + ".wav";

				FileInputStream inputStream;
				inputStream = new FileInputStream(soundFilename);

				// initializes the playSound Object
				playSound = new PlaySound(inputStream);
				playSound.initSoundFile();
		    }
		} catch (Exception e) {
			openFile();
		}
    }

	private void runVideo () {
		importVideo.setEnabled(false);
		play.setEnabled(false);
		pause.setEnabled(true);
		stop.setEnabled(true);
		
		if (threadIsAlive) startSound();
		else {
			thread.start();
			threadIsAlive = true;
		}

		executor = Executors.newScheduledThreadPool(1);
		Runnable runnable1 = new Runnable() {
		    public void run() {
		    	try {
			    	frame.getContentPane().remove(lbIm1);
			    	frame.getContentPane().remove(frameNumLbl);
	        		String filename = folderName.substring(folderName.lastIndexOf("/") + 1) + String.format("%04d", frameNumber+1) + ".rgb";
					processImgFile(new File(folderName + "/" + filename));

	        		BufferedImage img = processImgFile(new File(folderName + "/" + filename));
					lbIm1 = new JLabel(new ImageIcon(img))/* {
							@Override
							public void paintComponent(Graphics g){
								super.paintComponent(g);
								g.drawRect(10, 10, 100, 100);
							}
					}*/;

					c.gridx = 0;
					c.gridy = 1;
					frame.getContentPane().add(lbIm1, c);

					frameNumLbl = new JLabel("Playing Frame " + Integer.toString(frameNumber));
					c.fill = GridBagConstraints.HORIZONTAL;
					c.gridx = 0;
					c.gridy = 2;
					frame.getContentPane().add(frameNumLbl, c);

					frame.pack();
					frame.setVisible(true);
					frame.repaint();
					frameNumber++;

					if (frameNumber == 9000) executor.shutdown();
				} catch (ArrayIndexOutOfBoundsException e) {}
		    }
		};
		
		// draws images with 30fps
		executor.scheduleAtFixedRate(runnable1, 0, 33, TimeUnit.MILLISECONDS);
		//executor.shutdown();
	}

	static void startSound () {
		try {
			playSound.play();
		} catch (Exception e) {}
	}

	// Read image file and process data
	private BufferedImage processImgFile (File file) {
		FileInputStream fileInputStream = null;
		byte[] stream = new byte[(int) file.length()];
		BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		int baseIdx;
		
		try {
			//convert file into byte array
			fileInputStream = new FileInputStream(file);
			fileInputStream.read(stream);
			fileInputStream.close();
		}
		catch (Exception e) {}

		// Save each R, G, and B values of image in byte
		for(int y = 0; y < HEIGHT; y++) {
			for(int x = 0; x < WIDTH; x++) {
				baseIdx = x + WIDTH * y;

				red[y][x] = stream[baseIdx];
				green[y][x] = stream[baseIdx + (HEIGHT * WIDTH)];
				blue[y][x] = stream[baseIdx + 2 * (HEIGHT * WIDTH)];

				int pix = 0xff000000 | ((red[y][x] & 0xff) << 16) | ((green[y][x] & 0xff) << 8) | (blue[y][x] & 0xff);
				img.setRGB(x, y, pix);
			}
		}

		return img;
	}

	private void pauseVideo () {
    	executor.shutdown();

    	importVideo.setEnabled(true);
    	play.setEnabled(true);
    	pause.setEnabled(false);
    	stop.setEnabled(false);

    	try {
    		playSound.pause();
    	} catch(Exception e) {}
    }

    private void stopVideo () {
    	//resetFrame();

    	executor.shutdown();

    	importVideo.setEnabled(true);
    	play.setEnabled(true);
    	pause.setEnabled(false);
    	stop.setEnabled(false);

    	//resetFrame();
    	frameNumber = 0;

    	try {
	    	playSound.stop();
	    	playSound.initSoundFile();
	    } catch (Exception e) {}
    }
}
