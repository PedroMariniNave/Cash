package com.zpedroo.voltzcash.utils;

import com.zpedroo.voltzcash.VoltzCash;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class FileUtils {

    private static FileUtils instance;
    public static FileUtils get() { return instance; }

    private static String CHARSET_NAME = "UTF-8";

    private VoltzCash voltzCash;
    private Map<Files, FileManager> files;

    public FileUtils(VoltzCash voltzCash) {
        instance = this;
        this.voltzCash = voltzCash;
        this.files = new HashMap<>(Files.values().length);

        for (Files files : Files.values()) {
            getFiles().put(files, new FileManager(files));
        }
    }

    public String getString(Files file, String path) {
        return getString(file, path, "NULL");
    }

    public String getString(Files file, String path, String defaultValue) {
        return getFile(file).get().getString(path, defaultValue);
    }

    public List<String> getStringList(Files file, String path) {
        return getFiles().get(file).get().getStringList(path);
    }

    public Boolean getBoolean(Files file, String path) {
        return getFile(file).get().getBoolean(path);
    }

    public Integer getInt(Files file, String path) {
        return getInt(file, path, 0);
    }

    public Integer getInt(Files file, String path, int defaultValue) {
        return getFile(file).get().getInt(path, defaultValue);
    }

    public Long getLong(Files file, String path) {
        return getLong(file, path, 0);
    }

    public Long getLong(Files file, String path, long defaultValue) {
        return getFile(file).get().getLong(path, defaultValue);
    }

    public Double getDouble(Files file, String path) {
        return getDouble(file, path, 0);
    }

    public Double getDouble(Files file, String path, double defaultValue) {
        return getFile(file).get().getDouble(path, defaultValue);
    }

    public Float getFloat(Files file, String path) {
        return getFloat(file, path, 0);
    }

    public Float getFloat(Files file, String path, float defaultValue) {
        return (float) getFile(file).get().getDouble(path, defaultValue);
    }

    public Set<String> getSection(Files file, String path) {
        return getFile(file).get().getConfigurationSection(path).getKeys(false);
    }

    public FileManager getFile(Files file) {
        return getFiles().get(file);
    }

    public Map<Files, FileManager> getFiles() {
        return files;
    }

    public void addLog(String log) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(getFile(FileUtils.Files.LOGS).getFile().getCanonicalPath(), true));
            writer.newLine();
            writer.write(log);
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void copy(InputStream is, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;

            while ((len=is.read(buf)) > 0) {
                out.write(buf,0,len);
            }

            out.close();
            is.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public enum Files {
        LOGS("logs.log", "configuration-files", "", false),
        CONFIG("config.yml", "configuration-files", "", false),
        CONFIRM("confirm.yml", "menus", "menus", false),
        INFO("info.yml", "menus", "menus", false),
        MAIN("main.yml", "menus", "menus", false),
        SHOP("shop.yml", "menus", "menus", false),
        TOP("top.yml", "menus", "menus", false),
        TRANSACTIONS("transactions.yml", "menus", "menus", false),
        PURCHASES("purchases.yml", "menus", "menus", false),
        EXAMPLE("example.yml", "categories", "categories", true);

        public String name;
        public String resource;
        public String folder;
        public Boolean requireEmpty;

        Files(String name, String resource, String folder, Boolean requireEmpty) {
            this.name = name;
            this.resource = resource;
            this.folder = folder;
            this.requireEmpty = requireEmpty;
        }

        public String getName() {
            return name;
        }

        public String getResource() {
            return resource;
        }

        public String getFolder() {
            return folder;
        }
    }

    public class FileManager {

        private File pdfile;
        private FileConfiguration language;

        public FileManager(Files file) {
            this.pdfile = new File(voltzCash.getDataFolder() + (file.getFolder().isEmpty() ? "" : "/" + file.getFolder()), file.getName());

            if (!pdfile.exists()) {
                if (file.requireEmpty) {
                    File folder = new File(voltzCash.getDataFolder(), "/categories");
                    if (folder.listFiles() != null) {
                        if (Stream.of(folder.listFiles()).map(YamlConfiguration::loadConfiguration).count() > 0) return;
                    }
                }

                try {
                    pdfile.getParentFile().mkdirs();
                    pdfile.createNewFile();

                    copy(voltzCash.getResource((file.getResource().isEmpty() ? "" : file.getResource() + "/") + file.getName()), pdfile);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            if (!StringUtils.contains(file.getName(), ".yml")) return;

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(pdfile), CHARSET_NAME));
                language = YamlConfiguration.loadConfiguration(reader);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public FileConfiguration get() {
            return language;
        }

        public File getFile() {
            return pdfile;
        }

        public void save() {
            try {
                language.save(pdfile);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public void reload() {
            try {
                language = YamlConfiguration.loadConfiguration(pdfile);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}