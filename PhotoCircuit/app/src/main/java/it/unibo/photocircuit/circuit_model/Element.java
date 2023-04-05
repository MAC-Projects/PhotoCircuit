package it.unibo.photocircuit.circuit_model;

import java.util.Objects;

import it.unibo.photocircuit.ml_handling.MLController;

public class Element extends Segment {
    private int type;

    public Element(Node node1, Node node2, int type) {
        super(node1, node2);
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTypeAsString() {
        return MLController.getClassNameFromIndex(type);
    }

    private boolean isForwards() {
        if (isVertical()) {
            return getNode1().getY() < getNode2().getY();
        } else {
            return getNode1().getX() < getNode2().getX();
        }
    }

    private String invertIfNeeded(String inStr, String rot, boolean isConsistent) {
        boolean isActuallyForwards = (isConsistent && isForwards()) || (!isConsistent && !isForwards());
        boolean needsInvert =
                (isActuallyForwards && (rot.charAt(1) == '2' || rot.charAt(1) == '3')) ||
                (!isActuallyForwards && (rot.charAt(1) == '0' || rot.charAt(1) == '1'));
        if (needsInvert)
            return inStr + ",invert";
        else
            return inStr;
    }

    @Override
    public String getCircuiTikZLabel() {
        String type = getTypeAsString();
        if (type.startsWith("ac_src")) {
            return "sV";
        } else if (type.startsWith("battery")) {
            String rot = type.split("_")[1];
            return invertIfNeeded("battery", rot, false);
        } else if (type.startsWith("cap")) {
            return "C";
        } else if (type.startsWith("curr_src")) {
            String rot = type.split("_")[2];
            return invertIfNeeded("I", rot, true);
        } else if (type.startsWith("dc_volt_src_1")) {
            String rot = type.split("_")[4];
            return invertIfNeeded("V", rot, false);
        } else if (type.startsWith("dc_volt_src_2")) {
            String rot = type.split("_")[4];
            return invertIfNeeded("battery2", rot, false);
        } else if (type.startsWith("dep_curr_src")) {
            String rot = type.split("_")[3];
            return invertIfNeeded("cI", rot, true);
        } else if (type.startsWith("dep_volt")) {
            String rot = type.split("_")[2];
            return invertIfNeeded("cV", rot, false);
        } else if (type.startsWith("diode")) {
            String rot = type.split("_")[1];
            return invertIfNeeded("diode", rot, true);
        } else if (type.startsWith("inductor")) {
            return "L";
        } else if (type.startsWith("resistor")) {
            return "R";
        }
        return null;
    }

    @Override
    public String getCircuiTikZDraw() {
        if (getNode1() == null)
            return null;
        int x1 = getNode1().getX();
        int y1 = getNode1().getY();
        String node1Str = "(" + x1 + ", " + y1 + ")";
        String node2Str = null;
        if (getNode2() != null) {
            int x2 = getNode2().getX();
            int y2 = getNode2().getY();
            node2Str = "(" + x2 + ", " + y2 + ")";
        }

        if (getTypeAsString().startsWith("gnd_1") || getNode2() == null) {
            return "\\draw " + node1Str + " node[eground]{};\n";
        } else {
            return "\\draw " + node1Str + " to [" + getCircuiTikZLabel() + "] " + node2Str + ";\n";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Element element = (Element) o;
        return type == element.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type);
    }
}
