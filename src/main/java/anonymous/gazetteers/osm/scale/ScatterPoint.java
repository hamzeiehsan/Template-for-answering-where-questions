package anonymous.gazetteers.osm.scale;

public class ScatterPoint implements Comparable<ScatterPoint> {
    private Integer x, y;

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }


    @Override
    public int hashCode() {
        return this.x.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ScatterPoint) {
            ScatterPoint temp = (ScatterPoint) o;
            if (this.compareTo(temp) == 0)
                return true;
        }
        return super.equals(o);
    }

    @Override
    public int compareTo(ScatterPoint scatterPoint) {
        if (this.x == scatterPoint.x)
            return 0;
        else if (this.y - scatterPoint.y > 0)
            return 1;
        return -1;
    }
}
