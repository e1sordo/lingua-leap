import dayjs from 'dayjs';

export function convertDateToSinceString(date: Date): string {
    const today = dayjs();
    const parsedDate = dayjs(date);

    if (parsedDate.isSame(today, 'day')) {
        return 'Сегодня';
    } else if (parsedDate.isSame(today.subtract(1, 'day'), 'day')) {
        return 'Вчера';
    } else if (parsedDate.isSame(today.subtract(2, 'day'), 'day')) {
        return 'Позавчера';
    } else if (parsedDate.isSame(today, 'week')) {
        return 'На этой неделе';
    } else if (parsedDate.isSame(today.subtract(1, 'week'), 'week')) {
        return 'На прошлой неделе';
    } else if (parsedDate.isSame(today, 'month')) {
        return 'В этом месяце';
    } else if (parsedDate.isSame(today.subtract(1, 'month'), 'month')) {
        return 'В прошлом месяце';
    } else {
        return parsedDate.format('DD.MM.YYYY');
    }
}
