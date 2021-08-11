import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

/**
 * This file contains the getting started view.
 */

class SettingsView extends JPanel implements ActionListener, PlayerView.Delegate {
    // Offset from the edge of the screen.
    private static final int PADDING = 75;

    // MARK: - Delegate

    public interface Delegate {
        /**
         * Triggered when the game should start.
         */
        void onStart();
        /**
         * Returns the current white player.
         */
        Player white();
        /**
         * Triggered when white player property changes.
         */
        void onWhiteChanged(Player white);
        /**
         * Returns the current black player.
         */
        Player black();
        /**
         * Triggered when white player property changes.
         */
        void onBlackChanged(Player black);
    }

    // MARK: - Properties

    /**
     * The delegate class to communicate with the outer world.
     */
    private final Delegate delegate;

    // MARK: - State

    private final JLabel title;
    private final PlayerView white;
    private final PlayerView black;
    private final JButton start;

    // MARK: - Constructor

    public SettingsView(Delegate delegate) {
        this.delegate = delegate;

        // Layout
        this.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        this.title = new JLabel("BACKGAMMON", JLabel.CENTER);
        this.title.setFont(new Font("Monaco", Font.BOLD, 48));
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 0.3;
        this.add(this.title, c);

        this.white = new PlayerView(this);
        this.white.setBorder(BorderFactory.createLineBorder(Color.black));
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        this.add(this.white, c);

        this.black = new PlayerView(this);
        c.gridx = 1;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        this.add(this.black, c);

        this.start = new JButton("Start");
        c.gridx = 0;
        c.gridy = 2;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 0.2;
        this.add(this.start, c);

        // Events
        this.start.addActionListener(this);
    }

    // MARK: - Accessors

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(500, 500);
    }

    // MARK: - View

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    }



    // MARK: - Events
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.start) this.delegate.onStart();
    }

    @Override
    public String name(PlayerView source) {
        if (source == this.white) return this.delegate.white().name;
        if (source == this.black) return this.delegate.black().name;
        return null;
    }

    @Override
    public Color checker(PlayerView source) {
        if (source == this.white) return this.delegate.white().checker;
        if (source == this.black) return this.delegate.black().checker;
        return null;
    }

    @Override
    public Color point(PlayerView source) {
        if (source == this.white) return this.delegate.white().point;
        if (source == this.black) return this.delegate.black().point;
        return null;
    }

    @Override
    public void onNameChanged(PlayerView.NameChangedEvent event) {
        if (event.getSource() == this.white) {
            Player white = this.delegate.white();
            white.name = event.name;
            this.delegate.onWhiteChanged(white);
        }
        if (event.getSource() == this.black) {
            Player black = this.delegate.black();
            black.name = event.name;
            this.delegate.onBlackChanged(black);
        }
    }

    @Override
    public void onCheckerChanged(PlayerView.ColorChangedEvent event) {
        if (event.getSource() == this.white) {
            Player white = this.delegate.white();
            white.checker = event.color;
            this.delegate.onWhiteChanged(white);
        }
        if (event.getSource() == this.black) {
            Player black = this.delegate.black();
            black.checker = event.color;
            this.delegate.onBlackChanged(black);
        }
    }

    @Override
    public void onPointChanged(PlayerView.ColorChangedEvent event) {
        if (event.getSource() == this.white) {
            Player white = this.delegate.white();
            white.point = event.color;
            this.delegate.onWhiteChanged(white);
        }
        if (event.getSource() == this.black) {
            Player black = this.delegate.black();
            black.point = event.color;
            this.delegate.onBlackChanged(black);
        }
    }

}
