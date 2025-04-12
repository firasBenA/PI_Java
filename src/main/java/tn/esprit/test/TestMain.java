package tn.esprit.test;

import tn.esprit.models.Article;
import tn.esprit.models.Evenement;
import tn.esprit.services.ServiceArticle;
import tn.esprit.services.ServiceEvenement;

import java.time.LocalDate;

public class TestMain {

    public static void main(String[] args) {
        // Initialize services
        ServiceArticle serviceArticle = new ServiceArticle();
        ServiceEvenement serviceEvenement = new ServiceEvenement();

        // Test Article CRUD
        System.out.println("===== TESTING ARTICLE CRUD =====");

        // Create test
        Article article = new Article(0, "Test Article", "TestContenueArtic", "test-image.jpg");
        serviceArticle.add(article);
        System.out.println("Article avec succes");

        // Read test
        System.out.println("\nRetrieving all articles:");
        System.out.println(serviceArticle.getAll());

        // Test Evenement CRUD
        System.out.println("\n===== TESTING EVENEMENT CRUD =====");

        // Create test
        Evenement evenement = new Evenement(
                0,
                "Test Event",
                "TestContenueEven",
                "Conference",
                "soon",
                "Test Location",
                LocalDate.now()
        );
        serviceEvenement.add(evenement);
        System.out.println("Evenement avec succes");

        // Read test
        System.out.println("\nRetrieving all events:");
        System.out.println(serviceEvenement.getAll());

        // Uncomment these sections one by one as needed for testing

        /*
        // Update article test
        // First get an article ID from your database after running the add test
        Article articleToUpdate = new Article();
        articleToUpdate.setId(1); // Replace with actual ID from your database
        articleToUpdate.setTitre("Updated Article Title");
        articleToUpdate.setContenue("Updated content");
        articleToUpdate.setImage("updated-image.jpg");

        serviceArticle.update(articleToUpdate);
        System.out.println("\nArticle updated successfully");
        System.out.println(serviceArticle.getAll());
        */

        /*
        // Update event test
        // First get an event ID from your database after running the add test
        Evenement eventToUpdate = new Evenement();
        eventToUpdate.setId(1); // Replace with actual ID from your database
        eventToUpdate.setNom("Updated Event Name");
        eventToUpdate.setContenue("Updated event content");
        eventToUpdate.setType("Workshop");
        eventToUpdate.setStatut("In Progress");
        eventToUpdate.setLieuxEvent("Updated Location");
        eventToUpdate.setDateEvent(LocalDate.now().plusDays(7));

        serviceEvenement.update(eventToUpdate);
        System.out.println("\nEvent updated successfully");
        System.out.println(serviceEvenement.getAll());
        */

        /*
        // Delete article test
        Article articleToDelete = new Article();
        articleToDelete.setId(1); // Replace with actual ID from your database

        serviceArticle.delete(articleToDelete);
        System.out.println("\nArticle deleted successfully");
        System.out.println(serviceArticle.getAll());
        */

        /*
        // Delete event test
        Evenement eventToDelete = new Evenement();
        eventToDelete.setId(1); // Replace with actual ID from your database

        serviceEvenement.delete(eventToDelete);
        System.out.println("\nEvent deleted successfully");
        System.out.println(serviceEvenement.getAll());
        */
    }
}