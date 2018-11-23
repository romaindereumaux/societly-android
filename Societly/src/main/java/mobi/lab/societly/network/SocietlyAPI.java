package mobi.lab.societly.network;

import java.util.List;

import mobi.lab.societly.dto.Candidate;
import mobi.lab.societly.dto.Customer;
import mobi.lab.societly.dto.District;
import mobi.lab.societly.dto.LoginData;
import mobi.lab.societly.dto.Question;
import mobi.lab.societly.dto.State;
import mobi.lab.societly.dto.Submission;
import mobi.lab.societly.dto.SubmissionsItem;
import mobi.lab.societly.dto.SubmitData;
import mobi.lab.societly.dto.SubmitResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SocietlyAPI {

    @GET("questions?per_page=250")
    Call<List<Question>> getQuestions();

    @GET("candidates?per_page=250")
    Call<List<Candidate>> getCandidates(@Query("level") String level);

    @GET("states?per_page=250")
    Call<List<State>> getStates();

    @GET("districts?per_page=250")
    Call<List<District>> getDistricts();

    @GET("customers/current/submissions")
    Call<List<SubmissionsItem>> getCustomerSubmissions();

    @GET("submissions/{clientHash}")
    Call<Submission> getSubmission(@Path("clientHash") String clientHash);

    @POST("submit")
    Call<SubmitResponse> submitResults(@Body SubmitData data);

    @POST("customers")
    Call<Customer> signUp(@Body LoginData data);

    @POST("customers/login")
    Call<Customer> signIn(@Body LoginData data);
}
