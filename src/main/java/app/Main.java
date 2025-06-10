package app;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import dao.CategoriaDAO;
import dao.CredCategoriaDAO;
import dao.ItemCredencialDAO;
import dao.RelatorioDAO;
import dao.UsuarioDAO;
import model.Categoria;
import model.ItemCredencial;
import model.Usuario;
import util.CriptoUtil;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private static final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private static final ItemCredencialDAO itemCredencialDAO = new ItemCredencialDAO();
    private static final CredCategoriaDAO credCategoriaDAO = new CredCategoriaDAO();
    private static final RelatorioDAO relatorioDAO = new RelatorioDAO();

    // VARIÁVEIS DE SESSÃO (Para simular o usuário logado)
    private static Usuario usuarioLogado = null;
    private static String senhaMestraLogada = null; // A senha em texto PURO, necessária para cripto/descripto

    public static void main(String[] args) {
        int opcao;

        System.out.println("==================================================");
        System.out.println("        GERENCIADOR DE SENHAS - COFRE DIGITAL");
        System.out.println("==================================================");

        do {
            exibirMenuPrincipal();
            opcao = lerOpcao();

            // Lógica de autenticação: Só permite acesso se houver um usuário logado
            if (usuarioLogado == null && opcao > 0 && opcao != 99) {
                System.out.println("\n🚨 ATENÇÃO: Você precisa fazer o Login para acessar o cofre!");
                if (!realizarLogin()) {
                    continue; // Retorna ao menu se o login falhar
                }
            }

            // Opções principais do sistema
            switch (opcao) {
                case 1: menuGerenciarUsuarios(); break;
                case 2: menuGerenciarCategorias(); break;
                case 3: menuGerenciarCredenciais(); break;
                case 4: menuProcessosNegocio(); break;
                case 5: menuRelatorios(); break;
                case 99: menuLoginESair(); break; // Opção Login/Logout
                case 0: System.out.println("\nFechando o Gerenciador. Até logo!"); break;
                default: System.out.println("Opção inválida. Tente novamente.");
            }
        } while (opcao != 0);

        scanner.close();
    }

    // -----------------------------------------------------------------
    // --- FUNÇÕES DE UTILIDADE E AUTENTICAÇÃO ---
    // -----------------------------------------------------------------

    private static void exibirMenuPrincipal() {
        String status = usuarioLogado != null ? ("USUÁRIO: " + usuarioLogado.getLogin()) : "NENHUM";
        System.out.println("\n--------------------------------------------------");
        System.out.println("        STATUS: " + status);
        System.out.println("--------------------------------------------------");
        System.out.println("[1] Gerenciar Usuários (CRUD)");
        System.out.println("[2] Gerenciar Categorias (CRUD)");
        System.out.println("[3] Gerenciar Credenciais (CRUD - Cofre)");
        System.out.println("[4] Processos de Negócio (Associação M:N)");
        System.out.println("[5] Relatórios do Sistema (JOINs)");
        System.out.println("[99] Fazer Login / Logout");
        System.out.println("[0] Sair do Programa");
        System.out.print("\n>> Digite a opção desejada: ");
    }

    private static int lerOpcao() {
        try {
            int opcao = scanner.nextInt();
            scanner.nextLine();
            return opcao;
        } catch (InputMismatchException e) {
            scanner.nextLine();
            return -1;
        }
    }

    private static boolean realizarLogin() {
        System.out.println("\n--- AUTENTICAÇÃO ---");
        System.out.print("Login: ");
        String login = scanner.nextLine();
        System.out.print("Senha Mestra: ");
        String senha = scanner.nextLine();

        // 1. Busca o usuário no banco
        Usuario usuario = usuarioDAO.buscarUsuarioPorLogin(login);

        if (usuario != null) {
            // 2. Gera o hash da senha fornecida com o salt do usuário
            String hashDigitado = CriptoUtil.gerarHashMestre(senha, usuario.getSalt());

            // 3. Compara o hash gerado com o hash salvo no banco
            if (hashDigitado.equals(usuario.getSenhaMestraHash())) {
                usuarioLogado = usuario; // Define a sessão
                senhaMestraLogada = senha; // Guarda a senha (para descriptografia)
                System.out.println("[SUCESSO] Login realizado com sucesso. Bem-vindo, " + usuario.getNome() + "!");
                return true;
            }
        }
        System.out.println("[FALHA] Login ou Senha Mestra incorretos.");
        return false;
    }

    private static void menuLoginESair() {
        if (usuarioLogado != null) {
            System.out.println("Usuário " + usuarioLogado.getLogin() + " deslogado.");
            usuarioLogado = null;
            senhaMestraLogada = null;
        } else {
            realizarLogin();
        }
    }

    // -----------------------------------------------------------------
    // --- 1. CRUD USUÁRIOS (Já implementado) ---
    // -----------------------------------------------------------------

    private static void menuGerenciarUsuarios() {
        int opcao;
        do {
            System.out.println("\n--- CRUD DE USUÁRIOS ---");
            System.out.println("[1] Cadastrar | [2] Buscar por Login | [3] Atualizar | [4] Remover | [9] Voltar");
            System.out.print(">> Opção: ");

            opcao = lerOpcao();

            switch (opcao) {
                case 1: realizarCadastroUsuario(); break;
                case 2: realizarBuscaUsuario(); break;
                case 3: realizarAtualizacaoUsuario(); break;
                case 4: realizarRemocaoUsuario(); break;
                case 9: break;
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

        String salt = CriptoUtil.gerarSalt();
        String hash = CriptoUtil.gerarHashMestre(senha, salt);

        Usuario novoUsuario = new Usuario(nome, login, hash, salt);
        usuarioDAO.cadastrarUsuario(novoUsuario);
    }

    private static void realizarBuscaUsuario() {
        System.out.print("Digite o Login para buscar: ");
        String login = scanner.nextLine();

        Usuario usuario = usuarioDAO.buscarUsuarioPorLogin(login);

        if (usuario != null) {
            System.out.println("--- DADOS ---");
            System.out.println("ID: " + usuario.getIdUsuario());
            System.out.println("Nome: " + usuario.getNome());
            System.out.println("Login: " + usuario.getLogin());
            System.out.println("Data de Cadastro: " + usuario.getDataCadastro());
        }
    }

    private static void realizarAtualizacaoUsuario() {
        // Implementação simplificada (reutilizando métodos)
        if (usuarioLogado == null) return;

        System.out.println("\n--- Atualizar Dados do Próprio Usuário ---");

        System.out.print("Novo Nome (" + usuarioLogado.getNome() + "): ");
        String novoNome = scanner.nextLine();
        if (novoNome.isEmpty()) novoNome = usuarioLogado.getNome();

        System.out.print("Novo Login (" + usuarioLogado.getLogin() + "): ");
        String novoLogin = scanner.nextLine();
        if (novoLogin.isEmpty()) novoLogin = usuarioLogado.getLogin();

        // Cria objeto temporário para o update
        Usuario usuarioAtualizado = new Usuario(
                usuarioLogado.getIdUsuario(),
                novoNome,
                novoLogin,
                usuarioLogado.getSenhaMestraHash(), // Mantém o hash e salt
                usuarioLogado.getSalt(),
                usuarioLogado.getDataCadastro()
        );

        if (usuarioDAO.atualizarUsuario(usuarioAtualizado)) {
            // Atualiza a sessão após o sucesso no DB
            usuarioLogado = usuarioAtualizado;
        }
    }

    private static void realizarRemocaoUsuario() {
        // Por segurança, só permite remover o próprio usuário (requer ID)
        if (usuarioLogado == null) return;

        System.out.println("\n--- Remoção de Conta ---");
        System.out.print("Confirma a remoção da sua conta? (ID: " + usuarioLogado.getIdUsuario() + ") Digite SIM: ");
        String confirmacao = scanner.nextLine();

        if (confirmacao.equalsIgnoreCase("SIM")) {
            if (usuarioDAO.removerUsuario(usuarioLogado.getIdUsuario())) {
                System.out.println("Sua conta foi removida. O programa será fechado.");
                System.exit(0); // Sai do programa após a remoção
            }
        } else {
            System.out.println("Remoção cancelada.");
        }
    }

    // -----------------------------------------------------------------
    // --- 2. CRUD CATEGORIAS ---
    // -----------------------------------------------------------------

    private static void menuGerenciarCategorias() {
        int opcao;
        do {
            System.out.println("\n--- CRUD DE CATEGORIAS ---");
            System.out.println("[1] Cadastrar | [2] Listar Todas | [3] Atualizar | [4] Remover | [9] Voltar");
            System.out.print(">> Opção: ");

            opcao = lerOpcao();

            switch (opcao) {
                case 1: realizarCadastroCategoria(); break;
                case 2: listarTodasCategorias(); break;
                case 3: realizarAtualizacaoCategoria(); break;
                case 4: realizarRemocaoCategoria(); break;
                case 9: break;
                default: System.out.println("Opção inválida.");
            }
        } while (opcao != 9);
    }

    private static void realizarCadastroCategoria() {
        System.out.println("\n--- Cadastro de Categoria ---");
        System.out.print("Nome da Categoria (Ex: 'Bancos'): ");
        String nome = scanner.nextLine();
        categoriaDAO.cadastrarCategoria(new Categoria(nome));
    }

    private static void listarTodasCategorias() {
        List<Categoria> categorias = categoriaDAO.listarTodas();
        System.out.println("\n--- Lista de Categorias ---");
        if (categorias.isEmpty()) {
            System.out.println("Nenhuma categoria cadastrada.");
            return;
        }
        categorias.forEach(System.out::println);
    }

    private static void realizarAtualizacaoCategoria() {
        listarTodasCategorias();
        System.out.println("\n--- Atualização de Categoria ---");
        System.out.print("Digite o ID da Categoria para atualizar: ");
        int id = lerOpcao();
        System.out.print("Novo Nome: ");
        String novoNome = scanner.nextLine();

        categoriaDAO.atualizarCategoria(new Categoria(id, novoNome));
    }

    private static void realizarRemocaoCategoria() {
        listarTodasCategorias();
        System.out.println("\n--- Remoção de Categoria ---");
        System.out.print("Digite o ID da Categoria para remover: ");
        int id = lerOpcao();

        categoriaDAO.removerCategoria(id);
    }

    // -----------------------------------------------------------------
    // --- 3. CRUD CREDENCIAIS (Cofre) ---
    // -----------------------------------------------------------------

    private static void menuGerenciarCredenciais() {
        if (usuarioLogado == null) return;
        int opcao;
        do {
            System.out.println("\n--- CRUD DE CREDENCIAIS (Cofre) ---");
            System.out.println("[1] Cadastrar Novo Item | [2] Listar Meus Itens | [3] Atualizar Item | [4] Remover Item | [9] Voltar");
            System.out.print(">> Opção: ");

            opcao = lerOpcao();

            switch (opcao) {
                case 1: realizarCadastroItem(); break;
                case 2: listarItensDoUsuario(); break;
                case 3: realizarAtualizacaoItem(); break;
                case 4: realizarRemocaoItem(); break;
                case 9: break;
                default: System.out.println("Opção inválida.");
            }
        } while (opcao != 9);
    }

    private static void realizarCadastroItem() {
        if (usuarioLogado == null || senhaMestraLogada == null) return;
        System.out.println("\n--- Cadastro de Novo Item ---");
        System.out.print("Título (Ex: Netflix): ");
        String titulo = scanner.nextLine();
        System.out.print("Tipo (SENHA, CARTAO, NOTA): ");
        String tipo = scanner.nextLine().toUpperCase();
        System.out.print("Login do Serviço: ");
        String loginServico = scanner.nextLine();
        System.out.print("URL (Opcional): ");
        String url = scanner.nextLine();
        System.out.print("Dado Sensível (Senha, Número de Cartão, Conteúdo da Nota): ");
        String dadoPuro = scanner.nextLine();

        // CRIAÇÃO DO OBJETO E CHAMADA DO DAO (A criptografia é feita no DAO)
        ItemCredencial novoItem = new ItemCredencial(
                usuarioLogado.getIdUsuario(),
                tipo,
                titulo,
                loginServico,
                dadoPuro, // Passado em texto puro para o DAO
                url
        );

        itemCredencialDAO.cadastrarItem(novoItem, senhaMestraLogada);
    }

    private static void listarItensDoUsuario() {
        if (usuarioLogado == null || senhaMestraLogada == null) return;

        List<ItemCredencial> itens = itemCredencialDAO.listarItensPorUsuario(usuarioLogado.getIdUsuario(), senhaMestraLogada);
        System.out.println("\n--- MEUS ITENS DE COFRE ---");

        if (itens.isEmpty()) {
            System.out.println("Seu cofre está vazio.");
            return;
        }

        for (ItemCredencial item : itens) {
            System.out.println("----------------------------------");
            System.out.println("ID: " + item.getIdItem() + " | Título: " + item.getTitulo() + " | Tipo: " + item.getTipo());
            System.out.println("Login: " + item.getLoginServico());
            // Mostra o dado descriptografado!
            System.out.println("Dado Secreto: " + item.getSenhaCriptografada());

            // Lista as associações de categorias para este item (Processo de Negócio)
            Map<Integer, String> cats = credCategoriaDAO.listarCategoriasPorItem(item.getIdItem());
            String listaCats = cats.isEmpty() ? "Nenhuma" : String.join(", ", cats.values());
            System.out.println("Categorias: " + listaCats);
        }
    }

    private static void realizarAtualizacaoItem() {
        if (usuarioLogado == null) return;
        listarItensDoUsuario();

        System.out.println("\n--- Atualização de Item ---");
        System.out.print("Digite o ID do item para atualizar: ");
        int id = lerOpcao();

        System.out.print("Novo Título: ");
        String novoTitulo = scanner.nextLine();
        System.out.print("Novo Tipo (SENHA/CARTAO/NOTA): ");
        String novoTipo = scanner.nextLine().toUpperCase();
        System.out.print("Novo Login: ");
        String novoLogin = scanner.nextLine();
        System.out.print("Nova URL: ");
        String novaUrl = scanner.nextLine();
        System.out.print("Novo Dado Secreto (Texto Puro): ");
        String novoDadoPuro = scanner.nextLine();

        // Cria um objeto temporário para o update
        ItemCredencial itemAtualizado = new ItemCredencial(
                id,
                usuarioLogado.getIdUsuario(),
                novoTipo,
                novoTitulo,
                novoLogin,
                novoDadoPuro, // Passa o novo dado em texto puro para o DAO
                novaUrl,
                null // Data de modificação será atualizada pelo DB
        );

        itemCredencialDAO.atualizarItem(itemAtualizado, senhaMestraLogada);
    }

    private static void realizarRemocaoItem() {
        if (usuarioLogado == null) return;
        listarItensDoUsuario();

        System.out.println("\n--- Remoção de Item ---");
        System.out.print("Digite o ID do item para remover: ");
        int id = lerOpcao();

        itemCredencialDAO.removerItem(id, usuarioLogado.getIdUsuario());
    }

    // -----------------------------------------------------------------
    // --- 4. PROCESSOS DE NEGÓCIO (M:N) ---
    // -----------------------------------------------------------------

    private static void menuProcessosNegocio() {
        if (usuarioLogado == null) return;
        int opcao;
        do {
            System.out.println("\n--- PROCESSOS DE NEGÓCIO (Associação M:N) ---");
            System.out.println("[1] Associar Item a Categoria | [2] Desassociar Item | [9] Voltar");
            System.out.print(">> Opção: ");

            opcao = lerOpcao();

            switch (opcao) {
                case 1: realizarAssociacao(); break;
                case 2: realizarDesassociacao(); break;
                case 9: break;
                default: System.out.println("Opção inválida.");
            }
        } while (opcao != 9);
    }

    private static void realizarAssociacao() {
        System.out.println("\n--- Associar Item e Categoria ---");
        System.out.print("ID do Item de Credencial: ");
        int idItem = lerOpcao();
        listarTodasCategorias();
        System.out.print("ID da Categoria para associar: ");
        int idCategoria = lerOpcao();

        credCategoriaDAO.associar(idItem, idCategoria);
    }

    private static void realizarDesassociacao() {
        System.out.println("\n--- Desassociar Item e Categoria ---");
        System.out.print("ID do Item de Credencial: ");
        int idItem = lerOpcao();
        System.out.print("ID da Categoria para desassociar: ");
        int idCategoria = lerOpcao();

        credCategoriaDAO.desassociar(idItem, idCategoria);
    }

    // -----------------------------------------------------------------
    // --- 5. RELATÓRIOS ---
    // -----------------------------------------------------------------

    private static void menuRelatorios() {
        if (usuarioLogado == null) return;
        int opcao;
        do {
            System.out.println("\n--- RELATÓRIOS DO SISTEMA ---");
            System.out.println("[1] Itens Salvos por Usuário (JOIN)");
            System.out.println("[2] Contagem de Itens por Categoria (JOIN)");
            System.out.println("[3] Itens com Múltiplas Categorias (M:N JOIN/GROUP)");
            System.out.println("[9] Voltar");
            System.out.print(">> Opção: ");

            opcao = lerOpcao();

            switch (opcao) {
                case 1: exibirRelatorioItensPorUsuario(); break;
                case 2: exibirRelatorioItensPorCategoria(); break;
                case 3: exibirRelatorioMultiplasCategorias(); break;
                case 9: break;
                default: System.out.println("Opção inválida.");
            }
        } while (opcao != 9);
    }

    private static void exibirRelatorioItensPorUsuario() {
        Map<String, Integer> resultados = relatorioDAO.contarItensPorUsuario();
        System.out.println("\n==================================================");
        System.out.println("RELATÓRIO 1: ITENS SALVOS POR USUÁRIO");
        System.out.println("--------------------------------------------------");
        System.out.printf("| %-20s | %-15s |\n", "USUÁRIO", "TOTAL DE ITENS");
        System.out.println("--------------------------------------------------");
        resultados.forEach((nome, total) ->
                System.out.printf("| %-20s | %-15d |\n", nome, total)
        );
        System.out.println("==================================================");
    }

    private static void exibirRelatorioItensPorCategoria() {
        Map<String, Integer> resultados = relatorioDAO.contarItensPorCategoria();
        System.out.println("\n==================================================");
        System.out.println("RELATÓRIO 2: ITENS POR CATEGORIA");
        System.out.println("--------------------------------------------------");
        System.out.printf("| %-30s | %-10s |\n", "CATEGORIA", "TOTAL");
        System.out.println("--------------------------------------------------");
        resultados.forEach((nome, total) ->
                System.out.printf("| %-30s | %-10d |\n", nome, total)
        );
        System.out.println("==================================================");
    }

    private static void exibirRelatorioMultiplasCategorias() {
        // Pedimos o mínimo de 2, pois 1 é a regra básica.
        Map<String, Integer> resultados = relatorioDAO.itensComMultiplasCategorias(2);
        System.out.println("\n==================================================");
        System.out.println("RELATÓRIO 3: ITENS COM MÚLTIPLAS CATEGORIAS (>= 2)");
        System.out.println("--------------------------------------------------");
        System.out.printf("| %-30s | %-15s |\n", "TÍTULO DO ITEM", "Nº DE CATEGORIAS");
        System.out.println("--------------------------------------------------");

        if (resultados.isEmpty()) {
            System.out.println("Nenhum item possui 2 ou mais categorias associadas.");
        } else {
            resultados.forEach((titulo, total) ->
                    System.out.printf("| %-30s | %-15d |\n", titulo, total)
            );
        }
        System.out.println("==================================================");
    }
}