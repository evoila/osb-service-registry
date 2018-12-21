package de.evoila.osb.service.registry.util;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdService {

    private IdService() {}

    public static boolean verifyId(String id) {
        if (id == null || id.isEmpty())
            return false;

        Pattern p = Pattern.compile("[a-z0-9-]+", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(id);
        return m.matches();
    }

    public static String getNextId() {
        return UUID.randomUUID().toString();
    }
}
