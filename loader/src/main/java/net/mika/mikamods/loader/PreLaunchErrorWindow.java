package net.mika.mikamods.loader;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class PreLaunchErrorWindow {

    /** Show the Swing window on the current JVM */
    public static void show(String title, List<String> errors) {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println(title);
            for (String e : errors) System.err.println("- " + e);
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(title);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setMinimumSize(new Dimension(500, 400));

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            for (String e : errors) {
                JLabel lbl = new JLabel(e);
                lbl.setForeground(Color.RED);
                panel.add(lbl);
            }

            JButton closeBtn = new JButton("Close");
            closeBtn.addActionListener(ev -> {
                frame.dispose();
                System.exit(1);
            });
            panel.add(closeBtn);

            frame.add(new JScrollPane(panel));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    public static File getLoaderJar() throws URISyntaxException {
        URI uri = PreLaunchErrorWindow.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI();

        String path = uri.toString();

        // If it starts with "jar:", strip it
        if (path.startsWith("jar:")) {
            path = path.substring(4);
        }

        // If it contains "!", strip everything after it
        int excl = path.indexOf('!');
        if (excl != -1) {
            path = path.substring(0, excl);
        }

        // Remove "file:" prefix
        if (path.startsWith("file:/")) {
            path = path.substring(6); // on Windows
            // convert slashes
            path = path.replace('/', File.separatorChar);
        }

        return new File(path);
    }

    /** Fork a new JVM to show the error window */
    public static void forkAndShow(String title, List<String> errors) {
        try {
            String jarPath = getLoaderJar().getAbsolutePath();

            String errorArg = String.join(";", errors);
            String javaBin = System.getProperty("java.home") + "/bin/java";

            ProcessBuilder pb = new ProcessBuilder(
                    javaBin,
                    "-cp", jarPath,
                    PreLaunchErrorWindow.class.getName(),
                    title,
                    errorArg
            );

            pb.inheritIO();
            Process p = pb.start();

            int exit = p.waitFor();
            if (exit != 0) {
                System.exit(1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            show(title, errors);
        }
    }

    /** Main method to run in a separate JVM */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java PreLaunchErrorWindow <title> <error1;error2;...>");
            System.exit(1);
        }

        String title = args[0];
        List<String> errors = Arrays.asList(args[1].split(";"));

        show(title, errors);
    }
}