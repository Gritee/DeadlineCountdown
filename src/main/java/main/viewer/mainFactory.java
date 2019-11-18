package main.viewer;

import localParser.Parser;
import main.controller.GUIController;
import main.viewer.calendarPanel.TitleButton;
import javafx.util.Pair;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * This class can be used to create necessary components for main.
 * Components generated by this class would fit the theme of main:
 * borderless, flat, transparent, etc.
 */
public class mainFactory {
    /**
     * This function would create a JButton that will appear on top of the calendar panel
     * @param text the text that will appear on the button
     * @param backgroundColor the color of the background
     * @param textColor the color of the text
     * @param hoverColor the color when user's mouse is over the button
     * @requires None
     * @modifies None
     * @effects None
     * @return a button with white background and no border
     */
    static JButton createTitleButton(String text, Color backgroundColor, Color textColor, Color hoverColor) {
        LookAndFeel oldTheme = UIManager.getLookAndFeel();
        try {
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException |
                IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }

        JButton resultTitleButton = new TitleButton(text, backgroundColor, textColor, hoverColor);

        try {
            UIManager.setLookAndFeel(oldTheme);
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        return resultTitleButton;
    }

    /**
     * This function would create a JFileChooser for saving/loading feature
     * @param defaultName the default file name
     * @param textResource the ResourceBundle containing all text strings
     * @requires textResource != null
     * @modifies None
     * @effects None
     * @return a file chooser with only txt, csv, ics and json allowed
     */
    public static JFileChooser createFileChooser(String defaultName, ResourceBundle textResource) {
        // save to
        JFileChooser fileChooser = new JFileChooser() {
            @Override
            public void approveSelection() {
                File f = Objects.requireNonNull(getFileFromFileChooser(this)).getKey();
                if (f.exists() && getDialogType() == SAVE_DIALOG) {
                    // overwrite confirmation
                    int result = JOptionPane.showConfirmDialog(this,
                            textResource.getString("exist_file_label"),
                            textResource.getString("exist_file_title"),
                            JOptionPane.YES_NO_CANCEL_OPTION);
                    switch (result) {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.NO_OPTION:
                        case JOptionPane.CLOSED_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            cancelSelection();
                            return;
                    }
                }
                super.approveSelection();
            }
        };
        fileChooser.setAcceptAllFileFilterUsed(false);
        int count = 0;
        for (String ext : Parser.SUPPORTED_EXTIONSION) {
            if (count != 0) {
                fileChooser.addChoosableFileFilter(
                        new FileNameExtensionFilter(Parser.SUPPORTED_EXTIONSION_DESCRIPTION[count], ext));
            }
            count++;
        }
        fileChooser.setFileFilter(new FileNameExtensionFilter(Parser.SUPPORTED_EXTIONSION_DESCRIPTION[0],
                Parser.SUPPORTED_EXTIONSION[0]));
        // set the default filename
        fileChooser.setSelectedFile(new File(defaultName));
        return fileChooser;
    }

    /**
     * This function gets the user selected file from a file Chooser
     * @param fileChooser the file chooser
     * @requires fileChooser != null
     * @modifies None
     * @effects None
     * @return a <File, extension> pair
     */
    public static Pair<File,String> getFileFromFileChooser(JFileChooser fileChooser) {
        String extDesSelected = fileChooser.getFileFilter().getDescription();
        int idx = Arrays.asList(Parser.SUPPORTED_EXTIONSION_DESCRIPTION).indexOf(extDesSelected);
        if (idx == -1) {
            return null;
        }
        String extSelected = Parser.SUPPORTED_EXTIONSION[idx];
        File file;
        File selected = fileChooser.getSelectedFile();
        if (!selected.getAbsolutePath().endsWith(extSelected)) {
            file = new File(selected + "." + extSelected);
        } else {
            file = selected;
        }
        return new Pair<>(file, extSelected);
    }

    /**
     * This function creates the dialog after the user hits the save/load button
     * @param type whether the action is to save or to load
     * @param frame the main JFrame
     * @param parent the GUIController main part
     * @requires frame != null, parent != null
     * @modifies frame, parent
     * @effects creates a dialog to confirm the save/load action
     */
    private static void buttonActionLoadSave(String type, JFrame frame, GUIController parent) {
        String[] options;
        JLabel label;
        if (type.equals("SAVE")) {
            options = new String[]{parent.getFrame().getText("save_option"),
                    parent.getFrame().getText("save_to_option"), parent.getFrame().getText("cancel")};
            label = new JLabel(parent.getFrame().getText("save_label"));
        } else {
            options = new String[]{parent.getFrame().getText("load_option"),
                    parent.getFrame().getText("load_from_option"), parent.getFrame().getText("cancel")};
            label = new JLabel(parent.getFrame().getText("load_label"));
        }
        int n = JOptionPane.showOptionDialog(frame, label,
                "", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        System.out.println("DEBUG: [" + type + " button] user clicks " + n);
        if (n == 0) {
            if (type.equals("SAVE")) {
                parent.saveToLocal(null, "JSON");
            } else {
                parent.loadFromLocal(null, "JSON");
            }
        } else if (n == 1) {
            // save to
            JFileChooser fileChooser = mainFactory.createFileChooser("deadlines",
                    parent.getFrame().getTextResource());
            int result;
            if (type.equals("SAVE"))
                result = fileChooser.showSaveDialog(frame);
            else
                result = fileChooser.showOpenDialog(frame);

            if (result == JFileChooser.APPROVE_OPTION) {
                // If 'Save/Open' is clicked
                Pair<File, String> choice = getFileFromFileChooser(fileChooser);
                if (choice == null) return;
                File file = choice.getKey();
                String extSelected = choice.getValue();
                if (type.equals("SAVE")) {
                    parent.saveToLocal(file, extSelected.toUpperCase());
                } else {
                    parent.loadFromLocal(file, extSelected.toUpperCase());
                }
            }
        }
    }

    /**
     * This function creates the MouseAdapter that activated after the user clicks
     * the save/load button
     * @param frame the main JFrame
     * @param parent the GUIController main part
     * @requires frame != null, parent != null
     * @modifies frame, parent
     * @effects creates a MouseAdapter
     * @return a MouseAdapter with an action that will creates a dialog to confirm the save/load action
     */
    static MouseAdapter createButtonActionLoadSave(String type, JFrame frame, GUIController parent) {
        return new MouseAdapter() {
            /**
             * {@inheritDoc}
             * Invoked when a mouse button has been pressed on a component.
             * @param e the mouse event
             * @requires None
             * @modifies None
             * @effects creates a dialog to confirm the save/load action
             */
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                buttonActionLoadSave(type, frame, parent);
            }
        };
    }

    /**
     * This function would create an empty JPanel with specified transparency and size
     * @param width the preferred / minimum width
     * @param height the absolute height of this area
     * @param isTransparent transparency
     * @requires None
     * @modifies None
     * @effects None
     * @return a new JPanel
     */
    public static JPanel createPanel(int width, int height, boolean isTransparent) {
        JPanel panel = new JPanel();
        panel.setOpaque(!isTransparent);
        panel.setPreferredSize(new Dimension(width, height));
        panel.setMinimumSize(new Dimension(width, height));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        panel.setBorder(BorderFactory.createEmptyBorder());
        panel.setBounds(0,0,0,0);
        return panel;
    }

    /**
     * This function would create an empty area with specified height
     * @param height the absolute height of this area
     * @param isTransparent transparency
     * @requires None
     * @modifies None
     * @effects None
     * @return a empty JPanel
     */
    public static JPanel createEmptyArea(int height, boolean isTransparent) {
        return createPanel(0, height, isTransparent);
    }

    /**
     * This function would create a JButton with Flat design and an icon: Hover effect,
     * grey background and no border. Need to set font manually after calling this function.
     * @param path path to the image file containing the icon
     * @param backgroundColor the color for background
     * @param textColor the color for the text
     * @param hoverColor the hover effect color
     * @requires None
     * @modifies None
     * @effects None
     * @return a new JButton
     */
    static JButton createSettingsToolbarButton(String path, Color backgroundColor, Color textColor, Color hoverColor) {
        JButton result = createSimpleButton("", backgroundColor, textColor);
        try {
            URL iconPath = GUIController.class.getResource(path);
            // ImageIcon saveIcon = new ImageIcon(iconPath);
            // saveIcon = new ImageIcon(saveIcon.getImage().getScaledInstance(35, 35,  Image.SCALE_SMOOTH));

            BufferedImage img = ImageIO.read(iconPath);
            BufferedImage newImage = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());

            // change the color of the icon
            int color;
            int alpha;
            for (int i = 0; i < img.getWidth(); i++) {
                for (int j = 0; j < img.getHeight(); j++) {
                    color = img.getRGB(i, j);
                    alpha = color >> 24;
                    if (alpha != 0) {
                        newImage.setRGB(i, j, textColor.getRGB());
                    }
                    else {
                        newImage.setRGB(i, j, color);
                    }
                }
            }

            // resize
            ImageIcon saveIcon = new ImageIcon(newImage);
            saveIcon = new ImageIcon(saveIcon.getImage().getScaledInstance(35, 35,  Image.SCALE_SMOOTH));

            result.setIcon(saveIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> result.addMouseListener(getHoverEffect(result, hoverColor)));
        result.setMaximumSize(new Dimension(55,55));
        result.setMinimumSize(new Dimension(55,55));
        result.setPreferredSize(new Dimension(55,55));
        return result;
    }

    /**
     * This function will create a flat design JToolBar: transparent background and
     * no border
     * @requires None
     * @modifies None
     * @effects None
     * @return a new JToolBar
     */
    public static JToolBar createToolbar() {
        JToolBar result = new JToolBar();
        result.setFloatable(false);
        result.setBorder(BorderFactory.createEmptyBorder());
        result.setBorderPainted(false);
        result.setMargin(new Insets(0, 0, 0, 0));
        return result;
    }

    /**
     * This function would set the style of a JMenuItem to a specified style
     * @param menuItem the JMenuItem which should be set to have a flat style
     * @param textColor the color for right click menu text
     * @param backgroundColor the color for right click menu background
     * @requires None
     * @modifies None
     * @effects show the popup menu
     */
    private static void setMenuItemStyle(JMenuItem menuItem,
                                         Color textColor, Color backgroundColor) {
        menuItem.setBackground(backgroundColor);
        menuItem.setForeground(textColor);
    }

    /**
     * This function generates the right-click menu for DeadlineBlock and
     * DeadlineInfoBlock that the user can use the menu to delete a deadline
     * @param parent the JComponent that will have the right-click menu
     * @param textColor the color for right click menu text
     * @param backgroundColor the color for right click menu background
     * @requires parent != null
     * @modifies None
     * @effects None
     * @return a new mouse adapter
     */
    public static JPopupMenu createDeadlineBlockRightMenu(DeadlineBlockInterface parent,
                                                   Color textColor, Color backgroundColor) {
        JPopupMenu menu = new JPopupMenu();
        // DELETE * FROM deadlines WHERE deadline = ?
        JMenuItem deleteMenu = new JMenuItem("Delete (D)");
        deleteMenu.addMenuKeyListener(new MenuKeyListener() {
            @Override
            public void menuKeyTyped(MenuKeyEvent e) {
            }
            @Override
            public void menuKeyPressed(MenuKeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_D) {
                    parent.delete();
                }
            }
            @Override
            public void menuKeyReleased(MenuKeyEvent e) {
            }
        });
        setMenuItemStyle(deleteMenu, textColor, backgroundColor);
        deleteMenu.addActionListener(e2 -> parent.delete());
        menu.add(deleteMenu);

        // DELETE * FROM deadlines WHERE deadline = ?
        JMenuItem editMenu = new JMenuItem("Edit (E)");
        editMenu.addMenuKeyListener(new MenuKeyListener() {
            @Override
            public void menuKeyTyped(MenuKeyEvent e) {
            }

            @Override
            public void menuKeyPressed(MenuKeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_E) {
                    parent.edit();
                }
            }

            @Override
            public void menuKeyReleased(MenuKeyEvent e) {
            }
        });
        setMenuItemStyle(editMenu, textColor, backgroundColor);
        editMenu.addActionListener(e2 -> parent.edit());
        menu.add(editMenu);

        // export
        JMenuItem exportMenu = new JMenuItem("Export to...  ");
        setMenuItemStyle(exportMenu, textColor, backgroundColor);
        exportMenu.addActionListener(e2 -> parent.export());

        menu.setForeground(textColor);
        menu.setBackground(backgroundColor);
        menu.setBorder(BorderFactory.createLineBorder(backgroundColor, 1));
        return menu;
    }

    /**
     * This function generates the action showing right-click menu for DeadlineBlock
     * and DeadlineInfoBlock that the user can use the menu to delete a deadline
     * @param menu the right-click menu for DeadlineBlock
     * @requires parent != null
     * @modifies None
     * @effects None
     * @return a new mouse adapter
     */
    public static MouseListener createRightClickMenuAction(JPopupMenu menu){
        return new MouseAdapter() {
            /**
             * {@inheritDoc}
             * Invoked when a mouse button has been pressed on a component.
             *
             * @param e the mouse event
             * @requires None
             * @modifies None
             * @effects show the popup menu
             */
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger())
                    menu.show(e.getComponent(), e.getX(), e.getY());
            }
        };
    }

    /**
     * This function would create a JButton with Flat design and text over the button:
     * Hover effect, grey background and no border. Need to set font manually after
     * calling this function.
     * @param text the text on the button
     * @param backgroundColor the color for background
     * @param textColor the color for the text
     * @requires None
     * @modifies None
     * @effects None
     * @return a new JButton
     */
    public static JButton createSimpleButton(String text, Color backgroundColor, Color textColor) {
        LookAndFeel oldTheme = UIManager.getLookAndFeel();
        try {
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException |
                IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        JButton result = new JButton(text);
        result.setMargin(new Insets(0, 0, 0, 0));
        result.setBackground(backgroundColor);
        result.setBorderPainted(false);
        result.setFocusPainted(false);
        result.setForeground(textColor);
        try {
            UIManager.setLookAndFeel(oldTheme);
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * This function generates the hover effects that can be used on side bar buttons
     * It changes color when the user move the mouse above the parent component and
     * change the color back when the user move the mouse away
     * @param parent the JComponent that will have the hover effect
     * @param hoverColor the hover effect color
     * @requires parent != null
     * @modifies None
     * @effects None
     * @return a new mouse adapter
     */
    public static MouseAdapter getHoverEffect(JComponent parent, Color hoverColor) {
        return new MouseAdapter() {
            /**
             * This function will be called once the user move the mouse over the button
             * (Invoked when the mouse enters a component)
             * @param e the mouse event
             * @requires None
             * @modifies parent
             * @effects change parent's background color
             */
            public void mouseEntered(MouseEvent e) {
                parent.setBackground(hoverColor);
                parent.setOpaque(true);
            }
            /**
             * This function will be called once the user move the mouse away from the
             * button. (Invoked when the mouse exits a component)
             * @param e the mouse event
             * @requires None
             * @modifies parent
             * @effects change parent's background color
             */
            public void mouseExited(MouseEvent e) {
                parent.setBackground(null);
                parent.setOpaque(false);
            }
        };
    }
}
