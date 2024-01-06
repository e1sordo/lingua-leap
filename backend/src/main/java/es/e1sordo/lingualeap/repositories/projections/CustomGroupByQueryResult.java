package es.e1sordo.lingualeap.repositories.projections;

import java.time.LocalDate;

public record CustomGroupByQueryResult(LocalDate date, Long count) {
}
