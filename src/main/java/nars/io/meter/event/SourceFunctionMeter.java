/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.nars.io.meter.event;

import main.java.nars.io.meter.FunctionMeter;

/**
 * Function meter with one specific ID
 */
abstract class SourceFunctionMeter<T> extends FunctionMeter<T> {
    
    private final String name;

    public SourceFunctionMeter(String id) {
        super(id);
        this.name = id;
    }

    public String id() { return name; }
    

    
}
