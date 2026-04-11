package net.mika.mikamods.access;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class AccessWidenerReader {

    public static AccessWidener read(InputStream is) throws IOException {
        AccessWidener aw = new AccessWidener();

        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String line;

        while ((line = reader.readLine()) != null) {
            line = line.trim();

            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] parts = line.split(" ");

            String action = parts[0];
            String type = parts[1];
            String owner = parts[2]; // must be slash format

            AccessWidener.ClassAccess cls =
                    aw.classes.computeIfAbsent(owner, k -> new AccessWidener.ClassAccess());

            switch (type) {
                case "class":
                    if (action.equals("accessible")) cls.accessible = true;
                    if (action.equals("extendable")) cls.extendable = true;
                    break;

                case "field": {
                    String name = parts[3];

                    AccessWidener.MemberAccess acc =
                            cls.fields.computeIfAbsent(name, k -> new AccessWidener.MemberAccess());

                    if (action.equals("accessible")) acc.accessible = true;
                    if (action.equals("mutable")) acc.mutable = true;
                    break;
                }

                case "method": {
                    String name = parts[3];
                    String desc = parts[4];

                    String key = name + desc;

                    AccessWidener.MemberAccess acc =
                            cls.methods.computeIfAbsent(key, k -> new AccessWidener.MemberAccess());

                    if (action.equals("accessible")) acc.accessible = true;
                    break;
                }
            }
        }

        return aw;
    }
}