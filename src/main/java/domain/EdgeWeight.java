package domain;

import util.Utility;

public class EdgeWeight {
    private Object edge; //arista
    public Object weight; //peso
    public Object origen;
    public Object destino;


    public EdgeWeight(Object origen, Object destino, Object weight) {
        this.origen = origen;
        this.destino = destino;
        this.weight = weight;
    }

    public EdgeWeight(Object edge, Object weight) {
        this.edge = edge;
        this.weight = weight;
    }
    public Object getFrom() {
        return origen;
    }

    public Object getTo() {
        return destino;
    }

    public Object getEdge() {
        return edge;
    }

    public void setEdge(Object edge) {
        this.edge = edge;
    }

    public Object getWeight() {
        return weight;
    }

    public void setWeight(Object weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        if(weight==null) return "Edge="+edge;
        else return "Edge="+edge+". Weight="+weight;
    }
}
