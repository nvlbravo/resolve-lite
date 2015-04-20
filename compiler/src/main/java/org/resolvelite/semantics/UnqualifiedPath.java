package org.resolvelite.semantics;

import org.resolvelite.semantics.programtype.PTType;
import org.resolvelite.semantics.searchers.TableSearcher;
import org.resolvelite.semantics.searchers.TableSearcher.SearchContext;
import org.resolvelite.semantics.symbol.FacilitySymbol;
import org.resolvelite.semantics.symbol.Symbol;
import org.resolvelite.semantics.SymbolTable.FacilityStrategy;
import org.resolvelite.semantics.SymbolTable.ImportStrategy;

import java.util.*;

/**
 * Defines the search path used when a symbol is referenced in an
 * unqualified way, along with some parameters for tweaking how the search is
 * accomplished. In general, the path is as follows:
 * 
 * <ol>
 * <li>Search the local scope.</li>
 * <li>Search any facilities declared in the local scope.</li>
 * <li>Search any imports in a depth-first manner, skipping any already-searched
 * scopes.</li>
 * <ul>
 * <li>For each searched import, search any facilities declared inside.</li>
 * </ul>
 * </ol>
 * 
 * Instance of this class can be parameterized to search only direct imports or
 * to exclude all imports, as well as to exclude searching facilities, or change
 * how generics are handled when searching facilities.
 * 
 * Additionally, by setting the {@code localPriority} flag, the search can be
 * made to stop without considering imports (regardless of the import strategy)
 * if at least one local match is found. Note that any local facilities will
 * still be searched if the facility strategy requires it.
 */
public class UnqualifiedPath implements ScopeSearchPath {

    private final ImportStrategy importStrategy;
    private final FacilityStrategy facilityStrategy;
    private final boolean localPriority;

    public UnqualifiedPath(ImportStrategy imports, FacilityStrategy facilities,
            boolean localPriority) {
        this.importStrategy = imports;
        this.facilityStrategy = facilities;
        this.localPriority = localPriority;
    }

    @Override public <E extends Symbol> List<E> searchFromContext(
            TableSearcher<E> searcher, Scope source, SymbolTable repo)
            throws DuplicateSymbolException {

        List<E> result = new ArrayList<>();
        Set<Scope> searchedScopes = new HashSet<>();
        Map<String, PTType> genericInstantiations = new HashMap<>();

        searchModule(searcher, source, repo, result, searchedScopes,
                genericInstantiations, null, importStrategy, 0);

        return result;
    }

    private <E extends Symbol> boolean searchModule(TableSearcher<E> searcher,
            Scope source, SymbolTable repo, List<E> results,
            Set<Scope> searchedScopes,
            Map<String, PTType> genericInstantiations,
            FacilitySymbol instantiatingFacility,
            ImportStrategy importStrategy, int depth)
            throws DuplicateSymbolException {

        //First we search locally
        boolean finished =
                source.addMatches(searcher, results, searchedScopes,
                        genericInstantiations, instantiatingFacility,
                        SearchContext.SOURCE_MODULE);

        //Next, if requested, we search any local facilities.
        if ( !finished && facilityStrategy != FacilityStrategy.FACILITY_IGNORE ) {
            throw new UnsupportedOperationException(
                    "searching for unqualified symbols using 'facility generic' or 'facility instantiate' "
                            + "is not currently permitted by the compiler. It"
                            + "could be easily added, though for code generation reasons"
                            + "we currently are disallowing this; thus,"
                            + "resolve programmers must  explicitly qualify any "
                            + "facility-bound symbols");
            // finished =
            //         searchFacilities(searcher, results, source,
            //                 genericInstantiations, searchedScopes, repo);
        }

        //Finally, if requested, we search imports
        if ( (results.isEmpty() || !localPriority)
                && source instanceof SyntacticScope
                && importStrategy != ImportStrategy.IMPORT_NONE ) {

            SyntacticScope sourceAsSyntacticScope = (SyntacticScope) source;
            try {
                ModuleScopeBuilder module =
                        repo.getModuleScope(sourceAsSyntacticScope
                                .getModuleID());
                List<String> imports = module.getImports();

                for (String s : module.getImports()) {
                    finished =
                            searchModule(searcher, repo.getModuleScope(s),
                                    repo, results, searchedScopes,
                                    genericInstantiations,
                                    instantiatingFacility,
                                    importStrategy.cascadingStrategy(),
                                    depth + 1);
                    if ( finished ) break;
                }
            }
            catch (NoSuchSymbolException nsse) {
                //This shouldn't be possible--we'd've caught it by now
                throw new RuntimeException(nsse);
            }
        }
        return finished;
    }
}
