import {firstUpperTranslated, translate} from '../services/translationService';
import {GatewayDataSummaryKey} from '../state/domain-models-paginated/gateway/gatewayModels';
import {MeterDataSummaryKey} from '../state/domain-models-paginated/meter/meterModels';
import {ParameterName} from '../state/user-selection/userSelectionModels';
import {Status} from '../types/Types';
import {texts} from './texts';

export const orUnknown = (name: string) => name === 'unknown' ? translate('unknown') : name;

export const getTranslationOrName = (name: string, domainModelName: ParameterName): string => {
  switch (domainModelName) {
    case ParameterName.cities:
    case ParameterName.addresses:
      return orUnknown(name);
    default:
      return name;
  }
};

export const statusTranslation = (name: string): string => {
  const statuses = {
    [Status.ok]: translate('ok'),
    [Status.error]: translate('error'),
    [Status.unknown]: translate('unknown'),
    [Status.warning]: translate('warning'),
  };
  return statuses[name] || statuses[Status.unknown];
};

const flaggedTranslation = (text: string): string => {
  const texts = {
    flagged: translate('flagged'),
    unFlagged: translate('unFlagged'),
  };
  return texts[text] || text;
};

export const translatedErrorMessage = (message: string): string => {
  if (message === texts.invalidToken) {
    return firstUpperTranslated('token missing or invalid');
  } else if (message === texts.badCredentials) {
    return firstUpperTranslated('bad credentials');
  } else {
    return message;
  }
};

type FieldKey = MeterDataSummaryKey | GatewayDataSummaryKey;

export const pieChartTranslation = (fieldKey: FieldKey, name: string): string => {
  switch (fieldKey) {
    case 'flagged':
      return flaggedTranslation(name);
    case 'status':
      return statusTranslation(name);
    default:
      return name;
  }
};
