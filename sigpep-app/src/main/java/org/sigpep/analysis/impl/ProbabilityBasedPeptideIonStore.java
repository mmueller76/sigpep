package org.sigpep.analysis.impl;

import org.sigpep.analysis.ChargeProbabilityCalculator;
import org.sigpep.analysis.PeptideIonStore;
import org.sigpep.Configuration;
import org.sigpep.model.MassOverChargeRange;
import org.sigpep.model.PeptideIon;
import org.sigpep.model.impl.MassOverChargeRangeImpl;
import org.sigpep.util.SigPepUtil;

import java.util.*;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.<br/>
 * User: mmueller<br/>
 * Date: 12-Aug-2008<br/>
 * Time: 11:31:10<br/>
 */
public class ProbabilityBasedPeptideIonStore<P extends PeptideIon> implements PeptideIonStore<P> {

    protected static Logger logger = Logger.getLogger(ProbabilityBasedPeptideIonStore.class);

    private SortedMap<Double, Set<P>> store;

    private int massPrecission = Configuration.getInstance().getInt("sigpep.app.monoisotopic.mass.precision");
    private List<Map<Double, Integer>> observedMassChargeStateCombinations;
    private double massAccuracy;
    private ChargeProbabilityCalculator chargeProbabilityCalculator;
    private double probabilityThreshold = 0.1;

    public ProbabilityBasedPeptideIonStore(List<Map<Double, Integer>> observedMassChargeStateCombinations,
                                           double massAccuracy,
                                           int massPrecission) {
        this(observedMassChargeStateCombinations, massAccuracy);
        this.massPrecission = massPrecission;

    }

    public ProbabilityBasedPeptideIonStore(List<Map<Double, Integer>> observedMassChargeStateCombinations,
                                           double massAccuracy) {
        this.chargeProbabilityCalculator = new KernelBasedChargeProbabilityCalculator(observedMassChargeStateCombinations);
        this.observedMassChargeStateCombinations = observedMassChargeStateCombinations;
        this.massAccuracy = massAccuracy;

    }

    public void populate(Collection<P> peptideIons) {

        //logger.info("ProbabilityBasedPeptideIonStore.populate");
        store = new TreeMap<Double, Set<P>>();
        for (P peptideIon : peptideIons) {

            double mass = peptideIon.getNeutralMassPeptide();
            mass = SigPepUtil.round(mass, massPrecission);

            if (!store.containsKey(mass)) {
                store.put(mass, new HashSet<P>());
            }
            store.get(mass).add(peptideIon);

        }

        assignChargeStates();

    }

    private void assignChargeStates() {

        //logger.info("ProbabilityBasedPeptideIonStore.assignChargeStates");

        double[] uniqueMasses = new double[store.keySet().size()];
        int i = 0;
        for (Double mass : store.keySet()) {
            uniqueMasses[i] = mass;
            i++;
        }


        Map<Double, Map<Integer, Double>> chargeProbabilities = chargeProbabilityCalculator.getChargeProbablitiesGivenMass(uniqueMasses);

        for (double mass : chargeProbabilities.keySet()) {

            Map<Integer, Double> probabilities = chargeProbabilities.get(mass);

            for (P peptideIon : store.get(mass)) {

                for (Integer z : probabilities.keySet()) {
                    double p = probabilities.get(z);
                    if (z > 0) {
                        peptideIon.addAllowedChargeState(z, p);
                    }
                }

            }

        }

        //logger.info("done");

    }


    public Set<P> getPeptideIonsWithMass(double mass) {

        return store.get(mass);

    }

    public Set<P> getPeptideIonsInMassRange(double lowerMassLimit, double upperMassLimit) {

        Set<P> retVal = new HashSet<P>();
        for (Set<P> peptideIons : store.subMap(lowerMassLimit, upperMassLimit).values()) {
            retVal.addAll(peptideIons);
        }
        return retVal;

    }

    public Map<Integer, Set<P>> getPeptideIonsWithOverlappingMassOverCharge(P peptideIon) {

        double mass = peptideIon.getNeutralMassPeptide();
        mass = SigPepUtil.round(mass, massPrecission);
        return getPeptideIonsWithOverlappingMassOverCharge(mass);

    }

    public Map<Integer, Set<P>> getPeptideIonsWithOverlappingMassOverCharge(double neutralMassPeptide) {

        
        double mass = SigPepUtil.round(neutralMassPeptide, massPrecission);

        Map<Integer, Set<P>> retVal = new TreeMap<Integer, Set<P>>();

        Map<Double, Map<Integer, Double>> chargeProbabilityByMass = chargeProbabilityCalculator.getChargeProbablitiesGivenMass(mass);

        Map<Integer, Double> chargeProbability = chargeProbabilityByMass.get(mass);

        //get most probable charge state
        int mostProbableCharge = -1;
        double previousProbability = 0;
        for (Integer charge : chargeProbability.keySet()) {

            double probability = chargeProbability.get(charge);
            if (probability > previousProbability) {
                mostProbableCharge = charge;
            }
            previousProbability = probability;

        }


        if (mostProbableCharge != -1) {

            Set<P> peptideIons = new HashSet<P>();

            Set<Integer> targetChargeState = new HashSet<Integer>();
            targetChargeState.add(mostProbableCharge);


            MassOverChargeRange targetPeptideMassOverChargeRange = new MassOverChargeRangeImpl(mass, targetChargeState, massAccuracy);

            for (Integer charge : chargeProbabilityCalculator.getAllowedChargeStates()) {


                List<MassOverChargeRange[]> backgroundPeptideMassOverChargeRanges = targetPeptideMassOverChargeRange.getFlankingPeptideMassOverChargeRanges(charge);


                for (MassOverChargeRange[] backgroundPeptideMassOverChargeRange : backgroundPeptideMassOverChargeRanges) {

                    double lowerFlankingMass = backgroundPeptideMassOverChargeRange[0].getNeutralPeptideMass();
                    double upperFlankingMass = backgroundPeptideMassOverChargeRange[1].getNeutralPeptideMass();

                    for (P peptideIon : this.getPeptideIonsInMassRange(lowerFlankingMass, upperFlankingMass)) {

                        double peptideMass = peptideIon.getNeutralMassPeptide();
                        double[] pCharge = chargeProbabilityCalculator.getProbabilityOfChargeGivenMass(charge, peptideMass);
                        if (pCharge[0] >= probabilityThreshold) {
                            peptideIons.add(peptideIon);
                        }

                    }

                }

            }

            retVal.put(mostProbableCharge, peptideIons);


        }

        return retVal;

    }

    public Set<Double> getUniqueNeutralPeptideIonMasses() {
        return store.keySet();
    }

    public List<Map<Double, Integer>> getObservedMassChargeStateCombinations() {
        return observedMassChargeStateCombinations;
    }

    public void setObservedMassChargeStateCombinations(List<Map<Double, Integer>> observedMassChargeStateCombinations) {
        this.observedMassChargeStateCombinations = observedMassChargeStateCombinations;
    }

    public double getMassAccuracy() {
        return massAccuracy;
    }

    public void setMassAccuracy(double massAccuracy) {
        this.massAccuracy = massAccuracy;
    }

    public int getMassPrecission() {
        return massPrecission;
    }

    public void setMassPrecission(int massPrecission) {
        this.massPrecission = massPrecission;
    }



}