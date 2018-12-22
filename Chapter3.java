import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.connectedcomponent.GreyscaleConnectedComponentLabeler;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.processor.PixelProcessor;
import org.openimaj.image.segmentation.FelzenszwalbHuttenlocherSegmenter;
import org.openimaj.image.segmentation.SegmentationUtilities;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.FloatKMeans;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class Chapter3 {

    public static void main(String[] args) throws IOException {

        //Reading an image from URL
        MBFImage image = ImageUtilities.readMBF(new URL("https://scontent-lhr3-1.cdninstagram.com/vp/b0ae9ae8c79ba287989fad548763535c/5C78C45D/t51.2885-15/e35/35324506_213131365971383_2531439645359603712_n.jpg"));
        DisplayUtilities.display(image, "Original image");

        //Applying colour-space transform
        image = ColourSpace.convert(image, ColourSpace.CIE_Lab);


        //Construct K-means algorithm with 4 clusters with 30 iterations (default) - add second int parameter to set #iterations
        FloatKMeans cluster = FloatKMeans.createExact(4);

        //Flatten the pixels of an image into an array of floating point vectors
        float[][] imageData = image.getPixelVectorNative(new float[image.getWidth() * image.getHeight()][3]);

        //Grouping the pixels into the selected number of classes
        FloatCentroidsResult result = cluster.cluster(imageData);

        //Printing the coordinates of each centroid (average location of all the points belonging to the class)
        final float[][] centroids = result.getCentroids();
        for (float[] fs : centroids) {
            System.out.println(Arrays.toString(fs));
        }

        //Class HardAssigner assigns each pixel in the image to its respective class using the centroids
        final HardAssigner<float[], ?, ?> assigner = result.defaultHardAssigner();

        //Going through each pixel one by one and assigning it the class it belongs to

        /*
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                float[] pixel = image.getPixelNative(x, y);
                int centroid = assigner.assign(pixel);
                image.setPixelNative(x, y, centroids[centroid]);
            }
        }
        */

        //Exercise 1: The PixelProcessor
        //Processing the image in place (substitutes the above outer for loops)

        image.processInplace(new PixelProcessor<Float[]>() {
            @Override
            public Float[] processPixel(Float[] current) {

                //each pixel vector is now traversed in order to replace its values
                float[] f = new float[current.length];
                for (int i = 0; i < current.length; i++) {
                    f[i] = current[i];
                }

                int centroid = assigner.assign(f);

                //replacing the current pixel with the centroid of its respective class
                Float[] ans = new Float[f.length];
                for (int i = 0; i < centroids[centroid].length; i++) {
                    ans[i] = centroids[centroid][i];
                }
                return ans;
            }
        });

        //What are the advantages and disadvantages of using a PixelProcessor?

        /*
            - The main advantage of PixelProcessor is that the image can be processed in place:
              we don't need to loop over the pixel matrix with 2 for loops, instead we use one line ->
              the code is more generalised, which helps for code readability and maintainability

            - A disadvantage would be that if someone is new to the OpenIMAJ library and is not familiar with
              the PixelProcessor class and processInPlace methods they will need to browse the documentation
              to understand the code better
         */


        //Converting the image back to RGB colour space (for displaying purposes)
        image = ColourSpace.convert(image, ColourSpace.RGB);
        DisplayUtilities.display(image);


        //A segment is a group of neighbour pixels with the same class
        //A set of pixels representing a segment is a connected component
        //GreyscaleConnectedComponentLabeler class finds the connected components
        GreyscaleConnectedComponentLabeler labeler = new GreyscaleConnectedComponentLabeler();

        //flatten() method on MBFImage merges the colours into grey values by averaging their RGB values
        List<ConnectedComponent> components = labeler.findComponents(image.flatten());

        //Exercise 2: A real segmentation algorithm

        /*
        - Takes considerably more time to run, but results in more defined segments
         */

        FelzenszwalbHuttenlocherSegmenter<MBFImage> fhSegmenter = new FelzenszwalbHuttenlocherSegmenter<>();
        fhSegmenter.segment(image);

        SegmentationUtilities.renderSegments(image, components);

        //Drawing an image with the components numbered on it
        int i = 0;
        for (ConnectedComponent comp : components) {
            if (comp.calculateArea() < 50) //only render numbers for regions that are over 50 pixels
                continue;
            image.drawText("Point:" + (i++), comp.calculateCentroidPixel(), HersheyFont.TIMES_MEDIUM, 20);
        }
        DisplayUtilities.display(image, "FelzenszwalbHuttenlocherSegmenter output image");

    }
}
