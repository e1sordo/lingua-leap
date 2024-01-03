package es.e1sordo.lingualeap.repositories.projections;

import java.time.LocalDate;

public record CustomQueryResult(LocalDate date, Long count) {
}
