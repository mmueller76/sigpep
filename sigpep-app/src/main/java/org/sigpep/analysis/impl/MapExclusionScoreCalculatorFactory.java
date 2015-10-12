package org.sigpep.analysis.impl;

import org.sigpep.analysis.ExclusionScoreCalculator;
import org.sigpep.analysis.ExclusionScoreCalculatorFactory;
import org.sigpep.model.ProductIon;
import org.sigpep.model.Peptide;

import java.util.Set;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.<br/>
 * User: mmueller<br/>
 * Date: 05-Aug-2008<br/>
 * Time: 12:02:14<br/>
 */
public class MapExclusionScoreCalculatorFactory extends ExclusionScoreCalculatorFactory {

    public ExclusionScoreCalculator getCalculator(Set<ProductIon> productIonCombination, Map<ProductIon, Map<Peptide, Integer>> exclusionMatrix) {
        return new MapExclusionScoreCalculator(productIonCombination, exclusionMatrix);
    }

}
