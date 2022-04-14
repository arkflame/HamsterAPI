package dev._2lstudios.hamsterapi;

import org.bukkit.Bukkit;

public class Version {
    private int first = 0;
    private int second = 0;
    private int third = 0;

    private static Version currentVersion;

    public Version(String versionText) {
        String[] splittedVersion = versionText.split("[.]");

        for (int i = 0; i < splittedVersion.length; i++) {
            String part = splittedVersion[i];
            int value = Integer.parseInt(part);

            if (i == 0) {
                first = value;
            }

            if (i == 1) {
                second = value;
            }

            if (i == 2) {
                third = value;
            }
        }
    }

    public static Version getCurrentVersion() {
        if (currentVersion == null) {
            currentVersion = new Version(
                HamsterAPI.getVersion(Bukkit.getServer())
                    .split("v")[1]
                    .split("_R")[0]
                    .replace("_", ".")
            );
        }
        return currentVersion;
    }

    public boolean isMajor(Version version) {
        return 
            (first > version.first)  
            || 
            (first == version.first && second > version.second) 
            ||  
            (first == version.first && second == version.second && third > version.third)
        ;
    }

    public boolean isMajor(String versionText) {
        return this.isMajor(new Version(versionText));
    }

    public boolean isMinor(Version version) {
        return version.isMajor(this);
    }

    public boolean isMinor(String versionText) {
        return this.isMinor(new Version(versionText));
    }

    public String toString() {
        String ver = first + "." + second;
        if (third != 0) {
            ver += "." + third;
        }
        return ver;
    }
}
