package Classes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Biblioteca {
    private List<Livro> livros;
    private List<Usuario> usuarios;
    private List<Emprestimo> emprestimos;

    public Biblioteca() {
        this.livros = new ArrayList<>();
        this.usuarios = new ArrayList<>();
        this.emprestimos = new ArrayList<>();
    }

    public void adicionarLivro(Livro livro) {
        if (livro == null) {
            throw new IllegalArgumentException("Livro não pode ser nulo");
        }
        livros.add(livro);
    }

    public void registrarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não pode ser nulo");
        }
        usuarios.add(usuario);
    }

    public boolean emprestarLivro(String isbn, String usuarioId, LocalDate dataEmprestimo) {
        Livro livro = buscarLivroPorIsbn(isbn);
        Usuario usuario = buscarUsuarioPorId(usuarioId);

        if (livro == null || usuario == null) {
            return false;
        }

        if (!livro.isDisponivel() || usuario.isPossuiMulta()) {
            return false;
        }

        livro.setDisponivel(false);
        Emprestimo emprestimo = new Emprestimo(livro, usuario, dataEmprestimo);
        emprestimos.add(emprestimo);
        return true;
    }

    public double devolverLivro(String isbn, LocalDate dataDevolucao) {
        Emprestimo emprestimo = buscarEmprestimoAtivo(isbn);
        if (emprestimo == null) {
            return -1; // Empréstimo não encontrado
        }

        Livro livro = emprestimo.getLivro();
        livro.setDisponivel(true);

        // Calcular multa se houver atraso
        long diasAtraso = ChronoUnit.DAYS.between(emprestimo.getDataDevolucaoPrevista(), dataDevolucao);
        double multa = 0;

        if (diasAtraso > 0) {
            multa = diasAtraso * 2.0; // R$ 2,00 por dia de atraso
            emprestimo.getUsuario().setPossuiMulta(true);
        }

        emprestimo.setDataDevolucaoReal(dataDevolucao);
        return multa;
    }

    public List<Livro> listarLivrosDisponiveis() {
        return livros.stream()
                .filter(Livro::isDisponivel)
                .toList();
    }

    private Livro buscarLivroPorIsbn(String isbn) {
        return livros.stream()
                .filter(l -> l.getIsbn().equals(isbn))
                .findFirst()
                .orElse(null);
    }

    private Usuario buscarUsuarioPorId(String id) {
        return usuarios.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private Emprestimo buscarEmprestimoAtivo(String isbn) {
        return emprestimos.stream()
                .filter(e -> e.getLivro().getIsbn().equals(isbn) && e.getDataDevolucaoReal() == null)
                .findFirst()
                .orElse(null);
    }
}
