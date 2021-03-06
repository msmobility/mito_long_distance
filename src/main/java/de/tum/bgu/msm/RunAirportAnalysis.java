package de.tum.bgu.msm;

import de.tum.bgu.msm.longDistance.AirportAnalysisModel;
import de.tum.bgu.msm.longDistance.airportAnalysis.AirportAnalysis;
import de.tum.bgu.msm.longDistance.data.DataSet;
import de.tum.bgu.msm.longDistance.io.writer.OutputWriterOntario;
import de.tum.bgu.msm.longDistance.io.reader.*;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;


/**
 * Ontario Provincial Model
 * Module to simulate long-distance travel
 * Author: Rolf Moeckel, Technische Universität München (TUM), rolf.moeckel@tum.de
 * Date: 11 December 2015
 * Version 1
 */

public class RunAirportAnalysis {
    // main class
    private static Logger logger = Logger.getLogger(RunAirportAnalysis.class);
    private JSONObject prop;

    private RunAirportAnalysis(JSONObject prop) {
        // constructor
        this.prop = prop;
    }


    public static void main(String[] args) {
        // main model run method

        logger.info("MITO Long distance model");
        long startTime = System.currentTimeMillis();
        JsonUtilMto jsonUtilMto = new JsonUtilMto(args[1]);
        JSONObject prop = jsonUtilMto.getJsonProperties();

        RunAirportAnalysis model = new RunAirportAnalysis(prop);
        model.runLongDistModel();
        float endTime = Util.rounder(((System.currentTimeMillis() - startTime) / 60000), 1);
        int hours = (int) (endTime / 60);
        int min = (int) (endTime - 60 * hours);
        int seconds = (int) ((endTime - 60 * hours - min) * 60);
        logger.info("Runtime: " + hours + " hours and " + min + " minutes and " + seconds + " seconds.");
    }


    private void runLongDistModel() {
        // main method to run long-distance model
        logger.info("Started runLongDistModel for the year " + JsonUtilMto.getIntProp(prop, "year"));
        DataSet dataSet = new DataSet();
        String inputFolder =  JsonUtilMto.getStringProp(prop, "work_folder");
        String outputFolder = JsonUtilMto.getStringProp(prop, "work_folder");



        AirportAnalysisModel ldModel = new AirportAnalysisModel(new ZoneReaderGermany(), new SkimsAutoReaderGermany(), new AirportAnalysis(), new OutputWriterOntario());
        ldModel.setup(prop, inputFolder, outputFolder);
        ldModel.load(dataSet);
        ldModel.run(dataSet, -1);
        logger.info("Module runLongDistModel completed.");

    }
}
