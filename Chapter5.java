import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.*;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.math.geometry.transforms.FundamentalRefinement;
import org.openimaj.math.geometry.transforms.HomographyRefinement;
import org.openimaj.math.geometry.transforms.estimation.RobustAffineTransformEstimator;
import org.openimaj.math.geometry.transforms.estimation.RobustFundamentalEstimator;
import org.openimaj.math.geometry.transforms.estimation.RobustHomographyEstimator;
import org.openimaj.math.model.Model;
import org.openimaj.math.model.fit.LMedS;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.math.model.fit.residuals.ResidualCalculator;

import java.io.IOException;
import java.net.URL;

public class Chapter5 {

    public static void main(String[] args) throws IOException {

        MBFImage query = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/query.jpg"));
        MBFImage target = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/target.jpg"));

        //Using a difference-of-Gaussian feature detector, described with a SIFT descriptor.
        DoGSIFTEngine engine = new DoGSIFTEngine();
        LocalFeatureList<Keypoint> queryKeypoints = engine.findFeatures(query.flatten());
        LocalFeatureList<Keypoint> targetKeypoints = engine.findFeatures(target.flatten());

        //Comparing based on a given Keypoint in the query and a Keypoint that is closest in the target

        //Setting up a matcher to compare keypoints
        LocalFeatureMatcher<Keypoint> matcher = new BasicMatcher<>(80);
        matcher.setModelFeatures(queryKeypoints);
        matcher.findMatches(targetKeypoints);

        //Drawing the matches between the two images found with the basic matcher using the MatchingUtilities class
        MBFImage matches = MatchingUtilities.drawMatches(query, target, matcher.getMatches(), RGBColour.RED);
        DisplayUtilities.display(matches, "Matches between the two images");


        //Exercise 2: Experimenting with different models:

        //Constructor params for RobustHomography Estimator: double threshold, int nIterations,
                                            //RANSAC.StoppingCondition stoppingCondition, HomographyRefinement refinement
        //Constructor params for LMedS: M model, ResidualCalculator<I,D,M> residualEstimator, boolean impEst (from documentation)


        /*
        //First model:
        //RobustFundamentalEstimator(double outlierProportion, FundamentalRefinement refinement)
        //Construct using the LMedS algorithm with the given expected outlier percentage (from OpenIMAJ docs)
        //Does not give a rectangular shape
        RobustFundamentalEstimator modelFitter = new RobustFundamentalEstimator(0.5, 1500,
                       new RANSAC.PercentageInliersStoppingCondition(0.5), FundamentalRefinement.NONE);
        matcher = new ConsistentLocalFeatureMatcher2d<>(
                new BasicTwoWayMatcher<Keypoint>(), modelFitter);
        */


        //Second model:
        //Draws a much more precise rectangle around the matching points
        RobustHomographyEstimator modelFitter = new RobustHomographyEstimator(0.5, 1500,
                new RANSAC.PercentageInliersStoppingCondition(0.5), HomographyRefinement.NONE);
        matcher = new ConsistentLocalFeatureMatcher2d<>(
                new BasicTwoWayMatcher<Keypoint>(), modelFitter);


        /*
        //Second model RobustHomographyEstimator(double outlierProportion, HomographyRefinement refinement)
        //Construct using the LMedS algorithm with the given expected outlier percentage (from OpenIMAJ docs)

        RobustHomographyEstimator modelFitter = new RobustHomographyEstimator(0.5, HomographyRefinement.NONE);
        matcher = new ConsistentLocalFeatureMatcher2d<>(
                new BasicTwoWayMatcher<Keypoint>(), modelFitter);
        */


        /*
        //Third model:
        //Filter the matches based on a given geometric model using ConsistentLocalFeatureMatcher
        //Gives rectangular shape, but not very precise

        RobustAffineTransformEstimator modelFitter = new RobustAffineTransformEstimator(5.0, 1500,
                new RANSAC.PercentageInliersStoppingCondition(0.5));
        matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(
                new FastBasicKeypointMatcher<Keypoint>(8), modelFitter);
        */

        /*

       Exercise 1: Experimenting with different matchers:

       Matches a lot of points in text section
       new BasicTwoWayMatcher<Keypoint>(), modelFitter);

       For Fast Limited Euclidean Keypoint macher: threshold = 8 -> matches 3 points (red, black, white)
       threshold = 15 -> matches a few points in text section; threshold = 35 -> more points in text section
       new FastLimitedEuclideanKeypointMatcher<>(35), modelFitter);

       Fast Euclidean Keypoint matcher gives java.lang.RuntimeException: Matrix is singular.
       new FastEuclideanKeypointMatcher<>(15), modelFitter);

       For Voting Keypoint Matcher when threshold = 3 it matches title section of images
       when equal to 4 it matches in the corner of text section
       when equal to 15 matches points from the whole text section
       new VotingKeypointMatcher<>(15), modelFitter);

       For Fast Basic Keypoint matcher, when threshold = 8 - matches mainly in text section
       when threshold = 3 -> does not produce any output
       for 15 -> matches corner of text with images below
       new FastBasicKeypointMatcher<>(15), modelFitter);

       For Multiple Matches Matcher with count >= 4 I get java.lang.OutOfMemoryError: Java heap space -> max = 3
       new MultipleMatchesMatcher<>(3, 8), modelFitter);
        */


        matcher.setModelFeatures(queryKeypoints);
        matcher.findMatches(targetKeypoints);

        MBFImage consistentMatches = MatchingUtilities.drawMatches(query, target, matcher.getMatches(),
                RGBColour.RED);

        DisplayUtilities.display(consistentMatches);

        //RobustAffineTransformEstimator class provides a method getModel() which returns the internal Affine Transform model
        //Drawing a polygon around the estimated location of the query within the target
        //Note that method getF() needs to be called instead of getTransform() when using RobustFundamentalEstimator class
        target.drawShape(query.getBounds().transform(modelFitter.getModel().getTransform().inverse()), 3, RGBColour.BLUE);
        DisplayUtilities.display(target);
    }
}
