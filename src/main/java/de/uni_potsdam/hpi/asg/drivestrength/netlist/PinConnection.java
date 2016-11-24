package de.uni_potsdam.hpi.asg.drivestrength.netlist;

public class PinConnection {
    private Signal signal;
    private int signalBitIndex;
    private int pinPosition;
    private String pinName;

    public PinConnection(Signal signal, int signalBitIndex, String pinName) {
        this.signal = signal;
        this.signalBitIndex = signalBitIndex;
        this.pinName = pinName;        
    }

    public PinConnection(Signal signal, int signalBitIndex, int pinPosition) {
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
}
