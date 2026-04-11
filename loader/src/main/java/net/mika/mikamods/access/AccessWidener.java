package net.mika.mikamods.access;

import java.util.HashMap;
import java.util.Map;

public class AccessWidener {
    public final Map<String, ClassAccess> classes = new HashMap<>();

    public static class ClassAccess {
        public boolean accessible;
        public boolean extendable;

        public Map<String, MemberAccess> fields = new HashMap<>();
        public Map<String, MemberAccess> methods = new HashMap<>();
    }

    public static class MemberAccess {
        public boolean accessible;
        public boolean mutable;
    }
}
