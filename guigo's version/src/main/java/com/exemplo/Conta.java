package com.exemplo;

public class Conta {
    int id;
    String nome;
    double valor;
    String vencimento;
    String tipo; // PAGAR ou RECEBER

    public Conta(int id, String nome, double valor, String vencimento, String tipo) {
        this.id = id;
        this.nome = nome;
        this.valor = valor;
        this.vencimento = vencimento;
        this.tipo = tipo;
    }

    public String toString() {
        return String.format("ID: %d | Nome: %s | Valor: %.2f | Vencimento: %s | Tipo: %s",
                id, nome, valor, vencimento, tipo);
    }
}
