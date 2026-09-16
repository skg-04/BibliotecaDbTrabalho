package com.mycompany.bibliotecadb.servidor;

import com.mycompany.bibliotecadb.dao.LivroDao;
import com.mycompany.bibliotecadb.model.Livro;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LivroHandler implements HttpHandler
{

    private final LivroDao livroDao = new LivroDao();

    @Override
    public void handle(HttpExchange exchange) throws IOException
    {
        String metodo = exchange.getRequestMethod();

        try
        {
            switch (metodo.toUpperCase())
            {
                case "GET" ->
                    listarLivros(exchange);

                case "POST" ->
                    cadastrarLivro(exchange);

                case "PUT" ->
                    atualizarLivro(exchange);

                case "DELETE" ->
                    excluirLivro(exchange);

                default ->
                    enviarResposta(
                            exchange,
                            405,
                            "{\"mensagem\":\"Método não permitido.\"}"
                    );
            }

        } catch (NumberFormatException e)
        {
            enviarResposta(
                    exchange,
                    400,
                    "{\"mensagem\":\"Número inválido.\"}"
            );

        } catch (IllegalArgumentException e)
        {
            enviarResposta(
                    exchange,
                    400,
                    "{\"mensagem\":\""
                    + escaparJson(e.getMessage())
                    + "\"}"
            );

        } catch (Exception e)
        {
            e.printStackTrace();

            enviarResposta(
                    exchange,
                    500,
                    "{\"mensagem\":\"Erro interno do servidor.\"}"
            );
        }
    }

    // ==================================================
    // GET
    // ==================================================
    private void listarLivros(HttpExchange exchange) throws IOException
    {
        List<Livro> livros = livroDao.listarTodos();

        StringBuilder json = new StringBuilder();

        json.append("[");

        for (int i = 0; i < livros.size(); i++)
        {
            Livro livro = livros.get(i);

            json.append("{");

            json.append("\"idLivro\":")
                    .append(livro.getIdLivro())
                    .append(",");

            json.append("\"titulo\":\"")
                    .append(escaparJson(livro.getTitulo()))
                    .append("\",");

            json.append("\"autor\":\"")
                    .append(escaparJson(livro.getAutor()))
                    .append("\",");

            json.append("\"anoPublicacao\":")
                    .append(livro.getAnoPublicacao())
                    .append(",");

            json.append("\"quantidadeTotal\":")
                    .append(livro.getQuantidadeTotal())
                    .append(",");

            json.append("\"quantidadeDisponivel\":")
                    .append(livro.getQuantidadeDisponivel());

            json.append("}");

            if (i < livros.size() - 1)
            {
                json.append(",");
            }
        }

        json.append("]");

        enviarResposta(exchange, 200, json.toString());
    }

    // ==================================================
    // POST
    // ==================================================
    private void cadastrarLivro(HttpExchange exchange) throws IOException
    {
        String corpo = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        Map<String, String> dados = lerFormulario(corpo);

        Livro livro = new Livro();

        livro.setTitulo(dados.get("titulo"));
        livro.setAutor(dados.get("autor"));

        livro.setAnoPublicacao(
                Integer.parseInt(dados.get("anoPublicacao"))
        );

        int quantidadeTotal
                = Integer.parseInt(dados.get("quantidadeTotal"));

        livro.setQuantidadeTotal(quantidadeTotal);

        // Livro novo começa com todas as unidades disponíveis.
        livro.setQuantidadeDisponivel(quantidadeTotal);

        boolean cadastrado = livroDao.cadastrar(livro);

        if (cadastrado)
        {
            enviarResposta(
                    exchange,
                    201,
                    "{\"mensagem\":\"Livro cadastrado com sucesso!\"}"
            );
        } else
        {
            enviarResposta(
                    exchange,
                    500,
                    "{\"mensagem\":\"Não foi possível cadastrar o livro.\"}"
            );
        }
    }

    // ==================================================
    // PUT
    // ==================================================
    private void atualizarLivro(HttpExchange exchange) throws IOException
    {
        int idLivro = obterIdDaUrl(exchange);

        Livro livroAtual = livroDao.buscarPorId(idLivro);

        if (livroAtual == null)
        {
            enviarResposta(
                    exchange,
                    404,
                    "{\"mensagem\":\"Livro não encontrado.\"}"
            );

            return;
        }

        String corpo = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        Map<String, String> dados = lerFormulario(corpo);

        int novaQuantidadeTotal
                = Integer.parseInt(dados.get("quantidadeTotal"));

        /*
         * Calcula quantas unidades estão emprestadas.
         *
         * Exemplo:
         * Total = 5
         * Disponíveis = 3
         * Emprestadas = 2
         */
        int quantidadeEmprestada
                = livroAtual.getQuantidadeTotal()
                - livroAtual.getQuantidadeDisponivel();

        /*
         * Não podemos diminuir o total para uma quantidade
         * menor que o número de exemplares emprestados.
         */
        if (novaQuantidadeTotal < quantidadeEmprestada)
        {
            enviarResposta(
                    exchange,
                    400,
                    "{\"mensagem\":\"A quantidade total não pode ser menor "
                    + "que a quantidade de livros atualmente emprestados.\"}"
            );

            return;
        }

        int novaQuantidadeDisponivel
                = novaQuantidadeTotal - quantidadeEmprestada;

        Livro livro = new Livro();

        livro.setIdLivro(idLivro);
        livro.setTitulo(dados.get("titulo"));
        livro.setAutor(dados.get("autor"));

        livro.setAnoPublicacao(
                Integer.parseInt(dados.get("anoPublicacao"))
        );

        livro.setQuantidadeTotal(novaQuantidadeTotal);
        livro.setQuantidadeDisponivel(novaQuantidadeDisponivel);

        boolean atualizado = livroDao.atualizar(livro);

        if (atualizado)
        {
            enviarResposta(
                    exchange,
                    200,
                    "{\"mensagem\":\"Livro atualizado com sucesso!\"}"
            );
        } else
        {
            enviarResposta(
                    exchange,
                    500,
                    "{\"mensagem\":\"Não foi possível atualizar o livro.\"}"
            );
        }
    }

    // ==================================================
    // DELETE
    // ==================================================
    private void excluirLivro(HttpExchange exchange) throws IOException
    {
        int idLivro = obterIdDaUrl(exchange);

        Livro livro = livroDao.buscarPorId(idLivro);

        if (livro == null)
        {
            enviarResposta(
                    exchange,
                    404,
                    "{\"mensagem\":\"Livro não encontrado.\"}"
            );

            return;
        }

        boolean excluido = livroDao.excluir(idLivro);

        if (excluido)
        {
            enviarResposta(
                    exchange,
                    200,
                    "{\"mensagem\":\"Livro excluído com sucesso!\"}"
            );
        } else
        {
            enviarResposta(
                    exchange,
                    409,
                    "{\"mensagem\":\"Não foi possível excluir o livro. "
                    + "Ele pode possuir empréstimos registrados.\"}"
            );
        }
    }

    // ==================================================
    // PEGAR ID DA URL
    // ==================================================
    private int obterIdDaUrl(HttpExchange exchange)
    {
        String caminho = exchange.getRequestURI().getPath();

        // Exemplo:
        // /api/livros/2
        String prefixo = "/api/livros/";

        if (!caminho.startsWith(prefixo))
        {
            throw new IllegalArgumentException(
                    "ID do livro não informado."
            );
        }

        String id = caminho.substring(prefixo.length());

        if (id.isBlank() || id.contains("/"))
        {
            throw new IllegalArgumentException(
                    "ID do livro inválido."
            );
        }

        return Integer.parseInt(id);
    }

    // ==================================================
    // LER FORMULÁRIO
    // ==================================================
    private Map<String, String> lerFormulario(String corpo)
    {
        Map<String, String> dados = new HashMap<>();

        String[] campos = corpo.split("&");

        for (String campo : campos)
        {
            String[] partes = campo.split("=", 2);

            if (partes.length == 2)
            {
                String chave = URLDecoder.decode(
                        partes[0],
                        StandardCharsets.UTF_8
                );

                String valor = URLDecoder.decode(
                        partes[1],
                        StandardCharsets.UTF_8
                );

                dados.put(chave, valor);
            }
        }

        return dados;
    }

    // ==================================================
    // RESPOSTA HTTP
    // ==================================================
    private void enviarResposta(
            HttpExchange exchange,
            int status,
            String resposta) throws IOException
    {
        byte[] bytes
                = resposta.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(
                status,
                bytes.length
        );

        try (OutputStream os = exchange.getResponseBody())
        {
            os.write(bytes);
        }
    }

    // ==================================================
    // ESCAPAR JSON
    // ==================================================
    private String escaparJson(String texto)
    {
        if (texto == null)
        {
            return "";
        }

        return texto
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
