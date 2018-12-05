package application;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.lang.Math.*;
//import java.lang.*;
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

	private BufferedImage[] video = new BufferedImage[MAX_FRAME];
	private int frameNumber;
	private ArrayList<Integer> BBoxindex;
	private int mX;
	private int mY;
	private boolean mclick; 
	private JLabel lbIm1;
	private JFrame frame;
	private GridBagConstraints c;
	static String folderName = null;
	private File file;
	static PlayVideo ren;
	private ScheduledExecutorService executor;
	private boolean onPlay = false;
	private JButton importVideo;
	private JButton play;
	private JButton pause;
	private JButton stop;
	static PlaySound playSound;
	static Thread thread;

	public static void main(String[] args) {
		ren = new PlayVideo();
		thread = (new Thread(){
    		public void run(){
      			startSound();
    		}
		});
		ren.showImgs();
	}

	public void showImgs(){
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
		    	if (folderName != null) resetFrame();
		        openFile();
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
			//System.out.println("Working Directory = " +
		             // System.getProperty("user.dir"));
			//System.out.println(image.getPath());
			BufferedImage img = ImageIO.read(image);
			//System.out.println("OK2");
			lbIm1 = new JLabel(new ImageIcon(img));
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = 1;
			frame.getContentPane().add(lbIm1, c);
		} catch (Exception e) {}

		frame.pack();
		frame.setVisible(true);
	}

	private void resetFrame () {
		frame.getContentPane().remove(lbIm1);
	}

	private void openFile () {
        try {
        	JFileChooser folderChooser = new JFileChooser();

	        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		    // disable the "All files" option.
		    folderChooser.setAcceptAllFileFilterUsed(false);

		    if (folderChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
		    	frameNumber = 0;
		    	play.setEnabled(true);
		    	folderName = folderChooser.getSelectedFile().getAbsolutePath();
		    	String soundFilename = folderName + "\\" + folderName.substring(folderName.lastIndexOf("\\") + 1) + ".wav";

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

    private void pauseVideo () {
    	executor.shutdown();
    	importVideo.setEnabled(true);
    	play.setEnabled(true);
    	pause.setEnabled(false);
    	stop.setEnabled(false);
    	try {
    		//thread.wait();
    		//Thread.sleep(4000);
    		playSound.stop();
    	} catch(Exception e) {}
    }

    private void stopVideo () {
    	executor.shutdown();
    	frameNumber = 0;
    	importVideo.setEnabled(true);
    	play.setEnabled(true);
    	pause.setEnabled(false);
    	stop.setEnabled(false);
    }

	private void runVideo () {
		importVideo.setEnabled(false);
		play.setEnabled(false);
		pause.setEnabled(true);
		stop.setEnabled(true);
		
		//Access the JSON file 
		JSONParser jsonParser = new JSONParser();
		JSONArray jsonArray = new JSONArray();

		Object obj = new Object();
		
		//System.out.println("Entering try of JSON file!!");
		//System.out.println(folderName.substring(folderName.lastIndexOf("\\") + 1) + ".json");
		
		try{
			obj = jsonParser.parse(new FileReader(folderName + "\\" + folderName.substring(folderName.lastIndexOf("\\") + 1) + ".json"));
			System.out.println("Successfully loaded the JSON file");
			jsonArray = (JSONArray) obj;			
		}
		catch (Exception e) {}
		
		//System.out.println("Done with try catch of JSON file!!");
		
		//JSON FILE DATA in 2D array
		ArrayList<ArrayList<String>> jsonData = new ArrayList<ArrayList<String>>();
		
		
		if (jsonArray != null){
			Iterator it = jsonArray.iterator();
			while(it.hasNext()){
					JSONObject jsonObject = (JSONObject) it.next();
					Iterator dataIterator = jsonObject.entrySet().iterator();
					for(Iterator iterator = jsonObject.keySet().iterator(); iterator.hasNext();) {
						
						ArrayList<String> interData = new ArrayList<String>();
						Object jsonobj = iterator.next();
						String key = (String) jsonobj;
						interData.add(key);
						//linkNames.add(key);
						//System.out.println("Link name: " + key);
						
						String data = dataIterator.next().toString();
						//System.out.println("Data: "+data);
						
						//ArrayList<String> temp = data.split("=");
						String[] temp = data.split("=");
						//System.out.println("Temp[1] before split: "+ temp[1]);
						String[] splitdata = temp[1].split(",");
						//System.out.println("Splitdata: "+ splitdata);
						
						
						for(int i=0; i<splitdata.length; ++i) {
							String[] temp1 = splitdata[i].split(":");
							//System.out.println(temp1[0] + " -- " + temp1[1]);
							interData.add(temp1[1]);
						}
						
						jsonData.add(interData);
					}
			}
			
			
		}
		
	
		/*
		System.out.println("Printing data stored in array");
		//Just Checking if data has been stored properly
		for(int i=0; i<jsonData.size(); ++i) {
			for(int j=0; j < jsonData.get(i).size() ; ++j) {
				System.out.println(jsonData.get(i).get(j));
				
			}
			System.out.println("Next Iteration");
			
		}
		*/
		
		thread.start();
		
		
		
		executor = Executors.newScheduledThreadPool(1);
		Runnable runnable1 = new Runnable() {
		    public void run() {
		    	try {
		    		System.out.println(frameNumber);
			    	//frame.getContentPane().removeAll();
			    	//if (frameNumber != 0) frame.getContentPane().remove(lbIm1);
			    	frame.getContentPane().remove(lbIm1);
			    	//System.out.println("Hi");
	        		String filename = folderName.substring(folderName.lastIndexOf("\\") + 1) + String.format("%04d", frameNumber+1) + ".rgb";
					//System.out.println("Hi");
	        		//processImgFile(new File(folderName + "\\" + filename));

	        		BufferedImage img = processImgFile(new File(folderName + "\\" + filename));
	        		
	        		boolean BBpresent = false;
	        		BBoxindex = new ArrayList<Integer>();
	        		for(int i=0; i < jsonData.size(); ++i) {
	        			//String start = jsonData.get(i).get(7).substring(12, 16);
	        			int start = Integer.parseInt(jsonData.get(i).get(7).substring(12, 16));
	        			//String end = jsonData.get(i).get(5).substring(12,  16);
	        			int end = Integer.parseInt(jsonData.get(i).get(5).substring(12, 16));
	        			
	        			//System.out.println("Start: "+start+"-- End:" +end);
	        			if(frameNumber >= start && frameNumber <= end) {
	        				//System.out.println("BB PRESENT!!!");
	        				BBpresent = true;
	        				BBoxindex.add(i);
	        			}
	        		}
	        		
	        		if(BBpresent) {
	        			//System.out.println("PRINTING BOUNDING BOX!!!");
	        			lbIm1 = new JLabel(new ImageIcon(img)) {
								@Override
								public void paintComponent(Graphics g){
									super.paintComponent(g);
									
									for(int i=0; i< BBoxindex.size(); ++i) {
										int x = Integer.parseInt(jsonData.get(BBoxindex.get(i)).get(2).substring(0, jsonData.get(BBoxindex.get(i)).get(2).length() - 2 ));
										int y = Integer.parseInt(jsonData.get(BBoxindex.get(i)).get(3).substring(0, jsonData.get(BBoxindex.get(i)).get(3).length() - 2 ));
										int w = Integer.parseInt(jsonData.get(BBoxindex.get(i)).get(6).substring(0, jsonData.get(BBoxindex.get(i)).get(6).length() - 2 ));
										int h = Integer.parseInt(jsonData.get(BBoxindex.get(i)).get(4).substring(0, jsonData.get(BBoxindex.get(i)).get(4).length() - 2 ));
										//System.out.println("X: "+x+" Y: "+y+" W: "+w+" H: "+h);
										g.drawRect(x, y, w, h);
									}
									
								}
						};
	        		}
	        		else
	        		{
	        			lbIm1 = new JLabel(new ImageIcon(img));;
	        		}

					//lbIm1.setLayout(new BorderLayout());
					//c.fill = GridBagConstraints.HORIZONTAL;
					c.gridx = 0;
					c.gridy = 1;
					frame.getContentPane().add(lbIm1, c);	

					frame.pack();
					frame.setVisible(true);
					frame.repaint();
					
					//Retrieve mouse click coordinates
					//mclick = false;
					//mX = -1;
					//mY = -1;
					lbIm1.addMouseListener(new MouseAdapter() {
						@Override 
						public void mousePressed(MouseEvent e) {
							mX = e.getX(); 
							mY = e.getY();
							//mclick = true;
							System.out.println(mX + " , " + mY);
						}
					});
					//System.out.println("Follow up: " +x + "-" + y);
					
					
					//Run through all bounding box values and check if x,y inside any bounding box
					
					//System.out.println("RECORDED CLICK!!");
					for(int i=0; i<BBoxindex.size(); ++i) {
						int BBx = Integer.parseInt(jsonData.get(BBoxindex.get(i)).get(2).substring(0, jsonData.get(BBoxindex.get(i)).get(2).length() - 2 ));
						int BBy = Integer.parseInt(jsonData.get(BBoxindex.get(i)).get(3).substring(0, jsonData.get(BBoxindex.get(i)).get(3).length() - 2 ));
						int BBw = Integer.parseInt(jsonData.get(BBoxindex.get(i)).get(6).substring(0, jsonData.get(BBoxindex.get(i)).get(6).length() - 2 ));
						int BBh = Integer.parseInt(jsonData.get(BBoxindex.get(i)).get(4).substring(0, jsonData.get(BBoxindex.get(i)).get(4).length() - 2 )); 
						if( BBx < mX && BBy < mY && (BBx + BBw) > mX && (BBy + BBh) > mY){
							System.out.println("Play secondary video NOW: "+jsonData.get(BBoxindex.get(i)).get(1));
						}
					}	
						
					
					
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
		} catch (PlayWaveException e) {
			e.printStackTrace();
			return;
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
}
