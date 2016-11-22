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

public class VerilogSignalGroupSignal extends VerilogSignal {

    private VerilogSignalGroup group;
    private int                count_id;

    public VerilogSignalGroupSignal(String name, Direction dir, VerilogSignalGroup group, int count_id) {
        super(name, dir);
        this.group = group;
        this.count_id = count_id;
    }

    public VerilogSignalGroup getGroup() {
        return group;
    }

    @Override
    public String toString() {
        return name + ":" + dir + ",width:" + width + ",group:" + group.getGroupName() + ",id:" + count_id + "/" + (group.getCount() - 1);
    }
}
