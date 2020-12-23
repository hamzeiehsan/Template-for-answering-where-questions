package anonymous.gazetteers.osm.scale;

public enum RelationType {
    //Q->A
    noToPoint, noToNo, noToInterval,
    equalPoint, finerPoint, coarserPoint, pointToNo, //point-> point (n)
    equalInterval, inside, contain, rightInside, leftInside, rightMeet, rightContain, rightOverlap, leftMeet, leftContain, leftOverlap, leftOutside, rightOutside, intervalToNo, //interval->interval (n)
    leftPoint, rightPoint, inPoint, outLeftPoint, outRightPoint, //interval->point
    leftInterval, rightInterval, containInterval, leftOutInterval, rightOutInterval//point->interval
}
