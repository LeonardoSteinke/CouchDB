package Couch;

import java.util.Random;
import java.util.Scanner;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.ViewQuery;
import org.ektorp.ViewResult;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;

/**
 * Programa exemplificando os métodos de criação, edição, busca e exclusão utilizando o banco de dados CouchDB
 * em conjunto com a biblioteca Ektorp
 * 
 * @authors Leonardo Steinke, Rodrigo Valle
 */

public class CouchDb {

    /**
     * @param args the command line arguments
     */
    static Scanner sc = new Scanner(System.in);

    static Pessoa pessoa;

    static ViewQuery q;

    static ViewResult result;

    public static void main(String[] args) throws Exception {
        /**
         * Criar conexão
         */
        HttpClient httpClient = new StdHttpClient.Builder()
                .port(5984)
                .username("admin")
                .password("admin")
                .url("http://localhost:5984")
                .build();
        CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);

        /**
         * Criar o DataBase ou se conectar a um banco existente
         */
        CouchDbConnector db = new StdCouchDbConnector("bancopessoa", dbInstance);
        db.createDatabaseIfNotExists();

        /**
         * CRUD para o banco
         */
        boolean r = true;
        do {
            System.out.println("C - CRIAR PESSOA\n"
                    + "B - BUSCAR PESSOA\n"
                    + "A - ATUALIZAR PESSOA\n"
                    + "D - DELETAR PESSOA");

            switch (sc.next().toUpperCase()) {
                case "C":
                    criarPessoa(db);
                    break;

                case "B":
                    buscarPessoa(db);
                    break;

                case "A":
                    atualizarPessoa(db);
                    break;

                case "D":
                    deletarPessoa(db);
                    System.out.println("ITEM DELETADO COM SUCESSO");
                    break;

                case "INSERIR":
                    System.out.println("INSERINDO DADOS ...");
                    povoarBanco(db);
                    System.out.println("BANCO POVOADO COM SUCESSO");
                    break;

                default:
                    r = false;

            }

        } while (r);

    }

    /**
     * Esse metodo Cria um novo objeto de Pessoa
     *
     * @param db = parametro de conexão do CouchDB
     */
    private static void criarPessoa(CouchDbConnector db) {

        pessoa = new Pessoa();
        System.out.println("Digite o CPF (CHAVE PRIMÁRIA - NÃO PODE SER ALTERADA)");
        /*
        * _id do arquivo JSON que é utilizado como chave única do documento, só é possível recuperar um
        * documento do banco a partir desse id
         */
        pessoa.setId(sc.next());

        System.out.println("Digite o nome: ");
        pessoa.setNome(sc.next().toUpperCase());

        System.out.println("Digite a idade: ");
        pessoa.setIdade(sc.nextInt());

        System.out.println("Digite o salário: ");
        pessoa.setSalario(sc.nextDouble());

        System.out.println("Quantas linguagens " + pessoa.getNome() + " sabe?");

        String[] aLinguagens = new String[sc.nextInt()];
        for (int i = 0; i < aLinguagens.length; i++) {
            System.out.println("Digite a " + (i + 1) + "ª linguagem: ");
            aLinguagens[i] = sc.next().toUpperCase();
        }
        pessoa.setLinguagens(aLinguagens);

        /**
         * metodo create cria um novo objeto no banco
         */
        db.create(pessoa);
    }

    /**
     * Esse metodo busca um ou muitos objetos de Pessoa no banco
     *
     * @param db = parametro de conexão do CouchDB
     */
    private static void buscarPessoa(CouchDbConnector db) {

        System.out.println("\nPor qual Coluna você que buscar?"
                + "\nID - ID ESPECIFICO"
                + "\nL - BUSCA POR LINGUAGEM"
                + "\nI - BUSCA POR IDADE"
                + "\nS - SALARIO");

        switch (sc.next().toUpperCase()) {
            case "ID":
                System.out.println("\nDigite o id: ");
                pessoa = db.get(Pessoa.class, sc.next().toUpperCase());
                System.out.println("Dados dessa pessoa:"
                        + "\nNome: " + pessoa.getNome()
                        + "\nIdade: " + pessoa.getIdade()
                        + "\nSalario: " + pessoa.getSalario()
                        + "\nLinguagens conhecidas: ");
                for (int i = 0; i < pessoa.getLinguagens().length; i++) {
                    if (pessoa.getLinguagens()[i] != null) {
                        System.out.println(pessoa.getLinguagens()[i]);
                    }
                }
                break;

            case "I":
                /**
                 * VIEW - NECESSARIO ADICIONAR A FUNCTION NO SGBD
                 *
                 * function(doc){ if(doc.idade){ emit(doc.idade, doc._id); } }
                 */
                System.out.println("Você quer buscar por qual Idade? ");

                //ViewResult result
                q = new ViewQuery()
                        .viewName("idade")
                        .designDocId("_design/view")
                        .key(sc.nextInt());

                result = db.queryView(q);
                imprimirBusca(db, result);
                break;

            case "L":
                /**
                 * VIEW - NECESSARIO ADICIONAR A FUNCTION NO SGBD
                 *
                 * function(doc){ if(doc.linguagens){ for(var i in
                 * doc.linguagens){ emit(doc.linguagens[i], doc._id); } } }
                 */
                System.out.println("Você quer buscar por qual linguagem? ");
                //
                q = new ViewQuery()
                        .viewName("linguagens")
                        .designDocId("_design/view")
                        .key(sc.next().toUpperCase());
                //
                result = db.queryView(q);
                imprimirBusca(db, result);

                break;
            case "S":
                /**
                 * VIEW - NECESSARIO ADICIONAR A FUNCTION NO SGBD
                 *
                 * function(doc) { if (doc.salario) { emit(doc.salario,
                 * doc._id); } }
                 */
                System.out.println("BUSCA POR:"
                        + "\nE - SALARIO IGUAL A"
                        + "\nMA - SALARIO MAIOR QUE"
                        + "\nME - SALARIO MENOR QUE"
                        + "\nEN - SALARIO ENTRE");

                switch (sc.next().toUpperCase()) {
                    case "E":
                        System.out.println("DIGITE O SALARIO");
                        q = new ViewQuery()
                                .viewName("salario")
                                .designDocId("_design/view")
                                .key(sc.nextDouble());
                        result = db.queryView(q);
                        imprimirBusca(db, result);
                        break;
                    case "MA":
                        System.out.println("DIGITE O SALARIO");
                        q = new ViewQuery()
                                .viewName("salario")
                                .designDocId("_design/view")
                                .startKey(sc.nextDouble());
                        result = db.queryView(q);
                        imprimirBusca(db, result);
                        break;
                    case "ME":
                        System.out.println("DIGITE O SALARIO");
                        q = new ViewQuery()
                                .viewName("salario")
                                .designDocId("_design/view")
                                .endKey(sc.nextDouble());
                        result = db.queryView(q);
                        imprimirBusca(db, result);
                        break;
                    case "EN":
                        System.out.println("DIGITE O SALARIO");
                        System.out.println("COMEÇANDO EM: ");
                        double salario = sc.nextDouble();
                        System.out.println("TERMINANDO EM:");
                        q = new ViewQuery()
                                .viewName("salario")
                                .designDocId("_design/view")
                                .startKey(salario).endKey(sc.nextDouble());
                        result = db.queryView(q);
                        imprimirBusca(db, result);
                        break;
                }

                break;

        }
    }

    /**
     * Esse metodo atualiza um objeto de Pessoa
     *
     * @param db = parametro de conexão do CouchDB
     */
    private static void atualizarPessoa(CouchDbConnector db) {
        System.out.println("Digite o CPF da pessoa que você quer atualizar: ");
        pessoa = db.get(Pessoa.class, sc.next());

        System.out.println("Qual atributo de pessoa você quer atualizar?\n"
                + "N - NOME\n"
                + "S - SALÁRIO\n"
                + "I - IDADE\n"
                + "L - ADD LINGUAGEM");

        switch (sc.next().toLowerCase()) {
            case "n":
                System.out.println("Digite o novo nome para " + pessoa.getNome());
                pessoa.setNome(sc.nextLine().toUpperCase());
                db.update(pessoa);
                break;
            case "s":
                System.out.println("Digite o novo salário de " + pessoa.getNome());
                pessoa.setSalario(sc.nextDouble());
                db.update(pessoa);
                break;
            case "i":
                System.out.println("Digite a nova Idade para " + pessoa.getNome());
                pessoa.setIdade(sc.nextInt());
                db.update(pessoa);
                break;
            case "l":
                for (int i = 0; i < pessoa.getLinguagens().length; i++) {
                    if (pessoa.getLinguagens()[i] == null) {
                        System.out.println("Digite a nova linguagem");
                        pessoa.getLinguagens()[i] = sc.next().toUpperCase();
                        break;
                    } else if (i == (pessoa.getLinguagens().length - 1)) {
                        System.out.println("Essa pessoa não pode aprender mais nenhuma linguagem");
                    }
                }
                /**
                 * Método update procura no banco buscando pelo id e atualiza o
                 * registro, com isso um novo numero de _rev é criado, porém o
                 * _id permanece o mesmo
                 */
                db.update(pessoa);
                break;

        }
    }

    /**
     * Esse metodo Deleta um objeto de Pessoa
     *
     * @param db = parametro de conexão do CouchDB
     */
    private static void deletarPessoa(CouchDbConnector db) {
        System.out.println("Digite o CPF da pessoa que você quer deletar: ");
        pessoa = db.get(Pessoa.class, sc.next());
        db.delete(pessoa);
    }

    /**
     * Esse método faz um povoamento aleatório no banco
     *
     * @param db
     */
    private static void povoarBanco(CouchDbConnector db) {

        String NomesM[] = new String[]{"Miguel", "Lucas", "Guilherme", "Gabriel", "Arthur", "Enzo", "Rafael", "João", "Gustavo", "Pedro", "Robson", "Adilson", "Leonardo", "Rodrigo", "Paolo", "Roberto", "Mário", "Luigi", "Marcelo", "Giancarlo", "Adroan", "Tadeu", "Samuel", "Vinicius", "Tobias", "Otavio", "Bernardo", "George", "Davi", "Luke", "Patrick", "Lorenzo", "Roberto", "Luiz", "Eduardo", "Victor", "Mohammed"};
        String NomesF[] = new String[]{"Laura", "Beatriz", "Ana", "Maria", "Julia", "Alice", "Brenda", "Sofia", "Mariana", "Anna", "Lúcia", "Jenifer", "Fabiola", "Jéssica", "Jurema", "Lana", "Diana", "Isabela", "Joyce", "Kethryn", "Bruna", "Maria Eduarda", "Eloisa", "Vitória", "Gabriela", "Carina", "Georgia", "Roberta", "Michelle", "Jordana", "Melissa", "Marina", "Marcela", "Larissa"};
        String Sobrenomes[] = new String[]{"da Silva", "de Souza", "de Jesus", "Pereira", "Nunes", "Costa", "Coelho", "Farah", "Valle", "Steinke", "Paetzoldt", "Vahldick", "Ferreira", "Mantau", "Moser", "Schimitz", "Pinto", "Winchester", "Uzumaki", "Uchiha", "Potter", "Katz", "Almeida", "Alves", "Lopes", "Mendes", "Moura", "Reis", "Rocha", "Vieira"};
        String Linguagens[] = new String[]{"Java", "C", "C++", "C#", "Python", "R", "Visual Basic", "SQL", "PHP", "CSS", "HTML", "JS", "Node", "Mindstorms", "Cobol", "Perl", "Swift", "Delphi", "Go!", "Assembly", "Erlang", "Blueprint", "Scratch", "RPG Maker", "Kotlin", "Java", "TypeScript", "Groovy"};

        for (int i = 0; i < 1000; i++) {
            int x = new Random().nextInt(2) + 1;
            pessoa = new Pessoa();

            switch (x) {
                case 1:
                    pessoa.setNome(NomesM[new Random().nextInt(NomesM.length)].toUpperCase() + " " + Sobrenomes[new Random().nextInt(Sobrenomes.length)].toUpperCase());
                    break;
                case 2:
                    pessoa.setNome(NomesF[new Random().nextInt(NomesF.length)].toUpperCase() + " " + Sobrenomes[new Random().nextInt(Sobrenomes.length)].toUpperCase());
                    break;

            }

            pessoa.setId("" + (i + 1));
            pessoa.setIdade((new Random().nextInt(50) + 18));
            pessoa.setSalario(((new Random().nextInt(100) + 20) * (new Random().nextInt(100) + 30)));
            String[] aLing = new String[new Random().nextInt(10) + 1];
            for (int j = 0; j < aLing.length; j++) {
                pessoa.getLinguagens()[j] = Linguagens[new Random().nextInt(Linguagens.length)].toUpperCase();
                for (int k = 0; k < aLing.length - 1; k++) {
                    if (j == k) {
                        k++;
                    } else if (pessoa.getLinguagens()[j].equals(pessoa.getLinguagens()[k])) {
                        pessoa.getLinguagens()[j] = null;
                        break;
                    }

                }
            }
            db.create(pessoa);
        }
    }

    /**
     * Esse metodo imprime o resultado das buscas
     *
     * @param db instancia do banco
     * @param result Lista de id's dos objetos
     */
    private static void imprimirBusca(CouchDbConnector db, ViewResult result) {
        System.out.println("Essa busca retornou " + result.getSize() + " registros");
        System.out.println("Deseja imprimir os resultados? S-N\n");
        if (sc.next().toUpperCase().equals("S")) {
            for (ViewResult.Row row : result) {
                String value = row.getValue();
                pessoa = db.get(Pessoa.class, value);
                System.out.println("Dados dessa pessoa:"
                        + "\nNome: " + pessoa.getNome()
                        + "\nIdade: " + pessoa.getIdade()
                        + "\nSalario: " + pessoa.getSalario()
                        + "\nLinguagens conhecidas: ");
                for (int i = 0; i < pessoa.getLinguagens().length; i++) {
                    if (pessoa.getLinguagens()[i] != null) {
                        System.out.println("\t" + pessoa.getLinguagens()[i]);
                    }
                }
                System.out.println("---------------------");
            }

        }

    }
}
