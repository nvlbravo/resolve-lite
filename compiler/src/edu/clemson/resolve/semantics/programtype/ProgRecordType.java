package edu.clemson.resolve.semantics.programtype;

import org.jetbrains.annotations.NotNull;
import edu.clemson.resolve.semantics.DumbMathClssftnHandler;
import edu.clemson.resolve.semantics.MathCartesianClssftn;
import edu.clemson.resolve.semantics.MathClssftn;
import edu.clemson.resolve.semantics.symbol.FacilitySymbol;

import java.util.*;

public class ProgRecordType extends ProgType {

    @NotNull
    private final Map<String, ProgType> fields = new HashMap<>();
    @NotNull
    private MathClssftn mathTypeAlterEgo;

    public ProgRecordType(@NotNull DumbMathClssftnHandler g, @NotNull Map<String, ProgType> types) {
        super(g);
        this.fields.putAll(types);
        List<MathCartesianClssftn.Element> eles = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, ProgType> field : types.entrySet()) {
            eles.add(new MathCartesianClssftn.Element(field.getKey(),
                    field.getValue().toMath()));
        }
        this.mathTypeAlterEgo = new MathCartesianClssftn(g, eles);
    }

    @NotNull
    public ProgType getFieldType(@NotNull String name)
            throws NoSuchElementException {
        ProgType result = fields.get(name);
        if (result == null) {
            throw new NoSuchElementException();
        }
        return result;
    }

    @Override
    public boolean isAggregateType() {
        return true;
    }

    @NotNull
    @Override
    public MathClssftn toMath() {
        return mathTypeAlterEgo;
    }

    @Override
    public String toString() {
        return "record " + fields;
    }

    @NotNull
    @Override
    public ProgType instantiateGenerics(@NotNull Map<String, ProgType> genericInstantiations,
                                        @NotNull FacilitySymbol instantiatingFacility) {

        Map<String, ProgType> newFields = new HashMap<>();
        for (Map.Entry<String, ProgType> type : fields.entrySet()) {
            newFields.put(
                    type.getKey(),
                    type.getValue().instantiateGenerics(genericInstantiations, instantiatingFacility));
        }
        return new ProgRecordType(getTypeGraph(), newFields);
    }
}
