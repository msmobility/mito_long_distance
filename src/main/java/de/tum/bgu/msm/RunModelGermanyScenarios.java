package de.tum.bgu.msm;

import de.tum.bgu.msm.longDistance.CalibrationGermany;
import de.tum.bgu.msm.longDistance.LDModelGermany;
import de.tum.bgu.msm.longDistance.LDModelGermanyScenarios;
import de.tum.bgu.msm.longDistance.data.DataSet;
import de.tum.bgu.msm.longDistance.destinationChoice.DestinationChoiceGermany;
import de.tum.bgu.msm.longDistance.emissions.Emissions;
import de.tum.bgu.msm.longDistance.io.reader.EconomicStatusReader;
import de.tum.bgu.msm.longDistance.io.reader.SkimsReaderGermany;
import de.tum.bgu.msm.longDistance.io.reader.SyntheticPopulationReaderGermany;
import de.tum.bgu.msm.longDistance.io.reader.ZoneReaderGermany;
import de.tum.bgu.msm.longDistance.io.writer.OutputWriterGermanScenario;
import de.tum.bgu.msm.longDistance.io.writer.OutputWriterGermany;
import de.tum.bgu.msm.longDistance.modeChoice.ModeChoiceGermany;
import de.tum.bgu.msm.longDistance.modeChoice.ModeChoiceGermanyScenario;
import de.tum.bgu.msm.longDistance.timeOfDay.TimeOfDayChoiceGermany;
import de.tum.bgu.msm.longDistance.tripGeneration.TripGenerationGermany;
import org.apache.log4j.BasicConfigurator; // Alona
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.io.File;


/**
 * Germany Model
 * Module to simulate long-distance travel
 * Author: Ana Moreno, Technische Universität München (TUM), ana.moreno@tum.de
 * Date: 11 December 2020
 * Version 1
 * Adapted from Ontario Provincial Model
 */

public class RunModelGermanyScenarios {
    // main class
    private static Logger logger = Logger.getLogger(RunModelGermanyScenarios.class);
    private JSONObject prop;

    private RunModelGermanyScenarios(JSONObject prop) {
        // constructor
        this.prop = prop;
    }


    public static void main(String[] args) {
        // main model run method
        BasicConfigurator.configure();
        logger.info("MITO Long distance model");
        long startTime = System.currentTimeMillis();
        JsonUtilMto jsonUtilMto = new JsonUtilMto(args[0]);
        JSONObject prop = jsonUtilMto.getJsonProperties();

        RunModelGermanyScenarios model = new RunModelGermanyScenarios(prop);
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
        String outputFolder = inputFolder + "output/" +  JsonUtilMto.getStringProp(prop, "scenario") + "/";
        createDirectoryIfNotExistingYet(outputFolder);


        LDModelGermanyScenarios ldModelGermany = new LDModelGermanyScenarios(new ZoneReaderGermany(), new SkimsReaderGermany(),
                new SyntheticPopulationReaderGermany(),new EconomicStatusReader(),
                new TripGenerationGermany(), new DestinationChoiceGermany(), new ModeChoiceGermanyScenario(),
                new TimeOfDayChoiceGermany(), new Emissions(), new OutputWriterGermanScenario(), new CalibrationGermany());
        ldModelGermany.setup(prop, inputFolder, outputFolder);
        ldModelGermany.load(dataSet);
        ldModelGermany.run(dataSet, -1);
        logger.info("Module runLongDistModel completed.");

    }

    public static void createDirectoryIfNotExistingYet (String directory) {
        File test = new File (directory);
        test.mkdirs();
        if(!test.exists()) {
            logger.error("Could not create scenarios directory " + directory);
            throw new RuntimeException();
        }
    }

}
