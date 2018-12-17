import java.awt.image.*;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.*;
import org.opencv.imgproc.Imgproc;

public class Videoopencv {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	static Mat imag = null;

	public static BufferedImage Mat2BufferedImage(Mat m) {

		int type = BufferedImage.TYPE_BYTE_GRAY;
		if (m.channels() > 1) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		int bufferSize = m.channels() * m.cols() * m.rows();
		byte[] b = new byte[bufferSize];
		m.get(0, 0, b); // get all the pixels
		BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(b, 0, targetPixels, 0, b.length);
		return image;

	}

	public static void main(String[] args) throws IOException {
		JFrame jframe = new JFrame("SkinnyCodes - George Qupty");
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JLabel vidpanel = new JLabel();
		jframe.setContentPane(vidpanel);
		jframe.setLocationByPlatform(true);
		jframe.setSize(800, 800);
		jframe.setVisible(true);

		BufferedImage img = ImageIO.read(new File("images/net.png"));
		ImageIcon icon = new ImageIcon(img);

		jframe.setLayout(new FlowLayout());
		JLabel lbl = new JLabel();

		lbl.setIcon(icon);
		lbl.setBorder(new EmptyBorder(0, 90, 410, 90));
		jframe.add(lbl);

		
		jframe.setVisible(true);
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Mat frame = new Mat();
		Mat outerBox = new Mat();
		Mat diff_frame = null;
		Mat tempon_frame = null;
		ArrayList<Rect> array = new ArrayList<Rect>();
		VideoCapture camera = new VideoCapture(0);

		Size sz = new Size(800, 800);
		int i = 0;

		final long PERIOD = 200; // Adjust to suit timing
		long lastTime = System.currentTimeMillis() - PERIOD;

		while (true) {
			if (camera.read(frame)) {
				Imgproc.resize(frame, frame, sz);
				imag = frame.clone();
				outerBox = new Mat(frame.size(), CvType.CV_8UC1);
				Imgproc.cvtColor(frame, outerBox, Imgproc.COLOR_BGR2GRAY);
				Imgproc.GaussianBlur(outerBox, outerBox, new Size(3, 3), 0);

				if (i == 0) {
					jframe.setSize(frame.width(), frame.height());
					diff_frame = new Mat(outerBox.size(), CvType.CV_8UC1);
					tempon_frame = new Mat(outerBox.size(), CvType.CV_8UC1);
					diff_frame = outerBox.clone();
				}

				if (i == 1) {
					Core.subtract(outerBox, tempon_frame, diff_frame);
					Imgproc.adaptiveThreshold(diff_frame, diff_frame, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
							Imgproc.THRESH_BINARY_INV, 5, 2);

					long thisTime = System.currentTimeMillis();
					array = detection_contours(diff_frame);
					if (array.size() > 0 && (thisTime - lastTime) >= PERIOD) {
						lastTime = thisTime;
						Iterator<Rect> it2 = array.iterator();
						// while (it2.hasNext() ) {
						Rect obj = it2.next();

						if (obj.x > 200 && obj.x <= 320 && obj.y > 0 && obj.y < 120) {
							System.out.println("You pressed 1");
						} else if (obj.x > 320 && obj.x <= 440 && obj.y > 0 && obj.y < 120) {
							System.out.println("You pressed 2");
						} else if (obj.x > 440 && obj.x <= 560 && obj.y > 0 && obj.y < 120) {
							System.out.println("You pressed 3");
						}

						else if (obj.x > 200 && obj.x <= 320 && obj.y > 120 && obj.y < 240) {
							System.out.println("You pressed 4");
						} else if (obj.x > 320 && obj.x <= 440 && obj.y > 120 && obj.y < 240) {
							System.out.println("You pressed 5");
						} else if (obj.x > 440 && obj.x <= 560 && obj.y > 120 && obj.y < 240) {
							System.out.println("You pressed 6");
						}

						else if (obj.x > 200 && obj.x <= 320 && obj.y > 240 && obj.y < 360) {
							System.out.println("You pressed 7");
						} else if (obj.x > 320 && obj.x <= 440 && obj.y > 240 && obj.y < 360) {
							System.out.println("You pressed 8");
						} else if (obj.x > 440 && obj.x <= 560 && obj.y > 240 && obj.y < 360) {
							System.out.println("You pressed 9");
						}

						// }
					}
				}

				i = 1;

				ImageIcon image = new ImageIcon(Mat2BufferedImage(imag));
				vidpanel.setIcon(image);
				vidpanel.repaint();
				tempon_frame = outerBox.clone();
			}
		}
	}

	public static ArrayList<Rect> detection_contours(Mat outmat) {
		Mat v = new Mat();
		Mat vv = outmat.clone();
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(vv, contours, v, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

		int maxAreaIdx = -1;
		Rect r = null;
		ArrayList<Rect> rect_array = new ArrayList<Rect>();
		for (int idx = 0; idx < contours.size(); idx++) {
			Mat contour = contours.get(idx);
			double contourarea = Imgproc.contourArea(contour);
			if (contourarea > 100) {
				maxAreaIdx = idx;
				r = Imgproc.boundingRect(contours.get(maxAreaIdx));
				rect_array.add(r);
			}
		}
		v.release();
		return rect_array;
	}
}