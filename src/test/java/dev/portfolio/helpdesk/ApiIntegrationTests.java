package dev.portfolio.helpdesk;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties={"spring.datasource.url=jdbc:h2:mem:integration;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"})
class ApiIntegrationTests {
    @LocalServerPort int port;
    @Autowired AccountRepository accounts;
    @Autowired PasswordEncoder encoder;
    final HttpClient client = HttpClient.newHttpClient();
    final String password = "SenhaTeste123!";
    HttpResponse<String> request(String method, String path, String body, String email) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:"+port+path))
            .header("Content-Type","application/json");
        if(email != null) builder.header("Authorization","Basic "+Base64.getEncoder().encodeToString((email+":"+password).getBytes(StandardCharsets.UTF_8)));
        return client.send(builder.method(method,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
    }
    String register() throws Exception {
        String email = UUID.randomUUID()+"@test.local";
        var response=request("POST","/api/auth/register","{\"name\":\"Cliente\",\"email\":\""+email+"\",\"password\":\""+password+"\"}",null);
        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.body()).doesNotContain("passwordHash",password);
        return email;
    }
    String create(String email) throws Exception {
        var response=request("POST","/api/tickets","{\"title\":\"Erro ao pagar\",\"description\":\"Pagamento retorna erro 500\",\"priority\":\"HIGH\"}",email);
        assertThat(response.statusCode()).isEqualTo(201);
        return response.headers().firstValue("Location").orElseThrow();
    }
    @Test void authenticationAndValidation() throws Exception {
        assertThat(request("GET","/api/tickets",null,null).statusCode()).isEqualTo(401);
        assertThat(request("POST","/api/auth/register","{\"name\":\"\",\"email\":\"invalido\",\"password\":\"123\"}",null).statusCode()).isEqualTo(400);
        String email=register();
        assertThat(request("GET","/api/auth/me",null,email).body()).contains("CUSTOMER");
        assertThat(request("POST","/api/auth/register","{\"name\":\"Outro\",\"email\":\""+email+"\",\"password\":\""+password+"\"}",null).statusCode()).isEqualTo(409);
        assertThat(request("GET","/api/tickets?size=101",null,email).statusCode()).isEqualTo(400);
        assertThat(request("GET","/api/tickets?status=INVALID",null,email).statusCode()).isEqualTo(400);
    }
    @Test void customersCannotReadOrChangeOtherCustomersTickets() throws Exception {
        String owner=register(), other=register(), path=create(owner);
        assertThat(request("GET",path,null,other).statusCode()).isEqualTo(404);
        assertThat(request("GET",path+"/history",null,other).statusCode()).isEqualTo(404);
        assertThat(request("POST",path+"/comments","{\"message\":\"invasao\"}",other).statusCode()).isEqualTo(404);
        assertThat(request("GET","/api/tickets",null,other).body()).contains("\"totalElements\":0");
        assertThat(request("PATCH",path+"/status","{\"status\":\"IN_PROGRESS\"}",owner).statusCode()).isEqualTo(403);
        assertThat(request("GET","/api/tickets?priority=LOW",null,owner).body()).contains("\"totalElements\":0");
    }
    @Test void completeWorkflowAndAuditHistory() throws Exception {
        String owner=register(), path=create(owner), agent=UUID.randomUUID()+"@agent.local";
        accounts.save(new Account(agent,encoder.encode(password),"Agente",Account.Role.AGENT));
        assertThat(request("PATCH",path+"/status","{\"status\":\"CLOSED\"}",agent).statusCode()).isEqualTo(409);
        assertThat(request("POST",path+"/comments","{\"message\":\"Anexei os detalhes do erro\"}",owner).statusCode()).isEqualTo(201);
        for(String status:List.of("IN_PROGRESS","RESOLVED","IN_PROGRESS","RESOLVED","CLOSED")) {
            var response=request("PATCH",path+"/status","{\"status\":\""+status+"\"}",agent);
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("\"status\":\""+status+"\"");
        }
        assertThat(request("POST",path+"/comments","{\"message\":\"Tarde demais\"}",owner).statusCode()).isEqualTo(409);
        assertThat(request("PATCH",path+"/status","{\"status\":\"IN_PROGRESS\"}",agent).statusCode()).isEqualTo(409);
        assertThat(request("GET",path+"/history",null,owner).body()).contains("\"totalElements\":7","Anexei os detalhes");
        assertThat(request("GET",path,null,owner).body()).contains("\"status\":\"CLOSED\"");
    }
}
