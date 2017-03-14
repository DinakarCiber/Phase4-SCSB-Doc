package org.recap.converter;

import java.util.Map;

/**
 * Created by premkb on 15/12/16.
 */
@FunctionalInterface
public interface XmlToBibEntityConverterInterface {

    public Map convert(Object record, String institutionName, String customerCode);
}
