package de.uni_potsdam.hpi.asg.drivestrength.cells.libertyparser;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.Test;

import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;
import de.uni_potsdam.hpi.asg.drivestrength.testhelper.TestHelper;

public class LibertyParserTest {
    
    protected static TestHelper testHelper = new TestHelper();

    @Test
    public void testLibertyParser() {
        File libertyFile = testHelper.getResourceAsFile("/minimalLibraryInvRandomDelays.lib");
        
        List<Cell> cells = new LibertyParser(libertyFile).run();
        
        assertEquals("INV_1", cells.get(0).getName());
    }
    
    @Test
    public void testPinTimingParser() {
        File libertyFile = testHelper.getResourceAsFile("/minimalLibraryInvRandomDelays.lib");
        
        List<Cell> cells = new LibertyParser(libertyFile).run();
        
        assertEquals(0.6, cells.get(0).getPins().get(0).getTimings().get(0).getRiseDelays().getDelayAt(0, 1), 0.001);
    }

}
