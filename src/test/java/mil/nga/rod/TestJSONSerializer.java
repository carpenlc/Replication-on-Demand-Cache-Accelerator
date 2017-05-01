package mil.nga.rod;



import mil.nga.rod.model.QueryRequestAccelerator;

import static org.junit.Assert.*;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class TestJSONSerializer {

    private static final DateFormat dateFormatter = 
            new SimpleDateFormat("yyyy-MM-dd hh:mm:ss Z");
    
    @Test
    public void testStringListSerialization() {
        
        List<String> list = new ArrayList<String>();
        list.add("Element 1");
        list.add("Element 2");
        list.add("Element 3");
        list.add("Element 4");
        list.add("Element 5");
        list.add("Element 6");
        list.add("Element 7");
        
        String serialized = JSONSerializer.getInstance().serialize(list);
        List<String> list2 = JSONSerializer
                    .getInstance()
                    .deserializeToStringList(serialized);
        
        assertTrue(list.equals(list2));
        
    }
    
    @Test
    public void testQueryRequestAcceleratorSerialization() {
        
        QueryRequestAccelerator record = new QueryRequestAccelerator
                .QueryRequestAcceleratorBuilder()
                .fileDate(new java.util.Date(System.currentTimeMillis()))
                .path("/path/to/file")
                .hash("12345-MD5-12345")
                .size(1500L)
                .build();
        
        String serialized = JSONSerializer.getInstance().serialize(record);
        QueryRequestAccelerator record2 = JSONSerializer
                    .getInstance()
                    .deserializeToQueryRequestAccelerator(serialized);
        
        // System.out.println(serialized);
        // System.out.println(dateFormatter.format(record.getFileDate()));
        // System.out.println(dateFormatter.format(record2.getFileDate()));
        
        // Compare the strings.
        assertEquals(dateFormatter.format(record.getFileDate()), 
                dateFormatter.format(record2.getFileDate()));
        assertEquals(record.getHash(), record2.getHash());
        assertEquals(record.getPath(), record2.getPath());
        assertEquals(record.getSize(), record2.getSize());
        
    }
    
 
    
}
