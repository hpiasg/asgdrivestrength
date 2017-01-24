package de.uni_potsdam.hpi.asg.drivestrength.netlist;

import java.util.ArrayList;
import java.util.List;

import de.uni_potsdam.hpi.asg.drivestrength.netlist.Signal.Direction;

public class Module {

    private String name;
    private List<String> interfaceSignals;
    private List<Signal> signals;
    private List<AssignConnection> assignConnections;
    private List<CellInstance> cellInstances;
    private List<ModuleInstance> moduleInstances; /* these are instances of *other* modules */
    
    public Module() {
        this.name = null;
        this.signals = new ArrayList<>();
        this.interfaceSignals = new ArrayList<>();
        this.assignConnections = new ArrayList<>();
        this.cellInstances = new ArrayList<>();
        this.moduleInstances = new ArrayList<>();
    }
    
    public Module(Module moduleToCopy) {
        this(moduleToCopy, false);
    }
    
    public Module(Module moduleToCopy, boolean keepCellAvatars) {
        this.name = moduleToCopy.getName();
        
        this.signals = new ArrayList<>();
        for (Signal s: moduleToCopy.getSignals()) this.signals.add(new Signal(s));

        this.interfaceSignals = new ArrayList<>();
        for (String s: moduleToCopy.getInterfaceSignals()) this.interfaceSignals.add(s);
        
        this.assignConnections = new ArrayList<>();
        for (AssignConnection s: moduleToCopy.getAssignConnections()) {
            Signal sourceSignal = this.getSignalByName(s.getSourceSignal().getName());
            Signal destinationSignal = this.getSignalByName(s.getDestinationSignal().getName());
            this.assignConnections.add(new AssignConnection(sourceSignal, destinationSignal,
                                       s.getSourceBitIndex(), s.getDestinationBitIndex()));
        }
        
        this.cellInstances = new ArrayList<>();
        for (CellInstance i : moduleToCopy.getCellInstances()) {
            CellInstance newCellInstance = new CellInstance(i.getName(), i.getDefinition(),
                    this.copyPinAssignments(i.getPinAssignments()));
            if (keepCellAvatars) {
                newCellInstance.setAvatar(i.getAvatarOrSelf());
            }
            this.cellInstances.add(newCellInstance);
        }
        
        this.moduleInstances = new ArrayList<>();
        for (ModuleInstance i : moduleToCopy.getModuleInstances()) {
            this.moduleInstances.add(new ModuleInstance(i.getName(), i.getDefinition(),
                                   this.copyPinAssignments(i.getPinAssignments())));
        }
    }
    
    private List<PinAssignment> copyPinAssignments(List<PinAssignment> pinAssignments) {
        List<PinAssignment> newPinAssignments = new ArrayList<>();
        for (PinAssignment p : pinAssignments) {
            Signal signal = this.getSignalByName(p.getSignal().getName());
            if (p.isPositional()) {
                newPinAssignments.add(new PinAssignment(signal, p.getSignalBitIndex(), p.getPinPosition())); 
            } else {
                newPinAssignments.add(new PinAssignment(signal, p.getSignalBitIndex(), p.getPinName()));                    
            }
        }
        return newPinAssignments;
    }
    
    public String toVerilog() {
        String verilog = "module " + this.name + " (";
        verilog += String.join(", ", this.interfaceSignals);
        verilog += ");\n";
        
        for (Signal signal : this.signals) {
            verilog += "  " + signal.toVerilog() + "\n";
        }
        
        if(this.assignConnections.size() > 0) {
            verilog += "\n";            
        }
        
        for (AssignConnection assignConnection: this.assignConnections) {
            verilog += "  " + assignConnection.toVerilog() + "\n";
        }

        if(this.getAllInstances().size() > 0) {
            verilog += "\n";            
        }
        
        for (AbstractInstance instance: this.getAllInstances()) {
            verilog += "  " + instance.toVerilog() + "\n";
        }
        
        verilog += "endmodule";
        return verilog; 
    }
    
    public String getName() {
        return name;
    }
    
    public List<AbstractInstance> getAllInstances() {
        List<AbstractInstance> allInstances = new ArrayList<>();
        allInstances.addAll(this.cellInstances);
        allInstances.addAll(this.moduleInstances);
        return allInstances;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void addInterfaceSignal(String interfaceSignal) {
        this.interfaceSignals.add(interfaceSignal);
    }
    
    public void addSignal(Signal signal) {
        this.signals.add(signal);
    }

    public void addInstance(ModuleInstance instance) {
        this.moduleInstances.add(instance);
    }
    
    public void addInstance(CellInstance instance) {
        this.cellInstances.add(instance);
    }
    
    public List<ModuleInstance> getModuleInstances() {
        return this.moduleInstances;
    }

    public List<CellInstance> getCellInstances() {
        return this.cellInstances;
    }
    
    public List<Signal> getSignals() {
        return this.signals;
    }
    
    public List<String> getInterfaceSignals() {
        return this.interfaceSignals;
    }
    
    public List<AssignConnection> getAssignConnections() {
        return this.assignConnections;
    }
    
    public List<Signal> getWires() {
        List<Signal> wires = new ArrayList<>();
        for (Signal s : this.signals) {
            if (s.getDirection() == Direction.wire) {
                wires.add(s);
            }
        }
        return wires;
    }
    
    public List<Signal> getIOSignals() {
        List<Signal> ioSignals = new ArrayList<>();
        for (Signal s : this.signals) {
            if (s.getDirection() == Direction.input || s.getDirection() == Direction.output) {
                ioSignals.add(s);
            }
        }
        return ioSignals;
    }
    
    public Signal getSignalByName(String signalName) {
        if (signalName.equals("0") || signalName.equals("1'b0")) {
            return Signal.getZeroInstance();
        }
        if (signalName.equals("1") || signalName.equals("1'b1")) {
            return Signal.getOneInstance();
        }
        for (Signal s : this.signals) {
            if (s.getName().equals(signalName)) {
                return s;
            }
        }
        throw new Error("Module does not have a Signal named " + signalName);
    }
    
    public Signal getSignalByInterfacePosition(int position) {
        String interfaceSignalName = this.interfaceSignals.get(position);
        return this.getSignalByName(interfaceSignalName);
    }
    
    public void addAssignConnection(AssignConnection assignConnection) {
        this.assignConnections.add(assignConnection);
    }
    
    public boolean hasAssignStatementsOnly() {
        return this.getAllInstances().isEmpty();
    }
    
    public void removeAllModuleInstances() {
    	this.moduleInstances = new ArrayList<ModuleInstance>();
    }
    
    public void removeAssignConnection(AssignConnection assignConnectionToRemove) {
        this.assignConnections.remove(assignConnectionToRemove);
    }
    
    public void removeSignal(Signal signalToRemove) {
        this.signals.remove(signalToRemove);
    }
    
    public void findLoads() {
        for (CellInstance c : this.cellInstances) {
            findLoad(c);
        }
    }
    
    public void findDrivers() {
        for (CellInstance c : this.cellInstances) {
            findDrivers(c);
        }
    }
    
    public void findLoad(CellInstance cellInstance) {
        Signal signal = cellInstance.getOutputSignal();
        double totalLoadCapacitance = 0.0;
        for (CellInstance c : this.cellInstances) {
            if (c.isInputSignal(signal)) {
                double capacitance = c.getAverageInputPinCapacitance();
                totalLoadCapacitance += capacitance;
                //System.out.println("   Pin " + c.pinNameForConnectedSignal(signal) + " of CellInstance " + c.getName() + " (C=" + capacitance + ")");
            }
        }
        //System.out.println("  total load capacitance: " + totalLoadCapacitance);
        double electricalEffort = totalLoadCapacitance / cellInstance.getAverageInputPinCapacitance();
        //double stageEffort = electricalEffort * cellInstance.getDefinition().getAvgLogicalEffort();
        if (electricalEffort > 1.5) {
            for (String pinName : cellInstance.getInputPinNames()) {
                double oldCapacitance = cellInstance.getInputPinCapacitance(pinName);
                cellInstance.setInputPinCapacitance(pinName, oldCapacitance * 1.5);
            }
        }
        System.out.println(cellInstance.getName() + " h=" + electricalEffort);
    }
    
    public void findDrivers(CellInstance cellInstance) {
        for (String pinName : cellInstance.getInputPinNames()) {
            Signal drivingSignal = cellInstance.getInputSignal(pinName);
            for (CellInstance c : this.cellInstances) {
                if (drivingSignal == c.getOutputSignal()) {
                    System.out.println(cellInstance.getName() + " pin " + pinName + " driven by " + c.getName());
                }
            }
        }
    }
}
