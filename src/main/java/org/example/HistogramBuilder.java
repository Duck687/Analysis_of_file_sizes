package org.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;



public class HistogramBuilder {

    public static void main(String[] args) {
        File directory = new File("/home/bohdan");
        Map<Long, Integer> sizeDistribution = new TreeMap<>();
        DefaultCategoryDataset dataSet = new DefaultCategoryDataset();

        ForkJoinPool executor = new ForkJoinPool(12);
        ForkJoinTask<?> task = executor.submit(() -> analyzeFiles(directory, sizeDistribution));
        task.join();
        ForkJoinTask<?> task2 = executor.submit(() -> configurateDataSet(dataSet, sizeDistribution));
        task2.join();

        JFreeChart chart = ChartFactory.createBarChart(
                "File Size Histogram",
                "File Size (bytes)",
                "Percent",
                dataSet
        );

        ChartFrame frame = new ChartFrame("File Size Histogram", chart);
        frame.pack();
        frame.setVisible(true);

        try {
            ChartUtils.saveChartAsPNG(new File("histogram.png"), chart, 800, 600);
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

    private static void configurateDataSet(DefaultCategoryDataset dataSet, Map<Long, Integer> sizeDistribution)
    {
        Integer sum = sizeDistribution.values().stream().parallel().mapToInt(Integer::intValue).sum();

        for (Long fileSize : sizeDistribution.keySet()) {
            Integer count = sizeDistribution.get(fileSize);
            double v = Double.valueOf(count) / Double.valueOf(sum);
            dataSet.setValue(v *100, "File Size", String.valueOf(fileSize));
        }
    }



}