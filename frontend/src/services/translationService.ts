import {default as i18next} from 'i18next';
import i18n from '../i18n/i18n';

export const translate = (key: string, options?: i18next.TOptions): string =>
  i18n.t(key, options);

export const onTranslationInitialized = (callback: (options: i18next.InitOptions) => void) =>
  i18n.on('initialized', callback);

export const changeTranslationLanguage = async (language: string, callback?: i18next.Callback) => {
  await i18n.changeLanguage(language, callback);
};

export const firstUpper = (original: string): string =>
  original.length === 0
    ? original
    : original[0].toUpperCase() + original.substr(1);

export const capitalized = (original: string): string =>
  original.split(' ').map(firstUpper).join(' ');

export const firstUpperTranslated = (key: string, options?: i18next.TOptions): string =>
  firstUpper(translate(key, options));

export const getI18nLanguage = (): string => i18n.language;
