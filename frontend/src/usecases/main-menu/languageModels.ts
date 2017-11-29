export interface LanguageState {
  language: Language;
}

export interface Language {
  name: string;
  code: string;
}

export const supportedLanguages: {readonly [key: string]: Language} = {
  sv: {
    code: 'sv',
    name: 'Swedish',
  },
  en: {
    code: 'en',
    name: 'English',
  },
};
