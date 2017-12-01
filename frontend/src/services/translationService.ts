import {InitOptions, TranslationOptions} from 'i18next';
import i18n from '../i18n/i18n';
import {ParameterName} from '../state/search/selection/selectionModels';
import {IdNamed} from '../types/Types';

export const translate = (key: string, options?: TranslationOptions) => i18n.t(key, options);

export const onTranslationInitialized = (callback: (options: InitOptions) => any) => {
  i18n.on('initialized', callback);
};

export const changeTranslationLanguage = (language: string, callback?: () => any) =>
  i18n.changeLanguage(language, callback);

// TODO: Write tests on this.
export const getNameTranslation = (idName: IdNamed, domainModelName: ParameterName) => {
  switch (domainModelName) {
    case ParameterName.meterStatuses:
      return meterStatus(idName);
    default:
      return idName.name;
  }
};

const meterStatus = (idName: IdNamed) => {
  const statuses = {
    0: translate('ok'),
    3: translate('alarm'),
    4: translate('unknown'),
  };
  return statuses[idName.id] || idName.name;
};
