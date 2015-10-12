package org.sigpep.model.impl;

import org.sigpep.model.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.<br/>
 * User: mmueller<br/>
 * Date: 07-Mar-2008<br/>
 * Time: 15:14:38<br/>
 */
public class TransitionImpl implements Transition {

    private Peptide peptide;
    private List<ProductIon> productIons;

    public TransitionImpl(Peptide peptide) {
        this.peptide = peptide;
        this.productIons = new ArrayList<ProductIon>();
    }

    public TransitionImpl(List<ProductIon> productIons) {

        if(productIons.size() == 0){
            throw new IllegalArgumentException("The collection of product ions defining the transition has to contain at least one precursor.");                              
        }

        productIonSanityCheck(productIons);

        this.productIons = productIons;
        this.peptide = productIons.iterator().next().getPrecursorIon().getPeptide();
    }

    public List<ProductIon> getProductIons() {
        return productIons;
    }

    public void setProductIons(List<ProductIon> productIons) {
        productIonSanityCheck(productIons);
        this.productIons = productIons;
    }

    public Peptide getPeptide() {
        return peptide;
    }

    public void addProductIon(ProductIonType type, int length){
        ProductIon  pi = peptide.getPrecursorIon().getProductIon(type, length);
        this.productIons.add(pi);
    }

    private void productIonSanityCheck(Collection<ProductIon> productIons) throws IllegalArgumentException {

        //check that all product ions come from the same precursor
        PrecursorIon previousPrecursor = null;
        for(ProductIon product : productIons){
            PrecursorIon precursor = product.getPrecursorIon();
            if(precursor == null){
                throw new IllegalArgumentException("The precursor ion of one or more product ion(s) is NULL.");
            }
            if(previousPrecursor != null && !precursor.equals(previousPrecursor)){
                throw new IllegalArgumentException("All product ions defining the transition have to come from the same precursor.");
            }
            previousPrecursor = precursor;
        }

    }

    

    public String toString() {
        return "TransitionImpl{" +
                "productIons=" + productIons +
                '}';
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transition)) return false;

        Transition that = (Transition) o;

        if (!productIons.equals(that.getProductIons())) return false;

        return true;
    }

    public int hashCode() {
        return productIons.hashCode();
    }


}
