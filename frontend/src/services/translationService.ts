import {Callback, InitOptions, TranslationOptions} from 'i18next';
import i18n from '../i18n/i18n';

export const translate = (key: string, options?: TranslationOptions): string =>
  i18n.t(key, options);

export const onTranslationInitialized = (callback: (options: InitOptions) => void) =>
  i18n.on('initialized', callback);

export const changeTranslationLanguage = (language: string, callback?: Callback) =>
  i18n.changeLanguage(language, callback);

export const firstUpper = (original: string): string =>
  original.length === 0
    ? original
    : original[0].toUpperCase() + original.substr(1);

export const firstUpperTranslated = (key: string, options?: TranslationOptions): string =>
  firstUpper(translate(key, options));

export const getI18nLanguage = (): string => i18n.language;
