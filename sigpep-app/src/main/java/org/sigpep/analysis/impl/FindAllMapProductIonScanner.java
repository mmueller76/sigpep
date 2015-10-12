package org.sigpep.analysis.impl;

import org.sigpep.ExecutorServiceLocator;
import org.sigpep.analysis.ExclusionScoreCalculator;
import org.sigpep.model.Peptide;
import org.sigpep.model.ProductIon;
import org.sigpep.model.ProductIonType;
import org.sigpep.util.Combinations;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by IntelliJ IDEA.<br/>
 * User: mmueller<br/>
 * Date: 05-Aug-2008<br/>
 * Time: 13:44:31<br/>
 */
public class FindAllMapProductIonScanner extends AbstractMapProductIonScanner{


    public FindAllMapProductIonScanner(HashSet<ProductIonType> targetProductIonTypes,
                                       HashSet<ProductIonType> backgroundProductIonTypes,
                                       HashSet<Integer> productIonChargeStates,
                                       Double massAccuracy,
                                       Integer minimumCombinationSize,
                                       Integer maximumCombinationSize) {

        super(targetProductIonTypes,
                backgroundProductIonTypes,
                productIonChargeStates,
                massAccuracy,
                minimumCombinationSize,
                maximumCombinationSize);

    }

    protected Map<Set<ProductIon>, Double> getUniqueProductIonCombinations(Map<ProductIon, Map<Peptide, Integer>> exclusionMatrix, int minCombinationSize, int maxCombinationSize) {
        Map<Set<ProductIon>, Double> retVal = Collections.synchronizedMap(new HashMap<Set<ProductIon>, Double>());

        Set<ProductIon> targetProductIons = exclusionMatrix.keySet();

        ExecutorService executorService = ExecutorServiceLocator.getInstance().getExecutorService();

        for (int k = minCombinationSize; k <= maxCombinationSize; k++) {

            List<Future<Map<Set<ProductIon>, Double>>> results = new ArrayList<Future<Map<Set<ProductIon>, Double>>>();

            Combinations<ProductIon> combinations = new Combinations<ProductIon>(k, targetProductIons);
            while (combinations.hasNext()) {

                Set<ProductIon> combination = combinations.next();

                ExclusionScoreCalculator<Map<Set<ProductIon>, Double>> calculator = exclusionScoreCalculatorFactory.getCalculator(combination, exclusionMatrix);
                Future<Map<Set<ProductIon>, Double>> f = executorService.submit(calculator);
                results.add(f);

            }

            for (Future<Map<Set<ProductIon>, Double>> result : results) {

                try {
                    retVal.putAll(result.get());
                } catch (InterruptedException e) {
                    throw new RuntimeException("Exception while finding unique product ion combination.", e);
                } catch (ExecutionException e) {
                    throw new RuntimeException("Exception while finding unique product ion combination.", e);
                }

            }

        }


        return retVal;
    }
}
