import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.typography.hershey.HersheyFont;

public class Chapter1 {

    public static void main(String[] args) {

        //Create an image template
        MBFImage image = new MBFImage(600,100, ColourSpace.RGB);

        //Fill the image with white
        image.fill(RGBColour.WHITE);

        //Exercise 1: Playing with the sample application

        //Render the text passed as first parameter with the selected font
        image.drawText("BARBIE WORLD", 10, 60, HersheyFont.TIMES_MEDIUM_ITALIC, 60, RGBColour.PINK);
        image.drawText("life in plastic... it's fantastic!", 10, 90, HersheyFont.TIMES_MEDIUM_ITALIC, 30, RGBColour.PINK);

        //Apply a Gaussian blur
        image.processInplace(new FGaussianConvolve(2f));

        //Display the image
        DisplayUtilities.display(image);
    }
}
