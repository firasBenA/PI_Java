package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Personne;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePersonne implements IService<Personne> {
    private Connection cnx  ;

    public ServicePersonne(){
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Personne personne) {
        //todo's
        //creation de la req done
        //executer notre req
        String qry ="INSERT INTO `personne`( `nom`, `prenom`, `age`) VALUES (?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1,personne.getNom());
            pstm.setString(2,personne.getPrenom());
            pstm.setInt(3,personne.getAge());


            pstm.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }


    }

    @Override
    public List<Personne> getAll() {
       //todo's
        //creation req
        //execution req
        // matching entre le resultat et list Personne
        List<Personne> personnes = new ArrayList<>();

        String qry ="SELECT * FROM `personne`";
        try {
            Statement stm = cnx.createStatement();
         ResultSet rs = stm.executeQuery(qry);

         while(rs.next()){
             Personne p =new Personne();
             p.setId(rs.getInt(1));
             p.setNom(rs.getString("nom"));
             p.setPrenom(rs.getString(3));
             p.setAge(rs.getInt("age"));

             personnes.add(p);
         }


        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }


        return personnes;
    }

    @Override
    public void update(Personne personne) {

    }

    @Override
    public void delete(Personne personne) {

    }
}
