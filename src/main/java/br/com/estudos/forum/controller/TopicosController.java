package br.com.estudos.forum.controller;

import br.com.estudos.forum.controller.DTO.TopicoDTO;
import br.com.estudos.forum.controller.form.AtualizacaoTopicoForm;
import br.com.estudos.forum.controller.form.TopicoForm;
import br.com.estudos.forum.modelo.Topico;
import br.com.estudos.forum.repository.CursoRepository;
import br.com.estudos.forum.repository.TopicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.data.domain.Pageable;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/topicos")
public class TopicosController {
    @Autowired
    private TopicoRepository topicoRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @GetMapping
    @Cacheable(value = "listadeTopicos") //Armazena os resultados dessa query em memória para optimizar processos
    public Page<TopicoDTO> lista(@RequestParam(required = false) String nomeCurso,
                                 @PageableDefault(sort="id", direction = Sort.Direction.DESC, page =0, size=10) Pageable pagina){

        if(nomeCurso != null){
            Page<Topico> topicos = topicoRepository.findByCursoNome(nomeCurso, pagina);
           return TopicoDTO.converter(topicos);
        }
        Page<Topico> topicos = topicoRepository.findAll(pagina);
        return TopicoDTO.converter(topicos);
    }

    @PostMapping
    @CacheEvict(value = "listadeTopicos", allEntries = true)
    public ResponseEntity<TopicoDTO> cadastrar (@RequestBody @Valid TopicoForm form, UriComponentsBuilder uriBuilder){
        Topico topico = form.converter(cursoRepository);
        topicoRepository.save(topico);
        URI uri = uriBuilder.path("/topicos/{id}").buildAndExpand(topico.getId()).toUri();
        return ResponseEntity.created(uri).body(new TopicoDTO(topico));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TopicoDTO> detalhar(@PathVariable Long id){
        Optional<Topico> topico = topicoRepository.findById(id);
        if(topico.isPresent()){
            return ResponseEntity.ok(new TopicoDTO(topico.get()));
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @Transactional
    @CacheEvict(value = "listadeTopicos", allEntries = true)
    public ResponseEntity<TopicoDTO> atualizar(@PathVariable Long id, @RequestBody @Valid AtualizacaoTopicoForm form){
        Topico topico = form.atualizar(id, topicoRepository);

        return ResponseEntity.ok(new TopicoDTO(topico));
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = "listadeTopicos", allEntries = true)
    public ResponseEntity<?> remover(@PathVariable Long id){
        topicoRepository.deleteById(id);

        return ResponseEntity.ok().build();
    }

}
