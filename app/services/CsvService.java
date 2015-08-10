package services;

import com.google.common.collect.ImmutableSet;
import com.google.inject.ImplementedBy;
import services.impl.CsvServiceImpl;

/**
 * Created by kdoherty on 8/10/15.
 */
@ImplementedBy(CsvServiceImpl.class)
public interface CsvService {
    ImmutableSet<String> readToImmutableSet(String fileName);
}
