package org.example;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;


public class HistogramBuilder {

    public static void main(String[] args) {
        File directory = new File("C:\\");
        TreeMap<Long, Integer> sizeDistribution = new TreeMap<>();
        DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
        DefaultCategoryDataset dataSetInterval = new DefaultCategoryDataset();

        ForkJoinPool executor = new ForkJoinPool(12);
        ForkJoinTask<?> task = executor.submit(() -> analyzeFiles(directory, sizeDistribution));
        task.join();
        ForkJoinTask<?> task2 = executor.submit(() -> configurateDataSet(dataSet, dataSetInterval, sizeDistribution));
        task2.join();

        System.out.println(sizeDistribution.lastEntry().getKey());

        JFreeChart chart = ChartFactory.createBarChart(
            "File Size Histogram",
            "File Size (bytes)",
            "Percent",
            dataSet
        );

        ChartFrame frame = new ChartFrame("File Size Histogram", chart);
        frame.pack();
        frame.setVisible(true);

        JFreeChart chart2 = ChartFactory.createBarChart(
            "File Size Histogram segments",
            "File Size (bytes)",
            "Percent",
            dataSetInterval
        );

        ChartFrame frame2 = new ChartFrame("File Size Histogram segments", chart2);
        frame2.pack();
        System.out.println("end");
        frame2.setVisible(true);

        try {
            ChartUtils.saveChartAsPNG(new File("histogram.png"), chart, 1920, 1080);
            ChartUtils.saveChartAsPNG(new File("histogram2.png"), chart2, 1920, 1080);
            System.out.println("photo");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void analyzeFiles(File file, Map<Long, Integer> sizeDistribution) {
        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            if (subFiles == null) return;

            for (File subFile : subFiles) {
                if (subFile == null) continue;
                analyzeFiles(subFile, sizeDistribution);
            }
        } else {
            long fileSize = file.length();
            sizeDistribution.compute(fileSize, (key, value) -> value == null ? 1 : value + 1);
        }
    }

    private static void configurateDataSet(DefaultCategoryDataset dataSet, DefaultCategoryDataset dataSetInterval, Map<Long, Integer> sizeDistribution) {

        Integer sum = sizeDistribution.values().stream().parallel().mapToInt(Integer::intValue).sum();
        System.out.println(sum);
        List<Long> boundariesOfIntervals = new ArrayList<>();
        int j = 20;
        Integer max = new Integer(0);
        Long maxKey = new Long(0);

        for(int i =0; i < j; i++)
            boundariesOfIntervals.add(10000l*i);

        Double sumOnInterval = 0d;
        int i = 0;

        for (Map.Entry<Long, Integer> a : sizeDistribution.entrySet()) {
            Integer value = a.getValue();
            Double v = Double.valueOf(value) / Double.valueOf(sum);
            if(a.getValue()> max)
            {
                max = value;
                maxKey = a.getKey();
            }

            if(a.getKey()<=boundariesOfIntervals.get(i)) sumOnInterval+=v;

            else
            {
                dataSetInterval.addValue(sumOnInterval * 100, "File Size", String.valueOf(boundariesOfIntervals.get(i)));
                i++;
                if(i == j) i--;
                else sumOnInterval = 0d;
            }
            dataSet.setValue(v * 100, "File Size", String.valueOf(a.getKey()));
        }
            System.out.println(max+" "+maxKey);
    }



}