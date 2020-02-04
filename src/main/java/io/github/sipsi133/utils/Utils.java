/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Qveshn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.sipsi133.utils;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static boolean isValidLocation(Location location) {
        double x, y, z;
        return (x = location.getX()) >= -30000000 && x < 30000000
                && (z = location.getZ()) >= -30000000 && z < 30000000
                && NmsHelper.isValidY(location.getY());
    }

    private static Location toBlockLocation(Location location) {
        return new Location(
                location.getWorld(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(), 0, 0);
    }

    public static Location toValidBlockLocation(Location location) {
        return isValidLocation(location) ? toBlockLocation(location) : null;
    }

    public static <T> T nullIf(T value, T compareTo) {
        return value != null && value.equals(compareTo) ? null : value;
    }

    private static String readYamlHeader(Reader reader) throws IOException {
        try (BufferedReader input = reader instanceof BufferedReader
                ? (BufferedReader) reader : new BufferedReader(reader)
        ) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = input.readLine()) != null) {
                String tmpLine = line.trim();
                if (!tmpLine.isEmpty() && !tmpLine.startsWith("#")) {
                    break;
                }
                sb.append(line).append('\n');
            }
            return sb.toString();
        }
    }

    public static void mkdirs(File directory) throws IOException {
        if (!directory.exists() && !directory.mkdirs())
            throw new IOException("Can not create directory " + directory);
    }

    public static void saveConfigWithHeader(JavaPlugin plugin) {
        final String configName = "config.yml";
        File config = new File(plugin.getDataFolder(), configName);
        try {
            StringBuilder sb = new StringBuilder();
            InputStream in = plugin.getResource(configName);
            if (in != null) {
                InputStreamReader input = new InputStreamReader(in, StandardCharsets.UTF_8);
                sb.append(Utils.readYamlHeader(input));
                input.close();
            }
            boolean copyHeader = plugin.getConfig().options().copyHeader();
            plugin.getConfig().options().copyHeader(false);
            sb.append(plugin.getConfig().saveToString());
            plugin.getConfig().options().copyHeader(copyHeader);
            mkdirs(config.getParentFile());
            OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(config), StandardCharsets.UTF_8);
            writer.write(sb.toString());
            writer.close();
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + config.getName(), e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static String leftPad(String text, String regex, char padCharacter, int width) {
        StringBuilder sb = new StringBuilder();
        Matcher m = Pattern.compile(regex).matcher(text);
        String chars = String.format("%" + width + "s", "").replace(' ', padCharacter);
        int last = 0;
        while (m.find()) {
            int start = m.start();
            int n = m.end() - start;
            if (n < width) {
                sb.append(text, last, start);
                sb.append(chars, n, width);
                last = start;
            }
        }
        if (last == 0) return text;
        if (last < text.length()) sb.append(text, last, text.length());
        return sb.toString();
    }
}
