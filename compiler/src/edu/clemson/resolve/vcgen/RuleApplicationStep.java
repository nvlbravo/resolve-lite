package edu.clemson.resolve.vcgen;

import edu.clemson.resolve.codegen.Model.OutputModelObject;
import edu.clemson.resolve.vcgen.AssertiveBlock;

public class RuleApplicationStep {

    private final String step, description;

    public RuleApplicationStep(String step, String description) {
        this.step = step;
        this.description = description;
    }

    @Override
    public String toString() {
        return description + ":\n" + step;
    }
}
