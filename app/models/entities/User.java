package models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User extends AbstractUser {

    public User() { }

    @JsonIgnore
    @OneToMany(targetEntity = Device.class, mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Device> devices = new ArrayList<>();

    @Override
    public boolean isAnon() {
        return false;
    }

    @Override
    public User getActual() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        User user = (User) o;
        return Objects.equal(devices, user.devices);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), devices);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("userId", userId)
                .add("facebookId", facebookId)
                .add("gender", gender)
                .add("name", name)
                .add("devices", devices)
                .add("createdAt", createdAt)
                .toString();
    }

}
