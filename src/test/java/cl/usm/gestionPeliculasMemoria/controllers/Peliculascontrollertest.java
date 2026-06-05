package cl.usm.gestionPeliculasMemoria.controllers;

import cl.usm.gestionPeliculasMemoria.entities.Comentario;
import cl.usm.gestionPeliculasMemoria.entities.Pelicula;
import cl.usm.gestionPeliculasMemoria.services.PeliculasService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de PeliculasController")
class PeliculasControllerTest {

    @Mock
    private PeliculasService peliculasService;

    @InjectMocks
    private PeliculasController peliculasController;

    // ───────────── GET /peliculas (sin query) ─────────────

    @Test
    @DisplayName("getAll: retorna 200 con lista de peliculas")
    void getAll_sinQuery_deberiaRetornar200ConLista() {
        List<Pelicula> peliculas = Arrays.asList(
                new Pelicula("p1", "Inception", "Nolan", "tok1", null),
                new Pelicula("p2", "Matrix", "Wachowski", "tok2", null)
        );
        when(peliculasService.getAll()).thenReturn(peliculas);

        ResponseEntity<List<Pelicula>> response = peliculasController.getAll(null);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("p1", response.getBody().get(0).getId());
        verify(peliculasService, times(1)).getAll();
        verify(peliculasService, never()).filter(any());
    }

    @Test
    @DisplayName("getAll: retorna 200 con lista vacia")
    void getAll_sinQuery_deberiaRetornar200ConListaVacia() {
        when(peliculasService.getAll()).thenReturn(Collections.emptyList());

        ResponseEntity<List<Pelicula>> response = peliculasController.getAll(null);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("getAll: retorna 500 cuando el servicio lanza excepcion")
    void getAll_sinQuery_deberiaRetornar500SiServicioFalla() {
        when(peliculasService.getAll()).thenThrow(new RuntimeException("Error interno"));

        ResponseEntity<List<Pelicula>> response = peliculasController.getAll(null);

        assertEquals(500, response.getStatusCode().value());
    }

    // ───────────── GET /peliculas?q=... ─────────────

    @Test
    @DisplayName("getAll: con query llama a filter y retorna 200")
    void getAll_conQuery_deberiaFiltrarYRetornar200() {
        List<Pelicula> filtradas = List.of(
                new Pelicula("p1", "Inception", "Nolan", "tok1", null)
        );
        when(peliculasService.filter("inception")).thenReturn(filtradas);

        ResponseEntity<List<Pelicula>> response = peliculasController.getAll("inception");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals("Inception", response.getBody().get(0).getTitulo());
        verify(peliculasService, times(1)).filter("inception");
        verify(peliculasService, never()).getAll();
    }

    @Test
    @DisplayName("getAll: con query vacio llama a getAll")
    void getAll_conQueryVacio_deberiaLlamarGetAll() {
        when(peliculasService.getAll()).thenReturn(Collections.emptyList());

        ResponseEntity<List<Pelicula>> response = peliculasController.getAll("");

        assertEquals(200, response.getStatusCode().value());
        verify(peliculasService, times(1)).getAll();
        verify(peliculasService, never()).filter(any());
    }

    @Test
    @DisplayName("getAll: con query retorna lista vacia si no hay coincidencias")
    void getAll_conQuery_deberiaRetornarListaVaciaSiNoHayCoincidencias() {
        when(peliculasService.filter("zzz")).thenReturn(Collections.emptyList());

        ResponseEntity<List<Pelicula>> response = peliculasController.getAll("zzz");

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isEmpty());
    }

    // ───────────── POST /peliculas ─────────────

    @Test
    @DisplayName("createPelicula: retorna 200 con pelicula creada")
    void createPelicula_deberiaRetornar200ConPeliculaCreada() {
        Pelicula input = new Pelicula("p1", "Inception", "Nolan", null, null);
        Pelicula output = new Pelicula("p1", "Inception", "Nolan", "tokenABC123", null);
        when(peliculasService.createPelicula(any(Pelicula.class))).thenReturn(output);

        ResponseEntity<?> response = peliculasController.createPelicula(input);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        Pelicula body = (Pelicula) response.getBody();
        assertEquals("p1", body.getId());
        assertEquals("tokenABC123", body.getTokenDescarga());
        verify(peliculasService, times(1)).createPelicula(input);
    }

    @Test
    @DisplayName("createPelicula: retorna 500 cuando servicio retorna null")
    void createPelicula_deberiaRetornar500SiServicioRetornaNulo() {
        Pelicula input = new Pelicula("p1", "Inception", "Nolan", null, null);
        when(peliculasService.createPelicula(any(Pelicula.class))).thenReturn(null);

        ResponseEntity<?> response = peliculasController.createPelicula(input);

        assertEquals(500, response.getStatusCode().value());
    }

    // ───────────── GET /peliculas/{id} ─────────────

    @Test
    @DisplayName("findById: retorna 200 con pelicula encontrada")
    void findById_deberiaRetornar200ConPelicula() {
        Pelicula pelicula = new Pelicula("p1", "Inception", "Nolan", "tok1", null);
        when(peliculasService.findById("p1")).thenReturn(pelicula);

        ResponseEntity<Pelicula> response = peliculasController.findById("p1");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("p1", response.getBody().getId());
        assertEquals("Inception", response.getBody().getTitulo());
    }

    @Test
    @DisplayName("findById: retorna 404 si la pelicula no existe")
    void findById_deberiaRetornar404SiNoExiste() {
        when(peliculasService.findById("noExiste")).thenReturn(null);

        ResponseEntity<Pelicula> response = peliculasController.findById("noExiste");

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    @DisplayName("findById: retorna 500 cuando el servicio lanza excepcion")
    void findById_deberiaRetornar500SiServicioFalla() {
        when(peliculasService.findById(any())).thenThrow(new RuntimeException("Error"));

        ResponseEntity<Pelicula> response = peliculasController.findById("p1");

        assertEquals(500, response.getStatusCode().value());
    }

    // ───────────── GET /peliculas/{id}/comentarios ─────────────

    @Test
    @DisplayName("getComentarios: retorna 200 con array de comentarios")
    void getComentarios_deberiaRetornar200ConComentarios() {
        Comentario[] comentarios = {
                new Comentario("usuario1", "Excelente"),
                new Comentario("usuario2", "Muy buena")
        };
        Pelicula pelicula = new Pelicula("p1", "Inception", "Nolan", "tok1", comentarios);
        when(peliculasService.findById("p1")).thenReturn(pelicula);

        ResponseEntity<?> response = peliculasController.getComentarios("p1");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        Comentario[] body = (Comentario[]) response.getBody();
        assertEquals(2, body.length);
        assertEquals("usuario1", body[0].getUsuario());
        assertEquals("Excelente", body[0].getComentario());
    }

    @Test
    @DisplayName("getComentarios: retorna 200 con null si no tiene comentarios")
    void getComentarios_deberiaRetornar200SiSinComentarios() {
        Pelicula pelicula = new Pelicula("p1", "Inception", "Nolan", "tok1", null);
        when(peliculasService.findById("p1")).thenReturn(pelicula);

        ResponseEntity<?> response = peliculasController.getComentarios("p1");

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @DisplayName("getComentarios: retorna 404 si la pelicula no existe")
    void getComentarios_deberiaRetornar404SiPeliculaNoExiste() {
        when(peliculasService.findById("noExiste")).thenReturn(null);

        ResponseEntity<?> response = peliculasController.getComentarios("noExiste");

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    @DisplayName("getComentarios: retorna 500 cuando el servicio lanza excepcion")
    void getComentarios_deberiaRetornar500SiServicioFalla() {
        when(peliculasService.findById(any())).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = peliculasController.getComentarios("p1");

        assertEquals(500, response.getStatusCode().value());
    }
}