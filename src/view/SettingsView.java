package view;

import view.PlayerView;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import model.*;

/**
 * This file contains the getting started view.
 */

public class SettingsView extends JPanel implements ActionListener, PlayerView.Delegate {
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
        c.weighty = 0.2;
        this.add(this.title, c);

        this.white = new PlayerView(this.delegate.white(), this);
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 1;
        c.weightx = 0.5;
        c.weighty = 0.6;
        c.insets = new Insets(10, 30, 10, 30);
        this.add(this.white, c);

        this.black = new PlayerView(this.delegate.black(), this);
        c.gridx = 1;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.insets = new Insets(10, 30, 10, 30);
        this.add(this.black, c);

        this.start = new JButton("Start");
        c.gridx = 0;
        c.gridy = 2;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 0.2;
        c.insets = new Insets(30, 30, 30, 30);
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
    public Player player(PlayerView source) {
        if (source == this.white) return this.delegate.white();
        if (source == this.black) return this.delegate.black();
        return null;
    }

    @Override
    public void onPlayerChange(PlayerView.PlayerChangedEvent event) {
        if (event.getSource() == this.white) {
            this.delegate.onWhiteChanged(event.player);
        }
        if (event.getSource() == this.black) {
            this.delegate.onBlackChanged(event.player);
        }
    }
}
