package es.e1sordo.lingualeap.services.impl;

import es.e1sordo.lingualeap.dto.UpsertVocabularyListRequestDto;
import es.e1sordo.lingualeap.enums.PartOfSpeech;
import es.e1sordo.lingualeap.models.VocabularyList;
import es.e1sordo.lingualeap.repositories.VocabularyListsRepository;
import es.e1sordo.lingualeap.services.VocabularyListsService;
import es.e1sordo.lingualeap.services.WordMeaningsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VocabularyListsServiceImpl implements VocabularyListsService {

    private final VocabularyListsRepository repository;
    private final WordMeaningsService wordMeaningsService;

    @Override
    public List<VocabularyList> getAll() {
        return repository.findAll();
    }

    @Override
    public VocabularyList getBy(final Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("VocabularyList not found"));
    }

    @Override
    public VocabularyList getByPos(final String posStr) {
        final var pos = PartOfSpeech.valueOf(posStr.toUpperCase());
        return new VocabularyList(
                0L,
                pos.name(),
                true,
                new HashSet<>(wordMeaningsService.getAllByPos(pos)),
                LocalDate.now()
        );
    }

    @Override
    public VocabularyList getSmartListOfRecentlyAdded() {
        return getSmartListByName("New words");
    }

    @Override
    public VocabularyList getSmartListOfProblemWords() {
        return getSmartListByName("Problem words");
    }

    private VocabularyList getSmartListByName(String name) {
        return repository.findByNameAndSmartIsTrue(name).orElseGet(
                () -> repository.save(new VocabularyList(null, name, true, Collections.emptySet(), LocalDate.now())));
    }

    @Override
    public VocabularyList upsertList(final UpsertVocabularyListRequestDto request) {
        final var list = (request.id() != null && request.id() > 0)
                ? repository.findById(request.id()).get()
                : new VocabularyList();

        if (request.name().isBlank()) throw new RuntimeException("Name of List cannot be null!");
        list.setName(request.name());

        if (list.getId() == null) {
            list.setAdded(LocalDate.now());
        }

        return repository.save(list);
    }

    @Override
    public void delete(final Long id) {
        final var list = repository.findById(id).orElseThrow(() -> new RuntimeException("VocabularyList not found"));
        if (list.isSmart()) throw new RuntimeException("Cannot delete smart list!");
        repository.deleteById(id);
    }
}
