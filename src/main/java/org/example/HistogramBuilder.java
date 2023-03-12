package org.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class HistogramBuilder {

    public static void main(String[] args) {
        // Директорія для аналізу
        File directory = new File("/Downloads");

        // Отримання розподілу розмірів файлів
        Map<Long, Integer> sizeDistribution = getFileSizeDistribution(directory);

        // Побудова гістограми
        JFreeChart chart = buildHistogram(sizeDistribution);

        // Відображення гістограми в окремому вікні
        ChartFrame frame = new ChartFrame("File Size Histogram", chart);
        frame.pack();
        frame.setVisible(true);

        // Збереження гістограми в файл
        try {
            ChartUtils.saveChartAsPNG(new File("histogram.png"), chart, 800, 600);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Map<Long, Integer> getFileSizeDistribution(File directory) {
        Map<Long, Integer> sizeDistribution = new HashMap<>();
        // Рекурсивний аналіз розміру файлів у директорії
        analyzeFiles(directory, sizeDistribution);
        return sizeDistribution;
    }

    private static void analyzeFiles(File file, Map<Long, Integer> sizeDistribution) {
        if (file.isDirectory()) {
            // Рекурсивний виклик для кожної підпапки
            File[] subFiles = file.listFiles();
            for (File subFile : subFiles) {
                analyzeFiles(subFile, sizeDistribution);
            }
        } else {
            // Додавання розміру файлу до гістограми
            long fileSize = file.length();
            if (sizeDistribution.containsKey(fileSize)) {
                int count = sizeDistribution.get(fileSize) + 1;
                sizeDistribution.put(fileSize, count);
            } else {
                sizeDistribution.put(fileSize, 1);
            }
        }
    }

    private static JFreeChart buildHistogram(Map<Long, Integer> sizeDistribution) {
        System.out.println(sizeDistribution);
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Long fileSize : sizeDistribution.keySet()) {
            int count = sizeDistribution.get(fileSize);
            dataset.addValue(count, "File Size", String.valueOf(fileSize));
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "File Size Histogram",  // Головний заголовок
                "File Size (bytes)",    // Вісь X
                "Count",                // Вісь Y
                dataset,                // Дані для гра
                PlotOrientation.VERTICAL,  // Орієнтація графіку
                true,                   // Легенда
                true,                   // Відображення назв категорій на вісі X
                false                   // Відображення значень на бічних панелях
        );
        return chart;
    }

}