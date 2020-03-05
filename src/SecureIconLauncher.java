import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * @author Veda
 *
 */
public class SecureIconLauncher{
	
	public static void main(String [] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		addTrayIcon();
		
		
		JFrame frame= new JFrame();
		JLabel certificate = new JLabel();
		certificate.setIcon(new ImageIcon(SecureIconLauncher.class.getResource("certificate.png")));
		certificate.setVisible(false);
		JButton icon = new JButton(new ImageIcon(SecureIconLauncher.class.getResource("secure.png")));
		icon.setBorderPainted(false);
		
		updateIconLocation(certificate, icon);
			

		try {
			icon.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("button pressed");
					System.out.println("Certificate is located at: " + certificate.getX() + ", " + certificate.getY());
					if(certificate.isVisible())
					{
						certificate.setVisible(false);
					} else {
						certificate.setVisible(true);
					}
				}
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
		//hello world label. APPARENTLY THIS IS NEEDED OR CODE BUGS (i think it is becuase there must be at least one object on the screen?)
		// TODO can be removed at end.
		JLabel testLabel = new JLabel(" ");
		testLabel.setSize(100, 30);
		testLabel.setBounds(150, 100, 100, 30);
		testLabel.setForeground(Color.GREEN);
		
		frame.add(testLabel);

		//adding the components to the screen
		frame.add(icon);
		frame.add(certificate);
		
		//full screen
		setFrameProperties(frame);
		
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				updateIconLocation(certificate, icon);
			}
		};
		
		ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
		executorService.scheduleAtFixedRate(runnable, 0, 3, TimeUnit.SECONDS);
	}
	
	
	private static void updateIconLocation(JLabel certificate, JButton icon)
	{
		Mat secureIcon = Imgcodecs.imread("./res/secure.png");
		
		
		Dimension location = new Dimension();
		try {
			location = templateMatch(Imgcodecs.imread("./res/secure.png"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		icon.setBounds((int) location.getWidth(), (int) location.getHeight() , 15, 15); //change size of this icon based on screen, need to find exact size.
		certificate.setBounds((int) location.getWidth(), (int) location.getHeight() + secureIcon.height(), 320, 310);
		
		System.out.println("Target is found at: " + location.getWidth() + ", " + location.getHeight());
	}
	
	
	private static void setFrameProperties(JFrame frame)
	{
		//full screen
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setSize((int) screenSize.getWidth(), (int) screenSize.getHeight());
		//System.out.println(screenSize.getWidth() + ", " + screenSize.getHeight());
		
		//removes title bar
		frame.setUndecorated(true);
		
		
		frame.getContentPane().setBackground(new Color(1.0f,1.0f,1.0f,0.0f));
		frame.setBackground(new Color(1.0f,1.0f,1.0f,0.0f));
		frame.setAlwaysOnTop(true);
		frame.setType(javax.swing.JFrame.Type.UTILITY);
		
		//added this line to allow certificate to freeflow.
		frame.setLayout(null);
		
		frame.setVisible(true);
	}
	
	private static void addTrayIcon()
	{
		//hide the icon into system tray, change secure icon to be invisible
		TrayIcon trayIcon = null;
		if (SystemTray.isSupported()) {
		    SystemTray tray = SystemTray.getSystemTray();
		    //not working yet??
		    Image image = Toolkit.getDefaultToolkit().getImage("./res/emptyIcon.png");

		    
		    ActionListener clickListener = new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		            System.exit(0);
		        }
		    };
		    // create a popup menu
		    PopupMenu popup = new PopupMenu();
		    // create menu item for the default action
		    MenuItem closeOption = new MenuItem("Close");
		    closeOption.addActionListener(clickListener);
		    popup.add(closeOption);

		    
		    trayIcon = new TrayIcon(image, "Tray Demo", popup);

		    trayIcon.addActionListener(clickListener);

		    try {
		        tray.add(trayIcon);
		    } catch (AWTException e) {
		        System.err.println(e);
		    }

		} else {
		    System.exit(0); //system tray not supported
		}
		
	}
	
	/**
	 * method used to get the location of the current insecure icon.
	 * @return
	 * @throws AWTException
	 */
	private static Dimension templateMatch(Mat inputImg) throws AWTException
	{
		BufferedImage screenshot = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
		//ImageIO.write(screenshot, "png", new File("./screenshot.png"));
		
		Mat screenshotMatrix = bufferedImageToMat(screenshot);
		Mat resImg = new Mat(screenshotMatrix.rows(), screenshotMatrix.cols(), CvType.CV_32FC1);

		
		//template match
		Imgproc.matchTemplate(screenshotMatrix, inputImg, resImg, Imgproc.TM_SQDIFF_NORMED); //useminLoc
		
		//obtain result x y dimension on screen
		Core.normalize(resImg, resImg, 0, 1, Core.NORM_MINMAX, -1, new Mat());
		Core.MinMaxLocResult mmr = Core.minMaxLoc(resImg);
		
		Dimension result = new Dimension((int) mmr.minLoc.x, (int) mmr.minLoc.y);
		return result;
	}
	
	private static Mat bufferedImageToMat(BufferedImage inputImg)
	{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			ImageIO.write(inputImg, "jpg", byteArrayOutputStream);
			byteArrayOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Mat result = Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.IMREAD_UNCHANGED);
		return result;	
	}	
}
