package de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.aggregators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_potsdam.hpi.asg.drivestrength.aggregatedcells.AggregatedCell;
import de.uni_potsdam.hpi.asg.drivestrength.cells.Cell;
import de.uni_potsdam.hpi.asg.drivestrength.cells.InpinPowerContainer;
import de.uni_potsdam.hpi.asg.drivestrength.cells.OutpinPowerContainer;
import de.uni_potsdam.hpi.asg.drivestrength.cells.PowerMatrix7x7;

public class CellPowerAggregator {

    private int inputSlewIndex;
    private AggregatedCell aggregatedCell;

    public CellPowerAggregator(AggregatedCell aggregatedCell, int inputSlewIndex) {
        this.aggregatedCell = aggregatedCell;
        this.inputSlewIndex = inputSlewIndex;
    }

    public void run() {
        aggregatedCell.setSizePowerValues(this.extractPowerValuesFor(aggregatedCell.getRawSizes(), aggregatedCell.getInputPinNames()));
    }

    private Map<String, Map<String, Double>> extractPowerValuesFor(List<Cell> rawSizes, List<String> pinNames) {
        Map<String, Map<String, Double>> powerPerPinPerSize = new HashMap<>();

        for (String pinName : pinNames) {
            powerPerPinPerSize.put(pinName, new HashMap<String, Double>());
            for (Cell rawSize : rawSizes) {
                String sizeName = rawSize.getName();
                double outpinPowerValue = this.extractOutpinPowerValueFor(pinName, rawSize);
                double inpinPowervalue = this.extractInpinPowerValue(pinName, rawSize);
                powerPerPinPerSize.get(pinName).put(sizeName, outpinPowerValue + inpinPowervalue);
            }
        }

        return powerPerPinPerSize;
    }

    private double extractOutpinPowerValueFor(String pinName, Cell rawSize) {
        List<Double> powerValues = new ArrayList<>();
        for (OutpinPowerContainer p : rawSize.getOutputPin().getOutpinPowerContainers()) {
            if (!p.getRelatedPinName().equals(pinName)) continue;
            if (p.getRisePower() == null || p.getFallPower() == null) continue;
            double avgRisePower = this.extractOutpinPower(p.getRisePower());
            double avgFallPower = this.extractOutpinPower(p.getFallPower());
            powerValues.add((avgRisePower + avgFallPower)/2);
        }
        return avgDobuleList(powerValues);
    }

    private double extractOutpinPower(PowerMatrix7x7 powerMatrix) {
        double sum = 0.0;
        double size = 7;
        for (int i = 0; i < size; i++) {
            sum += powerMatrix.getPowerAt(this.inputSlewIndex, i);
        }
        return sum /= size;
    }

    private double extractInpinPowerValue(String pinName, Cell rawSize) {
        List<Double> powerValues = new ArrayList<>();
        List<InpinPowerContainer> inpinPowerContainers = rawSize.getPinByName(pinName).getInpinPowerContainers();
        if (inpinPowerContainers.size() == 0) {
            return 0;
        }
        for (InpinPowerContainer p : inpinPowerContainers) {
            if (p.getRisePower() == null || p.getFallPower() == null) continue;
            double risePower = p.getRisePower().getPowerAt(this.inputSlewIndex);
            double fallPower = p.getFallPower().getPowerAt(this.inputSlewIndex);
            powerValues.add((risePower + fallPower) / 2);
        }
        return avgDobuleList(powerValues);
    }

    private double avgDobuleList(List<Double> aListOfDouble) {
        double sum = 0;
        for (double d : aListOfDouble) {
            sum += d;
        }
        return sum / aListOfDouble.size();
    }
}
