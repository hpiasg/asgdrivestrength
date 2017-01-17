package de.uni_potsdam.hpi.asg.drivestrength.netlist.verilogparser;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.AggregatedCellLibrary;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Module;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.Netlist;
import de.uni_potsdam.hpi.asg.drivestrength.netlist.flattener.NetlistFlattener;
import de.uni_potsdam.hpi.asg.drivestrength.testhelper.TestHelper;

public class VerilogParserTest {
    protected static TestHelper testHelper = new TestHelper();
    
    @Test
    public void testVerilogParser() {
        File verilogFile = testHelper.getResourceAsFile("/minimalNetlist.v");        
        Netlist aNetlist = new VerilogParser(verilogFile).createNetlist();
        
        assertEquals(aNetlist.getRootModule().getName(), "aModule");
    }
    
    @Test
    public void testNetlistFlattener() {
        File verilogFile = testHelper.getResourceAsFile("/netlistWithSubmodules.v");
        Netlist aNetlist = new VerilogParser(verilogFile).createNetlist();

        assertEquals(aNetlist.getModules().size(), 2);
        
        new NetlistFlattener(aNetlist).run();
        
        assertEquals(aNetlist.getModules().size(), 3);
    }

    @Test
    public void testSignalMappingPositional() {
        File verilogFile = testHelper.getResourceAsFile("/netlistWithSubmodules.v");
        Netlist aNetlist = new VerilogParser(verilogFile).createNetlist();
        Module submodule = aNetlist.getRootModule().getModuleInstances().get(0).getDefinition();
        assertEquals(submodule.getSignalByInterfacePosition(0).getName(), "in"); 
    }
    
    @Test
    public void testSignalMappingNamed() {
        File verilogFile = testHelper.getResourceAsFile("/netlistWithSubmodules.v");
        Netlist aNetlist = new VerilogParser(verilogFile).createNetlist();
        Module submodule = aNetlist.getRootModule().getModuleInstances().get(0).getDefinition();
        assertEquals(submodule.getSignalByName("in").getName(), "in"); 
    }

}
