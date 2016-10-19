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
import java.util.Objects;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Phystem
 */
public class RecentItems {

    private final static File RECENT = new File("Recent.txt");

    public static File[][] getRecentItems() {
        List<File[]> files = new ArrayList<>();

        if (RECENT.exists()) {
            try (Scanner scanner = new Scanner(RECENT)) {
                while (scanner.hasNextLine()) {
                    String val = scanner.nextLine();
                    File file = new File(val.split(";")[0]);
                    File xsd = null;
                    if (val.split(";").length > 1) {
                        xsd = new File(val.split(";")[1]);
                    }
                    if (file.exists()) {
                        File[] x = new File[2];
                        x[0] = file;
                        x[1] = xsd;
                        files.add(x);
                    }
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(RecentItems.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return files.toArray(new File[][]{});
    }

    public static void storeRecentItems(List<File[]> files) {
        try (PrintWriter out = new PrintWriter(RECENT)) {
            for (File[] file : files) {
                String val = file[0].getAbsolutePath() + ";";
                if (file[1] != null) {
                    val += file[1].getAbsolutePath();
                }
                out.println(val);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(RecentItems.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
