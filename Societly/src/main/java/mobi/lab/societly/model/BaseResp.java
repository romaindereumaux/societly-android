package mobi.lab.societly.model;

import java.io.Serializable;

public class BaseResp implements Serializable {

    private Error error;

    public BaseResp(Error error) {
        this.error = error;
    }

    public Error getError() {
        return error;
    }

    public boolean isSuccess() {
        return error == null;
    }

    @Override
    public String toString() {
        String errorString = error == null ? "null" : error.toString();
        return "BaseResp{" +
                "error=" + errorString +
                '}';
    }
}
