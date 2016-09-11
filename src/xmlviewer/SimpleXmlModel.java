/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlviewer;

import java.io.File;
import java.util.Objects;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author Phystem
 */
class SimpleXmlModel {

    private final File file;
    DefaultTreeModel treeModel;

    public SimpleXmlModel(File file) {
        this.file = file;
        treeModel = Utils.getTreeModel(file);
    }

    public void reload() {
        treeModel = Utils.getTreeModel(file);
    }

    public void save() {
        Utils.saveFromTreeModel(treeModel, file);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.file);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SimpleXmlModel other = (SimpleXmlModel) obj;
        return Objects.equals(this.file.getAbsolutePath(), other.file.getAbsolutePath());
    }

    @Override
    public String toString() {
        return file.getName().substring(0, file.getName().lastIndexOf(".xml"));
    }

}
