package mil.nga.util;

import java.util.Set;

import mil.nga.rod.accelerator.RedisCacheManager;

public class ClearCache {

    public static void main(String[] args) {
        int keysRemoved = 0;
        try (RedisCacheManager manager = RedisCacheManager.getInstance()) { 
            Set<String> keySet = manager.getKeys();
            if (keySet.size() > 0) {
                for (String key : keySet) {
                    manager.remove(key);
                    keysRemoved++;
                }
            }
            else {
                System.out.println("The cache is empty.");
            }
        }
        System.out.println("Removed [ "
                + keysRemoved
                + " ] keys.");
    }
}
