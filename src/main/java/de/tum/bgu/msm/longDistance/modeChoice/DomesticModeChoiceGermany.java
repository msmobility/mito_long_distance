package de.tum.bgu.msm.longDistance.modeChoice;

import com.pb.common.datafile.TableDataSet;
import de.tum.bgu.msm.JsonUtilMto;
import de.tum.bgu.msm.Util;
import de.tum.bgu.msm.longDistance.data.DataSet;
import de.tum.bgu.msm.longDistance.data.airport.AirLeg;
import de.tum.bgu.msm.longDistance.data.airport.Airport;
import de.tum.bgu.msm.longDistance.data.sp.EconomicStatus;
import de.tum.bgu.msm.longDistance.data.sp.HouseholdGermany;
import de.tum.bgu.msm.longDistance.data.sp.PersonGermany;
import de.tum.bgu.msm.longDistance.data.trips.*;
import de.tum.bgu.msm.longDistance.data.zoneSystem.ZoneTypeGermany;
import de.tum.bgu.msm.longDistance.destinationChoice.DomesticDestinationChoice;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.*;

/**
 * Germany wide travel demand model
 * Class to select the mode of travel of long distance trips
 * Author: Ana Moreno, Technical University of Munich (TUM), ana.moreno@tum.de
 * Date: 7 January 2021
 * Version 1
 * Adapted from Mode Choice Model from Carlos.
 */

public class DomesticModeChoiceGermany {
    private static Logger logger = Logger.getLogger(DomesticDestinationChoice.class);

    ResourceBundle rb;

    private DataSet dataSet;

    private TableDataSet mcGermany;
    private TableDataSet costsPerKm;

    private boolean calibration;
    private Map<Purpose, Map<Type, Map<Mode, Double>>> calibrationDomesticMcMatrix;

    // Scenario
    private boolean runScenario1;
    private float shuttleBusCostPerKm;
    private float shuttleBusCostBase;
    private boolean runScenario2;
    private float busCostFactor;
    private boolean runScenario3;


    public DomesticModeChoiceGermany(JSONObject prop, String inputFolder) {
        this.rb = rb;

        mcGermany = Util.readCSVfile(inputFolder + JsonUtilMto.getStringProp(prop,"mode_choice.domestic.germany.coef_file"));
        mcGermany.buildStringIndex(1);
        costsPerKm = Util.readCSVfile(inputFolder + JsonUtilMto.getStringProp(prop,"mode_choice.costPerKm_file"));
        costsPerKm.buildStringIndex(2);
        calibration = JsonUtilMto.getBooleanProp(prop,"mode_choice.calibration_domestic");
        calibrationDomesticMcMatrix = new HashMap<>();
        runScenario1 = JsonUtilMto.getBooleanProp(prop, "scenarioPolicy.shuttleBusToRail.run");
        shuttleBusCostPerKm = JsonUtilMto.getFloatProp(prop, "scenarioPolicy.shuttleBusToRail.costPerKm");
        shuttleBusCostBase = JsonUtilMto.getFloatProp(prop, "scenarioPolicy.shuttleBusToRail.costBase");
        runScenario2 = JsonUtilMto.getBooleanProp(prop, "scenarioPolicy.BusSpeedImprovement.run");
        busCostFactor = JsonUtilMto.getFloatProp(prop, "scenarioPolicy.BusSpeedImprovement.busCostFactor");
        runScenario3 = JsonUtilMto.getBooleanProp(prop, "scenarioPolicy.DeutschlandTakt_InVehTransferTimesReduction.run");
        logger.info("Domestic MC set up");

    }


    public void loadDomesticModeChoice(DataSet dataSet){
        this.dataSet = dataSet;
        for(Purpose purpose : PurposeGermany.values()){
            this.calibrationDomesticMcMatrix.put(purpose, new HashMap<>());
            for (Type tripState : TypeGermany.values()){
                this.calibrationDomesticMcMatrix.get(purpose).put(tripState,new HashMap<>());
                for (Mode mode : ModeGermany.values()){
                    this.calibrationDomesticMcMatrix.get(purpose).get(tripState).putIfAbsent(mode, .0);
                }
            }
        }
        logger.info("Domestic MC loaded");
    }



    public Mode selectModeDomestic(LongDistanceTrip t) {
        LongDistanceTripGermany trip = (LongDistanceTripGermany) t;
        double[] expUtilities = new double[ModeGermany.values().length];
        Map<String, Float> attributes = new HashMap<>();
        Mode selectedMode = null;
        if (trip.getTripState().equals(TypeGermany.AWAY)) {
            expUtilities[0] = 1;
            expUtilities[1] = 0;
            expUtilities[2] = 0;
            expUtilities[3] = 0;
        } else {

            //calculate exp(Ui) for each destination
            expUtilities = Arrays.stream(ModeGermany.values()).mapToDouble(m -> Math.exp(calculateUtilityFromGermany(trip, m))).toArray();
            double probability_denominator = Arrays.stream(expUtilities).sum();

            attributes = ((LongDistanceTripGermany) t).getAdditionalAttributes();
            //if there is no access by any mode for the selected OD pair, just go by car
            if (probability_denominator != 0 && !Double.isNaN(probability_denominator)) {
                for (int mode = 0; mode < expUtilities.length; mode++) {
                    attributes.put("utility_" + ModeGermany.getMode(mode), (float) (expUtilities[mode] / probability_denominator));
                }
            } else {
                expUtilities[0] = 1;
                expUtilities[1] = 0;
                expUtilities[2] = 0;
                expUtilities[3] = 0;
                for (int mode = 0; mode < expUtilities.length; mode++) {
                    attributes.put("utility_" + ModeGermany.getMode(mode), (float) expUtilities[mode]);
                }
            }
            ((LongDistanceTripGermany) t).setAdditionalAttributes(attributes);
            //choose one destination, weighted at random by the probabilities
        }
        selectedMode = (Mode) Util.selectGermany(expUtilities, ModeGermany.values());
        return selectedMode;
        //return new EnumeratedIntegerDistribution(modes, expUtilities).sample();
    }


    public double calculateUtilityFromGermany(LongDistanceTripGermany trip, Mode m) {


        double utility;
        String tripPurpose = trip.getTripPurpose().toString().toLowerCase();
        String tripState = trip.getTripState().toString().toLowerCase();
        String column = m.toString() + "." + tripPurpose+ "." + tripState;

        //zone-related variables
        int origin = trip.getOrigZone().getId();
        int destination = trip.getDestZone().getId();

        Map<String, Float> attr = trip.getAdditionalAttributes();
        double impedance = 0;
        double vot = mcGermany.getStringIndexedValueAt("vot", column);
        double time = 1000000000 / 3600;
        double timeAccess = 0;
        double timeEgress = 0;
        double timeTotal = 0;
        double distance = 1000000000 / 1000; //convert to km
        double distanceAccess = 0;
        double distanceEgress = 0;
        if (m.equals(ModeGermany.AIR)){
            if (trip.getAdditionalAttributes().get("originAirport") != null) {
                Airport originAirport = dataSet.getAirportFromId(Math.round(trip.getAdditionalAttributes().get("originAirport")));
                Airport destinationAirport = dataSet.getAirportFromId(Math.round(trip.getAdditionalAttributes().get("destinationAirport")));
                int flightId = dataSet.getConnectedAirports().get(originAirport).get(destinationAirport).get("flightId");
                List<AirLeg> legs = dataSet.getFligthFromId(flightId).getLegs();
                time = 0;
                distance = 0;
                for (AirLeg leg : legs) {
                    time = time + leg.getTime();
                    distance = distance + leg.getDistance();
                }
                if (legs.size() > 1){
                    time = time + dataSet.getTransferTimeAirport().get(legs.get(0).getDestination());
                }
                time = time + dataSet.getBoardingTime_sec() + dataSet.getPostprocessTime_sec();
                dataSet.getTravelTimeMatrix().get(m).setValueAt(origin, destination, (float) time);
                time = time / 3600;
                timeAccess = dataSet.getTravelTimeMatrix().get(ModeGermany.AUTO).getValueAt(origin, originAirport.getZone().getId()) / 3600;
                timeEgress = dataSet.getTravelTimeMatrix().get(ModeGermany.AUTO).getValueAt(destinationAirport.getZone().getId(), destination) / 3600;
                timeTotal = time + timeAccess + timeEgress;
                distance = distance / 1000;
                distanceAccess = dataSet.getDistanceMatrix().get(ModeGermany.AUTO).getValueAt(origin, originAirport.getZone().getId())/1000;
                distanceEgress = dataSet.getDistanceMatrix().get(ModeGermany.AUTO).getValueAt(destinationAirport.getZone().getId(), destination)/1000;
                attr.put("originAirportX", (float) originAirport.getAirportCoordX());
                attr.put("originAirportY", (float) originAirport.getAirportCoordY());
                attr.put("destinationAirportX", (float) destinationAirport.getAirportCoordX());
                attr.put("destinationAirportY", (float) destinationAirport.getAirportCoordY());
            }
        }
        else if (m.equals(ModeGermany.RAIL)){
                timeAccess = dataSet.getRailAccessTimeMatrix().get(ModeGermany.RAIL).getValueAt(origin, destination) / 3600;
                timeEgress = dataSet.getRailEgressTimeMatrix().get(ModeGermany.RAIL).getValueAt(origin, destination) / 3600;

            if(runScenario1){
                distanceAccess = dataSet.getRailAccessDistMatrix().get(ModeGermany.RAIL).getValueAt(origin, destination)/1000;
                distanceEgress = dataSet.getRailEgressDistMatrix().get(ModeGermany.RAIL).getValueAt(origin, destination)/1000;
            }
                distance = dataSet.getDistanceMatrix().get(m).getValueAt(origin, destination) / 1000;

                time = dataSet.getTravelTimeMatrix().get(m).getValueAt(origin, destination) / 3600;
                timeTotal = time + timeAccess + timeEgress;


        } else {
            time = dataSet.getTravelTimeMatrix().get(m).getValueAt(origin, destination) / 3600;
            timeTotal = time;
            distance = dataSet.getDistanceMatrix().get(m).getValueAt(origin, destination) / 1000; //convert to km
        }
        if (time < 1000000000 / 3600){
            if (vot != 0) {
                double cost =0 ;
                double costAccess =0 ;
                double costEgress =0 ;
                double costTotal =0 ;
                if(distance != 0){
                    cost = costsPerKm.getStringIndexedValueAt("alpha", m.toString()) *
                            Math.pow(distance, costsPerKm.getStringIndexedValueAt("beta", m.toString()))
                            * distance;
                    costTotal = cost;
                } else {
                    //todo technically the distance and cost cannot be zero. However, this happens when there is no segment by main mode (only access + egress)
                    cost = 0;
                    costTotal = cost;
                }

                if (m.equals(ModeGermany.AIR)) {
                    costAccess = distanceAccess * costsPerKm.getStringIndexedValueAt("alpha", ModeGermany.AUTO.name()) *
                            Math.pow(distanceAccess , costsPerKm.getStringIndexedValueAt("beta", ModeGermany.AUTO.name()));
                    costEgress = distanceEgress * costsPerKm.getStringIndexedValueAt("alpha", ModeGermany.AUTO.name()) *
                            Math.pow(distanceEgress , costsPerKm.getStringIndexedValueAt("beta", ModeGermany.AUTO.name()));
                    costTotal = cost + costAccess + costEgress;
                }

                if(runScenario2){
                    if (m.equals(ModeGermany.BUS)) {
                    cost = (costsPerKm.getStringIndexedValueAt("alpha", m.toString()) *
                            Math.pow(distance, costsPerKm.getStringIndexedValueAt("beta", m.toString())))* busCostFactor * distance;
                    costTotal = cost;
                    }
                }

                if(runScenario1){
                        if (m.equals(ModeGermany.RAIL)) {
                        distanceAccess = dataSet.getRailAccessDistMatrix().get(ModeGermany.RAIL).getValueAt(origin, destination)/1000;
                        costAccess = distanceAccess * shuttleBusCostPerKm + shuttleBusCostBase;
                        distanceEgress = dataSet.getRailEgressDistMatrix().get(ModeGermany.RAIL).getValueAt(origin, destination)/1000;
                        costEgress = distanceEgress * shuttleBusCostPerKm + shuttleBusCostBase;
                        costTotal = cost + costAccess + costEgress;
                    }
                }

                impedance = costTotal / (vot) + timeTotal;
                attr.put("cost_"+ m.toString(), (float) cost);
                attr.put("costAccess_"+ m.toString(), (float) costAccess);
                attr.put("costEgress_"+ m.toString(), (float) costEgress);
                attr.put("costTotal_"+ m.toString(), (float) costTotal);
                attr.put("time_" + m.toString(), (float) time);
                attr.put("timeAccess_" + m.toString(), (float) timeAccess);
                attr.put("timeEgress_" + m.toString(), (float) timeEgress);
                attr.put("distance_" + m.toString(), (float) distance);
                attr.put("distanceAccess_" + m.toString(), (float) distanceAccess);
                attr.put("distanceEgress_" + m.toString(), (float) distanceEgress);

            }
            trip.setAdditionalAttributes(attr);


            //person-related variables
            PersonGermany pers = trip.getTraveller();
            HouseholdGermany hh = pers.getHousehold();

            //getCoefficients
            double b_intercept = mcGermany.getStringIndexedValueAt("intercept", column);
            double b_male = mcGermany.getStringIndexedValueAt("isMale", column);
            double b_employed = mcGermany.getStringIndexedValueAt("isEmployed", column);
            double b_student = mcGermany.getStringIndexedValueAt("isStudent", column);
            double b_hhSize1 = mcGermany.getStringIndexedValueAt("isHhSize1", column);
            double b_hhSize2 = mcGermany.getStringIndexedValueAt("isHhSize2", column);
            double b_hhSize3 = mcGermany.getStringIndexedValueAt("isHhSize3", column);
            double b_hhSize4 = mcGermany.getStringIndexedValueAt("isHhSize4+", column);
            double b_below18 = mcGermany.getStringIndexedValueAt("isBelow18", column);
            double b_between18and39 = mcGermany.getStringIndexedValueAt("isBetween18and39", column);
            double b_between40and59 = mcGermany.getStringIndexedValueAt("isBetween40and59", column);
            double b_over60 = mcGermany.getStringIndexedValueAt("isOver60", column);
            double b_lowEconomicStatus = mcGermany.getStringIndexedValueAt("isLowEconomicStatus", column);
            double b_veryLowStatus = mcGermany.getStringIndexedValueAt("isVeryLowEconomicStatus", column);
            double b_highStatus = mcGermany.getStringIndexedValueAt("isHighEconomicStatus", column);
            double b_veryHighStatus = mcGermany.getStringIndexedValueAt("isVeryHighEconomicStatus", column);
            double b_impedance = mcGermany.getStringIndexedValueAt("impedance", column);
            double alpha_impedance = mcGermany.getStringIndexedValueAt("alpha", column);
            double k_calibration = mcGermany.getStringIndexedValueAt("k_calibration", column);


            double impedance_exp = Math.exp(alpha_impedance * impedance * 60);
            attr.put("impedance_" + m.toString(), (float) impedance_exp);

            if (calibration) k_calibration = k_calibration + calibrationDomesticMcMatrix.get(trip.getTripPurpose()).get(trip.getTripState()).get(m);

            utility = b_intercept +
                    b_male * Boolean.compare(pers.isMale(), false) +
                    b_employed * Boolean.compare(pers.isEmployed(), false) +
                    b_student * Boolean.compare(pers.isStudent(), false) +
                    b_hhSize1 * Boolean.compare(hh.getHhSize() == 1, false) +
                    b_hhSize2 * Boolean.compare(hh.getHhSize() == 2, false) +
                    b_hhSize3 * Boolean.compare(hh.getHhSize() == 3, false) +
                    b_hhSize4 * Boolean.compare(hh.getHhSize() > 3, false) +
                    b_below18 * Boolean.compare(pers.isBelow18(), false) +
                    b_between18and39 * Boolean.compare(pers.isBetween18and39(), false) +
                    b_between40and59 * Boolean.compare(pers.isBetween40and59(), false) +
                    b_over60 * Boolean.compare(pers.isOver60(), false) + +
                    b_veryLowStatus * Boolean.compare(hh.getEconomicStatus().equals(EconomicStatus.VERYLOW), false) +
                    b_lowEconomicStatus * Boolean.compare(hh.getEconomicStatus().equals(EconomicStatus.LOW), false) +
                    b_highStatus * Boolean.compare(hh.getEconomicStatus().equals(EconomicStatus.HIGH), false) +
                    b_veryHighStatus * Boolean.compare(hh.getEconomicStatus().equals(EconomicStatus.VERYHIGH), false) +
                    b_impedance * Math.exp(alpha_impedance * impedance * 60) +
                    k_calibration
            ;

        } else {
            utility = Double.NEGATIVE_INFINITY;
        }

        return utility;

    }


    public float getDomesticModalTravelTime(LongDistanceTrip t){
        LongDistanceTripGermany trip = (LongDistanceTripGermany) t;
        int origin = trip.getOrigZone().getId();
        int destination = trip.getDestZone().getId();
        if (!trip.getOrigZone().getZoneType().equals(ZoneTypeGermany.GERMANY) || trip.getDestZoneType().equals(ZoneTypeGermany.EXTEU) || trip.getDestZoneType().equals(ZoneTypeGermany.EXTOVERSEAS) ){
            return -1.f;
        } else {
            Mode mode = trip.getMode();
            if (mode != null) {
                return dataSet.getTravelTimeMatrix().get(mode).getValueAt(origin, destination);
            } else {
                return 0;
            }
        }
    }



    public void updateDomesticMcCalibration(Map<Purpose, Map<Type, Map<Mode, Double>>> updatedMatrix) {

        for(Purpose purpose : PurposeGermany.values()){
            for (Type tripState : TypeGermany.values()){
                for (Mode mode : ModeGermany.values()){
                    double newValue = this.calibrationDomesticMcMatrix.get(purpose).get(tripState).get(mode) + updatedMatrix.get(purpose).get(tripState).get(mode);
                    this.calibrationDomesticMcMatrix.get(purpose).get(tripState).put(mode, newValue);
                    System.out.println("Domestic/k-factor: " + purpose + "\t" + tripState + "\t" + mode + "\t" + calibrationDomesticMcMatrix.get(purpose).get(tripState).get(mode));

                }
            }
        }
    }

    public Map<Purpose, Map<Type,  Map<Mode, Double>>> getCalibrationMatrix() {
        return calibrationDomesticMcMatrix;
    }

    public float getDomesticModalDistance(LongDistanceTrip t){
        LongDistanceTripGermany trip = (LongDistanceTripGermany) t;
        int origin = trip.getOrigZone().getId();
        int destination = trip.getDestZone().getId();
        if (!trip.getOrigZone().getZoneType().equals(ZoneTypeGermany.GERMANY) || trip.getDestZoneType().equals(ZoneTypeGermany.EXTEU) || trip.getDestZoneType().equals(ZoneTypeGermany.EXTOVERSEAS) ){
            return -1.f;
        } else {
            Mode mode = trip.getMode();
            if (mode != null) {
                return dataSet.getDistanceMatrix().get(mode).getValueAt(origin, destination);
            } else {
                return 0;
            }
        }
    }

}
