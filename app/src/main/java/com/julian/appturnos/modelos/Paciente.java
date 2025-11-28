package com.julian.appturnos.modelos;

public class Paciente extends Usuario {
    public int IdPaciente;
    public String DNI;
    public int IdObraSocial;

    public Paciente() {
        super();
    }

    public int getIdPaciente() {
        return IdPaciente;
    }

    public void setIdPaciente(int idPaciente) {
        IdPaciente = idPaciente;
    }

    public String getDNI() {
        return DNI;
    }

    public void setDNI(String DNI) {
        this.DNI = DNI;
    }

    public int getIdObraSocial() {
        return IdObraSocial;
    }

    public void setIdObraSocial(int idObraSocial) {
        IdObraSocial = idObraSocial;
    }
}