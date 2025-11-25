package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    private static final String PATH = "/config.yml";
    private final int min;
    private final int max;
    private final int attempts;

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * Load the config from file
     * 
     * @return a map containing the parameter associated to minimun, maximum e
     *         attemps
     */
    private Map<String, Integer> getConfigurationFromFile() {
        final Map<String, Integer> config = new HashMap<>();
        try (final InputStream is = DrawNumberApp.class.getResourceAsStream(PATH)) {
            if (is == null) {
                throw new IllegalStateException("Configuration file not found " + PATH);
            }

            try (final BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;

                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    final String[] parts = line.split(":");
                    if (parts.length == 2) {
                        final String key = parts[0].trim();
                        final int value = Integer.parseInt(parts[1].trim());
                        config.put(key, value);
                    }
                }

            }

            return config;

        } catch (Exception e) {
            throw new IllegalStateException("Error reading configuration file", e);
        }
    }
    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        final Map<String, Integer> config = getConfigurationFromFile();
        this.min = config.get("minimum");
        this.max = config.get("maximum");
        this.attempts = config.get("attempts");
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        this.model = new DrawNumberImpl(min, max, attempts);
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl(), new DrawNumberViewImpl(),
                new PrintStreamView(System.out), new PrintStreamView("src\\main\\resources\\prova.txt"));
    }

}
