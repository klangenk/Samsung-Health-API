package de.langenk.shealthapi;


import org.json.JSONArray;

import java.util.ArrayList;

public class List<E extends JsonAble> extends ArrayList<E> implements JsonAble {

    public List(){
        super();
    }


    public JSONArray toJSON() {
        JSONArray elements = new JSONArray();
        try{
            for(JsonAble element : this){
                elements.put(element.toJSON());
            }
        } catch (Exception ex) {

        }
        return elements;
    }
}
