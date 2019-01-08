package io.aime.graphs;

import io.aime.brain.Brain;
import io.aime.brain.data.BrainXMLData;
import io.aime.brain.data.MetadataFetcher;
import io.aime.brain.xml.Handler;
import io.aime.util.AIMEConfiguration;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.Timer;
import org.apache.hadoop.conf.Configuration;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

public class FetcherStatusCodes extends JPanel
{

    private final DefaultCategoryDataset DATASET = new DefaultCategoryDataset();
    private final Configuration CONF = new AIMEConfiguration().create();

    public FetcherStatusCodes()
    {
        super(new BorderLayout());

        // Create chart with default values.
        DATASET.addValue(0, "Status Code", "Success");
        DATASET.addValue(0, "Status Code", "Failed");
        DATASET.addValue(0, "Status Code", "Temporary Moved");

        CategoryAxis domain = new CategoryAxis("Status Codes");
        domain.setLabelFont(getFont().deriveFont(Font.BOLD));
        domain.setLabelPaint(Color.ORANGE);
        domain.setLowerMargin(0.0);
        domain.setUpperMargin(0.0);
        domain.setTickLabelsVisible(true);
        domain.setTickLabelPaint(Color.WHITE);

        NumberAxis range = new NumberAxis("Count");
        range.setLabelFont(getFont().deriveFont(Font.BOLD));
        range.setLabelPaint(Color.ORANGE);
        range.setAutoRange(true);
        range.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        range.setTickLabelPaint(Color.WHITE);

        BarRenderer renderer = new BarRenderer();
        renderer.setShadowVisible(false);
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, new Color(45, 159, 201));
        renderer.setSeriesStroke(0, new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        renderer.setBaseItemLabelFont(getFont().deriveFont(Font.BOLD));
        renderer.setBaseLegendTextFont(getFont().deriveFont(Font.BOLD));

        CategoryPlot xyplot = new CategoryPlot(DATASET, domain, range, renderer);
        xyplot.setBackgroundPaint(new Color(43, 42, 41));
        xyplot.setDomainGridlinePaint(Color.GRAY);
        xyplot.setRangeGridlinePaint(Color.GRAY);
        xyplot.setDomainCrosshairVisible(true);
        xyplot.setRangeCrosshairVisible(true);

        JFreeChart chart = new JFreeChart(null, null, xyplot, false);
        ChartPanel chartPanel = new ChartPanel(chart, false);
        chartPanel.setPopupMenu(null);
        add(chartPanel);

        new Updater().start();
    }

    class Updater extends Timer
    {

        Updater()
        {
            super(CONF.getInt("fetcher.status.codes.graph", 1000), null);
            addActionListener((ActionEvent e) -> {
                final MetadataFetcher.Data DATA = (MetadataFetcher.Data) Brain
                        .getInstance()
                        .execute(Handler
                                .makeXMLRequest(BrainXMLData
                                        .newBuild()
                                        .setJob(BrainXMLData.JOB_REQUEST)
                                        .setClazz(MetadataFetcher.Data.class)
                                        .setFunction("Data"))).get();

                DATASET.clear();
                DATASET.addValue(DATA.getSuccess(), "Status Code", "Success");
                DATASET.addValue(DATA.getFailed(), "Status Code", "Failed");
                DATASET.addValue(DATA.getTempMoved(), "Status Code", "Temporary Moved");
            });
        }
    }
}
