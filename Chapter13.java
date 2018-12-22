import org.apache.commons.vfs2.FileSystemException;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.model.EigenImages;

import java.util.*;

public class Chapter13 {

    public static void main(String[] args) throws FileSystemException {

        //Loading the AT&T "The Database of Faces" dataset
        VFSGroupDataset<FImage> dataset =
                new VFSGroupDataset<>("zip:http://datasets.openimaj.org/att_faces.zip", ImageUtilities.FIMAGE_READER);

        //Splitting the dataset into 2 parts: one for training, and one for testing using GroupedRandomSplitter class
        int nTraining = 5;
        int nTesting = 5;
        GroupedRandomSplitter<String, FImage> splits =
                new GroupedRandomSplitter<>(dataset, nTraining, 0, nTesting);
        GroupedDataset<String, ListDataset<FImage>, FImage> training = splits.getTrainingDataset();
        GroupedDataset<String, ListDataset<FImage>, FImage> testing = splits.getTestDataset();



        //EigenImages class needs a list of images from which to learn the basis (training set)
        List<FImage> basisImages = DatasetAdaptors.asList(training);
        //and number of dimensions we want our features to be (i.e. how many of the eigenvectors)
        int nEigenvectors = 100;
        EigenImages eigen = new EigenImages(nEigenvectors);
        eigen.train(basisImages);

        List<FImage> eigenFaces = new ArrayList<>();

        //Drawing the first 12 basis vectors
        for (int i = 0; i < 12; i++) {
            eigenFaces.add(eigen.visualisePC(i));
        }
        DisplayUtilities.display("EigenFaces", eigenFaces);


        //Building a database of features from the training images
        //Map of Strings (Person ID) and an array of features (all the features of all the training instances of the person)

        Map<String, DoubleFV[]> features = new HashMap<>();
        for (final String person : training.getGroups()) {
            final DoubleFV[] fvs = new DoubleFV[nTraining];

            for (int i = 0; i < nTraining; i++) {
                final FImage face = training.get(person).get(i);
                fvs[i] = eigen.extractFeature(face);
            }
            features.put(person, fvs);
        }

        //To find the identity of an unknown face image we extract the feature from this image,
        //go through the map and find the feature with the smallest Euclidean distance,
        //then return the identifier of the corresponding person

        //Going through all the testing images and estimating the true identity + computing the accuracy of
        //the recognition (predicted vs true identity)

        double correct = 0;
        double incorrect = 0;
        for (String truePerson : testing.getGroups()) {
            for (FImage face : testing.get(truePerson)) {

                //Extract the feature on the image:
                DoubleFV testFeature = eigen.extractFeature(face);

                //The predicted identifier of the person:
                String bestPerson = null;

                //Start with largest distance and if a feature has a smaller distance it is
                //more likely to be of the true person on the image

                double minDistance = Double.MAX_VALUE;
                double threshold = 0.5; //Random guess has a 0.5 probability of being correct - this is why this value was chosen as a threshold
                for (final String person : features.keySet()) {
                    for (final DoubleFV fv : features.get(person)) {

                        //Comparing the distances
                        double distance = fv.compare(testFeature, DoubleFVComparison.EUCLIDEAN);

                        System.out.println("------------------------------");
                        System.out.println("MIN Distance: " + minDistance);
                        System.out.println("Distance: " + distance);
                        System.out.println();

                        //If the distance is smaller, update the current best distance and person
                        if (distance - minDistance < threshold) {
                            minDistance = distance;
                            bestPerson = person;
                        } else {
                            System.out.println("Unknown");
                        }
                    }
                }

                System.out.println("Actual: " + truePerson + "\tguess: " + bestPerson);

                //If the predicted identity is equal to the true identity on the photo, increment correct guesses
                if (truePerson.equals(bestPerson)) {
                    correct++;
                } else {
                    incorrect++;
                }
            }
        }

        //Accuracy is 0.93 in the first run and 0.945 in the second one
        //(varies with each run because of the random splitting of the dataset)
        System.out.println("Accuracy: " + (correct / (correct + incorrect)));


        //Exercise 1: Reconstructing faces


        //Building a PCA basis and extracting the feature of a randomly selected face from the test set

        Map<String, DoubleFV[]> featuresMap = new HashMap<>();
        for (final String person : training.getGroups()) {
            final DoubleFV[] fvs = new DoubleFV[nTraining];

            for (int i = 0; i < nTraining; i++) {
                final FImage face = training.get(person).get(i);
                fvs[i] = eigen.extractFeature(face);
            }
            featuresMap.put(person, fvs);
        }

        Set<String> people = testing.getGroups();
        String truePerson = people.iterator().next();
        FImage face = testing.getRandomInstance(truePerson);

        DoubleFV testFeature = eigen.extractFeature(face);
        FImage reconstructedImage = eigen.reconstruct(testFeature);
        reconstructedImage.normalise();

        DisplayUtilities.display(reconstructedImage, "Reconstructed Image");



        // Exercise 2: Exploring the effect of training set size

        /*
        Reducing the number of training images while keeping the number of testing images fixed at 5:

        training size = 4 -> accuracy goes to 0.92 in first run, 0.915 in second, 0.855 in third
        training size = 3 -> accuracy goes to 0.885 in first run, 0.855 in second, 0.895 in third
        training size = 2 -> accuracy goes to 0.825 in first run, 0.77 in second, 0.84 in third
        training size = 1 -> accuracy goes to 0.67 in first run, 0.66 in second, 0.745 in third

        Observation: with decreasing the training size the algorithm does not have enough instances to learn from;
        Since it doesn't have enough images to learn the basis from and make correct predictions,
        when trying to estimate new instances the accuracy will be worse.

         */

        /*

        Exercise 3: Apply a threshold

            - Implemented in code above
        */

    }
}