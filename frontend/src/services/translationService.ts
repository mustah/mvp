import {InitOptions, TranslationOptions} from 'i18next';
import i18n from '../i18n/i18n';
import {ParameterName} from '../state/search/selection/selectionModels';
import {IdNamed} from '../types/Types';
import {DataOverviewKey} from '../usecases/validation/components/validationOverviewHelper';
import {MeterStatus} from '../state/domain-models/meter/meterModels';

export const translate = (key: string, options?: TranslationOptions) => i18n.t(key, options);

export const onTranslationInitialized = (callback: (options: InitOptions) => any) => {
  i18n.on('initialized', callback);
};

export const changeTranslationLanguage = (language: string, callback?: () => any) =>
  i18n.changeLanguage(language, callback);

// TODO: Write tests on this.
export const getNameTranslation = (idName: IdNamed, domainModelName: ParameterName): string => {
  switch (domainModelName) {
    case ParameterName.meterStatuses:
      return meterStatusTranslate(idName);
    default:
      return idName.name;
  }
};

const meterStatusTranslate = (idName: IdNamed): string => {
  const statuses = {
    [MeterStatus.ok]: translate('ok'),
    [MeterStatus.alarm]: translate('alarm'),
    [MeterStatus.unknown]: translate('unknown'),
  };
  return statuses[idName.id] || idName.name;
};

const flagged = (text: string) => {
  const texts = {
    flagged: translate('flagged'),
    unFlagged: translate('unFlagged'),
  };
  return texts[text] || text;
};

export const pieChartTranslation = (fieldKey: DataOverviewKey, toBeTranslated: IdNamed): string | number => {
  switch (fieldKey) {
    case 'flagged':
      return flagged(toBeTranslated.name);
    case 'status':
      return meterStatusTranslate(toBeTranslated);
    default:
      return toBeTranslated.name;
  }
};
