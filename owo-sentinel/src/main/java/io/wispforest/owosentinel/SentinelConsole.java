package io.wispforest.owosentinel;

import java.util.Locale;
import java.util.Scanner;

public class SentinelConsole {
    public static void run() throws Exception {
        System.out.println("oωo-lib is required to run the following mods:");

        for (String dependent : OwoSentinel.listOwoDependents()) {
            System.out.println("- " + dependent);
        }

        System.out.println("\n" + OwoSentinel.OWO_EXPLANATION);
        System.out.print("Download and install (Y/n): ");

        Scanner in = new Scanner(System.in);
        boolean install = false;

        try {
            String answer = in.next();

            install = answer.isBlank() || answer.toLowerCase(Locale.ROOT).startsWith("y");
        } catch (Exception e) {
            System.out.println("<stdin blocked>");
        }

        if (install) {
            OwoSentinel.downloadAndInstall(System.out::println);
        } else {
            System.out.println("You can install oωo-lib at https://modrinth.com/mod/owo-lib.");
        }
    }
}
