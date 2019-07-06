package org.maxgamer.quickshop.Util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.maxgamer.quickshop.QuickShop;

public class Updater {
    /**
     * Check new update
     *
     * @return True=Have a new update; False=No new update or check update failed.
     */
    public static UpdateInfomation checkUpdate() {
        if (!QuickShop.instance.getConfig().getBoolean("updater")) {
            return new UpdateInfomation(null, false);
        }
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=62575")
                    .openConnection();
            int timed_out = 300000;
            connection.setConnectTimeout(timed_out);
            connection.setReadTimeout(timed_out);
            String localPluginVersion = QuickShop.instance.getDescription().getVersion();
            String spigotPluginVersion = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
            if (spigotPluginVersion != null && !spigotPluginVersion.equals(localPluginVersion)) {
                connection.disconnect();
                return new UpdateInfomation(spigotPluginVersion, spigotPluginVersion.toLowerCase().contains("beta"));
            }
            connection.disconnect();
            return new UpdateInfomation(spigotPluginVersion, false);
        } catch (IOException e) {
            Bukkit.getConsoleSender()
                    .sendMessage(ChatColor.RED + "[QuickShop] Failed to check for an update on SpigotMC.org! It might be an internet issue or the SpigotMC host is down. If you want disable the update checker, you can disable in config.yml, but we still high-recommend check for updates on SpigotMC.org often.");
            return new UpdateInfomation(null, false);
        }
    }

    public void replaceTheJar(byte[] data) throws RuntimeException, IOException {
        File pluginFolder = new File("plugins");
        if (!pluginFolder.exists())
            throw new RuntimeException("Can't find the plugins folder.");
        if (!pluginFolder.isDirectory())
            throw new RuntimeException("Plugins not a folder.");
        File[] plugins = pluginFolder.listFiles();
        if (plugins == null)
            throw new IOException("Can't get the files in plugins folder");
        File quickshop = null;
        for (File plugin : plugins) {
            try {
                PluginDescriptionFile desc = QuickShop.instance.getPluginLoader().getPluginDescription(plugin);
                if (!desc.getName().equals(QuickShop.instance.getDescription().getName()))
                    continue;
                quickshop = plugin;
                break;
            } catch (InvalidDescriptionException e) { //Ignore }
            }

        }
        if (quickshop == null)
            throw new RuntimeException("Failed to get QuickShop Jar File.");
        OutputStream outputStream = new FileOutputStream(quickshop, false);
        outputStream.write(data);
        outputStream.flush();
        outputStream.close();
    }

    public byte[] downloadUpdatedJar() throws IOException {
        final String uurl = "https://api.spiget.org/v2/resources/62575/versions/latest/download";
        URL url = new URL(uurl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("User-Agent", "QuickShop-Reremake " + QuickShop.getVersion());// Set User-Agent
        InputStream is = connection.getInputStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int len;
        while ((len = is.read(buff)) != -1) {
            os.write(buff, 0, len);
        }
        return os.toByteArray();
    }
}
