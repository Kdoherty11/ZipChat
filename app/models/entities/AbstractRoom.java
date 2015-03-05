package models.entities;


import javax.persistence.*;

@Entity
@Inheritance(strategy= InheritanceType.TABLE_PER_CLASS)
public class AbstractRoom {

    @Id
    @SequenceGenerator(name="room_id_seq",
            sequenceName="room_id_seq",
            allocationSize=1)
    @GeneratedValue(strategy = GenerationType.IDENTITY,
            generator="room_id_seq")
    public long roomId;

}
