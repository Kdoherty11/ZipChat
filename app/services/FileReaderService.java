package services;

import com.google.common.collect.ImmutableSet;
import com.google.inject.ImplementedBy;
import services.impl.FileReaderServiceImpl;

/**
 * Created by kdoherty on 8/10/15.
 */
@ImplementedBy(FileReaderServiceImpl.class)
public interface FileReaderService {
    ImmutableSet<String> readToImmutableSet(String fileName);
}
