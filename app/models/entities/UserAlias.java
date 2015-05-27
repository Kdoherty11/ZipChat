package models.entities;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.hibernate.annotations.GenericGenerator;
import play.data.validation.Constraints;
import play.db.jpa.JPA;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "aliases")
public class UserAlias {

    @Id
    @GenericGenerator(name = "user_alias_gen", strategy = "sequence", parameters = {
            @org.hibernate.annotations.Parameter(name = "sequenceName", value = "user_alias_gen"),
            @org.hibernate.annotations.Parameter(name = "allocationSize", value = "1"),
    })
    @GeneratedValue(generator = "user_alias_gen", strategy = GenerationType.SEQUENCE)
    public long userAliasId;

    @Constraints.Required
    public long userId;

    @Constraints.Required
    public long roomId;

    @Constraints.Required
    public String alias;

    private static final Set<String> FIRST_NAMES = ImmutableSet.of("John", "Drew", "Carl", "Trevor", "Rahul", "Jim", "Tom", "Dave", "Matt", "Eric");
    private static final Set<String> LAST_NAMES = ImmutableSet.of("Smith", "Brown", "Doe", "Forgo", "Cogan", "Dexter", "Matthews", "Jordan");
    private static final Set<String> FULL_NAMES = new HashSet<>(FIRST_NAMES.size() * LAST_NAMES.size());

    static {
        for (String firstName : FIRST_NAMES) {
            for (String lastName : LAST_NAMES) {
                FULL_NAMES.add(firstName + " " + lastName);
            }
        }
    }

    public UserAlias(long userId, long roomId) {
        this.userId = userId;
        this.roomId = roomId;

        Random random = new Random();
        this.alias = getAvailableAlias(roomId);
    }

    public static Set<String> aliasesByRoom(long roomId) {
        String queryString = "select a.alias from UserAlias a where a.roomId = :roomId";

        TypedQuery<String> query = JPA.em().createQuery(queryString, String.class)
                .setParameter("roomId", roomId);

        return new HashSet<>(query.getResultList());
    }

    public static String getAvailableAlias(long roomId) {
        Set<String> usedAliases = aliasesByRoom(roomId);

        Set<String> availableAliases = Sets.difference(FULL_NAMES, new HashSet<>(usedAliases));

        if (availableAliases.isEmpty()) {
            throw new IllegalStateException("There are no more available aliases for room " + roomId);
        }

        int item = new Random().nextInt(availableAliases.size());
        int i = 0;
        for (String alias : availableAliases) {
            if (i++ == item) {
                return alias;
            }
        }

        throw new RuntimeException("Random index was not in set");
    }

    public static String getOrCreateAlias(long userId, long roomId) {
        Optional<String> aliasOptional = getAlias(userId, roomId);

        if (aliasOptional.isPresent()) {
            return aliasOptional.get();
        }

        UserAlias alias = createAlias(userId, roomId);
        return alias.alias;
    }

    public static UserAlias createAlias(long userId, long roomId) {
        UserAlias alias = new UserAlias(userId, roomId);
        JPA.em().persist(alias);
        return alias;
    }

    public static Optional<String> getAlias(long userId, long roomId) {
        String queryString = "select a.alias from UserAlias a where a.userId = :userId and a.roomId = :roomId";

        TypedQuery<String> query = JPA.em().createQuery(queryString, String.class)
                .setParameter("userId", userId)
                .setParameter("roomId", roomId);

        List<String> aliases = query.getResultList();
        if (aliases.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(aliases.get(0));
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("userAliasId", userAliasId)
                .add("userId", userId)
                .add("roomId", roomId)
                .add("alias", alias)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAlias userAlias = (UserAlias) o;
        return Objects.equal(userAliasId, userAlias.userAliasId) &&
                Objects.equal(userId, userAlias.userId) &&
                Objects.equal(roomId, userAlias.roomId) &&
                Objects.equal(alias, userAlias.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userAliasId, userId, roomId, alias);
    }
}
