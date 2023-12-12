import i18n from '@/i18n';

interface PartOfSpeechMeta {
    [key: string]: PartOfSpeechMetaItem
}

interface PartOfSpeechMetaItem {
    label: string;
    labelEsp: string;
    definition: string;
    printOrder: number;
    bgClass: string;
    textColor: string;
}

export const partOfSpeechMeta: PartOfSpeechMeta = {
    NOUN: {
        label: 'Существительное',
        labelEsp: 'Sustantivo',
        definition: '',
        printOrder: 3,
        bgClass: '',
        textColor: 'rgb(255, 223, 223)'
    },
    VERB: {
        label: 'Глагол',
        labelEsp: 'Verbo',
        definition: '',
        printOrder: 2,
        bgClass: '',
        textColor: 'rgb(250, 253, 255)'
    },
    ADJECTIVE: {
        label: 'Прилагательное',
        labelEsp: 'Adjetivo',
        definition: '',
        printOrder: 4,
        bgClass: '',
        textColor: '#ffffff'
    },
    ADVERB: {
        label: 'Наречие',
        labelEsp: 'Adverbio',
        definition: '',
        printOrder: 5,
        bgClass: '',
        textColor: '#ffffff'
    },
    PRONOUN: {
        label: 'Местоимение',
        labelEsp: 'Pronombre',
        definition: '',
        printOrder: 6,
        bgClass: '',
        textColor: '#ffffff'
    },
    PREPOSITION: {
        label: 'Предлог',
        labelEsp: 'Preposición',
        definition: '',
        printOrder: 7,
        bgClass: '',
        textColor: '#ffffff'
    },
    CONJUNCTION: {
        label: 'Союз',
        labelEsp: 'Conjunción',
        definition: '',
        printOrder: 8,
        bgClass: '',
        textColor: '#ffffff'
    },
    INTERJECTION: {
        label: 'Междометие',
        labelEsp: 'Interjección',
        definition: '',
        printOrder: 9,
        bgClass: '',
        textColor: 'rgb(255, 223, 223)'
    },
    DETERMINER: {
        label: 'Определитель',
        labelEsp: 'Определитель',
        definition: '',
        printOrder: 10,
        bgClass: '',
        textColor: 'rgb(250, 253, 255)'
    },
    PHRASE: {
        label: 'Фраза',
        labelEsp: 'Locución',
        definition: '',
        printOrder: 1,
        bgClass: '',
        textColor: ''
    }
};

export function formatTime(minutes: number): string {
    const { t } = i18n.global;

    const h = t("time.shortHour");
    const m = t("time.shortMinute");

    if (minutes < 60) {
        return `${minutes}${m}`;
    }

    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;

    if (remainingMinutes === 0) {
        return `${hours}${h}`;
    } else if (remainingMinutes % 10 === 0) {
        return `${hours}${h} ${remainingMinutes}${m}`;
    } else {
        return `${hours}${h} ${remainingMinutes}${m}`;
    }
}
