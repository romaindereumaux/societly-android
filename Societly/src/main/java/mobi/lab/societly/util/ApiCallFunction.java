package mobi.lab.societly.util;

import java.io.IOException;

/**
 * Created by lauris on 28/10/2016.
 */

public interface ApiCallFunction<T> {

    T call() throws IOException;

}
