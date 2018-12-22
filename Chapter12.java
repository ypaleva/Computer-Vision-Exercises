import de.bwaldvogel.liblinear.SolverType;
import org.openimaj.data.DataSource;
import org.openimaj.data.dataset.Dataset;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.experiment.dataset.sampling.GroupSampler;
import org.openimaj.experiment.dataset.sampling.GroupedUniformRandomisedSampler;
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter;
import org.openimaj.experiment.evaluation.classification.ClassificationEvaluator;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAnalyser;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMResult;
import org.openimaj.feature.DiskCachingFeatureExtractor;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.SparseIntFV;
import org.openimaj.feature.local.data.LocalFeatureListDataSource;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101;
import org.openimaj.image.feature.dense.gradient.dsift.ByteDSIFTKeypoint;
import org.openimaj.image.feature.dense.gradient.dsift.DenseSIFT;
import org.openimaj.image.feature.dense.gradient.dsift.PyramidDenseSIFT;
import org.openimaj.image.feature.local.aggregate.BagOfVisualWords;
import org.openimaj.image.feature.local.aggregate.BlockSpatialAggregator;
import org.openimaj.image.feature.local.aggregate.PyramidSpatialAggregator;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator;
import org.openimaj.ml.clustering.ByteCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.ByteKMeans;
import org.openimaj.ml.kernel.HomogeneousKernelMap;
import org.openimaj.time.Timer;
import org.openimaj.util.pair.IntFloatPair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Chapter12 {

    public static void main(String[] args) throws IOException {


        Timer t1 = Timer.timer();
        //Creating a grouped dataset from the Caltech 101 dataset
        GroupedDataset<String, VFSListDataset<Caltech101.Record<FImage>>, Caltech101.Record<FImage>> allData =
                Caltech101.getData(ImageUtilities.FIMAGE_READER);

        /*

        //Creating a dataset using only 5 classes from the Caltech101 dataset
        GroupedDataset<String, ListDataset<Caltech101.Record<FImage>>, Caltech101.Record<FImage>> data =
                GroupSampler.sample(allData, 5, false);
        */

        //Creating a training dataset with 15 images per group, and 15 testing images per group (0 for validation)
        GroupedRandomSplitter<String, Caltech101.Record<FImage>> splits =
                new GroupedRandomSplitter<>(allData, 15, 0, 15); //Running the code over all the classes in the Caltech 101 dataset (ex. 3)

        DenseSIFT dsift = new DenseSIFT(3, 7); //Reducing the step of the DenseSIFT to 3 (ex. 3)

        //PyramidDenseSIFT applies Dense SIFT extractor to different sized windows on the regular sampling grid
        PyramidDenseSIFT<FImage> pdsift = new PyramidDenseSIFT<FImage>(dsift, 6f, 4, 6, 8, 10); //adding extra scales to the PyramidDenseSIFT (ex. 3)

        //Using the GroupedUniformRandomisedSampler to get a random sample of 30 images across all the groups of the training set
        HardAssigner<byte[], float[], IntFloatPair> assigner =
                trainQuantiser(GroupedUniformRandomisedSampler.sample(splits.getTrainingDataset(), 30), pdsift);


         /*
        Exercise 2: Feature caching
         */

        String pathName = "/home/yoanapaleva/Desktop/chapter-12/";
        File dir = new File(pathName);
        File assignerFile = new File(pathName + "HardAssigner");
        //Saving the HardAssigner to file since the features must be kept with the same HardAssigner that created them
        IOUtils.writeToFile(assigner, assignerFile);


        //Constructing an instance of the PHOWExtractor with the assigner read from file
        FeatureExtractor<DoubleFV, Caltech101.Record<FImage>> extractor = new PHOWExtractor(pdsift,
                IOUtils.<HardAssigner<byte[], float[], IntFloatPair>>readFromFile(assignerFile));

        FeatureExtractor<DoubleFV, Caltech101.Record<FImage>> diskCachingExtractor =
                new DiskCachingFeatureExtractor<>(dir, extractor);



          /*
        Exercise 1: Apply a Homogeneous Kernel Map
         */

        //Using the HomogeneousKernelMap class with a KernelType.Chi2 kernel and WindowType.Rectangular window
        HomogeneousKernelMap map = new HomogeneousKernelMap(HomogeneousKernelMap.KernelType.Chi2, HomogeneousKernelMap.WindowType.Rectangular);
        diskCachingExtractor = map.createWrappedExtractor(diskCachingExtractor);

        /*
        - What effect does this have on performance?


          Before applying the Homogeneous Kernel Map:

          ...
          iter  1 act 7.102e+00 pre 7.102e+00 delta 3.549e+00 f 7.500e+01 |g| 4.002e+00 CG   1
          iter  2 act 7.330e-03 pre 7.330e-03 delta 3.549e+00 f 6.790e+01 |g| 1.275e-01 CG   1
          iter  3 act 2.568e-05 pre 2.568e-05 delta 3.549e+00 f 6.789e+01 |g| 7.553e-03 CG   1
          iter  4 act 1.174e-07 pre 1.174e-07 delta 3.549e+00 f 6.789e+01 |g| 5.070e-04 CG   1
          iter  5 act 6.105e-10 pre 6.106e-10 delta 3.549e+00 f 6.789e+01 |g| 3.671e-05 CG   1
          ...
          Evaluating classifier...
          Result:   Accuracy: 0.653
          Error Rate: 0.347

          After applying the Homogeneous Kernel Map:

          ...
          iter  1 act 5.572e+01 pre 5.570e+01 delta 4.601e+00 f 7.500e+01 |g| 4.431e+01 CG   4
          iter  2 act 2.898e-01 pre 3.212e-01 delta 4.601e+00 f 1.928e+01 |g| 1.408e+00 CG   5
          iter  3 act 1.532e-02 pre 1.532e-02 delta 4.601e+00 f 1.899e+01 |g| 4.172e-01 CG   4
          iter  4 act 1.078e-04 pre 1.078e-04 delta 4.601e+00 f 1.898e+01 |g| 2.583e-02 CG   5
          iter  5 act 9.260e-07 pre 9.260e-07 delta 4.601e+00 f 1.898e+01 |g| 1.980e-03 CG   6
          ...
          Evaluating classifier...
          Result:   Accuracy: 0.840
          Error Rate: 0.160

          - What is noticed is a drastic improvement in accuracy and a change in the output from the iterations

         */


        //Constructing and training a classifier using the linear classifier provided by the LiblinearAnnotator class
        LiblinearAnnotator<Caltech101.Record<FImage>, String> ann = new LiblinearAnnotator<>(
                diskCachingExtractor, LiblinearAnnotator.Mode.MULTICLASS, SolverType.L2R_L2LOSS_SVC, 1.0, 0.00001);
        ann.train(splits.getTrainingDataset());

        //Evaluating the classifier’s accuracy
        ClassificationEvaluator<CMResult<String>, String, Caltech101.Record<FImage>> eval =
                new ClassificationEvaluator<>(
                        ann, splits.getTestDataset(), new CMAnalyser<Caltech101.Record<FImage>, String>(CMAnalyser.Strategy.SINGLE));

        Map<Caltech101.Record<FImage>, ClassificationResult<String>> guesses = eval.evaluate();
        CMResult<String> result = eval.analyse(guesses);

        System.out.println("Result: " + result);

        System.out.println("Time: " + t1.duration() + "ms");
    }


    //This method extracts the first 10000 dense SIFT features from the images in the dataset, and then clusters them into 300 separate classes
    //Then returns a HardAssigner which can be used to assign SIFT features to identifiers
    static HardAssigner<byte[], float[], IntFloatPair> trainQuantiser(Dataset<Caltech101.Record<FImage>> sample,
                                                                      PyramidDenseSIFT<FImage> pdsift) {

        List<LocalFeatureList<ByteDSIFTKeypoint>> allkeys = new ArrayList<>();

        for (Caltech101.Record<FImage> rec : sample) {
            FImage img = rec.getImage();
            pdsift.analyseImage(img);
            allkeys.add(pdsift.getByteKeypoints(0.005f));
        }

        if (allkeys.size() > 10000)
            allkeys = allkeys.subList(0, 10000);

        ByteKMeans km = ByteKMeans.createKDTreeEnsemble(600);
        DataSource<byte[]> datasource = new LocalFeatureListDataSource<>(allkeys);
        ByteCentroidsResult result = km.cluster(datasource);
        return result.defaultHardAssigner();
    }

    //This class uses a BlockSpatialAggregator together with a BagOfVisualWords to compute 4 histograms across the image
    static class PHOWExtractor implements FeatureExtractor<DoubleFV, Caltech101.Record<FImage>> {
        PyramidDenseSIFT<FImage> pdsift;
        HardAssigner<byte[], float[], IntFloatPair> assigner;

        public PHOWExtractor(PyramidDenseSIFT<FImage> pdsift, HardAssigner<byte[], float[], IntFloatPair> assigner) {
            this.pdsift = pdsift;
            this.assigner = assigner;
        }

        public DoubleFV extractFeature(Caltech101.Record<FImage> object) {
            FImage image = object.getImage();
            pdsift.analyseImage(image);

            //The BagOfVisualWords uses the HardAssigner to assign each Dense SIFT feature
            //to a visual word and compute the histogram
            BagOfVisualWords<byte[]> bovw = new BagOfVisualWords<>(assigner);

            PyramidSpatialAggregator<byte[], SparseIntFV> spatial = new PyramidSpatialAggregator<byte[], SparseIntFV>(
                    bovw, 2, 4); //Substituted the BlockSpatialAggregator with the PyramidSpatialAggregator (ex. 3)

            //The resultant spatial histograms are then appended together and normalised before being returned
            return spatial.aggregate(pdsift.getByteKeypoints(0.015f), image.getBounds()).normaliseFV();
        }
    }

    /*
    Exercise 3: The whole dataset

        - Implemented in code above: using the whole dataset, increased the number of visual words to 600,
          added extra scales to the PyramidDenseSIFT, reduced the step-size of the DenseSIFT to 3, and using
          the PyramidSpatialAggregator with [2, 4] blocks.

        - What level of classifier performance does this achieve?

          Result:   Accuracy: 0.533 - (Accuracy decreased)
          Error Rate: 0.467

          Time: 13267342ms (Runtime also increased drastically)


     */


}
