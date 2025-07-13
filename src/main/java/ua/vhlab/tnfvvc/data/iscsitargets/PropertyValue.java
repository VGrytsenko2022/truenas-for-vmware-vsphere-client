/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ua.vhlab.tnfvvc.data.iscsitargets;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = PropertyValueDeserializer.class)
public class PropertyValue {

    public Object parsed;
    public String rawvalue;
    public String value;
    public String source;
    public Object source_info;

    @Override
    public String toString() {
        return String.valueOf(parsed);
    }
}
