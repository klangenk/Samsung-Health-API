package de.langenk.shealthapi;

import org.json.JSONObject;


public class ResultWrapper implements JsonAble{
    private JsonAble elem;
    private String propertyName;

    public ResultWrapper(String propertyName, JsonAble elem){
        this.propertyName = propertyName;
        this.elem = elem;
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        try{
            obj.put(this.propertyName, elem.toJSON());
        } catch (Exception ex) {

        }
        return obj;
    }
}
