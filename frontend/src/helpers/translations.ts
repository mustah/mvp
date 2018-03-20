import {translate} from '../services/translationService';
import {GatewayDataSummaryKey} from '../state/domain-models-paginated/gateway/gatewayModels';
import {MeterDataSummaryKey} from '../state/domain-models/meter-all/allMetersModels';
import {ParameterName} from '../state/search/selection/selectionModels';
import {IdNamed, Status} from '../types/Types';

// TODO: Write tests on this.
export const getTranslationOrName = (idName: IdNamed, domainModelName: ParameterName): string => {
  switch (domainModelName) {
    case ParameterName.meterStatuses:
    case ParameterName.gatewayStatuses:
      return statusTranslation(idName);
    default:
      return idName.name;
  }
};

export const statusTranslation = ({id}: IdNamed): string => {
  const statuses = {
    [Status.ok]: translate('ok'),
    [Status.alarm]: translate('alarm'),
    [Status.active]: translate('active'),
    [Status.critical]: translate('critical'),
    [Status.info]: translate('info'),
    [Status.unknown]: translate('unknown'),
    [Status.warning]: translate('warning'),
    [Status.maintenance_scheduled]: translate('maintenance scheduled'),
  };
  return statuses[id] || statuses[Status.unknown];
};

const flaggedTranslation = (text: string): string => {
  const texts = {
    flagged: translate('flagged'),
    unFlagged: translate('unFlagged'),
  };
  return texts[text] || text;
};

type FieldKey = MeterDataSummaryKey | GatewayDataSummaryKey;

export const pieChartTranslation = (fieldKey: FieldKey, toBeTranslated: IdNamed): string => {
  switch (fieldKey) {
    case 'flagged':
      return flaggedTranslation(toBeTranslated.name);
    case 'status':
      return statusTranslation(toBeTranslated);
    default:
      return toBeTranslated.name;
  }
};
