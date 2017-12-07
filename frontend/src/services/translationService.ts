import {InitOptions, TranslationOptions} from 'i18next';
import i18n from '../i18n/i18n';
import {MeterStatus} from '../state/domain-models/meter/meterModels';
import {ParameterName} from '../state/search/selection/selectionModels';
import {IdNamed} from '../types/Types';
import {DataOverviewKey} from '../usecases/validation/components/validationOverviewHelper';

export const translate = (key: string, options?: TranslationOptions) => i18n.t(key, options);

export const onTranslationInitialized = (callback: (options: InitOptions) => void) =>
 i18n.on('initialized', callback);

export const changeTranslationLanguage = (language: string, callback?: () => any) =>
  i18n.changeLanguage(language, callback);

// TODO: Write tests on this.
export const getTranslationOrName = (idName: IdNamed, domainModelName: ParameterName): string => {
  switch (domainModelName) {
    case ParameterName.meterStatuses:
      return meterStatusTranslation(idName);
    default:
      return idName.name;
  }
};

export const meterStatusTranslation = ({id, name}: IdNamed): string => {
  const statuses = {
    [MeterStatus.ok]: translate('ok'),
    [MeterStatus.alarm]: translate('alarm'),
    [MeterStatus.unknown]: translate('unknown'),
  };
  return statuses[id] || name;
};

const flaggedTranslation = (text: string): string => {
  const texts = {
    flagged: translate('flagged'),
    unFlagged: translate('unFlagged'),
  };
  return texts[text] || text;
};

export const pieChartTranslation = (fieldKey: DataOverviewKey, toBeTranslated: IdNamed): string | number => {
  switch (fieldKey) {
    case 'flagged':
      return flaggedTranslation(toBeTranslated.name);
    case 'status':
      return meterStatusTranslation(toBeTranslated);
    default:
      return toBeTranslated.name;
  }
};
