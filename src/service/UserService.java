package service;

import dao.UserDAO;
import model.User;

public class UserService {
    private UserDAO userDAO = new UserDAO();

    public boolean register(String name, String email, String password, String phone) {
        if (userDAO.findByEmail(email) != null) return false;
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setPhone(phone);
        userDAO.insert(user);
        return true;
    }

    public User login(String email, String password) {
        User user = userDAO.findByEmail(email);
        if (user == null || !user.getPassword().equals(password)) return null;
        return user;
    }

    public void updateProfile(User user) {
        userDAO.update(user);
    }
}
