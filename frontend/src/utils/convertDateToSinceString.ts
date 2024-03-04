import dayjs from 'dayjs';

export function convertDateToSinceString(date: Date): string {
  const today = dayjs();
  const parsedDate = dayjs(date);

  if (parsedDate.isSame(today, 'day')) {
    return 'Hoy';
  } else if (parsedDate.isSame(today.subtract(1, 'day'), 'day')) {
    return 'Ayer';
  } else if (parsedDate.isSame(today.subtract(2, 'day'), 'day')) {
    return 'Anteayer';
  } else if (parsedDate.isSame(today, 'week')) {
    return 'Esta semana';
  } else if (parsedDate.isSame(today.subtract(1, 'week'), 'week')) {
    return 'Semana pasada';
  } else if (parsedDate.isSame(today, 'month')) {
    return 'Este mes';
  } else if (parsedDate.isSame(today.subtract(1, 'month'), 'month')) {
    return 'Mes pasado';
  } else {
    return parsedDate.format('D MMMM YYYY');
  }
}
