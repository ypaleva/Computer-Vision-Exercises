import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.algorithm.LocalContrastFilter;
import org.openimaj.image.processing.algorithm.MedianFilter;
import org.openimaj.image.processing.background.BasicBackgroundSubtract;
import org.openimaj.image.processing.convolution.CompassOperators;
import org.openimaj.image.processing.convolution.FFastGaussianConvolve;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.image.processing.edges.CannyEdgeDetector2;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.xuggle.XuggleVideo;

import java.net.MalformedURLException;
import java.net.URL;

public class Chapter7 {

    public static void main(String[] args) throws MalformedURLException {


        //Loading a video from url and displaying it
        XuggleVideo video = new XuggleVideo(new URL("http://static.openimaj.org/media/tutorial/keyboardcat.flv"));
        VideoDisplay<MBFImage> display = VideoDisplay.createVideoDisplay(video);

        //Iterating through every frame of the video and applying a Canny edge detector
        for (MBFImage mbfImage : video) {
            DisplayUtilities.displayName(mbfImage.process(new CannyEdgeDetector()), "videoFrames");
        }

        //Using a listener to apply different processing techniques to the video
        display.addVideoListener(
                new VideoDisplayListener<MBFImage>() {
                    public void beforeUpdate(MBFImage frame) {

                        //Exercise 1: Applying different types of image processing to the video

                        /*
                        CompassOperators.Compass0():
                        - high contrast between black and white
                        - movement in video results in sharp change in colour where there are edges

                        frame.processInplace(new CompassOperators.Compass0());
                        */


                         /*
                        CompassOperators.Compass45():
                        - smoother filter;
                        - movement in video does not affect edges as much as Compass0

                        frame.processInplace(new CompassOperators.Compass45());
                        */

                        /*
                        CompassOperators.Compass90():
                        - visible low quality; no sharp change in colour when there is movement
                        - other colours than black and white
                        frame.processInplace(new CompassOperators.Compass90());
                        */

                        /*
                        CompassOperators.Compass135():
                        - black and white filter, smooth when there is movement
                        frame.processInplace(new CompassOperators.Compass135());
                        */

                        /*
                        CannyEdgeDetector and CannyEdgeDetector2:
                        - visibly more edges detected by CannyEdgeDetector2 (results in more blue lines on frame)

                        frame.processInplace(new CannyEdgeDetector());
                        frame.processInplace(new CannyEdgeDetector2());
                        */

                    }

                    public void afterUpdate(VideoDisplay<MBFImage> display) {
                    }
                });
    }
}
