package models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import notifications.AbstractNotification;
import play.data.validation.Constraints;
import play.db.jpa.JPA;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by kevin on 6/10/15.
 */
@Entity
@Table(name = "anon_users")
public class AnonUser extends AbstractUser {

    private static final Set<String> FIRST_NAMES = ImmutableSet.of("John", "Drew", "Carl", "Trevor", "Rahul", "Jim", "Tom", "Dave", "Matt", "Eric");
    private static final Set<String> LAST_NAMES = ImmutableSet.of("Smith", "Brown", "Doe", "Forgo", "Cogan", "Dexter", "Matthews", "Jordan");
    private static final Set<String> FULL_NAMES = new HashSet<>(FIRST_NAMES.size() * LAST_NAMES.size());

    static {
        FIRST_NAMES.forEach(firstName -> LAST_NAMES.forEach(lastName -> FULL_NAMES.add(firstName + " " + lastName)));
    }

    @ManyToOne
    @JoinColumn(name = "actualUserId")
    @JsonIgnore
    @Constraints.Required
    public User actual;

    @ManyToOne
    @JoinColumn(name = "roomId")
    @JsonIgnore
    @Constraints.Required
    public PublicRoom room;

    public AnonUser() {
        // Needed for JPA
    }

    private AnonUser(User actual, PublicRoom room) {
        this.actual = actual;
        this.room = room;
        this.name = getAvailableAlias(room);
    }

    @Override
    public boolean isAnon() {
        return true;
    }

    @Override
    public User getActual() {
        return actual;
    }

    public String getName() {
        return name;
    }

    private static String getAvailableAlias(PublicRoom room) {
        Set<String> usedAliases = room.anonUsers.stream().map(AnonUser::getName).collect(Collectors.toSet());
        Set<String> availableAliases = Sets.difference(FULL_NAMES, new HashSet<>(usedAliases));

        if (availableAliases.isEmpty()) {
            throw new IllegalStateException("There are no more available aliases for room " + room.roomId);
        }

        // Pick a random alias from the set
        int item = new Random().nextInt(availableAliases.size());
        int i = 0;
        for (String alias : availableAliases) {
            if (i++ == item) {
                return alias;
            }
        }

        throw new RuntimeException("Random index was not in set");
    }

    public static AnonUser getOrCreateAnonUser(User actual, PublicRoom room) {
        Optional<AnonUser> aliasOptional = getAnonUser(actual, room);

        if (aliasOptional.isPresent()) {
            return aliasOptional.get();
        }

        return createAnonUser(actual, room);
    }

    private static AnonUser createAnonUser(User actual, PublicRoom room) {
        AnonUser anonUser = new AnonUser(actual, room);
        JPA.em().persist(anonUser);
        return anonUser;
    }

    private static Optional<AnonUser> getAnonUser(User actual, PublicRoom room) {
        String queryString = "select a from AnonUser a where a.actual.userId = :userId and a.room.roomId = :roomId";

        TypedQuery<AnonUser> query = JPA.em().createQuery(queryString, AnonUser.class)
                .setParameter("userId", actual.userId)
                .setParameter("roomId", room.roomId);

        List<AnonUser> aliases = query.getResultList();
        if (aliases.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(aliases.get(0));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AnonUser anonUser = (AnonUser) o;
        return Objects.equal(actual, anonUser.actual) &&
                Objects.equal(room, anonUser.room);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), actual, room);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("name", super.name)
                .add("actualId", User.getId(actual))
                .add("roomId", PublicRoom.getId(room))
                .toString();
    }
}
