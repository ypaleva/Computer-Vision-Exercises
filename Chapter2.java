import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.shape.Ellipse;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;


public class Chapter2 {

    public static void main( String[] args ) throws IOException {

        //Exercise 1: DisplayUtilities

        //Creating a reusable named display with a title using a JFrame instance
        JFrame jFrame = DisplayUtilities.createNamedWindow("My OpenIMAJ Gallery");

        //To read and write images we use the ImageUtilities class: readMBF function reads an image
        MBFImage image = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/sinaface.jpg"));

        System.out.println("Colour space: " + image.colourSpace);

        //Displaying the image we have loaded (in the reusable JFrame)
        DisplayUtilities.display(image, jFrame);

        //Displaying only the red channel of the image
        DisplayUtilities.display(image.getBand(0), jFrame);


        //Cloning the original image, so it is not modified (for future reuse)
        MBFImage clone = image.clone();

        //Going through all pixels in image one by one to set the blue and green pixels to black
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                clone.getBand(1).pixels[y][x] = 0;
                clone.getBand(2).pixels[y][x] = 0;
            }
        }
        DisplayUtilities.display(clone, jFrame);

        //The fill() function does exactly the same as the two for-loops above
        clone.getBand(1).fill(0f);
        clone.getBand(2).fill(0f);
        DisplayUtilities.display(clone, jFrame);

        //Using the Canny Edge Detector on a new image:
        MBFImage edgeDetectionImage = image.clone();
        edgeDetectionImage.processInplace(new CannyEdgeDetector());
        DisplayUtilities.display(edgeDetectionImage, jFrame);

        //Using the Image class drawing functions to draw figures with text on image
        MBFImage imageWithText = image.clone();
        imageWithText.drawShapeFilled(new Ellipse(700f, 450f, 20f, 10f, 0f), RGBColour.WHITE);
        imageWithText.drawShapeFilled(new Ellipse(650f, 425f, 25f, 12f, 0f), RGBColour.WHITE);
        imageWithText.drawShapeFilled(new Ellipse(600f, 380f, 30f, 15f, 0f), RGBColour.WHITE);
        imageWithText.drawShapeFilled(new Ellipse(500f, 300f, 100f, 70f, 0f), RGBColour.WHITE);
        imageWithText.drawText("OpenIMAJ is", 425, 300, HersheyFont.ASTROLOGY, 20, RGBColour.BLACK);
        imageWithText.drawText("Awesome", 425, 330, HersheyFont.ASTROLOGY, 20, RGBColour.BLACK);
        DisplayUtilities.display(imageWithText, jFrame);

        //Exercise 2: Drawing

        //The drawShapeFilled method draws an ellipse figure filled with white,
        //while the drawShape method is used to draw an ellipse with pink (no fill)
        //When the second shape is placed over the first we get a white ellipse with a pink border
        MBFImage imageWithBorder = image.clone();
        imageWithBorder.drawShapeFilled(new Ellipse(700f, 450f, 20f, 10f, 0f), RGBColour.WHITE);
        imageWithBorder.drawShape(new Ellipse(700f, 450f, 20f,10f,0f), 5, RGBColour.PINK);
        imageWithBorder.drawShapeFilled(new Ellipse(650f, 425f, 25f, 12f, 0f), RGBColour.WHITE);
        imageWithBorder.drawShape(new Ellipse(650f, 425f, 25f, 12f, 0f), 5, RGBColour.PINK);
        imageWithBorder.drawShapeFilled(new Ellipse(600f, 380f, 30f, 15f, 0f), RGBColour.WHITE);
        imageWithBorder.drawShape(new Ellipse(600f, 380f, 30f, 15f, 0f), 5, RGBColour.PINK);
        imageWithBorder.drawShapeFilled(new Ellipse(500f, 300f, 100f, 70f, 0f), RGBColour.WHITE);
        imageWithBorder.drawShape(new Ellipse(500f, 300f, 100f, 70f, 0f), 5 ,RGBColour.PINK);

        //The text on the image is also set to pink
        imageWithBorder.drawText("OpenIMAJ is", 425, 300, HersheyFont.ASTROLOGY, 20, RGBColour.PINK);
        imageWithBorder.drawText("Awesome", 425, 330, HersheyFont.ASTROLOGY, 20, RGBColour.PINK);
        DisplayUtilities.display(imageWithBorder, jFrame);

    }
}
