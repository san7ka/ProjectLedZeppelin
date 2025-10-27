package com.javarush.lepeshinskij.controller;

import com.javarush.lepeshinskij.entity.User;
import com.javarush.lepeshinskij.service.UserService;
import org.hibernate.SessionFactory;
import com.javarush.lepeshinskij.util.SessionUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class AuthController extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private UserService userService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            SessionFactory sessionFactory = (SessionFactory) getServletContext().getAttribute("sessionFactory");
            userService = new UserService(sessionFactory);
            logger.info("AuthController инициализирован успешно");
        } catch (Exception e) {
            logger.error("Ошибка инициализации AuthController", e);
            throw new ServletException("Не удалось инициализировать AuthController", e);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "/";
        }
        
        try {
            switch (pathInfo) {
                case "/login":
                    showLoginPage(request, response);
                    break;
                case "/register":
                    showRegisterPage(request, response);
                    break;
                case "/logout":
                    processLogout(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    break;
            }
        } catch (Exception e) {
            logger.error("Ошибка в doGet", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "/";
        }
        
        try {
            switch (pathInfo) {
                case "/login":
                    processLogin(request, response);
                    break;
                case "/register":
                    processRegister(request, response);
                    break;
                case "/logout":
                    processLogout(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    break;
            }
        } catch (Exception e) {
            logger.error("Ошибка в doPost", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
        }
    }
    
    private void showLoginPage(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String error = request.getParameter("error");
        if (error != null) {
            request.setAttribute("error", "Неверное имя пользователя/email или пароль");
        }
        
        request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
    }
    
    private void showRegisterPage(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
    }
    
    private void processLogin(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String usernameOrEmail = request.getParameter("username");
            String password = request.getParameter("password");
            
            if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
                request.setAttribute("error", "Имя пользователя/email не может быть пустым");
                request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
                return;
            }
            
            if (password == null || password.isEmpty()) {
                request.setAttribute("error", "Пароль не может быть пустым");
                request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
                return;
            }
            
            Optional<User> userOpt = userService.authenticate(usernameOrEmail.trim(), password);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                    SessionUtils.setUserInSession(request, user);
                
                logger.info("Пользователь {} успешно вошел в систему (роль: {})", user.getUsername(), user.getRole());
                
                response.sendRedirect(request.getContextPath() + "/welcome");
            } else {
                request.setAttribute("error", "Неверное имя пользователя/email или пароль");
                request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при входе", e);
            request.setAttribute("error", "Произошла ошибка при входе. Попробуйте еще раз.");
            request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
        }
    }
    
    private void processRegister(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String username = request.getParameter("username");
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            String confirmPassword = request.getParameter("confirmPassword");
            
            if (username == null || username.trim().isEmpty()) {
                request.setAttribute("error", "Имя пользователя не может быть пустым");
                request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
                return;
            }
            
            if (email == null || email.trim().isEmpty()) {
                request.setAttribute("error", "Email не может быть пустым");
                request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
                return;
            }
            
            if (password == null || password.isEmpty()) {
                request.setAttribute("error", "Пароль не может быть пустым");
                request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                request.setAttribute("error", "Пароли не совпадают");
                request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
                return;
            }
            
            if (password.length() < 6) {
                request.setAttribute("error", "Пароль должен содержать минимум 6 символов");
                request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
                return;
            }
            
            User user = userService.createUser(username.trim(), email.trim(), password);
            
            logger.info("Новый пользователь зарегистрирован: {}", user.getUsername());
            
            request.setAttribute("success", "Регистрация прошла успешно! Теперь вы можете войти в систему.");
            request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
            
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
        } catch (Exception e) {
            logger.error("Ошибка при регистрации", e);
            request.setAttribute("error", "Произошла ошибка при регистрации. Попробуйте еще раз.");
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
        }
    }
    
    private void processLogout(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = SessionUtils.getCurrentUser(request);
            if (user != null) {
                logger.info("Пользователь {} вышел из системы", user.getUsername());
            }
            session.invalidate();
        }
        
        response.sendRedirect(request.getContextPath() + "/welcome?logout=true");
    }
    
    @Override
    public void destroy() {
        super.destroy();
        logger.info("AuthController уничтожен");
    }
}
