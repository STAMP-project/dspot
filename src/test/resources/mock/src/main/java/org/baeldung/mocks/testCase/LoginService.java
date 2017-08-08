package org.baeldung.mocks.testCase;

public class LoginService {

    private org.baeldung.mocks.testCase.LoginDao loginDao;

    private String currentUser;

    public boolean login(org.baeldung.mocks.testCase.UserForm userForm) {
        assert null != userForm;

        int loginResults = loginDao.login(userForm);

        switch (loginResults) {
            case 1:
                return true;
            default:
                return false;
        }
    }

    public void setCurrentUser(String username) {
        if (null != username) {
            this.currentUser = username;
        }
    }

    public void setLoginDao(org.baeldung.mocks.testCase.LoginDao loginDao) {
        this.loginDao = loginDao;
    }

    // standard setters and getters
}
