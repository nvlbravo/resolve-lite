package edu.clemson.resolve.proving.absyn;

public abstract class PExpVisitor {

    public void beginPExp(PExp p) {}

    public void beginPSymbol(PSymbol p) {}

    public void beginPAlternatives(PAlternatives p) {}

    public void beginPSet(PSet p) {}

    public void beginPLambda(PLambda p) {}

    public void beginChildren(PExp p) {}

    public void fencepostPSymbol(PSymbol p) {}

    public void fencepostPAlternatives(PAlternatives p) {}

    public void endChildren(PExp p) {}

    public void endPExp(PExp p) {}

    public void endPSymbol(PSymbol p) {}

    public void endPAlternatives(PAlternatives p) {}

    public void endPLambda(PLambda p) {}

    public void endPSet(PSet p) {}

}