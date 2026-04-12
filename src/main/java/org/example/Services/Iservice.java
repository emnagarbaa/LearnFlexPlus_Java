package org.example.Services;
import org.example.entities.Challenge;
import org.example.entities.Examen;
import org.example.entities.Commentaire;
import java.sql.SQLException;
import java.util.List;

public interface Iservice<T> {
    void ajouter(T t) throws SQLException;
    void supprimer(T t) throws SQLException;

    void modifier(T t) throws SQLException;
    List<T> recuperer () throws SQLException;
}
