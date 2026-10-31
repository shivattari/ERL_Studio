package ERL;

import ERL.UI.SplashWindow;
import ERL.UI.WorkspaceSelector;
import com.formdev.flatlaf.FlatIntelliJLaf;

import javax.swing.*;
import java.awt.*;

/**
 * The entry point of the IDE.
 * <p>
 * Throughout this project, I used the following resources:
 * [1] JetBrains Mono Font: https://www.jetbrains.com/lp/mono/
 * [2] Heroicons: https://heroicons.com/
 * [3] FlatLaf: https://www.formdev.com/flatlaf/
 * <p>
 * Other resources that I used are referenced next to the appropriate code snippets.
 *
 * @author Shivam Attari
 */
public class Main
{
    public static void main(String[] args)
    {
        // Display splash screen
        SplashWindow splash = new SplashWindow();
        splash.setVisible(true);

        try
        {
            Thread.sleep(250);
        }
        catch (InterruptedException ignored)
        {
        }

        SwingUtilities.invokeLater(() ->
        {
            FlatIntelliJLaf.setup();
            UIManager.put("TextComponent.arc", 6);
            UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
            UIManager.put("MenuItem.minimumIconSize", new Dimension(8, 8));

            WorkspaceSelector workspaceSelector = new WorkspaceSelector();
            workspaceSelector.setVisible(true);
        });
    }
}