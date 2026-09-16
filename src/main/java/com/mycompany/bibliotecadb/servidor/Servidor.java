package com.mycompany.bibliotecadb.servidor;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Servidor
{

    public static void iniciar()
    {

        try
        {
            HttpServer servidor
                    = HttpServer.create(new InetSocketAddress(8080), 0);

            // =========================
            // FRONT-END
            // =========================
            servidor.createContext("/", exchange ->
            {

                String caminho = exchange.getRequestURI().getPath();

                // Somente a página inicial
                if (!caminho.equals("/"))
                {
                    exchange.sendResponseHeaders(404, -1);
                    exchange.close();
                    return;
                }

                try (InputStream arquivo
                        = Servidor.class.getResourceAsStream(
                                "/frontend/biblioteca.html"))
                {

                    if (arquivo == null)
                    {
                        String resposta
                                = "Erro: biblioteca.html não encontrado.";

                        byte[] bytes
                                = resposta.getBytes(StandardCharsets.UTF_8);

                        exchange.sendResponseHeaders(500, bytes.length);

                        try (OutputStream os = exchange.getResponseBody())
                        {
                            os.write(bytes);
                        }

                        return;
                    }

                    byte[] conteudo = arquivo.readAllBytes();

                    exchange.getResponseHeaders().set(
                            "Content-Type",
                            "text/html; charset=UTF-8"
                    );

                    exchange.sendResponseHeaders(
                            200,
                            conteudo.length
                    );

                    try (OutputStream os = exchange.getResponseBody())
                    {
                        os.write(conteudo);
                    }
                }
            });

            // =========================
            // TESTE DO SERVIDOR
            // =========================
            servidor.createContext("/teste", exchange ->
            {

                String resposta
                        = "Servidor da Biblioteca funcionando!";

                byte[] bytes
                        = resposta.getBytes(StandardCharsets.UTF_8);

                exchange.getResponseHeaders().set(
                        "Content-Type",
                        "text/plain; charset=UTF-8"
                );

                exchange.sendResponseHeaders(
                        200,
                        bytes.length
                );

                try (OutputStream os = exchange.getResponseBody())
                {
                    os.write(bytes);
                }
            });

            // =========================
            // API DE LIVROS
            // =========================
            servidor.createContext(
                    "/api/livros",
                    new LivroHandler()
            );

            servidor.createContext(
                    "/api/usuarios",
                    new UsuarioHandler()
            );

            servidor.createContext(
                    "/api/emprestimos",
                    new EmprestimoHandler()
            );
            // =========================
            // INICIAR SERVIDOR
            // =========================
            servidor.setExecutor(null);
            servidor.start();

            System.out.println("==============================");
            System.out.println(" SISTEMA DE BIBLIOTECA");
            System.out.println("==============================");
            System.out.println("Servidor iniciado com sucesso!");
            System.out.println();
            System.out.println("Sistema:");
            System.out.println("http://localhost:8080/");
            System.out.println();
            System.out.println("API Livros:");
            System.out.println("http://localhost:8080/api/livros");

        } catch (IOException e)
        {
            System.out.println("Erro ao iniciar servidor:");
            System.out.println(e.getMessage());
        }
    }
}
