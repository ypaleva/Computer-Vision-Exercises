import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.statistics.HistogramModel;
import org.openimaj.math.statistics.distribution.MultidimensionalHistogram;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Chapter4 {

    public static void main(String[] args) throws IOException, IOException {

        //Creating an array of image URLs
        URL[] imageURLs = new URL[]{
                new URL("http://openimaj.org/tutorial/figs/hist1.jpg"),
                new URL("http://openimaj.org/tutorial/figs/hist2.jpg"),
                new URL("http://openimaj.org/tutorial/figs/hist3.jpg")
        };

        //An image histogram holds counts for any number of colours in any number of dimensions into “bins”
        List<MultidimensionalHistogram> histograms = new ArrayList<>(); //This list holds the histograms for each image in the array above

        //A histogram model with 4 bins required for each dimension (3 for the red, green and blue values of a pixel)
        HistogramModel model = new HistogramModel(4, 4, 4);

        //Generating and storing the histograms for each image from the array
        for (URL u : imageURLs) {
            model.estimateModel(ImageUtilities.readMBF(u));
            histograms.add(model.histogram.clone());
        }


        //Going through each pair of images and comparing their histograms using the Euclidean distance measure

        for (int i = 0; i < histograms.size(); i++) {
            for (int j = i; j < histograms.size(); j++) {
                double distance = histograms.get(i).compare(histograms.get(j), DoubleFVComparison.EUCLIDEAN);
                if (i == j) {
                    System.out.println("Distance of image " + i + " from itself: " + distance);
                } else {
                    System.out.println("Distance of image " + i + " from image " + j + " : " + distance);
                }
            }
        }

        //Exercise 1: Finding and displaying similar images

        /*

        Results:

        Distance of image 0 from itself: 0.0
        Distance of image 0 from image 1 : 0.3504197936957126
        Distance of image 0 from image 2 : 0.5969851199618375
        Distance of image 1 from itself: 0.0
        Distance of image 1 from image 2 : 0.6611774763580472
        Distance of image 2 from itself: 0.0


        Which images are most similar?
        - The first and second images shown on the page are most similar (excluding comparison
          of an image with itself which results in 0.0),
          matching with what I expected looking at the images (Distance of image 1 (index 0)
          from image 2 (index 1) : 0.3504197936957126 is the smallest)
        - The code below displays the two most similar images using DoubleFVComparison.EUCLIDEAN

        */


        //This code is for exercise 2:
        //Depending on the comparison technique used, the two most similar images are displayed
        double minDistance = Double.MAX_VALUE;
        int first = 0;
        int second = 0;
        for (int i = 0; i < histograms.size(); i++) {
            for (int j = i; j < histograms.size(); j++) {
                if (i != j) {
                    double distance = histograms.get(i).compare(histograms.get(j), DoubleFVComparison.EUCLIDEAN);
                    if (distance < minDistance) {
                        minDistance = distance;
                        first = i;
                        second = j;
                    }
                }
            }
        }


        MBFImage firstImage = ImageUtilities.readMBF(imageURLs[first]);
        MBFImage secondImage = ImageUtilities.readMBF(imageURLs[second]);
        DisplayUtilities.display(firstImage, "First image");
        DisplayUtilities.display(secondImage, "Second image");

        /*
        Exercise 2: Exploring comparison measures

          - When using DoubleFVComparison.INTERSECTION and DoubleFVComparison.COSINE_SIM
            the two most similar images are images 2 and 3 as shown on the page
            (referring to images by number shown on OpenIMAJ site, not by index)

          - When using DoubleFVComparison.SUM_SQUARE, DoubleFVComparison.CHI_SQUARE and
            DoubleFVComparison.BHATTACHARYYA the two most similar images are again 1 and 2 (as EUCLIDEAN)

        */
    }

}
