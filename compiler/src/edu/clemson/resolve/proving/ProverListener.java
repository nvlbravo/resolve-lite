package edu.clemson.resolve.proving;

public interface ProverListener {

    public void progressUpdate(double progess);

    public void vcResult(boolean proved, PerVCProverModel finalModel, Metrics m);

    //isCancelled should return true to stop the prover
    public boolean isCancelled();
}
