/**
 * This code was taken from Github user MOUL and manually translated from Go to Javascript.
 * Original source: https://github.com/moul/number-to-words/blob/master/es-es.go
 */

const spanishMegasSingular = ["", "mil", "millón", "mil millones", "billón"];
const spanishMegasPlural = ["", "mil", "millones", "mil millones", "billones"];
const spanishUnitsAdjectives = ["", "un", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nove"];
const spanishUnits = ["", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve"];
const spanishHundreds = ["", "ciento", "doscientos", "trescientos", "cuatrocientos", "quinientos", "seiscientos", "setecientos", "ochocientos", "novecientos"];
const spanishTens = ["", "diez", "veinte", "treinta", "cuarenta", "cincuenta", "sesenta", "setenta", "ochenta", "noventa"];
const spanishTeens = ["diez", "once", "doce", "trece", "catorce", "quince", "dieciséis", "diecisiete", "dieciocho", "diecinueve"];
const spanishTwenties = ["veinte", "veintiuno", "veintidós", "veintitrés", "veinticuatro", "veinticinco", "veintiséis", "veintisiete", "veintiocho", "veintinueve"];

function integerToTriplets(input: number): number[] {
    const result = [];
    while (input > 0) {
        result.push(input % 1000);
        input = Math.floor(input / 1000);
    }
    return result;
}

function tripletEnd(triplet: number, idx: number, words: string[]): string[] {
    switch (triplet) {
        case 0:
            break;
        case 1:
            if (spanishMegasSingular[idx] !== "") {
                words.push(spanishMegasSingular[idx]);
            }
            break;
        default:
            if (spanishMegasPlural[idx] !== "") {
                words.push(spanishMegasPlural[idx]);
            }
            break;
    }
    return words;
}

export function numberToWords(input: number): string {
    // Log the input
    console.log("Input:", input);
    let words = [];

    if (input < 0) {
        words.push("menos");
        input *= -1;
    }

    // Split integer into triplets
    const triplets = integerToTriplets(input);
    // Log the triplets
    console.log("Triplets:", triplets);

    // Special cases
    switch (true) {
        case triplets.length === 0:
            return "cero";
        case input === 1000:
            words.push("mil");
            return words.join(" ");
        case input === 1:
            words.push("uno");
            return words.join(" ");
    }

    // Iterate over triplets
    for (let idx = triplets.length - 1; idx >= 0; idx--) {
        const triplet = triplets[idx];
        // Log the triplet
        console.log(`Triplet: ${triplet}, (idx=${idx})`);

        // Nothing to do for an empty triplet
        if (triplet === 0) {
            continue;
        }

        // Three-digits
        const hundreds = Math.floor(triplet / 100) % 10;
        const tens = Math.floor(triplet / 10) % 10;
        const units = triplet % 10;
        // Log the hundreds, tens, and units
        console.log(`Hundreds: ${hundreds}, Tens: ${tens}, Units: ${units}`);

        if (hundreds > 0) {
            words.push(spanishHundreds[hundreds]);
        }

        if (tens !== 0 || units !== 0) {
            switch (tens) {
                case 0:
                    if (idx > 0) {
                        words.push(spanishUnitsAdjectives[units]);
                    } else {
                        words.push(spanishUnits[units]);
                    }
                    break;
                case 1:
                    words.push(spanishTeens[units]);
                    break;
                case 2:
                    if (idx > 0 && units === 1) {
                        words.push("veintiún");
                    } else {
                        words.push(spanishTwenties[units]);
                    }
                    break;
                default:
                    if (units > 0) {
                        const word = `${spanishTens[tens]} y ${spanishUnits[units]}`;
                        words.push(word);
                    } else {
                        words.push(spanishTens[tens]);
                    }
                    break;
            }
        }

        words = tripletEnd(triplet, idx, words);
    }

    // Log the words length
    console.log("Words length:", words.length);
    return words.join(" ").replace("uno mil", "un mil");
}
