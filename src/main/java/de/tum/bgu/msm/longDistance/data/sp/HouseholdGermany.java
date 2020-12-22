package de.tum.bgu.msm.longDistance.data.sp;

import de.tum.bgu.msm.longDistance.data.zoneSystem.ZoneGermany;
import de.tum.bgu.msm.longDistance.data.zoneSystem.ZoneOntario;
import org.apache.log4j.Logger;

/**
 *
 * Ontario Provincial Model
 * Class to store synthetic households
 * Author: Ana Moreno, Technical University of Munich (TUM), ana.moreno@tum.de
 * Date: 8 December 2020
 * Version 1
 * Adapted from Ontario
 * Version 1
 *
 */

public class HouseholdGermany implements Household {

    static Logger logger = Logger.getLogger(HouseholdGermany.class);

    private int id;
    private int hhSize;
    private int hhInc;
    private int taz;
    private ZoneGermany zone;
    private PersonGermany[] persons;
    private EconomicStatus hhEconomicStatus;
    private int hhAutos;


    public HouseholdGermany(int id, int taz, int hhAutos, ZoneGermany zone) {
        this.id      = id;
        this.hhAutos = hhAutos;
        this.hhSize  = 0;
        this.hhInc   = 0;
        this.taz = taz;
        this.zone = zone;
        this.persons = new PersonGermany[0];
    }


    @Override
    public void addPersonForInitialSetup(Person per) {
        // This method adds a person to the household (only used for initial setup)

        PersonGermany[] personsAddedSoFar = persons;
        persons = new PersonGermany[personsAddedSoFar.length + 1];
        System.arraycopy(personsAddedSoFar, 0, persons, 0, persons.length-1);
        persons[persons.length-1] = (PersonGermany) per;
        hhSize++;
    }

    @Override
    public int getId() {
        return id;
    }

    public int getHhSize() {
        return hhSize;
    }

    public int getHhInc() {
        return hhInc;
    }

    public int getTaz() {
        return taz;
    }

    public ZoneGermany getZone() {return zone;}

    public PersonGermany[] getPersonsOfThisHousehold() {
        return persons;
    }

    public EconomicStatus getEconomicStatus() {
        return hhEconomicStatus;
    }

    public void setEconomicStatus(EconomicStatus hhEconomicStatus) {
        this.hhEconomicStatus = hhEconomicStatus;
    }

    public int getHhAutos() {
        return hhAutos;
    }

    public void setHhAutos(int hhAutos) {
        this.hhAutos = hhAutos;
    }

    public int getMonthlyIncome_EUR() {
        return hhInc;
    }

    public void addIncome(int inc) {
        hhInc += inc;
    }

    public int getAdultsEconomicStatusHh() {
        int adultsHh = 0;
        for (PersonGermany p : persons) {
            if (p.getAge() > 14) {
                adultsHh++;
            }
        }
        return adultsHh;
    }

    public int getChildrenEconomicStatusHh() {
        int childrenHh = 0;
        for (PersonGermany p : persons) {
            if (p.getAge() <= 14) {
                childrenHh++;
            }
        }
        return childrenHh;
    }
}