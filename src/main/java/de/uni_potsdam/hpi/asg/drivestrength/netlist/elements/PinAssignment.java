package de.uni_potsdam.hpi.asg.drivestrength.netlist.elements;

public class PinAssignment {
    private Signal signal;
    private int signalBitIndex; // -1 for "has no selected bit"
    private int pinPosition;
    private String pinName;

    public PinAssignment(Signal signal, int signalBitIndex, String pinName) {
        this.signal = signal;
        this.signalBitIndex = signalBitIndex;
        this.pinName = pinName;        
    }

    public PinAssignment(Signal signal, int signalBitIndex, int pinPosition) {
        this.signal = signal;
        this.signalBitIndex = signalBitIndex;
        this.pinPosition = pinPosition;        
    }
    
    public int getPinPosition() {
        return pinPosition;
    }
    public String getPinName() {
        return pinName;
    }
    public Signal getSignal() {
        return signal;
    }
    public int getSignalBitIndex() {
        return signalBitIndex;
    }
    public boolean isPositional() {
        return pinName == null;
    }
    
    public void setSignal(Signal newSignal) {
        this.signal = newSignal;
    }
    
    public void setPinName(String pinName) {
        this.pinName = pinName;
    }
    
    public void setSignalBitIndex(int newSignalBitIndex) {
        this.signalBitIndex = newSignalBitIndex;
    }
    
    public boolean hasSelectedBit() {
        return signalBitIndex != -1;
    }
    
    public String toVerilog() {
        String verilog = "";
        if (this.pinName == null) {
            //positional
            verilog += signal.getName();
            if (this.hasSelectedBit()) {
                verilog += "[" + signalBitIndex + "]";
            }
        } else {
            //mapped
            verilog += "." + this.pinName + "(" + signal.getName();
            if (this.hasSelectedBit()) {
                verilog += "[" + signalBitIndex + "]";
            }
            verilog += ")";
        }
        return verilog;
    }
}
