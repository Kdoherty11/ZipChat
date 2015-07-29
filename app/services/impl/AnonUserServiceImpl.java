package services.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import daos.AnonUserDao;
import models.entities.AnonUser;
import models.entities.PublicRoom;
import models.entities.User;
import services.AnonUserService;

import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by kdoherty on 7/4/15.
 */
public class AnonUserServiceImpl extends GenericServiceImpl<AnonUser> implements AnonUserService {

    private static final Set<String> FIRST_NAMES = ImmutableSet.of("John", "Drew", "Carl", "Trevor", "Rahul", "Jim", "Tom", "Dave", "Matt", "Eric");
    private static final Set<String> LAST_NAMES = ImmutableSet.of("Smith", "Brown", "Doe", "Forgo", "Cogan", "Dexter", "Matthews", "Jordan");
    private static final Set<String> FULL_NAMES = new HashSet<>(FIRST_NAMES.size() * LAST_NAMES.size());

    static {
        FIRST_NAMES.forEach(firstName -> LAST_NAMES.forEach(lastName -> FULL_NAMES.add(firstName + " " + lastName)));
    }

    private final AnonUserDao anonUserDao;
    private final Random random;

    @Inject
    public AnonUserServiceImpl(final AnonUserDao anonUserDao, final Random random) {
        super(anonUserDao);
        this.anonUserDao = anonUserDao;
        this.random = random;
    }

    @Override
    public AnonUser getOrCreateAnonUser(User actual, PublicRoom room) {
        Optional<AnonUser> aliasOptional = getAnonUser(actual, room);

        if (aliasOptional.isPresent()) {
            return aliasOptional.get();
        }

        return createAnonUser(actual, room);
    }

    @Override
    public Optional<AnonUser> getAnonUser(User actual, PublicRoom room) {
        return anonUserDao.getAnonUser(actual, room);
    }

    private AnonUser createAnonUser(User actual, PublicRoom room) {
        AnonUser anonUser = new AnonUser(actual, room, getAvailableAlias(room));
        anonUserDao.save(anonUser);
        return anonUser;
    }

    private String getAvailableAlias(PublicRoom room) {
        Set<String> usedAliases = room.anonUsers.stream().map(anonUser -> anonUser.name).collect(Collectors.toSet());
        Set<String> availableAliases = Sets.difference(FULL_NAMES, new HashSet<>(usedAliases));

        if (availableAliases.isEmpty()) {
            throw new IllegalStateException("There are no more available aliases for room " + room.roomId);
        }

        // Pick a random alias from the set
        int randomIndex = random.nextInt(availableAliases.size());
        int i = 0;
        for (String alias : availableAliases) {
            if (i++ == randomIndex) {
                return alias;
            }
        }

        throw new RuntimeException("Random index was not in set... Should never get here");
    }
}
