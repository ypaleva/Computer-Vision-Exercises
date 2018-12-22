import org.openimaj.data.dataset.MapBackedDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.dataset.BingImageDataset;
import org.openimaj.util.api.auth.DefaultTokenFactory;
import org.openimaj.util.api.auth.common.BingAPIToken;

import java.util.Map;

public class Chapter6 {

    public static void main(String[] args) throws Exception {


      //Creating a list dataset from a directory of images on the computer
      String path = "/home/yoanapaleva/Documents/Computer-Vision/dataset";
      VFSListDataset<FImage> images =
              new VFSListDataset<FImage>(path, ImageUtilities.FIMAGE_READER);

      System.out.println("Number of items in the dataset: " + images.size());
      DisplayUtilities.display(images.getRandomInstance(), "A random image from the dataset");

      //Displaying all images in a window
      DisplayUtilities.display("My images", images);

      //Creating an image dataset from images in a zip file which is hosted on a web-server
      VFSListDataset<FImage> faces =
              new VFSListDataset<FImage>("zip:http://datasets.openimaj.org/att_faces.zip", ImageUtilities.FIMAGE_READER);
      DisplayUtilities.display("ATT faces", faces);

      //A grouped dataset maps a set of keys (directory name) to sub-datasets
      VFSGroupDataset<FImage> groupedFaces =
              new VFSGroupDataset<FImage>("zip:http://datasets.openimaj.org/att_faces.zip", ImageUtilities.FIMAGE_READER);


      //Display all the images from each directory in a window
      for (final Map.Entry<String, VFSListDataset<FImage>> entry : groupedFaces.entrySet()) {
          DisplayUtilities.display(entry.getKey(), entry.getValue());
      }

      /*
      //FlickrImageDataset class dynamically constructs a dataset of images from a Flickr search

      FlickrAPIToken flickrToken = DefaultTokenFactory.get(FlickrAPIToken.class);
      FlickrImageDataset<FImage> cats =
              FlickrImageDataset.create(ImageUtilities.FIMAGE_READER, flickrToken, "cat", 10);
      DisplayUtilities.display("Cats", cats);
      */

      //Exercise 1: Selecting one image from every entry in the grouped dataset and displaying it with a title


      //Going through each entry keys (created from the names of the directories containing the images)
      //and displaying a random instance from each sub-dataset
      for (final Map.Entry<String, VFSListDataset<FImage>> entry : groupedFaces.entrySet()) {
          DisplayUtilities.display(entry.getValue().getRandomInstance(), "A random image from: " + entry.getKey());
      }

        //Exercise 2:

        /*

        Some of the features of Commons VFS are: (taken from https://commons.apache.org/proper/commons-vfs/)

        -A single consistent API for accessing files of different types.
        -Support for numerous file system types .
        -Caching of file information. Caches information in-JVM, and optionally can cache remote file information on the local file system (replicator).
        -Event delivery.
        -Support for logical file systems made up of files from various different file systems.
        -Utilities for integrating Commons VFS into applications, such as a VFS-aware ClassLoader and URLStreamHandlerFactory.
        -A set of VFS-enabled Ant tasks .

         */


        //Exercise 3:

        /*

        - I have attempted this part of the tutorial, getting an active key for 7 days (which has now expired)
        - The following code successfully returned images of a mouse, after all of the details were set in the command prompt

        //DefaultTokenFactory.delete(FlickrAPIToken.class);
        //DefaultTokenFactory.delete(BingAPIToken.class);

        BingAPIToken bingAPIToken = DefaultTokenFactory.get(BingAPIToken.class);
        BingImageDataset<MBFImage> bingImageDataset = BingImageDataset.create(ImageUtilities.MBFIMAGE_READER, bingAPIToken, "mouse", 10);
        DisplayUtilities.display("Mouse", bingImageDataset);

        */

        //Exercise 4:

      /*

      - As in Exercise 3, the following code was tested while the key was active - it successfully fetches photos
        based on the search query and creates a MapBackedDataset

        BingAPIToken token = DefaultTokenFactory.get(BingAPIToken.class);
        BingImageDataset<MBFImage> bingDatasetBenedict = BingImageDataset.create(ImageUtilities.MBFIMAGE_READER, token, "benedict cumberbatch", 5);
        BingImageDataset<MBFImage> bingDatasetTom = BingImageDataset.create(ImageUtilities.MBFIMAGE_READER, token, "tom hanks", 5);
        BingImageDataset<MBFImage> bingDatasetDenzel = BingImageDataset.create(ImageUtilities.MBFIMAGE_READER, token, "denzel washington", 5);
        
        MapBackedDataset map = MapBackedDataset.of(bingDatasetBenedict, bingDatasetTom ,bingDatasetDenzel);

        System.out.println(map.size());

      */

    }
}
