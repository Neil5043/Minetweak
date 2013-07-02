package org.minetweak.plugins;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class PluginLoader {

    private ArrayList<File> files = new ArrayList<File>();
    public static URLClassLoader loader;
    public static HashMap<String, MinetweakPlugin> plugins = new HashMap<String, MinetweakPlugin>();

    public static void initialize() {
        PluginLoader loader = new PluginLoader();
        loader.setupModules();
        loader.enable();
    }

    public void setupModules() {
        createDir();
        getPluginFiles();
        ArrayList<String> classes = new ArrayList<String>();
        ArrayList<URL> urls = new ArrayList<URL>();
        for (File f : files) {
            Manifest mf = getManifest(f);
            String pluginClass = mf.getMainAttributes().getValue("Plugin-class");
            if (pluginClass == null) {
                continue;
            }
            try {
                urls.add(f.toURI().toURL());
                classes.add(pluginClass);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        loader = new URLClassLoader(urls.toArray(new URL[urls.size()]), this.getClass().getClassLoader());
        for (String c : classes) {
            try {
                Class mc = Class.forName(c, true, loader);
                MinetweakPlugin plugin = (MinetweakPlugin) mc.newInstance();
                // Note that we override plugins even if they exist. This allows for alphabetical file-name plugin overriding
                plugins.put(plugin.getName(), plugin);
            } catch (Exception e) {
                throw new RuntimeException("Error loading plugin", e);
            }
        }
    }

    public void enable() {
        for (MinetweakPlugin plugin : plugins.values()) {
            plugin.onEnable();
        }
    }

    private Manifest getManifest(File f) {
        try {
            JarFile jf = new JarFile(f);
            return jf.getManifest();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void getPluginFiles() {
        try {
            FileVisitor<Path> visitor = new FileProcessor(this);
            Files.walkFileTree(Paths.get("plugins"), visitor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createDir() {
        File f = new File("plugins");
        if (!f.isDirectory()) {
            if (!f.mkdir()) {
                throw new RuntimeException("Unable to create plugins folder!");
            }
        }
    }

    private static final class FileProcessor extends SimpleFileVisitor<Path> {

        public PluginLoader instance;

        public FileProcessor(PluginLoader instance) {
            this.instance = instance;
        }

        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException {
            if (path.toString().endsWith(".jar")) {
                instance.files.add(path.toFile());
            }
            return FileVisitResult.CONTINUE;
        }
    }

    public static void disable(String pluginName) {
        if (plugins.containsKey(pluginName)) {
            plugins.get(pluginName).onDisable();
            plugins.remove(pluginName);
        }
    }

}