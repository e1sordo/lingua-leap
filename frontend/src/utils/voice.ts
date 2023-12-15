const synth = window.speechSynthesis;
const speech = new window.SpeechSynthesisUtterance();

export const voiceList = synth.getVoices().filter(voice => voice.lang.startsWith('es-')) as SpeechSynthesisVoice[];

const paulinaVoice = voiceList.find(voice => voice.name.startsWith('Paulina') || voice.name.startsWith('Паулина')) as SpeechSynthesisVoice;

export const speak = (num: number | string, selectedVoice: number = -1) => {
    speech.text = num.toString();
    if (selectedVoice >= 0) {
        speech.voice = voiceList[selectedVoice];
    } else {
        speech.voice = paulinaVoice;
    }
    speech.pitch = Math.random() * (1.5 - 0.7) + 0.7;
    speech.rate = Math.random() * (1.4 - 0.9) + 0.9;
    synth.speak(speech);
};
