package mil.nga.util;

import java.util.Set;

import mil.nga.rod.accelerator.RedisCacheManager;

public class DumpKeys {

    public static void main(String[] args) {
        try (RedisCacheManager manager = RedisCacheManager.getInstance()) { 
            Set<String> keySet = manager.getKeys();
            if (keySet.size() > 0) {
                for (String key : keySet) {
                    System.out.println(key);
                }
            }
            else {
                System.out.println("The cache is empty.");
            }
        }
    }
}
