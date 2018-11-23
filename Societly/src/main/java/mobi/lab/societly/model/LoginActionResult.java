package mobi.lab.societly.model;


import mobi.lab.societly.dto.Customer;
import mobi.lab.societly.fragment.LoginFragment;

public class LoginActionResult extends BaseResp {

    private Customer customer;
    private LoginFragment.Action action;
    private AppState appDataState;

    public LoginActionResult(LoginFragment.Action action, Customer customer, AppState dataState) {
        super(null);
        this.customer = customer;
        this.action = action;
        this.appDataState = dataState;
    }

    public LoginActionResult(LoginFragment.Action action, Error error) {
        super(error);
        this.action = action;
        this.customer = null;
    }

    public Customer getCustomer() {
        return customer;
    }

    public LoginFragment.Action getAction() {
        return action;
    }

    public AppState getAppDataState() {
        return appDataState;
    }

    @Override
    public String toString() {
        return "LoginActionResult{" +
                "customer=" + customer +
                ", action=" + action +
                ", appDataState=" + appDataState +
                '}';
    }
}
