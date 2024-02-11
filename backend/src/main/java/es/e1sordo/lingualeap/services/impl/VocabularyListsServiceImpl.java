package es.e1sordo.lingualeap.services.impl;

import es.e1sordo.lingualeap.dto.UpsertVocabularyListRequestDto;
import es.e1sordo.lingualeap.enums.PartOfSpeech;
import es.e1sordo.lingualeap.models.ForeignWord;
import es.e1sordo.lingualeap.models.VocabularyList;
import es.e1sordo.lingualeap.models.WordMeaning;
import es.e1sordo.lingualeap.repositories.VocabularyListsRepository;
import es.e1sordo.lingualeap.services.VocabularyListsService;
import es.e1sordo.lingualeap.services.WordMeaningsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

@Service
@RequiredArgsConstructor
public class VocabularyListsServiceImpl implements VocabularyListsService {

    public static final DateTimeFormatter SPANISH_DATE_FORMATTER = DateTimeFormatter
            .ofLocalizedDate(FormatStyle.LONG)
            .localizedBy(Locale.of("es", "ES"));

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
    public VocabularyList getByDate(final LocalDate date) {
        final List<WordMeaning> allMeanings = wordMeaningsService.getAllByDate(date);

        final Set<WordMeaning> allMeaningsWithCollocations = new HashSet<>(allMeanings);

        for (WordMeaning meaning : allMeanings) {
            if (meaning.getCollocations().isEmpty()) continue;
            final var collocationsAsMeanings = meaning.getCollocations().stream()
                    .map(collocation -> new WordMeaning(
                            collocation.getId(),
                            new ForeignWord(
                                    meaning.getWord().getId(),
                                    collocation.getResolvedPattern(false),
                                    meaning.getWord().getAdded(),
                                    emptyList()
                            ),
                            meaning.getPos(),
                            meaning.getGender(),
                            meaning.getImageUrl(),
                            collocation.getTranslationRussian(),
                            collocation.getTranslationEnglish(),
                            meaning.getDefinition(),
                            meaning.getFrequency(),
                            emptyList(),
                            emptyList(),
                            meaning.getLearningStatus(),
                            emptySet()
                    ))
                    .toList();

            allMeaningsWithCollocations.addAll(collocationsAsMeanings);
        }

        return new VocabularyList(
                0L,
                date.format(SPANISH_DATE_FORMATTER),
                true,
                allMeaningsWithCollocations,
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
                () -> repository.save(new VocabularyList(null, name, true, emptySet(), LocalDate.now())));
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
