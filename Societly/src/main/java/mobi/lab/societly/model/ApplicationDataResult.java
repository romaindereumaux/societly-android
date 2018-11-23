package mobi.lab.societly.model;


public class ApplicationDataResult extends BaseResp {

    private AppState state;

    public ApplicationDataResult(AppState state) {
        super(null);
        this.state = state;
    }

    public ApplicationDataResult(Error error) {
        super(error);
        this.state = AppState.ERROR;
    }

    public AppState getState() {
        return state;
    }

    @Override
    public String toString() {
        return "ApplicationDataResult{" +
                "state='" + state + '\'' +
                ",error='" + getError() + '\'' +
                '}';
    }
}
