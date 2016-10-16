/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlviewer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Phystem
 */
public class RecentItems {

    private final static File RECENT = new File("Recent.txt");

    public static File[] getRecentItems() {
        List<File> files = new ArrayList<>();

        if (RECENT.exists()) {
            try (Scanner scanner = new Scanner(RECENT)) {
                while (scanner.hasNextLine()) {
                    String val = scanner.nextLine();
                    File file = new File(val);
                    if (file.exists()) {
                        files.add(file);
                    }
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(RecentItems.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return files.toArray(new File[]{});
    }

    public static void storeRecentItems(List<File> files) {
        try (PrintWriter out = new PrintWriter(RECENT)) {
            for (File file : files) {
                out.println(file.getAbsolutePath());
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(RecentItems.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
