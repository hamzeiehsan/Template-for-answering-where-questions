package anonymous.gazetteers.osm.model;

import com.google.gson.annotations.SerializedName;
import fr.dudie.nominatim.model.BoundingBox;
import fr.dudie.nominatim.model.Element;
import fr.dudie.nominatim.model.PolygonPoint;


public final class Address {

    /** The OpenStreetMap place id. */
    @SerializedName("place_id")
    private long placeId;

    /** The data licence. */
    private String licence;

    /** The OpenStreetMap type (way, node...). */
    @SerializedName("osm_type")
    private String osmType;

    /** The OpenStreetMap identifier. */
    @SerializedName("osm_id")
    private String osmId;

    /** The bounding box enclosing the element. */
    @SerializedName("boundingbox")
    private BoundingBox boundingBox;

    /** The polygon points representing the element. */
    @Deprecated
    @SerializedName("polygonpoints")
    private PolygonPoint[] polygonPoints;

    /** The address longitude. */
    @SerializedName("lon")
    private double longitude;

    /** The address latitude. */
    @SerializedName("lat")
    private double latitude;

    /** The address display name. */
    @SerializedName("display_name")
    private String displayName;

    /** The OpenStreetMap element class (ex: highway). */
    @SerializedName("class")
    private String elementClass;

    /** The OpenStreetMap element type (ex: residential). */
    @SerializedName("type")
    private String elementType;

    /** The elements describing the address (ex: road, city, coutry...). */
    @SerializedName("address")
    private Element[] addressElements;

    /**
     * The elements describing the namedetails (ex: name, operator, short name...).
     */
    @SerializedName("namedetails")
    private Element[] namedetails;

    /**
     * The elements rank (ex: 30 = building).
     */
    @SerializedName("place_rank")
    private int placeRank;

    @SerializedName("importance")
    private double importance;

    /** The polygon as a WKT string */
    @SerializedName("geotext")
    private String wkt;

    /**
     * Gets the OpenStreetMap place id.
     *
     * @return the OpenStreetMap place id
     */
    public long getPlaceId() {

        return placeId;
    }

    /**
     * Sets the OpenStreetMap place id.
     *
     * @param placeId
     *            the OpenStreetMap place id to set
     */
    public void setPlaceId(final long placeId) {

        this.placeId = placeId;
    }

    /**
     * Gets the data licence.
     *
     * @return the data licence
     */
    public String getLicence() {

        return licence;
    }

    /**
     * Sets the data licence.
     *
     * @param licence
     *            the data licence to set
     */
    public void setLicence(final String licence) {

        this.licence = licence;
    }

    /**
     * Gets the OpenStreetMap type.
     *
     * @return the OpenStreetMap type
     */
    public String getOsmType() {

        return osmType;
    }

    /**
     * Sets the OpenStreetMap type.
     *
     * @param osmType
     *            the OpenStreetMap type to set
     */
    public void setOsmType(final String osmType) {

        this.osmType = osmType;
    }

    /**
     * Gets the OpenStreetMap identifier.
     *
     * @return the OpenStreetMap identifier
     */
    public String getOsmId() {

        return osmId;
    }

    /**
     * Sets the OpenStreetMap identifier.
     *
     * @param osmId
     *            the OpenStreetMap identifier to set
     */
    public void setOsmId(final String osmId) {

        this.osmId = osmId;
    }

    /**
     * Gets the bounding box enclosing the element.
     *
     * @return the bounding box enclosing the element
     */
    public BoundingBox getBoundingBox() {

        return boundingBox;
    }

    /**
     * Sets the bounding box enclosing the element.
     *
     * @param boundingBox
     *            the bounding box enclosing the element to set
     */
    public void setBoundingBox(final BoundingBox boundingBox) {

        this.boundingBox = boundingBox;
    }

    /**
     * Gets the polygon points representing the element.
     *
     * @return the polygon points representing the element
     */
    public PolygonPoint[] getPolygonPoints() {

        return polygonPoints;
    }

    /**
     * Sets the polygon points representing the element.
     *
     * @param polygonPoints
     *            the polygon points representing the element to set
     */
    public void setPolygonPoints(final PolygonPoint[] polygonPoints) {

        this.polygonPoints = polygonPoints;
    }


    /**
     * Gets the address longitude.
     *
     * @return the address longitude
     */
    public double getLongitude() {

        return longitude;
    }

    /**
     * Gets the address longitude.
     *
     * @return the address longitude
     */
    public int getLongitudeE6() {

        return (int) (longitude * 1E6);
    }

    /**
     * Sets the address longitude.
     *
     * @param longitude
     *            the address longitude to set
     */
    public void setLongitude(final double longitude) {

        this.longitude = longitude;
    }

    /**
     * Sets the address longitude.
     *
     * @param longitude
     *            the address longitude to set
     */
    public void setLongitudeE6(final int longitude) {

        this.longitude = longitude / 1E6;
    }

    /**
     * Gets the address latitude.
     *
     * @return the address latitude
     */
    public double getLatitude() {

        return latitude;
    }

    /**
     * Gets the address latitude.
     *
     * @return the address latitude
     */
    public int getLatitudeE6() {

        return (int) (latitude * 1E6);
    }

    /**
     * Sets the address latitude.
     *
     * @param latitude
     *            the address latitude to set
     */
    public void setLatitude(final double latitude) {

        this.latitude = latitude;
    }

    /**
     * Sets the address latitude.
     *
     * @param latitude
     *            the address latitude to set
     */
    public void setLatitudeE6(final int latitude) {

        this.latitude = latitude / 1E6;
    }

    /**
     * Gets the address display name.
     *
     * @return the address display name
     */
    public String getDisplayName() {

        return displayName;
    }

    /**
     * Sets the address display name.
     *
     * @param displayName
     *            the address display name to set
     */
    public void setDisplayName(final String displayName) {

        this.displayName = displayName;
    }

    /**
     * Gets the OpenStreetMap element class (ex: highway).
     *
     * @return the OpenStreetMap element class (ex: highway)
     */
    public String getElementClass() {

        return elementClass;
    }

    /**
     * Sets the OpenStreetMap element class (ex: highway).
     *
     * @param elementClass
     *            the OpenStreetMap element class (ex: highway) to set
     */
    public void setElementClass(final String elementClass) {

        this.elementClass = elementClass;
    }

    /**
     * Gets the penStreetMap element type (ex: residential).
     *
     * @return the penStreetMap element type (ex: residential)
     */
    public String getElementType() {

        return elementType;
    }

    /**
     * Sets the penStreetMap element type (ex: residential).
     *
     * @param elementType
     *            the penStreetMap element type (ex: residential) to set
     */
    public void setElementType(final String elementType) {

        this.elementType = elementType;
    }

    /**
     * Gets the elements describing the address (ex: road, city, coutry...).
     *
     * @return the elements describing the address (ex: road, city, coutry...)
     */
    public Element[] getAddressElements() {

        return addressElements;
    }

    /**
     * Sets the elements describing the address (ex: road, city, coutry...).
     *
     * @param addressElements
     *            the elements describing the address (ex: road, city, coutry...) to set
     */
    public void setAddressElements(final Element[] addressElements) {

        this.addressElements = addressElements;
    }

    /**
     * Sets the elements describing the namedetails (ex: name, operator, short name...).
     *
     * @return name
     */
    public Element[] getNameDetails() {
        return namedetails;
    }

    /**
     * Sets the elements describing the namedetails (ex: name, operator, short name...).
     *
     * @param namedetails
     *            the elements describing the namedetails (ex: name, operator, short name...)
     */
    public void setNameDetails(Element[] namedetails) {
        this.namedetails = namedetails;
    }

    /**
     * Gets the elements rank (ex: 30 = building).
     *
     * @return the elements elements rank (ex: 30 = building).
     */
    public int getPlaceRank() {

        return placeRank;
    }

    /**
     * Sets the elements rank (ex: 30 = building).
     *
     * @param placeRank
     *            the elements elements rank (ex: 30 = building) to set
     */
    public void setPlaceRank(final int placeRank) {

        this.placeRank = placeRank;
    }

    /**
     * Gets the address importance.
     *
     * @return the address importance
     */
    public double getImportance() {

        return importance;
    }

    /**
     * Gets the address importance.
     *
     * @return the address importance
     */
    public void setImportance(final double importance) {

        this.importance = importance;
    }

    /**
     * Gets polygon's WKT string.
     *
     * @return the polygon's WKT string; NULL if not available or applicable.
     */
    public String getWkt() {
        return wkt;
    }

    /**
     * Sets a polygon's WKT string.
     *
     * @param wkt
     *            the WKT string to set.
     */
    public String setWkt(String wkt) {
        return wkt;
    }

}