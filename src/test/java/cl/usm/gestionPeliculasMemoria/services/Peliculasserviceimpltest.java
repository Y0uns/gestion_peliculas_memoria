package cl.usm.gestionPeliculasMemoria.services;

import cl.usm.gestionPeliculasMemoria.entities.Pelicula;
import cl.usm.gestionPeliculasMemoria.repositories.PeliculasRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de PeliculasServiceImpl")
class PeliculasServiceImplTest {

    @Mock
    private PeliculasRepository peliculasRepository;

    @InjectMocks
    private PeliculasServiceImpl peliculasService;
    @Test
    @DisplayName("createPelicula: asigna tokenDescarga y retorna pelicula creada")
    void createPelicula_deberiaAsignarTokenYRetornarPelicula() {
        Pelicula pelicula = new Pelicula("p1", "Inception", "Nolan", null, null);
        when(peliculasRepository.insert(any(Pelicula.class))).thenAnswer(inv -> inv.getArgument(0));

        Pelicula resultado = peliculasService.createPelicula(pelicula);

        assertNotNull(resultado);
        assertNotNull(resultado.getTokenDescarga());
        assertEquals(10, resultado.getTokenDescarga().length());
        verify(peliculasRepository, times(1)).insert(pelicula);
    }

    @Test
    @DisplayName("createPelicula: el token generado es alphanumerico de 10 caracteres")
    void createPelicula_tokenDebeSerAlphanumerico() {
        Pelicula pelicula = new Pelicula("p1", "Inception", "Nolan", null, null);
        when(peliculasRepository.insert(any(Pelicula.class))).thenAnswer(inv -> inv.getArgument(0));

        Pelicula resultado = peliculasService.createPelicula(pelicula);

        assertTrue(resultado.getTokenDescarga().matches("[a-zA-Z0-9]{10}"));
    }

    @Test
    @DisplayName("createPelicula: retorna null cuando el repositorio lanza excepcion")
    void createPelicula_deberiaRetornarNullSiRepositorioFalla() {
        Pelicula pelicula = new Pelicula("p1", "Inception", "Nolan", null, null);
        when(peliculasRepository.insert(any(Pelicula.class)))
                .thenThrow(new IllegalArgumentException("ID duplicado"));

        Pelicula resultado = peliculasService.createPelicula(pelicula);

        assertNull(resultado);
        verify(peliculasRepository, times(1)).insert(pelicula);
    }

    @Test
    @DisplayName("createPelicula: llama exactamente una vez al repositorio")
    void createPelicula_deberiaLlamarRepositorioUnaVez() {
        Pelicula pelicula = new Pelicula("p1", "Inception", "Nolan", null, null);
        when(peliculasRepository.insert(any(Pelicula.class))).thenReturn(pelicula);

        peliculasService.createPelicula(pelicula);

        verify(peliculasRepository, times(1)).insert(any(Pelicula.class));
    }
    @Test
    @DisplayName("getAll: retorna lista de peliculas del repositorio")
    void getAll_deberiaRetornarTodasLasPeliculas() {
        List<Pelicula> peliculas = Arrays.asList(
                new Pelicula("p1", "Inception", "Nolan", null, null),
                new Pelicula("p2", "Matrix", "Wachowski", null, null)
        );
        when(peliculasRepository.findAll()).thenReturn(peliculas);

        List<Pelicula> resultado = peliculasService.getAll();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(peliculasRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAll: retorna lista vacia si no hay peliculas")
    void getAll_deberiaRetornarListaVaciaSiNoHayPeliculas() {
        when(peliculasRepository.findAll()).thenReturn(Collections.emptyList());

        List<Pelicula> resultado = peliculasService.getAll();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }
    @Test
    @DisplayName("findById: retorna pelicula cuando existe el ID")
    void findById_deberiaRetornarPeliculaSiExiste() {
        Pelicula pelicula = new Pelicula("p1", "Inception", "Nolan", null, null);
        when(peliculasRepository.findById("p1")).thenReturn(pelicula);

        Pelicula resultado = peliculasService.findById("p1");

        assertNotNull(resultado);
        assertEquals("p1", resultado.getId());
        assertEquals("Inception", resultado.getTitulo());
        verify(peliculasRepository, times(1)).findById("p1");
    }

    @Test
    @DisplayName("findById: retorna null cuando no existe el ID")
    void findById_deberiaRetornarNullSiNoExiste() {
        when(peliculasRepository.findById("noExiste")).thenReturn(null);

        Pelicula resultado = peliculasService.findById("noExiste");

        assertNull(resultado);
        verify(peliculasRepository, times(1)).findById("noExiste");
    }
    @Test
    @DisplayName("filter: filtra por titulo (case-insensitive)")
    void filter_deberiaFiltrarPorTitulo() {
        List<Pelicula> peliculas = Arrays.asList(
                new Pelicula("p1", "Inception", "Nolan", null, null),
                new Pelicula("p2", "Matrix", "Wachowski", null, null),
                new Pelicula("p3", "Interstellar", "Nolan", null, null)
        );
        when(peliculasRepository.findAll()).thenReturn(peliculas);

        List<Pelicula> resultado = peliculasService.filter("inter");

        assertEquals(1, resultado.size());
        assertEquals("Interstellar", resultado.get(0).getTitulo());
    }

    @Test
    @DisplayName("filter: filtra por ID (case-insensitive)")
    void filter_deberiaFiltrarPorId() {
        List<Pelicula> peliculas = Arrays.asList(
                new Pelicula("inception-01", "Inception", "Nolan", null, null),
                new Pelicula("matrix-02", "Matrix", "Wachowski", null, null)
        );
        when(peliculasRepository.findAll()).thenReturn(peliculas);

        List<Pelicula> resultado = peliculasService.filter("MATRIX");

        assertEquals(1, resultado.size());
        assertEquals("matrix-02", resultado.get(0).getId());
    }

    @Test
    @DisplayName("filter: retorna multiples resultados cuando el query coincide con varios")
    void filter_deberiaRetornarMultiplesResultados() {
        List<Pelicula> peliculas = Arrays.asList(
                new Pelicula("p1", "Inception", "Nolan", null, null),
                new Pelicula("p2", "Matrix", "Wachowski", null, null),
                new Pelicula("p3", "Interstellar", "Nolan", null, null)
        );
        when(peliculasRepository.findAll()).thenReturn(peliculas);

        List<Pelicula> resultado = peliculasService.filter("in");

        // "Inception" tiene "in", "Interstellar" tiene "in"
        assertEquals(2, resultado.size());
    }

    @Test
    @DisplayName("filter: retorna lista vacia si no hay coincidencias")
    void filter_deberiaRetornarListaVaciaSiNoHayCoincidencias() {
        List<Pelicula> peliculas = Arrays.asList(
                new Pelicula("p1", "Inception", "Nolan", null, null),
                new Pelicula("p2", "Matrix", "Wachowski", null, null)
        );
        when(peliculasRepository.findAll()).thenReturn(peliculas);

        List<Pelicula> resultado = peliculasService.filter("zzznomatch");

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("filter: retorna lista vacia si no hay peliculas")
    void filter_deberiaRetornarListaVaciaSiNoHayPeliculas() {
        when(peliculasRepository.findAll()).thenReturn(Collections.emptyList());

        List<Pelicula> resultado = peliculasService.filter("inception");

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("filter: query coincide tanto con titulo como con id en distintas peliculas")
    void filter_deberiaFiltrarPorTituloYPorId() {
        List<Pelicula> peliculas = Arrays.asList(
                new Pelicula("matrix-id", "Otra", "Director", null, null),
                new Pelicula("otro-id", "Matrix", "Wachowski", null, null)
        );
        when(peliculasRepository.findAll()).thenReturn(peliculas);

        List<Pelicula> resultado = peliculasService.filter("matrix");

        assertEquals(2, resultado.size());
    }
}