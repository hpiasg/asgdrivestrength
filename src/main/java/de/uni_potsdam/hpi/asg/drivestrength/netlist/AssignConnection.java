package de.uni_potsdam.hpi.asg.drivestrength.netlist;

public class AssignConnection {
    
    private Signal sourceSignal;
    private Signal destinationSignal;
    private int sourceBitIndex;
    private int destinationBitIndex;
    
    public Signal getSourceSignal() {
        return sourceSignal;
    }

    public Signal getDestinationSignal() {
        return destinationSignal;
    }

    public int getSourceBitIndex() {
        return sourceBitIndex;
    }

    public int getDestinationBitIndex() {
        return destinationBitIndex;
    }

    public AssignConnection(Signal sourceSignal, Signal destinationSignal, 
                            int sourceBitIndex, int destinationBitIndex) {
        this.sourceSignal = sourceSignal;
        this.sourceBitIndex = sourceBitIndex;
        this.destinationSignal = destinationSignal;
        this.destinationBitIndex = destinationBitIndex;
    }
    
    public String toVerilog() {
        String verilog = "assign ";
        verilog += destinationSignal.getName();
        if (destinationSignal.getWidth() > 1) {
            verilog += "[" + destinationBitIndex + "]";
        }
        verilog += " = ";
        verilog += sourceSignal.getName();
        if (sourceSignal.getWidth() > 1) {
            verilog += "[" + sourceBitIndex + "]";
        }
        verilog += ";";
        return verilog;
    }
    
}
