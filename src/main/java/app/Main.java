// Local: src/main/java/app/Main.java

package app;

import java.util.Scanner;
import dao.UsuarioDAO;
import model.Usuario;
import util.CriptoUtil;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public static void main(String[] args) {
        int opcao;

        System.out.println("==================================================");
        System.out.println("        INICIANDO GERENCIADOR DE SENHAS");
        System.out.println("==================================================");

        do {
            exibirMenuPrincipal();
            opcao = lerOpcao();

            switch (opcao) {
                case 1:
                    // Chama a função que gerencia o CRUD de Usuários
                    menuGerenciarUsuarios();
                    break;
                case 2:
                    System.out.println("Opção: Gerenciar Credenciais (CRUD - EM DESENVOLVIMENTO)");
                    break;
                case 3:
                    System.out.println("Opção: Processos de Negócio (EM DESENVOLVIMENTO)");
                    break;
                case 4:
                    System.out.println("Opção: Relatórios do Sistema (EM DESENVOLVIMENTO)");
                    break;
                case 0:
                    System.out.println("\nFechando o Gerenciador. Até logo!");
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        } while (opcao != 0);

        scanner.close(); // Fecha o Scanner ao sair do programa
    }

    private static void exibirMenuPrincipal() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("        MENU PRINCIPAL");
        System.out.println("--------------------------------------------------");
        System.out.println("[1] Gerenciar Usuários (CRUD)");
        System.out.println("[2] Gerenciar Credenciais (Itens do Cofre)");
        System.out.println("[3] Processos de Negócio (Tabelas Associativas)");
        System.out.println("[4] Relatórios do Sistema");
        System.out.println("[0] Sair do Programa");
        System.out.print("\n>> Digite a opção desejada: ");
    }

    private static int lerOpcao() {
        if (scanner.hasNextInt()) {
            int opcao = scanner.nextInt();
            scanner.nextLine(); // Consome a quebra de linha
            return opcao;
        } else {
            scanner.nextLine(); // Consome entrada inválida
            return -1; // Retorna um valor inválido
        }
    }

    // --- Implementação do Submenu CRUD de Usuários ---

    private static void menuGerenciarUsuarios() {
        int opcao;
        do {
            System.out.println("\n--------------------------------------------------");
            System.out.println("        CRUD DE USUÁRIOS");
            System.out.println("--------------------------------------------------");
            System.out.println("[1] Cadastrar Novo Usuário (CREATE)");
            System.out.println("[2] Buscar Usuário por Login (READ)");
            System.out.println("[3] Atualizar Dados do Usuário (UPDATE)");
            System.out.println("[4] Remover Usuário (DELETE)");
            System.out.println("[9] Voltar ao Menu Principal");
            System.out.print("\n>> Digite a opção desejada: ");

            opcao = lerOpcao();

            switch (opcao) {
                case 1: realizarCadastroUsuario(); break;
                case 2: realizarBuscaUsuario(); break;
                case 3: realizarAtualizacaoUsuario(); break;
                case 4: realizarRemocaoUsuario(); break;
                case 9: System.out.println("Voltando..."); break;
                default: System.out.println("Opção inválida.");
            }
        } while (opcao != 9);
    }

    private static void realizarCadastroUsuario() {
        System.out.println("\n--- Cadastro de Novo Usuário ---");
        System.out.print("Nome completo: ");
        String nome = scanner.nextLine();
        System.out.print("Login (e-mail único): ");
        String login = scanner.nextLine();
        System.out.print("Senha Mestra: ");
        String senha = scanner.nextLine();

        // 1. Gera o SALT e o HASH antes de criar o objeto (Segurança)
        String salt = CriptoUtil.gerarSalt();
        String hash = CriptoUtil.gerarHashMestre(senha, salt);

        // 2. Cria o objeto Model
        Usuario novoUsuario = new Usuario(nome, login, hash, salt);

        // 3. Chama o DAO (que imprime o resultado)
        usuarioDAO.cadastrarUsuario(novoUsuario);
    }

    private static void realizarBuscaUsuario() {
        System.out.println("\n--- Busca de Usuário ---");
        System.out.print("Digite o Login (e-mail) para buscar: ");
        String login = scanner.nextLine();

        Usuario usuario = usuarioDAO.buscarUsuarioPorLogin(login);

        if (usuario != null) {
            System.out.println("----------------------------------");
            System.out.println("DADOS DO USUÁRIO ENCONTRADO:");
            System.out.println("ID: " + usuario.getIdUsuario());
            System.out.println("Nome: " + usuario.getNome());
            System.out.println("Login: " + usuario.getLogin());
            System.out.println("Data de Cadastro: " + usuario.getDataCadastro());
            System.out.println("----------------------------------");
        } else {
            // O DAO já imprime uma mensagem, mas podemos reforçar
            System.out.println("Usuário não encontrado.");
        }
    }

    private static void realizarAtualizacaoUsuario() {
        System.out.println("\n--- Atualização de Usuário ---");
        System.out.print("Digite o ID do usuário para atualizar: ");
        int id = lerOpcao();

        // Em um sistema real, você buscaria o usuário pelo ID primeiro.
        // Aqui, vamos direto à atualização, assumindo que o ID é válido.

        System.out.print("Novo Nome Completo: ");
        String novoNome = scanner.nextLine();
        System.out.print("Novo Login (e-mail único): ");
        String novoLogin = scanner.nextLine();

        // Cria um objeto Usuario temporário com o ID e os novos dados
        // (Hash e Salt são mantidos como dummy, pois a senha não está sendo alterada neste métod)
        Usuario usuarioAtualizado = new Usuario(id, novoNome, novoLogin, "dummy_hash", "dummy_salt", null);

        usuarioDAO.atualizarUsuario(usuarioAtualizado);
    }

    private static void realizarRemocaoUsuario() {
        System.out.println("\n--- Remoção de Usuário ---");
        System.out.print("Digite o ID do usuário para remover: ");
        int id = lerOpcao();

        usuarioDAO.removerUsuario(id);
    }
}