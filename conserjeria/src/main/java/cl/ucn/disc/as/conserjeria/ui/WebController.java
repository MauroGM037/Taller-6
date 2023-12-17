package cl.ucn.disc.as.conserjeria.ui;

import cl.ucn.disc.as.conserjeria.model.Persona;
import cl.ucn.disc.as.conserjeria.services.Sistema;
import cl.ucn.disc.as.conserjeria.services.SistemaImpl;
import io.ebean.DB;
import io.javalin.Javalin;
import io.javalin.http.NotFoundResponse;
import cl.ucn.disc.as.conserjeria.grpc.PersonaGrpc;
import cl.ucn.disc.as.conserjeria.grpc.PersonaGrpcRequest;
import cl.ucn.disc.as.conserjeria.grpc.PersonaGrpcResponse;
import cl.ucn.disc.as.conserjeria.grpc.PersonaGrpcServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.List;
import java.util.Optional;

public final class WebController implements RoutesConfigurator {

    /**
     * The Sistema.
     */
    private final Sistema sistema;

    /**
     * The Web Controller.
     */
    public WebController(){
        this.sistema = new SistemaImpl(DB.getDefault());
        //only populate in case of new database.
        List<Persona> personas = this.sistema.getPersonas();

        if (personas.isEmpty()) {
            this.sistema.populate();
        }
    }
    /**
     * Configure the routes.
     *
     * @param app to use.
     */
    @Override
    public void configure(final Javalin app){

        app.get("/", ctx -> {
            ctx.result("Welcome to Conserjeria API REST");
        });

        // the personas api
        app.get("/personas", ctx -> {
            ctx.json(this.sistema.getPersonas());
        });

        app.get("/personas/rut/{rut}", ctx -> {
            String rut = ctx.pathParam("rut");
            Optional<Persona> oPersona = this.sistema.getPersona(rut);
            ctx.json(oPersona.orElseThrow(() -> new NotFoundResponse("Can't find Persona with rut: " + rut)));
        });

        app.get("/api/grpc/personas/rut/{rut}", ctx -> {

            String rut = ctx.pathParam("rut");

            // the channel
            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress("127.0.0.1", 50123) // FIXME: declare the port in configuration
                    .usePlaintext() // FIXME: don't use unencripted protocol
                    .build();

            // stub
            PersonaGrpcServiceGrpc.PersonaGrpcServiceBlockingStub stub =
                    PersonaGrpcServiceGrpc.newBlockingStub(channel);

            // call the gprc
            PersonaGrpcResponse response = stub.retrieve(PersonaGrpcRequest
                    .newBuilder()
                    .setRut("130144918")
                    .build());
            // get the response
            PersonaGrpc personaGrpc = response.getPersona();

            // FIXME: use the mapper to convert domain
            // return the persona
            Optional<Persona> oPersona = Optional.of(Persona.builder()
                    .rut(personaGrpc.getRut())
                    .nombre(personaGrpc.getNombre())
                    .apellidos(personaGrpc.getApellidos())
                    .email(personaGrpc.getEmail())
                    .build());

            ctx.json(oPersona.orElseThrow(() -> new NotFoundResponse("Can't find Persona with rut: " + rut)));

        });





    }
}
