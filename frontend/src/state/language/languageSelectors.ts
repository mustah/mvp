import {Language, languages} from './languageModels';

export const getLanguages = (): Language[] => Object.keys(languages)
  .map((languageCode) => languages[languageCode])
  .map(({code, name}) => ({code, name: name()}));
