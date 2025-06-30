package com.exemplo;

import static spark.Spark.*;
import java.util.*;
import java.io.*;

public class Main {
    static Scanner sc = new Scanner(System.in);
    static List<Conta> contas = new ArrayList<>();
    static int idCounter = 1;

    public static void main(String[] args) {
        // Parte 1: Servidor Web com Spark
        port(4567);
        staticFiles.location("/public");

        get("/", (req, res) -> {
            res.redirect("/index.html");
            return null;
        });

        // Rota 1 - Cadastrar
        post("/cadastrar", (req, res) -> {
            String nome = req.queryParams("nome");
            double valor = Double.parseDouble(req.queryParams("valor"));
            String vencimento = req.queryParams("vencimento");
            String tipo = req.queryParams("tipo").toUpperCase();

            if (!tipo.equals("PAGAR") && !tipo.equals("RECEBER")) {
                return "Tipo inválido.";
            }

            Conta c = new Conta(idCounter++, nome, valor, vencimento, tipo);
            contas.add(c);
            System.out.println("Conta cadastrada via HTML: " + c);
            res.redirect("/index.html");
            return null;
        });

        // Rota 2 - Consultar
        get("/consultar", (req, res) -> {
            String termo = req.queryParams("termo");
            System.out.println("--- Consulta via navegador ---");
            for (Conta c : contas) {
                if (String.valueOf(c.id).equals(termo) || c.nome.equalsIgnoreCase(termo)) {
                    System.out.println(c);
                    break;
                }
            }
            res.redirect("/index.html");
            return null;
        });

        // Rota 3 - Listar
        get("/listar", (req, res) -> {
            System.out.println("--- Listagem de contas via navegador ---");
            contas.forEach(System.out::println);
            res.redirect("/index.html");
            return null;
        });

        // Rota 4 - Atualizar
        post("/atualizar", (req, res) -> {
            int id = Integer.parseInt(req.queryParams("id"));
            String nome = req.queryParams("nome");
            String vencimento = req.queryParams("vencimento");
            String tipo = req.queryParams("tipo");
            String valorStr = req.queryParams("valor");

            for (Conta c : contas) {
                if (c.id == id) {
                    if (nome != null && !nome.isEmpty()) c.nome = nome;
                    if (vencimento != null && !vencimento.isEmpty()) c.vencimento = vencimento;
                    if (valorStr != null && !valorStr.isEmpty()) c.valor = Double.parseDouble(valorStr);
                    if (tipo != null && (tipo.equals("PAGAR") || tipo.equals("RECEBER"))) c.tipo = tipo;
                    System.out.println("Conta atualizada via HTML: " + c);
                    break;
                }
            }
            res.redirect("/index.html");
            return null;
        });

        // Rota 5 - Excluir
        post("/excluir", (req, res) -> {
            int id = Integer.parseInt(req.queryParams("id"));
            contas.removeIf(c -> c.id == id);
            System.out.println("Conta excluída via HTML: ID " + id);
            res.redirect("/index.html");
            return null;
        });

        // Rota 6 - Relatório
        get("/relatorio", (req, res) -> {
            System.out.println("\n--- RELATÓRIO HTML ---");
            List<Conta> pagar = new ArrayList<>();
            List<Conta> receber = new ArrayList<>();
            for (Conta c : contas) {
                if (c.tipo.equals("PAGAR")) pagar.add(c);
                else receber.add(c);
            }
            pagar.sort(Comparator.comparing(conta -> conta.vencimento));
            receber.sort(Comparator.comparing(conta -> conta.vencimento));

            System.out.println("--- CONTAS A PAGAR ---");
            pagar.forEach(System.out::println);
            System.out.println("--- CONTAS A RECEBER ---");
            receber.forEach(System.out::println);
            res.redirect("/index.html");
            return null;
        });

        // Rota 7 - Exportar
        get("/exportar", (req, res) -> {
            try (PrintWriter pw = new PrintWriter("contas_exportadas.txt")) {
                for (Conta c : contas) pw.println(c.toString());
                System.out.println("Exportado para contas_exportadas.txt via HTML.");
            } catch (IOException e) {
                System.out.println("Erro ao exportar: " + e.getMessage());
            }
            res.redirect("/index.html");
            return null;
        });

        // Rota para dashboard (JSON)
        get("/contas", (req, res) -> {
            res.type("application/json");
            return new com.google.gson.Gson().toJson(contas);
        });


        // Parte 2: Menu Interativo no terminal
        Thread terminalThread = new Thread(() -> {
            int opcao;
            do {
                System.out.println("\n--- MENU CONTAS A PAGAR/RECEBER ---");
                System.out.println("1. Cadastrar Conta");
                System.out.println("2. Consultar Conta (por nome ou ID)");
                System.out.println("3. Listar Todas as Contas");
                System.out.println("4. Atualizar Conta");
                System.out.println("5. Excluir Conta");
                System.out.println("6. Gerar Relatório");
                System.out.println("7. Exportar para TXT");
                System.out.println("0. Sair");
                System.out.print("Escolha: ");
                opcao = sc.nextInt();
                sc.nextLine();

                switch (opcao) {
                    case 1 -> cadastrar();
                    case 2 -> consultar();
                    case 3 -> listar();
                    case 4 -> atualizar();
                    case 5 -> excluir();
                    case 6 -> relatorio();
                    case 7 -> exportar();
                    case 0 -> System.out.println("Encerrando menu (Spark continua rodando)");
                    default -> System.out.println("Opção inválida.");
                }
            } while (true);
        });

        terminalThread.start();
    }

    // Os métodos abaixo seguem sem alteração
    static void cadastrar() {
        System.out.print("Nome da conta: ");
        String nome = sc.nextLine();
        System.out.print("Valor: ");
        double valor = sc.nextDouble();
        sc.nextLine();
        System.out.print("Data de vencimento (dd/mm/aaaa): ");
        String vencimento = sc.nextLine();
        System.out.print("Tipo (PAGAR/RECEBER): ");
        String tipo = sc.nextLine().toUpperCase();

        if (!tipo.equals("PAGAR") && !tipo.equals("RECEBER")) {
            System.out.println("Tipo inválido.");
            return;
        }

        Conta c = new Conta(idCounter++, nome, valor, vencimento, tipo);
        contas.add(c);
        System.out.println("Conta cadastrada com sucesso!");
    }

    static void consultar() {
        System.out.print("Digite ID ou nome: ");
        String input = sc.nextLine();

        for (Conta c : contas) {
            if (String.valueOf(c.id).equals(input) || c.nome.equalsIgnoreCase(input)) {
                System.out.println(c);
                return;
            }
        }
        System.out.println("Conta não encontrada.");
    }

    static void listar() {
        if (contas.isEmpty()) {
            System.out.println("Nenhuma conta cadastrada.");
            return;
        }
        for (Conta c : contas) {
            System.out.println(c);
        }
    }

    static void atualizar() {
        System.out.print("Digite o ID da conta a atualizar: ");
        int id = sc.nextInt(); sc.nextLine();
        for (Conta c : contas) {
            if (c.id == id) {
                System.out.print("Novo nome: ");
                c.nome = sc.nextLine();
                System.out.print("Novo valor: ");
                c.valor = sc.nextDouble(); sc.nextLine();
                System.out.print("Nova data de vencimento: ");
                c.vencimento = sc.nextLine();
                System.out.print("Novo tipo (PAGAR/RECEBER): ");
                String tipo = sc.nextLine().toUpperCase();
                if (!tipo.equals("PAGAR") && !tipo.equals("RECEBER")) {
                    System.out.println("Tipo inválido.");
                    return;
                }
                c.tipo = tipo;
                System.out.println("Conta atualizada!");
                return;
            }
        }
        System.out.println("Conta não encontrada.");
    }

    static void excluir() {
        System.out.print("Digite o ID da conta a excluir: ");
        int id = sc.nextInt(); sc.nextLine();
        Iterator<Conta> it = contas.iterator();
        while (it.hasNext()) {
            Conta c = it.next();
            if (c.id == id) {
                it.remove();
                System.out.println("Conta excluída.");
                return;
            }
        }
        System.out.println("Conta não encontrada.");
    }

    static void relatorio() {
        List<Conta> pagar = new ArrayList<>();
        List<Conta> receber = new ArrayList<>();
        for (Conta c : contas) {
            if (c.tipo.equals("PAGAR")) pagar.add(c);
            else receber.add(c);
        }

        pagar.sort(Comparator.comparing(conta -> conta.vencimento));
        receber.sort(Comparator.comparing(conta -> conta.vencimento));

        System.out.println("\n--- CONTAS A PAGAR (ORD. POR VENCIMENTO) ---");
        pagar.forEach(System.out::println);
        System.out.println("\n--- CONTAS A RECEBER (ORD. POR VENCIMENTO) ---");
        receber.forEach(System.out::println);
    }

    static void exportar() {
        try (PrintWriter pw = new PrintWriter("contas_exportadas.txt")) {
            for (Conta c : contas) pw.println(c.toString());
            System.out.println("Exportado para contas_exportadas.txt");
        } catch (IOException e) {
            System.out.println("Erro ao exportar: " + e.getMessage());
        }
    }
}
