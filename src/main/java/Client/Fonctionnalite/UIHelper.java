package Client.Fonctionnalite;

import Client.ClientUI;

import javax.swing.*;
import java.awt.*;

public class UIHelper {

    private static JPanel contentPane;
    private static JPanel topPanel;
    private static JPanel sidebarPanel;

    /**
     * UIHelper est une classe utilitaire pour configurer l'interface utilisateur principale de l'application client.
     */
    public static void setupUI(ClientUI clientUI) {

        /**
         * Configure l'interface utilisateur de la fenêtre principale du client.
         *
         * @param clientUI l'instance de ClientUI à configurer
         */
        Dimension localScreenSize = Toolkit.getDefaultToolkit().getScreenSize();

        clientUI.setSize((int) (localScreenSize.getWidth() * 0.8), (int) (localScreenSize.getHeight() * 0.8));
        clientUI.setTitle("RemoteDesk");
        clientUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        clientUI.setLocationRelativeTo(null);
        clientUI.setLayout(new BorderLayout());

        contentPane = new JPanel(new BorderLayout());
        clientUI.setContentPane(contentPane);

        topPanel = createTopPanel();
        contentPane.add(topPanel, BorderLayout.NORTH);
        ClientUI.topPanel = topPanel;

        sidebarPanel = createSidebarPanel();
        contentPane.add(sidebarPanel, BorderLayout.WEST);
        ClientUI.sidebarPanel = sidebarPanel;

        JLabel screenLabel = new JLabel();
        contentPane.add(screenLabel, BorderLayout.CENTER);

        clientUI.screenLabel = screenLabel;
    }

    /**
     * Crée le panneau supérieur de l'interface utilisateur.
     *
     * @return le panneau supérieur configuré
     */
    private static JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel appNameLabel = new JLabel("RemoteDesk");
        appNameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        topPanel.add(appNameLabel);
        return topPanel;
    }

    /**
     * Crée le panneau latéral de l'interface utilisateur.
     *
     * @return le panneau latéral configuré
     */
    private static JPanel createSidebarPanel() {
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Définir les dimensions de la barre latérale
        sidebarPanel.setPreferredSize(new Dimension(450, 500));
        sidebarPanel.setMaximumSize(new Dimension(400, 500));
        sidebarPanel.setMinimumSize(new Dimension(200, 250));
        JLabel instructionsLabel = new JLabel("Instructions:");
        Font font = new Font("Arial", Font.BOLD, 20);
        instructionsLabel.setFont(font);
        instructionsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebarPanel.add(instructionsLabel);

        JTextArea instructionsArea = new JTextArea("\n Veuillez suivre ces étapes pour accéder au serveur désiré :\n\n" +
                "1. Consultez la liste des serveurs disponibles affichée ci-dessous.\n\n" +
                "2. Sélectionnez le serveur auquel vous souhaitez vous connecter en cliquant dessus.\n\n" +
                "3. Saisissez le code généré par le serveur pour l'authentification.\n\n" +
                "4. Cliquez sur le bouton 'Ok' pour démarrer la session à distance une fois que vous avez saisi le code.");
        instructionsArea.setEditable(false);
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        sidebarPanel.setFont(new Font("Arial", Font.BOLD, 17));
        sidebarPanel.add(new JScrollPane(instructionsArea));


        // Modifier la taille de la police du texte dans la barre latérale
        Font sidebarFont = new Font("Arial", Font.PLAIN, 17); // Modifier la taille de la police ici
        instructionsArea.setFont(sidebarFont);

        return sidebarPanel;
    }
}
