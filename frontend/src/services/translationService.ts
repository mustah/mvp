import {InitOptions, TranslationOptions} from 'i18next';
import i18n from '../i18n/i18n';

export const translate = (key: string, options?: TranslationOptions) => i18n.t(key, options);

export const onTranslationInitialized = (callback: (options: InitOptions) => any) => {
  i18n.on('initialized', callback);
};

export const changeTranslationLanguage = (language: string, callback?: () => any) =>
  i18n.changeLanguage(language, callback);
