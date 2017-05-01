package mil.nga.rod.model;

import java.util.Date;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test class ensuring that the target POJO reads/writes correctly.
 * 
 * @author L. Craig Carpenter
 *
 */
public class TestQueryRequestAccelerator { 
    
    /**
     * Test construction of the target object.
     */
    @Test
    public void runTest() {
        
        String bogusHash   = "0123456789ABCDEF";
        String bogusPath   = "/path/to/file.extension";
        long   bogusSize   = 12345L;
        Date   currentDate = new Date(System.currentTimeMillis());
        
        QueryRequestAccelerator record = new QueryRequestAccelerator
                .QueryRequestAcceleratorBuilder()
                .fileDate(currentDate)
                .size(bogusSize)
                .path(bogusPath)
                .hash(bogusHash)
                .build();
        
        assertEquals(record.getHash(),     bogusHash);
        assertEquals(record.getPath(),     bogusPath);
        assertEquals(record.getSize(),     bogusSize);
        assertEquals(record.getFileDate(), currentDate);
        
    }
    
}
