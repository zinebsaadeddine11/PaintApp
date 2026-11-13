package com.example.paint;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class PaintFrame extends JFrame {
    private final PaintCanvas canvas = new PaintCanvas();
    private final JLabel statusLabel = new JLabel("Prêt");
    private final JButton colorButton = new JButton();
    private final JComboBox<String> sizeBox = new JComboBox<>(new String[]{"1 px", "2 px", "4 px", "6 px", "10 px", "16 px", "24 px"});

    public PaintFrame() {
        super("PaintJava");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 650));
        setSize(1100, 760);
        setLocationByPlatform(true);
        setLayout(new BorderLayout());
        add(createTopBar(), BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
        installShortcuts();
        updateColorButton();
    }

    private JPanel createTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new EmptyBorder(8, 8, 8, 8));
        top.setBackground(new Color(245, 247, 250));
        top.add(createToolBar(), BorderLayout.CENTER);
        top.add(createActionsPanel(), BorderLayout.EAST);
        return top;
    }

    private JToolBar createToolBar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder());

        ButtonGroup tools = new ButtonGroup();
        addTool(toolbar, tools, "Crayon", "PENCIL", true);
        addTool(toolbar, tools, "Gomme", "ERASER", false);
        addTool(toolbar, tools, "Ligne", "LINE", false);
        addTool(toolbar, tools, "Rectangle", "RECTANGLE", false);
        addTool(toolbar, tools, "Ellipse", "ELLIPSE", false);

        toolbar.addSeparator(new Dimension(12, 0));
        toolbar.add(new JLabel("Taille :"));
        sizeBox.setPreferredSize(new Dimension(78, 28));
        sizeBox.addActionListener(event -> {
            String selectedSize = (String) sizeBox.getSelectedItem();
            canvas.setStrokeWidth(Integer.parseInt(selectedSize.replace(" px", "")));
        });
        toolbar.add(sizeBox);
        toolbar.addSeparator(new Dimension(12, 0));
        toolbar.add(new JLabel("Couleur :"));
        colorButton.setPreferredSize(new Dimension(42, 28));
        colorButton.setToolTipText("Choisir une couleur");
        colorButton.addActionListener(event -> chooseColor());
        toolbar.add(colorButton);
        return toolbar;
    }

    private void addTool(JToolBar toolbar, ButtonGroup group, String label, String tool, boolean selected) {
        JToggleButton button = new JToggleButton(label);
        button.setFocusable(false);
        button.setMargin(new Insets(4, 8, 4, 8));
        button.addActionListener(event -> {
            canvas.setTool(PaintCanvas.Tool.valueOf(tool));
            statusLabel.setText(label + " sélectionné");
        });
        group.add(button);
        button.setSelected(selected);
        toolbar.add(button);
    }

    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        panel.setOpaque(false);
        panel.add(actionButton("Annuler", event -> canvas.undo()));
        panel.add(actionButton("Rétablir", event -> canvas.redo()));
        panel.add(actionButton("Effacer", event -> canvas.clear()));
        panel.add(actionButton("Ouvrir", event -> openImage()));
        panel.add(actionButton("Enregistrer", event -> saveImage()));
        return panel;
    }

    private JButton actionButton(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFocusable(false);
        button.addActionListener(listener);
        return button;
    }

    private JPanel createStatusBar() {
        JPanel status = new JPanel(new BorderLayout());
        status.setBorder(new EmptyBorder(5, 10, 5, 10));
        status.setBackground(new Color(238, 241, 245));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 12f));
        status.add(statusLabel, BorderLayout.WEST);
        JLabel hint = new JLabel("Astuce : utilisez Ctrl+Z / Ctrl+Y", SwingConstants.RIGHT);
        hint.setFont(hint.getFont().deriveFont(Font.PLAIN, 12f));
        status.add(hint, BorderLayout.EAST);
        return status;
    }

    private void chooseColor() {
        Color color = JColorChooser.showDialog(this, "Choisir une couleur", canvas.getDrawingColor());
        if (color != null) {
            canvas.setDrawingColor(color);
            updateColorButton();
            statusLabel.setText("Couleur modifiée");
        }
    }

    private void updateColorButton() {
        colorButton.setBackground(canvas.getDrawingColor());
        colorButton.setOpaque(true);
        colorButton.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
    }

    private void saveImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Enregistrer le dessin");
        chooser.setSelectedFile(new File("mon-dessin.png"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = ensurePngExtension(chooser.getSelectedFile());
        try {
            ImageIO.write(canvas.getImage(), "png", file);
            statusLabel.setText("Dessin enregistré : " + file.getName());
        } catch (IOException exception) {
            showError("Impossible d'enregistrer l'image.", exception);
        }
    }

    private void openImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Ouvrir une image");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            BufferedImage image = ImageIO.read(chooser.getSelectedFile());
            if (image == null) {
                throw new IOException("Format d'image non reconnu.");
            }
            canvas.setImage(image);
            statusLabel.setText("Image ouverte : " + chooser.getSelectedFile().getName());
        } catch (IOException exception) {
            showError("Impossible d'ouvrir l'image.", exception);
        }
    }

    private File ensurePngExtension(File file) {
        return file.getName().toLowerCase().endsWith(".png")
                ? file
                : new File(file.getParentFile(), file.getName() + ".png");
    }

    private void showError(String message, Exception exception) {
        JOptionPane.showMessageDialog(this, message + "\n" + exception.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    private void installShortcuts() {
        getRootPane().getInputMap().put(KeyStroke.getKeyStroke("control Z"), "undo");
        getRootPane().getActionMap().put("undo", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                canvas.undo();
            }
        });
        getRootPane().getInputMap().put(KeyStroke.getKeyStroke("control Y"), "redo");
        getRootPane().getActionMap().put("redo", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                canvas.redo();
            }
        });
    }
}
