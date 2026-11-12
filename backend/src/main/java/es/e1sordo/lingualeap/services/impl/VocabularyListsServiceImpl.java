package es.e1sordo.lingualeap.services.impl;

import es.e1sordo.lingualeap.dto.UpsertVocabularyListRequestDto;
import es.e1sordo.lingualeap.enums.PartOfSpeech;
import es.e1sordo.lingualeap.enums.SmartListType;
import es.e1sordo.lingualeap.models.ForeignWord;
import es.e1sordo.lingualeap.models.VocabularyList;
import es.e1sordo.lingualeap.models.WordMeaning;
import es.e1sordo.lingualeap.models.WordMeaningTranslation;
import es.e1sordo.lingualeap.repositories.VocabularyListsRepository;
import es.e1sordo.lingualeap.services.LanguageCoursesService;
import es.e1sordo.lingualeap.services.NotFoundException;
import es.e1sordo.lingualeap.services.VocabularyListsService;
import es.e1sordo.lingualeap.services.WordMeaningsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

@Service
@RequiredArgsConstructor
public class VocabularyListsServiceImpl implements VocabularyListsService {

    private final VocabularyListsRepository repository;
    private final WordMeaningsService wordMeaningsService;
    private final LanguageCoursesService languageCoursesService;

    @Override
    public List<VocabularyList> getAll(final Long courseId) {
        return repository.findAllByCourseId(courseId);
    }

    @Override
    public VocabularyList getBy(final Long courseId, final Long id) {
        return repository.findByCourseIdAndId(courseId, id)
                .orElseThrow(() -> new NotFoundException("VocabularyList not found: " + id));
    }

    @Override
    public VocabularyList getByPos(final Long courseId, final String posStr) {
        final var pos = PartOfSpeech.valueOf(posStr.toUpperCase());
        final var list = new VocabularyList();
        list.setId(0L);
        list.setName(pos.name());
        list.setSmart(true);
        list.setCourse(languageCoursesService.getCourseById(courseId));
        list.setMeanings(new HashSet<>(wordMeaningsService.getAllByPos(courseId, pos)));
        list.setAdded(LocalDate.now());
        return list;
    }

    @Override
    public VocabularyList getByDate(final Long courseId, final LocalDate date) {
        final var course = languageCoursesService.getCourseById(courseId);
        final List<WordMeaning> allMeanings = wordMeaningsService.getAllByDate(courseId, date);

        final Set<WordMeaning> allMeaningsWithCollocations = new HashSet<>(allMeanings);

        for (WordMeaning meaning : allMeanings) {
            if (meaning.getCollocations().isEmpty()) continue;
            final var collocationsAsMeanings = meaning.getCollocations().stream()
                    .map(collocation -> {
                        final var collocationMeaning = new WordMeaning();
                        final var collocationWord = new ForeignWord();
                        collocationWord.setId(meaning.getWord().getId());
                        collocationWord.setWord(collocation.getResolvedPattern());
                        collocationWord.setAdded(meaning.getWord().getAdded());
                        collocationWord.setCourse(course);

                        collocationMeaning.setId(collocation.getId());
                        collocationMeaning.setWord(collocationWord);
                        collocationMeaning.setPos(PartOfSpeech.PHRASE);
                        collocationMeaning.setGender(meaning.getGender());
                        collocationMeaning.setImageUrl(meaning.getImageUrl());
                        collocationMeaning.setTranslations(collocation.getTranslations().stream()
                                .map(translation -> new WordMeaningTranslation(
                                        collocationMeaning,
                                        translation.getLanguage(),
                                        translation.getText()))
                                .toList());
                        collocationMeaning.setDefinition(meaning.getDefinition());
                        collocationMeaning.setFrequency(meaning.getFrequency());
                        collocationMeaning.setCollocations(emptyList());
                        collocationMeaning.setContexts(emptyList());
                        collocationMeaning.setLearningStatus(meaning.getLearningStatus());
                        collocationMeaning.setListsContaining(emptySet());
                        return collocationMeaning;
                    })
                    .toList();

            allMeaningsWithCollocations.addAll(collocationsAsMeanings);
        }

        final DateTimeFormatter dateFormatter = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.LONG)
                .localizedBy(Locale.forLanguageTag(
                        course.getTargetLanguage().getDefaultSpeechLocale().replace('_', '-')));

        final var list = new VocabularyList();
        list.setId(0L);
        list.setName(date.format(dateFormatter));
        list.setSmart(true);
        list.setCourse(course);
        list.setMeanings(allMeaningsWithCollocations);
        list.setAdded(LocalDate.now());
        return list;
    }

    @Override
    @Transactional
    public VocabularyList getSmartListOfRecentlyAdded(final Long courseId) {
        return getSmartListByType(courseId, SmartListType.RECENTLY_ADDED);
    }

    @Override
    @Transactional
    public VocabularyList getSmartListOfProblemWords(final Long courseId) {
        return getSmartListByType(courseId, SmartListType.PROBLEM_WORDS);
    }

    private VocabularyList getSmartListByType(final Long courseId, final SmartListType smartType) {
        return repository.findByCourseIdAndSmartType(courseId, smartType).orElseGet(() -> {
            final var list = new VocabularyList();
            list.setName(smartType == SmartListType.RECENTLY_ADDED ? "New words" : "Problem words");
            list.setSmart(true);
            list.setSmartType(smartType);
            list.setCourse(languageCoursesService.getCourseById(courseId));
            list.setAdded(LocalDate.now());
            return repository.save(list);
        });
    }

    @Override
    @Transactional
    public VocabularyList upsertList(final Long courseId, final UpsertVocabularyListRequestDto request) {
        final var list = (request.id() != null && request.id() > 0)
                ? getBy(courseId, request.id())
                : newListOfCourse(courseId);

        if (request.name().isBlank()) throw new IllegalArgumentException("Name of List cannot be null!");
        list.setName(request.name());

        if (list.getId() == null) {
            list.setAdded(LocalDate.now());
        }

        return repository.save(list);
    }

    private VocabularyList newListOfCourse(final Long courseId) {
        final var list = new VocabularyList();
        list.setCourse(languageCoursesService.getCourseById(courseId));
        list.setSmart(false);
        return list;
    }

    @Override
    @Transactional
    public void removeWordFromList(final Long courseId, final Long listId, final Long wordMeaningId) {
        final var list = getBy(courseId, listId);
        final var meaning = wordMeaningsService.getById(courseId, wordMeaningId);
        final boolean removed = list.getMeanings().removeIf(wordMeaning -> {
            if (Objects.equals(wordMeaning.getId(), meaning.getId())) {
                wordMeaning.getListsContaining().remove(list);
                return true;
            }
            return false;
        });
        if (!removed) throw new NotFoundException("WordMeaning not found in VocabularyList");
        repository.save(list);
    }

    @Override
    @Transactional
    public void delete(final Long courseId, final Long id) {
        final var list = getBy(courseId, id);
        if (list.isSmart()) throw new IllegalArgumentException("Cannot delete smart list!");
        repository.deleteById(id);
    }
}
