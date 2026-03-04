package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosEpsodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoAPI;
import br.com.alura.screenmatch.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
  private final String ENDERECO = "https://www.omdbapi.com/?apikey=b71688b1&t=";
  private Scanner leitura = new Scanner(System.in);
  private ConsumoAPI consumo = new ConsumoAPI();
  private ConverteDados conversor = new ConverteDados();

  public void exibeMenu(){
    System.out.println("Digite o nome da série para busca: ");
    var nomeSerie = leitura.nextLine();
    var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+"));
    DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
//    System.out.println(dados);

    List<DadosTemporada> temporadas = new ArrayList<>();

    for (int i = 1; i <= dados.totalTemporadas(); i++) {
      json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i);

      DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
      temporadas.add(dadosTemporada);
    }
//    temporadas.forEach(System.out::println);

//    temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));


    List<DadosEpsodio> dadosEpsodios = temporadas.stream().flatMap(t -> t.episodios().stream())
      .collect(Collectors.toList());

//    dadosEpsodios.stream()
//      .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
//      .sorted(Comparator.comparing(DadosEpsodio::avaliacao).reversed())
//      .limit(5)
//      .map(e -> e.titulo().toUpperCase())
//      .forEach(System.out::println);

    List<Episodio> episodios = temporadas.stream()
      .flatMap(t -> t.episodios().stream()
        .map(d -> new Episodio(t.numero(), d)))
      .collect(Collectors.toList());

    episodios.forEach(System.out::println);

//    System.out.println("Digite o nome do título: ");
//    String trechoTitulo = leitura.nextLine();
//    Optional<Episodio> episodioBuscado = episodios.stream()
//      .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
//      .findFirst();
//
//    if(episodioBuscado.isPresent()){
//      System.out.println("Episodio encontrado.Temporada: " + episodioBuscado.get().getTemporada());
//    } else {
//      System.out.println("Episódio não encontrado.");
//    }

//    System.out.println("A partir de que ano vc deseja ver os episodios?");
//    var ano = leitura.nextInt();
//    leitura.nextLine();
//
//    LocalDate dataBusca = LocalDate.of(ano, 01,01);
//
//    DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/YYYY");
//    episodios.stream().filter(e -> e.getDataLancamento()!= null && e.getDataLancamento().isAfter(dataBusca))
//      .forEach(e -> System.out.println(
//        "Temporada: " + e.getTemporada()
//          + " Episódio: " + e.getTitulo()
//          + " Data lançamento: " + e.getDataLancamento().format(formatador)
//      ));

    Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
      .filter(e -> e.getAvaliacao() > 0.0)
      .collect(Collectors.groupingBy(Episodio::getTemporada, Collectors.averagingDouble(Episodio::getAvaliacao)));

    System.out.println(avaliacoesPorTemporada);

    DoubleSummaryStatistics est = episodios.stream()
      .filter(e -> e.getAvaliacao() > 0.0)
      .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));

    System.out.println("Média: " + est.getAverage());
    System.out.println("Melhor episódio: " + est.getMax());
    System.out.println("Pior episódio: " + est.getMin());
    System.out.println("Quantidade: " + est.getCount());
  }
}
