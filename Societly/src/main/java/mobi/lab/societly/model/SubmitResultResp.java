package mobi.lab.societly.model;

public class SubmitResultResp extends BaseResp {

    private String clientHash;

    public SubmitResultResp(String clientHash) {
        super(null);
        this.clientHash = clientHash;
    }

    public SubmitResultResp(Error error) {
        super(error);
    }

    public String getClientHash() {
        return clientHash;
    }
}
