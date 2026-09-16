package com.mycompany.bibliotecadb.servidor;

import com.mycompany.bibliotecadb.dao.UsuarioDao;
import com.mycompany.bibliotecadb.model.Usuario;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsuarioHandler implements HttpHandler
{

    private final UsuarioDao usuarioDao = new UsuarioDao();

    @Override
    public void handle(HttpExchange exchange) throws IOException
    {
        String metodo = exchange.getRequestMethod();

        try
        {
            switch (metodo.toUpperCase())
            {
                case "GET" ->
                    listarUsuarios(exchange);

                case "POST" ->
                    cadastrarUsuario(exchange);

                case "PUT" ->
                    atualizarUsuario(exchange);

                case "DELETE" ->
                    excluirUsuario(exchange);

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

    // ==============================
    // GET
    // ==============================

    private void listarUsuarios(HttpExchange exchange)
            throws IOException
    {
        List<Usuario> usuarios = usuarioDao.listarTodos();

        StringBuilder json = new StringBuilder();

        json.append("[");

        for (int i = 0; i < usuarios.size(); i++)
        {
            Usuario usuario = usuarios.get(i);

            json.append("{");

            json.append("\"idUsuario\":")
                    .append(usuario.getIdUsuario())
                    .append(",");

            json.append("\"nome\":\"")
                    .append(escaparJson(usuario.getNome()))
                    .append("\",");

            json.append("\"email\":\"")
                    .append(escaparJson(usuario.getEmail()))
                    .append("\",");

            json.append("\"telefone\":\"")
                    .append(escaparJson(usuario.getTelefone()))
                    .append("\",");

            json.append("\"dataCadastro\":\"")
                    .append(
                            usuario.getDataCadastro() != null
                            ? usuario.getDataCadastro().toString()
                            : ""
                    )
                    .append("\"");

            json.append("}");

            if (i < usuarios.size() - 1)
            {
                json.append(",");
            }
        }

        json.append("]");

        enviarResposta(exchange, 200, json.toString());
    }

    // ==============================
    // POST
    // ==============================

    private void cadastrarUsuario(HttpExchange exchange)
            throws IOException
    {
        String corpo = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        Map<String, String> dados = lerFormulario(corpo);

        Usuario usuario = new Usuario();

        usuario.setNome(dados.get("nome"));
        usuario.setEmail(dados.get("email"));
        usuario.setTelefone(dados.get("telefone"));

        boolean cadastrado = usuarioDao.cadastrar(usuario);

        if (cadastrado)
        {
            enviarResposta(
                    exchange,
                    201,
                    "{\"mensagem\":\"Leitor cadastrado com sucesso!\"}"
            );
        } else
        {
            enviarResposta(
                    exchange,
                    409,
            "{\"mensagem\":\"Não foi possível cadastrar o leitor. "
            + "Verifique se o e-mail já está cadastrado.\"}"
    );
}
    }

    // ==============================
    // PUT
    // ==============================

    private void atualizarUsuario(HttpExchange exchange)
            throws IOException
    {
        int idUsuario = obterIdDaUrl(exchange);

        Usuario usuarioExistente =
                usuarioDao.buscarPorId(idUsuario);

        if (usuarioExistente == null)
        {
            enviarResposta(
                    exchange,
                    404,
                    "{\"mensagem\":\"Leitor não encontrado.\"}"
            );

            return;
        }

        String corpo = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        Map<String, String> dados = lerFormulario(corpo);

        Usuario usuario = new Usuario();

        usuario.setIdUsuario(idUsuario);
        usuario.setNome(dados.get("nome"));
        usuario.setEmail(dados.get("email"));
        usuario.setTelefone(dados.get("telefone"));

        boolean atualizado = usuarioDao.atualizar(usuario);

        if (atualizado)
        {
            enviarResposta(
                    exchange,
                    200,
                    "{\"mensagem\":\"Leitor atualizado com sucesso!\"}"
            );
        } else
        {
            enviarResposta(
                    exchange,
                    409,
                    "{\"mensagem\":\"Não foi possível atualizar o leitor. "
                    + "Verifique se o e-mail já está sendo utilizado.\"}"
            );
        }
    }

    // ==============================
    // DELETE
    // ==============================

    private void excluirUsuario(HttpExchange exchange)
            throws IOException
    {
        int idUsuario = obterIdDaUrl(exchange);

        Usuario usuario =
                usuarioDao.buscarPorId(idUsuario);

        if (usuario == null)
        {
            enviarResposta(
                    exchange,
                    404,
                    "{\"mensagem\":\"Leitor não encontrado.\"}"
            );

            return;
        }

        boolean excluido = usuarioDao.excluir(idUsuario);

        if (excluido)
        {
            enviarResposta(
                    exchange,
                    200,
                    "{\"mensagem\":\"Leitor excluído com sucesso!\"}"
            );
        } else
        {
            enviarResposta(
                    exchange,
                    409,
                    "{\"mensagem\":\"Não foi possível excluir o leitor. "
                    + "Ele pode possuir empréstimos registrados.\"}"
            );
        }
    }

    // ==============================
    // ID DA URL
    // ==============================

    private int obterIdDaUrl(HttpExchange exchange)
    {
        String caminho =
                exchange.getRequestURI().getPath();

        String prefixo = "/api/usuarios/";

        if (!caminho.startsWith(prefixo))
        {
            throw new IllegalArgumentException(
                    "ID do leitor não informado."
            );
        }

        String id =
                caminho.substring(prefixo.length());

        if (id.isBlank() || id.contains("/"))
        {
            throw new IllegalArgumentException(
                    "ID do leitor inválido."
            );
        }

        return Integer.parseInt(id);
    }

    // ==============================
    // FORMULÁRIO
    // ==============================

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

    // ==============================
    // RESPOSTA
    // ==============================

    private void enviarResposta(
            HttpExchange exchange,
            int status,
            String resposta) throws IOException
    {
        byte[] bytes =
                resposta.getBytes(StandardCharsets.UTF_8);

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