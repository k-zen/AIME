package io.aime.mvc.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import net.apkc.emma.mvc.AbstractViewMenuBar;

final class MainViewMenuBar extends AbstractViewMenuBar
{

    private JMenu file = new JMenu();
    private JMenu mainFunctions = new JMenu();
    private JMenuItem newCrawlJob = new JMenuItem();
    private JMenu tools = new JMenu();
    private JMenuItem filters = new JMenuItem();
    private JMenuItem mainDBase = new JMenuItem();
    private JMenuItem quickFetch = new JMenuItem();
    private JMenuItem seeds = new JMenuItem();
    private JMenuItem segmentDBase = new JMenuItem();
    private JMenuItem urlInjector = new JMenuItem();
    private JMenuItem exit = new JMenuItem();
    private JMenu windows = new JMenu();
    private JMenuItem cerebellum = new JMenuItem();
    private JMenuItem fetcher = new JMenuItem();
    private JMenu help = new JMenu();
    private JMenuItem about = new JMenuItem();

    MainViewMenuBar()
    {
        file.setMnemonic('F');
        file.setText("File");

        mainFunctions.setMnemonic('M');
        mainFunctions.setText("Main Functions");

        newCrawlJob.setText("New Crawl Job");
        newCrawlJob.addActionListener(new MainViewMenuBarEvt());

        mainFunctions.add(newCrawlJob);
        file.add(mainFunctions);

        tools.setMnemonic('T');
        tools.setText("Tools");

        filters.setText("Filters");
        filters.addActionListener(new MainViewMenuBarEvt());

        mainDBase.setText("Main DBase");
        mainDBase.addActionListener(new MainViewMenuBarEvt());

        quickFetch.setText("Quick Fetch");
        quickFetch.addActionListener(new MainViewMenuBarEvt());

        seeds.setText("Seeds");
        seeds.addActionListener(new MainViewMenuBarEvt());

        segmentDBase.setText("Segment DBase");
        segmentDBase.addActionListener(new MainViewMenuBarEvt());

        urlInjector.setText("URL Injector");
        urlInjector.addActionListener(new MainViewMenuBarEvt());

        tools.add(filters);
        tools.add(mainDBase);
        tools.add(quickFetch);
        tools.add(seeds);
        tools.add(segmentDBase);
        tools.add(urlInjector);
        file.add(tools);

        exit.setMnemonic('E');
        exit.setText("Exit");
        exit.addActionListener(new MainViewMenuBarEvt());
        file.add(exit);

        add(file);

        windows.setMnemonic('W');
        windows.setText("Stats Windows");

        cerebellum.setText("Cerebellum Stats");
        cerebellum.addActionListener(new MainViewMenuBarEvt());

        fetcher.setText("Fetcher Stats");
        fetcher.addActionListener(new MainViewMenuBarEvt());

        windows.add(cerebellum);
        windows.add(fetcher);

        add(windows);

        help.setMnemonic('H');
        help.setText("Help");

        about.setText("About");
        about.addActionListener(new MainViewMenuBarEvt());

        help.add(about);

        add(help);
    }

    @Override
    public void modelPropertyChange(PropertyChangeEvent evt)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private class MainViewMenuBarEvt implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (e.getSource() == newCrawlJob)
            {
                CrawlJobWindow.getInstance().makeVisible();
            }
            else if (e.getSource() == filters)
            {
                FiltersWindow.getInstance().makeVisible();
            }
            else if (e.getSource() == mainDBase)
            {
                MainDBaseWindow.getInstance().makeVisible();
            }
            else if (e.getSource() == quickFetch)
            {
                QuickFetchWindow.getInstance().makeVisible();
            }
            else if (e.getSource() == seeds)
            {
                SeedsWindow.getInstance().makeVisible();
            }
            else if (e.getSource() == segmentDBase)
            {
                SegmentDBaseWindow.getInstance().makeVisible();
            }
            else if (e.getSource() == urlInjector)
            {
                URLInjectorWindow.getInstance().makeVisible();
            }
            else if (e.getSource() == exit)
            {
                int r1 = JOptionPane.showConfirmDialog(
                        MainWindow.getInstance(),
                        "Are you sure you want to leave the application ...?",
                        "Exit AIME Confirmation",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (r1 == JOptionPane.YES_OPTION)
                {
                    System.exit(0); // Shutdown AIME.
                }
            }
            else if (e.getSource() == cerebellum)
            {
                CerebellumWindow.getInstance().makeVisible();
            }
            else if (e.getSource() == fetcher)
            {
                FetcherWindow.getInstance().makeVisible();
            }
            else if (e.getSource() == about)
            {
                AboutWindow.getInstance().makeVisible();
            }
        }
    }
}
