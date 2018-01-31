export interface LanguageState {
  language: Language;
}

type SupportedLanguages = 'sv' | 'en';

export interface Language {
  name: string;
  code: string;
}

export const supportedLanguages: Readonly<Record<SupportedLanguages, Language>> = {
  sv: {
    code: 'sv',
    name: 'Swedish',
  },
  en: {
    code: 'en',
    name: 'English',
  },
};
