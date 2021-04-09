package model.areas;

import model.Area;
import model.Coords;
import model.Size;

/**
 * Area in which carts can move.
 */
public class CartRouteArea implements Area {

    private Coords upperLeft;
    private Coords lowerRight;
    private Size size;
    private final AreaType type = AreaType.PATH;
    private final int[] neighbors = null;

    public CartRouteArea(Coords upperLeft, Coords lowerRight) {
        this.upperLeft = upperLeft;
        this.lowerRight = lowerRight;
        calcSize();
    }

    @Override
    public AreaType getType() {
        return type;
    }

    @Override
    public Size getSize() {
        calcSize();
        return size;
    }

    @Override
    public Coords getUpperLeft() {
        return upperLeft;
    }

    @Override
    public Coords getLowerRight() {
        return lowerRight;
    }

    @Override
    public void setUpperLeft(Coords c) {
        this.upperLeft = c;
    }

    @Override
    public void setLowerRight(Coords c) {
        this.lowerRight = c;
    }

    private void calcSize(){
        this.size = new Size(lowerRight.getRow() - upperLeft.getRow(), lowerRight.getColumn() - upperLeft.getColumn());
    }
}
