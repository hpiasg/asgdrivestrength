package de.uni_potsdam.hpi.asg.drivestrength.verilogparserOld.model;

/*
 * Copyright (C) 2016 Norman Kluge
 * 
 * This file is part of ASGdelaymatch.
 * 
 * ASGdelaymatch is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ASGdelaymatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ASGdelaymatch.  If not, see <http://www.gnu.org/licenses/>.
 */

public class VerilogModuleInstanceConnectionTemp {

    private VerilogSignal localSig;
    private String        moduleSigName;
    private Integer       moduleSigPos;

    public VerilogModuleInstanceConnectionTemp(VerilogSignal localSig, Integer moduleSigPos) {
        this.localSig = localSig;
        this.moduleSigName = null;
        this.moduleSigPos = moduleSigPos;
    }

    public VerilogModuleInstanceConnectionTemp(VerilogSignal localSig, String moduleSigName) {
        this.localSig = localSig;
        this.moduleSigName = moduleSigName;
        this.moduleSigPos = null;
    }

    public VerilogSignal getLocalSig() {
        return localSig;
    }

    public String getModuleSigName() {
        return moduleSigName;
    }

    public Integer getModuleSigPos() {
        return moduleSigPos;
    }
}
