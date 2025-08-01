/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;

/**
 * Provides functions to save the state of a View during a session, even when the view is closed and re-opened 
 * saves the state in an XML-File in the Plugins-Path (Standard: .metadata in the workspace).
 *
 * @author SebastianRaffel ( 24.05.2005 ), Philippe Herlin, Sven Jacob
 *
 */
public class ViewMemento {
    private final IPath path;
    private final File file;
    private XMLMemento memento;

    protected static final String XML_PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    protected static final String LIST_SEPARATOR = ":";
    protected static final String MEMENTO_PREFIX = "memento";
    protected static final String ATTRIBUTE_PREFIX = "attribute";
    protected static final String ATTR_NAME = "name";
    protected static final String ATTR_VALUE = "value";

    /**
     * Constructor Searches for the XML-File, where the Memento should be saved and creates it if there is none.
     *
     * @param type, a String identifying the View, used for the File's Name
     */
    public ViewMemento(String type) {
        this.path = PMDPlugin.getDefault().getStateLocation();
        this.file = new File(this.path.toString(), type);

        // we check for an existing XML-File
        // and create one, if needed
        if (!this.file.exists() || !checkForXMLFile(this.file)) {
            createNewFile(this.file);
        }

        // then we create a ReadRoot for the Memento
        try (BufferedReader reader = Files.newBufferedReader(this.file.toPath(), StandardCharsets.UTF_8)) {
            this.memento = XMLMemento.createReadRoot(reader);
        } catch (WorkbenchException wbe) {
            PMDPlugin.getDefault().logError(StringKeys.ERROR_VIEW_EXCEPTION + this.toString(), wbe);
        } catch (FileNotFoundException fnfe) {
            PMDPlugin.getDefault().logError(
                    StringKeys.ERROR_FILE_NOT_FOUND + path.toString() + "/" + type + " in " + this.toString(), fnfe);
        } catch (IOException e) {
            PMDPlugin.getDefault().logError(StringKeys.ERROR_IO_EXCEPTION + this.toString(), e);
        }

        // Validate that the memento has been built
        if (this.memento == null) {
            throw new IllegalStateException("Memento has not been built correctly. Check error log for details");
        }
    }

    /**
     * Creates a new XML-Structure in a given File.
     *
     * @param file
     */
    protected final void createNewFile(File file) {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write(XML_PREFIX + "\n" + "<" + MEMENTO_PREFIX + "/>");
        } catch (IOException ioe) {
            PMDPlugin.getDefault().logError(StringKeys.ERROR_IO_EXCEPTION + this.toString(), ioe);
        }
    }

    /**
     * Checks for an XML-Structure in a File.
     *
     * @param file
     * @return true, if the File is a XML-File we can use, false otherwise
     */
    protected final boolean checkForXMLFile(File file) {
        boolean isXmlFile = false;
        try (BufferedReader contentReader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            while (contentReader.ready()) {
                String line = contentReader.readLine();
                if (line != null && line.length() != 0) {
                    // the first Line of Text has to be the XML-Prefix
                    isXmlFile = XML_PREFIX.equalsIgnoreCase(line);
                    break;
                }
            }
        } catch (FileNotFoundException fnfe) {
            PMDPlugin.getDefault().logError(StringKeys.ERROR_FILE_NOT_FOUND + file.toString() + " in " + this.toString(),
                    fnfe);
        } catch (IOException ioe) {
            PMDPlugin.getDefault().logError(StringKeys.ERROR_IO_EXCEPTION + this.toString(), ioe);
        }

        return isXmlFile;
    }

    /**
     * Saves the Memento into the File.
     *
     * @param type
     */
    public void save() {
        if (this.memento != null) {
            try (BufferedWriter writer = Files.newBufferedWriter(this.file.toPath(), StandardCharsets.UTF_8)) {
                this.memento.save(writer);
            } catch (IOException ioe) {
                PMDPlugin.getDefault().logError(StringKeys.ERROR_IO_EXCEPTION + this.toString(), ioe);
            }
        }
    }

    /**
     * Returns a Memento with the given Attribute.
     *
     * @param name
     * @return a Memento
     */
    private IMemento getAttribute(String name) {
        final IMemento[] mementos = this.memento.getChildren(ATTRIBUTE_PREFIX);
        IMemento mem = null;

        for (IMemento memento2 : mementos) {
            final String attrName = memento2.getString(ATTR_NAME);
            if (name.equalsIgnoreCase(attrName)) {
                mem = memento2;
            }
        }

        if (mem == null) {
            mem = this.memento.createChild(ATTRIBUTE_PREFIX);
            mem.putString(ATTR_NAME, name);
        }

        return mem;
    }

    /**
     * Puts a String into a Memento.
     *
     * @param key
     * @param value
     */
    public void putString(String key, String value) {
        final IMemento mem = getAttribute(key);
        mem.putString(ATTR_VALUE, value);
    }

    /**
     * Puts an Integer into a Memento.
     *
     * @param key
     * @param value
     */
    public void putInteger(String key, int value) {
        final IMemento mem = getAttribute(key);
        mem.putInteger(ATTR_VALUE, value);
    }

    /**
     * Puts a Float into a Memento.
     *
     * @param key
     * @param value
     */
    public void putFloat(String key, float value) {
        final IMemento mem = getAttribute(key);
        mem.putFloat(ATTR_VALUE, value);
    }

    /**
     * puts an List into a Memento, the List is changed into a delimited String.
     *
     * @param key
     * @param valueList
     */
    public <T extends Object> void putList(String key, List<T> valueList) {
        if (valueList.isEmpty()) {
            putString(key, ""); // even necessary?
            return;
        }
        
        final StringBuilder sb = new StringBuilder(String.valueOf(valueList.get(0)));
        for (int k = 1; k < valueList.size(); k++) {
            sb.append(LIST_SEPARATOR).append(valueList.get(k));
        }

        putString(key, sb.toString());
    }

    /**
     * Gets a String from a Memento.
     *
     * @param key
     * @return a String with the Value
     */
    public String getString(String key) {
        final IMemento mem = getAttribute(key);
        return mem.getString(ATTR_VALUE);
    }

    /**
     * Gets an Integer From a Memento.
     *
     * @param key
     * @return an Integer with the Value
     */
    public Integer getInteger(String key) {
        final IMemento mem = getAttribute(key);
        return mem.getInteger(ATTR_VALUE);
    }

    /**
     * Returns a Float from a Memento.
     *
     * @param key
     * @return a Float with the Value
     */
    public Float getFloat(String key) {
        final IMemento mem = getAttribute(key);
        return mem.getFloat(ATTR_VALUE);
    }

    /**
     * Returns an List of Integers from a Memento.
     *
     * @param key
     * @return List of Integer-values
     */
    public List<Integer> getIntegerList(String key) {
        final List<Integer> valuelist = new ArrayList<>();
        final String valueString = getString(key);
        if (valueString != null) {
            final String[] objects = valueString.split(LIST_SEPARATOR);
            for (String object : objects) {
                if (StringUtils.isBlank(object) || "null".equals(object)) {
                    valuelist.add(0);
                } else {
                    valuelist.add(Integer.valueOf(object));
                }
            }
        }
        return valuelist;
    }

    /**
     * Returns an List of Strings from a Memento.
     *
     * @param key
     * @return a List of String values
     */
    public List<String> getStringList(String key) {
        List<String> valuelist = Collections.emptyList();
        final String valueString = getString(key);
        if (valueString != null) {
            valuelist = new ArrayList<>(Arrays.asList(valueString.split(LIST_SEPARATOR)));
        }

        return valuelist;
    }
}
