package org.recap.converter;

import org.marc4j.marc.Record;

import java.util.Map;

/**
 * Created by premkb on 15/12/16.
 */
public interface XmlToBibEntityConverterInterface {

    public Map convert(Object record, String institutionName);
}
