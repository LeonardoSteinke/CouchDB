package Couch;

import org.ektorp.support.CouchDbDocument;

/**
 *
 * @authors Leonardo Steinke, Rodrigo Valle
 */

//única coisa necessária na classe de modelo que deseja persistir é dar extends CouchDbDocument
public class Pessoa extends CouchDbDocument{

    private String Nome;
    private int Idade;
    private double Salario;
    private String[] Linguagens = {null, null, null, null, null, null, null, null, null, null};

    public Pessoa() {
    }
    

    public String getNome() {
        return Nome;
    }

    public void setNome(String Nome) {
        this.Nome = Nome;
    }

    public int getIdade() {
        return Idade;
    }

    public void setIdade(int Idade) {
        this.Idade = Idade;
    }

    public double getSalario() {
        return Salario;
    }

    public void setSalario(double Salario) {
        this.Salario = Salario;
    }

    public String[] getLinguagens() {
        return Linguagens;
    }

    public void setLinguagens(String[] Linguagens) {
        this.Linguagens = Linguagens;
    }
      
}
