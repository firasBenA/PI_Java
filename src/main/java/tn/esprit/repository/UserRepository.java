package tn.esprit.repository;

import tn.esprit.models.User;

import java.util.List;

public interface  UserRepository {
    User save(User user);
    User findByEmail(String email);
    List<User> findAll();
    void delete(int userId);
    User findById(int userId);

    List<User> searchUsers(String keyword);
}
