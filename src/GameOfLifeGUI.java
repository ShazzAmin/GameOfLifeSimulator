import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import javax.swing.BorderFactory;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;

/**
 * A graphical user interface for <code>GameOfLife</code>.
 *
 * @author Shazz Amin
 * @version 1.0 2015-04-28
 * @version 2.0 2015-05-03
 */
public class GameOfLifeGUI
{
    // class fields
    private static final Color ALIVE_CELL = Color.GREEN;
    private static final Color DEAD_CELL = Color.GRAY;
    private static final int DEFAULT_SIMULATION_DELAY = 500;
    private static final String FILE_FORMAT = "gol";
    private static final int HOW_TO_USE_DIALOG_HEIGHT = 9;
    private static final Image ICON = (new ImageIcon(GameOfLifeGUI.class.getResource("icon.png"))).getImage();
    private static final int INFO_BAR_HEIGHT = 3;
    private static final int INFO_BAR_WIDTH = 1;
    private static final int MOUSED_OVER_CELL_ALPHA = 100;
    private static final int SIMULATION_CHECK_DELAY = 5;
    private static final int SIMULATION_DELAY_MAXIMUM = 5000;
    private static final int SIMULATION_DELAY_MINIMUM = 100;
    private static final int SIMULATION_DELAY_STEP = 100;
    private static final int SPEED_DIALOG_HEIGHT = 3;
    private static final double VERSION = 1.0;

    // instance fields
    private CellPanel[][] cellPanels;
    private File currentFile;
    private JFrame frame;
    private JLabel generationLabel;
    private JPanel grid;
    private boolean hasChanged;
    private JLabel heightLabel;
    private boolean isSimulating;
    private GameOfLife simulation;
    private int simulationDelay;
    private JLabel statusLabel;
    private JLabel widthLabel;

    /*
     * constructor
     */

    /**
     * Creates a new Graphical User Interface for <code>GameOfLife</code>.
     */
    public GameOfLifeGUI()
    {
        hasChanged = false;
        isSimulating = false;
        simulationDelay = DEFAULT_SIMULATION_DELAY;
        makeFrame();
    }

    /*
     * methods
     */

    private void createNew()
    {
        stopSimulation();

        if (hasChanged && !promptToSave()) return;

        Dimension dimension = promptForDimensions();
        if (dimension == null) return;

        currentFile = null;
        setHasChanged(true);

        int width = (int)dimension.getWidth();
        int height = (int)dimension.getHeight();

        boolean[][] cells = new boolean[height][width];
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                cells[y][x] = false;
            }
        }

        simulation = new GameOfLife(cells);

        drawNewSimulation();
        statusLabel.setText("New simulation created.");
    }

    private void drawNewSimulation()
    {
        int width = simulation.getWidth();
        int height = simulation.getHeight();
        boolean[][] cells = simulation.getCells();
        cellPanels = new CellPanel[height][width];
        grid.removeAll();
        grid.setLayout(new GridLayout(height, width));
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                cellPanels[y][x] = new CellPanel(x, y);
                cellPanels[y][x].setState(cells[y][x]);
                grid.add(cellPanels[y][x]);
            }
        }

        grid.revalidate();
        grid.repaint();

        generationLabel.setText(Integer.toString(
            simulation.getCurrentGeneration()));
        widthLabel.setText(Integer.toString(simulation.getWidth()));
        heightLabel.setText(Integer.toString(simulation.getHeight()));
    }

    private void makeFrame()
    {
        frame = new JFrame("Game of Life Simulator");
        frame.setIconImage(ICON);
        frame.setJMenuBar(makeMenuBar());

        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(makeGrid(), BorderLayout.CENTER);
        contentPane.add(makeInfoBar(), BorderLayout.SOUTH);

        frame.addWindowListener(
            new WindowListener()
            {
                public void windowActivated(WindowEvent event) { }

                public void windowClosed(WindowEvent event) { }

                public void windowClosing(WindowEvent event)
                {
                    quit();
                }

                public void windowDeactivated(WindowEvent event) { }

                public void windowDeiconified(WindowEvent event) { }

                public void windowIconified(WindowEvent event) { }

                public void windowOpened(WindowEvent event) { }
            }
        );

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.pack();
        frame.setVisible(true);
    }

    private JPanel makeGrid()
    {
        grid = new JPanel();
        grid.setBackground(Color.WHITE);

        return grid;
    }

    private JPanel makeInfoBar()
    {
        // Create 'info bar' container.
        JPanel infoBar = new JPanel();
        infoBar.setLayout(new GridLayout(INFO_BAR_HEIGHT, INFO_BAR_WIDTH));

        // Create 'status' label.
        statusLabel = new JLabel("No simulation loaded.");
        infoBar.add(statusLabel);

        // Create 'generation' label.
        Container generationContainer = new Container();
        FlowLayout generationLayout = new FlowLayout(FlowLayout.LEFT);
        generationLayout.setHgap(0);
        generationContainer.setLayout(generationLayout);
        JLabel generationTextLabel = new JLabel("Generation: ");
        generationLabel = new JLabel("?");
        generationContainer.add(generationTextLabel);
        generationContainer.add(generationLabel);
        infoBar.add(generationContainer);

        // Create 'width' and 'height' labels.
        Container dimensionsContainer = new Container();
        FlowLayout dimensionsLayout = new FlowLayout(FlowLayout.LEFT);
        dimensionsLayout.setHgap(0);
        dimensionsContainer.setLayout(dimensionsLayout);
        JLabel widthTextLabel = new JLabel("Width: ");
        widthLabel = new JLabel("?");
        JLabel heightTextLabel = new JLabel(" Height: ");
        heightLabel = new JLabel("?");
        dimensionsContainer.add(widthTextLabel);
        dimensionsContainer.add(widthLabel);
        dimensionsContainer.add(heightTextLabel);
        dimensionsContainer.add(heightLabel);
        infoBar.add(dimensionsContainer);

        return infoBar;
    }

    private JMenuBar makeMenuBar()
    {
        final int SHORTCUT_MASK =
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        final char ELLIPSIS = '\u2026';

        // Create menu bar.
        JMenuBar menuBar = new JMenuBar();

        // Create 'file' menu.
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        // Create 'new' menu item.
        JMenuItem newItem = new JMenuItem("New" + ELLIPSIS);
        fileMenu.add(newItem);
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
            SHORTCUT_MASK));
        newItem.addActionListener((
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    createNew();
                }
            }
        ));

        // Create 'open' menu item.
        JMenuItem openItem = new JMenuItem("Open" + ELLIPSIS);
        fileMenu.add(openItem);
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
            SHORTCUT_MASK));
        openItem.addActionListener((
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    open();
                }
            }
        ));

        // Create 'save' menu item.
        JMenuItem saveItem = new JMenuItem("Save");
        fileMenu.add(saveItem);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
            SHORTCUT_MASK));
        saveItem.addActionListener((
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    save();
                }
            }
        ));

        // Create 'save as' menu item.
        JMenuItem saveAsItem = new JMenuItem("Save As" + ELLIPSIS);
        fileMenu.add(saveAsItem);
        saveAsItem.addActionListener((
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    saveAs();
                }
            }
        ));

        // Create 'quit' menu item.
        JMenuItem quitItem = new JMenuItem("Quit");
        fileMenu.add(quitItem);
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
            SHORTCUT_MASK));
        quitItem.addActionListener((
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    quit();
                }
            }
        ));

        // Create 'controls' menu.
        JMenu controlsMenu = new JMenu("Controls");
        menuBar.add(controlsMenu);

        // Create 'simulate next generation' menu item.
        JMenuItem simulateItem = new JMenuItem("Simulate Next Generation");
        controlsMenu.add(simulateItem);
        simulateItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,
            SHORTCUT_MASK));
        simulateItem.addActionListener((
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    simulateOneGeneration();
                }
            }
        ));

        // Create 'play/pause' menu item.
        JMenuItem playPauseItem = new JMenuItem("Play/Pause");
        controlsMenu.add(playPauseItem);
        playPauseItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
            SHORTCUT_MASK));
        playPauseItem.addActionListener((
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    playPause();
                }
            }
        ));

        // Create 'set speed' menu item.
        JMenuItem setSpeedItem = new JMenuItem("Set Speed" + ELLIPSIS);
        controlsMenu.add(setSpeedItem);
        setSpeedItem.addActionListener((
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    setSpeed();
                }
            }
        ));

        /// Create 'help' menu.
        JMenu helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);

        // Create 'how to use' menu item.
        JMenuItem howToUseItem = new JMenuItem("How to Use" + ELLIPSIS);
        helpMenu.add(howToUseItem);
        howToUseItem.addActionListener((
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    showHowToUseDialog();
                }
            }
        ));

        // Create 'about' menu item.
        JMenuItem aboutItem = new JMenuItem("About Game of Life Simulator" +
            ELLIPSIS);
        helpMenu.add(aboutItem);
        aboutItem.addActionListener((
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    showAboutDialog();
                }
            }
        ));

        return menuBar;
    }

    private void open()
    {
        stopSimulation();

        if (hasChanged && !promptToSave()) return;

        File file = FileManager.getOpenFile(frame);
        if (file != null)
        {
            currentFile = file;
            GameOfLife loadedSimulation =
                FileManager.loadSimulation(currentFile);
            if (loadedSimulation != null)
            {
                simulation = loadedSimulation;
                setHasChanged(false);
                drawNewSimulation();
                statusLabel.setText("Simulation opened.");
            }
            else
            {
                showError("Failed to open.");
            }
        }
    }

    private void playPause()
    {
        if (simulation == null)
        {
            showError("There is no simulation loaded.");
            return;
        }

        if (isSimulating) stopSimulation();
        else startSimulation();
    }

    private int promptForDelay()
    {
        JPanel speedDialogPanel = new JPanel();
        speedDialogPanel.setLayout(new GridLayout(SPEED_DIALOG_HEIGHT, 1));
        speedDialogPanel.add(new JLabel("Delay between each generation."));
        speedDialogPanel.add(new JLabel("Higher value results in slower" +
            " simulation."));
        JPanel delayInputPanel = new JPanel();
        delayInputPanel.setLayout(new FlowLayout());
        JSpinner delaySpinner = new JSpinner(new SpinnerNumberModel(
            simulationDelay, SIMULATION_DELAY_MINIMUM,
            SIMULATION_DELAY_MAXIMUM, SIMULATION_DELAY_STEP));
        delayInputPanel.add(delaySpinner);
        delayInputPanel.add(new JLabel("milliseconds"));
        speedDialogPanel.add(delayInputPanel);
        if (JOptionPane.showConfirmDialog(null, speedDialogPanel,
            "Set Speed", JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION)
        {
            return -1;
        } /* JOptionPane.showConfirmDialog(null, speedDialogPanel, "Set Speed",
             JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE !=
             JOptionPane.OK_OPTION) */

        return ((Integer)delaySpinner.getValue()).intValue();
    }

    private Dimension promptForDimensions()
    {
        JTextField widthField = new JTextField();
        JTextField heightField = new JTextField();

        JPanel dimensionsDialogPanel = new JPanel();
        dimensionsDialogPanel.setLayout(new GridLayout(2, 2));

        dimensionsDialogPanel.add(new JLabel("Width: "));
        dimensionsDialogPanel.add(widthField);

        dimensionsDialogPanel.add(new JLabel("Height: "));
        dimensionsDialogPanel.add(heightField);

        if (JOptionPane.showConfirmDialog(null, dimensionsDialogPanel,
            "New Simulation", JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION)
        {
            return null;
        }

        try
        {
            int width = Integer.parseInt(widthField.getText());
            int height = Integer.parseInt(heightField.getText());

            if (width <= 0)
            {
                showError("Invalid input: width must be greater than 0.");
                return null;
            }

            if (height <= 0)
            {
                showError("Invalid input: height must be greater than 0.");
                return null;
            }

            return new Dimension(width, height);
        }
        catch (NumberFormatException exception)
        {
            showError("Invalid input: dimensions must be integers.");
            return null;
        }
    }

    private boolean promptToSave()
    {
        JPanel savePromptDialogPanel = new JPanel();
        savePromptDialogPanel.setLayout(new GridLayout(2, 1));
        savePromptDialogPanel.add(new JLabel("Are you sure you would like to" +
            " continue?"));
        savePromptDialogPanel.add(new JLabel("Any unsaved changes will be" +
            " lost."));
        int response = JOptionPane.showOptionDialog(null, savePromptDialogPanel,
            "Save Changes?", JOptionPane.DEFAULT_OPTION,
            JOptionPane.WARNING_MESSAGE, null,
            new String[]{"Save", "Don't Save", "Cancel"}, null);

        if (response == 1)
            return true;

        if (response == 0)
        {
            save();
            return true;
        }

        return false;
    }

    private void quit()
    {
        stopSimulation();
        if (hasChanged && !promptToSave()) return;

        frame.setVisible(false);
        frame.dispose();
        System.exit(0);
    }

    private void save()
    {
        if (simulation == null)
        {
            showError("There is no simulation loaded.");
            return;
        }

        stopSimulation();

        if (currentFile == null)
        {
            saveAs();
        }
        else
        {
            if (FileManager.saveSimulation(currentFile, simulation))
            {
                setHasChanged(false);
                statusLabel.setText("Simulation saved.");
            }
            else
            {
                showError("Failed to save.");
            }
        }
    }

    private void saveAs()
    {
        if (simulation == null)
        {
            showError("There is no simulation loaded.");
            return;
        }

        stopSimulation();

        File file = FileManager.getSaveFile(frame);
        if (file != null)
        {
            currentFile = file;
            if (FileManager.saveSimulation(currentFile, simulation))
            {
                setHasChanged(false);
                statusLabel.setText("Simulation saved.");
            }
            else
            {
                showError("Failed to save.");
            }
        }
    }

    private void setHasChanged(boolean hasChanged)
    {
        this.hasChanged = hasChanged;
        updateFrameTitle();
    }

    private void setSpeed()
    {
        if (simulation == null)
        {
            showError("There is no simulation loaded.");
            return;
        }

        stopSimulation();

        int delay = promptForDelay();
        if (delay == -1) return;

        simulationDelay = delay;
    }

    private void showAboutDialog()
    {
        JPanel aboutDialogPanel = new JPanel();
        aboutDialogPanel.setLayout(new GridLayout(2, 1));
        aboutDialogPanel.add(new JLabel("Game of Life Simulator v" + VERSION +
            " by Shazz Amin."));
        aboutDialogPanel.add(new JLabel("A GUI-based simulator for Conway's" +
            " Game of Life."));

        JOptionPane.showConfirmDialog(null, aboutDialogPanel,
            "About Game of Life Simulator", JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String error)
    {
        JOptionPane.showMessageDialog(null, error, "Error",
            JOptionPane.ERROR_MESSAGE);
    }

    private void showHowToUseDialog()
    {
        JPanel howToUseDialogPanel = new JPanel();
        howToUseDialogPanel.setLayout(new GridLayout(HOW_TO_USE_DIALOG_HEIGHT,
            1));

        howToUseDialogPanel.add(new JLabel("<html>Create a new simulation:" +
            " <font color=\"gray\">File > New.</font></html>"));

        howToUseDialogPanel.add(new JLabel("Click on a cell to toggle its" +
            " state."));

        howToUseDialogPanel.add(new JLabel("<html><font color=\"green\">" +
            "GREEN</font> = alive, <font color=\"gray\">GREY</font> = dead" +
            "</html>"));

        howToUseDialogPanel.add(new JLabel("<html>Simulate one generation:" +
            " <font color=\"gray\">Controls > Simulate Next Generation" +
            "</font></html>"));

        howToUseDialogPanel.add(new JLabel("<html>Start an indefinite" +
            " simulation: <font color=\"gray\">Controls > Play/Pause</font>" +
            "</html>"));

        howToUseDialogPanel.add(new JLabel("<html>Pause a running" +
            " simulation: <font color=\"gray\">Controls > Play/Pause</font>" +
            "</html>"));

        howToUseDialogPanel.add(new JLabel("<html>Set speed of simulation:" +
            " <font color=\"gray\">Controls > Set Speed</font></html>"));

        howToUseDialogPanel.add(new JLabel("<html>Save the simulation:" +
            " <font color=\"gray\">File > Save</font> or <font" +
            " color=\"gray\">File > Save As</font></html>"));

        howToUseDialogPanel.add(new JLabel("<html>Open a saved simulation:" +
            " <font color=\"gray\">File > Open</font></html>"));

        JOptionPane.showConfirmDialog(null, howToUseDialogPanel,
            "How to Use", JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void simulateNextGeneration()
    {
        simulation.simulateNextGeneration();
        setHasChanged(true);
        boolean[][] cells = simulation.getCells();
        int width = simulation.getWidth();
        int height = simulation.getHeight();
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                cellPanels[y][x].setState(cells[y][x]);
            }
        }

        generationLabel.setText(Integer.toString(
            simulation.getCurrentGeneration()));
    }

    private void simulateOneGeneration()
    {
        if (simulation == null)
        {
            showError("There is no simulation loaded.");
            return;
        }

        if (isSimulating) return;

        simulateNextGeneration();
        statusLabel.setText("Simulated one generation.");
    }

    private void startSimulation()
    {
        if (isSimulating) return;
        isSimulating = true;
        statusLabel.setText("Simulation started.");
        (new Thread(
            new Runnable()
            {
                public void run()
                {
                    int timeHasPassed = 0;
                    while (isSimulating)
                    {
                        try
                        {
                            Thread.sleep(SIMULATION_CHECK_DELAY);
                            timeHasPassed = timeHasPassed +
                                SIMULATION_CHECK_DELAY;
                            if (timeHasPassed >= simulationDelay)
                            {
                                timeHasPassed = 0;
                                simulateNextGeneration();
                            }
                        }
                        catch (Exception exception)
                        {
                        }
                    }
                }
            }
        )).start();
    }

    private void stopSimulation()
    {
        isSimulating = false;
        statusLabel.setText("Simulation stopped.");
    }

    private void updateFrameTitle()
    {
        if (currentFile != null)
        {
            frame.setTitle("Game of Life Simulator - " +
                currentFile.getName() + (hasChanged ? " *": "") +
                " (" + currentFile.getAbsolutePath() + ")");
        }
        else
        {
            frame.setTitle("Game of Life Simulator" +
                (hasChanged ? " - *" : ""));
        }
    }

    /*
     * main method
     */

    /**
     * Creates a <code>GameOfLifeGUI</code>.
     *
     * @param argument not used
     */
    public static void main(String[] argument)
    {
        new GameOfLifeGUI();
    }

    /*
     * inner classes
     */

    private class CellPanel extends JPanel
    {
        // instance fields
        private boolean isAlive;
        private int x;
        private int y;

        /*
         * constructor
         */

        /**
         * Calls the super class' constructor then sets this
         * <code>CellPanel</code>'s state to dead.
         *
         * @param x the x-coordinate of the cell in the simulation
         * @param y the y-coordinate of the cell in the simulation
         */
        public CellPanel(final int x, final int y)
        {
            super();

            this.x = x;
            this.y = y;
            setState(false);
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            addMouseListener(
                new MouseListener()
                {
                    public void mouseClicked(MouseEvent event) { }

                    public void mouseEntered(MouseEvent event)
                    {
                        Color b = getBackground();
                        setBackground(new Color(b.getRed(), b.getGreen(),
                            b.getBlue(), MOUSED_OVER_CELL_ALPHA));
                        grid.repaint();
                    }

                    public void mouseExited(MouseEvent event)
                    {
                        updateBackground();
                    }

                    public void mousePressed(MouseEvent event)
                    {
                        if (event.getButton() == MouseEvent.BUTTON1)
                        {
                            simulation.setCellState(x, y, !isAlive);
                            setState(!isAlive);
                            setHasChanged(true);
                        }
                    }

                    public void mouseReleased(MouseEvent event) { }
                }
            );
        }

        /*
         * mutator
         */

        /**
         * Sets the state of this <code>CellPanel</code>.
         *
         * @param state <code>true</code> for alive,
         * <code>false</code> for dead
         */
        public void setState(boolean state)
        {
            isAlive = state;
            updateBackground();
        }

        /*
         * methods
         */

        private void updateBackground()
        {
            if (isAlive)
                setBackground(ALIVE_CELL);
            else
                setBackground(DEAD_CELL);
        }
    }

    private static class FileManager
    {
        // class fields
        private static final char ALIVE_CELL = 'A';
        private static final char DEAD_CELL = 'D';
        private static final JFileChooser FILE_CHOOSER =
            new JFileChooser(System.getProperty("user.dir"));
        private static final FileNameExtensionFilter FILE_FILTER =
            new FileNameExtensionFilter("Game of Life Simulation File (." +
                FILE_FORMAT + ")", FILE_FORMAT);

        /*
         * methods
         */

        /**
         * Opens a file chooser and lets the user select a file to open.
         *
         * @param frame the <code>JFrame</code> this file chooser should be a
         * child of
         * @return the selected <code>File</code>; <code>null</code> if
         * user cancelled or file was not found
         */
        public static File getOpenFile(JFrame frame)
        {
            FILE_CHOOSER.setFileFilter(FILE_FILTER);
            if (FILE_CHOOSER.showOpenDialog(frame) !=
                JFileChooser.APPROVE_OPTION)
                return null;

            return FILE_CHOOSER.getSelectedFile();
        }

        /**
         * Opens a file chooser and lets the user select a file to save to.
         *
         * @param frame the <code>JFrame</code> this file chooser should be a
         * child of
         * @return the selected <code>File</code>; <code>null</code> if
         * user cancelled or an error occurred
         */
        public static File getSaveFile(JFrame frame)
        {
            FILE_CHOOSER.setFileFilter(FILE_FILTER);
            if (FILE_CHOOSER.showSaveDialog(frame) !=
                JFileChooser.APPROVE_OPTION)
                return null;

            File file = FILE_CHOOSER.getSelectedFile();
            String filePath = file.getAbsolutePath();
            if (!filePath.endsWith("." + FILE_FORMAT))
                file = new File(filePath + "." + FILE_FORMAT);
            return file;
        }

        /**
         * Loads a <code>File</code> as a <code>GameOfLife</code>.
         *
         * @param file the <code>File</code> to load
         * @return the loaded <code>GameOfLife</code>; <code>null</code>
         * if loading failed
         */
        public static GameOfLife loadSimulation(File file)
        {
            try
            {
                GameOfLife simulation = null;
                BufferedReader reader = new BufferedReader(
                    new FileReader(file));
                try
                {
                    int generation = Integer.parseInt(reader.readLine());
                    int width = Integer.parseInt(reader.readLine());
                    int height = Integer.parseInt(reader.readLine());

                    boolean[][] cells = new boolean[height][width];

                    for (int y = 0; y < height; y++)
                    {
                        String line = reader.readLine();
                        if (line == null)
                            throw new Exception();

                        cells[y] = parseRow(width, line);
                    }

                    simulation = new GameOfLife(cells, generation);
                }
                finally
                {
                    reader.close();
                    return simulation;
                }
            }
            catch (Exception exception)
            {
                return null;
            }
        }

        /**
         * Saves a <code>GameOfLife</code> as a <code>File</code>.
         *
         * @param file the <code>File</code> to save as
         * @param simulation the <code>GameOfLife</code> to save
         * @return <code>true</code> if the operation succeeded,
         * <code>false</code> otherwise
         */
        public static boolean saveSimulation(File file, GameOfLife simulation)
        {
            try
            {
                if (!file.exists())
                    file.createNewFile();

                BufferedWriter writer = new BufferedWriter(
                    new FileWriter(file));

                boolean success = false;
                try
                {
                    writer.write(Integer.toString(
                        simulation.getCurrentGeneration()));
                    writer.newLine();
                    writer.write(Integer.toString(simulation.getWidth()));
                    writer.newLine();
                    writer.write(Integer.toString(simulation.getHeight()));
                    writer.newLine();

                    boolean[][] cells = simulation.getCells();
                    for (int y = 0; y < simulation.getHeight(); y++)
                    {
                        for (int x = 0; x < simulation.getWidth(); x++)
                        {
                            if (cells[y][x])
                                writer.write(ALIVE_CELL);
                            else
                                writer.write(DEAD_CELL);
                        }
                        writer.newLine();
                    }

                    success = true;
                }
                finally
                {
                    writer.close();
                    return success;
                }
            }
            catch (Exception exception)
            {
                return false;
            }
        }

        private static boolean[] parseRow(int width, String line)
            throws Exception
        {
            if (line.length() != width)
                throw new Exception();

            boolean[] row = new boolean[width];

            for (int x = 0; x < width; x++)
            {
                char cell = line.charAt(x);
                if (cell == ALIVE_CELL)
                    row[x] = true;
                else if (cell == DEAD_CELL)
                    row[x] = false;
                else
                    throw new Exception();
            }

            return row;
        }
    }
}
