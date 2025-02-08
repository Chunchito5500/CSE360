package application;

public interface LoginCallback {
	void onLoginSuccess(User user);

	void onLoginFailure(String errorMessage);
}
