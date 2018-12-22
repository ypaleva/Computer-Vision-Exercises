import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.dataset.sampling.GroupSampler;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.time.Timer;
import org.openimaj.util.function.Operation;
import org.openimaj.util.parallel.Parallel;
import org.openimaj.util.parallel.partition.RangePartitioner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Chapter14 {

    public static void main(String[] args) throws IOException {

        //The parallel equivalent of a for (int i=0; i<10; i++) loop
        Parallel.forIndex(0, 10, 1, new Operation<Integer>() {
            public void perform(Integer i) {
                System.out.println(i);
            }
        });


        //Loading the CalTech 101 dataset
        VFSGroupDataset<MBFImage> allImages = Caltech101.getImages(ImageUtilities.MBFIMAGE_READER);

        //Using a subset of the first 8 groups (image categories) in the dataset
        GroupedDataset<String, ListDataset<MBFImage>, MBFImage> images = GroupSampler.sample(allImages, 8, false);

        final List<MBFImage> output = new ArrayList<MBFImage>();
        final ResizeProcessor resize = new ResizeProcessor(200);

        /*

        //Looping through the images in the group
        for (ListDataset<MBFImage> clzImages : images.values()) {
            MBFImage current = new MBFImage(200, 200, ColourSpace.RGB);

            for (MBFImage i : clzImages) {
                MBFImage tmp = new MBFImage(200, 200, ColourSpace.RGB);
                tmp.fill(RGBColour.WHITE);
                //Resampling and normalising each image before drawing it
                MBFImage small = i.process(resize).normalise();
                int x = (200 - small.getWidth()) / 2;
                int y = (200 - small.getHeight()) / 2;
                tmp.drawImage(small, x, y);
                //adding the result to an accumulator
                current.addInplace(tmp);
            }
            //Divide the accumulated image by the number of samples used to create it
            current.divideInplace((float) clzImages.size());
            output.add(current);
        }

        DisplayUtilities.display("Images", output);

        */

        //The code below is a parallel version of the code above
        //Using the Parallel.for method to parallelise the inner-loop

        /*
        for (ListDataset<MBFImage> clzImages : images.values()) {
            final MBFImage current = new MBFImage(200, 200, ColourSpace.RGB);

            Parallel.forEach(clzImages, new Operation<MBFImage>() {
                public void perform(MBFImage i) {
                    final MBFImage tmp = new MBFImage(200, 200, ColourSpace.RGB);
                    tmp.fill(RGBColour.WHITE);

                    final MBFImage small = i.process(resize).normalise();
                    final int x = (200 - small.getWidth()) / 2;
                    final int y = (200 - small.getHeight()) / 2;
                    tmp.drawImage(small, x, y);

                    synchronized (current) {
                        current.addInplace(tmp);
                    }
                }
            });
            current.divideInplace((float) clzImages.size());
            output.add(current);
        }
        */


        /*
        //Partitioned variant of the for-each loop in the Parallel class:
        //Giving each thread a single image at a time, the partitioned variant will
        //feed each thread a collection of images (provided as an Iterator) to process

        for (ListDataset<MBFImage> clzImages : images.values()) {
            final MBFImage current = new MBFImage(200, 200, ColourSpace.RGB);

            Parallel.forEachPartitioned(new RangePartitioner<MBFImage>(clzImages), new Operation<Iterator<MBFImage>>() {
                public void perform(Iterator<MBFImage> it) {
                    MBFImage tmpAccum = new MBFImage(200, 200, 3);
                    MBFImage tmp = new MBFImage(200, 200, ColourSpace.RGB);

                    while (it.hasNext()) {
                        final MBFImage i = it.next();
                        tmp.fill(RGBColour.WHITE);

                        final MBFImage small = i.process(resize).normalise();
                        final int x = (200 - small.getWidth()) / 2;
                        final int y = (200 - small.getHeight()) / 2;
                        tmp.drawImage(small, x, y);
                        tmpAccum.addInplace(tmp);
                    }
                    synchronized (current) {
                        current.addInplace(tmpAccum);
                    }
                }
            });
            current.divideInplace((float) clzImages.size());
            output.add(current);
        }

        */

        //Timing how long the program runs:
        Timer t1 = Timer.timer();


        //Exercise 1: Parallelise the outer loop

        //or Parallel.forEach(images.values(), new Operation<ListDataset<MBFImage>>() {
        Parallel.forEach(images.values(), new Operation<ListDataset<MBFImage>>() {

            @Override
            public void perform(ListDataset<MBFImage> mbfImages) {
                MBFImage current = new MBFImage(200, 200, ColourSpace.RGB);
                for (MBFImage i : mbfImages) {
                    MBFImage tmp = new MBFImage(200, 200, ColourSpace.RGB);
                    tmp.fill(RGBColour.WHITE);

                    MBFImage small = i.process(resize).normalise();
                    int x = (200 - small.getWidth()) / 2;
                    int y = (200 - small.getHeight()) / 2;
                    tmp.drawImage(small, x, y);

                    current.addInplace(tmp);
                }

                current.divideInplace((float) mbfImages.size());
                output.add(current);

            }


        });

        //Time when neither loops are parallelised: 16388ms
        //Time when inner loop is parallelised giving one image per thread: 9639ms
        //Time when inner loop is parallelised giving a collection of images per thread: 9816ms

        //Exercise 1: Parallelise the outer loop

        /*
        Time when outer loop is parallelised: 11163ms

        - Pros: Because current was modified by each thread in inner for loop (when parallelised) it was surrounded
                by a synchronised block; now when outer loop is parallelised - each thread has its own buffer image
                and there is no need for synchronisation (add operation on a list will not result in unwanted behaviour
                since if a thread interrupts another the element will be pushed to the next index)
        - Cons: This makes the code slower than when inner loop is parallelised

        */
        System.out.println("Time: " + t1.duration() + "ms");
        DisplayUtilities.display("Images", output);

    }
}
