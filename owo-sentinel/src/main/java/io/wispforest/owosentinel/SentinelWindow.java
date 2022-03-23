package io.wispforest.owosentinel;

import net.minecraft.util.Util;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SentinelWindow {
    public static void open() throws Exception {
        // Fix AA
        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("swing.aatext", "true");

        // Force GTK if available
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        for (var laf : UIManager.getInstalledLookAndFeels()) {
            if (!"GTK+".equals(laf.getName())) continue;
            UIManager.setLookAndFeel(laf.getClassName());
        }

        // ------
        // Window
        // ------

        JFrame window = new JFrame("oωo-sentinel");
        window.setVisible(false);

        //noinspection ConstantConditions
        final var owoIconImage = ImageIO.read(OwoSentinel.class.getClassLoader()
            .getResourceAsStream("owo_sentinel_icon.png"));

        window.setIconImage(owoIconImage);
        window.setMinimumSize(new Dimension(0, 250));
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                System.exit(0);
            }
        });
        window.setLocationByPlatform(true);

        // -----
        // Title
        // -----

        final var titleLabel = new JLabel("oωo-lib is required to run the following mods", new ImageIcon(owoIconImage), SwingConstants.LEFT);
        titleLabel.setFont(titleLabel.getFont().deriveFont(titleLabel.getFont().getSize() * 1.25f));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 15, 0, 15));
        window.getContentPane().add(titleLabel, BorderLayout.NORTH);

        // ----------
        // Dependents
        // ----------

        var dependents = "<html><center><b>" + String.join("<br>", OwoSentinel.listOwoDependents()) + "<p>\u200B";

        final var dependentsLabel = new JLabel(dependents);
        final var defaultDepFont = dependentsLabel.getFont();

        dependentsLabel.setFont(defaultDepFont.deriveFont(defaultDepFont.getSize() * 1.1f));
        dependentsLabel.setHorizontalAlignment(SwingConstants.CENTER);

        window.getContentPane().add(dependentsLabel, BorderLayout.CENTER);

        // -------
        // Buttons
        // -------

        var buttonsPanel = new JPanel();

        // Download

        final var downloadButton = new JButton("Download and install");

        final var progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        downloadButton.addActionListener(e -> {
            downloadButton.setEnabled(false);
            downloadButton.add(progressBar);
            downloadButton.updateUI();

            titleLabel.setText("Installing oωo-lib");
            window.getContentPane().remove(dependentsLabel);

            final var logBox = new JTextArea();
            logBox.setEditable(false);
            logBox.setMargin(new Insets(15, 15, 15, 15));
            final var scrollPane = new JScrollPane(logBox);
            scrollPane.setBorder(new EmptyBorder(0, 15, 0, 15));
            window.getContentPane().add(scrollPane, BorderLayout.CENTER);

            var task = new DownloadTask(s -> {
                OwoSentinel.LOGGER.info(s);
                logBox.setText(logBox.getText() + (logBox.getText().isBlank() ? "" : "\n") + s);
                scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
            }, () -> {
                progressBar.setVisible(false);
                titleLabel.setText("");
                downloadButton.setText("Installed");
            });
            task.execute();
        });

        // What is this

        final var whatIsThisButton = new JButton("What is this?");
        whatIsThisButton.addActionListener(e -> {
            String[] options = {"Open GitHub", "OK"};

            int selection = JOptionPane.showOptionDialog(window, OwoSentinel.OWO_EXPLANATION, "oωo-sentinel",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, new ImageIcon(owoIconImage),
                options, options[0]);

            if (selection == 0) Util.getOperatingSystem().open("https://github.com/glisco03/owo-lib");
        });

        // Exit

        final var exitButton = new JButton("Close");
        exitButton.addActionListener(e -> window.dispose());

        // Panel setup

        buttonsPanel.add(downloadButton);
        buttonsPanel.add(whatIsThisButton);
        buttonsPanel.add(exitButton);

        // ---------------
        // Window creation
        // ---------------

        window.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

        window.pack();
        window.setVisible(true);
        window.requestFocus();

        synchronized (SentinelWindow.class) {
            SentinelWindow.class.wait();
        }
    }
}
