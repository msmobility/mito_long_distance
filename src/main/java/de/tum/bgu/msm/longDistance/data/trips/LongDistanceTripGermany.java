package de.tum.bgu.msm.longDistance.data.trips;


import de.tum.bgu.msm.longDistance.data.sp.Person;
import de.tum.bgu.msm.longDistance.data.sp.PersonGermany;
import de.tum.bgu.msm.longDistance.data.zoneSystem.*;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Class to hold a long distance trip
 * Author: Ana Moreno, Technical University of Munich (TUM), ana.moreno@tum.de
 * Date: 8 December 2020
 * Version 1
 * Adapted from Ontario
 *
 */
public class LongDistanceTripGermany implements LongDistanceTrip {

    private int tripId;
    private PersonGermany traveller;
    private boolean international;
    private Purpose tripPurpose;
    private Type tripState;
    private ZoneGermany origZone;
    private ZoneTypeGermany destZoneType;
    private Zone destZone;
    private Mode travelMode;
    private float autoTravelDistance = -1;
    private float distanceByMode = -1;
    private float travelTime = -1;
    private int departureTimeInHours = -999;
    private int departureTimeInHoursSecondSegment = -999; //this is the return trip of daytrips
    private boolean returnOvernightTrip = false;
    private Map<Pollutant, Float> emissions = new HashMap<>();
    private Map<String, Float> additionalAttributes = new HashMap<>();
    private int scenario;

    public LongDistanceTripGermany(int tripId, PersonGermany traveller, boolean international, Purpose tripPurpose, Type tripState, ZoneGermany origZone ) {
        this.tripId = tripId;
        this.traveller = traveller;
        this.international = international;
        this.tripPurpose = tripPurpose;
        this.tripState = tripState;
        this.origZone = origZone;
    }

    public int getTripId() {
        return tripId;
    }

    public int getTravellerId() {
        return traveller.getPersonId();
    }

    public PersonGermany getTraveller() {
        return traveller;
    }

    public boolean isInternational() {
        return international;
    }

    public int getAdultsHhTravelPartySize() { return 0; }

    public int getKidsHhTravelPartySize() { return 0; }

    public ZoneGermany getOrigZone() { return origZone; }

    public void setMode(Mode travelMode) {
        this.travelMode = travelMode;
    }

    public ZoneTypeGermany getDestZoneType() {
        return destZoneType;
    }

    public void setDestZoneType(ZoneTypeGermany destZoneType) {
        this.destZoneType = destZoneType;
    }

    public Zone getDestZone() {
        return destZone;
    }

    public void setDestZone(Zone destZone) {
        this.destZone = destZone;
    }

    public float getAutoTravelDistance() {
        return autoTravelDistance;
    }

    public void setAutoTravelDistance(float autoTravelDistance) {
        this.autoTravelDistance = autoTravelDistance;
    }

    public float getDistanceByMode() {
        return distanceByMode;
    }

    public void setDistanceByMode(float distanceByMode) {
        this.distanceByMode = distanceByMode;
    }

    public int getDepartureTimeInHours() {
        return departureTimeInHours;
    }

    public void setDepartureTimeInHours(int departureTimeInHours) {
        this.departureTimeInHours = departureTimeInHours;
    }

    public int getDepartureTimeInHoursSecondSegment() {
        return departureTimeInHoursSecondSegment;
    }

    public void setDepartureTimeInHoursSecondSegment(int departureTimeInHoursSecondSegment) {
        this.departureTimeInHoursSecondSegment = departureTimeInHoursSecondSegment;
    }

    public boolean isReturnOvernightTrip() {
        return returnOvernightTrip;
    }

    public void setReturnOvernightTrip(boolean returnOvernightTrip) {
        this.returnOvernightTrip = returnOvernightTrip;
    }

    public float getTravelTime() {
        return travelTime;
    }

    public void setTravelTime(float travelTime) {
        this.travelTime = travelTime;
    }

    public static String getHeader() {
        return "tripId,personId" +
                ",international,tripPurpose,tripState,tripOriginZone,tripOriginType" +
                ",tripDestZone,tripDestType,travelDistance_km" +
                ",tripMode,travelTimeByMode_h"+
                ",departureTime,departureTimeReturnDaytrip,ReturnOvernightTrip"+
                ",CO2emissions_kg" +
                 ",utility_" + ModeGermany.getMode(0)+
                ",utility_" + ModeGermany.getMode(1)+
                ",utility_" + ModeGermany.getMode(2)+
                ",utility_" + ModeGermany.getMode(3)+
                ",impedance_" + ModeGermany.getMode(0)+
                ",impedance_" + ModeGermany.getMode(1)+
                ",impedance_" + ModeGermany.getMode(2)+
                ",impedance_" + ModeGermany.getMode(3)+
                ",cost_" + ModeGermany.getMode(0)+
                ",cost_" + ModeGermany.getMode(1)+
                ",cost_" + ModeGermany.getMode(2)+
                ",cost_" + ModeGermany.getMode(3)+
                ",time_" + ModeGermany.getMode(0)+
                ",time_" + ModeGermany.getMode(1)+
                ",time_" + ModeGermany.getMode(2)+
                ",time_" + ModeGermany.getMode(3)+
                ",distance_" + ModeGermany.getMode(0)+
                ",distance_" + ModeGermany.getMode(1)+
                ",distance_" + ModeGermany.getMode(2)+
                ",distance_" + ModeGermany.getMode(3)
                //"utility_auto", "tt_auto", ""cost_auto" +,
                //"utility_rail, "tt_rail", ""cost_rail" +,
                //"utility_bus", "tt_bus", ""cost_bus" +,
                //"utility_air", "tt_air", ""cost_air",
//                + ",personAge,personGender," +
        //        "personEducation,personWorkStatus,personIncome,adultsInHh,kidsInHh"
                ;
    }
    @Override
    public String toString() {
        LongDistanceTripGermany tr = this;
        String str = null;
        if (tr.getOrigZone().getZoneType().equals(ZoneTypeGermany.GERMANY)) {
            Person traveller = tr.getTraveller();

            str = (tr.getTripId()
                    + "," + tr.getTravellerId()
                    + "," + tr.isInternational()
                    + "," + tr.tripPurpose.toString()
                    + "," + tr.tripState.toString()
                    + "," + tr.getOrigZone().getId()
                    + "," + tr.getOrigZone().getZoneType()
                    + "," + tr.getDestZone().getId()
                    + "," + tr.getDestZone().getZoneType()
                    + "," + tr.getDistanceByMode() / 1000
                    + "," + tr.getMode()
                    + "," + tr.getTravelTime() / 3600
                    + "," + tr.getDepartureTimeInHours()
                    + "," + tr.getDepartureTimeInHoursSecondSegment()
                    + "," + tr.isReturnOvernightTrip()
                    + "," + tr.getEmissions().get(Pollutant.CO2)
                    + "," + tr.getAdditionalAttributes().get("utility_" + ModeGermany.getMode(0))
                    + "," + tr.getAdditionalAttributes().get("utility_" + ModeGermany.getMode(1))
                    + "," + tr.getAdditionalAttributes().get("utility_" + ModeGermany.getMode(2))
                    + "," + tr.getAdditionalAttributes().get("utility_" + ModeGermany.getMode(3))
                    + "," + tr.getAdditionalAttributes().get("impedance_auto")
                    + "," + tr.getAdditionalAttributes().get("impedance_air")
                    + "," + tr.getAdditionalAttributes().get("impedance_rail")
                    + "," + tr.getAdditionalAttributes().get("impedance_bus")
                    + "," + tr.getAdditionalAttributes().get("cost_auto")
                    + "," + tr.getAdditionalAttributes().get("cost_air")
                    + "," + tr.getAdditionalAttributes().get("cost_rail")
                    + "," + tr.getAdditionalAttributes().get("cost_bus")
                    + "," + tr.getAdditionalAttributes().get("time_auto")
                    + "," + tr.getAdditionalAttributes().get("time_air")
                    + "," + tr.getAdditionalAttributes().get("time_rail")
                    + "," + tr.getAdditionalAttributes().get("time_bus")
                    + "," + tr.getAdditionalAttributes().get("distance_auto")
                    + "," + tr.getAdditionalAttributes().get("distance_air")
                    + "," + tr.getAdditionalAttributes().get("distance_rail")
                    + "," + tr.getAdditionalAttributes().get("distance_bus")
                    /*+ "," + traveller.getAge()
                    + "," + Character.toString(traveller.getGender())
                    + "," + traveller.getEducation()
                    + "," + traveller.getWorkStatus()
                    + "," + traveller.getIncome()
                    + "," + traveller.getAdultsHh()
                    + "," + traveller.getKidsHh()*/
            );
        } else {
            str =  (tr.getTripId()
                    + "," + tr.getTravellerId()
                    + "," + tr.isInternational()
                    + "," + tr.tripPurpose.toString()
                    + "," + tr.tripState.toString()
                    + "," + tr.getOrigZone().getId()
                    + "," + tr.getOrigZone().getZoneType()
                    + "," + tr.getMode()
                    + "," + tr.getAdultsHhTravelPartySize()
                    + "," + tr.getKidsHhTravelPartySize()
                    + "," + tr.getDestZoneType()
                    + "," + tr.getDestZone().getId()
                    + "," + tr.getAutoTravelDistance()
                    + "," + tr.getDepartureTimeInHours()
                    + "," + tr.getDepartureTimeInHoursSecondSegment()
                    + "," + tr.isReturnOvernightTrip()
                    + "," + tr.getTravelTime()
                    //+ ",-1,,-1,-1,-1,-1,-1"
            );

        }
        return str;
    }

    @Override
    public Mode getMode() {
        return travelMode;
    }

    @Override
    public Type getTripState() {
        return tripState;
    }

    @Override
    public Purpose getTripPurpose() {
        return tripPurpose;
    }

    @Override
    public Map<Pollutant, Float> getEmissions() {
        return emissions;
    }

    public Map<String, Float> getAdditionalAttributes() {
        return additionalAttributes;
    }

    public void setAdditionalAttributes(Map<String, Float> additionalAttributes) {
        this.additionalAttributes = additionalAttributes;
    }

    public void setEmissions(Map<Pollutant, Float>  emissions) {
        this.emissions = emissions;
    }

    public int getScenario() {
        return scenario;
    }

    public void setScenario(int scenario) {
        this.scenario = scenario;
    }
}
