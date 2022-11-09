package servlets;

import exceptions.ConnectingDbException;
import exceptions.LoadingDbException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import listener.InitListener;
import model.User;
import services.AuthService;
import services.UserAchievementService;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private AuthService service;
    private UserAchievementService userAchievementService;

    @Override
    public void init() throws ServletException {
        super.init();
        service = (AuthService) getServletContext().getAttribute(InitListener.KEY_AUTH_SERVICE);
        userAchievementService = (UserAchievementService) getServletContext().getAttribute(InitListener.KEY_USER_ACHIEVEMENT_SERVICE);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        getServletContext().getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        try {
            Optional<User> user = service.findUser(username);
            if (user.isEmpty()) {
                resp.getWriter().println("There is no user with this username");
            } else {
                if (service.login(username, password)) {
                    service.auth(user.get(), req);
                    userAchievementService.unlockAchievement(user.get().getId(), 1L);
                    resp.setContentType("text/html;charset=UTF-8");
                    resp.sendRedirect(req.getContextPath() + "/home");
//                    req.getRequestDispatcher("/WEB-INF/jsp/main_page.jsp").forward(req, resp);
                } else {
                    resp.getWriter().println("Wrong password");
                }
            }
        } catch (ConnectingDbException | LoadingDbException e) {
            e.printStackTrace();
        }
    }
}
