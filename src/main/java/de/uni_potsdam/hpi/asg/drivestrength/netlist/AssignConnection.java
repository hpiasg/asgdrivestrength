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
    
    public AssignConnection(AssignConnection assignConnectionToCopy) {
        this.sourceSignal = assignConnectionToCopy.getSourceSignal();
        this.sourceBitIndex = assignConnectionToCopy.getSourceBitIndex();
        this.destinationSignal = assignConnectionToCopy.getDestinationSignal();
        this.destinationBitIndex = assignConnectionToCopy.getDestinationBitIndex();
    }
    
    public void setSourceSignal(Signal newSourceSignal) {
    	this.sourceSignal = newSourceSignal;
    }
    
    public void setDestinationSignal(Signal newDestinationSignal) {
    	this.destinationSignal = newDestinationSignal;
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
