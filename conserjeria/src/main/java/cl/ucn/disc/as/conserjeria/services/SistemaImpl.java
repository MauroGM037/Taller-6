package cl.ucn.disc.as.conserjeria.services;

import cl.ucn.disc.as.conserjeria.model.*;
import com.github.javafaker.Faker;
import com.github.javafaker.service.FakeValuesService;
import com.github.javafaker.service.RandomService;
import io.ebean.Database;
import java.util.Optional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

public class SistemaImpl implements Sistema {
    private final Database database;

    public SistemaImpl(Database database) {
        this.database = database;
    }

    @Override
    public Edificio add(Edificio edificio) {
        edificio.save();
        return edificio;
    }

    @Override
    public Persona add(Persona persona) {
        persona.save();
        return persona;
    }

    @Override
    public Departamento addDepartamento(Departamento departamento, Edificio edificio) {
        edificio.getDepartamentos().add(departamento);
        edificio.save();
        return departamento;
    }

    @Override
    public Departamento addDepartamento(Departamento departamento, Long idEdificio) {
        Edificio edificio = database.find(Edificio.class, idEdificio);
        assert edificio != null;
        edificio.getDepartamentos().add(departamento);
        edificio.save();
        return departamento;
    }

    @Override
    public Contrato realizarContrato(Persona duenio, Departamento departamento, Instant fechaPago) {
        Contrato contrato = new Contrato(duenio, departamento, fechaPago);
        contrato.save();
        return contrato;
    }

    @Override
    public Contrato realizarContrato(Long idDuenio, Long idDepartamento, Instant fechaPago) {
        Persona duenio = database.find(Persona.class, idDuenio);
        Departamento departamento = database.find(Departamento.class, idDepartamento);
        Contrato contrato = new Contrato(duenio, departamento, fechaPago);
        contrato.save();
        return contrato;
    }

    @Override
    public List<Contrato> getContratos() {
        return database.find(Contrato.class).findList();
    }

    @Override
    public List<Persona> getPersonas() {
        return database.find(Persona.class).findList();
    }

    @Override
    public List<Pago> getPagos(String rut) {
        return database.find(Pago.class)
                .where()
                .eq("contrato.persona.rut", rut)
                .findList();
    }

    @Override
    public Optional<Persona> getPersona(String rut) {
        return database.find(Persona.class)
                .where()
                .eq("rut", rut)
                .findOneOrEmpty();
    }

    @Override
    public void populate(){
        //build the persona
        {
            Persona persona = Persona.builder()
                    .rut("19735233-7")
                    .nombre("Mauricio")
                    .apellidos("Godoy Milla")
                    .email("mauricio.godoy@alumnos.ucn.cl")
                    .telefono("+56912345678")
                    .build();
            this.database.save(persona);
        }

        // the faker
        Locale locale = new Locale("es-CL");
        FakeValuesService fvs = new FakeValuesService(locale, new RandomService());
        Faker faker = new Faker(locale);

        // faker
        for (int i=0; i < 1000; i++){
            Persona persona = Persona.builder()
                    .rut(fvs.bothify("#######-#"))
                    .nombre(faker.name().firstName())
                    .apellidos(faker.name().lastName())
                    .email(fvs.bothify("????##@gmail.com"))
                    .telefono(fvs.bothify("+569########"))
                    .build();
            this.database.save(persona);

        }
    }

}
