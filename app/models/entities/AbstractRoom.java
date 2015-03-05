package models.entities;


import javax.persistence.*;

@Entity
@Inheritance(strategy= InheritanceType.TABLE_PER_CLASS)
public class AbstractRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    public long roomId;

}
