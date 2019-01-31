package fr.inria.lombok;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 06/12/18
 *
 * This class is a copy from catalog of Activeeon.
 * See org.ow2.proactive.catalog.service.model.AuthenticatedUser
 *
 * @author ActiveEon Team
 * @since 27/07/2017
 */
@Builder
@Data
public class LombokClassThatUseBuilder {

    public final static LombokClassThatUseBuilder EMPTY = LombokClassThatUseBuilder.builder().build();

    private String name;

    private List<String> groups;

}
