package de.uni_potsdam.hpi.asg.drivestrength.netlist;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


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

public class Signal {

    private static final Logger logger = LogManager.getLogger();

    public enum Direction {
        input, output, wire
    }

    protected String    name;
    protected Direction direction;
    protected int       width;
    
    public Signal(String name, Direction direction, int width) {
        this.direction = direction;
        this.name = name;
        this.width = width;
    }
    
    public String getName() {
        return name;
    }

    public int getWidth() {
        return width;
    }
    
    public boolean isBundle() {
        return width > 1;
    }

    @Override
    public String toString() {
        return name + ":" + direction + ",width:" + width;
    }
    
    public String toVerilog() {
        String directionString = "";
        switch (this.direction) {
            case input:
                directionString = "input";
                break;
            case output:
                directionString = "output";
                break;
            case wire:
                directionString = "wire";
                break;
        }
        String bundleString = "";
        if (width > 1) {
            bundleString = " [" + Integer.toString(width - 1) + ":0]";
        }
        return directionString + bundleString + " " + this.name + ";";
    }

    public Direction getDirection() {
        return direction;
    }
}
